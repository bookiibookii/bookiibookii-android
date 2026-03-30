package com.bookiibookii.bookiibookii.lib

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.common.showCustomToast // ★ 커스텀 토스트 임포트
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.ReviewRequest
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailWrtieReviewBinding
import kotlinx.coroutines.launch

class LibraryWriteReviewFragment : BaseDetailFragment<FragmentLibBookDetailWrtieReviewBinding>() {

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

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLibBookDetailWrtieReviewBinding {
        return FragmentLibBookDetailWrtieReviewBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        initView()
        initStarRating()
        initInputListener()
        handleSystemBackPressed()

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
        setSpannableColor(binding.libWriteTitleTv, "${bookTitle}에 대한 한줄평을 남겨주세요!", bookTitle)
        binding.libDetailBookAuthorTv.text = bookAuthor
        Glide.with(this).load(bookCover).into(binding.libDetailImageIv)

        binding.libDetailProfileTv.text = hostName
        Glide.with(this).load(hostProfileUrl)
            .placeholder(R.drawable.bg_circle_gray500)
            .error(R.drawable.img_profile_default)
            .circleCrop().into(binding.libDetailProfileIv)

        val formattedStart = if (startDate.isNullOrBlank() || startDate.startsWith("0000")) "0000. 00. 00." else DateUtils.formatDate(startDate)
        binding.libDetailDateTv.text = "$formattedStart ~"
    }

    private fun setSpannableColor(textView: TextView, fullText: String, targetWord: String) {
        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf(targetWord)
        if (startIndex != -1) {
            val endIndex = startIndex + targetWord.length
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.pre_main)),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        textView.text = spannable
    }

    private fun postReview() {
        loadingDialog.show()
        val comment = binding.libWriteReviewEt.text.toString()
        val rating = currentRating

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = ReviewRequest(rating, comment)
                val response = RetrofitClient.api().postBookReview(userBookId, request)

                if (loadingDialog.isShowing) loadingDialog.dismiss()
                if (!isAdded || activity == null) return@launch

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    requireContext().showCustomToast("리뷰가 등록되었습니다.", true)
                    isReviewSubmitted = true

                    requireActivity().supportFragmentManager.setFragmentResult("REFRESH_LIBRARY", Bundle())

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

                    val fm = requireActivity().supportFragmentManager
                    fm.popBackStackImmediate()
                    fm.beginTransaction()
                        .replace(R.id.fragmentContainer, togetherFragment)
                        .commit()

                } else {
                    val msg = response.body()?.message ?: "등록 실패"
                    requireContext().showCustomToast("리뷰 등록 실패: $msg", false)
                }
            } catch (e: Exception) {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
                requireContext().showCustomToast("네트워크 오류가 발생했습니다.", false)
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
                if (isReviewSubmitted) return@setOnClickListener

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
}