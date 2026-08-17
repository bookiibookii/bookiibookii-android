package com.bookiibookii.bookiibookii.data.api

import android.content.Context
import android.util.Log
import com.bookiibookii.bookiibookii.data.model.auth.TokenRefreshRequest
import com.bookiibookii.bookiibookii.error.model.ErrorType
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicLong

class AuthInterceptor(
    context: Context,
    private val tokenStore: AuthTokenStore = TokenManagerStore(context.applicationContext),
    private val refreshApi: () -> AuthApi = { RetrofitClient.authApiNoAuth() },
    private val router: AuthRouter = ActivityAuthRouter(context.applicationContext),
) : Interceptor {

    private enum class RefreshOutcome { SUCCESS, INVALID_TOKEN, NETWORK_ERROR, SYSTEM_ERROR }

    companion object {
        private const val ROUTE_COOLDOWN_MS = 3_000L
        private val lastRouteAt = AtomicLong(0L)

        fun unlockRouting() {
            lastRouteAt.set(0L)
        }

        internal fun tryClaimRoute(): Boolean {
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

        val accessToken = tokenStore.getAccessToken()

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
                router.routeComError(ErrorType.NETWORK)
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
                    logout()
                    throw IOException("Auth failed with $code")
                }
            }

            // 403 Forbidden 에러 발생 시 로그를 남기고, 필요하다면 로그아웃이나 에러 화면으로 유도할 수 있습니다.
            if (code == 403) {
                Log.e("AUTH_INT", "[403_FORBIDDEN] 서버가 접근을 거부했습니다. 권한 문제 또는 토큰 만료일 수 있습니다.")
            }

            if (code >= 500) {
                router.routeComError(ErrorType.SYSTEM)
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
            logout()
            throw IOException("Unauthorized on refresh endpoint")
        }

        // 존재 여부만 사전 확인. 실제 리프레시에 쓸 토큰은 락 획득 후 재읽기 —
        // 여기서 캡처한 값을 그대로 쓰면 다른 스레드의 리프레시(토큰 회전) 직후
        // 무효화된 옛 토큰으로 2차 리프레시를 시도해 정상 세션이 로그아웃된다.
        if (tokenStore.getRefreshToken().isNullOrEmpty()) {
            response.close()
            logout()
            throw IOException("Missing refresh token")
        }

        // 401 원본 응답은 반드시 닫기
        response.close()

        val outcome: RefreshOutcome = waitOrRefreshToken()

        return when (outcome) {
            RefreshOutcome.SUCCESS -> {
                val newAccessToken = tokenStore.getAccessToken()

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
                        router.routeComError(ErrorType.NETWORK)
                        throw e
                    }
                }
            }

            RefreshOutcome.INVALID_TOKEN -> {
                logout()
                throw IOException("Invalid refresh token (refresh 400/401)")
            }

            RefreshOutcome.NETWORK_ERROR -> {
                router.routeComError(ErrorType.NETWORK)
                throw IOException("Network error during token refresh")
            }

            RefreshOutcome.SYSTEM_ERROR -> {
                router.routeComError(ErrorType.SYSTEM)
                throw IOException("System error during token refresh")
            }
        }
    }

    private fun waitOrRefreshToken(): RefreshOutcome {

        // 이미 다른 요청이 refresh 중이면 끝날 때까지 기다리고 "그 결과"를 그대로 사용
        synchronized(refreshLock) {
            if (isRefreshing) {
                Log.d("AUTH_INT", "[WAIT] already refreshing, wait for completion")
                while (isRefreshing) {
                    try {
                        refreshLock.wait()
                    } catch (_: InterruptedException) {
                        Thread.currentThread().interrupt() // 인터럽트 플래그 복원
                    }
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

                val currentAccessToken = tokenStore.getAccessToken()
                if (currentAccessToken.isNullOrEmpty()) {
                    Log.e("AUTH_INT", "[REFRESH] accessToken is null/empty -> INVALID_TOKEN")
                    return@runBlocking RefreshOutcome.INVALID_TOKEN
                }

                // 락 획득 후 재읽기: 대기 중 다른 스레드가 회전시킨 최신 토큰을 사용
                val currentRefreshToken = tokenStore.getRefreshToken()
                if (currentRefreshToken.isNullOrEmpty()) {
                    Log.e("AUTH_INT", "[REFRESH] refreshToken is null/empty -> INVALID_TOKEN")
                    return@runBlocking RefreshOutcome.INVALID_TOKEN
                }

                val refreshRes = refreshApi().postRefresh(
                    authorization = "Bearer $currentAccessToken",
                    request = TokenRefreshRequest(currentRefreshToken)
                )

                if (refreshRes.isSuccessful && refreshRes.body()?.isSuccess == true) {
                    val result = refreshRes.body()?.result ?: return@runBlocking RefreshOutcome.SYSTEM_ERROR

                    Log.d("TOKEN_REFRESH", "새 AccessToken 발급 성공")

                    tokenStore.saveTokens(
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

    // 토큰 삭제는 라우팅 쿨다운과 무관하게 항상 수행한다.
    // 라우터에 맡기면 쿨다운에 걸린 두 번째 로그아웃 사유에서 토큰이 살아남는다.
    private fun logout() {
        tokenStore.clear()
        router.routeLogout()
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