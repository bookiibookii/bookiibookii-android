package com.bookiibookii.bookiibookii.tracker.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.tracker.data.TrackerRepository
import com.bookiibookii.bookiibookii.tracker.model.TrackerPartnerReviewUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackerPartnerReviewViewModel(
    private val groupId: Long,
    private val repository: TrackerRepository = TrackerRepository(RetrofitClient.trkApi())
) : ViewModel() {

    private val _state = MutableStateFlow(TrackerPartnerReviewUiState())
    val state: StateFlow<TrackerPartnerReviewUiState> = _state

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
                    _state.update {
                        it.copy(
                            groupName = dto?.groupName.orEmpty(),
                            myNickname = dto?.myBook?.currentReaderNickname.orEmpty(),
                            myBookTitle = dto?.myBook?.title.orEmpty(),
                            myBookCoverUrl = dto?.myBook?.image,
                            myProfileImageUrl = dto?.myBook?.currentReaderProfileImageUrl,
                            partnerNickname = dto?.partnerBook?.currentReaderNickname.orEmpty(),
                            partnerBookTitle = dto?.partnerBook?.title.orEmpty(),
                            partnerBookCoverUrl = dto?.partnerBook?.image,
                            partnerProfileImageUrl = dto?.partnerBook?.currentReaderProfileImageUrl,
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

    // reaction: BOOM_UP | BOOM_DOWN | null, comment: 필수
    fun submitReview(reaction: String?, comment: String, onSuccess: () -> Unit) {
        // 더블탭 가드 — 제출 진행 중이면 중복 호출/중복 네비 차단
        if (_state.value.submitting) return
        viewModelScope.launch {
            _state.update { it.copy(submitting = true) }
            try {
                val res = repository.submitMemberReview(groupId, reaction, comment)
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    onSuccess()
                }
            } catch (_: Exception) {
            } finally {
                _state.update { it.copy(submitting = false) }
            }
        }
    }

    companion object {
        fun factory(groupId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TrackerPartnerReviewViewModel(groupId)
            }
        }
    }
}
