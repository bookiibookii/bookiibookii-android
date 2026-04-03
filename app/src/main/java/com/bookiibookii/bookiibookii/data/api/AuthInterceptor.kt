package com.bookiibookii.bookiibookii.data.api

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.bookiibookii.bookiibookii.common.ComErrorActivity
import com.bookiibookii.bookiibookii.data.model.TokenRefreshRequest
import com.bookiibookii.bookiibookii.onboarding.login.LoginActivity
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicBoolean

class AuthInterceptor(private val context: Context) : Interceptor {

    private enum class RefreshOutcome { SUCCESS, INVALID_TOKEN, NETWORK_ERROR, SYSTEM_ERROR }

    companion object {
        private val isRouting = AtomicBoolean(false)

        fun unlockRouting() {
            isRouting.set(false)
        }
    }

    private val refreshLock = Object()
    @Volatile private var isRefreshing = false

    // ✅ 대기 중인 요청들도 refresh 결과를 동일하게 받도록 공유
    @Volatile private var lastRefreshOutcome: RefreshOutcome? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        //TODO: 추후 로그 삭제 예정
        val originalUrl = originalRequest.url.toString()
        val originalMethod = originalRequest.method
        Log.d("AUTH_INT", "[ENTER] $originalMethod $originalUrl")

        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

        val accessToken = prefs.getString("access_token", null)

        //TODO: 추후 로그 삭제 예정
        Log.d(
            "AUTH_INT",
            "[TOKEN] accessToken=${accessToken?.take(10)}... isNullOrEmpty=${accessToken.isNullOrEmpty()}"
        )

        val authedRequest = if (accessToken.isNullOrEmpty()) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        }

        val response = try {
            chain.proceed(authedRequest)
        } catch (e: IOException) {
            routeComError(appContext, ComErrorActivity.TYPE_NETWORK_ERROR)
            throw e
        }

        //TODO: 추후 로그 삭제 예정
        Log.d("AUTH_INT", "[RESP] code=${response.code} ${authedRequest.method} ${authedRequest.url}")

        val url = authedRequest.url.toString()
        val method = authedRequest.method

        if (response.isSuccessful) {
            Log.i("API_SUCCESS", "✅ [${response.code}] $method $url")
            return response
        } else {
            Log.e("API_FAILURE", "❌ [${response.code}] $method $url")
        }

        val code = response.code

// 401이 아니면 그대로 반환 (+ 5xx면 ComError 라우팅)
// 단, 서버가 "인증 문제"를 400으로 줄 수 있으니 400은 예외 처리
        if (code != 401) {
            if (code == 400) {
                val err = peekErrorBody(response)
                Log.d("AUTH_INT", "[400_ERR] $err")

                if (isAuthFailure400(err)) {
                    // 인증 토큰이 깨졌거나(형식 오류), AccessToken이 없다는 서버 판단이면 즉시 로그아웃
                    response.close()
                    routeLogout(appContext)
                    throw IOException("Auth failed with 400")
                }
            }

            if (code >= 500) {
                routeComError(appContext, ComErrorActivity.TYPE_SYSTEM_ERROR)
            }

            return response
        }

        //TODO: 추후 로그 삭제 예정
        Log.w("AUTH_INT", "[401] detected at ${authedRequest.method} ${authedRequest.url}")

        // ✅ refresh 자신이 401이면 루프 방지 (경로 비교로 정확히)
        val path = authedRequest.url.encodedPath
        if (path == "/api/auth/refresh") {
            Log.e("AUTH_INT", "[401] refresh endpoint got 401 -> routeLogout")
            response.close()
            routeLogout(appContext)
            throw IOException("Unauthorized on refresh endpoint")
        }

        val refreshToken = prefs.getString("refresh_token", null)

        //TODO: 추후 로그 삭제 예정
        Log.d(
            "AUTH_INT",
            "[RT] refreshToken=${refreshToken?.take(10)}... isNullOrEmpty=${refreshToken.isNullOrEmpty()}"
        )

        if (refreshToken.isNullOrEmpty()) {
            response.close()
            routeLogout(appContext)
            throw IOException("Missing refresh token")
        }

        // ✅ 401 원본 응답은 반드시 닫기
        response.close()

        val outcome: RefreshOutcome = waitOrRefreshToken(prefs, refreshToken)

        return when (outcome) {
            RefreshOutcome.SUCCESS -> {
                val newAccessToken = prefs.getString("access_token", null)

                val retryRequest = if (newAccessToken.isNullOrEmpty()) {
                    originalRequest
                } else {
                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()
                }

                //TODO: 추후 로그 삭제 예정
                Log.d(
                    "AUTH_INT",
                    "[RETRY] with accessToken=${newAccessToken?.take(10)}... url=${retryRequest.url}"
                )

                try {
                    val retryRes = chain.proceed(retryRequest)
                    Log.d("AUTH_INT", "[RETRY_RESP] code=${retryRes.code} ${retryRequest.method} ${retryRequest.url}")
                    retryRes
                } catch (e: IOException) {
                    routeComError(appContext, ComErrorActivity.TYPE_NETWORK_ERROR)
                    throw e
                }
            }

            RefreshOutcome.INVALID_TOKEN -> {
                routeLogout(appContext)
                throw IOException("Invalid refresh token (refresh 400/401)")
            }

            RefreshOutcome.NETWORK_ERROR -> {
                routeComError(appContext, ComErrorActivity.TYPE_NETWORK_ERROR)
                throw IOException("Network error during token refresh")
            }

            RefreshOutcome.SYSTEM_ERROR -> {
                routeComError(appContext, ComErrorActivity.TYPE_SYSTEM_ERROR)
                throw IOException("System error during token refresh")
            }
        }
    }

    private fun waitOrRefreshToken(
        prefs: SharedPreferences,
        refreshToken: String
    ): RefreshOutcome {

        // ✅ 이미 다른 요청이 refresh 중이면 끝날 때까지 기다리고 "그 결과"를 그대로 사용
        synchronized(refreshLock) {
            if (isRefreshing) {
                Log.d("AUTH_INT", "[WAIT] already refreshing, wait for completion")
                while (isRefreshing) {
                    try { refreshLock.wait() } catch (_: InterruptedException) {}
                }
                val shared = lastRefreshOutcome ?: RefreshOutcome.SYSTEM_ERROR
                Log.d("AUTH_INT", "[WAIT_DONE] sharedOutcome=$shared")
                return shared
            }
            isRefreshing = true
            lastRefreshOutcome = null
        }

        val outcome = runBlocking {
            try {
                Log.d("AUTH_INT", "[REFRESH] call refresh api (noAuth client)")

                val currentAccessToken = prefs.getString("access_token", null)
                if (currentAccessToken.isNullOrEmpty()) {
                    Log.e("AUTH_INT", "[REFRESH] accessToken is null/empty -> INVALID_TOKEN")
                    return@runBlocking RefreshOutcome.INVALID_TOKEN
                }

                // ✅ 요청 직전 값 확정 로그
                Log.d("AUTH_INT", "[REFRESH_REQ] auth=Bearer ${currentAccessToken.take(20)}...")
                Log.d("AUTH_INT", "[REFRESH_REQ] rt=${refreshToken.take(20)}...")

                val refreshRes = RetrofitClient.apiNoAuth().postRefresh(
                    authorization = "Bearer $currentAccessToken",
                    request = TokenRefreshRequest(refreshToken)
                )

                Log.d(
                    "AUTH_INT",
                    "[REFRESH] http=${refreshRes.code()} isSuccessful=${refreshRes.isSuccessful} bodySuccess=${refreshRes.body()?.isSuccess}"
                )
                Log.d("AUTH_INT", "[REFRESH] body=${refreshRes.body()}")

                val err = try { refreshRes.errorBody()?.string() } catch (_: Exception) { null }
                Log.d("AUTH_INT", "[REFRESH_ERR] code=${refreshRes.code()} errorBody=$err")

                if (refreshRes.isSuccessful && refreshRes.body()?.isSuccess == true) {
                    val result = refreshRes.body()?.result ?: return@runBlocking RefreshOutcome.SYSTEM_ERROR

                    Log.d("TOKEN_REFRESH", "새 AccessToken 발급 성공")

                    prefs.edit {
                        putString("access_token", result.accessToken)
                        putString("refresh_token", result.refreshToken)
                        putInt("user_id", result.userId)
                    }

                    val savedAT = prefs.getString("access_token", null)
                    val savedRT = prefs.getString("refresh_token", null)
                    Log.d("AUTH_INT", "[SAVE] access=${savedAT?.take(10)}... refresh=${savedRT?.take(10)}...")

                    return@runBlocking RefreshOutcome.SUCCESS
                }

                return@runBlocking when (refreshRes.code()) {
                    400, 401 -> RefreshOutcome.INVALID_TOKEN
                    else -> RefreshOutcome.SYSTEM_ERROR
                }

            } catch (_: SocketTimeoutException) {
                RefreshOutcome.NETWORK_ERROR
            } catch (_: IOException) {
                RefreshOutcome.NETWORK_ERROR
            } catch (_: Exception) {
                RefreshOutcome.SYSTEM_ERROR
            }
        }

        // ✅ 결과 공유 + notifyAll
        synchronized(refreshLock) {
            lastRefreshOutcome = outcome
            isRefreshing = false
            refreshLock.notifyAll()
        }

        return outcome
    }

    private fun routeLogout(context: Context) {
        Log.e("AUTH_ROUTE", "[LOGOUT] routeLogout called")

        if (!isRouting.compareAndSet(false, true)) {
            Log.w("AUTH_ROUTE", "[SKIP] already routing in progress")
            return
        }

        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit { clear() }

        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        android.os.Handler(android.os.Looper.getMainLooper()).post {
            context.startActivity(intent)
        }
    }

    private fun routeComError(context: Context, type: Int) {
        Log.e("AUTH_ROUTE", "[COM_ERROR] type=$type called")

        if (!isRouting.compareAndSet(false, true)) return

        val intent = ComErrorActivity.newIntent(context, type).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        android.os.Handler(android.os.Looper.getMainLooper()).post {
            context.startActivity(intent)
        }
    }

    private fun peekErrorBody(response: Response, maxBytes: Long = 1024 * 1024): String {
        return try {
            response.peekBody(maxBytes).string()
        } catch (_: Exception) {
            ""
        }
    }

    private fun isAuthFailure400(errorBody: String): Boolean {
        if (errorBody.isBlank()) return false

        // 서버 응답이 {"code":"AUTH404_2", "message":"AccessToken을 찾을 수 없습니다."} 같은 형태라면 여기서 잡힘
        return errorBody.contains("\"code\":\"AUTH", ignoreCase = true) ||
                errorBody.contains("AUTH", ignoreCase = true) ||
                errorBody.contains("AccessToken", ignoreCase = true) ||
                errorBody.contains("access token", ignoreCase = true) ||
                errorBody.contains("refresh", ignoreCase = true) && errorBody.contains("token", ignoreCase = true) ||
                errorBody.contains("토큰", ignoreCase = true)
    }
}