package com.bookiibookii.bookiibookii.bookData.API

import retrofit2.http.GET
import retrofit2.http.Query

interface AladinAPI {

    @GET("ItemSearch.aspx")
    suspend fun searchBooks(
        @Query("ttbkey") ttbKey: String,
        @Query("Query") query: String,
        @Query("QueryType") queryType: String = "Title",
        @Query("MaxResults") maxResults: Int = 10,
        @Query("start") start: Int = 1,
        @Query("SearchTarget") searchTarget: String = "Book",
        @Query("Output") output: String = "JS",
        @Query("Version") version: String = "20131101"
    ): AladinSearchResponse
}
