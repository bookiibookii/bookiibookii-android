package com.bookiibookii.bookiibookii.library.feat

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.library.ui.PublicCardViewerScreen
import com.bookiibookii.bookiibookii.library.ui.ReadingCard
import com.bookiibookii.bookiibookii.library.vm.toReadingCard
import com.bookiibookii.bookiibookii.onboarding.Intro.LoginIntroActivity
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// 공유 링크(App Links) 진입점.
// https://<공유도메인>/share/reading-card/{shareToken} → shareToken으로 공개 조회 후 뷰어 표시.
class PublicCardViewerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 링크의 마지막 경로 세그먼트가 shareToken. (내부 호출/테스트용 extra도 허용)
        val shareToken = intent?.data?.lastPathSegment
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
                        bookAuthor = s.author,
                        onClose = { finish() },
                        onOpenApp = { openApp() },
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
