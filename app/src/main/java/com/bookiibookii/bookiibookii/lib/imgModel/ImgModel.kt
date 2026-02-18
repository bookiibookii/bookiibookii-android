package com.bookiibookii.bookiibookii.lib.imgModel

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 1. ImgBB 응답 데이터 클래스
data class ImgBBResponse(
    val data: ImgBBData?,
    val success: Boolean,
    val status: Int
)

data class ImgBBData(
    val url: String,        // 원본 이미지 직접 링크
    val display_url: String // 웹페이지 뷰어 링크
)

// 2. ImgBB 전용 API 인터페이스
interface ImgBBApi {
    @FormUrlEncoded
    @POST("1/upload")
    suspend fun uploadImage(
        @Field("key") apiKey: String,
        @Field("image") base64Image: String
    ): Response<ImgBBResponse>
}

// 3. ImgBB 전용 Retrofit 클라이언트 (기존 우리 서버 BaseUrl과 다르기 때문)


object ImgBBRetrofitClient {
    private const val BASE_URL = "https://api.imgbb.com/"

    val api: ImgBBApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImgBBApi::class.java)
    }
}