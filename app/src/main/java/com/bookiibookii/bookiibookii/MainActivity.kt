package com.bookiibookii.bookiibookii

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.common.ComRetryBus
import com.bookiibookii.bookiibookii.ui.component.showCustomToast
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
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
import com.bookiibookii.bookiibookii.ui.component.BottomNavBar
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.google.gson.Gson
import kotlinx.coroutines.launch

enum class NavTab { HOME, TRACKER, LIBRARY }

class MainActivity : AppCompatActivity() {

    private var currentTab by mutableStateOf(NavTab.HOME)

    private lateinit var fragmentContainerView: FragmentContainerView
    private lateinit var bottomNavView: ComposeView

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
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

        setupLayout()
        hideNavigationBar()

        if (savedInstanceState == null) {
            currentTab = NavTab.HOME
            replaceFragment(HomeFragment())
        }

        lifecycleScope.launch {
            ComRetryBus.retryFlow.collect {
                if (supportFragmentManager.backStackEntryCount > 0) return@collect
                val fresh = when (currentTab) {
                    NavTab.HOME -> HomeFragment()
                    NavTab.TRACKER -> TrackerFragment()
                    NavTab.LIBRARY -> LibraryFragment()
                }
                selectTab(currentTab, fresh)
            }
        }

        observeFragmentChanges()
        handleNavigationIntent(intent)
        requestNotificationPermissionIfNeeded()

        val prefs = getSharedPreferences("bookii_prefs", android.content.Context.MODE_PRIVATE)
        if (prefs.getBoolean("push_notification_enabled", true)) {
            FcmTokenRegistrar.registerCurrentToken(this)
        }
    }

    private fun setupLayout() {
        fragmentContainerView = FragmentContainerView(this).apply {
            id = R.id.fragmentContainer
        }

        bottomNavView = ComposeView(this).apply {
            id = R.id.bottomNav
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BookiiBookiiTheme {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        BottomNavBar(
                            groupSelected = currentTab == NavTab.HOME,
                            trackerSelected = currentTab == NavTab.TRACKER,
                            librarySelected = currentTab == NavTab.LIBRARY,
                            onGroupClick = { selectTab(NavTab.HOME, HomeFragment()) },
                            onTrackerClick = { selectTab(NavTab.TRACKER, TrackerFragment()) },
                            onLibraryClick = { selectTab(NavTab.LIBRARY, LibraryFragment()) },
                            modifier = Modifier
                                .navigationBarsPadding()
                                .padding(bottom = 20.dp),
                        )
                    }
                }
            }
        }

        val root = FrameLayout(this).apply {
            addView(
                fragmentContainerView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                )
            )
            addView(
                bottomNavView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                )
            )
        }

        setContentView(root)

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            fragmentContainerView.updatePadding(top = systemBars.top)
            insets
        }
    }

    private fun hideNavigationBar() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideNavigationBar()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) return
        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun observeFragmentChanges() {
        supportFragmentManager.addOnBackStackChangedListener {
            refreshBottomNavVisibility()
            updateBottomNavSelection(supportFragmentManager.findFragmentById(R.id.fragmentContainer))
        }
    }

    fun refreshBottomNavVisibility() {
        val current = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        val isTopLevel = current is HomeFragment
            || current is TrackerFragment
            || (current is LibraryFragment && current.isAtMainRoute())
        bottomNavView.visibility = if (isTopLevel) View.VISIBLE else View.GONE
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent?) {
        val redirect = NotificationRedirectRouter.fromIntent(intent) ?: return
        dispatchNotificationRedirect(redirect)
        intent?.removeExtra(NotificationRedirectRouter.KEY_REDIRECT_TYPE)
    }

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
            else -> { /* NOTICE_DETAIL 등 미구현 라우트 */ }
        }
    }

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

    private fun pushDeepFragment(fragment: Fragment) {
        bottomNavView.visibility = View.GONE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun selectTab(tab: NavTab, fragment: Fragment) {
        currentTab = tab
        bottomNavView.visibility = View.VISIBLE
        replaceFragment(fragment)
    }

    fun moveToGroupTab() {
        selectTab(NavTab.HOME, HomeFragment())
    }

    fun moveToHomeMyGroupsTab() {
        selectTab(NavTab.HOME, HomeFragment.newInstanceAtMyGroups())
    }

    fun moveToLibraryTab() {
        selectTab(NavTab.LIBRARY, LibraryFragment())
    }

    private fun updateBottomNavSelection(current: Fragment?) {
        when (current) {
            is HomeFragment -> currentTab = NavTab.HOME
            is TrackerFragment -> currentTab = NavTab.TRACKER
            is LibraryFragment -> currentTab = NavTab.LIBRARY
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
