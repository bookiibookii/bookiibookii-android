package com.bookiibookii.bookiibookii.lib

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.ReviewRequest
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailWrtieReviewBinding
import kotlinx.coroutines.launch

class LibraryWriteReviewFragment : Fragment() {

    private var _binding: FragmentLibBookDetailWrtieReviewBinding? = null
    private val binding get() = _binding!!

    private var userBookId: Int = -1
    private var groupId: Int = -1
    private var bookTitle = ""
    private var bookAuthor = ""
    private var bookCover = ""
    private var hostName = ""
    private var hostProfileUrl = ""
    private var startDate = ""
    private var endDate = ""

    private lateinit var loadingDialog: LoadingDialog
    private var currentRating : Double = 0.0

    // ★ [핵심] 전송 완료 여부 체크 변수
    private var isReviewSubmitted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userBookId = it.getInt("userBookId", -1)
            groupId = it.getInt("groupId", -1)
            bookTitle = it.getString("bookTitle", "") ?: ""
            bookAuthor = it.getString("bookAuthor", "") ?: ""
            bookCover = it.getString("bookCover", "") ?: ""
            hostName = it.getString("hostName", "") ?: ""
            hostProfileUrl = it.getString("hostProfileUrl", "") ?: ""
            startDate = it.getString("startDate", "") ?: ""
            endDate = it.getString("endDate", "") ?: ""
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibBookDetailWrtieReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        initView()
        initStarRating()
        initInputListener()
        handleSystemBackPressed() // 시스템 백버튼 처리

        // ★ 뒤로가기 버튼 리스너
        binding.libDetailBackIv.setOnClickListener {
            if (isReviewSubmitted) {
                goToLibrary()
            } else {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }

        binding.libReviewAddBtn.setOnClickListener { postReview() }
    }

    private fun handleSystemBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isReviewSubmitted) {
                    goToLibrary()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })
    }

    private fun goToLibrary() {
        parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, LibraryFragment())
            .commit()
    }

    private fun initView() {
        binding.libDetailBookTitleTv.text = bookTitle
        binding.libWriteTitleTv.text = bookTitle
        binding.libDetailBookAuthorTv.text = bookAuthor
        Glide.with(this).load(bookCover).into(binding.libDetailImageIv)

        binding.libDetailProfileTv.text = hostName
        Glide.with(this).load(hostProfileUrl)
            .placeholder(R.drawable.bg_circle_gray500)
            .error(R.drawable.img_profile_default)
            .circleCrop().into(binding.libDetailProfileIv)

        binding.libDetailDateTv.text = if (endDate.isNotEmpty()) "$startDate ~ $endDate" else "$startDate ~"
    }

    private fun postReview() {
        loadingDialog.show()
        val comment = binding.libWriteReviewEt.text.toString()
        val rating = currentRating

        lifecycleScope.launch {
            try {
                val request = ReviewRequest(rating, comment)
                val response = RetrofitClient.api().postBookReview(userBookId, request)

                if (loadingDialog.isShowing) loadingDialog.dismiss()
                if (!isAdded || activity == null) return@launch

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(context, "리뷰가 등록되었습니다.", Toast.LENGTH_SHORT).show()

                    // 전송 완료 상태 업데이트
                    isReviewSubmitted = true

                    // 라이브러리 목록 새로고침 신호
                    requireActivity().supportFragmentManager.setFragmentResult("REFRESH_LIBRARY", Bundle())

                    // 투게더 화면으로 넘어갈 데이터 세팅
                    val togetherFragment = LibraryBookDetailTogetherFragment().apply {
                        arguments = Bundle().apply {
                            putInt("userBookId", userBookId)
                            putInt("groupId", groupId)
                            putString("bookTitle", bookTitle)
                            putString("bookAuthor", bookAuthor)
                            putString("bookCover", bookCover)
                            putString("hostName", hostName)
                            putString("hostProfileUrl", hostProfileUrl)
                            putString("startDate", startDate)
                            putString("endDate", endDate)
                            putDouble("rating", currentRating)
                        }
                    }

                    // ★ [핵심 수정 부분] 꼬임 방지를 위한 안전한 화면 전환 로직
                    val fm = requireActivity().supportFragmentManager

                    // 1. 현재 화면(리뷰 작성)을 '즉시' 스택에서 제거합니다.
                    fm.popBackStackImmediate()

                    // 2. 밑에 깔려있던 '아이엔지 화면'을 '투게더 화면'으로 교체합니다.
                    // (addToBackStack을 쓰지 않으면 투게더 화면에서 뒤로가기 시 자연스럽게 메인 라이브러리로 돌아갑니다)
                    fm.beginTransaction()
                        .replace(R.id.fragmentContainer, togetherFragment)
                        .commit()

                } else {
                    Toast.makeText(context, "리뷰 등록 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
                e.printStackTrace()
            }
        }
    }

    private fun initStarRating() {
        val stars = listOf(
            binding.libDetailRateList.getChildAt(0) as ImageView,
            binding.libDetailRateList.getChildAt(1) as ImageView,
            binding.libDetailRateList.getChildAt(2) as ImageView,
            binding.libDetailRateList.getChildAt(3) as ImageView,
            binding.libDetailRateList.getChildAt(4) as ImageView
        )

        stars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                if (isReviewSubmitted) return@setOnClickListener // 전송 후 수정 금지

                val targetHalf = index + 0.5
                val targetFull = index + 1.0
                currentRating = if (currentRating == targetHalf) targetFull else targetHalf
                updateStarUI(stars, currentRating)
                checkValidation()
            }
        }
    }

    private fun updateStarUI(stars: List<ImageView>, rating: Double) {
        stars.forEachIndexed { index, imageView ->
            val starValue = index + 1.0
            if (rating >= starValue) {
                imageView.setImageResource(R.drawable.ic_star_filled)
            } else if (rating >= starValue - 0.5) {
                imageView.setImageResource(R.drawable.ic_star_half)
            } else {
                imageView.setImageResource(R.drawable.ic_star_none)
            }
        }
    }

    private fun initInputListener() {
        binding.libWriteReviewEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { checkValidation() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun checkValidation() {
        // 이미 제출했다면 무조건 비활성화
        if (isReviewSubmitted) {
            binding.libReviewAddBtn.isEnabled = false
            binding.libReviewAddBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey_200))
            binding.libReviewAddBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_500))
            return
        }

        val hasText = binding.libWriteReviewEt.text.isNotBlank()
        val hasRating = currentRating > 0

        val isEnabled = hasText && hasRating
        binding.libReviewAddBtn.isEnabled = isEnabled

        val colorBg = if (isEnabled) R.color.grey_900 else R.color.grey_200
        val colorText = if (isEnabled) R.color.white else R.color.grey_500

        binding.libReviewAddBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), colorBg))
        binding.libReviewAddBtn.setTextColor(ContextCompat.getColor(requireContext(), colorText))
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