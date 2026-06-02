package com.bookiibookii.bookiibookii.data.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.library.MemberCardListResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberCardResponseDTO
import kotlinx.coroutines.launch

class LibraryCardViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _groupCardResult = MutableLiveData<MemberCardListResponseDTO?>()
    val groupCardResult: LiveData<MemberCardListResponseDTO?> get() = _groupCardResult

    private val _cardList = MutableLiveData<List<MemberCardResponseDTO>>()
    val cardList: LiveData<List<MemberCardResponseDTO>> get() = _cardList

    private var originalCards: List<MemberCardResponseDTO> = emptyList()

    fun fetchGroupCards(groupId: Int) {
        if (groupId == -1) return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.libApi().getGroupCards(groupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    if (result != null) {
                        _groupCardResult.value = result
                        originalCards = result.cards.sortedByDescending { it.createdAt }
                        _cardList.value = originalCards
                    }
                } else {
                    _errorMessage.value = "데이터를 불러오는 데 실패했습니다."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchBookmarkedCards() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.libApi().getBookmarkedCards()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val rawCards = response.body()?.result ?: emptyList()
                    originalCards = rawCards.sortedByDescending { it.createdAt }
                    _cardList.value = originalCards
                } else {
                    _errorMessage.value = "북마크를 불러오는 데 실패했습니다."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sortCards(isLately: Boolean) {
        _cardList.value = if (isLately) {
            originalCards.sortedByDescending { it.createdAt }
        } else {
            originalCards.sortedBy { it.createdAt }
        }
    }
}
