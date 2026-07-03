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

    private var myReviews: List<BookReviewItemDTO> = emptyList()
    private var memberBookIds: List<Int?> = emptyList()

    sealed interface ReviewEditEvent {
        object Success : ReviewEditEvent
        data class Error(val message: String) : ReviewEditEvent
    }

    fun load(groupId: Int) {
        if (groupId == -1) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val resp = RetrofitClient.libApi().getMyBookReviews(groupId)
                val reviews = if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    resp.body()?.result?.reviews.orEmpty()
                } else {
                    emptyList()
                }
                myReviews = reviews

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
                } catch (_: Exception) {  }

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
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _event.emit(ReviewEditEvent.Error("후기 정보를 불러오지 못했습니다."))
            }
        }
    }

    fun submit(
        groupId: Int,
        ratings: List<Double>,
        bookComments: List<String>,
        isPartnerGood: Boolean?,
        partnerComment: String,
    ) {
        if (groupId == -1) return
        // 더블탭 가드 — 제출 진행 중이면 중복 PATCH/중복 완료 이벤트 차단
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

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
