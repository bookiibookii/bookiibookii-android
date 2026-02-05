package com.bookiibookii.bookiibookii.trkHost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class TrackerDetailUiState {
    data object Idle : TrackerDetailUiState()
    data object Loading : TrackerDetailUiState()
    data class Success(val data: TrackerDetailUiModel) : TrackerDetailUiState()
    data class Error(val message: String) : TrackerDetailUiState()
}

class HostTrackerDetailViewModel : ViewModel() {

    private val _state = MutableStateFlow<TrackerDetailUiState>(TrackerDetailUiState.Idle)
    val state: StateFlow<TrackerDetailUiState> = _state.asStateFlow()

    fun load(groupId: Long) {
        viewModelScope.launch {
            _state.value = TrackerDetailUiState.Loading

            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api().getTrackerDetail(groupId)
                }

                if (!response.isSuccessful) {
                    _state.value = TrackerDetailUiState.Error("HTTP ${response.code()}")
                    return@launch
                }

                val body = response.body()
                if (body == null) {
                    _state.value = TrackerDetailUiState.Error("응답 바디가 null")
                    return@launch
                }

                if (!body.isSuccess) {
                    _state.value = TrackerDetailUiState.Error("실패: ${body.code} / ${body.message}")
                    return@launch
                }

                val dto = body.result
                if (dto == null) {
                    _state.value = TrackerDetailUiState.Error("result가 null")
                    return@launch
                }

                _state.value = TrackerDetailUiState.Success(TrackerDetailUiModel.from(dto))

            } catch (e: Exception) {
                _state.value = TrackerDetailUiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun loadDummy(status: TrackerStatus) {
        val ui = TrackerDetailUiModel(
            trackerId = -1L,
            status = status,
            layoutRes = when (status) {
                TrackerStatus.READY -> com.bookiibookii.bookiibookii.R.layout.fragment_start_bottom_sheet_dialog
                TrackerStatus.HOST_READING -> com.bookiibookii.bookiibookii.R.layout.fragment_reading_bottom_sheet_dialog
                TrackerStatus.HOST_DONE -> com.bookiibookii.bookiibookii.R.layout.fragment_host_shipping_bottom_dialog
                TrackerStatus.SHIPPING_TO_GUEST -> com.bookiibookii.bookiibookii.R.layout.fragment_host_shipped_bottom_dialog
                TrackerStatus.RECEIVED, TrackerStatus.GUEST_READING ->
                    com.bookiibookii.bookiibookii.R.layout.fragment_host_reading_status_bottom_dialog
                TrackerStatus.GUEST_DONE ->
                    com.bookiibookii.bookiibookii.R.layout.fragment_host_reading_done_bottom_dialog
                TrackerStatus.SHIPPING_TO_HOST ->
                    com.bookiibookii.bookiibookii.R.layout.fragment_host_shipped_bottom_dialog
                else ->
                    com.bookiibookii.bookiibookii.R.layout.fragment_host_trade_finish_bottom_dialog
            }
        )
        _state.value = TrackerDetailUiState.Success(ui)
    }
}
