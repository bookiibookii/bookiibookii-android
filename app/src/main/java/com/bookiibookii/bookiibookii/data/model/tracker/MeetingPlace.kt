package com.bookiibookii.bookiibookii.data.model.tracker

import com.bookiibookii.bookiibookii.data.model.location.ExchangeAddress
import com.bookiibookii.bookiibookii.data.model.location.PlaceSearchResult

// 약속 등록에 필요한 장소 정보
// (id/isDefault 같은 저장 레코드 전용 필드는 담지 않음)
data class MeetingPlace(
    val placeName: String,
    val address: String,
    val zipCode: String?,   // 카카오 키워드 검색은 우편번호 미제공 → null
    val x: Double,
    val y: Double,
)

// 저장된 희망교환장소
fun ExchangeAddress.toMeetingPlace() = MeetingPlace(
    placeName = placeName,
    address = address,
    zipCode = zipCode,
    x = x,
    y = y,
)

// 카카오 검색 결과 → 약속 장소
fun PlaceSearchResult.toMeetingPlace() = MeetingPlace(
    placeName = placeName,
    address = address,
    zipCode = null,
    x = x,
    y = y,
)
