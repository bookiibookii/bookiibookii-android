package com.bookiibookii.bookiibookii.lib

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R

import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailRelayWriteBinding;


class LibraryBookDetailRelayWriteFragment : Fragment() {

    private var _binding: FragmentLibBookDetailRelayWriteBinding ? = null
    private val binding get() = _binding!!

    // 상태 저장 변수
    private var bookRating = 0
    private var partnerRating = 0
    private val selectedTags = mutableSetOf<TextView>() // 선택된 태그 뷰 저장

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibBookDetailRelayWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initListener()
    }

    private fun initView() {
        // 1. 글자 수 제한 (최대 500자 / 200자)
        setEditTextMaxLength(binding.libWriteReviewEt, 500)
        setEditTextMaxLength(binding.libWritePartnerReviewEt, 200)

        // 초기 버튼 상태 업데이트 (비활성화)
        updateButtonState()
    }

    private fun initListener() {
        // 뒤로가기
        binding.libDetailBackIv.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 1. 책 별점 설정
        setupStarRating(binding.libDetailRateList) { rating ->
            bookRating = rating
            updateButtonState()
        }

        // 2. 파트너 별점 설정
        setupStarRating(binding.libPartnerRateList) { rating ->
            partnerRating = rating
            updateButtonState()
        }

        // 3. 태그 선택 기능 설정
        setupTagSelection()

        // 4. 완료 버튼 클릭
        binding.libReviewAddBtn.setOnClickListener {
            // TODO: 리뷰 등록 API 호출 로직 작성
        }
    }

    private fun setEditTextMaxLength(editText: EditText, maxLength: Int) {
        editText.filters = arrayOf(InputFilter.LengthFilter(maxLength))
    }

    /**
     * 별점 기능 세팅
     */
    private fun setupStarRating(container: LinearLayout, onRatingChanged: (Int) -> Unit) {
        val stars = ArrayList<ImageView>()

        // 컨테이너 안의 ImageView(별)만 수집
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is ImageView) {
                stars.add(child)
            }
        }

        stars.forEachIndexed { index, starView ->
            starView.setOnClickListener {
                val currentRating = index + 1
                onRatingChanged(currentRating)
                updateStarUI(stars, currentRating)
            }
        }
    }

    /**
     * 별점 UI 업데이트 (채워진 별 / 빈 별)
     */
    private fun updateStarUI(stars: List<ImageView>, rating: Int) {
        for (i in stars.indices) {
            if (i < rating) {
                // 채워진 별 (리소스 이름 확인 필요: 예: ic_star_filled)
                stars[i].setImageResource(R.drawable.ic_star_filled)
                // 색상 틴트가 필요하다면 여기서 적용
            } else {
                // 빈 별
                stars[i].setImageResource(R.drawable.ic_star_none)
            }
        }
    }

    /**
     * 태그 선택 리스너 설정
     * (lib_write_partner_review_container 내부의 LinearLayout 행들을 순회하며 TextView 찾기)
     */
    private fun setupTagSelection() {
        val container = binding.libWritePartnerReviewContainer

        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)

            // 별점 리스트가 아닌 수평 LinearLayout(태그 행)만 찾음
            if (child is LinearLayout && child.orientation == LinearLayout.HORIZONTAL && child.id != R.id.lib_partner_rate_list) {

                // 해당 행 안의 TextView(태그)들에 리스너 연결
                for (j in 0 until child.childCount) {
                    val tagView = child.getChildAt(j)
                    if (tagView is TextView) {
                        tagView.setOnClickListener { toggleTag(tagView) }
                    }
                }
            }
        }
    }

    /**
     * 태그 토글 로직
     */
    private fun toggleTag(textView: TextView) {
        if (selectedTags.contains(textView)) {
            // 선택 해제
            selectedTags.remove(textView)
            textView.setBackgroundResource(R.drawable.bg_round_20dp_white_stroke_1dp_gray200)
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_900))
        } else {
            // 선택
            selectedTags.add(textView)
            textView.setBackgroundResource(R.drawable.bg_round_20dp_orange_stroke) // 오렌지 테두리
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.pre_main)) // 메인 컬러
        }
        updateButtonState()
    }

    /**
     * 버튼 상태 업데이트
     * 조건: (책 별점 > 0) && (파트너 별점 > 0) && (태그 1개 이상)
     */
    private fun updateButtonState() {
        // 모든 조건 충족 여부 확인
        val isEnabled = (bookRating > 0) && (partnerRating > 0) && selectedTags.isNotEmpty()

        binding.libReviewAddBtn.isEnabled = isEnabled

        if (isEnabled) {
            // 활성화 스타일: 배경 Grey 900 / 글자 White
            binding.libReviewAddBtn.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.grey_900)
            )
            binding.libReviewAddBtn.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
        } else {
            // 비활성화 스타일: 배경 Grey 200 / 글자 Grey 500 (XML 초기값)
            binding.libReviewAddBtn.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.grey_200)
            )
            binding.libReviewAddBtn.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.grey_500)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}