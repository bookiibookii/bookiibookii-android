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
                    val cards = (response.body()?.result ?: emptyList()).map { it.toReadingCard() }
                    originalCards = cards.sortedByDescending { it.date }
                    _uiState.update { it.copy(isLoading = false, cards = originalCards) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "북마크를 불러오지 못했습니다.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "네트워크 오류가 발생했습니다.") }
            }
        }
    }

    fun sortByLatest(isLatest: Boolean) {
        val sorted = if (isLatest) {
            originalCards.sortedByDescending { it.date }
        } else {
            originalCards.sortedBy { it.date }
        }
        _uiState.update { it.copy(cards = sorted) }
    }
}
