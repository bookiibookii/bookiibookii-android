package com.bookiibookii.bookiibookii.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.group.GroupItem
import com.bookiibookii.bookiibookii.data.model.group.HomeSection
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HomeTab { RECOMMEND, MY_GROUPS, APPLIED }

data class HomeUiState(
    val nickname: String = "",
    val selectedTab: HomeTab = HomeTab.RECOMMEND,
    val recommendSections: List<HomeSection> = emptyList(),
    val myGroups: List<GroupItem> = emptyList(),
    val appliedGroups: List<GroupItem> = emptyList(),
    val hasNewNotification: Boolean = false,
    val isLoading: Boolean = false,
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        TokenManager.getNickname(app)?.let { cached ->
            _uiState.update { it.copy(nickname = cached) }
        }
        fetchNickname()
        fetchRecommendedGroups()
        fetchNotificationDot()
    }

    // 화면 복귀(onResume) 시 현재 선택된 탭만 다시 불러옴 — 수락 후 상태 변경 반영
    fun refreshCurrentTab() {
        when (_uiState.value.selectedTab) {
            HomeTab.MY_GROUPS -> fetchMyGroups()
            HomeTab.APPLIED -> fetchAppliedGroups()
            HomeTab.RECOMMEND -> fetchRecommendedGroups()
        }
    }

    // 풀-투-리프레시: 전체 데이터 재로딩 후 isLoading 해제
    fun refresh() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                coroutineScope {
                    launch { loadNickname() }
                    launch { loadRecommendedGroups() }
                    launch { loadNotificationDot() }
                    when (_uiState.value.selectedTab) {
                        HomeTab.MY_GROUPS -> launch { loadMyGroups() }
                        HomeTab.APPLIED -> launch { loadAppliedGroups() }
                        HomeTab.RECOMMEND -> Unit
                    }
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectTab(tab: HomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        when (tab) {
            HomeTab.MY_GROUPS -> fetchMyGroups()
            HomeTab.APPLIED -> fetchAppliedGroups()
            HomeTab.RECOMMEND -> Unit
        }
    }

    private fun fetchNickname() = viewModelScope.launch { loadNickname() }

    private suspend fun loadNickname() {
        runCatching {
            RetrofitClient.mypApi().getMypage()
        }.onSuccess { response ->
            val nickname = response.body()?.result?.nickname ?: return@onSuccess
            _uiState.update { it.copy(nickname = nickname) }
            TokenManager.saveNickname(getApplication(), nickname)
        }
    }

    private fun fetchRecommendedGroups() = viewModelScope.launch { loadRecommendedGroups() }

    private suspend fun loadRecommendedGroups() {
        runCatching {
            RetrofitClient.grpApi().getHomeGroups()
        }.onSuccess { response ->
            val result = response.body()?.result ?: return@onSuccess
            _uiState.update { it.copy(recommendSections = result.sections) }
        }
    }

    fun fetchNotificationDot() = viewModelScope.launch { loadNotificationDot() }

    private suspend fun loadNotificationDot() {
        coroutineScope {
            val systemDeferred = async {
                runCatching {
                    RetrofitClient.notiApi().getNotifications("SYSTEM", null, 20)
                }.getOrNull()?.body()?.result?.items.orEmpty()
            }
            val keywordDeferred = async {
                runCatching {
                    RetrofitClient.notiApi().getNotifications("KEYWORD", null, 20)
                }.getOrNull()?.body()?.result?.items.orEmpty()
            }
            val hasUnread = (systemDeferred.await() + keywordDeferred.await()).any { !it.isRead }
            _uiState.update { it.copy(hasNewNotification = hasUnread) }
        }
    }

    private fun fetchMyGroups() = viewModelScope.launch { loadMyGroups() }

    private suspend fun loadMyGroups() {
        runCatching {
            RetrofitClient.grpApi().getMyHostedGroups()
        }.onSuccess { response ->
            val groups = response.body()?.result
                ?.filter { it.displayStatus == "BEFORE_MATCHING" } ?: return@onSuccess
            _uiState.update { it.copy(myGroups = groups) }
        }
    }

    private fun fetchAppliedGroups() = viewModelScope.launch { loadAppliedGroups() }

    private suspend fun loadAppliedGroups() {
        runCatching {
            RetrofitClient.grpApi().getAppliedGroups()
        }.onSuccess { response ->
            val groups = response.body()?.result?.applicationList
                ?.filter { it.applicationStatus == "PENDING" }
                ?.map { it.toGroupItem() } ?: return@onSuccess
            _uiState.update { it.copy(appliedGroups = groups) }
        }.onFailure { e ->
            Log.e("HomeVM", "fetchAppliedGroups error", e)
        }
    }
}
