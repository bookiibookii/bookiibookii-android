package com.bookiibookii.bookiibookii.data.model

import com.google.gson.annotations.SerializedName

// 전체 응답
data class MypageResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: MypageResult
)

// 메인 데이터
data class MypageResult(
    val userId: Int,
    val userImage: UserImageInfo?, // 프로필 이미지 & 태그 정보 포함
    val nickname: String,
    val manner: Double,
    val topTags: List<String>,     // 획득한 후기 (가로 스크롤)
    val completeBook: Int,         // 완독 수
    val relayGroup: Int,
    val togetherGroup: Int,
    val userBadge: List<UserBadge>?,
    val groups: List<MypageGroup>, // 주최한 그룹
    val books: List<MypageBook>    // 최근 읽은 책
)

// 프로필 이미지 및 유저 상세
data class UserImageInfo(
    val s3Key: String?,
    val user: UserDetail?
)

data class UserDetail(
    val id: Int,
    val name: String?,       // 이름
    val meetPlace: String?,  // 활동 지역
    val region: String?,
    val userTags: List<UserTagWrapper>?,

    // ▼ [추가] 조회 시 없으면 null로 처리됨
    // 서버가 나중에 이 필드들을 내려주면 그때 자동으로 매핑됨
    val phone: String? = null,
    val zipCode: String? = null,
    val address: String? = null,
    val addressDetail: String? = null
)

data class UserTagWrapper(
    val tag: TagDetail?
)

data class TagDetail(
    val code: String // 태그 이름 (ex: "인사이트")
)

// 그룹 정보
data class MypageGroup(
    val groupId: Int,
    val bookTitle: String,
    val auth: String,
    val GENRE: String,
    val group_status: String,
    val groupTags: List<String>
)

// 책 정보
data class MypageBook(
    val bookTitle: String,
    val rating: Double
)

//
data class UserBadge(
    val userBadge : String,
    val count : Int
)