package com.bookiibookii.bookiibookii.library.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.library.BookReviewItemDTO
import com.bookiibookii.bookiibookii.data.model.library.BookReviewUpdateItemDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberReviewUpdateItemDTO
import com.bookiibookii.bookiibookii.data.model.library.MyGroupReviewsUpdateDTO
import com.bookiibookii.bookiibookii.library.ui.ReviewBookInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewEditUiState(
    val isLoading: Boolean = false,
    val books: List<ReviewBookInfo> = emptyList(),
    val initialRatings: List<Double> = emptyList(),
    val initialComments: List<String> = emptyList(),
    val initialIsPartnerGood: Boolean? = null,
    val initialPartnerComment: String = "",
)

class ReviewEditViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewEditUiState())
    val uiState: StateFlow<ReviewEditUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<ReviewEditEvent>()
    val event: SharedFlow<ReviewEditEvent> = _event.asSharedFlow()

    // 프리필·수정 대상 = 내가 작성한 책 리뷰(GET /reviews/book/me).
    private var myReviews: List<BookReviewItemDTO> = emptyList()
    // 일괄 수정(PATCH /reviews/my-group)은 memberBookId 기준 — 리뷰 응답엔 없어 라이브러리 멤버북에서 매핑. myReviews와 동일 순서.
    private var memberBookIds: List<Int?> = emptyList()

    sealed interface ReviewEditEvent {
        object Success : ReviewEditEvent
        data class Error(val message: String) : ReviewEditEvent
    }

    /**
     * 프리필
     * - 책: GET /api/groups/{groupId}/reviews/book/me — 내 리뷰만, 정확한 Double 별점·내용·표지·reviewId·bookId.
     * - memberBookId: GET /api/library/memberbooks 에서 같은 groupId의 bookId→memberBookId 매핑(일괄 수정용).
     * - 파트너 후기: GET /api/groups/{groupId}/reviews 의 memberReviews 중 내 것(writerNickname 일치).
     */
    fun load(groupId: Int) {
        if (groupId == -1) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1) 내 책 리뷰
                val resp = RetrofitClient.libApi().getMyBookReviews(groupId)
                val reviews = if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    resp.body()?.result?.reviews.orEmpty()
                } else {
                    emptyList()
                }
                myReviews = reviews

                // 2) bookId → memberBookId 매핑
                val bookIdToMemberBookId: Map<Int, Int> = try {
                    val libResp = RetrofitClient.libApi().getLibraryBooks()
                    if (libResp.isSuccessful && libResp.body()?.isSuccess == true) {
                        libResp.body()?.result.orEmpty()
                            .filter { it.groupId == groupId }
                            .associate { it.bookId to it.memberBookId }
                    } else {
                        emptyMap()
                    }
                } catch (_: Exception) { emptyMap() }
                memberBookIds = reviews.map { r -> r.bookId?.let { bookIdToMemberBookId[it] } }

                // 3) 내 파트너(멤버) 후기 — reaction/comment 프리필
                var initialIsPartnerGood: Boolean? = null
                var initialPartnerComment = ""
                try {
                    val myNickname = RetrofitClient.mypApi().getMypage().body()?.result?.nickname
                    val grpResp = RetrofitClient.libApi().getGroupReviews(groupId)
                    if (grpResp.isSuccessful && grpResp.body()?.isSuccess == true) {
                        val mine = grpResp.body()?.result?.memberReviews
                            ?.firstOrNull { it.writerNickname == myNickname }
                        when (mine?.reaction) {
                            "BOOM_UP"   -> initialIsPartnerGood = true
                            "BOOM_DOWN" -> initialIsPartnerGood = false
                        }
                        initialPartnerComment = mine?.comment.orEmpty()
                    }
                } catch (_: Exception) { /* 파트너 후기 프리필 실패 무시 */ }

                _uiState.update {
                    it.copy(
                        isLoading             = false,
                        books                 = reviews.map { r -> ReviewBookInfo(r.bookTitle.orEmpty(), r.bookAuthor.orEmpty(), "", r.bookImageUrl) },
                        initialRatings        = reviews.map { r -> r.rating ?: 0.0 },
                        initialComments       = reviews.map { r -> r.content.orEmpty() },
                        initialIsPartnerGood  = initialIsPartnerGood,
                        initialPartnerComment = initialPartnerComment,
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * 종료 그룹 내 리뷰 일괄 수정 — PATCH /api/groups/{groupId}/reviews/my-group
     * 책 리뷰(memberBookId별 별점·코멘트) + 파트너 후기(reaction·comment)를 한 번에 보낸다.
     * - ratings / bookComments: load()로 받은 내 책 리뷰와 같은 순서.
     * - isPartnerGood / partnerComment: 선택 시에만 파트너 후기 포함.
     */
    fun submit(
        groupId: Int,
        ratings: List<Double>,
        bookComments: List<String>,
        isPartnerGood: Boolean?,
        partnerComment: String,
    ) {
        if (groupId == -1) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // memberBookId가 매핑된 책 리뷰만 포함(서버는 memberBookId required)
            val bookItems = myReviews.mapIndexedNotNull { i, review ->
                val memberBookId = memberBookIds.getOrNull(i) ?: return@mapIndexedNotNull null
                BookReviewUpdateItemDTO(
                    memberBookId = memberBookId,
                    star         = ratings.getOrElse(i) { review.rating ?: 0.0 },
                    comment      = bookComments.getOrElse(i) { review.content.orEmpty() },
                )
            }
            val memberReview = if (isPartnerGood != null) {
                MemberReviewUpdateItemDTO(
                    reaction = if (isPartnerGood) "BOOM_UP" else "BOOM_DOWN",
                    comment  = partnerComment,
                )
            } else {
                null
            }

            val success = if (bookItems.isEmpty() && memberReview == null) {
                false
            } else {
                try {
                    val resp = RetrofitClient.libApi().updateMyGroupReviews(
                        groupId,
                        MyGroupReviewsUpdateDTO(
                            bookReviews  = bookItems.ifEmpty { null },
                            memberReview = memberReview,
                        ),
                    )
                    resp.isSuccessful && resp.body()?.isSuccess == true
                } catch (_: Exception) { false }
            }

            _uiState.update { it.copy(isLoading = false) }
            if (success) {
                _event.emit(ReviewEditEvent.Success)
            } else {
                _event.emit(ReviewEditEvent.Error("리뷰 수정에 실패했습니다."))
            }
        }
    }
}
