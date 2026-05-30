package com.bookiibookii.bookiibookii.data.model.location

import com.google.gson.annotations.SerializedName

// 카카오 Local 키워드 검색(keyword.json) 응답
data class KakaoPlaceSearchResponse(
    @SerializedName("documents") val documents: List<KakaoPlaceDocument>,
    @SerializedName("meta") val meta: KakaoPlaceMeta,
)

data class KakaoPlaceDocument(
    @SerializedName("place_name") val placeName: String,
    @SerializedName("address_name") val addressName: String,
    @SerializedName("road_address_name") val roadAddressName: String,
    // x = 경도(longitude), y = 위도(latitude). 카카오는 문자열로 내려줌
    @SerializedName("x") val longitude: String,
    @SerializedName("y") val latitude: String,
)

data class KakaoPlaceMeta(
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("is_end") val isEnd: Boolean,
)
