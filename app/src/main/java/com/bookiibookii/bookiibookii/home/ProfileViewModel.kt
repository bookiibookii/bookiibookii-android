package com.bookiibookii.bookiibookii.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repo: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()


    fun loadProfile(nickname: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                val profile = repo.getUserProfile(nickname)

                if (profile != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        profile = profile,
                        errorMessage = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        profile = null,
                        errorMessage = "프로필 조회 실패"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    profile = null,
                    errorMessage = "네트워크 오류"
                )
            }
        }
    }
}