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

// 공유 링크(App Links) 진입점.
// https://<공유도메인>/share/reading-card/{shareToken} → shareToken으로 공개 조회 후 뷰어 표시.
class PublicCardViewerActivity : ComponentActivity() {

    // 갤러리 저장 — API 28 이하 WRITE_EXTERNAL_STORAGE 권한 결과 대기용
    private var pendingSaveCard: ReadingCard? = null
    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val card = pendingSaveCard
            pendingSaveCard = null
            if (granted && card != null) saveCardToGallery(card)
            else showCustomToast("저장 권한이 필요해요", false)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // App Links(https): 마지막 경로 세그먼트가 shareToken.
        // 카카오 공유(kakao{key}://kakaolink): executionParams가 쿼리(?shareToken=)로 전달됨.
        val shareToken = intent?.data?.getQueryParameter("shareToken")
            ?: intent?.data?.lastPathSegment
            ?: intent?.getStringExtra(EXTRA_SHARE_TOKEN)

        // 공개 조회 — 로그인 없이 누구나 열람 가능. 앱 진입은 "부키부키 앱으로 이동하기" 버튼으로.
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
                        onGoMain = { openApp() },
                        onSaveImage = { saveImage(s.card) },
                    )
                }
            }
        }
    }

    // 공개 조회 — libApi(authed)지만 공개 엔드포인트라 토큰 유무와 무관하게 동작
    private suspend fun loadCard(shareToken: String?): ViewerState {
        if (shareToken.isNullOrBlank()) return ViewerState.Error
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.libApi().getPublicReadingCard(shareToken)
                val body = response.body()
                val dto = body?.result
                if (response.isSuccessful && body?.isSuccess == true && dto != null) {
                    ViewerState.Success(dto.toReadingCard(), dto.bookAuthor.orEmpty())
                } else {
                    ViewerState.Error
                }
            } catch (_: Exception) {
                ViewerState.Error
            }
        }
    }

    // ── 이미지 저장 (갤러리) ──────────────────────────────────────────────────
    // API 28 이하는 WRITE_EXTERNAL_STORAGE 런타임 권한 필요, Q+는 MediaStore로 권한 불필요

    private fun saveImage(card: ReadingCard) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingSaveCard = card
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        saveCardToGallery(card)
    }

    private fun saveCardToGallery(card: ReadingCard) {
        captureCard(card) { bitmap ->
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

    // 오프스크린 ComposeView로 ShareableCard를 렌더해 비트맵 생성 (뷰어 카드와 동일한 320:520 비율)
    private fun captureCard(card: ReadingCard, onBitmap: (Bitmap?) -> Unit) {
        val maxWidthPx = (320 * resources.displayMetrics.density).toInt()
        val cardWidthPx = (resources.displayMetrics.widthPixels * 0.82f).toInt().coerceAtMost(maxWidthPx)
        val cardHeightPx = (cardWidthPx * 520f / 320f).toInt()

        val cardView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            visibility = View.INVISIBLE
            setContent {
                BookiiBookiiTheme {
                    ShareableCard(card = card)
                }
            }
        }

        val container = findViewById<ViewGroup>(android.R.id.content) ?: run {
            onBitmap(null)
            return
        }
        container.addView(cardView, ViewGroup.LayoutParams(cardWidthPx, cardHeightPx))

        // 레이아웃/컴포지션(이미지 로드 포함) 완료 대기 후 캡처
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

    // 비트맵을 갤러리(Pictures/부키부키)에 저장. 성공 여부 반환
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

    // "앱에서 보기" — 런처(LoginIntroActivity)로 진입해 정상 라우팅에 맡김
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
        data class Success(val card: ReadingCard, val author: String) : ViewerState
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
