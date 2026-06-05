package com.bookiibookii.bookiibookii.placesearch.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.KakaoRetrofitClient
import com.bookiibookii.bookiibookii.placesearch.data.PlaceSearchRepository
import com.bookiibookii.bookiibookii.placesearch.model.PlaceSearchUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaceSearchViewModel(
    private val repository: PlaceSearchRepository =
        PlaceSearchRepository(KakaoRetrofitClient.kakaoLocalApi),
) : ViewModel() {

    private val _state = MutableStateFlow(PlaceSearchUiState())
    val state: StateFlow<PlaceSearchUiState> = _state

    // 검색바 입력 변경
    fun onQueryChange(value: String) =
        _state.update { it.copy(query = value) }

    // 검색 제출. 빈 검색어면 결과 비움, 아니면 검색어 확정 후 첫 페이지 로드
    fun onSearch() {
        val keyword = _state.value.query.trim()
        if (keyword.isBlank()) {
            _state.update {
                it.copy(
                    searchKeyword = "",
                    results = emptyList(),
                    totalCount = null,
                    hasNext = false,
                    error = null,
                )
            }
            return
        }
        _state.update { it.copy(searchKeyword = keyword) }
        loadFirstPage()
    }

    fun retry() = loadFirstPage()

    // 첫 페이지 로드
    private fun loadFirstPage() {
        val keyword = _state.value.searchKeyword
        if (keyword.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val page = repository.searchPlaces(query = keyword, page = FIRST_PAGE)
                if (page != null) {
                    _state.update {
                        it.copy(
                            results = page.results,
                            currentPage = FIRST_PAGE,
                            hasNext = !page.isEnd,
                            totalCount = page.totalCount,
                            loading = false,
                        )
                    }
                } else {
                    _state.update { it.copy(error = "검색에 실패했어요", loading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "네트워크 오류가 발생했어요", loading = false) }
            }
        }
    }

    // 다음 페이지 이어붙이기 (무한 스크롤)
    fun loadMore() {
        val s = _state.value
        if (!s.hasNext || s.loading || s.loadingMore) return
        val nextPage = s.currentPage + 1
        viewModelScope.launch {
            _state.update { it.copy(loadingMore = true) }
            try {
                val page = repository.searchPlaces(query = s.searchKeyword, page = nextPage)
                if (page != null) {
                    _state.update {
                        it.copy(
                            results = it.results + page.results,
                            currentPage = nextPage,
                            hasNext = !page.isEnd,
                            loadingMore = false,
                        )
                    }
                } else {
                    // 추가 로드 실패는 멈춤(기존 결과 유지)
                    _state.update { it.copy(loadingMore = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(loadingMore = false) }
            }
        }
    }

    companion object {
        private const val FIRST_PAGE = 1
    }
}
