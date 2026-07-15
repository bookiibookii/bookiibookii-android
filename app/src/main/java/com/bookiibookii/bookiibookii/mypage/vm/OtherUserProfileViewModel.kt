package com.bookiibookii.bookiibookii.mypage.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.mypage.UserProfileResDTO
import kotlinx.coroutines.launch

class OtherUserProfileViewModel(val nickname: String) : ViewModel() {

    private val _profile = MutableLiveData<UserProfileResDTO?>()
    val profile: LiveData<UserProfileResDTO?> = _profile

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _hasError = MutableLiveData(false)
    val hasError: LiveData<Boolean> = _hasError

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        _isLoading.value = true
        _hasError.value = false
        viewModelScope.launch {
            try {
                val response = RetrofitClient.userApi().getUserProfile(nickname)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _profile.value = response.body()!!.result
                } else {
                    _hasError.value = true
                }
            } catch (e: Exception) {
                _hasError.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    class Factory(private val nickname: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OtherUserProfileViewModel(nickname) as T
    }
}
