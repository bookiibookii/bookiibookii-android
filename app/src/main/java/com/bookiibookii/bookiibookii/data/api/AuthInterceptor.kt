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
import java.io.InterruptedIOException
import java.net.SocketException
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

    // 대기 중인 요청들도 refresh 결과를 동일하게 받도록 공유
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
            // ★ 수정된 핵심 로직: 코루틴 취소로 인한 Exception인지 판단합니다.
            val isCanceled = e is InterruptedIOException ||
                    e is SocketException ||
                    e.message?.contains("Canceled", ignoreCase = true) == true ||
                    e.message?.contains("Socket closed", ignoreCase = true) == true

            if (isCanceled) {
                // 사용자가 화면을 닫아서 발생한 정상적인 취소이므로 에러 화면을 띄우지 않고 조용히 throw 합니다.
                Log.d("AUTH_INT", "[CANCELED] Request was canceled by user/lifecycle: ${e.message}")
                throw e
            } else {
                // 진짜 통신 에러일 경우에만 에러 화면을 띄웁니다.
                routeComError(appContext, ComErrorActivity.TYPE_NETWORK_ERROR)
                throw e
            }
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

            // 403 Forbidden 에러 발생 시 로그를 남기고, 필요하다면 로그아웃이나 에러 화면으로 유도할 수 있습니다.
            if (code == 403) {
                Log.e("AUTH_INT", "[403_FORBIDDEN] 서버가 접근을 거부했습니다. 권한 문제 또는 토큰 만료일 수 있습니다.")
            }

            if (code >= 500) {
                routeComError(appContext, ComErrorActivity.TYPE_SYSTEM_ERROR)
            }

            return response
        }

        //TODO: 추후 로그 삭제 예정
        Log.w("AUTH_INT", "[401] detected at ${authedRequest.method} ${authedRequest.url}")

        // refresh 자신이 401이면 루프 방지 (경로 비교로 정확히)
        val path = authedRequest.url.encodedPath
        if (path == "/api/auth/refresh") {
            Log.e("AUTH_INT", "[401] refresh endpoint got 401 -> routeLogout")
            response.close()
            routeLogout(appContext)
            throw IOException("Unauthorized on refresh endpoint")
        }

        val refreshToken = prefs.getString("refresh_token", null)

        if (refreshToken.isNullOrEmpty()) {
            response.close()
            routeLogout(appContext)
            throw IOException("Missing refresh token")
        }

        // 401 원본 응답은 반드시 닫기
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

                try {
                    val retryRes = chain.proceed(retryRequest)
                    Log.d("AUTH_INT", "[RETRY_RESP] code=${retryRes.code} ${retryRequest.method} ${retryRequest.url}")
                    retryRes
                } catch (e: IOException) {
                    // ★ 여기도 마찬가지로 취소 예외 처리 적용
                    val isCanceled = e is InterruptedIOException ||
                            e is SocketException ||
                            e.message?.contains("Canceled", ignoreCase = true) == true ||
                            e.message?.contains("Socket closed", ignoreCase = true) == true
                    if (isCanceled) {
                        throw e
                    } else {
                        routeComError(appContext, ComErrorActivity.TYPE_NETWORK_ERROR)
                        throw e
                    }
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

        // 이미 다른 요청이 refresh 중이면 끝날 때까지 기다리고 "그 결과"를 그대로 사용
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

                val refreshRes = RetrofitClient.apiNoAuth().postRefresh(
                    authorization = "Bearer $currentAccessToken",
                    request = TokenRefreshRequest(refreshToken)
                )

                if (refreshRes.isSuccessful && refreshRes.body()?.isSuccess == true) {
                    val result = refreshRes.body()?.result ?: return@runBlocking RefreshOutcome.SYSTEM_ERROR

                    Log.d("TOKEN_REFRESH", "새 AccessToken 발급 성공")

                    prefs.edit {
                        putString("access_token", result.accessToken)
                        putString("refresh_token", result.refreshToken)
                        putInt("user_id", result.userId)
                    }
                    return@runBlocking RefreshOutcome.SUCCESS
                }

                return@runBlocking when (refreshRes.code()) {
                    400, 401, 404 -> RefreshOutcome.INVALID_TOKEN
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

        // 결과 공유 + notifyAll
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

        return errorBody.contains("\"code\":\"AUTH", ignoreCase = true) ||
                errorBody.contains("AUTH", ignoreCase = true) ||
                errorBody.contains("AccessToken", ignoreCase = true) ||
                errorBody.contains("access token", ignoreCase = true) ||
                errorBody.contains("refresh", ignoreCase = true) && errorBody.contains("token", ignoreCase = true) ||
                errorBody.contains("토큰", ignoreCase = true)
    }
}