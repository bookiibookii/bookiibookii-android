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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.library.BaseLibraryFragment
import com.bookiibookii.bookiibookii.library.ui.AddCardMode
import com.bookiibookii.bookiibookii.library.ui.LibraryAddCardScreen
import com.bookiibookii.bookiibookii.library.ui.ReadingCard
import com.bookiibookii.bookiibookii.library.ui.ReadingCardType
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

    // 수정 모드: cardId가 있으면 수정, 없으면(-1L) 신규 등록
    private val cardId: Long get() = arguments?.getLong(ARG_CARD_ID, -1L) ?: -1L
    private val isEdit: Boolean get() = cardId != -1L
    private val initialQuote: String get() = arguments?.getString(ARG_QUOTE).orEmpty()
    private val initialPage: String get() = arguments?.getString(ARG_PAGE).orEmpty()
    private val initialMemo: String get() = arguments?.getString(ARG_MEMO).orEmpty()
    private val initialImageUrl: String? get() = arguments?.getString(ARG_IMAGE_URL)
    private val initialS3Key: String? get() = arguments?.getString(ARG_S3KEY)
    private val bookTitle: String get() = arguments?.getString(ARG_BOOK_TITLE).orEmpty()

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
                val uiState by vm.uiState.collectAsStateWithLifecycle()
                LibraryAddCardScreen(
                    mode             = mode,
                    selectedImageUri = selectedImageUri,
                    isEdit           = isEdit,
                    initialQuote     = initialQuote,
                    initialPage      = initialPage,
                    initialMemo      = initialMemo,
                    initialImageUrl  = initialImageUrl,
                    bookTitle        = bookTitle,
                    onImagePick      = { pickImageLauncher.launch("image/*") },
                    onImageCapture   = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    onBackClick      = { parentFragmentManager.popBackStack() },
                    isLoading        = uiState.isLoading,
                    onSubmit         = { page, quotation, memo ->
                        if (memberBookId == -1) {
                            requireContext().showCustomToast("책 정보를 찾을 수 없습니다.", false)
                            return@LibraryAddCardScreen
                        }
                        if (isEdit) {
                            vm.updateCard(
                                cardId          = cardId,
                                memberBookId    = memberBookId,
                                mode            = mode,
                                page            = page,
                                quotation       = quotation,
                                memo            = memo,
                                newImageUri     = selectedImageUri,
                                existingS3Key   = initialS3Key,
                                contentResolver = requireContext().contentResolver,
                            )
                        } else {
                            vm.createCard(
                                memberBookId    = memberBookId,
                                mode            = mode,
                                page            = page,
                                quotation       = quotation,
                                memo            = memo,
                                imageUri        = selectedImageUri,
                                contentResolver = requireContext().contentResolver,
                            )
                        }
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
                        requireContext().showCustomToast(
                            if (isEdit) "독서카드가 수정되었습니다." else "독서카드가 등록되었습니다.",
                            true,
                        )
                        // 수정은 상세(정적 인자) 위에서 진입 → 상세까지 닫고 목록(onResume 재조회)으로 복귀
                        parentFragmentManager.popBackStack()
                        if (isEdit) parentFragmentManager.popBackStack()
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
        private const val ARG_CARD_ID        = "arg_card_id"
        private const val ARG_QUOTE          = "arg_quote"
        private const val ARG_PAGE           = "arg_page"
        private const val ARG_MEMO           = "arg_memo"
        private const val ARG_IMAGE_URL      = "arg_image_url"
        private const val ARG_S3KEY          = "arg_s3key"
        private const val ARG_BOOK_TITLE     = "arg_book_title"

        fun newInstance(mode: AddCardMode, memberBookId: Int = -1, bookTitle: String = "") = LibraryAddCardFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MODE, mode.name)
                putInt(ARG_MEMBER_BOOK_ID, memberBookId)
                putString(ARG_BOOK_TITLE, bookTitle)
            }
        }

        // 수정 진입 — 기존 카드 정보를 프리필 인자로 전달
        fun newInstanceEdit(card: ReadingCard) = LibraryAddCardFragment().apply {
            val mode = if (card.type == ReadingCardType.PHOTO) AddCardMode.PHOTO else AddCardMode.TEXT
            arguments = Bundle().apply {
                putString(ARG_MODE, mode.name)
                putInt(ARG_MEMBER_BOOK_ID, card.memberBookId)
                putLong(ARG_CARD_ID, card.cardId)
                putString(ARG_QUOTE, card.quotation)
                putString(ARG_PAGE, card.page)
                putString(ARG_MEMO, card.content)
                putString(ARG_IMAGE_URL, card.imageUrl)
                putString(ARG_S3KEY, card.s3Key)
                putString(ARG_BOOK_TITLE, card.bookTitle)
            }
        }
    }
}
