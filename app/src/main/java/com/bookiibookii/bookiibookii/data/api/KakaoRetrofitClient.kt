package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// 카카오 Local API 전용 Retrofit
object KakaoRetrofitClient {

    private const val BASE_URL = "https://dapi.kakao.com/"

    private val retrofit: Retrofit by lazy {
        // 릴리즈 빌드에서는 API 키 등 민감정보가 로그에 남지 않도록 비활성화
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(KakaoAuthInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 카카오 Local API 접근자
    val kakaoLocalApi: KakaoLocalApi by lazy { retrofit.create(KakaoLocalApi::class.java) }
}
