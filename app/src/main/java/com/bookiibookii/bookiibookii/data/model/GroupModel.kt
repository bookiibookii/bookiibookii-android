package com.bookiibookii.bookiibookii.data.model

import com.bookiibookii.bookiibookii.group.main.GroupData
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
    @SerializedName("preferRegion") val preferRegion: String,
    @SerializedName("meetPlace") val meetPlace: String,
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

    @SerializedName("customTag")
    val customTag : String?,

    val groupType: String, // "TOGETHER"
    val tradeType: String?, // "NONE"
    val startDate: String?,
    val isHot: Boolean
) {
    // UI 모델로 변환
    fun toUiModel(): GroupData {
        // 태그에 # 붙이기
        val safeTags = tags ?: emptyList()

        val uiStatus = if (groupStatus == "RECRUITING") "모집 중" else "모집 완료"

        return GroupData(
            groupId  = groupId.toInt(),
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
            tags = safeTags,
            customTag = customTag ?: "",
            groupType = groupType,

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

    //그룹 아이템 관리
    data class GroupDetailResponse(
        val isSuccess: Boolean,
        val code: String,
        val message: String,
        val result: GroupDetailResult
    )

    data class GroupDetailResult(
        val groupId: Int,
        val title: String,          // 게시글 제목
        val bookTitle: String,      // 책 제목
        val bookImage: String?,     // 책 표지
        val author: String,
        val category: String,
        val groupStatus: String,    // "RECRUITING", "CLOSED" 등
        val buttonStatus: String,   // ★ 핵심: "MANAGE", "TRACKER", "CANCEL", "FULL", "APPLY"
        val isHost: Boolean,        // ★ 호스트 여부 (메뉴 버튼 분기용)
        val readingPeriod: Int,
        val matchedCount: Int,      // 현재 인원
        val maxCapacity: Int,       // 최대 인원
        val waitingCount: Int,      // 대기 인원 (호스트용)
        val isHot: Boolean,
        val createdAt: String,
        val startDate: String,
        val hostNickname: String,
        val hostProfileImage: String?,
        val groupTags: List<String>?,
        val customTag: String?,
        val groupComment: String?,  // 소개글
        val participantSlots: List<ParticipantSlot>? // 하단 멤버 리스트용
    )


    data class ParticipantSlot(
        val nickname: String?,
        val profileImage: String?,
        val role: String, // "HOST", "GUEST", "EMPTY"
        val isMe: Boolean
    )

    data class GroupApplyRequest(
        val applyMsg: String
    )

    // 응답 바디 (성공 시 result 내부 데이터)
    data class GroupApplyResult(
        val applicationId: Long,
        val status: String,
        val createdAt: String
    )

    // 전체 응답 래퍼 (기존에 쓰시던 BaseResponse 형태가 있다면 그것을 쓰셔도 됩니다)
    data class GroupApplyResponse(
        val isSuccess: Boolean,
        val code: String,
        val message: String,
        val result: GroupApplyResult?
    )

    // 취소 결과 (Result)
    data class GroupCancelResult(
        val groupId: Long,
        val canceledAt: String
    )

    // 취소 응답 (Response Wrapper)
    data class GroupCancelResponse(
        val isSuccess: Boolean,
        val code: String,
        val message: String,
        val result: GroupCancelResult?
    )

    // 그룹 신청자 조회
    data class GroupAppListResponse(
        val isSuccess: Boolean,
        val code: String,
        val message: String,
        val result: GroupAppListResult?
    )

    // 2. 결과 (리스트 + 카운트)
    data class GroupAppListResult(
        val applicationList: List<GroupAppItem>,
        val totalCount: Int
    )

    // 3. 아이템 (서버에서 오는 필드명 기준)
    data class GroupAppItem(
        val applicationId: Long,
        val user: Int,
        val name: String,        // 닉네임
        val tags: List<String>?, // 태그 코드들
        val createdAt: String,
        val applyMsg: String
    )

    //참여요청 수락/거절
    data class GroupAppStatusRequest(
        val status: String // "ACCEPTED" 또는 "REJECTED"
    )

    // 2. 응답 (받을 데이터 - 성공/실패 확인용)
    data class GroupAppStatusResponse(
        val isSuccess: Boolean,
        val code: String,
        val message: String
    )

    //그룹 삭제
    data class GroupDeleteResponse(
        val isSuccess: Boolean,
        val code: String,
        val message: String,
        val result: GroupDeleteResult?
    )

    // 2. 그룹 삭제 결과 (알맹이)
    data class GroupDeleteResult(
        val groupId: Long,
        val deletedAt: String // "2026. 02. 10. 04:37"
    )

    //그룹수정하기
    data class GroupModifyRequest(
        val startDate: String,       // "2026-02-10"
        val readingPeriod: Int,      // 0
        val groupComment: String,    // "소개글"
        val customTag: String?,      // "직접입력태그"
        val tags: List<GroupTagRequest> // 태그 리스트
    )
    data class GroupModifyResponse(
        @SerializedName("isSuccess") val isSuccess: Boolean,
        @SerializedName("code") val code: String,
        @SerializedName("message") val message: String,
        @SerializedName("result") val result: Any?
    )

    //댓글작성
    data class CommentCreateRequest(
        val content: String,
        val parentId: Long?  // 일반 댓글이면 null, 대댓글이면 부모 ID
    )
    // [전체 응답] 서버에서 주는 전체 JSON을 받아줄 그릇
    data class CommentCreateResponse(
        val isSuccess: Boolean,
        val code: String,
        val message: String,
        val result: CommentCreateResult  // ★ 알맹이 데이터
    )

    // [알맹이] result 안에 들어가는 진짜 데이터
    data class CommentCreateResult(
        val commentId: Long,
        val groupId: Long,
        val parentId: Long?,
        val content: String,
        val createdAt: String,
        val writer: CommentWriter
    )

    // [작성자 정보]
    data class CommentWriter(
        val userId: Long,
        val name: String,
        val profileImage: String?,
        val role: String
    )

    //댓글 조회
    data class CommentListResponse(
        val isSuccess: Boolean,
        val code: String,
        val message: String,
        val result: List<CommentItem> // ★ 부모 댓글들의 리스트
    )

    // [알맹이] 댓글 하나 (재귀 구조)
    data class CommentItem(
        val id: Long,
        val deleted: Boolean,
        val content: String,
        val parentId: Long?, // 0 또는 null
        val writer: CommentWriter,
        val createdAt: String,
        val children: List<CommentItem>? = null // ★ 대댓글 리스트 (없으면 null)
    )
}