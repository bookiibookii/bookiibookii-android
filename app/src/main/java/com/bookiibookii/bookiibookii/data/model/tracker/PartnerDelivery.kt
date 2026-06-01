package com.bookiibookii.bookiibookii.data.model.tracker

// GET /api/groups/{groupId}/deliveries/partner — 상대방이 나에게 보낸 운송장 정보
data class PartnerDeliveryResponseDTO(
    val deliveryId: String?,
    val deliveryCompany: String?,
    val deliveryCompanyName: String?,
    val trackingNumber: String?,
    val registeredAt: String?,
    val canConfirmReceived: Boolean?,
)
