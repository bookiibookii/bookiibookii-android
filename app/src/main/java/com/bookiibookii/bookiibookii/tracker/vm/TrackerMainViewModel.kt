package com.bookiibookii.bookiibookii.tracker.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.tracker.data.TrackerRepository
import com.bookiibookii.bookiibookii.tracker.model.TrackerMainUiState
import com.bookiibookii.bookiibookii.tracker.model.toCardModel
import com.bookiibookii.bookiibookii.tracker.model.toCounts
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

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val res = repository.fetchMyTrackers()
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    val items = res.body()?.result.orEmpty()
                    val counts = items.toCounts()
                    _state.update {
                        it.copy(
                            cards = items.map { dto -> dto.toCardModel() },
                            totalCount = counts.total,
                            readingCount = counts.reading,
                            exchangingCount = counts.exchanging,
                            reviewCount = counts.review,
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
