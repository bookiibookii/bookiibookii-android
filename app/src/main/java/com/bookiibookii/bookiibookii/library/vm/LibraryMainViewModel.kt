package com.bookiibookii.bookiibookii.library.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.library.BookResult
import com.bookiibookii.bookiibookii.library.ui.LibraryBook
import com.bookiibookii.bookiibookii.library.ui.LibrarySortType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryMainUiState(
    val isLoading: Boolean = false,
    val readingBooks: List<LibraryBook> = emptyList(),
    val doneBooks: List<LibraryBook> = emptyList(),
    val sortType: LibrarySortType = LibrarySortType.RECENT,
    val errorMessage: String? = null,
)

class LibraryMainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryMainUiState())
    val uiState: StateFlow<LibraryMainUiState> = _uiState.asStateFlow()

    private var rawReadingBooks: List<LibraryBook> = emptyList()
    private var rawDoneBooks: List<LibraryBook> = emptyList()

    fun fetchBooks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = RetrofitClient.libApi().getLibraryBooks()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val results = response.body()?.result ?: emptyList()
                    rawReadingBooks = results.filterNot { it.isDone() }.map { it.toUiModel() }
                    rawDoneBooks   = results.filter { it.isDone() }.map { it.toUiModel() }
                    applySorting()
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "목록을 불러오는 데 실패했습니다.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "네트워크 오류가 발생했습니다.") }
            }
        }
    }

    fun setSortType(sortType: LibrarySortType) {
        _uiState.update { it.copy(sortType = sortType) }
        applySorting()
    }

    private fun applySorting() {
        val sort = _uiState.value.sortType
        _uiState.update {
            it.copy(
                isLoading = false,
                readingBooks = sort(rawReadingBooks, sort),
                doneBooks    = sort(rawDoneBooks, sort),
            )
        }
    }

    private fun sort(books: List<LibraryBook>, type: LibrarySortType): List<LibraryBook> = when (type) {
        LibrarySortType.RECENT -> books.sortedByDescending { it.memberBookId }
        LibrarySortType.OLDEST -> books.sortedBy { it.memberBookId }
        LibrarySortType.RATING -> books.sortedByDescending { it.rating ?: 0 }
        LibrarySortType.TITLE  -> books.sortedBy { it.title }
    }
}

private fun BookResult.isDone() = groupStatus == "COMPLETED"

private fun BookResult.toUiModel() = LibraryBook(
    groupId      = groupId,
    memberBookId = memberBookId,
    groupName    = groupName,
    title        = title,
    author       = author,
    genre        = genre.orEmpty(),
    coverUrl     = image,
    progress     = if (!isDone()) progressRate / 100f else null,
    rating       = if (isDone()) rating.toInt().coerceIn(0, 5) else null,
    startDate    = startDate.orEmpty(),
    endDate      = endDate,
    completedAt  = completedAt,
    totalPages   = totalPages,
)
