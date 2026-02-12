package com.bookiibookii.bookiibookii.data.model

data class ProfileResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: ProfileResult?
)

data class ProfileResult(
    val userId: Int,
    val profileImageUrl: String?, // 프로필 이미지 등
    val nickname: String,
    val manner: Double,
    val topTags: List<String>,     // 획득한 후기
    val completeBook: Int,         // 완독 수
    val relayGroup: Int,
    val togetherGroup: Int,
    val userBadge: List<UserBadge>?,
    val groups: List<MypageGroup>?, // 주최한 그룹 (null 가능성 대비)
    val books: List<MypageBook>?,   // 최근 읽은 책 (null 가능성 대비)

    // ▼▼▼ [새로 추가된 필드: DTO 최상단으로 이동함] ▼▼▼
    val receiverName: String?,  // 수령인 이름
    val phone: String?,         // 전화번호
    val zipCode: String?,       // 우편번호
    val address: String?,       // 주소
    val addressDetail: String?, // 상세주소
    val region: String?,        // 활동 지역 (시/도 시/군/구)
    val meetPlace: String?      // 교환 희망 장소
)

// 프로필 이미지 및 유저 상세
//data class UserImageInfo(
//    val s3Key: String?,
//    val user: MypageUserDetail?
//)

data class MypageUserDetail(
    val id: Int,
    @SerializedName("nickName") val nickName: String?, // 내부 닉네임 (JSON 키: nickName)
    val meetPlace: String?, // 직접 교환 장소
    val region: String?     // 희망 지역
    // 현재 JSON에 phone, address, zipCode 등이 없음 -> 매핑 제외 (코드에서 null 처리)
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