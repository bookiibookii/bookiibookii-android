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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.common.showCustomToast
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

class LibraryAddCardFragment : BaseDetailFragment<FragmentLibAddCardBinding>() {

    private lateinit var loadingDialog: LoadingDialog

    private var isEditMode = false
    private var userBookId: Int = -1
    private var cardId: Long = -1L

    private var originalPage: Int = 0
    private var originalMemo: String = ""
    private var originalImageUrl: String? = null

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

        // ★ 키보드가 올라올 때, 하단 레이아웃을 밀어올리고 스크롤이 끝까지 가능하게 해주는 로직
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            val bottomPadding = if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                imeInsets.bottom
            } else {
                systemBarsInsets.bottom
            }

            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, bottomPadding)
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

                // ★ 사진을 선택하면 높이를 늘려서 잘 보이게 함 (스크롤 가능해짐)
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

        // 메모 입력창 포커스 시, 살짝 스크롤해줘서 키보드에 가려지지 않게 도와줌
        binding.libAddMemoEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.libAddMemoEt.postDelayed({
                    binding.libAddMemoEt.requestRectangleOnScreen(
                        android.graphics.Rect(0, 0, binding.libAddMemoEt.width, binding.libAddMemoEt.height), true
                    )
                }, 300)
            }
        }

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
                if (isEditMode) {
                    val request = UpdateCardRequest(pageInput, memoInput, null)
                    val response = RetrofitClient.api().updateCard(cardId, request)

                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        requireContext().showCustomToast("카드가 수정되었습니다.", true)
                        requireActivity().supportFragmentManager.popBackStack()
                    } else {
                        requireContext().showCustomToast("수정 실패", false)
                        binding.libAddBtn.isEnabled = true
                    }
                    return@launch
                }

                if (selectedPhotoUri == null) {
                    requireContext().showCustomToast("이미지를 선택해주세요.", false)
                    binding.libAddBtn.isEnabled = true
                    return@launch
                }

                val presignedRes = RetrofitClient.api().postPresignedUrl(userBookId)
                if (!presignedRes.isSuccessful || presignedRes.body()?.isSuccess != true) {
                    requireContext().showCustomToast("이미지 업로드 주소 발급 실패", false)
                    binding.libAddBtn.isEnabled = true
                    return@launch
                }

                val result = presignedRes.body()!!.result!!
                val s3Key = result.s3Key
                val uploadUrl = result.presignedPutUrl

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
                        requireContext().showCustomToast("이미지 서버 업로드 실패", false)
                        binding.libAddBtn.isEnabled = true
                    }
                    return@launch
                }

                val createRequest = CreateCardRequest(s3Key, pageInput, memoInput)
                val createRes = RetrofitClient.api().createCard(userBookId, createRequest)

                if (createRes.isSuccessful && createRes.body()?.isSuccess == true) {
                    requireContext().showCustomToast("카드가 등록되었습니다.", true)
                    parentFragmentManager.popBackStack()
                } else {
                    requireContext().showCustomToast("카드 등록 실패", false)
                    binding.libAddBtn.isEnabled = true
                }

            } catch (e: Exception) {
                requireContext().showCustomToast("오류가 발생했습니다.", false)
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

        // ★ 사진 선택 후 레이아웃 크기를 키워줍니다
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