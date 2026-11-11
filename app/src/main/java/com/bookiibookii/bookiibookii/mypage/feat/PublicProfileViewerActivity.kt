package com.bookiibookii.bookiibookii.mypage.feat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.mypage.PublicProfileResponseDTO
import com.bookiibookii.bookiibookii.data.model.mypage.UserBookDto
import com.bookiibookii.bookiibookii.library.ui.PublicViewerScaffold
import com.bookiibookii.bookiibookii.mypage.nav.captureProfileCardBitmap
import com.bookiibookii.bookiibookii.mypage.nav.compositeProfileCardFullBleed
import com.bookiibookii.bookiibookii.mypage.nav.saveProfileBitmapToGallery
import com.bookiibookii.bookiibookii.mypage.ui.main.ProfileShareCardContent
import com.bookiibookii.bookiibookii.ui.component.showCustomToast
import com.bookiibookii.bookiibookii.onboarding.Intro.LoginIntroActivity
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PublicProfileViewerActivity : ComponentActivity() {

    private var pendingSave: Pair<PublicProfileResponseDTO, Boolean>? = null
    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val pending = pendingSave
            pendingSave = null
            if (granted && pending != null) saveProfileToGallery(pending.first, pending.second)
            else showCustomToast("저장 권한이 필요해요", false)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shareToken = intent?.data?.lastPathSegment
        // 다크 모드로 공유한 링크는 ?dark=1 이 붙는다 (MypageNavHost.fetchProfileShareUrl)
        val isDark = intent?.data?.getQueryParameter("dark").let { it == "1" || it.equals("true", ignoreCase = true) }

        setContent {
            BookiiBookiiTheme {
                var state by remember { mutableStateOf<ViewerState>(ViewerState.Loading) }

                LaunchedEffect(shareToken) {
                    state = loadProfile(shareToken)
                }

                when (val s = state) {
                    ViewerState.Loading -> CenterBox(isDark) {
                        CircularProgressIndicator(color = BookiiBookiiTheme.colors.uiMain)
                    }
                    ViewerState.Error -> CenterBox(isDark) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "프로필을 불러올 수 없어요",
                                style = BookiiBookiiTheme.typography.medium16,
                                color = if (isDark) BookiiBookiiTheme.colors.white else BookiiBookiiTheme.colors.grey900,
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
                        isDark = isDark,
                        onOpenApp = { openApp() },
                        onSaveImage = { saveImage(s.profile, isDark) },
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

    private fun saveImage(profile: PublicProfileResponseDTO, isDark: Boolean) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingSave = profile to isDark
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        saveProfileToGallery(profile, isDark)
    }

    // 마이페이지 프로필 공유의 "다운로드"와 같은 이미지(1080x1920 배경 위 프로필 카드)로 저장
    private fun saveProfileToGallery(profile: PublicProfileResponseDTO, isDark: Boolean) {
        captureProfileCardBitmap(
            context = this,
            name = profile.nickname,
            motto = profile.introduction ?: "",
            imageUrl = profile.profileImageUrl,
            representativeBooks = profile.toUserBooks(),
            isDark = isDark,
        ) { bitmap ->
            if (bitmap == null) {
                showCustomToast("저장 중 오류가 발생했어요", false)
                return@captureProfileCardBitmap
            }
            lifecycleScope.launch(Dispatchers.IO) {
                val finalBitmap = compositeProfileCardFullBleed(bitmap, isDark)
                bitmap.recycle()
                val saved = saveProfileBitmapToGallery(this@PublicProfileViewerActivity, finalBitmap)
                finalBitmap.recycle()
                withContext(Dispatchers.Main) {
                    if (isDestroyed) return@withContext
                    showCustomToast(if (saved) "사진을 저장했어요" else "사진 저장에 실패했어요", saved)
                }
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
    isDark: Boolean,
    onOpenApp: () -> Unit,
    onSaveImage: () -> Unit,
) {
    // 독서카드 공개 화면과 같은 틀(로고 헤더 + 하단 "메인으로 / 이미지 저장")
    PublicViewerScaffold(
        onGoMain = onOpenApp,
        onSaveImage = onSaveImage,
        isDark = isDark,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 20.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            ProfileShareCardContent(
                name = profile.nickname,
                motto = profile.introduction ?: "",
                imageUrl = profile.profileImageUrl,
                representativeBooks = profile.toUserBooks(),
                isDark = isDark,
            )
        }
    }
}

private fun PublicProfileResponseDTO.toUserBooks(): List<UserBookDto> =
    representativeBooks.map { UserBookDto(title = it.title, auth = it.author ?: "", image = it.image) }

@Composable
private fun CenterBox(isDark: Boolean, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackground(isDark))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}

// 프로필 카드(ProfileShareCardContent)의 배경색과 같은 색으로 화면 전체를 채운다
@Composable
private fun screenBackground(isDark: Boolean) =
    if (isDark) BookiiBookiiTheme.colors.grey900 else BookiiBookiiTheme.colors.white
