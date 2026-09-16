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

    private var hasLoadedOnce = false

    /**
     * 화면이 보이게 됐음을 알린다. 최초 진입인지 복귀인지는 데이터를 가진 이쪽에서 판단한다.
     *
     * 화면은 자기가 처음인지 알 수 없다. 탭 전환·백스택 복귀·앱 복귀가 모두 같은
     * ON_RESUME으로 오기 때문이다. 판단을 화면에 두면 화면 상태와 이 ViewModel의
     * 실제 보유 상태가 어긋날 수 있다(프로세스 사망 복원 시 ViewModel만 다시 만들어진다).
     */
    fun onScreenResumed() {
        // 닉네임은 마이페이지가 갱신해 둔 캐시를 매번 읽는다.
        val hasCachedNickname = syncNicknameFromCache()
        if (hasLoadedOnce) {
            refreshCurrentTab()
            fetchNotificationDot()
            return
        }
        hasLoadedOnce = true
        // 캐시가 비어 있을 때만(로그인 직후 등) 직접 조회한다.
        if (!hasCachedNickname) fetchNickname()
        fetchRecommendedGroups()
        fetchNotificationDot()
    }

    /**
     * 캐시에 저장된 닉네임을 화면 상태에 반영한다.
     *
     * 탭 전환으로는 이 ViewModel이 죽지 않으므로, 마이페이지에서 닉네임을 바꾸고
     * 돌아왔을 때 갱신된 캐시를 다시 읽어야 인사말이 따라온다.
     *
     * @return 캐시에 닉네임이 있었으면 true
     */
    private fun syncNicknameFromCache(): Boolean {
        val cached = TokenManager.getNickname(getApplication()) ?: return false
        _uiState.update { it.copy(nickname = cached) }
        return true
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
