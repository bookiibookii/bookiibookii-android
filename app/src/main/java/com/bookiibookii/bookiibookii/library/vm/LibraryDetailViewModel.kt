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
            Log.d("LibraryDetail", "fetchGroupCards 시작 — groupId=$groupId")
            try {
                val response = RetrofitClient.libApi().getGroupCards(groupId)
                Log.d("LibraryDetail", "응답 코드: ${response.code()}")
                Log.d("LibraryDetail", "isSuccessful: ${response.isSuccessful}")
                Log.d("LibraryDetail", "isSuccess(body): ${response.body()?.isSuccess}")

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    Log.d("LibraryDetail", "result: $result")
                    Log.d("LibraryDetail", "cards 개수: ${result?.cards?.size}")
                    result?.cards?.forEachIndexed { i, card ->
                        Log.d("LibraryDetail", "  card[$i] id=${card.cardId} type=${card.cardType} memo=${card.memo?.take(20)}")
                    }
                    val cards = result?.cards?.map { it.toReadingCard() } ?: emptyList()
                    Log.d("LibraryDetail", "변환된 ReadingCard 개수: ${cards.size}")
                    _uiState.update { it.copy(isLoading = false, cards = cards) }
                } else {
                    val errBody = response.errorBody()?.string()
                    Log.e("LibraryDetail", "실패 — code=${response.code()}, errBody=$errBody")
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
                        )
                    }
                }
            } catch (_: Exception) { }
        }
    }

    fun addRepresentative(memberBookId: Int) {
        viewModelScope.launch {
            try {
                val request = AddRepresentativeBookRequest(memberBookId = memberBookId.toLong())
                Log.d("Representative", "대표도서 등록 요청 → memberBookId=$memberBookId, request=$request")

                val response = RetrofitClient.mypApi().addRepresentativeBook(request)
                Log.d("Representative", "응답 코드: ${response.code()}")
                Log.d("Representative", "응답 body: ${response.body()}")

                if (!response.isSuccessful) {
                    val errBody = response.errorBody()?.string()
                    Log.e("Representative", "실패 에러 body: $errBody")
                }

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _uiState.update { it.copy(isRepresentative = true) }
                    _event.emit("대표 도서로 등록되었습니다.")
                } else {
                    Log.e("Representative", "isSuccess=false, code=${response.code()}, message=${response.message()}")
                    _event.emit("대표 도서 등록에 실패했습니다.")
                }
            } catch (e: Exception) {
                Log.e("Representative", "예외 발생: ${e.message}", e)
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

