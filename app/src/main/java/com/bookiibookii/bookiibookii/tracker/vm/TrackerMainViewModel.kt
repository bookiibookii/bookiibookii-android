package com.bookiibookii.bookiibookii.tracker.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.tracker.data.TrackerRepository
import com.bookiibookii.bookiibookii.tracker.model.TrackerMainUiState
import com.bookiibookii.bookiibookii.tracker.model.toCardModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackerMainViewModel(
    private val repository: TrackerRepository = TrackerRepository(RetrofitClient.trkApi())
) : ViewModel() {

    private val _state = MutableStateFlow(TrackerMainUiState())
    val state: StateFlow<TrackerMainUiState> = _state

    init {
        load()
    }

    fun registerDelivery(groupId: Long, deliveryCompany: String, trackingNumber: String) {
        viewModelScope.launch {
            try {
                val res = repository.registerDelivery(groupId, deliveryCompany, trackingNumber)
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    load()
                }
            } catch (_: Exception) {
                // 실패 시 무시
            }
        }
    }

    fun recordProgress(groupId: Long, currentPage: Int) {
        viewModelScope.launch {
            try {
                val res = repository.recordReadingProgress(groupId, currentPage)
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    load()
                }
            } catch (_: Exception) {
                // 실패 시 무시 (다음 단계에서 에러 표시 추가)
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val res = repository.fetchMyTrackers()
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    val result = res.body()?.result
                    val items = result?.items.orEmpty()
                    val summary = result?.summary
                    _state.update {
                        it.copy(
                            cards = items.map { dto -> dto.toCardModel() },
                            totalCount = summary?.totalCount ?: 0,
                            readingCount = summary?.readingCount ?: 0,
                            exchangingCount = summary?.exchangingCount ?: 0,
                            reviewCount = summary?.reviewCount ?: 0,
                            loading = false,
                        )
                    }
                } else {
                    _state.update { it.copy(error = "트래커를 불러오지 못했어요", loading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, loading = false) }
            }
        }
    }
}
