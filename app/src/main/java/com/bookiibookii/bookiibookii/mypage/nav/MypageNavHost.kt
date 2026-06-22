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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun MypageNavHost(
    mypageViewModel: MypageViewModel,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    startDestination: String = MypageDestinations.MAIN,
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    var pendingDownloadProfile by remember { mutableStateOf<UserProfileResDTO?>(null) }
    val storagePermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val profile = pendingDownloadProfile
        pendingDownloadProfile = null
        if (granted && profile != null) {
            saveProfileCardToGallery(context, coroutineScope, profile)
        } else {
            context.showCustomToast("저장 권한이 필요해요", false)
        }
    }

    fun downloadProfileCard(profile: UserProfileResDTO) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P &&
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            pendingDownloadProfile = profile
            storagePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        saveProfileCardToGallery(context, coroutineScope, profile)
    }

    val popOrExit: () -> Unit = {
        if (!navController.popBackStack()) onBackClick()
    }

    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
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
                onDownloadClick = {
                    mypageViewModel.profileData.value?.let { downloadProfileCard(it) }
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

private fun captureProfileCardBitmap(
    context: android.content.Context,
    profile: UserProfileResDTO,
    onBitmap: (Bitmap?) -> Unit,
) {
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

    val container = (context as? Activity)?.findViewById<ViewGroup>(android.R.id.content) ?: run {
        onBitmap(null)
        return
    }
    container.addView(cardView, ViewGroup.LayoutParams(cardWidth, ViewGroup.LayoutParams.WRAP_CONTENT))

    cardView.postDelayed({
        val bitmap = try {
            val w = cardView.width
            val h = cardView.height
            if (w <= 0 || h <= 0) {
                null
            } else {
                Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { bmp -> cardView.draw(Canvas(bmp)) }
            }
        } catch (e: Exception) {
            null
        } finally {
            if (cardView.isAttachedToWindow) container.removeView(cardView)
        }
        onBitmap(bitmap)
    }, 500L)
}

private fun shareProfileToInstagram(context: android.content.Context, profile: UserProfileResDTO) {
    captureProfileCardBitmap(context, profile) { bitmap ->
        if (bitmap == null) {
            context.showCustomToast("공유 준비 중 오류가 발생했습니다.", false)
            return@captureProfileCardBitmap
        }
        try {
            val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(imagesDir, "profile_card_${System.currentTimeMillis()}.png")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            launchInstagramStoryIntent(context, uri)
        } catch (e: Exception) {
            context.showCustomToast("공유 준비 중 오류가 발생했습니다.", false)
        }
    }
}

private fun saveProfileCardToGallery(
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    profile: UserProfileResDTO,
) {
    captureProfileCardBitmap(context, profile) { bitmap ->
        if (bitmap == null) {
            context.showCustomToast("저장 중 오류가 발생했어요", false)
            return@captureProfileCardBitmap
        }
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val saved = saveProfileBitmapToGallery(context, bitmap)
            bitmap.recycle()
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (saved) context.showCustomToast("사진을 저장했어요", true)
                else context.showCustomToast("사진 저장에 실패했어요", false)
            }
        }
    }
}

private fun saveProfileBitmapToGallery(context: android.content.Context, bitmap: Bitmap): Boolean {
    val resolver = context.contentResolver
    val values = android.content.ContentValues().apply {
        put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "bookii_profile_${System.currentTimeMillis()}.png")
        put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "${android.os.Environment.DIRECTORY_PICTURES}/부키부키")
            put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
        }
    }
    val uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
    return try {
        resolver.openOutputStream(uri)?.use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) } ?: return false
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            values.clear()
            values.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
        true
    } catch (_: Exception) {
        resolver.delete(uri, null, null)
        false
    }
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
