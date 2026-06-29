package com.bookiibookii.bookiibookii.mypage.feat

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.bookiibookii.bookiibookii.data.model.mypage.PublicProfileResponseDTO
import com.bookiibookii.bookiibookii.data.model.mypage.UserBookDto
import com.bookiibookii.bookiibookii.mypage.ui.main.ProfileShareCardContent
import com.bookiibookii.bookiibookii.onboarding.Intro.LoginIntroActivity
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PublicProfileViewerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shareToken = intent?.data?.lastPathSegment

        setContent {
            BookiiBookiiTheme {
                var state by remember { mutableStateOf<ViewerState>(ViewerState.Loading) }

                LaunchedEffect(shareToken) {
                    state = loadProfile(shareToken)
                }

                when (val s = state) {
                    ViewerState.Loading -> CenterBox {
                        CircularProgressIndicator(color = BookiiBookiiTheme.colors.uiMain)
                    }
                    ViewerState.Error -> CenterBox {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "프로필을 불러올 수 없어요",
                                style = BookiiBookiiTheme.typography.medium16,
                                color = BookiiBookiiTheme.colors.grey900,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "링크가 만료되었거나 존재하지 않는 프로필이에요",
                                style = BookiiBookiiTheme.typography.regular14,
                                color = BookiiBookiiTheme.colors.grey500,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    is ViewerState.Success -> ProfileViewerContent(
                        profile = s.profile,
                        onOpenApp = { openApp() },
                    )
                }
            }
        }
    }

    private suspend fun loadProfile(shareToken: String?): ViewerState {
        if (shareToken.isNullOrBlank()) return ViewerState.Error
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.libApi().getPublicProfile(shareToken)
                val body = response.body()
                val dto = body?.result
                if (response.isSuccessful && body?.isSuccess == true && dto != null) {
                    ViewerState.Success(dto)
                } else {
                    ViewerState.Error
                }
            } catch (_: Exception) {
                ViewerState.Error
            }
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
        data class Success(val profile: PublicProfileResponseDTO) : ViewerState
    }
}

@Composable
private fun ProfileViewerContent(
    profile: PublicProfileResponseDTO,
    onOpenApp: () -> Unit,
) {
    val books = profile.representativeBooks.map {
        UserBookDto(title = it.title, auth = it.author ?: "", image = it.image)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        ProfileShareCardContent(
            name = profile.nickname,
            motto = profile.introduction ?: "",
            imageUrl = profile.profileImageUrl,
            representativeBooks = books,
            isDark = false,
        )

        Button(
            onClick = onOpenApp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BookiiBookiiTheme.colors.uiMain,
            ),
        ) {
            Text(
                text = "부키부키 앱 열기",
                style = BookiiBookiiTheme.typography.semibold16,
                color = BookiiBookiiTheme.colors.white,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
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
