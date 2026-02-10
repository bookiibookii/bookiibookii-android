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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    // 수정 모드용 기존 데이터
    private var originalPage: Int = 0
    private var originalMemo: String = ""
    private var originalImageUrl: String? = null

    // 이미지 관련
    private var cameraImageUri: Uri? = null
    private var selectedPhotoUri: Uri? = null

    // 갤러리 런처
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { showPreview(it) }
        }

    // 카메라 런처
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
                // 수정 시에도 userBookId가 필요하다면 여기서 받아야 함 (현재는 생성에만 집중)
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
        setupFragmentResultListener() // 사진 선택 다이얼로그 결과 수신
    }

    private fun initView() {
        if (isEditMode) {
            binding.libAddTitleTv.text = "카드 수정"
            binding.libAddBtn.text = "수정하기"
            binding.libAddPageEt.setText(originalPage.toString())
            binding.libAddMemoEt.setText(originalMemo)

            if (!originalImageUrl.isNullOrEmpty()) {
                binding.libAddCardGuideLl.visibility = View.GONE
                binding.libAddCardPreviewIv.visibility = View.VISIBLE
                binding.libAddCardEditIv.visibility = View.VISIBLE
                Glide.with(this).load(originalImageUrl).into(binding.libAddCardPreviewIv)

                val params = binding.libAddCardCv.layoutParams
                params.height = dpToPx(400)
                binding.libAddCardCv.layoutParams = params
            }
        } else {
            binding.libAddTitleTv.text = "카드 추가"
            binding.libAddBtn.text = "등록하기"
        }
        updateButtonState()
    }

    private fun initListeners() {
        binding.libAddBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 사진 영역 클릭 -> 다이얼로그 띄우기
        binding.libAddCardCv.setOnClickListener { openPhotoPicker() }
        binding.libAddCardEditIv.setOnClickListener { openPhotoPicker() }

        // 입력 감지
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateButtonState() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        binding.libAddPageEt.addTextChangedListener(textWatcher)
        // 메모는 필수가 아니라고 가정하거나, 버튼 활성화 조건에 포함시킬 수 있음 (여기선 페이지+사진만 필수 조건으로 작성됨)

        // 등록/수정 버튼
        binding.libAddBtn.setOnClickListener {
            saveCard()
        }
    }

    // ==========================================
    // ★ 핵심 로직: 카드 저장
    // ==========================================
// ==========================================
    // ★ 핵심 로직: 카드 저장
    // ==========================================
    private fun saveCard() {
        val pageInput = binding.libAddPageEt.text.toString().toIntOrNull() ?: 0
        val memoInput = binding.libAddMemoEt.text.toString()

        Log.d("AddCardDebug", "saveCard 호출됨 - page: $pageInput, userBookId: $userBookId, isEditMode: $isEditMode")

        binding.libAddBtn.isEnabled = false // 중복 클릭 방지

        lifecycleScope.launch {
            try {
                // [수정 모드]
                if (isEditMode) {
                    Log.d("AddCardDebug", "수정 모드 진입")
                    val request = UpdateCardRequest(pageInput, memoInput, null)
                    val response = RetrofitClient.api().updateCard(cardId, request)

                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        Log.d("AddCardDebug", "카드 수정 성공")
                        parentFragmentManager.popBackStack()
                    } else {
                        Log.e("AddCardDebug", "카드 수정 실패: ${response.code()} / ${response.errorBody()?.string()}")
                        Toast.makeText(context, "수정 실패", Toast.LENGTH_SHORT).show()
                        binding.libAddBtn.isEnabled = true
                    }
                    return@launch
                }

                // [생성 모드]
                Log.d("AddCardDebug", "생성 모드 진입")

                // 1. 이미지 선택 확인
                if (selectedPhotoUri == null) {
                    Log.e("AddCardDebug", "이미지 URI가 null임")
                    Toast.makeText(context, "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
                    binding.libAddBtn.isEnabled = true
                    return@launch
                }
                Log.d("AddCardDebug", "선택된 이미지 URI: $selectedPhotoUri")

                // 2. Presigned URL 발급 요청
                Log.d("AddCardDebug", "Presigned URL 요청 시작 (userBookId: $userBookId)")

                val presignedRes = RetrofitClient.api().postPresignedUrl(userBookId)

                // ★★★ 로그 확인 포인트 1: Presigned URL 응답 ★★★
                Log.d("AddCardDebug", "Presigned URL 응답 코드: ${presignedRes.code()}")

                if (!presignedRes.isSuccessful || presignedRes.body()?.isSuccess != true) {
                    val errorBody = presignedRes.errorBody()?.string()
                    Log.e("AddCardDebug", "Presigned URL 발급 실패 - Body: ${presignedRes.body()}, ErrorBody: $errorBody")

                    Toast.makeText(context, "이미지 업로드 주소 발급 실패", Toast.LENGTH_SHORT).show()
                    binding.libAddBtn.isEnabled = true
                    return@launch
                }

                val result = presignedRes.body()!!.result!!
                val s3Key = result.s3Key
                val uploadUrl = result.presignedPutUrl
                Log.d("AddCardDebug", "Presigned URL 발급 성공 - Key: $s3Key, URL: $uploadUrl")

                // 3. S3에 이미지 업로드 (PUT)
                Log.d("AddCardDebug", "S3 이미지 업로드 시작")

// (1) 파일의 실제 타입(MIME Type)을 가져옵니다. (예: image/jpeg, image/png)
                val mimeType = requireContext().contentResolver.getType(selectedPhotoUri!!) ?: "image/jpeg"
                Log.d("AddCardDebug", "파일 타입: $mimeType")

// (2) 이미지 데이터를 바이트 배열로 읽어옵니다.
                val inputStream = requireContext().contentResolver.openInputStream(selectedPhotoUri!!)
                val imageBytes = inputStream?.readBytes()
                inputStream?.close()

                if (imageBytes != null) {
                    // ★ 핵심 변경점: RetrofitClient 대신 '새로운' OkHttpClient를 사용합니다.
                    // 이렇게 해야 앱의 로그인 토큰(Authorization 헤더)이 S3로 전송되지 않습니다.
                    val cleanClient = okhttp3.OkHttpClient()

                    val requestBody = imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())

                    // S3 PUT 요청 생성
                    val request = okhttp3.Request.Builder()
                        .url(uploadUrl) // 받아온 Presigned URL
                        .put(requestBody)
                        .build()

                    try {
                        // 동기적으로 실행 (이미 코루틴 내부이므로 멈추지 않음)
                        val response = withContext(Dispatchers.IO) {
                            cleanClient.newCall(request).execute()
                        }
                        if (!response.isSuccessful) {
                            // 실패 시 로그 출력
                            Log.e("AddCardDebug", "S3 업로드 실패 - 코드: ${response.code}, 메시지: ${response.message}")
                            // 필요하다면 에러 본문 확인: Log.e("AddCardDebug", "에러 내용: ${response.body?.string()}")

                            activity?.runOnUiThread {
                                Toast.makeText(context, "이미지 서버 업로드 실패", Toast.LENGTH_SHORT).show()
                                binding.libAddBtn.isEnabled = true
                            }
                            return@launch
                        }
                        Log.d("AddCardDebug", "S3 업로드 성공! (200 OK)")
                    } catch (e: Exception) {
                        Log.e("AddCardDebug", "S3 업로드 중 오류 발생", e)
                        activity?.runOnUiThread {
                            Toast.makeText(context, "업로드 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                            binding.libAddBtn.isEnabled = true
                        }
                        return@launch
                    }

                } else {
                    Log.e("AddCardDebug", "이미지 파일을 읽을 수 없습니다.")
                    return@launch
                }

                // 4. 최종 카드 생성 요청 (POST)
                Log.d("AddCardDebug", "최종 카드 생성 요청 시작")
                val createRequest = CreateCardRequest(s3Key, pageInput, memoInput)
                val createRes = RetrofitClient.api().createCard(userBookId, createRequest)

                // ★★★ 로그 확인 포인트 3: 카드 생성 응답 ★★★
                if (createRes.isSuccessful && createRes.body()?.isSuccess == true) {
                    Log.d("AddCardDebug", "카드 생성 최종 성공")
                    Toast.makeText(context, "카드가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    val errorBody = createRes.errorBody()?.string()
                    Log.e("AddCardDebug", "카드 생성 실패 - Code: ${createRes.code()}, Error: $errorBody")
                    Toast.makeText(context, "카드 등록 실패", Toast.LENGTH_SHORT).show()
                    binding.libAddBtn.isEnabled = true
                }

            } catch (e: Exception) {
                Log.e("AddCardDebug", "전체 프로세스 중 예외 발생", e)
                Toast.makeText(context, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                binding.libAddBtn.isEnabled = true
            }
        }
    }
    // 사진 선택 다이얼로그 호출
    private fun openPhotoPicker() {
        if (childFragmentManager.isStateSaved) return
        HostPhotoSelectionDialogFragment().show(
            childFragmentManager, HostPhotoSelectionDialogFragment.TAG
        )
    }

    // 다이얼로그 결과 처리 (갤러리 vs 카메라)
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

    // 선택된 사진 미리보기 설정
    // 기존 showPreview 함수를 아래와 같이 수정하세요.
    private fun showPreview(uri: Uri) {
        selectedPhotoUri = uri

        binding.libAddCardGuideLl.visibility = View.GONE
        binding.libAddCardPreviewIv.visibility = View.VISIBLE
        binding.libAddCardEditIv.visibility = View.VISIBLE

        // [수정] setImageURI 대신 Glide 사용
        Glide.with(this)
            .load(uri)
            .centerCrop() // 혹은 .fitCenter()
            .into(binding.libAddCardPreviewIv)

        val params = binding.libAddCardCv.layoutParams
        params.height = dpToPx(400)
        binding.libAddCardCv.layoutParams = params

        updateButtonState()
    }

    // 버튼 활성화 상태 업데이트
    private fun updateButtonState() {
        val hasPage = binding.libAddPageEt.text.toString().isNotEmpty()

        // 수정 모드면 기존 이미지가 있거나 새 이미지가 있으면 OK
        // 생성 모드면 반드시 새 이미지가 있어야 OK
        val hasPhoto = if (isEditMode) {
            !originalImageUrl.isNullOrEmpty() || selectedPhotoUri != null
        } else {
            selectedPhotoUri != null
        }

        val isEnabled = hasPage && hasPhoto

        binding.libAddBtn.isEnabled = isEnabled

        // 버튼 색상 변경
        val colorBg = if (isEnabled) R.color.grey_900 else R.color.grey_200
        val colorText = if (isEnabled) R.color.white else R.color.grey_500

        binding.libAddBtn.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), colorBg)
        )
        binding.libAddBtn.setTextColor(
            ContextCompat.getColor(requireContext(), colorText)
        )
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun createCameraImageUri(): Uri {
        val dir = File(requireContext().cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "card_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
    }

    // 바텀 네비게이션 숨김 처리
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