package com.bookiibookii.bookiibookii.group.main

data class GroupData(
    val groupId : Int,
    val coverImgUrl: String,      // 책 표지 URL
    val bookTitle: String,        // 책 제목
    val bookAuthor: String,       // 저자
    val genre: String,        // 장르 (ex: 소설)
    val status: String,           // 모집 상태 (ex: 모집 중)
    val readingPeriod: String,         // 마감일 숫자 (ex: 7)
    val memberCount: String,      // 현재 멤버 수 (ex: 5)
    val isHot: Boolean,           // HOT 태그 표시 여부 (true/false)
    val profileImgUrl: String,    // 프로필 이미지 URL
    val nickname: String,         // 닉네임
    val date: String,             // 등록 날짜
    val tags: List<String>,        // 해시태그 리스트
    val customTag : String?,
    val groupType: String
)