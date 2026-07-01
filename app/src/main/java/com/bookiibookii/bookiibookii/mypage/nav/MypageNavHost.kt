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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
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
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun MypageNavHost(
    mypageViewModel: MypageViewModel,
    onBackClick: () -> Unit = {},
    onLibraryDetailClick: (com.bookiibookii.bookiibookii.data.model.mypage.CompletedBook) -> Unit = {},
    onGroupReviewClick: (com.bookiibookii.bookiibookii.mypage.vm.GroupReviewNavTarget) -> Unit = {},
    modifier: Modifier = Modifier,
    startDestination: String = MypageDestinations.MAIN,
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    var pendingDownloadProfile by remember { mutableStateOf<Pair<UserProfileResDTO, Boolean>?>(null) }
    val storagePermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val pending = pendingDownloadProfile
        pendingDownloadProfile = null
        if (granted && pending != null) {
            saveProfileCardToGallery(context, coroutineScope, pending.first, pending.second)
        } else {
            context.showCustomToast("저장 권한이 필요해요", false)
        }
    }

    fun downloadProfileCard(profile: UserProfileResDTO, isDark: Boolean) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P &&
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            pendingDownloadProfile = profile to isDark
            storagePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        saveProfileCardToGallery(context, coroutineScope, profile, isDark)
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
                onInstagramShareClick = { isDark ->
                    mypageViewModel.profileData.value?.let { shareProfileToInstagram(context, coroutineScope, it, isDark) }
                },
                onDownloadClick = { isDark ->
                    mypageViewModel.profileData.value?.let { downloadProfileCard(it, isDark) }
                },
                onXShareClick = { isDark ->
                    mypageViewModel.profileData.value?.let { shareProfileToX(context, coroutineScope, it, isDark) }
                },
                onLinkCopyClick = { isDark ->
                    copyProfileShareLink(context, coroutineScope, isDark)
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
            MyBookshelfRoute(
                onBack = popOrExit,
                onLibraryClick = onLibraryDetailClick,
                onReviewClick = onGroupReviewClick,
            )
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
    isDark: Boolean,
    onBitmap: (Bitmap?) -> Unit,
) {
    val density = context.resources.displayMetrics.density
    val cardWidth = (context.resources.displayMetrics.widthPixels - (40 * density).toInt())

    val offscreenImageLoader = coil.ImageLoader.Builder(context).allowHardware(false).build()

    val cardView = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        visibility = View.INVISIBLE
        setContent {
            BookiiBookiiTheme {
                Box(
                    modifier = Modifier
                        .background(if (isDark) BookiiBookiiTheme.colors.grey900 else BookiiBookiiTheme.colors.white)
                        .padding(vertical = 20.dp),
                ) {
                    ProfileShareCardContent(
                        name = profile.nickname,
                        motto = profile.introduction ?: "",
                        imageUrl = profile.profileImageUrl,
                        representativeBooks = profile.userBooks ?: emptyList(),
                        isDark = isDark,
                        imageLoader = offscreenImageLoader,
                    )
                }
            }
        }
    }

    val container = (context as? Activity)?.findViewById<ViewGroup>(android.R.id.content) ?: run {
        android.util.Log.e("ProfileShare", "캡처 실패: Activity content 컨테이너를 찾을 수 없음")
        onBitmap(null)
        return
    }
    container.addView(cardView, ViewGroup.LayoutParams(cardWidth, ViewGroup.LayoutParams.WRAP_CONTENT))

    var captured = false
    fun captureAndCleanup() {
        if (captured) return
        captured = true
        val bitmap = try {
            val w = cardView.width
            val h = cardView.height
            if (w <= 0 || h <= 0) {
                android.util.Log.e("ProfileShare", "캡처 실패: 잘못된 크기 ${w}x$h")
                null
            } else {
                Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { bmp -> cardView.draw(Canvas(bmp)) }
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileShare", "캡처 실패: 예외 발생", e)
            null
        } finally {
            if (cardView.isAttachedToWindow) container.removeView(cardView)
        }
        onBitmap(bitmap)
    }

    cardView.viewTreeObserver.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (cardView.width > 0 && cardView.height > 0) {
                cardView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                cardView.postDelayed(::captureAndCleanup, 300L)
            }
        }
    })
    cardView.postDelayed(::captureAndCleanup, 2000L)
}


private fun compositeProfileCardFullBleed(card: Bitmap): Bitmap {
    val storyW = 1080
    val storyH = 1920
    val out = Bitmap.createBitmap(storyW, storyH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(out)
    canvas.drawColor(android.graphics.Color.WHITE)

    val scale = maxOf(storyW.toFloat() / card.width, storyH.toFloat() / card.height)
    val drawW = (card.width * scale).toInt()
    val drawH = (card.height * scale).toInt()
    val dx = ((storyW - drawW) / 2).toFloat()
    val dy = ((storyH - drawH) / 2).toFloat()

    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
    canvas.drawBitmap(card, null, android.graphics.RectF(dx, dy, dx + drawW, dy + drawH), paint)
    return out
}

private fun shareProfileToInstagram(
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    profile: UserProfileResDTO,
    isDark: Boolean,
) {
    captureProfileCardBitmap(context, profile, isDark) { bitmap ->
        if (bitmap == null) {
            context.showCustomToast("공유 준비 중 오류가 발생했습니다.", false)
            return@captureProfileCardBitmap
        }
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }

                val fullBleedBitmap = compositeProfileCardFullBleed(bitmap)
                bitmap.recycle()
                val backgroundFile = File(imagesDir, "profile_bg_${System.currentTimeMillis()}.png")
                backgroundFile.outputStream().use { fullBleedBitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                fullBleedBitmap.recycle()
                val backgroundUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", backgroundFile)

                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    launchInstagramStoryBackgroundOnly(context, backgroundUri)
                }
            } catch (e: Exception) {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    context.showCustomToast("공유 준비 중 오류가 발생했습니다.", false)
                }
            }
        }
    }
}

private fun saveProfileCardToGallery(
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    profile: UserProfileResDTO,
    isDark: Boolean,
) {
    captureProfileCardBitmap(context, profile, isDark) { bitmap ->
        if (bitmap == null) {
            context.showCustomToast("저장 중 오류가 발생했어요", false)
            return@captureProfileCardBitmap
        }
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val finalBitmap = compositeProfileCardFullBleed(bitmap)
            bitmap.recycle()
            val saved = saveProfileBitmapToGallery(context, finalBitmap)
            finalBitmap.recycle()
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

private suspend fun fetchProfileShareUrl(isDark: Boolean = false): String? {
    return withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.mypApi().createProfileShareToken()
            response.body()?.result?.shareUrl?.let { url ->
                if (isDark) "$url?dark=1" else url
            }
        } catch (_: Exception) {
            null
        }
    }
}

private fun copyProfileShareLink(context: android.content.Context, coroutineScope: CoroutineScope, isDark: Boolean = false) {
    coroutineScope.launch {
        val shareUrl = fetchProfileShareUrl(isDark) ?: run {
            context.showCustomToast("링크 생성에 실패했어요", false)
            return@launch
        }
        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("프로필 링크", shareUrl))
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            context.showCustomToast("링크를 복사했어요", true)
        }
    }
}

private fun shareProfileToX(
    context: android.content.Context,
    coroutineScope: CoroutineScope,
    profile: UserProfileResDTO,
    isDark: Boolean = false,
) {
    coroutineScope.launch {
        val shareUrl = fetchProfileShareUrl(isDark) ?: run {
            context.showCustomToast("링크 생성에 실패했어요", false)
            return@launch
        }
        val text = "${profile.nickname}님의 부키부키 프로필"
        val intentUrl = "https://twitter.com/intent/tweet?text=" + Uri.encode(text) + "&url=" + Uri.encode(shareUrl)
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(intentUrl)))
        } catch (_: Exception) {
            context.showCustomToast("X를 열 수 없어요", false)
        }
    }
}

private fun launchInstagramStoryIntent(context: android.content.Context, stickerUri: Uri, backgroundUri: Uri) {
    val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
        setPackage("com.instagram.android")
        setDataAndType(backgroundUri, "image/*")
        putExtra("interactive_asset_uri", stickerUri)
        putExtra("source_application", context.packageName)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    intent.clipData = ClipData.newRawUri("Sticker", stickerUri).also {
        it.addItem(ClipData.Item(backgroundUri))
    }
    val resInfoList = context.packageManager.queryIntentActivities(intent, 0)
    for (info in resInfoList) {
        val pkg = info.activityInfo.packageName
        context.grantUriPermission(pkg, stickerUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.grantUriPermission(pkg, backgroundUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        context.showCustomToast("인스타그램 앱을 찾을 수 없습니다.", false)
    }
}

private fun launchInstagramStoryBackgroundOnly(context: android.content.Context, backgroundUri: Uri) {
    val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
        setPackage("com.instagram.android")
        setDataAndType(backgroundUri, "image/*")
        putExtra("source_application", context.packageName)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    intent.clipData = ClipData.newRawUri("Background", backgroundUri)
    val resInfoList = context.packageManager.queryIntentActivities(intent, 0)
    for (info in resInfoList) {
        context.grantUriPermission(info.activityInfo.packageName, backgroundUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        context.showCustomToast("인스타그램 앱을 찾을 수 없습니다.", false)
    }
}
