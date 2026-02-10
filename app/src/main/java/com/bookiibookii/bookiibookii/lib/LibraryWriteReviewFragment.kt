package com.bookiibookii.bookiibookii.lib

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    private var currentRating = 0

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

        binding.libDetailBackIv.setOnClickListener { parentFragmentManager.popBackStack() }
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
        val rating = currentRating.toDouble()

        lifecycleScope.launch {
            try {
                val request = ReviewRequest(rating, comment)
                val response = RetrofitClient.api().postBookReview(userBookId, request)

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
                    parentFragmentManager.beginTransaction()
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
        parentFragmentManager.beginTransaction()
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
                currentRating = index + 1
                updateStarUI(stars, currentRating)
                checkValidation()
            }
        }
    }

    private fun updateStarUI(stars: List<ImageView>, rating: Int) {
        stars.forEachIndexed { index, imageView ->
            if (index < rating) {
                imageView.setImageResource(R.drawable.ic_star_filled)
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