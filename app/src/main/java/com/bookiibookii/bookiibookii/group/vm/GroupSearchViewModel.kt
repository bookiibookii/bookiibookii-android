package com.bookiibookii.bookiibookii.group.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.group.GroupItem
import com.bookiibookii.bookiibookii.group.model.GroupSearchUiState
import com.bookiibookii.bookiibookii.group.nav.GroupDestinations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupSearchViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(GroupSearchUiState())
    val state: StateFlow<GroupSearchUiState> = _state

    init {
        // 검색어 인자(예: 홈에서 책 탭)가 있으면 검색 모드로 시작, 없으면 기본 목록
        val keyword = savedStateHandle.get<String>(GroupDestinations.ARG_KEYWORD).orEmpty().trim()
        if (keyword.isNotEmpty()) {
            _state.update { it.copy(query = keyword, searchKeyword = keyword) }
        }
        load()
        // 상세 등에서 복귀하며 재조회 신호가 오면 현재 모드(검색/필터) 그대로 첫 페이지 리로드
        viewModelScope.launch {
            savedStateHandle.getStateFlow(GroupDestinations.RESULT_REFRESH, false).collect { refresh ->
                if (refresh) {
                    savedStateHandle[GroupDestinations.RESULT_REFRESH] = false
                    load()
                }
            }
        }
    }

    // 첫 페이지 로드. 검색어 있으면 searchGroups, 없으면 필터 기반 getGroupList
    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val s = _state.value
            try {
                if (s.searchKeyword.isBlank()) {
                    val res = RetrofitClient.grpApi().getGroupList(
                        tradeTypes = s.tradeTypes,
                        regions = s.regions,
                        categories = s.categories,
                        sort = s.sort,
                        page = 0,
                        size = PAGE_SIZE,
                    )
                    val r = res.body()?.result
                    if (res.isSuccessful && res.body()?.isSuccess == true) {
                        _state.update {
                            it.copy(
                                items = r?.groupList.orEmpty(),
                                currentPage = r?.currentPage ?: 0,
                                hasNext = r?.hasNext ?: false,
                                totalCount = r?.totalCount,
                                loading = false,
                            )
                        }
                    } else {
                        _state.update { it.copy(error = "목록을 불러오지 못했어요", loading = false) }
                    }
                } else {
                    val res = RetrofitClient.grpApi().searchGroups(
                        keyword = s.searchKeyword,
                        sort = s.sort,
                        page = 0,
                        size = PAGE_SIZE,
                    )
                    val r = res.body()?.result
                    if (res.isSuccessful && res.body()?.isSuccess == true) {
                        _state.update {
                            it.copy(
                                items = r?.groupList.orEmpty(),
                                currentPage = r?.currentPage ?: 0,
                                hasNext = r?.hasNext ?: false,
                                totalCount = r?.totalCount,
                                loading = false,
                            )
                        }
                    } else {
                        _state.update { it.copy(error = "검색에 실패했어요", loading = false) }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "네트워크 오류가 발생했어요", loading = false) }
            }
        }
    }

    // 다음 페이지 이어붙이기 (무한 스크롤). 현재 모드(검색/필터)에 맞는 API 사용
    fun loadMore() {
        val s = _state.value
        if (!s.hasNext || s.loading || s.loadingMore) return
        val nextPage = s.currentPage + 1
        viewModelScope.launch {
            _state.update { it.copy(loadingMore = true) }
            try {
                // (추가목록, currentPage, hasNext). 실패 시 null
                val page: Triple<List<GroupItem>, Int, Boolean>? =
                    if (s.searchKeyword.isBlank()) {
                        val res = RetrofitClient.grpApi().getGroupList(
                            tradeTypes = s.tradeTypes,
                            regions = s.regions,
                            categories = s.categories,
                            sort = s.sort,
                            page = nextPage,
                            size = PAGE_SIZE,
                        )
                        val r = res.body()?.result
                        if (res.isSuccessful && res.body()?.isSuccess == true && r != null) {
                            Triple(r.groupList.orEmpty(), r.currentPage, r.hasNext)
                        } else {
                            null
                        }
                    } else {
                        val res = RetrofitClient.grpApi().searchGroups(
                            keyword = s.searchKeyword,
                            sort = s.sort,
                            page = nextPage,
                            size = PAGE_SIZE,
                        )
                        val r = res.body()?.result
                        if (res.isSuccessful && res.body()?.isSuccess == true && r != null) {
                            Triple(r.groupList, r.currentPage, r.hasNext)
                        } else {
                            null
                        }
                    }
                if (page != null) {
                    val (more, current, hasNext) = page
                    _state.update {
                        it.copy(
                            items = it.items + more,
                            currentPage = current,
                            hasNext = hasNext,
                            loadingMore = false,
                        )
                    }
                } else {
                    // 추가 로드 실패는 조용히 멈춤(기존 목록 유지)
                    _state.update { it.copy(loadingMore = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(loadingMore = false) }
            }
        }
    }

    // 검색바 입력 변경 (제출 전이라 API 호출 안 함)
    fun onQueryChange(value: String) =
        _state.update { it.copy(query = value) }

    // 검색 제출. 빈 검색어면 검색 해제 → 필터(전체) 목록, 아니면 필터 초기화 후 검색
    // totalCount는 비우지 않음 — 응답 도착 시 갱신(헤더 깜빡임 방지)
    fun onSearch() {
        val keyword = _state.value.query.trim()
        if (keyword.isBlank()) {
            _state.update { it.copy(searchKeyword = "") }
        } else {
            _state.update {
                it.copy(
                    searchKeyword = keyword,
                    tradeTypes = emptyList(),
                    regions = emptyList(),
                    categories = emptyList(),
                )
            }
        }
        load()
    }

    // 필터 변경 → 검색 해제(검색어/입력값 비움) 후 첫 페이지부터 재조회
    fun applyTradeTypes(tradeTypes: List<String>) {
        _state.update { it.copy(tradeTypes = tradeTypes, query = "", searchKeyword = "") }
        load()
    }

    fun applyRegions(regions: List<String>) {
        _state.update { it.copy(regions = regions, query = "", searchKeyword = "") }
        load()
    }

    fun applyCategories(categories: List<String>) {
        _state.update { it.copy(categories = categories, query = "", searchKeyword = "") }
        load()
    }

    fun retry() = load()

    companion object {
        private const val PAGE_SIZE = 10
    }
}
