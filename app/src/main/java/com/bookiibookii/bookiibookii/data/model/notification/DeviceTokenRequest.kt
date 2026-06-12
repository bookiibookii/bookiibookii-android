package com.bookiibookii.bookiibookii.data.model.notification

// POST /api/device-tokens (Register)
data class DeviceTokenRegisterRequest(
    val token: String,
    val platform: String = "ANDROID"
)

// DELETE /api/device-tokens (Deactivate)
data class DeviceTokenDeactivateRequest(
    val token: String
)
