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
import com.bookiibookii.bookiibookii.common.observeSearchQuery
import com.bookiibookii.bookiibookii.onboarding.steps.model.BookSearchState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

enum class SortOrder { LATEST, OLDEST, RATING, TITLE }

data class GroupReviewNavTarget(
    val groupId: Int,
    val bookTitle: String,
    val groupName: String,
    val startDate: String,
    val endDate: String,
)

class BookshelfViewModel : ViewModel() {

    private val _bookshelf = MutableLiveData<BookshelfResult?>(null)

    private val _bookSearchState = MutableLiveData<BookSearchState>(BookSearchState.Idle)
    val bookSearchState: LiveData<BookSearchState> get() = _bookSearchState

    private val _bookSearchQuery = MutableStateFlow("")

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
        val bookshelf = _bookshelf.value
        val books = bookshelf?.completedBooks ?: emptyList()
        val representativeTitles = (bookshelf?.representativeBooks ?: emptyList()).map { it.title }.toSet()

        val sorted = when (_sortOrder.value ?: SortOrder.LATEST) {
            SortOrder.LATEST -> books.sortedByDescending { it.completedAt ?: "" }
            SortOrder.OLDEST -> books.sortedBy { it.completedAt ?: "9999" }
            SortOrder.RATING -> books.sortedByDescending { it.rating }
            SortOrder.TITLE -> books.sortedBy { it.title }
        }
        // 대표책을 앞에, 나머지를 뒤에 (안정 정렬이라 그룹 내부는 선택된 정렬 기준이 그대로 유지됨)
        sortedCompletedBooks.value = sorted.sortedByDescending { it.title in representativeTitles }
    }

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class Event {
        data class ShowToast(val message: String, val isSuccess: Boolean = false) : Event()
    }

    init {
        fetchBookshelf()
        observeSearchQuery(
            queryFlow = _bookSearchQuery,
            onBelowMinLength = { _bookSearchState.value = BookSearchState.Idle },
            onSearch = { performBookSearch(it) },
        )
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun fetchBookshelf() {
        viewModelScope.launch { loadBookshelf() }
    }

    private suspend fun loadBookshelf(): BookshelfResult? {
        return try {
            val response = RetrofitClient.mypApi().getBookshelf()
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                val result = response.body()?.result
                _bookshelf.value = result
                result
            } else {
                _eventFlow.emit(Event.ShowToast("책장 정보를 불러오지 못했습니다."))
                null
            }
        } catch (e: Exception) {
            Log.e("BookshelfViewModel", "fetchBookshelf error", e)
            _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            null
        }
    }

    fun onBookSearchQueryChange(query: String) {
        _bookSearchQuery.value = query
    }

    fun searchBooks() {
        val query = _bookSearchQuery.value.trim()
        if (query.isBlank()) return
        viewModelScope.launch { performBookSearch(query) }
    }

    private suspend fun performBookSearch(query: String) {
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

    fun clearBookSearch() {
        _bookSearchQuery.value = ""
        _bookSearchState.value = BookSearchState.Idle
    }

    // 나의 책장 응답(CompletedBookDto)에는 groupName/기간이 없어, 그룹 후기 화면 진입 전
    // 그룹 상세 API로 보강한다.
    fun fetchGroupReviewTarget(groupId: Long, bookTitle: String, onResult: (GroupReviewNavTarget) -> Unit) {
        viewModelScope.launch {
            val target = try {
                val resp = RetrofitClient.grpApi().getGroupDetail(groupId)
                val detail = resp.body()?.result
                if (resp.isSuccessful && resp.body()?.isSuccess == true && detail != null) {
                    val startDate = detail.startDate.orEmpty()
                    val endDate = if (startDate.isNotBlank()) {
                        try {
                            java.time.LocalDate.parse(startDate).plusDays(detail.readingPeriod.toLong()).toString()
                        } catch (_: Exception) {
                            ""
                        }
                    } else ""
                    GroupReviewNavTarget(
                        groupId = groupId.toInt(),
                        bookTitle = bookTitle,
                        groupName = detail.groupName,
                        startDate = startDate,
                        endDate = endDate,
                    )
                } else {
                    GroupReviewNavTarget(groupId = groupId.toInt(), bookTitle = bookTitle, groupName = "", startDate = "", endDate = "")
                }
            } catch (e: Exception) {
                Log.e("BookshelfViewModel", "fetchGroupReviewTarget error", e)
                GroupReviewNavTarget(groupId = groupId.toInt(), bookTitle = bookTitle, groupName = "", startDate = "", endDate = "")
            }
            onResult(target)
        }
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
                val response = RetrofitClient.mypApi().reorderRepresentativeBooks(
                    UpdateRepresentativeOrderRequest(userBookId = userBookId, targetOrder = newOrder)
                )
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    loadBookshelf()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "순서 변경에 실패했습니다."))
                }
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
                    ensureFavoritesAreRepresentative()
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
                    val isRepresentative = _bookshelf.value?.representativeBooks
                        ?.any { it.userBookId == userBookId } == true
                    if (isRepresentative) {
                        runCatching { RetrofitClient.mypApi().deleteRepresentativeBook(userBookId) }
                    }
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
                val response = RetrofitClient.mypApi().replaceFavoriteBook(
                    oldUserBookId, AddFavoriteBookRequest(isbn13 = isbn13)
                )
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    ensureFavoritesAreRepresentative()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "인생 책 교체에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("BookshelfViewModel", "replaceFavoriteBook error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    private suspend fun ensureFavoritesAreRepresentative() {
        val bookshelf = loadBookshelf() ?: return
        val representativeIds = (bookshelf.representativeBooks ?: emptyList()).map { it.userBookId }.toSet()
        val missing = (bookshelf.favoriteBooks ?: emptyList()).filter { it.userBookId !in representativeIds }
        if (missing.isEmpty()) return

        var anyAdded = false
        for (favorite in missing) {
            runCatching {
                RetrofitClient.mypApi().addRepresentativeBook(
                    AddRepresentativeBookRequest(userBookId = favorite.userBookId)
                )
            }.onSuccess { resp ->
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    anyAdded = true
                } else {
                    _eventFlow.emit(Event.ShowToast(resp.body()?.message ?: "대표 도서 등록에 실패했습니다."))
                }
            }.onFailure { e ->
                Log.e("BookshelfViewModel", "ensureFavoritesAreRepresentative error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
        if (anyAdded) loadBookshelf()
    }
}
