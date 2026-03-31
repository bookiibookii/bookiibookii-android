package com.bookiibookii.bookiibookii.lib

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.CreateCardRequest
import com.bookiibookii.bookiibookii.data.model.UpdateCardRequest
import com.bookiibookii.bookiibookii.databinding.FragmentLibAddCardBinding
import com.bookiibookii.bookiibookii.trkHost.HostPhotoSelectionDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

// 1. 제네릭 타입 명시
class LibraryAddCardFragment : BaseDetailFragment<FragmentLibAddCardBinding>() {

    private lateinit var loadingDialog: LoadingDialog

    // 데이터 변수
    private var isEditMode = false
    private var userBookId: Int = -1
    private var cardId: Long = -1L

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
            } else {
                userBookId = it.getInt("userBookId", -1)
            }
        }
    }

    // 2. BaseFragment에서 요구하는 바인딩 인플레이트 함수 구현
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLibAddCardBinding {
        return FragmentLibAddCardBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        initView()
        initListeners()
        setupFragmentResultListener()

        // ★ 키보드(IME) 높이를 무시하도록 변경
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            v.setPadding(0, 0, 0, systemBarHeight)
            insets
        }
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
        binding.libAddBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        binding.libAddCardCv.setOnClickListener { openPhotoPicker() }
        binding.libAddCardEditIv.setOnClickListener { openPhotoPicker() }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateButtonState() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        binding.libAddPageEt.addTextChangedListener(textWatcher)

        binding.libAddBtn.setOnClickListener {
            saveCard()
        }
    }

    private fun saveCard() {
        val pageInput = binding.libAddPageEt.text.toString().toIntOrNull() ?: 0
        val memoInput = binding.libAddMemoEt.text.toString()

        binding.libAddBtn.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            loadingDialog.show()
            try {
                // [수정 모드]
                if (isEditMode) {
                    val request = UpdateCardRequest(pageInput, memoInput, null)
                    val response = RetrofitClient.api().updateCard(cardId, request)

                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        requireActivity().supportFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(context, "수정 실패", Toast.LENGTH_SHORT).show()
                        binding.libAddBtn.isEnabled = true
                    }
                    return@launch
                }

                // [생성 모드]
                if (selectedPhotoUri == null) {
                    Toast.makeText(context, "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
                    binding.libAddBtn.isEnabled = true
                    return@launch
                }

                // 2. Presigned URL 발급
                val presignedRes = RetrofitClient.api().postPresignedUrl(userBookId)
                if (!presignedRes.isSuccessful || presignedRes.body()?.isSuccess != true) {
                    Toast.makeText(context, "이미지 업로드 주소 발급 실패", Toast.LENGTH_SHORT).show()
                    binding.libAddBtn.isEnabled = true
                    return@launch
                }

                val result = presignedRes.body()!!.result!!
                val s3Key = result.s3Key
                val uploadUrl = result.presignedPutUrl

                // 3. S3 이미지 업로드
                val mimeType = requireContext().contentResolver.getType(selectedPhotoUri!!) ?: "image/jpeg"

                val tempFile = File(requireContext().cacheDir, "upload_temp_${System.currentTimeMillis()}.jpg")
                val inputStream = requireContext().contentResolver.openInputStream(selectedPhotoUri!!)
                val outputStream = java.io.FileOutputStream(tempFile)

                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())

                val cleanClient = okhttp3.OkHttpClient()
                val request = okhttp3.Request.Builder()
                    .url(uploadUrl)
                    .put(requestBody)
                    .build()

                val response = withContext(Dispatchers.IO) { cleanClient.newCall(request).execute() }

                if (tempFile.exists()) tempFile.delete()

                if (!response.isSuccessful) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "이미지 서버 업로드 실패", Toast.LENGTH_SHORT).show()
                        binding.libAddBtn.isEnabled = true
                    }
                    return@launch
                }

                // 4. 최종 카드 생성 요청
                val createRequest = CreateCardRequest(s3Key, pageInput, memoInput)
                val createRes = RetrofitClient.api().createCard(userBookId, createRequest)

                if (createRes.isSuccessful && createRes.body()?.isSuccess == true) {
                    Toast.makeText(context, "카드가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(context, "카드 등록 실패", Toast.LENGTH_SHORT).show()
                    binding.libAddBtn.isEnabled = true
                }

            } catch (e: Exception) {
                Toast.makeText(context, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                binding.libAddBtn.isEnabled = true
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
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
        selectedPhotoUri = uri

        binding.libAddCardGuideLl.visibility = View.GONE
        binding.libAddCardPreviewIv.visibility = View.VISIBLE
        binding.libAddCardEditIv.visibility = View.VISIBLE

        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(binding.libAddCardPreviewIv)

        val params = binding.libAddCardCv.layoutParams
        params.height = dpToPx(400)
        binding.libAddCardCv.layoutParams = params

        updateButtonState()
    }

    private fun updateButtonState() {
        val hasPage = binding.libAddPageEt.text.toString().isNotEmpty()
        val hasPhoto = if (isEditMode) {
            !originalImageUrl.isNullOrEmpty() || selectedPhotoUri != null
        } else {
            selectedPhotoUri != null
        }

        val isEnabled = hasPage && hasPhoto
        binding.libAddBtn.isEnabled = isEnabled

        val colorBg = if (isEnabled) R.color.grey_900 else R.color.grey_200
        val colorText = if (isEnabled) R.color.white else R.color.grey_500

        binding.libAddBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), colorBg))
        binding.libAddBtn.setTextColor(ContextCompat.getColor(requireContext(), colorText))
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun createCameraImageUri(): Uri {
        val dir = File(requireContext().cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "card_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
    }
}