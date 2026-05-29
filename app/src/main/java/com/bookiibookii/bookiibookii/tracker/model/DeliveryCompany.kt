package com.bookiibookii.bookiibookii.tracker.model

// UI 라벨 ↔ API enum 매핑. 백엔드 enum이 확장되면 항목 추가.
enum class DeliveryCompany(val apiValue: String, val label: String) {
    CJ_LOGISTICS("CJ_LOGISTICS", "CJ대한통운"),
    HANJIN("HANJIN", "한진택배"),
    LOTTE("LOTTE", "롯데택배"),
    POST_OFFICE("POST_OFFICE", "우체국택배"),
    LOGEN("LOGEN", "로젠택배"),
    CU("CU", "CU편의점택배"),
    GS("GS", "GS25편의점택배"),
}
