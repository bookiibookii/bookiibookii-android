package com.bookiibookii.bookiibookii.lib

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bookiibookii.bookiibookii.bookData.Data.LibReview
import com.bookiibookii.bookiibookii.bookData.viewModel.ReviewModel
import com.bookiibookii.bookiibookii.databinding.FragmentLibAddCardBinding
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LibraryAddCardFragment : Fragment() {

    private var _binding: FragmentLibAddCardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReviewModel by activityViewModels()

    private var selectedImageUri: Uri? = null
    private var isEditMode = false
    private var editTargetId: Long = -1

    // [수정] 4.5.0 버전의 표준 방식 (Contract 사용)
    // 람다 식의 result는 'CropImageView.CropResult' 타입입니다.
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // 성공 시 uriContent를 가져옵니다.
            selectedImageUri = result.uriContent
            showImage(selectedImageUri)
            checkInputValidity()
        } else {
            // 실패 시 에러 메시지
            val exception = result.error
            Toast.makeText(requireContext(), "사진 로드 실패: ${exception?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibAddCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 수정 모드 데이터 확인
        val reviewId = arguments?.getLong("edit_review_id", -1L) ?: -1L
        if (reviewId != -1L) {
            val data = viewModel.getReviewById(reviewId)
            if (data != null) {
                setupEditMode(data)
            }
        }

        initListeners()
        initTextWatchers()
    }

    private fun setupEditMode(data: LibReview) {
        isEditMode = true
        editTargetId = data.id

        binding.libAddTitleTv.text = "카드 수정"
        binding.btnSubmit.text = "수정하기"

        binding.libAddPageEt.setText(data.page.toString())
        binding.libAddMemoEt.setText(data.content)

        if (data.reviewImageUri != null) {
            selectedImageUri = Uri.parse(data.reviewImageUri)
            showImage(selectedImageUri)
        }
    }

    private fun showImage(uri: Uri?) {
        if (uri == null) return
        binding.libAddCardCv.removeAllViews()
        val imageView = ImageView(requireContext())
        imageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        Glide.with(this).load(uri).into(imageView)
        binding.libAddCardCv.addView(imageView)
    }

    private fun initListeners() {
        binding.libAddBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 카드 클릭 시 크롭 실행
        binding.libAddCardCv.setOnClickListener { startCrop() }

        binding.btnSubmit.setOnClickListener { saveReview() }
    }

    // [수정] 크롭 실행 함수
    private fun startCrop() {
        // 옵션 설정
        val options = CropImageOptions(
            imageSourceIncludeGallery = true, // 갤러리 포함
            imageSourceIncludeCamera = true,  // 카메라 포함
            guidelines = CropImageView.Guidelines.ON, // 가이드라인 표시
            aspectRatioX = 1, // 1:1 비율
            aspectRatioY = 1,
            fixAspectRatio = true
        )

        // 실행 (uri = null이면 갤러리 선택창이 뜸)
        cropImage.launch(
            CropImageContractOptions(uri = null, cropImageOptions = options)
        )
    }

    private fun saveReview() {
        val page = binding.libAddPageEt.text.toString().toIntOrNull() ?: 0
        val memo = binding.libAddMemoEt.text.toString()
        val date = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())

        if (isEditMode) {
            val updatedReview = LibReview(
                id = editTargetId,
                userName = "kanghunsim",
                content = memo,
                page = page,
                date = date,
                reviewImageUri = selectedImageUri?.toString(),
                isMine = true
            )
            viewModel.updateReview(updatedReview)
            Toast.makeText(context, "수정되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            val newReview = LibReview(
                id = System.currentTimeMillis(),
                userName = "kanghunsim",
                content = memo,
                page = page,
                date = date,
                reviewImageUri = selectedImageUri?.toString(),
                isMine = true
            )
            viewModel.addReview(newReview)
            Toast.makeText(context, "등록되었습니다.", Toast.LENGTH_SHORT).show()
        }

        parentFragmentManager.popBackStack()
    }

    private fun initTextWatchers() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = checkInputValidity()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        binding.libAddPageEt.addTextChangedListener(watcher)
        binding.libAddMemoEt.addTextChangedListener(watcher)
    }

    private fun checkInputValidity() {
        val isPageValid = binding.libAddPageEt.text.isNotEmpty()
        val isMemoValid = binding.libAddMemoEt.text.isNotEmpty()

        binding.btnSubmit.isEnabled = isPageValid && isMemoValid

//        if(binding.btnSubmit.isEnabled) {
//            binding.btnSubmit.setBackgroundResource(R.drawable.bg_round_20dp_black)
//            binding.btnSubmit.setTextColor(resources.getColor(R.color.white, null))
//        } else {
//            binding.btnSubmit.setBackgroundResource(R.drawable.bg_round_20dp_gray200)
//            binding.btnSubmit.setTextColor(resources.getColor(R.color.grey_500, null))
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}