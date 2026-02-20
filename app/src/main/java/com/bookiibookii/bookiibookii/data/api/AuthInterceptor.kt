package com.bookiibookii.bookiibookii.data.api

import android.content.Context
import android.content.Intent
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

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

        val accessToken = prefs.getString("access_token", null)

        val newRequest = if (accessToken.isNullOrEmpty()) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        }

        // proceed 자체가 네트워크 예외로 터질 수 있음
        val response = try {
            chain.proceed(newRequest)
        } catch (e: IOException) {
            routeComError(appContext, ComErrorActivity.TYPE_NETWORK_ERROR)
            throw e
        }

        val url = newRequest.url.toString()
        val method = newRequest.method

        if (response.isSuccessful) {
            Log.i("API_SUCCESS", "✅ [${response.code}] $method $url")
            return response
        } else {
            Log.e("API_FAILURE", "❌ [${response.code}] $method $url")
        }

        if (response.code == 401) {

            // refresh 요청 자체가 401이면 재시도 루프 방지 → 로그아웃 라우팅
            if (url.contains("/api/auth/refresh")) {
                response.close()
                routeLogout(appContext)

                // ⚠️ close된 response를 반환하지 않음 (호출부가 사용하면 문제 생길 수 있음)
                throw IOException("Unauthorized on refresh endpoint")
            }

            val refreshToken = prefs.getString("refresh_token", null)
            if (refreshToken.isNullOrEmpty()) {
                response.close()
                routeLogout(appContext)
                throw IOException("Missing refresh token")
            }

            // 기존 401 응답은 여기서 소비(닫기)
            response.close()

            val outcome = runBlocking {
                try {
                    val refreshRes = RetrofitClient.apiNoAuth().postRefresh(
                        TokenRefreshRequest(refreshToken)
                    )

                    if (refreshRes.isSuccessful && refreshRes.body()?.isSuccess == true) {
                        val result = refreshRes.body()?.result
                            ?: return@runBlocking RefreshOutcome.SYSTEM_ERROR

                        Log.d("TOKEN_REFRESH", "새 AccessToken 발급 성공")

                        prefs.edit {
                            putString("access_token", result.accessToken)
                            putString("refresh_token", result.refreshToken)
                            putInt("user_id", result.userId)
                        }

                        return@runBlocking RefreshOutcome.SUCCESS
                    }

                    val code = refreshRes.code()
                    return@runBlocking when {
                        code == 400 || code == 401 -> RefreshOutcome.INVALID_TOKEN
                        code >= 500 -> RefreshOutcome.SYSTEM_ERROR
                        else -> RefreshOutcome.SYSTEM_ERROR
                    }

                } catch (e: SocketTimeoutException) {
                    RefreshOutcome.NETWORK_ERROR
                } catch (e: IOException) {
                    RefreshOutcome.NETWORK_ERROR
                } catch (e: Exception) {
                    RefreshOutcome.SYSTEM_ERROR
                }
            }

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

                    // retry도 네트워크 예외가 날 수 있으니 동일하게 처리
                    try {
                        chain.proceed(retryRequest)
                    } catch (e: IOException) {
                        routeComError(appContext, ComErrorActivity.TYPE_NETWORK_ERROR)
                        throw e
                    }
                }

                RefreshOutcome.INVALID_TOKEN -> {
                    routeLogout(appContext)
                    throw IOException("Invalid refresh token")
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

        if (response.code >= 500) {
            routeComError(appContext, ComErrorActivity.TYPE_SYSTEM_ERROR)
            return response
        }

        return response
    }

    private fun routeLogout(context: Context) {
        if (!isRouting.compareAndSet(false, true)) return

        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit { clear() }

        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        android.os.Handler(android.os.Looper.getMainLooper()).post {
            context.startActivity(intent)
            // 여기서는 unlock 하지 않음 (LoginActivity에서 풀 것)
        }
    }

    private fun routeComError(context: Context, type: Int) {
        if (!isRouting.compareAndSet(false, true)) return

        val intent = ComErrorActivity.newIntent(context, type).apply {
            // 추천: 에러 화면도 스택 정리해서 꼬임 방지
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        android.os.Handler(android.os.Looper.getMainLooper()).post {
            context.startActivity(intent)
            // 여기서는 unlock 하지 않음 (ComErrorActivity에서 풀 것)
        }
    }
}