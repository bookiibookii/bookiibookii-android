package com.bookiibookii.bookiibookii.home.notification.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.home.notification.data.KeywordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class KeywordViewModel(
    private val repo: KeywordRepository
) : ViewModel() {

    private val _state = MutableStateFlow(KeywordUiState())
    val state: StateFlow<KeywordUiState> = _state

    fun load(sort: String = _state.value.sort) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, sort = sort) }

            val http = repo.fetchKeywords(sort)
            val body = http.body()

            if (http.isSuccessful && body != null && body.isSuccess && body.result != null) {
                val r = body.result
                _state.update {
                    it.copy(
                        items = r.keywordList,
                        isLoading = false
                    )
                }
            } else {
                val msg = body?.message ?: "키워드 목록 조회에 실패했습니다."
                _state.update { it.copy(isLoading = false, errorMessage = msg) }
            }
        }
    }

    fun add(content: String) {
        val trimmed = content.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(errorMessage = null) }

            val http = repo.addKeyword(trimmed)
            val body = http.body()

            if (http.isSuccessful && body != null && body.isSuccess && body.result != null) {
                // 등록 후 목록 다시 조회(정렬 유지)
                load(_state.value.sort)
            } else {
                val msg = body?.message ?: "키워드 등록에 실패했습니다."
                _state.update { it.copy(errorMessage = msg) }
            }
        }
    }

    fun delete(keywordId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(errorMessage = null) }

            val http = repo.removeKeyword(keywordId)
            val body = http.body()

            if (http.isSuccessful && body != null && body.isSuccess) {
                // 삭제 후 목록 다시 조회(정렬 유지)
                load(_state.value.sort)
            } else {
                val msg = body?.message ?: "키워드 삭제에 실패했습니다."
                _state.update { it.copy(errorMessage = msg) }
            }
        }
    }
}