package com.bookiibookii.bookiibookii

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.library.nav.LibraryDestinations
import com.bookiibookii.bookiibookii.library.ui.ReadingCard
import com.bookiibookii.bookiibookii.library.vm.toReadingCard
import com.bookiibookii.bookiibookii.notification.fcm.FcmTokenRegistrar
import com.bookiibookii.bookiibookii.notification.nav.NotificationRedirect
import com.bookiibookii.bookiibookii.notification.nav.NotificationRedirectRouter
import com.bookiibookii.bookiibookii.onboarding.login.LoginActivity
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import com.bookiibookii.bookiibookii.ui.component.showCustomToast
import com.bookiibookii.bookiibookii.ui.nav.BookiiApp
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.google.gson.Gson
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var pendingRedirect by mutableStateOf<NotificationRedirect?>(null)

    // 카드 상세는 이동 전 서버 조회가 필요해 여기서 처리하고 결과 라우트를 넘긴다.
    private var pendingCardDetailRoute by mutableStateOf<String?>(null)

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

        consumeRedirectIntent(intent)

        setContent {
            BookiiBookiiTheme {
                BookiiApp(
                    pendingRedirect = pendingRedirect,
                    onRedirectConsumed = { pendingRedirect = null },
                    onCardDetailRequest = ::openCardDetail,
                    onUnsupportedRedirect = { showCustomToast("카드를 불러오지 못했어요", false) },
                    pendingCardDetailRoute = pendingCardDetailRoute,
                    onCardDetailRouteConsumed = { pendingCardDetailRoute = null },
                )
            }
        }

        hideNavigationBar()
        requestNotificationPermissionIfNeeded()

        val prefs = getSharedPreferences("bookii_prefs", android.content.Context.MODE_PRIVATE)
        if (prefs.getBoolean("push_notification_enabled", true)) {
            FcmTokenRegistrar.registerCurrentToken(this)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeRedirectIntent(intent)
    }

    private fun consumeRedirectIntent(intent: Intent?) {
        pendingRedirect = NotificationRedirectRouter.fromIntent(intent)
        intent?.removeExtra(NotificationRedirectRouter.KEY_REDIRECT_TYPE)
    }

    // 서버가 memberBookId 기준으로 같은 그룹·같은 책 카드만 반환
    private fun openCardDetail(memberBookId: Long, cardId: Long) {
        lifecycleScope.launch {
            val bookCards = try {
                val resp = RetrofitClient.libApi().getMemberBookCards(memberBookId.toInt())
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    resp.body()?.result?.cards?.map { it.toReadingCard() }
                } else null
            } catch (_: Exception) {
                null
            }

            val sorted = bookCards
                ?.sortedWith(compareByDescending<ReadingCard> { it.date }.thenByDescending { it.cardId })
            val index = sorted?.indexOfFirst { it.cardId == cardId } ?: -1

            if (sorted == null || index < 0) {
                showCustomToast("카드를 불러오지 못했어요", false)
                return@launch
            }

            pendingCardDetailRoute = LibraryDestinations.cardDetail(
                initialIndex = index,
                sortByLatest = true,
                cardsJson = Gson().toJson(sorted),
            )
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
