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
import com.bookiibookii.bookiibookii.common.BaseActivity
import com.bookiibookii.bookiibookii.databinding.ActivityMainBinding
import com.bookiibookii.bookiibookii.group.GroupFragment
import com.bookiibookii.bookiibookii.home.HomeFragment
import com.bookiibookii.bookiibookii.library.feat.LibraryFragment
import com.bookiibookii.bookiibookii.notification.fcm.FcmTokenRegistrar
import com.bookiibookii.bookiibookii.onboarding.login.LoginActivity
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import com.bookiibookii.bookiibookii.tracker.TrackerFragment

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

        Log.d("DEV_TOKEN", "AccessToken: ${TokenManager.getAccessToken(this)}")

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

    private fun handleNavigationIntent(intent: Intent?) {
        when (intent?.getStringExtra("NAV_ACTION")) {
            "OPEN_GROUP" -> {
                moveToGroupTab()
                intent.removeExtra("NAV_ACTION")
            }
        }
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
