package com.bookiibookii.bookiibookii.data.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bookiibookii.bookiibookii.data.model.LibBook

enum class SortType {
    TITLE, RATING_HIGH, RATING_LOW, RECENT, OLD
}

class LibraryViewModel : ViewModel() {

    // 기본 정렬값: 제목 순
    private val _sortType = MutableLiveData<SortType>(SortType.TITLE)
    val sortType: LiveData<SortType> get() = _sortType

    private val _bookList = MutableLiveData<List<LibBook>>(emptyList())
    val bookList: LiveData<List<LibBook>> get() = _bookList

    fun setSortType(type: SortType) {
        _sortType.value = type
    }

    fun setBookList(books: List<LibBook>) {
        _bookList.value = books
    }
}