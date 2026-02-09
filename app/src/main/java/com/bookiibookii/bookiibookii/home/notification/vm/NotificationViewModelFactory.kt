package com.bookiibookii.bookiibookii.home.notification.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bookiibookii.bookiibookii.home.notification.data.NotificationRepository

class NotificationViewModelFactory(
    private val repo: NotificationRepository,
    private val category: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(repo, category) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
