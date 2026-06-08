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

    fun selectTab(tab: HomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        when (tab) {
            HomeTab.MY_GROUPS -> fetchMyGroups()
            HomeTab.APPLIED -> fetchAppliedGroups()
            HomeTab.RECOMMEND -> Unit
        }
    }

    private fun fetchNickname() {
        viewModelScope.launch {
            runCatching {
                RetrofitClient.mypApi().getMypage()
            }.onSuccess { response ->
                val nickname = response.body()?.result?.nickname ?: return@onSuccess
                _uiState.update { it.copy(nickname = nickname) }
                TokenManager.saveNickname(getApplication(), nickname)
            }
        }
    }

    private fun fetchRecommendedGroups() {
        viewModelScope.launch {
            runCatching {
                RetrofitClient.grpApi().getHomeGroups()
            }.onSuccess { response ->
                val result = response.body()?.result ?: return@onSuccess
                _uiState.update { it.copy(recommendSections = result.sections) }
            }
        }
    }

    private fun fetchNotificationDot() {
        viewModelScope.launch {
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

    private fun fetchMyGroups() {
        viewModelScope.launch {
            runCatching {
                RetrofitClient.grpApi().getMyHostedGroups()
            }.onSuccess { response ->
                val groups = response.body()?.result
                    ?.filter { it.displayStatus == "BEFORE_MATCHING" } ?: return@onSuccess
                _uiState.update { it.copy(myGroups = groups) }
            }
        }
    }

    private fun fetchAppliedGroups() {
        viewModelScope.launch {
            runCatching {
                RetrofitClient.grpApi().getAppliedGroups()
            }.onSuccess { response ->
                val groups = response.body()?.result?.applicationList
                    ?.map { it.toGroupItem() }
                    ?.filter { it.groupStatus == "RECRUITING" } ?: return@onSuccess
                _uiState.update { it.copy(appliedGroups = groups) }
            }.onFailure { e ->
                Log.e("HomeVM", "fetchAppliedGroups error", e)
            }
        }
    }
}
