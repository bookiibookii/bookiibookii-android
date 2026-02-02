package com.bookiibookii.bookiibookii.lib

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentLibAddCardBinding
import com.bookiibookii.bookiibookii.trkHost.HostPhotoSelectionDialogFragment
import java.io.File

class LibraryAddCardFragment : Fragment() {

    private var _binding: FragmentLibAddCardBinding? = null
    private val binding get() = _binding!!

    private var isEditMode = false
    private var cameraImageUri: Uri? = null
    private var selectedPhotoUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { showPreview(it) }
        }

    private val takePicLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                cameraImageUri?.let { showPreview(it) }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isEditMode = arguments?.getBoolean("isEdit", false) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibAddCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListeners()
        setupFragmentResultListener()
    }

    private fun initView() {
        if (isEditMode) {
            binding.libAddTitleTv.text = "카드 수정"
            binding.libAddBtn.text = "수정하기"
        } else {
            binding.libAddTitleTv.text = "카드 추가"
            binding.libAddBtn.text = "등록하기"
        }

        // 초기 상태: 수정 버튼 숨김
        binding.libAddCardEditIv.visibility = View.GONE
    }

    private fun initListeners() {
        binding.libAddBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 사진이 없을 때 클릭하는 영역 (카드뷰)
        binding.libAddCardCv.setOnClickListener {
            openPhotoPicker()
        }

        // 사진이 있을 때 수정을 위해 클릭하는 버튼 (Edit 아이콘)
        binding.libAddCardEditIv.setOnClickListener {
            openPhotoPicker()
        }

        binding.libAddPageEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateButtonState() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.libAddBtn.setOnClickListener {
            // 등록 로직 실행 후 이동
            parentFragmentManager.popBackStack()
        }
    }

    private fun openPhotoPicker() {
        if (childFragmentManager.isStateSaved) return
        HostPhotoSelectionDialogFragment().show(
            childFragmentManager, HostPhotoSelectionDialogFragment.TAG
        )
    }

    private fun setupFragmentResultListener() {
        childFragmentManager.setFragmentResultListener(
            HostPhotoSelectionDialogFragment.REQ_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            when (bundle.getString(HostPhotoSelectionDialogFragment.ACTION_KEY)) {
                HostPhotoSelectionDialogFragment.ACTION_GALLERY -> pickImageLauncher.launch("image/*")
                HostPhotoSelectionDialogFragment.ACTION_CAMERA -> {
                    cameraImageUri = createCameraImageUri()
                    takePicLauncher.launch(cameraImageUri)
                }
            }
        }
    }

    private fun showPreview(uri: Uri) {
        selectedPhotoUri = uri

        // 1. 안내 문구 숨기고 프리뷰 이미지와 수정 버튼 활성화
        binding.libAddCardGuideLl.visibility = View.GONE
        binding.libAddCardPreviewIv.visibility = View.VISIBLE
        binding.libAddCardEditIv.visibility = View.VISIBLE

        // 2. 이미지 세팅
        binding.libAddCardPreviewIv.setImageURI(uri)

        // 3. 🌟 동적 높이 변경: CardView를 GONE 하지 않으므로 하단 제약 조건이 유지됨
        val params = binding.libAddCardCv.layoutParams
        params.height = dpToPx(400) // 사진 선택 시 400dp로 확장
        binding.libAddCardCv.layoutParams = params

        updateButtonState()
    }

    // 렌더링 오류를 방지하기 위해 안전하게 Int로 변환
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun updateButtonState() {
        // 1. 조건 확인: 페이지 번호가 입력되었고 + 사진이 선택되었는가
        val hasPage = binding.libAddPageEt.text.toString().isNotEmpty()
        val hasPhoto = selectedPhotoUri != null
        val isEnabled = hasPage && hasPhoto

        binding.libAddBtn.isEnabled = isEnabled

        // 2. 색상 리소스 결정
        val colorBg = if (isEnabled) R.color.grey_900 else R.color.grey_200
        val colorText = if (isEnabled) R.color.white else R.color.grey_500

        // 3. 배경색(Tint) 업데이트
        binding.libAddBtn.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), colorBg)
        )

        // 4. 글자색 업데이트 (다시 추가되었습니다!)
        binding.libAddBtn.setTextColor(
            ContextCompat.getColor(requireContext(), colorText)
        )
    }

    private fun createCameraImageUri(): Uri {
        val dir = File(requireContext().cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "card_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
    }

    override fun onResume() {
        super.onResume()
        hideBottomNavigation(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideBottomNavigation(false)
        _binding = null
    }

    private fun hideBottomNavigation(shouldHide: Boolean) {
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNav)
        bottomNav?.visibility = if (shouldHide) View.GONE else View.VISIBLE
    }
}