package com.bookiibookii.bookiibookii.library.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.library.ui.ReadingCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryBookmarkUiState(
    val isLoading: Boolean = false,
    val cards: List<ReadingCard> = emptyList(),
    // 화면 복귀 때마다 다시 조회하므로 정렬 선택을 여기에 두어야 순서가 유지된다
    val isLatest: Boolean = true,
    val errorMessage: String? = null,
)

class LibraryBookmarkViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryBookmarkUiState())
    val uiState: StateFlow<LibraryBookmarkUiState> = _uiState.asStateFlow()

    private var originalCards: List<ReadingCard> = emptyList()

    fun fetchBookmarkedCards() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = RetrofitClient.libApi().getBookmarkedCards()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    originalCards = (response.body()?.result ?: emptyList()).map { it.toReadingCard() }
                    _uiState.update {
                        it.copy(isLoading = false, cards = sorted(originalCards, it.isLatest))
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "북마크를 불러오지 못했습니다.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "네트워크 오류가 발생했습니다.") }
            }
        }
    }

    fun sortByLatest(isLatest: Boolean) {
        _uiState.update { it.copy(isLatest = isLatest, cards = sorted(originalCards, isLatest)) }
    }

    private fun sorted(cards: List<ReadingCard>, isLatest: Boolean): List<ReadingCard> =
        if (isLatest) cards.sortedByDescending { it.cardId } else cards.sortedBy { it.cardId }
}
