package com.bookiibookii.bookiibookii.mypage.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.mypage.AddFavoriteBookRequest
import com.bookiibookii.bookiibookii.data.model.mypage.AddRepresentativeBookRequest
import com.bookiibookii.bookiibookii.data.model.mypage.BookshelfResult
import com.bookiibookii.bookiibookii.data.model.mypage.CompletedBook
import com.bookiibookii.bookiibookii.data.model.mypage.FavoriteBook
import com.bookiibookii.bookiibookii.data.model.mypage.RepresentativeBook
import com.bookiibookii.bookiibookii.data.model.mypage.UpdateRepresentativeOrderRequest
import com.bookiibookii.bookiibookii.onboarding.steps.model.BookSearchState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

enum class SortOrder { LATEST, OLDEST, RATING, TITLE }

class BookshelfViewModel : ViewModel() {

    private val _bookshelf = MutableLiveData<BookshelfResult?>(null)

    private val _bookSearchState = MutableLiveData<BookSearchState>(BookSearchState.Idle)
    val bookSearchState: LiveData<BookSearchState> get() = _bookSearchState

    private val _sortOrder = MutableLiveData<SortOrder>(SortOrder.LATEST)
    val sortOrder: LiveData<SortOrder> get() = _sortOrder

    val representativeBooks: LiveData<List<RepresentativeBook>> = _bookshelf.map { bookshelf ->
        bookshelf?.representativeBooks?.sortedBy { it.displayOrder } ?: emptyList()
    }

    val favoriteBooks: LiveData<List<FavoriteBook>> = _bookshelf.map { bookshelf ->
        bookshelf?.favoriteBooks ?: emptyList()
    }

    val representativeTitles: LiveData<Set<String>> = representativeBooks.map { books ->
        books.map { it.title }.toSet()
    }

    val sortedCompletedBooks: MediatorLiveData<List<CompletedBook>> = MediatorLiveData<List<CompletedBook>>().apply {
        addSource(_bookshelf) { recompute() }
        addSource(_sortOrder) { recompute() }
    }

    private fun recompute() {
        val books = _bookshelf.value?.completedBooks ?: emptyList()
        sortedCompletedBooks.value = when (_sortOrder.value ?: SortOrder.LATEST) {
            SortOrder.LATEST -> books.sortedByDescending { it.completedAt ?: "" }
            SortOrder.OLDEST -> books.sortedBy { it.completedAt ?: "9999" }
            SortOrder.RATING -> books.sortedByDescending { it.rating }
            SortOrder.TITLE -> books.sortedBy { it.title }
        }
    }

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class Event {
        data class ShowToast(val message: String) : Event()
    }

    init {
        fetchBookshelf()
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun fetchBookshelf() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().getBookshelf()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _bookshelf.value = response.body()?.result
                } else {
                    _eventFlow.emit(Event.ShowToast("책장 정보를 불러오지 못했습니다."))
                }
            } catch (e: Exception) {
                Log.e("BookshelfViewModel", "fetchBookshelf error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    fun searchBooks(query: String) {
        if (query.isBlank()) {
            _bookSearchState.value = BookSearchState.Idle
            return
        }
        viewModelScope.launch {
            _bookSearchState.value = BookSearchState.Loading
            runCatching { RetrofitClient.grpApi().searchBooks(query) }
                .onSuccess { response ->
                    val body = response.body()
                    if (body?.isSuccess == true && body.result != null) {
                        _bookSearchState.value = BookSearchState.Success(body.result.books)
                    } else {
                        _bookSearchState.value = BookSearchState.Error(body?.message ?: "검색에 실패했습니다.")
                    }
                }
                .onFailure { e ->
                    _bookSearchState.value = BookSearchState.Error(e.message ?: "네트워크 오류가 발생했습니다.")
                }
        }
    }

    fun clearBookSearch() {
        _bookSearchState.value = BookSearchState.Idle
    }

    fun addRepresentativeBook(memberBookId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().addRepresentativeBook(
                    AddRepresentativeBookRequest(memberBookId = memberBookId)
                )
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    fetchBookshelf()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "대표 도서 등록에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("BookshelfViewModel", "addRepresentativeBook error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    fun deleteRepresentativeBook(userBookId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().deleteRepresentativeBook(userBookId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    fetchBookshelf()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "대표 도서 삭제에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("BookshelfViewModel", "deleteRepresentativeBook error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    fun reorderRepresentativeBook(userBookId: Long, newOrder: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.mypApi().reorderRepresentativeBooks(
                    UpdateRepresentativeOrderRequest(userBookId = userBookId, targetOrder = newOrder)
                )
            } catch (e: Exception) {
                Log.e("BookshelfViewModel", "reorderRepresentativeBook error", e)
                _eventFlow.emit(Event.ShowToast("순서 변경에 실패했습니다."))
            }
        }
    }

    fun addFavoriteBook(isbn13: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().addFavoriteBook(AddFavoriteBookRequest(isbn13 = isbn13))
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    fetchBookshelf()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "인생 책 등록에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("BookshelfViewModel", "addFavoriteBook error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    fun deleteFavoriteBook(userBookId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().deleteFavoriteBook(userBookId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    fetchBookshelf()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "인생 책 삭제에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("BookshelfViewModel", "deleteFavoriteBook error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    fun replaceFavoriteBook(oldUserBookId: Long, isbn13: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.mypApi().deleteFavoriteBook(oldUserBookId)
                RetrofitClient.mypApi().addFavoriteBook(AddFavoriteBookRequest(isbn13 = isbn13))
                fetchBookshelf()
            } catch (e: Exception) {
                Log.e("BookshelfViewModel", "replaceFavoriteBook error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }
}
