package com.bookiibookii.bookiibookii.lib

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.RelayReviewRequest // ★ 생성한 DTO import
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailRelayWriteBinding
import kotlinx.coroutines.launch

class LibraryBookDetailRelayWriteFragment : Fragment() {

    private var _binding: FragmentLibBookDetailRelayWriteBinding? = null
    private val binding get() = _binding!!

    // 이전 화면에서 받아올 userBookId
    private var userBookId: Int = -1

    // ★ 상태 저장 변수 (Float/Double 처리를 위해 0.0으로 변경)
    private var bookRating = 0.0
    private var partnerRating = 0.0
    private val selectedTags = mutableSetOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bundle에서 userBookId 수신 (이전 화면에서 넘겨주어야 함)
        arguments?.let {
            userBookId = it.getInt("userBookId", -1)
        }
    }

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
        setEditTextMaxLength(binding.libWriteReviewEt, 500)
        setEditTextMaxLength(binding.libWritePartnerReviewEt, 200)
        updateButtonState()
    }

    private fun initListener() {
        binding.libDetailBackIv.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 1. 책 별점 설정 (0.5 단위 지원)
        setupStarRating(binding.libDetailRateList, isBookRating = true)

        // 2. 파트너 별점 설정 (0.5 단위 지원)
        setupStarRating(binding.libPartnerRateList, isBookRating = false)

        // 3. 태그 선택
        setupTagSelection()

        // 4. 리뷰 완료 버튼 클릭 (API 연동)
        binding.libReviewAddBtn.setOnClickListener {
            submitReview()
        }
    }

    private fun setEditTextMaxLength(editText: EditText, maxLength: Int) {
        editText.filters = arrayOf(InputFilter.LengthFilter(maxLength))
    }

    /**
     * ★ 별점 0.5 단위 토글 로직
     */
    private fun setupStarRating(container: LinearLayout, isBookRating: Boolean) {
        val stars = ArrayList<ImageView>()
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is ImageView) stars.add(child)
        }

        // 각 별 컨테이너마다 독립적인 현재 점수를 기억해야 함
        var currentContainerRating = 0.0

        stars.forEachIndexed { index, starView ->
            starView.setOnClickListener {
                val targetHalf = index + 0.5
                val targetFull = index + 1.0

                // 같은 별을 반복 클릭하면 0.5 -> 1.0 -> 0.5 로 토글됨
                currentContainerRating = if (currentContainerRating == targetHalf) {
                    targetFull
                } else {
                    targetHalf
                }

                // 외부 변수에 상태 저장
                if (isBookRating) bookRating = currentContainerRating
                else partnerRating = currentContainerRating

                updateStarUI(stars, currentContainerRating)
                updateButtonState()
            }
        }
    }

    /**
     * ★ 별점 UI 업데이트 (ic_star_half 활용)
     */
    private fun updateStarUI(stars: List<ImageView>, rating: Double) {
        for (i in stars.indices) {
            when {
                rating >= (i + 1.0) -> {
                    // 꽉 찬 별
                    stars[i].setImageResource(R.drawable.ic_star_filled)
                }
                rating >= (i + 0.5) -> {
                    // 반쪽 별 (해당 Drawable 파일이 res/drawable에 있어야 합니다)
                    stars[i].setImageResource(R.drawable.ic_star_half)
                }
                else -> {
                    // 빈 별
                    stars[i].setImageResource(R.drawable.ic_star_none)
                }
            }
        }
    }

    private fun setupTagSelection() {
        val container = binding.libWritePartnerReviewContainer
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is LinearLayout && child.orientation == LinearLayout.HORIZONTAL && child.id != R.id.lib_partner_rate_list) {
                for (j in 0 until child.childCount) {
                    val tagView = child.getChildAt(j)
                    if (tagView is TextView) {
                        tagView.setOnClickListener { toggleTag(tagView) }
                    }
                }
            }
        }
    }

    private fun toggleTag(textView: TextView) {
        if (selectedTags.contains(textView)) {
            selectedTags.remove(textView)
            textView.setBackgroundResource(R.drawable.bg_round_20dp_white_stroke_1dp_gray200)
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_900))
        } else {
            selectedTags.add(textView)
            textView.setBackgroundResource(R.drawable.bg_round_20dp_orange_stroke)
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.pre_main))
        }
        updateButtonState()
    }

    private fun updateButtonState() {
        val isEnabled = (bookRating > 0.0) && (partnerRating > 0.0) && selectedTags.isNotEmpty()
        binding.libReviewAddBtn.isEnabled = isEnabled

        if (isEnabled) {
            binding.libReviewAddBtn.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.grey_900)
            )
            binding.libReviewAddBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        } else {
            binding.libReviewAddBtn.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.grey_200)
            )
            binding.libReviewAddBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_500))
        }
    }

    /**
     * ★ API 연동: 리뷰 데이터 전송
     */
    private fun submitReview() {
        if (userBookId == -1) {
            Toast.makeText(context, "도서 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. UI의 태그 텍스트를 서버의 Enum 값(영문)으로 변환
        val badgeCodes = selectedTags.map { mapUiTextToBadgeCode(it.text.toString()) }

        // 2. 요청 DTO 생성
        val request = RelayReviewRequest(
            bookRating = bookRating,
            bookComment = binding.libWriteReviewEt.text.toString(),
            partnerRating = partnerRating,
            partnerComment = binding.libWritePartnerReviewEt.text.toString(),
            badgeCodes = badgeCodes
        )

        binding.libReviewAddBtn.isEnabled = false // 중복 클릭 방지

        // 3. API 통신
        lifecycleScope.launch {
            try {
                // RetrofitClient 인터페이스에 postRelayReview 가 선언되어 있어야 합니다.
                val response = RetrofitClient.api().postRelayReview(userBookId, request)

                Log.d("ReviewAPI", "Code: ${response.code()}, Body: ${response.body()}")

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(context, "리뷰 작성이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    // 완료 후 이전 화면으로 복귀
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                } else {
                    val msg = response.body()?.message ?: "리뷰 등록에 실패했습니다."
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    binding.libReviewAddBtn.isEnabled = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                binding.libReviewAddBtn.isEnabled = true
            }
        }
    }

    /**
     * 화면의 한글 태그를 서버 전송용 영어(Enum) 코드로 변환
     * TODO: 실제 서버에서 정의한 영문 코드값과 일치하는지 백엔드 명세서를 확인하고 수정하세요!
     */
    private fun mapUiTextToBadgeCode(text: String): String {
        return when (text) {
            "친절하고 매너가 좋아요" -> "KINDNESS" // 명세서 예제에 있던 값
            "글씨가 예뻐요" -> "PRETTY_HANDWRITING"
            "코멘트가 다정해요" -> "SWEET_COMMENT"
            "책에 대한 인사이트가 넘쳐요" -> "GOOD_INSIGHT"
            "책을 빠르게 보내줬어요" -> "FAST_SENDER"
            "코멘트가 재미있어요" -> "FUNNY_COMMENT"
            "책을 깨끗하고 깔끔하게 읽어요" -> "CLEAN_READER"
            "약속을 잘 지켜요" -> "PUNCTUAL"
            else -> "UNKNOWN"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}