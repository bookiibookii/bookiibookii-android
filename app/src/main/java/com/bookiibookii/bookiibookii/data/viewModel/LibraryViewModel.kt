package com.bookiibookii.bookiibookii.data.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bookiibookii.bookiibookii.data.model.library.LibBook

enum class SortType {
    TITLE, RATING_HIGH, RATING_LOW, RECENT, OLD
}

class LibraryViewModel : ViewModel() {

    private val _originalList = MutableLiveData<List<LibBook>>(emptyList())

    private val _sortType = MutableLiveData<SortType>(SortType.TITLE)
    val sortType: LiveData<SortType> get() = _sortType

    val bookList = MediatorLiveData<List<LibBook>>().apply {
        addSource(_originalList) { updateSortedList() }
        addSource(_sortType) { updateSortedList() }
    }

    fun setBookList(books: List<LibBook>) {
        _originalList.value = books
    }

    fun setSortType(type: SortType) {
        _sortType.value = type
    }

    private fun updateSortedList() {
        val original = _originalList.value ?: emptyList()
        val type = _sortType.value ?: SortType.TITLE

        if (original.isEmpty()) {
            bookList.value = emptyList()
            return
        }

        val sorted = when (type) {
            SortType.TITLE -> original.sortedBy { it.title }
            SortType.RATING_HIGH -> original.sortedByDescending { it.rating }
            SortType.RATING_LOW -> original.sortedBy { it.rating }
            SortType.RECENT -> original.sortedByDescending { it.memberBookId }
            SortType.OLD -> original.sortedBy { it.memberBookId }
        }

        bookList.value = sorted
    }
}