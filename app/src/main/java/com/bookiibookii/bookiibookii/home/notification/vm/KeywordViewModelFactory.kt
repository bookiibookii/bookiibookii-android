package com.bookiibookii.bookiibookii.home.notification.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookiibookii.bookiibookii.home.notification.data.KeywordRepository

class KeywordViewModelFactory(
    private val repo: KeywordRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KeywordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KeywordViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}