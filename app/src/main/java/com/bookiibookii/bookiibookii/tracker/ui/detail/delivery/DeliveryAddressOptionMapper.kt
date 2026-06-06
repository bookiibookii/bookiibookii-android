package com.bookiibookii.bookiibookii.tracker.ui.detail.delivery

import com.bookiibookii.bookiibookii.data.model.location.DeliveryAddress
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressItemDTO

// 마이페이지 등록 배송지 → 다이얼로그 "나의 배송지" 항목
fun DeliveryAddress.toDeliveryAddressOption(): DeliveryAddressOption {
    return DeliveryAddressOption(
        userDeliveryId = id,
        title = placeName,
        address = address,
        addressDetail = addressDetail.orEmpty(),
        zipCode = zipCode,
    )
}

// 현재 교환 myAddress와 같은 주소를 가진 등록 배송지의 userDeliveryId 찾기
// 스냅샷에 userDeliveryId가 없어 zipCode+address+addressDetail로 매칭
fun List<DeliveryAddress>.matchUserDeliveryId(snapshot: DeliveryAddressItemDTO?): Long? {
    if (snapshot == null) return null
    return firstOrNull { saved ->
        saved.zipCode == snapshot.zipCode.orEmpty() &&
            saved.address == snapshot.address.orEmpty() &&
            saved.addressDetail.orEmpty() == snapshot.addressDetail.orEmpty()
    }?.id
}
