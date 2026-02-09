package com.bookiibookii.bookiibookii.home.notification.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.home.notification.data.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repo: NotificationRepository,
    private val category: String
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationUiState())
    val state: StateFlow<NotificationUiState> = _state

    private val pageSize = 20

    fun loadFirstPage() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    items = emptyList(),
                    nextCursor = null,
                    hasNext = false,
                    isLoading = true,
                    isLoadingMore = false,
                    errorMessage = null
                )
            }

            val http = repo.fetchNotifications(category = category, cursor = null, size = pageSize)
            val body = http.body()

            if (http.isSuccessful && body != null && body.isSuccess && body.result != null) {
                val r = body.result
                _state.update {
                    it.copy(
                        items = r.items,
                        nextCursor = r.nextCursor,
                        hasNext = r.hasNext,
                        isLoading = false
                    )
                }
            } else {
                val msg = body?.message ?: "알림 목록 조회에 실패했습니다."
                _state.update { it.copy(isLoading = false, errorMessage = msg) }
            }
        }
    }

    fun loadNextPage() {
        val s = _state.value
        if (s.isLoading || s.isLoadingMore) return
        if (!s.hasNext) return
        if (s.nextCursor.isNullOrBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true, errorMessage = null) }

            val http = repo.fetchNotifications(category = category, cursor = s.nextCursor, size = pageSize)
            val body = http.body()

            if (http.isSuccessful && body != null && body.isSuccess && body.result != null) {
                val r = body.result
                _state.update {
                    it.copy(
                        items = it.items + r.items,
                        nextCursor = r.nextCursor,
                        hasNext = r.hasNext,
                        isLoadingMore = false
                    )
                }
            } else {
                val msg = body?.message ?: "알림 추가 조회에 실패했습니다."
                _state.update { it.copy(isLoadingMore = false, errorMessage = msg) }
            }
        }
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            val http = repo.read(notificationId)
            val body = http.body()

            if (http.isSuccessful && body != null && body.isSuccess && body.result != null) {
                val updated = body.result
                _state.update { cur ->
                    cur.copy(
                        items = cur.items.map { item ->
                            if (item.id == notificationId) updated else item
                        }
                    )
                }
            } else {
                val msg = body?.message ?: "알림 읽음 처리에 실패했습니다."
                _state.update { it.copy(errorMessage = msg) }
            }
        }
    }
}