package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

// 카카오 Local API 전용 인터셉터. 모든 요청에 REST 키 헤더를 자동으로 붙임
class KakaoAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Authorization", "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}")
            .build()
        return chain.proceed(request)
    }
}
