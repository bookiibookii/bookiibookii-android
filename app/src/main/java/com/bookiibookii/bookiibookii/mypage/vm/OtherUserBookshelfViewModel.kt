package com.bookiibookii.bookiibookii.mypage.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.mypage.OtherUserBookshelfResult
import kotlinx.coroutines.launch

class OtherUserBookshelfViewModel(val nickname: String) : ViewModel() {

    enum class BookshelfError { DEACTIVATED, NOT_FOUND, GENERIC }

    private val _bookshelf = MutableLiveData<OtherUserBookshelfResult?>()
    val bookshelf: LiveData<OtherUserBookshelfResult?> = _bookshelf

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<BookshelfError?>()
    val error: LiveData<BookshelfError?> = _error

    init { fetchBookshelf() }

    fun fetchBookshelf() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.userApi().getOtherUserBookshelf(nickname)
                when {
                    response.isSuccessful && response.body()?.isSuccess == true ->
                        _bookshelf.value = response.body()!!.result
                    response.code() == 403 -> _error.value = BookshelfError.DEACTIVATED
                    response.code() == 404 -> _error.value = BookshelfError.NOT_FOUND
                    else -> _error.value = BookshelfError.GENERIC
                }
            } catch (e: Exception) {
                _error.value = BookshelfError.GENERIC
            } finally {
                _isLoading.value = false
            }
        }
    }

    class Factory(private val nickname: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OtherUserBookshelfViewModel(nickname) as T
    }
}
