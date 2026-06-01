package com.bookiibookii.bookiibookii.library.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.mypage.AddRepresentativeBookRequest
import com.bookiibookii.bookiibookii.library.ui.ReadingCard
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryDetailUiState(
    val isLoading: Boolean = false,
    val cards: List<ReadingCard> = emptyList(),
    val isRepresentative: Boolean = false,
    val representativeUserBookId: Long = -1L,
    val errorMessage: String? = null,
)

class LibraryDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryDetailUiState())
    val uiState: StateFlow<LibraryDetailUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<String>()
    val event: SharedFlow<String> = _event.asSharedFlow()

    fun fetchGroupCards(groupId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = RetrofitClient.libApi().getGroupCards(groupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val cards = response.body()?.result?.cards?.map { it.toReadingCard() } ?: emptyList()
                    _uiState.update { it.copy(isLoading = false, cards = cards) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "독서카드를 불러오지 못했습니다.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "네트워크 오류가 발생했습니다.") }
            }
        }
    }

    fun checkRepresentativeStatus(memberBookId: Int, bookTitle: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().getBookshelf()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val representatives = response.body()?.result?.representativeBooks ?: emptyList()
                    val match = representatives.firstOrNull { it.title == bookTitle }
                    _uiState.update {
                        it.copy(
                            isRepresentative        = match != null,
                            representativeUserBookId = match?.userBookId ?: -1L,
                        )
                    }
                }
            } catch (_: Exception) { }
        }
    }

    fun addRepresentative(memberBookId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().addRepresentativeBook(
                    AddRepresentativeBookRequest(memberBookId = memberBookId.toLong())
                )
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _uiState.update { it.copy(isRepresentative = true) }
                    _event.emit("대표 도서로 등록되었습니다.")
                } else {
                    _event.emit("대표 도서 등록에 실패했습니다.")
                }
            } catch (e: Exception) {
                _event.emit("네트워크 오류가 발생했습니다.")
            }
        }
    }

    fun removeRepresentative(userBookId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().deleteRepresentativeBook(userBookId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _uiState.update { it.copy(isRepresentative = false, representativeUserBookId = -1L) }
                    _event.emit("대표 도서 등록이 해제되었습니다.")
                } else {
                    _event.emit("대표 도서 해제에 실패했습니다.")
                }
            } catch (e: Exception) {
                _event.emit("네트워크 오류가 발생했습니다.")
            }
        }
    }

    fun deleteMemberBook(memberBookId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.libApi().deleteMemberBook(memberBookId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _event.emit("서재에서 삭제되었습니다.")
                    onSuccess()
                } else {
                    _event.emit("삭제에 실패했습니다.")
                }
            } catch (e: Exception) {
                _event.emit("네트워크 오류가 발생했습니다.")
            }
        }
    }
}

