package com.bookiibookii.bookiibookii.onboarding.steps

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import com.bookiibookii.bookiibookii.onboarding.steps.model.OnboardingSubmitState
import com.bookiibookii.bookiibookii.onboarding.steps.ui.OnbStepScreen
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.launch
import java.io.File

class OnbStepActivity : AppCompatActivity() {

    private val vm: OnbViewModel by viewModels()

    // ── 프로필 이미지: 카메라 ───────────────────────────────────────────────────
    private var cameraImageUri: Uri? = null

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchCamera()
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val uri = cameraImageUri ?: return@registerForActivityResult
                vm.setProfileUri(uri)
                vm.uploadProfileImage(contentResolver, uri)
            }
        }

    // ── 프로필 이미지: 갤러리 ───────────────────────────────────────────────────
    private val pickProfileImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val localUri = copyUriToLocalCache(uri)
                if (localUri != null) {
                    vm.setProfileUri(localUri)
                    vm.uploadProfileImage(contentResolver, localUri)
                } else {
                    Toast.makeText(this, "이미지를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm.onboardingSubmitState.observe(this) { state ->
            when (state) {
                is OnboardingSubmitState.Success -> {
                    TokenManager.saveOnboardingDone(this, true)
                    vm.state.value?.nickname?.let { TokenManager.saveNickname(this, it) }
                    startActivity(
                        Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                    finish()
                }
                is OnboardingSubmitState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }

        setContent {
            BookiiBookiiTheme {
                OnbStepScreen(
                    vm = vm,
                    onBack = { finish() },
                    onFinish = { vm.submitOnboarding() },
                    onOpenProfileCamera = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onOpenProfileGallery = {
                        pickProfileImageLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }
        }
    }

    private fun launchCamera() {
        val uri = createCameraImageUri()
        cameraImageUri = uri
        takePictureLauncher.launch(uri)
    }

    // 갤러리 URI를 로컬 캐시에 복사 (클라우드 백업 사진 등 openInputStream이 실패하는 케이스 방어)
    private fun copyUriToLocalCache(sourceUri: Uri): Uri? {
        return try {
            val tempFile = File(cacheDir, "profile_${System.currentTimeMillis()}.jpg")
            contentResolver.openInputStream(sourceUri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (tempFile.length() > 0) Uri.fromFile(tempFile) else null
        } catch (e: Exception) {
            // TODO: 추후 로그 삭제 (갤러리 URI 복사 실패 확인용)
            Log.e("OnbStepActivity", "갤러리 URI 로컬 복사 실패", e)
            null
        }
    }

    private fun createCameraImageUri(): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "profile_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
    }
}
