package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.location.KakaoPlaceSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface KakaoLocalApi {

    // 키워드로 장소 검색 (장소명 + 주소 동시 검색)
    @GET("v2/local/search/keyword.json")
    suspend fun searchKeyword(
        @Query("query") query: String,
        @Query("page") page: Int = 1,    // 1~45
        @Query("size") size: Int = 15,   // 1~15
    ): Response<KakaoPlaceSearchResponse>
}
