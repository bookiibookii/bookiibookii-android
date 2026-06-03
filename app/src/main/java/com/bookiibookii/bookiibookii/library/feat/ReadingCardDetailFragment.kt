package com.bookiibookii.bookiibookii.library.feat

import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.library.MemberCardReactionToggleRequestDTO
import com.bookiibookii.bookiibookii.library.BaseLibraryFragment
import com.bookiibookii.bookiibookii.library.ui.ReadingCard
import com.bookiibookii.bookiibookii.library.ui.ReadingCardDetailScreen
import com.bookiibookii.bookiibookii.library.ui.ShareableCard
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ReadingCardDetailFragment : BaseLibraryFragment() {

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
                    onInstaShare = { card -> shareCardToInstagram(card) },
                )
            }
        }
    }

    // ── 인스타그램 스토리 공유 ───────────────────────────────────────────────

    private fun shareCardToInstagram(card: ReadingCard) {
        val context = requireContext()

        // 카드 크기: 화면 너비의 80%, 비율 348:464
        val cardWidthPx  = (resources.displayMetrics.widthPixels * 0.8f).toInt()
        val cardHeightPx = (cardWidthPx * 464f / 348f).toInt()

        // 오프스크린 ComposeView로 카드 렌더링
        val cardView = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            visibility = View.INVISIBLE
            setContent {
                BookiiBookiiTheme {
                    ShareableCard(card = card)
                }
            }
        }

        val container = (requireView().parent as? ViewGroup) ?: return
        container.addView(cardView, ViewGroup.LayoutParams(cardWidthPx, cardHeightPx))

        cardView.postDelayed({
            if (!isAdded) {
                if (cardView.isAttachedToWindow) container.removeView(cardView)
                return@postDelayed
            }
            try {
                val w = cardView.width
                val h = cardView.height
                if (w <= 0 || h <= 0) {
                    container.removeView(cardView)
                    return@postDelayed
                }

                val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                cardView.draw(canvas)
                container.removeView(cardView)

                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
                        imagesDir.listFiles()?.forEach { it.delete() }

                        // 카드 스티커 저장
                        val stickerFile = File(imagesDir, "card_sticker_${System.currentTimeMillis()}.png")
                        FileOutputStream(stickerFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        bitmap.recycle()

                        val stickerUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            stickerFile,
                        )

                        // 배경 이미지 (주황 그라데이션 1080×1920)
                        val bgBitmap = createGradientBackground()
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
            } catch (e: Exception) {
                if (cardView.isAttachedToWindow) container.removeView(cardView)
                if (isAdded) context.showCustomToast("공유 준비 중 오류가 발생했습니다.", false)
            }
        }, 500L)
    }

    /** Bookii 브랜드 오렌지 그라데이션 배경 비트맵 (Instagram 스토리 1080×1920) */
    private fun createGradientBackground(): Bitmap {
        val w = 1080
        val h = 1920
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val gradient = LinearGradient(
            0f, h.toFloat(), w.toFloat(), 0f,
            intArrayOf(
                android.graphics.Color.parseColor("#FF4E18"),
                android.graphics.Color.parseColor("#FF7618"),
                android.graphics.Color.parseColor("#FFC9A4"),
            ),
            null,
            Shader.TileMode.CLAMP,
        )
        val paint = Paint().apply { shader = gradient }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        return bitmap
    }

    private fun launchInstagramStoryIntent(stickerUri: Uri, backgroundUri: Uri) {
        val context = requireContext()
        val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
            setPackage("com.instagram.android")
            setDataAndType(backgroundUri, "image/*")  // 배경
            putExtra("interactive_asset_uri", stickerUri)  // 스티커
            putExtra("source_application", context.packageName)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val clipData = ClipData.newRawUri("Sticker", stickerUri).also {
            it.addItem(ClipData.Item(backgroundUri))
        }
        intent.clipData = clipData

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
