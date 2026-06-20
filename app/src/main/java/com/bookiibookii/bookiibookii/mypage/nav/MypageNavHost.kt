package com.bookiibookii.bookiibookii.mypage.nav

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.data.model.mypage.UserProfileResDTO
import com.bookiibookii.bookiibookii.mypage.ui.detail.MyBookshelfRoute
import com.bookiibookii.bookiibookii.mypage.ui.detail.ReviewRoute
import com.bookiibookii.bookiibookii.mypage.ui.detail.ReviewTab
import com.bookiibookii.bookiibookii.mypage.ui.main.AddressManagementRoute
import com.bookiibookii.bookiibookii.mypage.ui.main.MypageMainRoute
import com.bookiibookii.bookiibookii.mypage.ui.main.ProfileSettingRoute
import com.bookiibookii.bookiibookii.mypage.ui.main.ProfileShareCardContent
import com.bookiibookii.bookiibookii.mypage.ui.setting.FaqRoute
import com.bookiibookii.bookiibookii.mypage.ui.setting.NoticeDetailRoute
import com.bookiibookii.bookiibookii.mypage.ui.setting.NoticeRoute
import com.bookiibookii.bookiibookii.mypage.ui.setting.SettingRoute
import com.bookiibookii.bookiibookii.mypage.ui.setting.WebViewRoute
import com.bookiibookii.bookiibookii.mypage.ui.setting.WithdrawRoute
import com.bookiibookii.bookiibookii.mypage.vm.MypageViewModel
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import java.io.File

// 트래커/서재 모듈과 동일한 패턴: 단일 Fragment(MypageFragment) 안에서 11개 화면 전환을
// 전부 Compose Navigation으로 처리한다.
//
// MypageViewModel은 메인/프로필수정/후기/탈퇴 화면에서 공유되어야 하므로(구 activityViewModels())
// 호출자(MypageFragment)가 activityViewModels()로 생성한 단일 인스턴스를 파라미터로 받는다.
@Composable
fun MypageNavHost(
    mypageViewModel: MypageViewModel,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    startDestination: String = MypageDestinations.MAIN,
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // 내부 스택에 더 갈 곳이 있으면 popBackStack, 없으면(딥링크 시작점, 예: 그룹 모듈에서
    // 주소지 관리로 바로 진입) 마이페이지 Fragment 자체를 종료한다. 그렇지 않으면 뒤로가기가
    // 조용히 무시된다.
    val popOrExit: () -> Unit = {
        if (!navController.popBackStack()) onBackClick()
    }

    // 마이페이지 전체는 탑레벨이 아니므로 진입 시 바텀네비를 숨기고, 빠져나갈 때(Fragment 전체가
    // 컴포지션을 떠날 때) 원래 상태로 복원한다. (구 BaseMypageFragment.onResume/onDetach를 대체)
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
        // Activity의 탭 전환 동기 코드가 nav를 VISIBLE로 override할 수 있으므로
        // 현재 메시지 큐가 처리된 다음 프레임에도 한 번 더 적용
        activity?.window?.decorView?.post {
            activity.findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
        }
        onDispose {
            (activity as? MainActivity)?.refreshBottomNavVisibility()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        // 화면 전환 애니메이션 제거(기본 크로스페이드 시 이전 화면이 잔상처럼 겹쳐 보이는 현상 방지)
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(MypageDestinations.MAIN) {
            MypageMainRoute(
                viewModel = mypageViewModel,
                onBackClick = onBackClick,
                onSettingClick = { navController.navigate(MypageDestinations.SETTING) },
                onProfileSettingClick = { navController.navigate(MypageDestinations.PROFILE_SETTING) },
                onAddressManagementClick = { navController.navigate(MypageDestinations.addressManagement()) },
                onBookshelfClick = { navController.navigate(MypageDestinations.MY_BOOKSHELF) },
                onWrittenReviewClick = { navController.navigate(MypageDestinations.review(ReviewTab.WRITTEN)) },
                onReceivedReviewClick = { navController.navigate(MypageDestinations.review(ReviewTab.RECEIVED)) },
                onInstagramShareClick = {
                    mypageViewModel.profileData.value?.let { shareProfileToInstagram(context, it) }
                },
            )
        }

        composable(MypageDestinations.PROFILE_SETTING) {
            ProfileSettingRoute(
                viewModel = mypageViewModel,
                onBackClick = popOrExit,
            )
        }

        composable(
            route = MypageDestinations.ADDRESS_MANAGEMENT_ROUTE,
            arguments = listOf(
                navArgument(MypageDestinations.ADDRESS_ARG_INITIAL_TAB) { type = NavType.IntType; defaultValue = 0 },
            ),
        ) { backStackEntry ->
            AddressManagementRoute(
                initialTab = backStackEntry.arguments?.getInt(MypageDestinations.ADDRESS_ARG_INITIAL_TAB) ?: 0,
                onBackClick = popOrExit,
            )
        }

        composable(MypageDestinations.MY_BOOKSHELF) {
            MyBookshelfRoute(onBack = popOrExit)
        }

        composable(
            route = MypageDestinations.REVIEW_ROUTE,
            arguments = listOf(
                navArgument(MypageDestinations.REVIEW_ARG_TAB) { type = NavType.StringType; defaultValue = ReviewTab.WRITTEN.name },
            ),
        ) { backStackEntry ->
            val tabName = backStackEntry.arguments?.getString(MypageDestinations.REVIEW_ARG_TAB) ?: ReviewTab.WRITTEN.name
            ReviewRoute(
                viewModel = mypageViewModel,
                initialTab = ReviewTab.valueOf(tabName),
                onBackClick = popOrExit,
            )
        }

        composable(MypageDestinations.SETTING) {
            SettingRoute(
                onBackClick = popOrExit,
                onNoticeClick = { navController.navigate(MypageDestinations.NOTICE) },
                onQuestionClick = { navController.navigate(MypageDestinations.FAQ) },
                onWithdrawClick = { navController.navigate(MypageDestinations.WITHDRAW) },
            )
        }

        composable(MypageDestinations.NOTICE) {
            NoticeRoute(
                onBackClick = popOrExit,
                onNoticeClick = { noticeId, title -> navController.navigate(MypageDestinations.noticeDetail(noticeId, title)) },
            )
        }

        composable(
            route = MypageDestinations.NOTICE_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(MypageDestinations.NOTICE_DETAIL_ARG_NOTICE_ID) { type = NavType.LongType },
                navArgument(MypageDestinations.NOTICE_DETAIL_ARG_TITLE) { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            NoticeDetailRoute(
                noticeId = args?.getLong(MypageDestinations.NOTICE_DETAIL_ARG_NOTICE_ID) ?: -1L,
                title = args?.getString(MypageDestinations.NOTICE_DETAIL_ARG_TITLE).orEmpty(),
                onBackClick = popOrExit,
            )
        }

        composable(MypageDestinations.FAQ) {
            FaqRoute(onBackClick = popOrExit)
        }

        composable(MypageDestinations.WITHDRAW) {
            WithdrawRoute(
                mypageViewModel = mypageViewModel,
                onBackClick = popOrExit,
            )
        }

        composable(
            route = MypageDestinations.WEBVIEW_ROUTE,
            arguments = listOf(
                navArgument(MypageDestinations.WEBVIEW_ARG_TITLE) { type = NavType.StringType; defaultValue = "" },
                navArgument(MypageDestinations.WEBVIEW_ARG_ASSET) { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            WebViewRoute(
                title = args?.getString(MypageDestinations.WEBVIEW_ARG_TITLE).orEmpty(),
                assetFileName = args?.getString(MypageDestinations.WEBVIEW_ARG_ASSET).orEmpty(),
                onBackClick = popOrExit,
            )
        }
    }
}

// ── 프로필 인스타그램 스토리 공유 — 구 MypageFragment.shareProfileToInstagram을 그대로 이식 ──

private fun shareProfileToInstagram(context: android.content.Context, profile: UserProfileResDTO) {
    val density = context.resources.displayMetrics.density
    val cardWidth = (context.resources.displayMetrics.widthPixels - (40 * density).toInt())

    val cardView = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        visibility = View.INVISIBLE
        setContent {
            BookiiBookiiTheme {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(BookiiBookiiTheme.colors.white),
                ) {
                    ProfileShareCardContent(
                        name = profile.nickname,
                        motto = profile.introduction ?: "",
                        imageUrl = profile.profileImageUrl,
                        representativeBooks = profile.userBooks ?: emptyList(),
                        isDark = false,
                    )
                }
            }
        }
    }

    val container = (context as? Activity)?.findViewById<ViewGroup>(android.R.id.content) ?: return
    container.addView(cardView, ViewGroup.LayoutParams(cardWidth, ViewGroup.LayoutParams.WRAP_CONTENT))

    cardView.postDelayed({
        try {
            val w = cardView.width
            val h = cardView.height
            if (w <= 0 || h <= 0) {
                container.removeView(cardView)
                context.showCustomToast("공유 준비 중 오류가 발생했습니다.", false)
                return@postDelayed
            }

            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            cardView.draw(canvas)
            container.removeView(cardView)

            val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(imagesDir, "profile_card_${System.currentTimeMillis()}.png")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            launchInstagramStoryIntent(context, uri)
        } catch (e: Exception) {
            if (cardView.isAttachedToWindow) container.removeView(cardView)
            context.showCustomToast("공유 준비 중 오류가 발생했습니다.", false)
        }
    }, 500L)
}

private fun launchInstagramStoryIntent(context: android.content.Context, stickerUri: Uri) {
    val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
        setPackage("com.instagram.android")
        type = "image/*"
        putExtra("interactive_asset_uri", stickerUri)
        putExtra("source_application", context.packageName)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    intent.clipData = ClipData.newRawUri("Sticker", stickerUri)
    context.grantUriPermission(
        "com.instagram.android",
        stickerUri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION,
    )
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        context.showCustomToast("인스타그램 앱을 찾을 수 없습니다.", false)
    }
}
