package com.bookiibookii.bookiibookii.library.feat

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.library.ui.PublicCardViewerScreen
import com.bookiibookii.bookiibookii.library.ui.ReadingCard
import com.bookiibookii.bookiibookii.library.ui.ShareableCard
import com.bookiibookii.bookiibookii.library.vm.toReadingCard
import com.bookiibookii.bookiibookii.onboarding.Intro.LoginIntroActivity
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PublicCardViewerActivity : ComponentActivity() {

    private var pendingSaveCard: Pair<ReadingCard, Int>? = null
    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val pending = pendingSaveCard
            pendingSaveCard = null
            if (granted && pending != null) saveCardToGallery(pending.first, pending.second)
            else showCustomToast("저장 권한이 필요해요", false)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shareToken = intent?.data?.getQueryParameter("shareToken")
            ?: intent?.data?.lastPathSegment
            ?: intent?.getStringExtra(EXTRA_SHARE_TOKEN)

        setContent {
            BookiiBookiiTheme {
                var state by remember { mutableStateOf<ViewerState>(ViewerState.Loading) }

                LaunchedEffect(shareToken) {
                    state = loadCard(shareToken)
                }

                when (val s = state) {
                    ViewerState.Loading -> CenterBox { CircularProgressIndicator(color = BookiiBookiiTheme.colors.uiMain) }
                    ViewerState.Error -> CenterBox {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "독서카드를 불러올 수 없어요",
                                style = BookiiBookiiTheme.typography.medium16,
                                color = BookiiBookiiTheme.colors.grey900,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = "링크가 만료되었거나 삭제된 카드일 수 있어요",
                                style = BookiiBookiiTheme.typography.regular14,
                                color = BookiiBookiiTheme.colors.grey500,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    is ViewerState.Success -> PublicCardViewerScreen(
                        card = s.card,
                        cardVersion = s.cardVersion,
                        onGoMain = { openApp() },
                        onSaveImage = { saveImage(s.card, s.cardVersion) },
                    )
                }
            }
        }
    }

    private suspend fun loadCard(shareToken: String?): ViewerState {
        if (shareToken.isNullOrBlank()) return ViewerState.Error
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.libApi().getPublicReadingCard(shareToken)
                val body = response.body()
                val dto = body?.result
                if (response.isSuccessful && body?.isSuccess == true && dto != null) {
                    // PHOTO: OVERLAY=v1, SPLIT=v2 | QUOTE: SPLIT=v1, OVERLAY=v2
                    val cardVersion = if (dto.cardType == "IMAGE") {
                        if (dto.shareLayout == "OVERLAY") 1 else 2
                    } else {
                        if (dto.shareLayout == "SPLIT") 1 else 2
                    }
                    ViewerState.Success(dto.toReadingCard(), dto.bookAuthor.orEmpty(), cardVersion)
                } else {
                    ViewerState.Error
                }
            } catch (_: Exception) {
                ViewerState.Error
            }
        }
    }

    private fun saveImage(card: ReadingCard, cardVersion: Int) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingSaveCard = card to cardVersion
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        saveCardToGallery(card, cardVersion)
    }

    private fun saveCardToGallery(card: ReadingCard, cardVersion: Int) {
        captureCard(card, cardVersion) { bitmap ->
            if (bitmap == null) {
                showCustomToast("저장 중 오류가 발생했어요", false)
                return@captureCard
            }
            lifecycleScope.launch(Dispatchers.IO) {
                val saved = saveBitmapToGallery(bitmap)
                bitmap.recycle()
                withContext(Dispatchers.Main) {
                    if (isDestroyed) return@withContext
                    showCustomToast(if (saved) "사진을 저장했어요" else "사진 저장에 실패했어요", saved)
                }
            }
        }
    }

    private fun captureCard(card: ReadingCard, cardVersion: Int, onBitmap: (Bitmap?) -> Unit) {
        val maxWidthPx = (320 * resources.displayMetrics.density).toInt()
        val cardWidthPx = (resources.displayMetrics.widthPixels * 0.82f).toInt().coerceAtMost(maxWidthPx)
        val cardHeightPx = (cardWidthPx * 520f / 320f).toInt()

        val offscreenImageLoader = coil.ImageLoader.Builder(this).allowHardware(false).build()

        val cardView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            visibility = View.INVISIBLE
            setContent {
                androidx.compose.runtime.CompositionLocalProvider(coil.compose.LocalImageLoader provides offscreenImageLoader) {
                    BookiiBookiiTheme {
                        ShareableCard(card = card, cardVersion = cardVersion)
                    }
                }
            }
        }

        val container = findViewById<ViewGroup>(android.R.id.content) ?: run {
            onBitmap(null)
            return
        }
        container.addView(cardView, ViewGroup.LayoutParams(cardWidthPx, cardHeightPx))

        cardView.postDelayed({
            val bitmap = try {
                val w = cardView.width
                val h = cardView.height
                if (isDestroyed || w <= 0 || h <= 0) {
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

    private fun saveBitmapToGallery(bitmap: Bitmap): Boolean {
        val resolver = contentResolver
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

    private fun openApp() {
        startActivity(
            Intent(this, LoginIntroActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            },
        )
        finish()
    }

    private sealed interface ViewerState {
        data object Loading : ViewerState
        data object Error : ViewerState
        data class Success(val card: ReadingCard, val author: String, val cardVersion: Int) : ViewerState
    }

    companion object {
        const val EXTRA_SHARE_TOKEN = "extra_share_token"
    }
}

@Composable
private fun CenterBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}
