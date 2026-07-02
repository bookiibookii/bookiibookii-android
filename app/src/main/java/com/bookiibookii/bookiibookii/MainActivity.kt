package com.bookiibookii.bookiibookii

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.common.BaseActivity
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.ActivityMainBinding
import com.bookiibookii.bookiibookii.group.GroupFragment
import com.bookiibookii.bookiibookii.group.nav.GroupDestinations
import com.bookiibookii.bookiibookii.home.HomeFragment
import com.bookiibookii.bookiibookii.library.feat.LibraryFragment
import com.bookiibookii.bookiibookii.library.ui.ReadingCard
import com.bookiibookii.bookiibookii.library.vm.toReadingCard
import com.bookiibookii.bookiibookii.notification.fcm.FcmTokenRegistrar
import com.bookiibookii.bookiibookii.notification.nav.NotificationRedirect
import com.bookiibookii.bookiibookii.notification.nav.NotificationRedirectRouter
import com.bookiibookii.bookiibookii.onboarding.login.LoginActivity
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import com.bookiibookii.bookiibookii.tracker.TrackerFragment
import com.bookiibookii.bookiibookii.tracker.nav.TrackerDestinations
import com.google.gson.Gson
import kotlinx.coroutines.launch

private enum class NavTab { HOME, TRACKER, LIBRARY }


class MainActivity : BaseActivity<ActivityMainBinding>() {

    // 안드13+ 알림 권한 요청 런처. 거부해도 앱 동작엔 지장 없음(푸시 알림만 안 옴).
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("FCM", "POST_NOTIFICATIONS granted=$granted")
        }

    override fun getViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (!TokenManager.hasAccessToken(this)) {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        if (!TokenManager.isOnboardingDone(this)) {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        if (savedInstanceState == null) {
            setBottomNavSelected(NavTab.HOME)
            replaceFragment(HomeFragment())
        }

        initBottomNav()
        observeFragmentChanges()
        handleNavigationIntent(intent)
        requestNotificationPermissionIfNeeded()

        // FCM 토큰 서버 등록
        FcmTokenRegistrar.registerCurrentToken(this)
    }

    // 안드13(TIRAMISU)+ 에서만 런타임 요청 필요
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) return
        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    // 탑레벨 Fragment(홈·트래커·서재 메인)일 때만 BottomNav 표시
    private fun observeFragmentChanges() {
        supportFragmentManager.addOnBackStackChangedListener {
            refreshBottomNavVisibility()
            updateBottomNavSelection(supportFragmentManager.findFragmentById(R.id.fragmentContainer))
        }
    }

    // fragmentContainer의 현재 프래그먼트가 top-level(홈·트래커·서재)일 때만 BottomNav 표시.
    // 백스택 복귀 시 콜백 순서(onDetach vs onBackStackChanged)와 무관하게 항상 올바른 값으로
    // 맞추기 위해 Base*Fragment.onDetach 에서도 이 메서드를 호출한다.
    fun refreshBottomNavVisibility() {
        val current = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        val isTopLevel = current is HomeFragment
                || current is TrackerFragment
                || (current is LibraryFragment && current.isAtMainRoute())
        binding.bottomNav.root.visibility = if (isTopLevel) View.VISIBLE else View.GONE
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNavigationIntent(intent)
    }

    // 알림(FCM 포그라운드/백그라운드, 인앱) 클릭 → 화면 이동.
    // FCM data 또는 OS 가 주입한 extra를 라우터가 한 번에 파싱한다.
    private fun handleNavigationIntent(intent: Intent?) {
        val redirect = NotificationRedirectRouter.fromIntent(intent) ?: return
        dispatchNotificationRedirect(redirect)
        // 회전·onNewIntent 재설정 시 중복 처리 방지
        intent?.removeExtra(NotificationRedirectRouter.KEY_REDIRECT_TYPE)
    }

    // redirectType(백엔드 RedirectType) → 목적지. groupId 없으면 해당 분기는 무시.
    fun dispatchNotificationRedirect(redirect: NotificationRedirect) {
        val groupId = redirect.groupId
        when (redirect.redirectType) {
            "EXPLORE_HOME" -> selectTab(NavTab.HOME, HomeFragment())
            "TRACKER_HOME" -> selectTab(NavTab.TRACKER, TrackerFragment())
            "APPLICATION_MANAGEMENT" -> groupId?.let {
                pushDeepFragment(GroupFragment.newInstance(GroupDestinations.joinRequests(it.toString())))
            }
            "GROUP_DETAIL" -> groupId?.let {
                pushDeepFragment(GroupFragment.newInstance(GroupDestinations.detail(it)))
            }
            "TRACKER_DETAIL" -> groupId?.let {
                pushDeepFragment(TrackerFragment.newInstance(TrackerDestinations.detail(it)))
            }
            "TRACKER_COMMENT" -> groupId?.let {
                pushDeepFragment(TrackerFragment.newInstance(TrackerDestinations.comment(it, redirect.title.orEmpty())))
            }
            "BOOK_CARD_DETAIL" -> {
                val cardId = redirect.cardId
                if (groupId != null && cardId != null) openCardDetail(groupId, cardId)
                else showCustomToast("카드를 불러오지 못했어요", false)
            }
            else -> Log.d("FCM", "라우팅 보류 redirectType=${redirect.redirectType}")
        }
    }

    // 알림 → 독서카드 상세. groupId로 카드 목록을 받아 cardId와 매칭,
    // 같은 책 카드만 추려(일반 진입과 동일 범위) 라이브러리 상세화면을 그대로 재사용해 연다.
    private fun openCardDetail(groupId: Long, cardId: Long) {
        lifecycleScope.launch {
            val allCards = try {
                val resp = RetrofitClient.libApi().getGroupCards(groupId.toInt())
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    resp.body()?.result?.cards?.map { it.toReadingCard() }
                } else null
            } catch (_: Exception) {
                null
            }

            val target = allCards?.firstOrNull { it.cardId == cardId }
            val sorted = allCards
                ?.filter { it.bookTitle == target?.bookTitle }
                ?.sortedWith(compareByDescending<ReadingCard> { it.date }.thenByDescending { it.cardId })
            val index = sorted?.indexOfFirst { it.cardId == cardId } ?: -1

            if (target == null || sorted == null || index < 0) {
                showCustomToast("카드를 불러오지 못했어요", false)
                return@launch
            }

            pushDeepFragment(
                LibraryFragment.newInstanceAtCardDetail(
                    initialIndex = index,
                    sortByLatest = true,
                    cardsJson = Gson().toJson(sorted),
                ),
            )
        }
    }

    // 상세류 화면 진입 — 바텀네비 숨기고 백스택에 쌓아 뒤로가기 시 이전 화면 복귀
    private fun pushDeepFragment(fragment: Fragment) {
        binding.bottomNav.root.visibility = View.GONE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun initBottomNav() {
        binding.bottomNav.itemGroup.setOnClickListener {
            selectTab(NavTab.HOME, HomeFragment())
        }
        binding.bottomNav.itemTracker.setOnClickListener {
            selectTab(NavTab.TRACKER, TrackerFragment())
        }
        binding.bottomNav.itemLibrary.setOnClickListener {
            selectTab(NavTab.LIBRARY, LibraryFragment())
        }
    }

    private fun selectTab(tab: NavTab, fragment: Fragment) {
        // 탑레벨 탭으로 전환 — 트래커 상세 등에서 GONE된 바텀네비를 다시 표시
        binding.bottomNav.root.visibility = View.VISIBLE
        setBottomNavSelected(tab)
        replaceFragment(fragment)
    }

    fun moveToGroupTab() {
        selectTab(NavTab.HOME, HomeFragment())
    }

    // 홈 탭의 "내 그룹"(매칭 현황) 탭으로 바로 이동 (서재 메인의 빈 상태 CTA 등에서 사용)
    fun moveToHomeMyGroupsTab() {
        selectTab(NavTab.HOME, HomeFragment.newInstanceAtMyGroups())
    }

    // 서재 탭으로 이동 (바텀네비 '서재'를 누른 것과 동일)
    // 서재 탭으로 이동
    fun moveToLibraryTab() {
        selectTab(NavTab.LIBRARY, LibraryFragment())
    }

    private fun updateBottomNavSelection(current: Fragment?) {
        when (current) {
            is HomeFragment -> setBottomNavSelected(NavTab.HOME)
            is TrackerFragment -> setBottomNavSelected(NavTab.TRACKER)
            is LibraryFragment -> setBottomNavSelected(NavTab.LIBRARY)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun setBottomNavSelected(tab: NavTab) {
        fun setItem(item: LinearLayout, tv: TextView, selected: Boolean) {
            item.background = if (selected)
                ContextCompat.getDrawable(this, R.drawable.bg_grey100_circle)
            else null
            tv.visibility = if (selected) View.GONE else View.VISIBLE
        }

        setItem(binding.bottomNav.itemGroup, binding.bottomNav.tvGroup, tab == NavTab.HOME)
        setItem(binding.bottomNav.itemTracker, binding.bottomNav.tvTracker, tab == NavTab.TRACKER)
        setItem(binding.bottomNav.itemLibrary, binding.bottomNav.tvLibrary, tab == NavTab.LIBRARY)
    }

    override fun setupWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.fragmentContainer.updatePadding(
                top = systemBars.top
            )

            val params = binding.bottomNav.root.layoutParams as ConstraintLayout.LayoutParams
            val bottomNavMargin = (20 * resources.displayMetrics.density).toInt()
            params.bottomMargin = systemBars.bottom + bottomNavMargin
            binding.bottomNav.root.layoutParams = params

            insets
        }
    }

}
