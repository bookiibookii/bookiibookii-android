package com.bookiibookii.bookiibookii.data.api

import android.content.Context
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

        if (accessToken.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        val newRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(newRequest)
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
