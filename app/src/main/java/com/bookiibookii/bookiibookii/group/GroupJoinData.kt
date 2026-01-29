package com.bookiibookii.bookiibookii.group
data class GroupJoinData(
val id: Int,                 // 유저 고유 ID
val profileResId: Int?,      // 프로필 이미지 리소스 (URL이라면 String으로 변경)
val nickname: String,        // 닉네임
val date: String,            // 날짜 (ex: "2026.01.29")
val intro: String,           // 간단 소개
val tags: List<String>       // 태그 리스트 (ex: ["#열정", "#성실"])
)