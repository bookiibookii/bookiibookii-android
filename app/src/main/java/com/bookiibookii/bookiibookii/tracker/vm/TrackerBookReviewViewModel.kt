package com.bookiibookii.bookiibookii.tracker.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.tracker.BookReviewItem
import com.bookiibookii.bookiibookii.tracker.data.TrackerRepository
import com.bookiibookii.bookiibookii.tracker.model.TrackerBookReviewUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackerBookReviewViewModel(
    private val groupId: Long,
    private val isEdit: Boolean = false,   // true면 제출 시 PATCH(수정), false면 POST(작성)
    private val repository: TrackerRepository = TrackerRepository(RetrofitClient.trkApi())
) : ViewModel() {

    private val _state = MutableStateFlow(TrackerBookReviewUiState())
    val state: StateFlow<TrackerBookReviewUiState> = _state

    // 수정 모드에서 PATCH 대상 reviewId (프리필 때 GET reviews/book/me 로 확보)
    private var editReviewId: Long? = null

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val res = repository.fetchTrackerDetail(groupId)
                val body = res.body()
                if (res.isSuccessful && body?.isSuccess == true) {
                    val dto = body.result
                    // EXCHANGE_REVIEW_WRITING은 파트너 책 후기, 그 외 REVIEW_WRITING은 내 책
                    val isPartnerReview = dto?.displayStatus == "EXCHANGE_REVIEW_WRITING"
                    val book = if (isPartnerReview) dto?.partnerBook else dto?.myBook
                    // 수정 모드면 화면에 표시 중인 책과 같은 책의 내 리뷰를 골라 프리필
                    val myReview = if (isEdit) fetchMyBookReview(book?.title) else null
                    editReviewId = myReview?.reviewId
                    _state.update {
                        it.copy(
                            bookTitle = book?.title.orEmpty(),
                            bookImageUrl = book?.image,
                            initialStar = myReview?.rating ?: 0.0,
                            initialComment = myReview?.content.orEmpty(),
                            loading = false,
                        )
                    }
                } else {
                    _state.update { it.copy(error = body?.message ?: "정보를 불러오지 못했어요", loading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, loading = false) }
            }
        }
    }

    // 내 책 리뷰 목록 조회 후 표시 중인 책 제목과 일치하는 항목 1건 선별. 실패/없으면 null.
    private suspend fun fetchMyBookReview(bookTitle: String?): BookReviewItem? {
        if (bookTitle.isNullOrBlank()) return null
        return try {
            val res = repository.fetchMyBookReviews(groupId)
            val body = res.body()
            if (res.isSuccessful && body?.isSuccess == true) {
                body.result?.reviews?.firstOrNull { it.bookTitle == bookTitle }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun submitReview(star: Double, comment: String?, onSuccess: () -> Unit) {
        // 더블탭 가드 — 제출 진행 중이면 중복 호출/중복 네비 차단
        if (_state.value.submitting) return
        viewModelScope.launch {
            _state.update { it.copy(submitting = true) }
            try {
                val res = if (isEdit) {
                    val reviewId = editReviewId ?: return@launch
                    repository.updateMyBookReview(groupId, reviewId, star, comment)
                } else {
                    repository.submitBookReview(groupId, star, comment)
                }
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    onSuccess()
                }
            } catch (_: Exception) {
                // 실패 시 무시 (다음 단계에서 에러 UI)
            } finally {
                _state.update { it.copy(submitting = false) }
            }
        }
    }

    companion object {
        fun factory(
            groupId: Long,
            isEdit: Boolean = false,
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    TrackerBookReviewViewModel(groupId, isEdit)
                }
            }
    }
}
