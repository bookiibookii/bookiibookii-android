package com.bookiibookii.bookiibookii.placesearch.data

import com.bookiibookii.bookiibookii.data.api.KakaoLocalApi
import com.bookiibookii.bookiibookii.data.model.location.KakaoPlaceSearchResponse
import com.bookiibookii.bookiibookii.data.model.location.PlaceSearchPage
import com.bookiibookii.bookiibookii.data.model.location.PlaceSearchResult

// 카카오 Local 키워드 검색
class PlaceSearchRepository(
    private val api: KakaoLocalApi,
) {
    suspend fun searchPlaces(query: String, page: Int = 1): PlaceSearchPage? {
        val res = api.searchKeyword(query = query, page = page)
        if (!res.isSuccessful) return null
        return res.body()?.toPlaceSearchPage()
    }
}

private fun KakaoPlaceSearchResponse.toPlaceSearchPage(): PlaceSearchPage {
    val results = documents.mapNotNull { doc ->
        val x = doc.longitude.toDoubleOrNull() ?: return@mapNotNull null
        val y = doc.latitude.toDoubleOrNull() ?: return@mapNotNull null
        PlaceSearchResult(
            placeName = doc.placeName,
            address = doc.roadAddressName.ifBlank { doc.addressName },
            x = x,
            y = y,
        )
    }
    return PlaceSearchPage(
        results = results,
        isEnd = meta.isEnd,
        totalCount = meta.totalCount,
    )
}
