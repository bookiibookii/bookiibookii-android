package com.bookiibookii.bookiibookii.home

import com.bookiibookii.bookiibookii.data.model.ProfileResult

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: ProfileResult? = null,
    val errorMessage: String? = null
)