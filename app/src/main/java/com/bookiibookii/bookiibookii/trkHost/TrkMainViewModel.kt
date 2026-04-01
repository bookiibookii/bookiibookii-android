package com.bookiibookii.bookiibookii.trkHost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TrackerTab { MY_GROUP, JOINED_GROUP }

class TrkMainViewModel : ViewModel() {

    private val _hostTrackers = MutableStateFlow<List<TrackerData>>(emptyList())
    private val _guestTrackers = MutableStateFlow<List<TrackerData>>(emptyList())

    private val _currentTab = MutableStateFlow(TrackerTab.MY_GROUP)
    val currentTab: StateFlow<TrackerTab> = _currentTab.asStateFlow()

    val trackers: StateFlow<List<TrackerData>> = _currentTab
        .flatMapLatest { tab ->
            when (tab) {
                TrackerTab.MY_GROUP -> _hostTrackers
                TrackerTab.JOINED_GROUP -> _guestTrackers
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadHostTrackers()
    }

    fun selectTab(tab: TrackerTab) {
        _currentTab.value = tab
        when (tab) {
            TrackerTab.MY_GROUP -> if (_hostTrackers.value.isEmpty()) loadHostTrackers()
            TrackerTab.JOINED_GROUP -> if (_guestTrackers.value.isEmpty()) loadGuestTrackers()
        }
    }

    private fun loadHostTrackers() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().getHostTrackers()
                if (!response.isSuccessful) {
                    android.util.Log.e("TRACKER", "HOST HTTP ${response.code()} ${response.message()}")
                    return@launch
                }
                val body = response.body() ?: return@launch
                if (!body.isSuccess) {
                    android.util.Log.e("TRACKER", "HOST API fail: ${body.message}")
                    return@launch
                }
                _hostTrackers.value = body.result?.map { it.toTrackerData() } ?: emptyList()
            } catch (e: Exception) {
                android.util.Log.e("TRACKER", "HOST exception", e)
            }
        }
    }

    private fun loadGuestTrackers() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().getGuestTrackers()
                if (!response.isSuccessful) {
                    android.util.Log.e("TRACKER", "GUEST HTTP ${response.code()} ${response.message()}")
                    return@launch
                }
                val body = response.body() ?: return@launch
                if (!body.isSuccess) {
                    android.util.Log.e("TRACKER", "GUEST API fail: ${body.message}")
                    return@launch
                }
                _guestTrackers.value = body.result?.map { it.toTrackerData() } ?: emptyList()
            } catch (e: Exception) {
                android.util.Log.e("TRACKER", "GUEST exception", e)
            }
        }
    }

}
