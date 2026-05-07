package com.bookiibookii.bookiibookii.home.notification.vm

import com.bookiibookii.bookiibookii.data.model.keyword.KeywordItem

data class KeywordUiState(
    val items: List<KeywordItem> = emptyList(),
    val sort: String = "LATEST",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
