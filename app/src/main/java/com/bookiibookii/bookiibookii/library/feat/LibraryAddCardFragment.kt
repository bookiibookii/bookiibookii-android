package com.bookiibookii.bookiibookii.library.feat

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.library.BaseLibraryFragment
import com.bookiibookii.bookiibookii.library.ui.AddCardMode
import com.bookiibookii.bookiibookii.library.ui.LibraryAddCardScreen
import com.bookiibookii.bookiibookii.library.vm.LibraryAddCardViewModel
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.launch

class LibraryAddCardFragment : BaseLibraryFragment() {

    private val vm: LibraryAddCardViewModel by viewModels()

    private val mode: AddCardMode
        get() = arguments?.getString(ARG_MODE)
            ?.let { AddCardMode.valueOf(it) } ?: AddCardMode.TEXT

    private val memberBookId: Int
        get() = arguments?.getInt(ARG_MEMBER_BOOK_ID, -1) ?: -1

    private var selectedImageUri by mutableStateOf<Uri?>(null)

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedImageUri = it }
    }

    // 카메라가 풀해상도 원본을 기록할 대상 파일 URI (FileProvider)
    private var cameraImageUri: Uri? = null

    // TakePicture: 카메라가 풀해상도 원본을 지정한 파일 URI에 직접 저장한다.
    // (TakePicturePreview는 저해상도 썸네일 비트맵만 반환해 화질이 크게 떨어짐)
    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraImageUri?.let { selectedImageUri = it }
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera()
    }

    private fun launchCamera() {
        val uri = createCameraUri() ?: run {
            requireContext().showCustomToast("카메라를 실행할 수 없습니다.", false)
            return
        }
        cameraImageUri = uri
        takePhotoLauncher.launch(uri)
    }

    // 카메라 출력 = 앱 내부 캐시(cache/camera) 파일의 FileProvider URI
    private fun createCameraUri(): Uri? = try {
        val cameraDir = java.io.File(requireContext().cacheDir, "camera").apply { mkdirs() }
        val file = java.io.File(cameraDir, "card_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file,
        )
    } catch (e: Exception) {
        null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                LibraryAddCardScreen(
                    mode             = mode,
                    selectedImageUri = selectedImageUri,
                    onImagePick      = { pickImageLauncher.launch("image/*") },
                    onImageCapture   = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    onBackClick      = { parentFragmentManager.popBackStack() },
                    onSubmit         = { page, quotation, memo ->
                        if (memberBookId == -1) {
                            requireContext().showCustomToast("책 정보를 찾을 수 없습니다.", false)
                            return@LibraryAddCardScreen
                        }
                        vm.createCard(
                            memberBookId    = memberBookId,
                            mode            = mode,
                            page            = page,
                            quotation       = quotation,
                            memo            = memo,
                            imageUri        = selectedImageUri,
                            contentResolver = requireContext().contentResolver,
                        )
                    },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ViewModel 이벤트 구독 — viewLifecycleOwner 스코프로 누수 방지, 재생성 시 중복 없음
        viewLifecycleOwner.lifecycleScope.launch {
            vm.event.collect { event ->
                when (event) {
                    is LibraryAddCardViewModel.AddCardEvent.Success -> {
                        requireContext().showCustomToast("독서카드가 등록되었습니다.", true)
                        parentFragmentManager.popBackStack()
                    }
                    is LibraryAddCardViewModel.AddCardEvent.Error -> {
                        requireContext().showCustomToast(event.message, false)
                    }
                }
            }
        }
    }

    companion object {
        private const val ARG_MODE           = "arg_mode"
        private const val ARG_MEMBER_BOOK_ID = "arg_member_book_id"

        fun newInstance(mode: AddCardMode, memberBookId: Int = -1) = LibraryAddCardFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MODE, mode.name)
                putInt(ARG_MEMBER_BOOK_ID, memberBookId)
            }
        }
    }
}
