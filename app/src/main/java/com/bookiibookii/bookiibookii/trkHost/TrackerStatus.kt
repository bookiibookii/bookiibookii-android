package com.bookiibookii.bookiibookii.trkHost

enum class TrackerStatus(val label: String) {
    READY("준비중"),
    HOST_READING("호스트읽는중"),
    HOST_DONE("호스트독서완료"),
    SHIPPING_TO_GUEST("게스트에게배송중"),
    RECEIVED("수령완료"),
    GUEST_READING("게스트읽는중"),
    GUEST_DONE("게스트독서완료"),
    SHIPPING_TO_HOST("회수중"),
    RETURNED("회수완료"),
    COMPLETED("릴레이종료"),
    UNKNOWN("알수없음");

    companion object {
        fun from(raw: String?): TrackerStatus {
            if (raw.isNullOrBlank()) return UNKNOWN
            return try { valueOf(raw.uppercase()) } catch (_: Exception) { UNKNOWN }
        }
    }
}
