package com.bookiibookii.bookiibookii.group.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.group.model.GroupSearchUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupSearchViewModel : ViewModel() {

    private val _state = MutableStateFlow(GroupSearchUiState())
    val state: StateFlow<GroupSearchUiState> = _state

    init {
        // 화면 진입 시 기본값(전체)으로 목록 로드
        loadGroups()
    }

    // 현재 필터 상태로 그룹 목록 조회 (첫 페이지부터). 에러 후 재시도에도 사용
    fun loadGroups() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val s = _state.value
            try {
                val res = RetrofitClient.grpApi().getGroupList(
                    tradeTypes = s.tradeTypes,
                    regions = s.regions,
                    categories = s.categories,
                    sort = s.sort,
                    page = 0,
                    size = PAGE_SIZE,
                )
                val result = res.body()?.result
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    _state.update {
                        it.copy(
                            items = result?.groupList.orEmpty(),
                            currentPage = result?.currentPage ?: 0,
                            hasNext = result?.hasNext ?: false,
                            loading = false,
                        )
                    }
                } else {
                    _state.update { it.copy(error = "목록을 불러오지 못했어요", loading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "네트워크 오류가 발생했어요", loading = false) }
            }
        }
    }

    // 다음 페이지를 이어붙인다 (무한 스크롤). 중복/불가 상황이면 무시
    fun loadMore() {
        val s = _state.value
        if (!s.hasNext || s.loading || s.loadingMore) return
        val nextPage = s.currentPage + 1
        viewModelScope.launch {
            _state.update { it.copy(loadingMore = true) }
            try {
                val res = RetrofitClient.grpApi().getGroupList(
                    tradeTypes = s.tradeTypes,
                    regions = s.regions,
                    categories = s.categories,
                    sort = s.sort,
                    page = nextPage,
                    size = PAGE_SIZE,
                )
                val result = res.body()?.result
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    _state.update {
                        it.copy(
                            items = it.items + result?.groupList.orEmpty(),
                            currentPage = result?.currentPage ?: nextPage,
                            hasNext = result?.hasNext ?: false,
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

    // 필터 변경 → 상태 갱신 후 재조회
    fun applyTradeTypes(tradeTypes: List<String>) {
        _state.update { it.copy(tradeTypes = tradeTypes) }
        loadGroups()
    }

    fun applyRegions(regions: List<String>) {
        _state.update { it.copy(regions = regions) }
        loadGroups()
    }

    fun applyCategories(categories: List<String>) {
        _state.update { it.copy(categories = categories) }
        loadGroups()
    }

    companion object {
        private const val PAGE_SIZE = 10
    }
}
