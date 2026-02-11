package com.bookiibookii.bookiibookii.home.notification.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.common.ComErrorActivity
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
                    errorType = null
                )
            }

            try {
                val http = repo.fetchNotifications(category, null, pageSize)
                val body = http.body()

                if (http.isSuccessful && body?.isSuccess == true && body.result != null) {
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
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorType = ComErrorActivity.TYPE_SYSTEM_ERROR
                        )
                    }
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorType = ComErrorActivity.TYPE_NETWORK_ERROR
                    )
                }
            }
        }
    }

    fun loadNextPage() {
        val s = _state.value
        if (s.isLoading || s.isLoadingMore) return
        if (!s.hasNext) return
        if (s.nextCursor.isNullOrBlank()) return

        viewModelScope.launch {

            _state.update { it.copy(isLoadingMore = true, errorType = null) }

            try {
                val http = repo.fetchNotifications(category, s.nextCursor, pageSize)
                val body = http.body()

                if (http.isSuccessful && body?.isSuccess == true && body.result != null) {
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
                    _state.update {
                        it.copy(
                            isLoadingMore = false,
                            errorType = ComErrorActivity.TYPE_SYSTEM_ERROR
                        )
                    }
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingMore = false,
                        errorType = ComErrorActivity.TYPE_NETWORK_ERROR
                    )
                }
            }
        }
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            try {
                val http = repo.read(notificationId)
                val body = http.body()

                if (http.isSuccessful && body?.isSuccess == true && body.result != null) {
                    val updated = body.result
                    _state.update { cur ->
                        cur.copy(
                            items = cur.items.map { item ->
                                if (item.id == notificationId) updated else item
                            }
                        )
                    }
                }
            } catch (_: Exception) {
                // 읽음 실패는 에러 화면 안 띄움
            }
        }
    }
}