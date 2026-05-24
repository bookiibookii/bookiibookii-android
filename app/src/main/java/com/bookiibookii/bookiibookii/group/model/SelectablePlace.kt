package com.bookiibookii.bookiibookii.group.model

// 주소 선택 카드용 공통 UI 모델. ExchangeAddress/DeliveryAddress를 공통으로 표현
data class SelectablePlace(
    val id: Long,
    val placeName: String,
    val address: String,
    val isDefault: Boolean,
)
