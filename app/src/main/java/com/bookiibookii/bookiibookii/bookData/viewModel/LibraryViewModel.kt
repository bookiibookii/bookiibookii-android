package com.bookiibookii.bookiibookii.bookData.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

enum class SortType {
    TITLE, RATING_HIGH, RATING_LOW, RECENT, OLD
}

class LibraryViewModel : ViewModel() {

    private val _sortType = MutableLiveData<SortType>(SortType.TITLE)
    val sortType: LiveData<SortType> get() = _sortType

    fun setSortType(type: SortType) {
        _sortType.value = type
    }
}