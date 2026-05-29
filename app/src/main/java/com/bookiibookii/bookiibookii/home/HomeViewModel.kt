package com.bookiibookii.bookiibookii.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.group.GroupItem
import com.bookiibookii.bookiibookii.data.model.group.HomeCategorySection
import com.bookiibookii.bookiibookii.data.model.group.HomeRegionSection
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HomeTab { RECOMMEND, MY_GROUPS, APPLIED }

data class HomeUiState(
    val nickname: String = "",
    val selectedTab: HomeTab = HomeTab.RECOMMEND,
    val newGroups: List<GroupItem> = emptyList(),
    val categorySection: HomeCategorySection? = null,
    val regionSection: HomeRegionSection? = null,
    val myGroups: List<GroupItem> = emptyList(),
    val appliedGroups: List<GroupItem> = emptyList(),
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
                _uiState.update {
                    it.copy(
                        newGroups = result.newGroups,
                        categorySection = result.categorySection,
                        regionSection = result.regionSection,
                    )
                }
            }
        }
    }

    private fun fetchMyGroups() {
        viewModelScope.launch {
            runCatching {
                RetrofitClient.grpApi().getGroupList(
                    tradeTypes = null,
                    regions = null,
                    categories = null,
                    sort = "LATEST",
                    page = 0,
                    size = 20,
                )
            }.onSuccess { response ->
                val groups = response.body()?.result?.groupList ?: return@onSuccess
                _uiState.update { it.copy(myGroups = groups) }
            }
        }
    }

    private fun fetchAppliedGroups() {
        viewModelScope.launch {
            runCatching {
                RetrofitClient.grpApi().getAppliedGroups()
            }.onSuccess { response ->
                val groups = response.body()?.result ?: return@onSuccess
                _uiState.update { it.copy(appliedGroups = groups) }
            }
        }
    }
}
