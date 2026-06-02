package com.bookiibookii.bookiibookii.library.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.library.BookReviewUpsertDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberReviewCreateDTO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewEditUiState(val isLoading: Boolean = false)

class ReviewEditViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewEditUiState())
    val uiState: StateFlow<ReviewEditUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<ReviewEditEvent>()
    val event: SharedFlow<ReviewEditEvent> = _event.asSharedFlow()

    sealed interface ReviewEditEvent {
        object Success : ReviewEditEvent
        data class Error(val message: String) : ReviewEditEvent
    }

    /**
     * 책 리뷰 수정 + 파트너 후기 제출
     * - bookStar / bookComment: 내 책 평점·코멘트 (PATCH /api/groups/{groupId}/reviews/me)
     * - isPartnerGood / partnerComment: 파트너 후기 (POST /api/groups/{groupId}/member-reviews)
     */
    fun submit(
        groupId: Int,
        bookStar: Double,
        bookComment: String,
        isPartnerGood: Boolean?,
        partnerComment: String,
    ) {
        if (groupId == -1) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            var success = true

            // 책 리뷰 수정
            try {
                val resp = RetrofitClient.libApi().updateMyReview(
                    groupId,
                    BookReviewUpsertDTO(star = bookStar, comment = bookComment),
                )
                if (!resp.isSuccessful || resp.body()?.isSuccess != true) success = false
            } catch (_: Exception) { success = false }

            // 파트너 후기 (선택한 경우에만)
            if (isPartnerGood != null) {
                try {
                    val reaction = if (isPartnerGood) "BOOM_UP" else "DISLIKE"
                    RetrofitClient.libApi().postMemberReview(
                        groupId,
                        MemberReviewCreateDTO(reaction = reaction, comment = partnerComment),
                    )
                } catch (_: Exception) { /* 파트너 후기 실패 무시 */ }
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
