package com.bookiibookii.bookiibookii.lib

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log // ★ 로그 사용
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.GroupItemDto
import com.bookiibookii.bookiibookii.data.model.RelayReviewRequest
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailRelayWriteBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class LibraryBookDetailRelayWriteFragment : Fragment() {

    private var _binding: FragmentLibBookDetailRelayWriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog

    private var userBookId: Int = -1
    private var groupId: Int = -1

    // 상태 저장 변수
    private var bookRating = 0.0
    private var partnerRating = 0.0
    private val selectedTags = mutableSetOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userBookId = it.getInt("userBookId", -1)
            groupId = it.getInt("groupId", -1)
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
        loadingDialog = LoadingDialog(requireContext())

        // 1. 초기 UI 설정 (버튼 비활성화 등)
        updateButtonState()

        // 2. 리스너 등록
        initListener()

        // 3. 서버 데이터 가져오기
        fetchGroupDetail()
    }

    private fun fetchGroupDetail() {
        if (groupId == -1) {
            Toast.makeText(context, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            loadingDialog.show()
            try {
                // 그룹 상세 정보 호출
                val response = RetrofitClient.api().getGroupDetail(groupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()!!.result
                    updateUI(result)
                } else {
                    Toast.makeText(context, "정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }

    private fun updateUI(data: GroupItemDto.GroupDetailResult) {
        // 1. 책 이미지
        Glide.with(this)
            .load(data.bookImage)
            .transform(CenterCrop(), RoundedCorners(dpToPx(10))) // 라운드 처리
            .placeholder(R.drawable.bg_round_20dp_gray200)
            .into(binding.libDetailImageIv)

        // 2. 호스트 정보
        Glide.with(this)
            .load(data.hostProfileImage)
            .placeholder(R.drawable.bg_round_10dp_gray300)
            .error(R.drawable.img_profile_default)
            .circleCrop()
            .into(binding.libDetailProfileIv)
        binding.libDetailProfileTv.text = data.hostNickname

        // 3. 책 정보
        binding.libDetailBookTitleTv.text = data.bookTitle
        binding.libDetailBookAuthorTv.text = data.author

        // 4. 날짜 처리 (UTC StartDate ~ Today)
        val formattedDate = formatDateRange(data.startDate)
        binding.libDetailDateTv.text = formattedDate

        // 5. 책 평가 타이틀 (Spannable 적용)
        val bookQuestion = "${data.bookTitle}에 대한 평가를 남겨주세요!"
        setSpannableColor(binding.libWriteTitleTv, bookQuestion, data.bookTitle)

        // 6. 파트너 평가 타이틀 (ParticipantSlot에서 내가 아닌 사람 찾기)
        val partner = data.participantSlots?.find { !it.isMe }
        val partnerName = partner?.nickname ?: "상대방"
        val partnerQuestion = "$partnerName 님에 대한 평가를 남겨주세요!"
        setSpannableColor(binding.libWritePartnerTv, partnerQuestion, partnerName)
    }

    // 날짜 포맷팅 함수 (UTC -> Local ~ Today)
    private fun formatDateRange(serverDateStr: String): String {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault())

            val startDate = inputFormat.parse(serverDateStr) ?: Date()
            val todayDate = Date()

            return "${outputFormat.format(startDate)} ~ ${outputFormat.format(todayDate)}"

        } catch (e: Exception) {
            e.printStackTrace()
            return "$serverDateStr ~"
        }
    }

    private fun setSpannableColor(textView: TextView, fullText: String, targetWord: String) {
        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf(targetWord)
        if (startIndex != -1) {
            val endIndex = startIndex + targetWord.length
            val color = ContextCompat.getColor(requireContext(), R.color.pre_main)
            spannable.setSpan(ForegroundColorSpan(color), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        textView.text = spannable
    }

    private fun initListener() {
        binding.libDetailBackIv.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // 책 별점 (0.5 단위)
        setupStarRating(binding.libDetailRateList, isBookRating = true)

        // 파트너 별점 (0.5 단위)
        setupStarRating(binding.libPartnerRateList, isBookRating = false)

        // 태그 선택
        setupTagSelection()

        // 작성 완료 버튼
        binding.libReviewAddBtn.setOnClickListener {
            submitReview()
        }
    }

    private fun setupStarRating(container: LinearLayout, isBookRating: Boolean) {
        val stars = ArrayList<ImageView>()
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is ImageView) stars.add(child)
        }

        var currentContainerRating = 0.0

        stars.forEachIndexed { index, starView ->
            starView.setOnClickListener {
                val targetHalf = index + 0.5
                val targetFull = index + 1.0

                currentContainerRating = if (currentContainerRating == targetHalf) targetFull else targetHalf

                if (isBookRating) bookRating = currentContainerRating
                else partnerRating = currentContainerRating

                updateStarUI(stars, currentContainerRating)
                updateButtonState()
            }
        }
    }

    private fun updateStarUI(stars: List<ImageView>, rating: Double) {
        for (i in stars.indices) {
            when {
                rating >= (i + 1.0) -> stars[i].setImageResource(R.drawable.ic_star_filled)
                rating >= (i + 0.5) -> stars[i].setImageResource(R.drawable.ic_star_half)
                else -> stars[i].setImageResource(R.drawable.ic_star_none)
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
            binding.libReviewAddBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey_900))
            binding.libReviewAddBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        } else {
            binding.libReviewAddBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey_200))
            binding.libReviewAddBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_500))
        }
    }

    private fun submitReview() {
        if (userBookId == -1) {
            Toast.makeText(context, "정보가 부족합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val badgeCodes = selectedTags.map { mapUiTextToBadgeCode(it.text.toString()) }

        val request = RelayReviewRequest(
            bookRating = bookRating,
            bookComment = binding.libWriteReviewEt.text.toString(),
            partnerRating = partnerRating,
            partnerComment = binding.libWritePartnerReviewEt.text.toString(),
            badgeCodes = badgeCodes
        )

        // ★ [로그 1] 내가 보내려는 데이터가 정확한지 확인
        Log.d("RelayReview", "======== 리뷰 전송 시도 ========")
        Log.d("RelayReview", "Target userBookId: $userBookId")
        Log.d("RelayReview", "Request Body: $request")

        lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().postRelayReview(userBookId, request)

                // ★ [로그 2] 서버 응답 코드 확인
                Log.d("RelayReview", "Response Code: ${response.code()}")

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d("RelayReview", "성공 Response: ${response.body()}")
                    Toast.makeText(context, "리뷰 작성이 완료되었습니다.", Toast.LENGTH_SHORT).show()

                    requireActivity().supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, LibraryFragment())
                        .commit()
                } else {
                    // ★ [로그 3] 실패 원인 (서버 에러 메시지) 확인
                    val errorBody = response.errorBody()?.string()
                    val msg = response.body()?.message ?: "등록 실패"

                    Log.e("RelayReview", "실패 메시지: $msg")
                    Log.e("RelayReview", "Error Body: $errorBody")

                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // ★ [로그 4] 아예 통신이 안 되거나 터졌을 때 확인
                Log.e("RelayReview", "Exception 발생: ${e.message}")
                e.printStackTrace()
                Toast.makeText(context, "네트워크 오류", Toast.LENGTH_SHORT).show()
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }

    private fun mapUiTextToBadgeCode(text: String): String {
        // ★ [참고] 서버에서 정의한 enum 값과 일치해야 400 에러가 안 납니다.
        return when (text) {
            "친절하고 매너가 좋아요" -> "KINDNESS"
            "글씨가 예뻐요" -> "GOOD_HANDWRITING"
            "코멘트가 다정해요" -> "SWEET_COMMENT"
            "책에 대한 인사이트가 넘쳐요" -> "INSIGHTFUL"
            "책을 빠르게 보내줬어요" -> "FAST_SHIPPING"
            "코멘트가 재미있어요" -> "FUNNY"
            "책을 깨끗하고 깔끔하게 읽어요" -> "CLEAN_CONDITION"
            "약속을 잘 지켜요" -> "PUNCTUAL"
            else -> "UNKNOWN"
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.VISIBLE
        _binding = null
    }
}