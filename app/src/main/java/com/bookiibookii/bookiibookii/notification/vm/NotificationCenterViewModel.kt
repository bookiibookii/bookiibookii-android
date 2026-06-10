package com.bookiibookii.bookiibookii.notification.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.notification.NotificationItem
import com.bookiibookii.bookiibookii.notification.model.NotificationTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationCenterUiState(
    val selectedTab: NotificationTab = NotificationTab.SYSTEM,
    val items: List<NotificationItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val nextCursor: String? = null,
    val hasNext: Boolean = false,
)

class NotificationCenterViewModel : ViewModel() {

    private val _state = MutableStateFlow(NotificationCenterUiState())
    val state: StateFlow<NotificationCenterUiState> = _state.asStateFlow()

    private val pageSize = 20

    init {
        loadFirstPage()
    }

    // 탭 전환 — 카테고리가 바뀌면 목록을 처음부터 다시 조회
    fun selectTab(tab: NotificationTab) {
        if (_state.value.selectedTab == tab) return
        _state.update { it.copy(selectedTab = tab) }
        loadFirstPage()
    }

    fun loadFirstPage() {
        val category = _state.value.selectedTab.name
        viewModelScope.launch {
            _state.update {
                it.copy(items = emptyList(), nextCursor = null, hasNext = false, isLoading = true, isLoadingMore = false)
            }
            runCatching {
                RetrofitClient.notiApi().getNotifications(category, null, pageSize)
            }.onSuccess { http ->
                val result = http.body()?.takeIf { http.isSuccessful && it.isSuccess }?.result
                if (result != null) {
                    _state.update {
                        // 응답 도착 사이 탭이 바뀌었으면 무시 (오래된 결과로 덮어쓰기 방지)
                        if (it.selectedTab.name != category) it
                        else it.copy(items = result.items, nextCursor = result.nextCursor, hasNext = result.hasNext, isLoading = false)
                    }
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            }.onFailure {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadNextPage() {
        val s = _state.value
        if (s.isLoading || s.isLoadingMore || !s.hasNext || s.nextCursor.isNullOrBlank()) return
        val category = s.selectedTab.name
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            runCatching {
                RetrofitClient.notiApi().getNotifications(category, s.nextCursor, pageSize)
            }.onSuccess { http ->
                val result = http.body()?.takeIf { http.isSuccessful && it.isSuccess }?.result
                if (result != null) {
                    _state.update {
                        if (it.selectedTab.name != category) it
                        else it.copy(items = it.items + result.items, nextCursor = result.nextCursor, hasNext = result.hasNext, isLoadingMore = false)
                    }
                } else {
                    _state.update { it.copy(isLoadingMore = false) }
                }
            }.onFailure {
                _state.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    // 알림 읽음 처리 — 읽음 응답의 isRead로 해당 아이템 상태만 갱신
    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            runCatching {
                RetrofitClient.notiApi().readNotification(notificationId)
            }.onSuccess { http ->
                val result = http.body()?.takeIf { http.isSuccessful && it.isSuccess }?.result ?: return@onSuccess
                _state.update { cur ->
                    cur.copy(
                        items = cur.items.map { item ->
                            if (item.id == notificationId) item.copy(isRead = result.isRead)
                            else item
                        },
                    )
                }
            }
        }
    }
}
