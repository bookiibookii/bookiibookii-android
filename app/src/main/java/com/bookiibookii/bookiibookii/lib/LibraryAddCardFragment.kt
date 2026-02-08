package com.bookiibookii.bookiibookii.lib

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.CreateCardRequest
import com.bookiibookii.bookiibookii.data.model.UpdateCardRequest
import com.bookiibookii.bookiibookii.databinding.FragmentLibAddCardBinding
import com.bookiibookii.bookiibookii.trkHost.HostPhotoSelectionDialogFragment
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class LibraryAddCardFragment : Fragment() {

    private var _binding: FragmentLibAddCardBinding? = null
    private val binding get() = _binding!!

    // 데이터 변수
    private var isEditMode = false
    private var userBookId: Int = -1      // 생성 시 필요
    private var cardId: Long = -1L        // 수정 시 필요

    // 수정 모드일 때 기존 데이터
    private var originalPage: Int = 0
    private var originalMemo: String = ""
    private var originalImageUrl: String? = null // Glide로 보여줄 용도

    // 이미지 관련
    private var cameraImageUri: Uri? = null
    private var selectedPhotoUri: Uri? = null // 새로 선택한 이미지 URI

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
        arguments?.let {
            isEditMode = it.getBoolean("isEdit", false)
            if (isEditMode) {
                cardId = it.getLong("cardId", -1L)
                originalPage = it.getInt("page", 0)
                originalMemo = it.getString("memo", "") ?: ""
                originalImageUrl = it.getString("imageUrl", null)
            } else {
                userBookId = it.getInt("userBookId", -1)
            }
        }
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

            // 기존 데이터 채우기
            binding.libAddPageEt.setText(originalPage.toString())
            binding.libAddMemoEt.setText(originalMemo)

            // 기존 이미지가 있다면 프리뷰 모드로 전환
            if (!originalImageUrl.isNullOrEmpty()) {
                binding.libAddCardGuideLl.visibility = View.GONE
                binding.libAddCardPreviewIv.visibility = View.VISIBLE
                binding.libAddCardEditIv.visibility = View.VISIBLE

                // 기존 이미지 로드
                Glide.with(this).load(originalImageUrl).into(binding.libAddCardPreviewIv)

                // 높이 확장
                val params = binding.libAddCardCv.layoutParams
                params.height = dpToPx(400)
                binding.libAddCardCv.layoutParams = params
            }
        } else {
            binding.libAddTitleTv.text = "카드 추가"
            binding.libAddBtn.text = "등록하기"
        }

        // 초기 상태 버튼 업데이트
        updateButtonState()
    }

    private fun initListeners() {
        binding.libAddBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.libAddCardCv.setOnClickListener { openPhotoPicker() }
        binding.libAddCardEditIv.setOnClickListener { openPhotoPicker() }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateButtonState() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        binding.libAddPageEt.addTextChangedListener(textWatcher)

        // ★ 등록/수정 버튼 클릭 시 API 호출
        binding.libAddBtn.setOnClickListener {
            saveCard()
        }
    }

    // ==========================================
    // ★ 핵심 로직: 카드 저장 (생성/수정)
    // ==========================================
    private fun saveCard() {
        val pageInput = binding.libAddPageEt.text.toString().toIntOrNull() ?: 0
        val memoInput = binding.libAddMemoEt.text.toString()

        // 로딩 처리 (버튼 비활성화 등)
        binding.libAddBtn.isEnabled = false

        lifecycleScope.launch {
            try {
                // [Step 1] 이미지가 새로 선택되었는지 확인
                var finalS3Key: String? = null

                if (selectedPhotoUri != null) {
                    // 1-1. 새 이미지가 있으면 -> Presigned URL 발급 -> S3 업로드
                    // 생성/수정 모두 'userBookId'를 기준으로 URL을 발급받는다고 가정 (명세: POST /api/card/{userBookId}/presigned-url)
                    // 수정 시 userBookId가 없다면 cardId로 조회하거나, 이전 화면에서 받아와야 함.
                    // 여기서는 생성 시 받은 userBookId를 사용하거나, 수정 시에도 userBookId가 필요함.

                    val targetBookId = if(isEditMode) userBookId else userBookId // 수정 시에도 userBookId가 필요하다면 arguments에 추가해야 함
                    // ※ 만약 수정 API에 presigned url 발급이 따로 없다면, 기존 userBookId를 계속 사용

                    val presignedRes = RetrofitClient.api().getPresignedUrl(targetBookId)

                    if (presignedRes.isSuccessful && presignedRes.body()?.isSuccess == true) {
                        val result = presignedRes.body()?.result!!
                        finalS3Key = result.s3Key
                        val uploadUrl = result.presignedPutUrl

                        // 1-2. S3에 실제 이미지 바이너리 업로드
                        val imageBytes = requireContext().contentResolver.openInputStream(selectedPhotoUri!!)?.readBytes()
                        if (imageBytes != null) {
                            val requestBody = imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
                            val uploadRes = RetrofitClient.api().uploadImageToS3(uploadUrl, requestBody)

                            if (!uploadRes.isSuccessful) {
                                Toast.makeText(context, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                                binding.libAddBtn.isEnabled = true
                                return@launch
                            }
                        }
                    }
                }

                // [Step 2] 최종 API 호출 (생성 vs 수정)
                if (isEditMode) {
                    // 수정 (PATCH)
                    // 이미지를 안 바꿨으면 finalS3Key는 null -> 서버가 기존 이미지 유지
                    val request = UpdateCardRequest(pageInput, memoInput, finalS3Key)
                    val response = RetrofitClient.api().updateCard(cardId, request)

                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        parentFragmentManager.popBackStack() // 성공 시 뒤로가기
                    } else {
                        Toast.makeText(context, "수정 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    // 생성 (POST)
                    if (finalS3Key != null) {
                        val request = CreateCardRequest(finalS3Key, pageInput, memoInput)
                        val response = RetrofitClient.api().createCard(userBookId, request)

                        if (response.isSuccessful && response.body()?.isSuccess == true) {
                            parentFragmentManager.popBackStack() // 성공 시 뒤로가기
                        } else {
                            Toast.makeText(context, "등록 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // 생성 시 이미지는 필수라고 가정 (버튼 활성 로직에서 체크함)
                        Toast.makeText(context, "이미지 처리 오류", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.libAddBtn.isEnabled = true
            }
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
        selectedPhotoUri = uri // 새 이미지 선택됨

        binding.libAddCardGuideLl.visibility = View.GONE
        binding.libAddCardPreviewIv.visibility = View.VISIBLE
        binding.libAddCardEditIv.visibility = View.VISIBLE

        binding.libAddCardPreviewIv.setImageURI(uri)

        val params = binding.libAddCardCv.layoutParams
        params.height = dpToPx(400)
        binding.libAddCardCv.layoutParams = params

        updateButtonState()
    }

    private fun updateButtonState() {
        val hasPage = binding.libAddPageEt.text.toString().isNotEmpty()

        // 생성 모드: 사진 필수 / 수정 모드: 기존 사진이 있거나 새 사진을 골랐으면 OK
        val hasPhoto = if (isEditMode) {
            !originalImageUrl.isNullOrEmpty() || selectedPhotoUri != null
        } else {
            selectedPhotoUri != null
        }

        val isEnabled = hasPage && hasPhoto

        binding.libAddBtn.isEnabled = isEnabled
        val colorBg = if (isEnabled) R.color.grey_900 else R.color.grey_200
        val colorText = if (isEnabled) R.color.white else R.color.grey_500

        binding.libAddBtn.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), colorBg)
        )
        binding.libAddBtn.setTextColor(
            ContextCompat.getColor(requireContext(), colorText)
        )
    }

    // ... 나머지 유틸 함수 (dpToPx, createCameraImageUri, hideBottomNavigation 등 기존 동일) ...
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
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