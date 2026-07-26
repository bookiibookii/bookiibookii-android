package com.bookiibookii.bookiibookii.onboarding.steps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.bookiibookii.bookiibookii.common.showCustomToast
import androidx.activity.compose.setContent
import androidx.core.content.FileProvider
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import com.bookiibookii.bookiibookii.onboarding.steps.model.OnboardingSubmitState
import com.bookiibookii.bookiibookii.onboarding.steps.ui.OnbStepScreen
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import java.io.File

class OnbStepActivity : AppCompatActivity() {

    private val vm: OnbViewModel by viewModels()

    // ── 프로필 이미지: 카메라 ───────────────────────────────────────────────────
    private var cameraImageUri: Uri? = null

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchCamera()
            else showCustomToast("카메라 권한이 필요합니다. 설정에서 허용해주세요.", false)
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
                    showCustomToast("이미지를 불러오지 못했습니다.", false)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm.onboardingSubmitState.observe(this) { state ->
            // 성공 시에만 화면 전환. 실패(Error)는 OnbStepScreen이 토스트로 사유를 노출.
            if (state is OnboardingSubmitState.Success) {
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
        if (uri == null) {
            showCustomToast("카메라를 실행할 수 없습니다. 잠시 후 다시 시도해주세요.", false)
            return
        }
        cameraImageUri = uri
        // FileProvider URI에 카메라 앱이 결과를 쓸 수 있도록 임시 쓰기 권한 부여
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        packageManager
            .queryIntentActivities(captureIntent, PackageManager.MATCH_DEFAULT_ONLY)
            .forEach { info ->
                grantUriPermission(
                    info.activityInfo.packageName,
                    uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
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

    // 카메라 출력 = 앱 내부 캐시(cache/camera) 파일의 FileProvider URI.
    // MediaStore(공용 갤러리)에 넣지 않으므로 촬영본이 갤러리에 남지 않는다.
    private fun createCameraImageUri(): Uri? {
        return try {
            val cameraDir = File(cacheDir, "camera").apply { mkdirs() }
            val file = File(cameraDir, "profile_${System.currentTimeMillis()}.jpg")
            FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        } catch (e: Exception) {
            null
        }
    }
}
