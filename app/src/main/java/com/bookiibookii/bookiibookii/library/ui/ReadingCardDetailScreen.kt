package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.ui.component.BottomSheetBtnStyle
import com.bookiibookii.bookiibookii.ui.component.BottomSheetTwoBtnShort
import com.bookiibookii.bookiibookii.ui.component.LocalOnProfileClick
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.common.stripBookSubtitle
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.bookiibookii.bookiibookii.ui.theme.MaruBuri
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.bookiibookii.bookiibookii.ui.component.showCustomToast
import java.util.UUID

private data class Reaction(
    val iconRes: Int,
    val label: String,
)

private val reactionList = listOf(
    Reaction(R.drawable.ic_empathy, "공감해요"),
    Reaction(R.drawable.ic_good,    "좋아요"),
    Reaction(R.drawable.ic_fun,     "웃겨요"),
    Reaction(R.drawable.ic_sad,     "슬퍼요"),
    Reaction(R.drawable.ic_angry,   "화나요"),
)

private val reactionToApiKey = mapOf(
    "공감해요" to "FEELYOU",
    "좋아요"  to "LIKE",
    "웃겨요"  to "FUN",
    "슬퍼요"  to "SAD",
    "화나요"  to "ANGRY",
)

private val apiKeyToReaction: Map<String, Reaction> by lazy {
    mapOf(
        "FEELYOU" to reactionList[0],
        "LIKE"    to reactionList[1],
        "FUN"     to reactionList[2],
        "SAD"     to reactionList[3],
        "ANGRY"   to reactionList[4],
    )
}

private data class Particle(
    val id: String,
    val iconResId: Int,
    val sizeDp: Float,
    val startX: Float,
    val targetY: Float,
    val rotation: Float,
)


private fun saveCardBitmapToGallery(context: android.content.Context, bitmap: android.graphics.Bitmap): Boolean {
    val resolver = context.contentResolver
    val values = android.content.ContentValues().apply {
        put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "bookii_card_${System.currentTimeMillis()}.png")
        put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "${android.os.Environment.DIRECTORY_PICTURES}/부키부키")
            put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
        }
    }
    val uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
    return try {
        resolver.openOutputStream(uri)?.use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
        } ?: return false
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

private fun compositeCardOnWhiteBackground(card: android.graphics.Bitmap): android.graphics.Bitmap {
    val canvasWidth = 1080
    val canvasHeight = 1920
    val canvasBitmap = android.graphics.Bitmap.createBitmap(canvasWidth, canvasHeight, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(canvasBitmap)
    canvas.drawColor(android.graphics.Color.WHITE)

    val maxWidth = canvasWidth * 0.85f
    val maxHeight = canvasHeight * 0.75f
    val scale = minOf(maxWidth / card.width, maxHeight / card.height, 1f)
    val drawWidth = (card.width * scale).toInt()
    val drawHeight = (card.height * scale).toInt()
    val left = ((canvasWidth - drawWidth) / 2).toFloat()
    val top = ((canvasHeight - drawHeight) / 2).toFloat()

    val shadowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        maskFilter = android.graphics.BlurMaskFilter(30f, android.graphics.BlurMaskFilter.Blur.NORMAL)
        color = android.graphics.Color.argb(50, 0, 0, 0)
    }
    canvas.drawRect(left, top, left + drawWidth, top + drawHeight, shadowPaint)

    val cardPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
    canvas.drawBitmap(card, null, android.graphics.RectF(left, top, left + drawWidth, top + drawHeight), cardPaint)
    return canvasBitmap
}

private fun captureShareableCardBitmap(
    context: android.content.Context,
    card: ReadingCard,
    cardVersion: Int,
    onBitmap: (android.graphics.Bitmap?) -> Unit,
) {
    val cardWidthPx = (context.resources.displayMetrics.widthPixels * 0.85f).toInt()
    val cardHeightPx = (cardWidthPx * 464f / 320f).toInt()

    val cardView = androidx.compose.ui.platform.ComposeView(context).apply {
        setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        visibility = android.view.View.INVISIBLE
        setContent {
            BookiiBookiiTheme {
                ShareableCard(card = card, cardVersion = cardVersion)
            }
        }
    }

    val container = (context as? android.app.Activity)?.findViewById<android.view.ViewGroup>(android.R.id.content) ?: run {
        onBitmap(null)
        return
    }
    container.addView(cardView, android.view.ViewGroup.LayoutParams(cardWidthPx, cardHeightPx))

    cardView.postDelayed({
        val bitmap = try {
            val w = cardView.width
            val h = cardView.height
            if (w <= 0 || h <= 0) {
                null
            } else {
                android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888).also { bmp ->
                    cardView.draw(android.graphics.Canvas(bmp))
                }
            }
        } catch (_: Exception) {
            null
        } finally {
            if (cardView.isAttachedToWindow) container.removeView(cardView)
        }
        onBitmap(bitmap)
    }, 500L)
}

private fun launchInstagramStoryIntentFor(context: android.content.Context, stickerUri: android.net.Uri, backgroundUri: android.net.Uri) {
    val intent = android.content.Intent("com.instagram.share.ADD_TO_STORY").apply {
        setPackage("com.instagram.android")
        setDataAndType(backgroundUri, "image/*")
        putExtra("interactive_asset_uri", stickerUri)
        putExtra("source_application", context.packageName)
        flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    intent.clipData = android.content.ClipData.newRawUri("Sticker", stickerUri).also {
        it.addItem(android.content.ClipData.Item(backgroundUri))
    }
    val resInfoList = context.packageManager.queryIntentActivities(intent, 0)
    for (info in resInfoList) {
        val pkg = info.activityInfo.packageName
        context.grantUriPermission(pkg, stickerUri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.grantUriPermission(pkg, backgroundUri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        context.showCustomToast("인스타그램 앱을 찾을 수 없습니다.", false)
    }
}

private fun launchInstagramStoryBackgroundOnly(context: android.content.Context, backgroundUri: android.net.Uri) {
    val intent = android.content.Intent("com.instagram.share.ADD_TO_STORY").apply {
        setPackage("com.instagram.android")
        setDataAndType(backgroundUri, "image/*")
        putExtra("source_application", context.packageName)
        flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    intent.clipData = android.content.ClipData.newRawUri("Background", backgroundUri)
    val resInfoList = context.packageManager.queryIntentActivities(intent, 0)
    for (info in resInfoList) {
        context.grantUriPermission(info.activityInfo.packageName, backgroundUri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        context.showCustomToast("인스타그램 앱을 찾을 수 없습니다.", false)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingCardDetailRoute(
    initialIndex: Int,
    sortByLatest: Boolean,
    cardsJson: String,
    onBackClick: () -> Unit,
    onEditCard: (card: ReadingCard) -> Unit,
    onDeleted: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val cards = remember(cardsJson) {
        try {
            com.google.gson.Gson().fromJson(cardsJson, object : com.google.gson.reflect.TypeToken<List<ReadingCard>>() {}.type)
                ?: emptyList<ReadingCard>()
        } catch (_: Exception) {
            emptyList<ReadingCard>()
        }
    }

    var pendingDownload by remember { mutableStateOf<Pair<ReadingCard, Int>?>(null) }
    val storagePermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val pending = pendingDownload
        pendingDownload = null
        if (granted && pending != null) {
            saveCardToGalleryFor(context, coroutineScope, pending.first, pending.second)
        } else {
            context.showCustomToast("저장 권한이 필요해요", false)
        }
    }

    fun deleteCard(card: ReadingCard) {
        coroutineScope.launch {
            val success = try {
                val resp = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.bookiibookii.bookiibookii.data.api.RetrofitClient.libApi().deleteCard(card.cardId)
                }
                resp.isSuccessful && resp.body()?.isSuccess == true
            } catch (_: Exception) {
                false
            }
            if (success) {
                context.showCustomToast("독서카드를 삭제했어요", true)
                onDeleted()
            } else {
                context.showCustomToast("삭제에 실패했어요", false)
            }
        }
    }

    fun fetchShareUrl(card: ReadingCard, cardVersion: Int, onResult: (String?) -> Unit) {
        coroutineScope.launch {
            // PHOTO: v1=OVERLAY, v2=SPLIT | QUOTE: v1=SPLIT, v2=OVERLAY
            val shareLayout = if (card.type == ReadingCardType.PHOTO) {
                if (cardVersion == 1) "OVERLAY" else "SPLIT"
            } else {
                if (cardVersion == 1) "SPLIT" else "OVERLAY"
            }
            val shareUrl = try {
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val resp = com.bookiibookii.bookiibookii.data.api.RetrofitClient.libApi()
                        .createShareToken(card.cardId, com.bookiibookii.bookiibookii.data.model.library.CreateShareTokenRequestDTO(shareLayout))
                    if (!resp.isSuccessful) {
                        android.util.Log.e("ShareToken", "createShareToken 실패: code=${resp.code()}, body=${resp.errorBody()?.string()}")
                    } else if (resp.body()?.isSuccess != true) {
                        android.util.Log.e("ShareToken", "createShareToken 실패: isSuccess=false, message=${resp.body()?.message}")
                    }
                    resp.body()?.result?.shareUrl
                }
            } catch (e: Exception) {
                android.util.Log.e("ShareToken", "createShareToken 예외 발생", e)
                null
            }
            onResult(shareUrl)
        }
    }

    fun copyShareLink(card: ReadingCard, cardVersion: Int) {
        fetchShareUrl(card, cardVersion) { shareUrl ->
            if (shareUrl.isNullOrBlank()) {
                context.showCustomToast("링크 복사에 실패했어요", false)
            } else {
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("독서카드 링크", shareUrl))
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
                    context.showCustomToast("링크를 복사했어요", true)
                }
            }
        }
    }

    fun shareToKakao(card: ReadingCard, cardVersion: Int) {
        fetchShareUrl(card, cardVersion) { shareUrl ->
            if (shareUrl.isNullOrBlank()) {
                context.showCustomToast("공유에 실패했어요", false)
                return@fetchShareUrl
            }
            val shareToken = android.net.Uri.parse(shareUrl).lastPathSegment.orEmpty()
            val link = com.kakao.sdk.template.model.Link(
                webUrl = shareUrl,
                mobileWebUrl = shareUrl,
                androidExecutionParams = mapOf("shareToken" to shareToken),
                iosExecutionParams = mapOf("shareToken" to shareToken),
            )
            val feed = com.kakao.sdk.template.model.FeedTemplate(
                content = com.kakao.sdk.template.model.Content(
                    title = card.bookTitle.ifBlank { "독서카드" },
                    description = card.quotation.ifBlank { card.content },
                    imageUrl = card.imageUrl.orEmpty(),
                    link = link,
                ),
                buttons = listOf(com.kakao.sdk.template.model.Button("보러가기", link)),
            )
            if (com.kakao.sdk.share.ShareClient.instance.isKakaoTalkSharingAvailable(context)) {
                com.kakao.sdk.share.ShareClient.instance.shareDefault(context, feed) { result, error ->
                    when {
                        error != null -> context.showCustomToast("공유에 실패했어요", false)
                        result != null -> context.startActivity(result.intent)
                    }
                }
            } else {
                try {
                    context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, com.kakao.sdk.share.WebSharerClient.instance.makeDefaultUrl(feed)))
                } catch (_: Exception) {
                    context.showCustomToast("카카오톡을 열 수 없어요", false)
                }
            }
        }
    }

    fun shareToX(card: ReadingCard, cardVersion: Int) {
        fetchShareUrl(card, cardVersion) { shareUrl ->
            if (shareUrl.isNullOrBlank()) {
                context.showCustomToast("공유에 실패했어요", false)
                return@fetchShareUrl
            }
            val text = card.bookTitle.stripBookSubtitle().ifBlank { "독서카드" }
            val intentUrl = "https://twitter.com/intent/tweet?text=" + android.net.Uri.encode(text) + "&url=" + android.net.Uri.encode(shareUrl)
            try {
                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(intentUrl)))
            } catch (_: Exception) {
                context.showCustomToast("X를 열 수 없어요", false)
            }
        }
    }

    fun shareCardToInstagram(card: ReadingCard, cardVersion: Int) {
        captureShareableCardBitmap(context, card, cardVersion) { bitmap ->
            if (bitmap == null) {
                context.showCustomToast("공유 준비 중 오류가 발생했습니다.", false)
                return@captureShareableCardBitmap
            }
            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val imagesDir = java.io.File(context.cacheDir, "images").apply { mkdirs() }
                    imagesDir.listFiles()?.forEach { it.delete() }

                    val bgBitmap = compositeCardOnWhiteBackground(bitmap)
                    bitmap.recycle()
                    val bgFile = java.io.File(imagesDir, "bg_${System.currentTimeMillis()}.png")
                    java.io.FileOutputStream(bgFile).use { bgBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
                    bgBitmap.recycle()
                    val backgroundUri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", bgFile)

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

    fun downloadCard(card: ReadingCard, cardVersion: Int) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P &&
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            pendingDownload = card to cardVersion
            storagePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        saveCardToGalleryFor(context, coroutineScope, card, cardVersion)
    }

    ReadingCardDetailScreen(
        cards = cards,
        initialIndex = initialIndex,
        sortByLatest = sortByLatest,
        onBackClick = onBackClick,
        onBookmarkToggle = { cardId ->
            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val resp = com.bookiibookii.bookiibookii.data.api.RetrofitClient.libApi().toggleBookmark(cardId)
                    if (!resp.isSuccessful) {
                        withContext(kotlinx.coroutines.Dispatchers.Main) { context.showCustomToast("북마크 처리에 실패했어요", false) }
                    }
                } catch (_: Exception) {
                    withContext(kotlinx.coroutines.Dispatchers.Main) { context.showCustomToast("네트워크 오류가 발생했어요", false) }
                }
            }
        },
        onReactionToggle = { cardId, reactionLabel ->
            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val resp = com.bookiibookii.bookiibookii.data.api.RetrofitClient.libApi().toggleReaction(
                        cardId,
                        com.bookiibookii.bookiibookii.data.model.library.MemberCardReactionToggleRequestDTO(reaction = reactionLabel),
                    )
                    if (!resp.isSuccessful) {
                        withContext(kotlinx.coroutines.Dispatchers.Main) { context.showCustomToast("반응 처리에 실패했어요", false) }
                    }
                } catch (_: Exception) {
                    withContext(kotlinx.coroutines.Dispatchers.Main) { context.showCustomToast("네트워크 오류가 발생했어요", false) }
                }
            }
        },
        onInstaShare = { card, version -> shareCardToInstagram(card, version) },
        onCopyLink = { card, version -> copyShareLink(card, version) },
        onKakaoShare = { card, version -> shareToKakao(card, version) },
        onXShare = { card, version -> shareToX(card, version) },
        onDownload = { card, version -> downloadCard(card, version) },
        onEditClick = { card -> onEditCard(card) },
        onDeleteConfirmed = { card -> deleteCard(card) },
    )
}

private fun saveCardToGalleryFor(
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    card: ReadingCard,
    cardVersion: Int,
) {
    captureShareableCardBitmap(context, card, cardVersion) { bitmap ->
        if (bitmap == null) {
            context.showCustomToast("저장 중 오류가 발생했어요", false)
            return@captureShareableCardBitmap
        }
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val finalBitmap = compositeCardOnWhiteBackground(bitmap)
            bitmap.recycle()
            val saved = saveCardBitmapToGallery(context, finalBitmap)
            finalBitmap.recycle()
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (saved) context.showCustomToast("사진을 저장했어요", true)
                else context.showCustomToast("사진 저장에 실패했어요", false)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingCardDetailScreen(
    cards: List<ReadingCard> = emptyList(),
    initialIndex: Int = 0,
    sortByLatest: Boolean = true,
    onBackClick: () -> Unit = {},
    onBookmarkToggle: (cardId: Long) -> Unit = {},
    onReactionToggle: (cardId: Long, reaction: String) -> Unit = { _, _ -> },
    onInstaShare: (card: ReadingCard, cardVersion: Int) -> Unit = { _, _ -> },
    onCopyLink: (card: ReadingCard, cardVersion: Int) -> Unit = { _, _ -> },
    onKakaoShare: (card: ReadingCard, cardVersion: Int) -> Unit = { _, _ -> },
    onXShare: (card: ReadingCard, cardVersion: Int) -> Unit = { _, _ -> },
    onDownload: (card: ReadingCard, cardVersion: Int) -> Unit = { _, _ -> },
    onEditClick: (card: ReadingCard) -> Unit = {},
    onDeleteConfirmed: (card: ReadingCard) -> Unit = {},
) {
    val context        = LocalContext.current
    val pagerState    = rememberPagerState(initialPage = initialIndex) { cards.size }
    val coroutineScope = rememberCoroutineScope()
    var showShareSheet  by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var cardVersion     by remember { mutableStateOf(1) }
    var showCoachMark        by remember { mutableStateOf(!TokenManager.isReadingCardCoachMarkDone(context)) }
    var bookmarkButtonTopLeft by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var shareButtonTopLeft    by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    val bookmarkStates = remember(cards) {
        mutableStateListOf(*Array(cards.size) { i -> cards.getOrNull(i)?.isBookmarked ?: false })
    }

    val reactionStates = remember(cards) {
        mutableStateMapOf(*Array(cards.size) { i ->
            i to (cards.getOrNull(i)?.myReactions?.mapNotNull { apiKeyToReaction[it] } ?: emptyList<Reaction>())
        })
    }
    val particles = remember { mutableStateListOf<Particle>() }

    LaunchedEffect(pagerState.currentPage) {
        particles.clear()
        cardVersion = 1
    }

    val activeReactionList = reactionStates[pagerState.currentPage] ?: emptyList()

    val currentCard     = cards.getOrNull(pagerState.currentPage)
    val progress        = if (cards.isNotEmpty()) (pagerState.currentPage + 1).toFloat() / cards.size else 1f
    val currentBookmark = bookmarkStates.getOrElse(pagerState.currentPage) { false }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.uiBg)) {

            CardDetailHeader(
                onBackClick = onBackClick,
                onShareClick = { showShareSheet = true },
                showMenu = currentCard?.isMine == true,
                onEditClick = { currentCard?.let(onEditClick) },
                onDeleteClick = { showDeleteDialog = true },
                onShareButtonPositioned = { shareButtonTopLeft = it },
            )

            CardInfoArea(
                card             = currentCard,
                sortByLatest     = sortByLatest,
                progress         = progress,
                isBookmarked     = currentBookmark,
                onBookmarkPositioned = { bookmarkButtonTopLeft = it },
                onBookmarkToggle = {
                    val idx = pagerState.currentPage
                    if (idx in bookmarkStates.indices) {
                        bookmarkStates[idx] = !bookmarkStates[idx]
                        currentCard?.cardId?.let { onBookmarkToggle(it) }
                    }
                },
            )

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                HorizontalPager(
                    state          = pagerState,
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    pageSpacing    = 8.dp,
                    modifier       = Modifier.fillMaxSize(),
                ) { page ->
                    val card          = cards[page]
                    val isCurrentPage = page == pagerState.currentPage
                    ReadingCardDetailItem(
                        card        = card,
                        cardVersion = if (isCurrentPage) cardVersion else 1,
                        particles   = if (isCurrentPage) particles else emptyList(),
                        onParticleEnd = { particles.remove(it) },
                        modifier    = Modifier.fillMaxHeight().padding(vertical = 8.dp),
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                val currentType = currentCard?.type ?: ReadingCardType.PHOTO
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CardVersionDot(version = 1, cardType = currentType, isSelected = cardVersion == 1) { cardVersion = 1 }
                    CardVersionDot(version = 2, cardType = currentType, isSelected = cardVersion == 2) { cardVersion = 2 }
                }
            }

        ReactionBar(
            activeReactions = activeReactionList,
            onReact = { reaction ->
                val currentPage     = pagerState.currentPage
                val currentReactions = reactionStates[currentPage] ?: emptyList()
                val label           = reaction.label
                val isAlreadyActive = currentReactions.any { it.label == label }

                if (isAlreadyActive) {
                    reactionStates[currentPage] = currentReactions.filter { it.label != label }
                } else {
                    reactionStates[currentPage] = currentReactions + reaction
                    coroutineScope.launch {
                        val burstCount = (10..14).random()
                        val half = burstCount / 2
                        val others = (reactionStates[currentPage] ?: emptyList())
                            .filter { it.label != reaction.label }
                            .ifEmpty { null }
                        repeat(burstCount) { idx ->
                            delay((10..80).random().toLong())
                            val r = if (idx < half || others == null) reaction else others.random()
                            particles.add(
                                Particle(
                                    id        = UUID.randomUUID().toString(),
                                    iconResId = r.iconRes,
                                    sizeDp    = (28..46).random().toFloat(),
                                    startX    = (-10..80).random().toFloat(),
                                    targetY   = -(180..400).random().toFloat(),
                                    rotation  = (-35..35).random().toFloat(),
                                )
                            )
                        }
                        }
                    }
                    val apiKey = reactionToApiKey[label] ?: label
                    currentCard?.cardId?.let { onReactionToggle(it, apiKey) }
                },
                modifier = Modifier.navigationBarsPadding().padding(bottom = 20.dp),
            )
        }

        if (showCoachMark) {
            ReadingCardCoachMarkOverlay(
                bookmarkTopLeft = bookmarkButtonTopLeft,
                shareTopLeft    = shareButtonTopLeft,
                onDismiss = {
                    showCoachMark = false
                    TokenManager.saveReadingCardCoachMarkDone(context)
                },
            )
        }
    }

    if (showShareSheet) {
        ReadingCardShareBottomSheet(
            onDismiss    = { showShareSheet = false },
            onKakaoClick = { currentCard?.let { onKakaoShare(it, cardVersion) } },
            onInstaClick = { currentCard?.let { onInstaShare(it, cardVersion) } },
            onXClick     = { currentCard?.let { onXShare(it, cardVersion) } },
            onDownloadClick = { currentCard?.let { onDownload(it, cardVersion) } },
            onCopyLinkClick = { currentCard?.let { onCopyLink(it, cardVersion) } },
        )
    }

    if (showDeleteDialog) {
        Dialog(onDismissRequest = { showDeleteDialog = false }) {
            ReadingCardDeleteDialog(
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    showDeleteDialog = false
                    currentCard?.let(onDeleteConfirmed)
                },
            )
        }
    }
}

@Composable
private fun ReadingCardDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round24)
            .background(BookiiBookiiTheme.colors.white)
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "독서카드 삭제",
                style = BookiiBookiiTheme.typography.bold24,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BookiiBookiiTheme.colors.grey100)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = "닫기",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(
            text = "독서카드를 삭제하시겠습니까? 삭제하면 즉시 사라지며, 이후 되돌릴 수 없습니다.",
            style = BookiiBookiiTheme.typography.regular16,
            color = BookiiBookiiTheme.colors.grey900,
            modifier = Modifier.padding(top = 24.dp),
        )
        Row(
            modifier = Modifier.padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BottomSheetTwoBtnShort(
                text = "취소",
                style = BottomSheetBtnStyle.White,
                textStyle = BookiiBookiiTheme.typography.regular16,
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
            BottomSheetTwoBtnShort(
                text = "삭제",
                style = BottomSheetBtnStyle.Red,
                textStyle = BookiiBookiiTheme.typography.regular16,
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FloatingParticle(particle: Particle, onAnimationEnd: (Particle) -> Unit) {
    val scale      = remember { Animatable(0f) }
    val rotation   = remember { Animatable(0f) }
    val translateY = remember { Animatable(0f) }
    val alpha      = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(1.2f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
            scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
        }
        launch {
            rotation.animateTo(particle.rotation, animationSpec = tween(durationMillis = 1900, easing = LinearOutSlowInEasing))
        }
        launch {
            delay(120)
            translateY.animateTo(particle.targetY, animationSpec = tween(durationMillis = 1700, easing = LinearOutSlowInEasing))
        }
        launch {
            delay(1100)
            alpha.animateTo(0f, animationSpec = tween(durationMillis = 800))
        }
        delay(1950)
        onAnimationEnd(particle)
    }

    Box(
        modifier = Modifier
            .offset(x = particle.startX.dp, y = translateY.value.dp)
            .rotate(rotation.value)
            .scale(scale.value)
            .alpha(alpha.value)
            .size(particle.sizeDp.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter            = painterResource(particle.iconResId),
            contentDescription = null,
            tint               = Color.Unspecified,
            modifier           = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun BoxScope.ReactionOverlay(
    particles: List<Particle>,
    onParticleEnd: (Particle) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        particles.forEach { particle ->
            key(particle.id) {
                FloatingParticle(particle = particle, onAnimationEnd = { onParticleEnd(it) })
            }
        }
    }
}

@Composable
private fun ReadingCardDetailItem(
    card: ReadingCard,
    cardVersion: Int,
    particles: List<Particle>,
    onParticleEnd: (Particle) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .shadow(elevation = 10.dp, shape = cardShape, ambientColor = Color(0x1A000000), spotColor = Color(0x1A000000))
            .clip(cardShape)
            .background(BookiiBookiiTheme.colors.white)
            .fillMaxWidth(),
    ) {
        when (card.type) {
            ReadingCardType.PHOTO -> PhotoCard(card, cardVersion, particles, onParticleEnd)
            ReadingCardType.QUOTE -> QuoteCard(card, cardVersion, particles, onParticleEnd)
        }
    }
}

private val photoCardTopGradient = Brush.verticalGradient(
    0f to Color.White,
    0.11f to Color.White,
    0.41f to Color.White.copy(alpha = 0.9f),
    1f to Color.White.copy(alpha = 0f),
)
private const val PHOTO_GRADIENT_HEIGHT_FRACTION = 0.55f

@Composable
private fun PhotoCard(
    card: ReadingCard,
    cardVersion: Int,
    particles: List<Particle>,
    onParticleEnd: (Particle) -> Unit,
) {
    if (cardVersion == 1) {
        // OVERLAY (첫 번째 타입)
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.grey300)) {
                if (!card.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model              = card.imageUrl,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.matchParentSize(),
                    )
                }
            }
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(PHOTO_GRADIENT_HEIGHT_FRACTION).background(photoCardTopGradient))
            if (card.content.isNotBlank()) {
                Text(
                    text = card.content,
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey800,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 52.dp, end = 20.dp),
                )
            }
            ReactionOverlay(particles, onParticleEnd, Modifier.align(Alignment.BottomStart).padding(bottom = 56.dp, start = 24.dp))
        }
    } else {
        // SPLIT (두 번째 타입)
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(336f / 464f).background(BookiiBookiiTheme.colors.grey300),
                ) {
                    if (!card.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model              = card.imageUrl,
                            contentDescription = null,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.matchParentSize(),
                        )
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth().weight(128f / 464f),
                ) {
                    if (card.content.isNotBlank()) {
                        Text(
                            text = card.content,
                            style = BookiiBookiiTheme.typography.regular16,
                            color = BookiiBookiiTheme.colors.grey800,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp).align(Alignment.TopStart),
                        )
                    }
                }
            }
            ReactionOverlay(particles, onParticleEnd, Modifier.align(Alignment.BottomStart).padding(bottom = 128.dp, start = 24.dp))
        }
    }
}

@Composable
private fun QuoteCard(
    card: ReadingCard,
    cardVersion: Int,
    particles: List<Particle>,
    onParticleEnd: (Particle) -> Unit,
) {
    val quotationText = card.quotation.ifBlank { card.content }

    val topGradient = if (cardVersion == 1) {
        Brush.linearGradient(
            colors = listOf(Color.White, Color(0xFFFFC9A4)),
            start  = Offset(0f, 0f),
            end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFFFF4E18), Color(0xFFFF7618), Color(0xFFFFC9A4)),
            start  = Offset(0f, 0f),
            end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )
    }
    val iconTint  = BookiiBookiiTheme.colors.uiMain150
    val textColor = if (cardVersion == 1) BookiiBookiiTheme.colors.uiMain else Color.White

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(336f / 464f).background(topGradient),
            ) {
                Column(
                    modifier            = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Icon(painter = painterResource(R.drawable.ic_quote), contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
                    Text(text = "“$quotationText”", style = TextStyle(fontFamily = MaruBuri, fontWeight = FontWeight.Bold, fontSize = 20.sp), color = textColor, overflow = TextOverflow.Ellipsis)
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().weight(128f / 464f),
            ) {
                if (card.content.isNotBlank()) {
                    Text(
                        text     = card.content,
                        style    = BookiiBookiiTheme.typography.regular16,
                        color    = BookiiBookiiTheme.colors.grey800,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 16.dp, end = 20.dp),
                    )
                }
            }
        }
        ReactionOverlay(particles, onParticleEnd, Modifier.align(Alignment.BottomStart).padding(bottom = 24.dp, start = 24.dp))
    }
}

@Composable
internal fun ShareableCard(card: ReadingCard, cardVersion: Int = 2, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(20.dp)).background(BookiiBookiiTheme.colors.white)) {
        when (card.type) {
            ReadingCardType.PHOTO -> if (cardVersion == 1) {
                // OVERLAY (첫 번째 타입)
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.grey300)) {
                        if (!card.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(card.imageUrl)
                                    .allowHardware(false)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize(),
                            )
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(PHOTO_GRADIENT_HEIGHT_FRACTION).background(photoCardTopGradient))
                    if (card.bookTitle.isNotBlank()) {
                        Box(modifier = Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 20.dp, end = 40.dp)) {
                            BookTitleChip(title = card.bookTitle.stripBookSubtitle(), style = BookTitleChipStyle.PALE_FILL)
                        }
                    }
                    Icon(
                        painter = painterResource(R.drawable.ic_logo_symbol),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 18.dp).size(20.dp),
                    )
                    if (card.content.isNotBlank()) {
                        Text(card.content, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey800, maxLines = 4, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 52.dp, end = 20.dp))
                    }
                    if (card.username.isNotBlank()) {
                        Text("by. ${card.username}", style = BookiiBookiiTheme.typography.regular14, color = Color.White,
                            modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, bottom = 20.dp))
                    }
                }
            } else {
                // SPLIT (두 번째 타입)
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(336f / 464f).background(BookiiBookiiTheme.colors.grey300)) {
                        if (!card.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(card.imageUrl)
                                    .allowHardware(false)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize(),
                            )
                        }
                        if (card.bookTitle.isNotBlank()) {
                            Box(modifier = Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 20.dp, end = 40.dp)) {
                                BookTitleChip(title = card.bookTitle.stripBookSubtitle())
                            }
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().weight(128f / 464f)) {
                        if (card.content.isNotBlank()) {
                            Text(card.content, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey800, maxLines = 4, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 20.dp, end = 20.dp))
                        }
                        if (card.username.isNotBlank()) {
                            Text("by. ${card.username}", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey400,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 16.dp))
                        }
                    }
                }
            }
            ReadingCardType.QUOTE -> {
                val quotationText = card.quotation.ifBlank { card.content }
                val gradient = if (cardVersion == 1) {
                    Brush.linearGradient(
                        colors = listOf(Color.White, Color(0xFFFFC9A4)),
                        start  = Offset(0f, 0f),
                        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFF4E18), Color(0xFFFF7618), Color(0xFFFFC9A4)),
                        start  = Offset(0f, 0f),
                        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    )
                }
                val iconTint  = BookiiBookiiTheme.colors.uiMain150
                val textColor = if (cardVersion == 1) BookiiBookiiTheme.colors.uiMain else Color.White
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(336f / 464f).background(gradient)) {
                        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Spacer(modifier = Modifier.height(20.dp))
                            if (card.bookTitle.isNotBlank()) {
                                BookTitleChip(
                                    title = card.bookTitle.stripBookSubtitle(),
                                    style = if (cardVersion == 2) BookTitleChipStyle.WHITE_STROKE else BookTitleChipStyle.SOLID,
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Icon(painter = painterResource(R.drawable.ic_quote), contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
                            val displayQuotation = "“$quotationText”"
                            Text(displayQuotation, style = TextStyle(fontFamily = MaruBuri, fontWeight = FontWeight.Bold, fontSize = 20.sp), color = textColor, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().weight(128f / 464f)) {
                        if (card.content.isNotBlank()) {
                            Text(card.content, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey800, maxLines = 4, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 20.dp, end = 20.dp))
                        }
                        if (card.username.isNotBlank()) {
                            Text("by. ${card.username}", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey400,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardVersionDot(
    version: Int,
    cardType: ReadingCardType,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) Color(0x99100F0E) else Color(0x66E2E1DF)

    Box(
        modifier = Modifier.size(46.dp).clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).border(2.dp, borderColor, CircleShape),
        ) {
            when (cardType) {
                ReadingCardType.PHOTO -> {
                    if (version == 1) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.White, Color(0xFFFF7618)),
                                    start  = Offset(0f, 0f),
                                    end    = Offset(size.width, size.height),
                                )
                            )
                        }
                    } else {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(color = Color(0xFFFFC9A4))
                            drawPath(
                                path = Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(size.width, 0f)
                                    lineTo(0f, size.height)
                                    close()
                                },
                                color = Color(0xFFFF7618),
                            )
                        }
                    }
                }
                ReadingCardType.QUOTE -> {
                    val bgColor  = if (version == 1) BookiiBookiiTheme.colors.uiMainPale else Color(0xFFFF7618)
                    val txtColor = if (version == 1) BookiiBookiiTheme.colors.uiMain else Color.White
                    Box(
                        modifier         = Modifier.fillMaxSize().background(bgColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "T", color = txtColor, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardDetailHeader(
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    showMenu: Boolean = false,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onShareButtonPositioned: (androidx.compose.ui.geometry.Offset) -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth().background(BookiiBookiiTheme.colors.white)) {
        Box(modifier = Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 16.dp)) {
            BookiiBackButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart))
            Text(
                text = "독서카드",
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.align(Alignment.Center),
            )
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier
                        .size(40.dp)
                        .onGloballyPositioned { coords ->
                            onShareButtonPositioned(coords.positionInRoot())
                        },
                ) {
                    Icon(painter = painterResource(R.drawable.ic_share), contentDescription = "공유", tint = BookiiBookiiTheme.colors.grey900, modifier = Modifier.size(32.dp))
                }
                if (showMenu) {
                    CardDetailEditMenu(onEditClick = onEditClick, onDeleteClick = onDeleteClick)
                }
            }
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 1.dp)
    }
}

@Composable
private fun CardDetailEditMenu(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val popupOffsetY = with(LocalDensity.current) { 56.dp.roundToPx() }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(40.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_meetball),
                contentDescription = "더보기",
                tint = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.size(32.dp),
            )
        }
        if (expanded) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(x = 0, y = popupOffsetY),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true),
            ) {
                CardDetailMenuPopover(
                    onEditClick = {
                        expanded = false
                        onEditClick()
                    },
                    onDeleteClick = {
                        expanded = false
                        onDeleteClick()
                    },
                )
            }
        }
    }
}

@Composable
private fun CardDetailMenuPopover(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(160.dp)
            .shadow(elevation = 6.dp, shape = BookiiBookiiTheme.shape.round10)
            .background(color = BookiiBookiiTheme.colors.white, shape = BookiiBookiiTheme.shape.round10)
            .border(width = 1.dp, color = BookiiBookiiTheme.colors.grey200, shape = BookiiBookiiTheme.shape.round10)
            .padding(vertical = 4.dp),
    ) {
        CardDetailMenuItem(text = "수정하기", iconRes = R.drawable.ic_edit, onClick = onEditClick)
        HorizontalDivider(thickness = 1.dp, color = BookiiBookiiTheme.colors.grey100)
        CardDetailMenuItem(text = "삭제하기", iconRes = R.drawable.ic_trash, onClick = onDeleteClick)
    }
}

@Composable
private fun CardDetailMenuItem(text: String, iconRes: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = text, style = BookiiBookiiTheme.typography.medium14, color = BookiiBookiiTheme.colors.grey700)
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = BookiiBookiiTheme.colors.grey700,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun CardInfoArea(
    card: ReadingCard?,
    sortByLatest: Boolean,
    progress: Float,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onBookmarkPositioned: (androidx.compose.ui.geometry.Offset) -> Unit = {},
) {
    val onProfileClick = LocalOnProfileClick.current
    val pageText = card?.page?.let { if (it.isNotBlank() && it != "0") "p.$it" else "" } ?: ""

    Column(
        modifier              = Modifier.fillMaxWidth().background(BookiiBookiiTheme.colors.white).padding(16.dp),
        verticalArrangement   = Arrangement.spacedBy(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(30.dp)).background(BookiiBookiiTheme.colors.grey200)) {
                Box(modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(BookiiBookiiTheme.colors.uiMain))
            }
            Text(text = "| ${if (sortByLatest) "최신순" else "과거순"}", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProfilePlaceholder(
                        imageUrl = card?.creatorProfileImageUrl,
                        modifier = Modifier.size(32.dp),
                        onClick = card?.username?.let { nick -> { onProfileClick(nick) } },
                    )
                    Text(text = card?.username ?: "", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey800)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isBookmarked) BookiiBookiiTheme.colors.uiMainPale else Color.Transparent)
                        .border(0.5.dp, if (isBookmarked) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey300, CircleShape)
                        .clickable { onBookmarkToggle() }
                        .onGloballyPositioned { coords ->
                            onBookmarkPositioned(coords.positionInRoot())
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter            = painterResource(if (isBookmarked) R.drawable.ic_bookmark_fill else R.drawable.ic_bookmark),
                        contentDescription = "북마크",
                        tint               = if (isBookmarked) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey500,
                        modifier           = Modifier.size(20.dp),
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier              = Modifier.weight(1f),
                ) {
                    Text(text = (card?.bookTitle).stripBookSubtitle(), style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey800, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (pageText.isNotBlank()) {
                        Text(text = pageText, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey500)
                    }
                }
                Text(text = card?.date ?: "", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
            }
        }
    }
}

@Composable
private fun ReactionBar(
    activeReactions: List<Reaction>,
    onReact: (Reaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        reactionList.forEach { reaction ->
            val isSelected = activeReactions.any { it.label == reaction.label }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier            = Modifier.clickable { onReact(reaction) },
            ) {
                Box(
                    modifier = Modifier
                        .size(63.dp)
                        .clip(CircleShape)
                        .background(BookiiBookiiTheme.colors.white)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey200,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painter = painterResource(reaction.iconRes), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(40.dp))
                }
                Text(text = reaction.label, style = BookiiBookiiTheme.typography.medium12, color = BookiiBookiiTheme.colors.grey800)
            }
        }
    }
}

private val previewCards = listOf(
    ReadingCard(
        cardId = 1L,
        username = "북이",
        content = "다시 읽어도 마음에 오래 남는 문장이었다.",
        page = "123",
        type = ReadingCardType.QUOTE,
        date = "2026.06.05",
        bookTitle = "데미안",
        quotation = "새는 알에서 나오려고 투쟁한다.",
        isBookmarked = true,
        myReactions = listOf("LIKE"),
    ),
    ReadingCard(
        cardId = 2L,
        username = "부키",
        content = "이 장면이 특히 인상 깊었어요.",
        page = "45",
        type = ReadingCardType.PHOTO,
        date = "2026.06.04",
        bookTitle = "어린 왕자",
    ),
)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ReadingCardDetailScreenQuotePreview() {
    BookiiPreview {
        ReadingCardDetailScreen(cards = previewCards, initialIndex = 0)
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ReadingCardDetailScreenPhotoPreview() {
    BookiiPreview {
        ReadingCardDetailScreen(cards = previewCards, initialIndex = 1)
    }
}
