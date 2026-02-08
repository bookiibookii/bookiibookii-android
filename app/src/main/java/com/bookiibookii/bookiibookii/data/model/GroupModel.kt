package com.bookiibookii.bookiibookii.data.model

import com.bookiibookii.bookiibookii.group.GroupData
import com.google.gson.annotations.SerializedName


// 그룹 생성
data class GroupCreateRequest(
    @SerializedName("isbn13") val isbn13: String,
    @SerializedName("maxCapacity") val maxCapacity: Int,
    @SerializedName("startDate") val startDate: String, // "yyyy-MM-dd"
    @SerializedName("readingPeriod") val readingPeriod: Int,
    @SerializedName("groupComment") val groupComment: String,
    @SerializedName("customTag") val customTag: String,
    @SerializedName("groupType") val groupType: String, // "TOGETHER" or "RELAY"
    @SerializedName("tradeType") val tradeType: String, // "DELIVERY" or "DIRECT"
    @SerializedName("tags") val tags: List<GroupTagRequest>
)

data class GroupTagRequest(
    @SerializedName("type") val type: String, // "GENRE" 등
    @SerializedName("value") val value: List<String>
)

data class GroupCreateResponse(
    @SerializedName("groupId") val groupId: Long?,
    @SerializedName("message") val message: String?
)


// 알라딘 검색하기
data class BookSearchResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: BookSearchResult? // 실패시 null일 수 있으므로 ? 붙임
)

// 2. 알맹이 (result 객체)
data class BookSearchResult(
    val books: List<BookItem>,
    val totalPage: Int,
    val totalResults: Int
)

// 3. 실제 책 정보 (books 배열 안의 아이템)
data class BookItem(
    val title: String,       // 책 제목
    val author: String,      // 저자
    val image: String,       // 표지 이미지 URL
    val publisher: String,   // 출판사
    val isbn13: String,      // ISBN (고유번호)
    val category: String,    // 카테고리 코드 (예: ECON_BIZ)
    val categoryLabel: String, // 카테고리 이름
    val link : String // 구매링크
)

data class GroupListRequest(
    @SerializedName("groupTypes")
    val groupTypes: List<String>?, // ["TOGETHER", "RELAY"] (없으면 null)

    @SerializedName("tradeTypes")
    val tradeTypes: List<String>?, // ["DELIVERY", "DIRECT"]

    @SerializedName("meetPlace")
    val meetPlace: List<String>?, // ["송파구", "강남구"]

    @SerializedName("categories")
    val categories: List<String>?, // ["ECON_BIZ"]

    @SerializedName("sort")
    val sort: String = "LATEST", // "LATEST", "DEADLINE" 등

    @SerializedName("page")
    val page: Int = 0,

    @SerializedName("size")
    val size: Int = 20
)

data class GroupListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: GroupPageResult?
)

data class GroupPageResult(
    @SerializedName("groupList")
    val groupList: List<GroupItemDto>?,

    val currentPage: Int,
    val hasNext: Boolean
)

// 3. 아이템 객체 (서버 필드명과 1:1 매칭)
data class GroupItemDto(
    @SerializedName("groupId")
    val groupId: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("author")
    val author: String?,

    @SerializedName("genre")
    val genre: String?,

    @SerializedName("bookImage") // ★ 서버는 bookImage
    val bookImage: String?,

    @SerializedName("hostNickname") // ★ 서버는 hostNickname
    val hostNickname: String?,

    @SerializedName("tags") // ★ 서버는 ["MEMO", "LIGHT_FUN"] (단순 문자열 리스트)
    val tags: List<String>?,

    @SerializedName("groupStatus")
    val groupStatus: String, // "RECRUITING"

    @SerializedName("currentCount")
    val currentCount: Int,

    @SerializedName("maxCapacity")
    val maxCapacity: Int,

    @SerializedName("readingPeriod")
    val readingPeriod: Int,

    val groupType: String, // "TOGETHER"
    val tradeType: String?, // "NONE"
    val startDate: String?,
    val isHot: Boolean
) {
    // UI 모델로 변환
    fun toUiModel(): GroupData {
        // 태그에 # 붙이기
        val uiTags = tags?.map {
            when(it) {
                "MEMO" -> "#메모환영"
                "POSTIT" -> "#포스트잇"
                "CLEAN" -> "#깔끔"
                "SERIOUS" -> "#진지하게"
                "LIGHT_FUN" -> "#재미있게"
                "INSIGHT" -> "#인사이트"
                else -> "#$it"
            }
        } ?: emptyList()

        val uiStatus = if (groupStatus == "RECRUITING") "모집 중" else "모집 완료"

        return GroupData(
            coverImgUrl = bookImage ?: "", // null이면 빈값
            bookTitle = title,
            bookAuthor = author ?: "저자 미상", // null이면 기본값
            genre = genre ?: "장르",
            status = uiStatus,
            readingPeriod = readingPeriod.toString(), // 책읽는 기간
            memberCount = currentCount.toString(),
            isHot = isHot,
            profileImgUrl = "",
            nickname = hostNickname ?: "알 수 없음",
            date = startDate?.replace("-",".") ?: "날짜 미정",
            tags = uiTags,
            groupType = groupType
        )
    }
    data class PopularSearchResponse(
        val isSuccess: Boolean,
        val code: String,
        val message: String,
        val result: List<String>? // ["최강록", "한강", ...]
    )

 // 그룹 검색하기
    // 서버 응답 껍데기
    data class GroupSearchResponse(
        val isSuccess: Boolean,
        val code: String,
        val message: String,
        val result: GroupSearchResult?
    )

    // result 내부 구조
    data class GroupSearchResult(
        @SerializedName("groupList")
        val groupList: List<GroupItemDto>,
        val totalCount: Int,
        val currentPage: Int,
        val hasNext: Boolean
    )
}