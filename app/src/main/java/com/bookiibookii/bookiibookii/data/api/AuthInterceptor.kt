package com.bookiibookii.bookiibookii.data.api

import android.content.Context
import android.content.Intent
import android.util.Log
import com.bookiibookii.bookiibookii.data.model.auth.TokenRefreshRequest
import com.bookiibookii.bookiibookii.error.ErrorActivity
import com.bookiibookii.bookiibookii.error.model.ErrorType
import com.bookiibookii.bookiibookii.onboarding.login.LoginActivity
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicLong

class AuthInterceptor(private val context: Context) : Interceptor {

    private enum class RefreshOutcome { SUCCESS, INVALID_TOKEN, NETWORK_ERROR, SYSTEM_ERROR }

    companion object {
        private const val ROUTE_COOLDOWN_MS = 3_000L
        private val lastRouteAt = AtomicLong(0L)

        fun unlockRouting() {
            lastRouteAt.set(0L)
        }

        private fun tryClaimRoute(): Boolean {
            val now = System.currentTimeMillis()
            val last = lastRouteAt.get()
            if (now - last < ROUTE_COOLDOWN_MS) return false
            return lastRouteAt.compareAndSet(last, now)
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
        val accessToken = TokenManager.getAccessToken(appContext)

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
            if (chain.call().isCanceled()) {
                throw e
            } else {
                routeComError(appContext, ErrorType.NETWORK)
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
        // 단, 서버가 "인증 문제"를 400/404로 줄 수 있으니 예외 처리
        if (code != 401) {
            if (code == 400 || code == 404) {
                val err = peekErrorBody(response)
                Log.d("AUTH_INT", "[${code}_ERR] $err")

                if (isAuthFailure(err)) {
                    // 인증 토큰이 깨졌거나(형식 오류), AccessToken이 없다는 서버 판단이면 즉시 로그아웃
                    response.close()
                    routeLogout(appContext)
                    throw IOException("Auth failed with $code")
                }
            }

            // 403 Forbidden 에러 발생 시 로그를 남기고, 필요하다면 로그아웃이나 에러 화면으로 유도할 수 있습니다.
            if (code == 403) {
                Log.e("AUTH_INT", "[403_FORBIDDEN] 서버가 접근을 거부했습니다. 권한 문제 또는 토큰 만료일 수 있습니다.")
            }

            if (code >= 500) {
                routeComError(appContext, ErrorType.SYSTEM)
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

        val refreshToken = TokenManager.getRefreshToken(appContext)

        if (refreshToken.isNullOrEmpty()) {
            response.close()
            routeLogout(appContext)
            throw IOException("Missing refresh token")
        }

        // 401 원본 응답은 반드시 닫기
        response.close()

        val outcome: RefreshOutcome = waitOrRefreshToken(appContext, refreshToken)

        return when (outcome) {
            RefreshOutcome.SUCCESS -> {
                val newAccessToken = TokenManager.getAccessToken(appContext)

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
                    if (chain.call().isCanceled()) {
                        throw e
                    } else {
                        routeComError(appContext, ErrorType.NETWORK)
                        throw e
                    }
                }
            }

            RefreshOutcome.INVALID_TOKEN -> {
                routeLogout(appContext)
                throw IOException("Invalid refresh token (refresh 400/401)")
            }

            RefreshOutcome.NETWORK_ERROR -> {
                routeComError(appContext, ErrorType.NETWORK)
                throw IOException("Network error during token refresh")
            }

            RefreshOutcome.SYSTEM_ERROR -> {
                routeComError(appContext, ErrorType.SYSTEM)
                throw IOException("System error during token refresh")
            }
        }
    }

    private fun waitOrRefreshToken(
        context: Context,
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

                val currentAccessToken = TokenManager.getAccessToken(context)
                if (currentAccessToken.isNullOrEmpty()) {
                    Log.e("AUTH_INT", "[REFRESH] accessToken is null/empty -> INVALID_TOKEN")
                    return@runBlocking RefreshOutcome.INVALID_TOKEN
                }

                val refreshRes = RetrofitClient.authApiNoAuth().postRefresh(
                    authorization = "Bearer $currentAccessToken",
                    request = TokenRefreshRequest(refreshToken)
                )

                if (refreshRes.isSuccessful && refreshRes.body()?.isSuccess == true) {
                    val result = refreshRes.body()?.result ?: return@runBlocking RefreshOutcome.SYSTEM_ERROR

                    Log.d("TOKEN_REFRESH", "새 AccessToken 발급 성공")

                    TokenManager.saveTokens(
                        context,
                        result.accessToken,
                        result.refreshToken,
                        result.userId
                    )
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

        if (!tryClaimRoute()) {
            Log.w("AUTH_ROUTE", "[SKIP] already routing in progress")
            return
        }

        TokenManager.clear(context)

        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        android.os.Handler(android.os.Looper.getMainLooper()).post {
            context.startActivity(intent)
        }
    }

    private fun routeComError(context: Context, type: ErrorType) {
        Log.e("AUTH_ROUTE", "[COM_ERROR] type=$type called")

        if (!tryClaimRoute()) return

        val intent = ErrorActivity.newIntent(context, type).apply {
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

    private fun isAuthFailure(errorBody: String): Boolean {
        if (errorBody.isBlank()) return false

        return try {
            val code = JSONObject(errorBody).optString("code", "")
            code.startsWith("AUTH")
        } catch (_: Exception) {
            false
        }
    }
}