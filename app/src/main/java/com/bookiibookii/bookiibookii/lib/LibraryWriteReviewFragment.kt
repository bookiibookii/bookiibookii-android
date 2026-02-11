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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.ReviewRequest
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailWrtieReviewBinding
import kotlinx.coroutines.launch

class LibraryWriteReviewFragment : Fragment() {

    private var _binding: FragmentLibBookDetailWrtieReviewBinding? = null
    private val binding get() = _binding!!

    private var userBookId: Int = -1
    private var groupId: Int = -1 // TogetherFragment로 넘겨주기 위해 받음
    private var bookTitle = ""
    private var bookAuthor = ""
    private var bookCover = ""
    private var currentRating : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userBookId = it.getInt("userBookId", -1)
            groupId = it.getInt("groupId", -1)
            bookTitle = it.getString("bookTitle", "") ?: ""
            bookAuthor = it.getString("bookAuthor", "") ?: ""
            bookCover = it.getString("bookCover", "") ?: ""
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibBookDetailWrtieReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initStarRating()
        initInputListener()

        binding.libDetailBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
        binding.libReviewAddBtn.setOnClickListener { postReview() }
    }

    private fun initView() {
        binding.libDetailBookTitleTv.text = bookTitle
        binding.libWriteTitleTv.text = bookTitle
        binding.libDetailBookAuthorTv.text = bookAuthor
        Glide.with(this).load(bookCover).into(binding.libDetailImageIv)
    }

    private fun postReview() {
        val comment = binding.libWriteReviewEt.text.toString()
        val rating = currentRating

        lifecycleScope.launch {
            try {
                val request = ReviewRequest(rating, comment)
                val response = RetrofitClient.api().postBookReview(userBookId, request)
                Log.d("Library", "${response.body()}")

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    // ★ 성공 시: Together(결과) 화면으로 이동
                    val togetherFragment = LibraryBookDetailTogetherFragment().apply {
                        arguments = Bundle().apply {
                            putInt("userBookId", userBookId)
                            putInt("groupId", groupId) // groupId 전달
                            putString("bookTitle", bookTitle)
                            putString("bookAuthor", bookAuthor)
                            putString("bookCover", bookCover)
                        }
                    }
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, togetherFragment)
                        // .addToBackStack(null) // 결과 화면에서 뒤로가기 시 목록으로 가려면 주석 처리
                        .commit()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun navigateToTogetherFragment() {
        // 결과 화면(Together) 프래그먼트 생성
        val togetherFragment = LibraryBookDetailTogetherFragment().apply {
            arguments = Bundle().apply {
                putInt("userBookId", userBookId)
                putString("bookTitle", bookTitle)
                putString("bookAuthor", bookAuthor)
                putString("bookCover", bookCover)
            }
        }

        // 현재 화면(WriteReview)을 대체하여 이동
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, togetherFragment)
            // .addToBackStack(null) // 결과 화면에서 뒤로가기 시 다시 목록으로 가고 싶다면 스택에 추가 X
            .commit()
    }

    // --- (이하 별점 및 입력 감지 UI 로직) ---
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
                val targetHalf = index + 0.5
                val targetFull = index + 1.0
                if (currentRating == targetHalf) {
                    currentRating = targetFull
                } else {
                    // 처음 누르거나 다른 별을 누르면 일단 '반 개' 상태로 만듦
                    currentRating = targetHalf
                }
                updateStarUI(stars, currentRating)
                checkValidation()
            }
        }
    }

    private fun updateStarUI(stars: List<ImageView>, rating: Double) {
        stars.forEachIndexed { index, imageView ->
            val starValue = index + 1.0

            // 🔹 4. 점수에 따라 아이콘 3가지 분기 처리
            if (rating >= starValue) {
                // 꽉 찬 별 (예: rating이 3.0인데 현재 별이 3번째(3.0)일 때)
                imageView.setImageResource(R.drawable.ic_star_filled)
            } else if (rating >= starValue - 0.5) {
                // 반 개 별 (예: rating이 2.5인데 현재 별이 3번째(3.0)일 때)
                // ⚠️ 주의: 프로젝트의 drawable 폴더에 ic_star_half 이미지가 꼭 있어야 합니다!
                imageView.setImageResource(R.drawable.ic_star_half)
            } else {
                // 빈 별
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
        val hasText = binding.libWriteReviewEt.text.isNotBlank()
        val hasRating = currentRating > 0

        val isEnabled = hasText && hasRating
        binding.libReviewAddBtn.isEnabled = isEnabled

        val colorBg = if (isEnabled) R.color.grey_900 else R.color.grey_200
        val colorText = if (isEnabled) R.color.white else R.color.grey_500

        binding.libReviewAddBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), colorBg))
        binding.libReviewAddBtn.setTextColor(ContextCompat.getColor(requireContext(), colorText))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}