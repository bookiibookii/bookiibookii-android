package com.bookiibookii.bookiibookii.mypage.feat.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.mypage.BaseMypageFragment
import com.bookiibookii.bookiibookii.mypage.ui.main.ProfileSettingScreen
import com.bookiibookii.bookiibookii.mypage.vm.MypageViewModel
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.launch
import java.io.File

class ProfileSettingFragment : BaseMypageFragment() {

    private val viewModel: MypageViewModel by activityViewModels()

    // Fragment 레벨 LiveData — Compose에서 observeAsState()로 관찰
    private val selectedImageUri = MutableLiveData<Uri?>(null)
    private var selectedImageFile: File? = null
    private var cameraImageUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraImageUri?.let { uri ->
                selectedImageUri.value = uri
                selectedImageFile = createUploadTempFile(uri)
            }
        }
    }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchCamera()
        }

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri.value = it
            selectedImageFile = createUploadTempFile(it)
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
                val profile by viewModel.profileData.observeAsState()
                val nicknameCheckState by viewModel.nicknameCheckState.observeAsState()
                val imageUri by selectedImageUri.observeAsState()

                ProfileSettingScreen(
                    profile = profile,
                    profileImageUri = imageUri,
                    nicknameCheckState = nicknameCheckState,
                    onBackClick = { parentFragmentManager.popBackStack() },
                    onOpenCamera = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onOpenGallery = {
                        pickMediaLauncher.launch("image/*")
                    },
                    onCheckNickname = { nickname -> viewModel.checkNickname(nickname) },
                    onSaveClick = { request ->
                        viewModel.updateProfile(request, selectedImageFile)
                    },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.resetNicknameCheckState()
        collectEvents()
    }

    private fun collectEvents() {
        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is MypageViewModel.Event.NavigateBack -> parentFragmentManager.popBackStack()
                    is MypageViewModel.Event.ShowToast -> requireContext().showCustomToast(event.message, !event.message.contains("실패") && !event.message.contains("오류"))
                    else -> {}
                }
            }
        }
    }

    private fun launchCamera() {
        val uri = createCameraUri()
        if (uri == null) {
            requireContext().showCustomToast("카메라를 실행할 수 없습니다. 잠시 후 다시 시도해주세요.", false)
            return
        }
        cameraImageUri = uri
        // FileProvider URI에 카메라 앱이 결과를 쓸 수 있도록 임시 쓰기 권한 부여
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        requireContext().packageManager
            .queryIntentActivities(captureIntent, PackageManager.MATCH_DEFAULT_ONLY)
            .forEach { info ->
                requireContext().grantUriPermission(
                    info.activityInfo.packageName,
                    uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
        takePictureLauncher.launch(uri)
    }

    // 카메라 출력 = 앱 내부 캐시(cache/camera) 파일의 FileProvider URI.
    // MediaStore(공용 갤러리)에 넣지 않으므로 촬영본이 갤러리에 남지 않는다.
    private fun createCameraUri(): Uri? {
        return try {
            val cameraDir = File(requireContext().cacheDir, "camera").apply { mkdirs() }
            val file = File(cameraDir, "profile_${System.currentTimeMillis()}.jpg")
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file,
            )
        } catch (e: Exception) {
            null
        }
    }

    // 원본 이미지를 임시 파일로 복사 (EXIF 보존).
    // 리사이즈/압축/EXIF 회전 적용은 업로드 시 S3Uploader가 처리
    private fun createUploadTempFile(uri: Uri): File? {
        return try {
            val tempFile = File.createTempFile("profile_", ".jpg", requireContext().cacheDir)
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            tempFile
        } catch (e: Exception) {
            null
        }
    }
}
