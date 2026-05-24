package com.bookiibookii.bookiibookii.group.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.group.BookItem
import com.bookiibookii.bookiibookii.group.model.ExchangeType
import com.bookiibookii.bookiibookii.group.model.GroupEditorUiState
import com.bookiibookii.bookiibookii.group.model.ReadingStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupEditorViewModel : ViewModel() {

    private val _state = MutableStateFlow(GroupEditorUiState())
    val state: StateFlow<GroupEditorUiState> = _state

    // 입력 텍스트 변경. 텍스트를 수정하면 기존 선택은 해제
    fun onBookSearchQueryChange(value: String) =
        _state.update { it.copy(bookSearchQuery = value, isbn13 = null) }

    // ic_search 클릭 또는 키보드 검색 액션으로 호출
    fun searchBooks() {
        val q = _state.value.bookSearchQuery
        if (q.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(bookSearchLoading = true, bookSearchError = null) }
            try {
                val res = RetrofitClient.grpApi().searchBooks(q, page = 1, size = 10)
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    _state.update {
                        it.copy(
                            bookSearchResults = res.body()?.result?.books.orEmpty(),
                            bookSearchLoading = false,
                        )
                    }
                } else {
                    _state.update {
                        it.copy(bookSearchError = "검색에 실패했어요", bookSearchLoading = false)
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(bookSearchError = "네트워크 오류가 발생했어요", bookSearchLoading = false)
                }
            }
        }
    }

    fun onBookSelect(book: BookItem) = _state.update {
        it.copy(
            isbn13 = book.isbn13,
            bookSearchQuery = book.title,
            bookSearchResults = emptyList(),
        )
    }

    // ic_x 클릭. 텍스트 모두 초기화
    fun onClearBookSearch() = _state.update {
        it.copy(
            bookSearchQuery = "",
            isbn13 = null,
            bookSearchResults = emptyList(),
            bookSearchError = null,
        )
    }

    fun onGroupNameChange(value: String) =
        _state.update { it.copy(groupName = value) }

    fun onTradeTypeSelect(type: ExchangeType) =
        _state.update { it.copy(tradeType = type) }

    fun onReadingPeriodSelect(index: Int) =
        _state.update { it.copy(readingPeriodIndex = index) }

    fun onRuleStyleSelect(style: ReadingStyle) =
        _state.update { it.copy(ruleStyle = style) }

    fun onGroupCommentChange(value: String) =
        _state.update { it.copy(groupComment = value) }

    fun onAddCustomRule() = _state.update {
        if (it.customRules.size >= GroupEditorUiState.MAX_CUSTOM_RULES) it
        else it.copy(customRules = it.customRules + "")
    }

    fun onCustomRuleChange(index: Int, value: String) = _state.update {
        it.copy(
            customRules = it.customRules.toMutableList()
                .apply { if (index in indices) this[index] = value },
        )
    }

    fun onRemoveCustomRule(index: Int) = _state.update {
        it.copy(customRules = it.customRules.filterIndexed { i, _ -> i != index })
    }
}
