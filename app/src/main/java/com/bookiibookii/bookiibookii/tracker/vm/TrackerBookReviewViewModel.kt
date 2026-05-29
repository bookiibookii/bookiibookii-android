package com.bookiibookii.bookiibookii.tracker.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.tracker.data.TrackerRepository
import com.bookiibookii.bookiibookii.tracker.model.TrackerBookReviewUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackerBookReviewViewModel(
    private val groupId: Long,
    private val repository: TrackerRepository = TrackerRepository(RetrofitClient.trkApi())
) : ViewModel() {

    private val _state = MutableStateFlow(TrackerBookReviewUiState())
    val state: StateFlow<TrackerBookReviewUiState> = _state

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
                    val book = when (dto?.displayStatus) {
                        "EXCHANGE_REVIEW_WRITING" -> dto.partnerBook
                        else -> dto?.myBook
                    }
                    _state.update {
                        it.copy(
                            bookTitle = book?.title.orEmpty(),
                            bookImageUrl = book?.image,
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

    fun submitReview(star: Double, comment: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val res = repository.submitBookReview(groupId, star, comment)
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    onSuccess()
                }
            } catch (_: Exception) {
                // 실패 시 무시 (다음 단계에서 에러 UI)
            }
        }
    }

    companion object {
        fun factory(groupId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TrackerBookReviewViewModel(groupId)
            }
        }
    }
}
