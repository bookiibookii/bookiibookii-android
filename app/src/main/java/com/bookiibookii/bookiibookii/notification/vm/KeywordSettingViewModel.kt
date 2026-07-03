package com.bookiibookii.bookiibookii.notification.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.keyword.KeywordCreateRequest
import com.bookiibookii.bookiibookii.notification.model.KeywordUiModel
import com.bookiibookii.bookiibookii.notification.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KeywordSettingUiState(
    val keywordInput: String = "",
    val keywords: List<KeywordUiModel> = emptyList(),
    val isLoading: Boolean = false,
)

class KeywordSettingViewModel : ViewModel() {

    private val _state = MutableStateFlow(KeywordSettingUiState())
    val state: StateFlow<KeywordSettingUiState> = _state.asStateFlow()

    // 새 화면엔 정렬 토글이 없어 최신순(LATEST) 고정
    private val sort = "LATEST"

    // 키워드 추가 진행 중 여부 — 더블탭으로 같은 키워드가 중복 등록되는 것을 차단
    private var adding = false

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                RetrofitClient.kwdApi().getKeywords(sort)
            }.onSuccess { http ->
                val result = http.body()?.takeIf { http.isSuccessful && it.isSuccess }?.result
                if (result != null) {
                    _state.update { it.copy(keywords = result.keywordList.map { dto -> dto.toUiModel() }, isLoading = false) }
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            }.onFailure {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onInputChange(text: String) {
        _state.update { it.copy(keywordInput = text) }
    }

    fun addKeyword(content: String, onSuccess: () -> Unit) {
        if (adding) return
        adding = true
        viewModelScope.launch {
            runCatching {
                RetrofitClient.kwdApi().createKeyword(KeywordCreateRequest(content = content))
            }.onSuccess { http ->
                if (http.isSuccessful && http.body()?.isSuccess == true) {
                    _state.update { it.copy(keywordInput = "") }
                    load()
                    onSuccess()
                }
            }
            adding = false
        }
    }

    fun deleteKeyword(item: KeywordUiModel) {
        viewModelScope.launch {
            runCatching {
                RetrofitClient.kwdApi().deleteKeyword(item.id)
            }.onSuccess { http ->
                if (http.isSuccessful && http.body()?.isSuccess == true) {
                    load()
                }
            }
        }
    }
}
