package com.bookiibookii.bookiibookii.data.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bookiibookii.bookiibookii.data.model.LibBook

enum class SortType {
    TITLE, RATING_HIGH, RATING_LOW, RECENT, OLD
}

class LibraryViewModel : ViewModel() {

    // 1. 원본 데이터 (서버에서 받아온 순수 리스트)
    private val _originalList = MutableLiveData<List<LibBook>>(emptyList())

    // 2. 현재 정렬 기준
    private val _sortType = MutableLiveData<SortType>(SortType.TITLE)
    val sortType: LiveData<SortType> get() = _sortType

    // 3. UI에 보여줄 최종 리스트 (정렬이 적용된 결과)
    // MediatorLiveData를 사용하면 _originalList나 _sortType이 바뀔 때마다 자동으로 동작합니다.
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
            SortType.RECENT -> original.sortedByDescending { it.id } // 최신순
            SortType.OLD -> original.sortedBy { it.id }             // 오래된순
        }

        bookList.value = sorted
    }
}