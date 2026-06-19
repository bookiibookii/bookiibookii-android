package com.bookiibookii.bookiibookii.library.feat

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.library.MemberCardReactionToggleRequestDTO
import com.bookiibookii.bookiibookii.library.BaseLibraryFragment
import com.bookiibookii.bookiibookii.library.ui.ReadingCard
import com.bookiibookii.bookiibookii.library.ui.ReadingCardDetailScreen
import com.bookiibookii.bookiibookii.library.ui.ShareableCard
import com.bookiibookii.bookiibookii.common.stripBookSubtitle
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ReadingCardDetailFragment : BaseLibraryFragment() {

    // 다운로드 권한(API 28 이하) 승인 후 저장할 카드 보관
    private var pendingDownloadCard: ReadingCard? = null
    private var pendingDownloadVersion: Int = 2
    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val card = pendingDownloadCard
            val version = pendingDownloadVersion
            pendingDownloadCard = null
            if (granted && card != null) {
                saveCardToGallery(card, version)
            } else {
                requireContext().showCustomToast("저장 권한이 필요해요", false)
            }
        }

    private val initialIndex get() = arguments?.getInt(ARG_INDEX, 0) ?: 0
    private val sortByLatest get() = arguments?.getBoolean(ARG_SORT, true) ?: true
    private val cards: List<ReadingCard>
        get() {
            val json = arguments?.getString(ARG_CARDS_JSON) ?: return emptyList()
            return try {
                Gson().fromJson(json, object : TypeToken<List<ReadingCard>>() {}.type) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                ReadingCardDetailScreen(
                    cards        = cards,
                    initialIndex = initialIndex,
                    sortByLatest = sortByLatest,
                    onBackClick  = { parentFragmentManager.popBackStack() },
                    onBookmarkToggle = { cardId ->
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            try { RetrofitClient.libApi().toggleBookmark(cardId) } catch (_: Exception) { }
                        }
                    },
                    onReactionToggle = { cardId, reactionLabel ->
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                RetrofitClient.libApi().toggleReaction(
                                    cardId,
                                    MemberCardReactionToggleRequestDTO(reaction = reactionLabel)
                                )
                            } catch (_: Exception) { }
                        }
                    },
                    onInstaShare = { card, version -> shareCardToInstagram(card, version) },
                    onCopyLink   = { card -> copyShareLink(card) },
                    onKakaoShare = { card -> shareToKakao(card) },
                    onXShare     = { card -> shareToX(card) },
                    onDownload   = { card, version -> downloadCard(card, version) },
                    onEditClick  = { card ->
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, LibraryAddCardFragment.newInstanceEdit(card))
                            .addToBackStack(null)
                            .commit()
                    },
                    onDeleteConfirmed = { card -> deleteCard(card) },
                )
            }
        }
    }

    // ── 카드 삭제 ──────────────────────────────────────────────────────────────
    // DELETE .../cards/{cardId} → 성공 시 토스트 + 목록으로 복귀(목록은 onResume에서 갱신)

    private fun deleteCard(card: ReadingCard) {
        val context = requireContext()
        viewLifecycleOwner.lifecycleScope.launch {
            val success = try {
                val resp = withContext(Dispatchers.IO) { RetrofitClient.libApi().deleteCard(card.cardId) }
                resp.isSuccessful && resp.body()?.isSuccess == true
            } catch (_: Exception) {
                false
            }
            if (!isAdded) return@launch
            if (success) {
                context.showCustomToast("독서카드를 삭제했어요", true)
                parentFragmentManager.popBackStack()
            } else {
                context.showCustomToast("삭제에 실패했어요", false)
            }
        }
    }

    // ── 공유 토큰 발급 (링크 복사 / 카카오 / X 공통) ──────────────────────────
    // POST .../share-token 호출 → 응답 shareUrl을 Main 스레드 콜백으로 전달 (실패 시 null)

    private fun fetchShareUrl(card: ReadingCard, onResult: (String?) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val shareUrl = try {
                RetrofitClient.libApi().createShareToken(card.cardId).body()?.result?.shareUrl
            } catch (_: Exception) {
                null
            }
            withContext(Dispatchers.Main) {
                if (!isAdded) return@withContext
                onResult(shareUrl)
            }
        }
    }

    // ── 링크 복사 ────────────────────────────────────────────────────────────

    private fun copyShareLink(card: ReadingCard) {
        val context = requireContext()
        fetchShareUrl(card) { shareUrl ->
            if (shareUrl.isNullOrBlank()) {
                context.showCustomToast("링크 복사에 실패했어요", false)
            } else {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("독서카드 링크", shareUrl))
                context.showCustomToast("링크를 복사했어요", true)
            }
        }
    }

    // ── 카카오톡 공유 (피드 템플릿) ───────────────────────────────────────────
    // 카카오톡 설치 시 ShareClient, 미설치 시 WebSharerClient(웹) 폴백

    private fun shareToKakao(card: ReadingCard) {
        val context = requireContext()
        fetchShareUrl(card) { shareUrl ->
            if (shareUrl.isNullOrBlank()) {
                context.showCustomToast("공유에 실패했어요", false)
                return@fetchShareUrl
            }
            // 공유 URL의 마지막 경로 세그먼트가 shareToken. 앱 설치 시 카카오가 웹 대신 앱을 직접 실행하도록
            // executionParams로 전달 (미설치 시 mobileWebUrl로 폴백). PublicCardViewerActivity가 kakaolink 스킴으로 수신.
            val shareToken = Uri.parse(shareUrl).lastPathSegment.orEmpty()
            val link = Link(
                webUrl = shareUrl,
                mobileWebUrl = shareUrl,
                androidExecutionParams = mapOf("shareToken" to shareToken),
                iosExecutionParams = mapOf("shareToken" to shareToken),
            )
            val feed = FeedTemplate(
                content = Content(
                    title = card.bookTitle.ifBlank { "독서카드" },
                    description = card.quotation.ifBlank { card.content },
                    imageUrl = card.imageUrl.orEmpty(),
                    link = link,
                ),
                buttons = listOf(Button("보러가기", link)),
            )

            if (ShareClient.instance.isKakaoTalkSharingAvailable(context)) {
                ShareClient.instance.shareDefault(context, feed) { result, error ->
                    when {
                        error != null -> context.showCustomToast("공유에 실패했어요", false)
                        result != null -> startActivity(result.intent)
                    }
                }
            } else {
                // 카카오톡 미설치 → 웹 공유 폴백
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, WebSharerClient.instance.makeDefaultUrl(feed)))
                } catch (_: Exception) {
                    context.showCustomToast("카카오톡을 열 수 없어요", false)
                }
            }
        }
    }

    // ── X 공유 (작성화면 인텐트) ──────────────────────────────────────────────

    private fun shareToX(card: ReadingCard) {
        val context = requireContext()
        fetchShareUrl(card) { shareUrl ->
            if (shareUrl.isNullOrBlank()) {
                context.showCustomToast("공유에 실패했어요", false)
                return@fetchShareUrl
            }
            val text = card.bookTitle.stripBookSubtitle().ifBlank { "독서카드" }
            val intentUrl = "https://twitter.com/intent/tweet?text=" +
                Uri.encode(text) + "&url=" + Uri.encode(shareUrl)
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(intentUrl)))
            } catch (_: Exception) {
                context.showCustomToast("X를 열 수 없어요", false)
            }
        }
    }

    // ── 카드 → 비트맵 캡처 (인스타 공유 / 다운로드 공통) ──────────────────────
    // 오프스크린 ComposeView로 ShareableCard를 렌더해 비트맵 생성. 결과는 Main 콜백(실패 시 null)

    private fun captureShareableCard(card: ReadingCard, cardVersion: Int, onBitmap: (Bitmap?) -> Unit) {
        val context = requireContext()
        
        val cardWidthPx  = (resources.displayMetrics.widthPixels * 0.85f).toInt()
        val cardHeightPx = (cardWidthPx * 520f / 320f).toInt()

        val cardView = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            visibility = View.INVISIBLE
            setContent {
                BookiiBookiiTheme {
                    ShareableCard(card = card, cardVersion = cardVersion)
                }
            }
        }

        // FragmentContainerView에는 Fragment 미연결 View를 못 붙이므로 액티비티 content 루트에 부착
        val container = requireActivity().findViewById<ViewGroup>(android.R.id.content) ?: run {
            onBitmap(null)
            return
        }
        container.addView(cardView, ViewGroup.LayoutParams(cardWidthPx, cardHeightPx))

        // 레이아웃/컴포지션 완료 대기 후 캡처
        cardView.postDelayed({
            val bitmap = try {
                val w = cardView.width
                val h = cardView.height
                if (!isAdded || w <= 0 || h <= 0) {
                    null
                } else {
                    Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { bmp ->
                        cardView.draw(Canvas(bmp))
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

    // ── 인스타그램 스토리 공유 ───────────────────────────────────────────────

    private fun shareCardToInstagram(card: ReadingCard, cardVersion: Int) {
        val context = requireContext()
        captureShareableCard(card, cardVersion) { bitmap ->
            if (bitmap == null) {
                context.showCustomToast("공유 준비 중 오류가 발생했습니다.", false)
                return@captureShareableCard
            }
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
                    imagesDir.listFiles()?.forEach { it.delete() }

                    // 카드 스티커 저장
                    val stickerFile = File(imagesDir, "card_sticker_${System.currentTimeMillis()}.png")
                    FileOutputStream(stickerFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

                    val stickerUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        stickerFile,
                    )

                    // 배경: 카드 위/아래 가장자리색 세로 그라데이션 (인스타 기본 배경 방식)
                    val bgBitmap = createEdgeGradientBackground(bitmap)
                    bitmap.recycle()
                    val bgFile = File(imagesDir, "bg_${System.currentTimeMillis()}.png")
                    FileOutputStream(bgFile).use { bgBitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    bgBitmap.recycle()

                    val backgroundUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        bgFile,
                    )

                    withContext(Dispatchers.Main) {
                        launchInstagramStoryIntent(stickerUri, backgroundUri)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (isAdded) context.showCustomToast("공유 준비 중 오류가 발생했습니다.", false)
                    }
                }
            }
        }
    }

    // ── 다운로드 (갤러리 저장) ────────────────────────────────────────────────
    // API 28 이하는 WRITE_EXTERNAL_STORAGE 런타임 권한 필요, Q+는 MediaStore로 권한 불필요

    private fun downloadCard(card: ReadingCard, cardVersion: Int) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingDownloadCard = card
            pendingDownloadVersion = cardVersion
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        saveCardToGallery(card, cardVersion)
    }

    private fun saveCardToGallery(card: ReadingCard, cardVersion: Int) {
        val context = requireContext()
        captureShareableCard(card, cardVersion) { bitmap ->
            if (bitmap == null) {
                context.showCustomToast("저장 중 오류가 발생했어요", false)
                return@captureShareableCard
            }
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val saved = saveBitmapToGallery(context, bitmap)
                bitmap.recycle()
                withContext(Dispatchers.Main) {
                    if (!isAdded) return@withContext
                    if (saved) {
                        context.showCustomToast("사진을 저장했어요", true)
                    } else {
                        context.showCustomToast("사진 저장에 실패했어요", false)
                    }
                }
            }
        }
    }

    // 비트맵을 갤러리(Pictures/부키부키)에 저장. 성공 여부 반환
    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Boolean {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "bookii_card_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/부키부키")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        return try {
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            } ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            true
        } catch (_: Exception) {
            resolver.delete(uri, null, null)
            false
        }
    }

    /** 카드 위/아래 가장자리색으로 세로 그라데이션을 채운 스토리 배경 (인스타 기본 배경 방식) */
    private fun createEdgeGradientBackground(card: Bitmap): Bitmap {
        // 카드를 1×N으로 축소 → 각 픽셀이 가로줄 평균색. 맨 위/맨 아래 픽셀 = 위/아래 가장자리 색
        val samples = 10
        val column = Bitmap.createScaledBitmap(card, 1, samples, true)
        val topColor = column.getPixel(0, 0)
        val bottomColor = column.getPixel(0, samples - 1)
        column.recycle()

        val w = 1080
        val h = 1920
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                topColor, bottomColor,
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        return bitmap
    }

    private fun launchInstagramStoryIntent(stickerUri: Uri, backgroundUri: Uri) {
        val context = requireContext()
        val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
            setPackage("com.instagram.android")
            setDataAndType(backgroundUri, "image/*")        // 카드 색감 블러 배경
            putExtra("interactive_asset_uri", stickerUri)   // 선명한 카드 스티커
            putExtra("source_application", context.packageName)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        intent.clipData = ClipData.newRawUri("Sticker", stickerUri).also {
            it.addItem(ClipData.Item(backgroundUri))
        }

        // 인스타그램에 URI 읽기 권한 부여
        val resInfoList = context.packageManager.queryIntentActivities(intent, 0)
        for (info in resInfoList) {
            val pkg = info.activityInfo.packageName
            context.grantUriPermission(pkg, stickerUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.grantUriPermission(pkg, backgroundUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(intent)
        } catch (_: Exception) {
            context.showCustomToast("인스타그램 앱을 찾을 수 없습니다.", false)
        }
    }

    companion object {
        private const val ARG_INDEX      = "arg_index"
        private const val ARG_SORT       = "arg_sort"
        private const val ARG_CARDS_JSON = "arg_cards_json"

        fun newInstance(
            initialIndex: Int,
            sortByLatest: Boolean,
            cards: List<ReadingCard>,
        ) = ReadingCardDetailFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_INDEX, initialIndex)
                putBoolean(ARG_SORT, sortByLatest)
                putString(ARG_CARDS_JSON, Gson().toJson(cards))
            }
        }
    }
}
