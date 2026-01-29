package com.bookiibookii.bookiibookii.lib

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.LibReview
import com.bookiibookii.bookiibookii.bookData.viewModel.ReviewModel
import com.bookiibookii.bookiibookii.databinding.FragmentLibAddCardBinding
import com.bumptech.glide.Glide
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

    // 이미지 크롭 런처
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            selectedImageUri = result.uriContent
            showImage(selectedImageUri)
            // 이미지 등록 여부는 필수 조건이 아닐 수도 있으나, 필요하다면 여기서 checkInputValidity() 호출
        } else {
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

        // 1. 수정 모드인지 확인 (Arguments)
        val reviewId = arguments?.getLong("edit_review_id", -1L) ?: -1L
        if (reviewId != -1L) {
            val data = viewModel.getReviewById(reviewId)
            if (data != null) {
                setupEditMode(data)
            }
        }

        // 2. 리스너 초기화
        initListeners()
        initTextWatchers()

        // 3. [중요] 화면 처음 진입 시 버튼 상태 초기화 (입력값이 없으면 비활성화 되도록)
        checkInputValidity()
    }

    // 수정 모드 세팅
    private fun setupEditMode(data: LibReview) {
        isEditMode = true
        editTargetId = data.id

        // 타이틀과 버튼 텍스트 변경
        binding.libAddTitleTv.text = "카드 수정"
        binding.libAddBtn.text = "수정하기"

        // 기존 데이터 채워넣기
        binding.libAddPageEt.setText(data.page.toString())
        binding.libAddMemoEt.setText(data.content)

        // 이미지 로드
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
        binding.libAddCardCv.setOnClickListener { startCrop() }
        binding.libAddBtn.setOnClickListener { saveReview() }
    }

    private fun startCrop() {
        val options = CropImageOptions(
            imageSourceIncludeGallery = true,
            imageSourceIncludeCamera = true,
            guidelines = CropImageView.Guidelines.ON,
            aspectRatioX = 1,
            aspectRatioY = 1,
            fixAspectRatio = true
        )
        cropImage.launch(
            CropImageContractOptions(uri = null, cropImageOptions = options)
        )
    }

    private fun saveReview() {
        val page = binding.libAddPageEt.text.toString().toIntOrNull() ?: 0
        val memo = binding.libAddMemoEt.text.toString()
        val date = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())

        // 실제 앱에서는 유저 정보를 가져와야 함 (현재는 하드코딩)
        val userName = "kanghunsim"

        if (isEditMode) {
            // 수정 로직
            val updatedReview = LibReview(
                id = editTargetId,
                userName = userName,
                content = memo,
                page = page,
                date = date,
                reviewImageUri = selectedImageUri?.toString(),
                isMine = true
            )
            viewModel.updateReview(updatedReview)
            Toast.makeText(context, "수정되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            // 등록 로직
            val newReview = LibReview(
                id = System.currentTimeMillis(),
                userName = userName,
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
            override fun afterTextChanged(s: Editable?) {
                // 텍스트가 변할 때마다 유효성 검사 수행
                checkInputValidity()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        binding.libAddPageEt.addTextChangedListener(watcher)
        binding.libAddMemoEt.addTextChangedListener(watcher)
    }

    // [핵심] 입력 유효성 검사 및 버튼 디자인 변경
    private fun checkInputValidity() {
        val isPageValid = binding.libAddPageEt.text.isNotEmpty()
        val isMemoValid = binding.libAddMemoEt.text.isNotEmpty()

        // 페이지와 메모가 모두 입력되어야 활성화
        val isEnabled = isPageValid && isMemoValid
        binding.libAddBtn.isEnabled = isEnabled

        if (isEnabled) {
            // 활성화 상태: 진한 회색 배경 + 흰색 글씨
            binding.libAddBtn.setBackgroundResource(R.drawable.bg_round_20dp_gray900)
            binding.libAddBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        } else {
            // 비활성화 상태: 연한 회색 배경 + 회색 글씨
            binding.libAddBtn.setBackgroundResource(R.drawable.bg_round_20dp_gray200)
            binding.libAddBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_500))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}