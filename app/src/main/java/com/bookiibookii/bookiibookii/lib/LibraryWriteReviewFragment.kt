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
    private var groupId: Int = -1
    private var bookTitle = ""
    private var bookAuthor = ""
    private var bookCover = ""

    // ★ 새로 받을 정보들
    private var hostName = ""
    private var hostProfileUrl = ""
    private var startDate = ""
    private var endDate = ""

    private var currentRating : Double = 0.0

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

        // ★ 프로필과 날짜 세팅 적용
        binding.libDetailProfileTv.text = hostName
        Glide.with(this).load(hostProfileUrl)
            .placeholder(R.drawable.bg_circle_gray500)
            .error(R.drawable.img_profile_default)
            .circleCrop()
            .into(binding.libDetailProfileIv)

        binding.libDetailDateTv.text = if (endDate.isNotEmpty()) "$startDate ~ $endDate" else "$startDate ~"
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
                    val togetherFragment = LibraryBookDetailTogetherFragment().apply {
                        arguments = Bundle().apply {
                            putInt("userBookId", userBookId)
                            putInt("groupId", groupId)
                            putString("bookTitle", bookTitle)
                            putString("bookAuthor", bookAuthor)
                            putString("bookCover", bookCover)

                            // ★ [핵심] 결과 화면이 하얗게 뜨지 않도록 내가 가진 정보를 모두 다시 담아 넘겨줍니다.
                            putString("hostName", hostName)
                            putString("hostProfileUrl", hostProfileUrl)
                            putString("startDate", startDate)
                            putString("endDate", endDate)
                            putDouble("rating", rating) // 내가 방금 적은 별점 적용!
                        }
                    }
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, togetherFragment)
                        .commit()
                }
            } catch (e: Exception) { e.printStackTrace() }
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
                val targetHalf = index + 0.5
                val targetFull = index + 1.0
                if (currentRating == targetHalf) {
                    currentRating = targetFull
                } else {
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