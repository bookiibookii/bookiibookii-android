package com.bookiibookii.bookiibookii.home.notification.vm

import com.bookiibookii.bookiibookii.data.model.KeywordItemDto

data class KeywordUiState(
    val items: List<KeywordItemDto> = emptyList(),
    val sort: String = "LATEST",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)