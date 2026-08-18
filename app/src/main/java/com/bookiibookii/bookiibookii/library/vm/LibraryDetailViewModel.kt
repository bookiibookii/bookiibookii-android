package com.bookiibookii.bookiibookii.library.vm

import android.util.Log
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
    val representativeCount: Int = 0,
    val errorMessage: String? = null,
)

data class LibraryDetailToastEvent(val message: String, val isSuccess: Boolean)

class LibraryDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryDetailUiState())
    val uiState: StateFlow<LibraryDetailUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<LibraryDetailToastEvent>()
    val event: SharedFlow<LibraryDetailToastEvent> = _event.asSharedFlow()

    // 서버가 memberBookId 기준으로 같은 그룹·같은 책 카드만 반환하므로 클라이언트 필터링 불필요
    fun fetchCards(memberBookId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = RetrofitClient.libApi().getMemberBookCards(memberBookId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    val cards = (result?.cards ?: emptyList()).map { it.toReadingCard() }
                    _uiState.update { it.copy(isLoading = false, cards = cards) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "독서카드를 불러오지 못했습니다.") }
                }
            } catch (e: Exception) {
                Log.e("LibraryDetail", "예외 발생: ${e.message}", e)
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
                            representativeCount      = representatives.size,
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("LibraryDetail", "checkRepresentativeStatus 실패: ${e.message}", e)
            }
        }
    }

    fun addRepresentative(memberBookId: Int) {
        viewModelScope.launch {
            if (_uiState.value.representativeCount >= 7) {
                _event.emit(LibraryDetailToastEvent("대표책은 최대 7개까지만 등록 가능합니다", false))
                return@launch
            }
            try {
                val request = AddRepresentativeBookRequest(memberBookId = memberBookId.toLong())
                val response = RetrofitClient.mypApi().addRepresentativeBook(request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _uiState.update { it.copy(isRepresentative = true) }
                    _event.emit(LibraryDetailToastEvent("대표 도서로 등록되었습니다.", true))
                } else {
                    _event.emit(LibraryDetailToastEvent("대표 도서 등록에 실패했습니다.", false))
                }
            } catch (e: Exception) {
                Log.e("Representative", "예외 발생: ${e.message}", e)
                _event.emit(LibraryDetailToastEvent("네트워크 오류가 발생했습니다.", false))
            }
        }
    }

    fun removeRepresentative(userBookId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().deleteRepresentativeBook(userBookId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _uiState.update { it.copy(isRepresentative = false, representativeUserBookId = -1L) }
                    _event.emit(LibraryDetailToastEvent("대표 도서 등록이 해제되었습니다.", true))
                } else {
                    _event.emit(LibraryDetailToastEvent("대표 도서 해제에 실패했습니다.", false))
                }
            } catch (e: Exception) {
                _event.emit(LibraryDetailToastEvent("네트워크 오류가 발생했습니다.", false))
            }
        }
    }

    // 서재 삭제 진행 중 여부 — 더블탭으로 중복 DELETE/중복 뒤로가기를 차단
    private var deleting = false

    fun deleteMemberBook(memberBookId: Int, onSuccess: () -> Unit) {
        if (deleting) return
        deleting = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.libApi().deleteMemberBook(memberBookId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _event.emit(LibraryDetailToastEvent("서재에서 삭제되었습니다.", true))
                    onSuccess()
                } else {
                    _event.emit(LibraryDetailToastEvent("삭제에 실패했습니다.", false))
                }
            } catch (e: Exception) {
                _event.emit(LibraryDetailToastEvent("네트워크 오류가 발생했습니다.", false))
            } finally {
                deleting = false
            }
        }
    }
}
