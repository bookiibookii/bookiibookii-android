package com.bookiibookii.bookiibookii.onboarding.steps.model

import com.bookiibookii.bookiibookii.data.model.group.BookItem

sealed class BookSearchState {
    data object Idle : BookSearchState()
    data object Loading : BookSearchState()
    data class Success(val books: List<BookItem>) : BookSearchState()
    data class Error(val message: String) : BookSearchState()
}
