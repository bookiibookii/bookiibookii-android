package com.bookiibookii.bookiibookii.group
data class GroupJoinData(
    val id: Int,                 // 유저(신청) 고유 ID
    val profileImgUrl: String?,  // [변경] 서버 이미지 URL (없으면 null)
    val nickname: String,
    val date: String,            // ex: "2026.01.29"
    val intro: String,           // 신청 한마디
    val tags: List<String>       // [#태그1, #태그2 ...] (이미 한글로 변환된 상태로 받음)
)