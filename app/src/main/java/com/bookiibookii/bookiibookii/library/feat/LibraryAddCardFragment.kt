package com.bookiibookii.bookiibookii.library.feat

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap ?: return@registerForActivityResult
        // 카메라 비트맵 → 캐시 파일로 저장 후 Uri 변환
        val file = java.io.File(requireContext().cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) }
        selectedImageUri = android.net.Uri.fromFile(file)
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
                    onImageCapture   = { takePhotoLauncher.launch(null) },
                    onBackClick      = { parentFragmentManager.popBackStack() },
                    onSubmit         = { page, quotation, memo ->
                        if (memberBookId == -1) {
                            Toast.makeText(requireContext(), "책 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(requireContext(), "독서카드가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                    is LibraryAddCardViewModel.AddCardEvent.Error -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
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
