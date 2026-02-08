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

// AuthInterceptor.kt 내부 수정

        if (response.code == 401 || response.code == 500) {
            if (url.contains("api/auth/refresh")) {
                response.close()
                performLogout(appContext)
                return response
            }

            Log.d("DEBUG_TOKEN", "⚠️ 401 발생! 리프레시 시도 시작")

            // 1. 저장된 리프레시 토큰 확인
            val refreshToken = prefs.getString("refresh_token", null)
            Log.d("DEBUG_TOKEN", "👉 저장된 리프레시 토큰 값: $refreshToken")

            if (!refreshToken.isNullOrEmpty()) {
                response.close() // 기존 응답 닫기

                try {
                    // 2. 서버로 갱신 요청 전송
                    Log.d("DEBUG_TOKEN", "👉 리프레시 API 호출 시도...")
                    val call = RetrofitClient.api().refreshToken(TokenRefreshRequest(refreshToken))
                    val refreshResponse = call.execute() // 동기 호출

                    // 3. 결과 확인
                    Log.d("DEBUG_TOKEN", "👉 리프레시 API 응답 코드: ${refreshResponse.code()}")

                    if (refreshResponse.isSuccessful && refreshResponse.body()?.isSuccess == true) {
                        val newTokens = refreshResponse.body()!!.result
                        if (newTokens != null) {
                            Log.i("DEBUG_TOKEN", "✅ 갱신 성공! 새 토큰으로 교체")

                            prefs.edit().apply {
                                putString("access_token", newTokens.accessToken)
                                putString("refresh_token", newTokens.refreshToken)
                                putInt("user_id", newTokens.userId)
                                apply()
                            }

                            val newRequest = originalRequest.newBuilder()
                                .removeHeader("Authorization")
                                .addHeader("Authorization", "Bearer ${newTokens.accessToken}")
                                .build()

                            return chain.proceed(newRequest)
                        } else {
                            Log.e("DEBUG_TOKEN", "❌ 성공은 했으나 result가 null임")
                        }
                    } else {
                        // 여기가 중요합니다! 왜 실패했는지 서버 에러 메시지를 까봅니다.
                        val errorBody = refreshResponse.errorBody()?.string()
                        Log.e("DEBUG_TOKEN", "❌ 리프레시 실패. 이유: $errorBody")
                    }

                } catch (e: Exception) {
                    Log.e("DEBUG_TOKEN", "❌ 네트워크/코드 에러: ${e.message}")
                    e.printStackTrace()
                }
            } else {
                Log.e("DEBUG_TOKEN", "❌ 저장소에 리프레시 토큰이 없음 (null)")
            }

            // 위에서 return 못 하고 여기까지 왔으면 실패한 것임
            Log.e("AuthInterceptor", "🚫 토큰 갱신 최종 실패. 강제 로그아웃")
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
