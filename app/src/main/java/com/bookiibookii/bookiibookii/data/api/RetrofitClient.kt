package com.bookiibookii.bookiibookii.data.api

import android.content.Context
import android.content.Intent
import android.util.Log
import com.bookiibookii.bookiibookii.data.model.TokenRefreshRequest
import com.bookiibookii.bookiibookii.onboarding.login.LoginActivity
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// 인터셉터: 로그인 직후에도 바로 토큰을 인식 가능
class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 수정 포인트: 앱 전체 컨텍스트 사용 (안전성 확보)
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("access_token", null)

        val newRequest = if (accessToken.isNullOrEmpty()) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        }

        val response = chain.proceed(newRequest)

        // 성공/실패 로그 출력 로직 ★★★
        val url = newRequest.url.toString()
        val method = newRequest.method

        if (response.isSuccessful) {
            // 200번대 (성공)
            Log.i("API_SUCCESS", "✅ [${response.code}] $method $url")
        } else {
            // 400, 500번대 (실패)
            Log.e("API_FAILURE", "❌ [${response.code}] $method $url")
        }

        if (response.code == 401) {
            if (url.contains("api/auth/refresh")) {
                response.close()
                performLogout(appContext)
                return response
            }
            Log.d("AuthInterceptor", "⚠️ 401 토큰 만료됨. 리프레시 토큰으로 갱신 시도...")
            val refreshToken = prefs.getString("refresh_token", null)
            if (refreshToken != null) {
                // 기존의 실패한 응답은 닫아줘야 리소스가 안 샙니다.
                response.close()

                try {
                    val refreshResponse = RetrofitClient.api().refreshToken(
                        TokenRefreshRequest(
                            refreshToken
                        )
                    ).execute()

                    if (refreshResponse.isSuccessful && refreshResponse.body()?.isSuccess == true) {
                        val newTokens = refreshResponse.body()!!.result

                        if (newTokens != null) {
                            Log.i("API_REFRESH", "♻️ 토큰 갱신 성공! 재요청 진행")

                            // (1) 새 토큰 저장
                            prefs.edit().apply {
                                putString("access_token", newTokens.accessToken)
                                putString("refresh_token", newTokens.refreshToken)
                                putInt("user_id", newTokens.userId)
                                apply()
                            }

                            // (2) 실패했던 요청의 헤더를 새 토큰으로 교체
                            val newRequest = originalRequest.newBuilder()
                                .removeHeader("Authorization")
                                .addHeader("Authorization", "Bearer ${newTokens.accessToken}")
                                .build()

                            // (3) 재전송 (사용자는 에러를 모름)
                            return chain.proceed(newRequest)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("API_REFRESH", "토큰 갱신 중 에러 발생: ${e.message}")
                }
            }

            // 리프레시 토큰이 없거나, 갱신 실패 시 -> 강제 로그아웃
            Log.e("AuthInterceptor", "🚫 토큰 갱신 실패. 강제 로그아웃")
            performLogout(appContext)
        }
        return response
    }
    private fun performLogout(context: Context) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}

object RetrofitClient {
    private const val BASE_URL = "https://bookii.gyeonseo.com/"

    @Volatile private var apiService: ApiService? = null

    fun init(context: Context) {
        if (apiService != null) return

        synchronized(this) {
            if (apiService != null) return

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(AuthInterceptor(context.applicationContext))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }
    }

    fun api(): ApiService =
        apiService ?: error("RetrofitClient.init(context) 먼저 호출해야 함")
}
