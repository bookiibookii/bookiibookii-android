package com.bookiibookii.bookiibookii.error.model
enum class ErrorType {
    SYSTEM,        // 시스템 장애 — Error 일러스트, [이전 · 다시 시도]
    NETWORK,       // 네트워크 오류 — Error 일러스트, [이전 · 다시 시도]
    NO_PERMISSION, // 접근 권한 없음 — 404 일러스트, [메인으로 이동]
    GROUP_DELETED, // 삭제된 페이지 — 404 일러스트, [메인으로 이동]
    GROUP_CLOSED,  // 종료된 그룹 — 404 일러스트, [메인으로 이동]
}
