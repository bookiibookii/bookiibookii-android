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
import androidx.fragment.app.setFragmentResult
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailWrtieReviewBinding

class LibraryWriteReviewFragment : Fragment() {

    private var _binding: FragmentLibBookDetailWrtieReviewBinding? = null
    private val binding get() = _binding!!

    private var currentRating = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibBookDetailWrtieReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initStarRating()
        initInputListener()

        binding.libDetailBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // [기능 4] 후기 남기기 버튼
        binding.libReviewAddBtn.setOnClickListener {
            // Fragment Result API를 사용하여 이전 화면(DetailFragment)으로 데이터 전달
            val result = Bundle().apply {
                putString("review_content", binding.libWriteReviewEt.text.toString())
                putInt("review_rating", currentRating)
            }
            setFragmentResult("review_request", result)


            parentFragmentManager.popBackStack()

            navigateToDetailFragment()
        }
    }

    private fun navigateToDetailFragment() {

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, LibraryBookDetailFragment())
            .commit()
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
                currentRating = index + 1
                updateStarUI(stars, currentRating)
                checkValidation()
            }
        }
    }

    private fun updateStarUI(stars: List<ImageView>, rating: Int) {
        stars.forEachIndexed { index, imageView ->
            if (index < rating) {
                imageView.setImageResource(R.drawable.ic_star_filled) // 채워진 별
            } else {
                imageView.setImageResource(R.drawable.ic_star_none)   // 빈 별
            }
        }
    }

    // [기능 4] 입력 감지 및 버튼 활성화
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

        if (hasText && hasRating) {
            // 버튼 활성화 스타일
            binding.libReviewAddBtn.isEnabled = true
            binding.libReviewAddBtn.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.grey_900)
            )
            binding.libReviewAddBtn.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
        } else {
            // 버튼 비활성화 스타일
            binding.libReviewAddBtn.isEnabled = false
            binding.libReviewAddBtn.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.grey_200)
            )
            binding.libReviewAddBtn.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.grey_500)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        setBottomNavVisibility(false)
    }

    // 화면이 파괴될 때(뒤로가기 등) 다시 보임
    override fun onDestroyView() {
        super.onDestroyView()
        setBottomNavVisibility(true)
        _binding = null
    }

    private fun setBottomNavVisibility(isVisible: Boolean) {
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNav)
        bottomNav?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}