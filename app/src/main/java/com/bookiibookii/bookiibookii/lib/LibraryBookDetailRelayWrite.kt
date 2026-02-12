package com.bookiibookii.bookiibookii.lib

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback // 백버튼 콜백
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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

class LibraryBookDetailRelayWriteFragment : Fragment() {

    private var _binding: FragmentLibBookDetailRelayWriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog

    private var userBookId: Int = -1
    private var groupId: Int = -1

    private var bookRating = 0.0
    private var partnerRating = 0.0
    private val selectedTags = mutableSetOf<TextView>()

    // ★ [핵심] 전송 완료 여부 체크 변수
    private var isReviewSubmitted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userBookId = it.getInt("userBookId", -1)
            groupId = it.getInt("groupId", -1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibBookDetailRelayWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        updateButtonState()
        initListener()
        handleSystemBackPressed() // 시스템 백버튼 처리
        fetchGroupDetail()
    }

    // ★ 시스템 백버튼(제스처/하단바) 눌렀을 때 로직
    private fun handleSystemBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isReviewSubmitted) {
                    goToLibrary() // 전송 완료 상태면 서재로
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed() // 아니면 그냥 뒤로가기
                }
            }
        })
    }

    // ★ 서재로 이동하며 스택 정리하는 함수
    private fun goToLibrary() {
        parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, LibraryFragment())
            .commit()
    }

    private fun fetchGroupDetail() {
        if (groupId == -1) return
        lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().getGroupDetail(groupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    updateUI(response.body()!!.result)
                }
            } catch (e: Exception) { e.printStackTrace() }
            finally { if (loadingDialog.isShowing) loadingDialog.dismiss() }
        }
    }

    private fun updateUI(data: GroupItemDto.GroupDetailResult) {
        Glide.with(this).load(data.bookImage)
            .transform(CenterCrop(), RoundedCorners(dpToPx(10)))
            .placeholder(R.drawable.bg_round_20dp_gray200).into(binding.libDetailImageIv)

        Glide.with(this).load(data.hostProfileImageUrl)
            .placeholder(R.drawable.bg_round_10dp_gray300).error(R.drawable.img_profile_default)
            .circleCrop().into(binding.libDetailProfileIv)

        binding.libDetailProfileTv.text = data.hostNickname
        binding.libDetailBookTitleTv.text = data.bookTitle
        binding.libDetailBookAuthorTv.text = data.author
        binding.libDetailDateTv.text = formatDateRange(data.startDate)

        setSpannableColor(binding.libWriteTitleTv, "${data.bookTitle}에 대한 평가를 남겨주세요!", data.bookTitle)

        val partner = data.participantSlots?.find { !it.isMe }
        val partnerName = partner?.nickname ?: "상대방"
        setSpannableColor(binding.libWritePartnerTv, "$partnerName 님에 대한 평가를 남겨주세요!", partnerName)
    }

    private fun formatDateRange(serverDateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault())
            val startDate = inputFormat.parse(serverDateStr) ?: Date()
            "${outputFormat.format(startDate)} ~ ${outputFormat.format(Date())}"
        } catch (e: Exception) { "$serverDateStr ~" }
    }

    private fun setSpannableColor(textView: TextView, fullText: String, targetWord: String) {
        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf(targetWord)
        if (startIndex != -1) {
            val endIndex = startIndex + targetWord.length
            spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.pre_main)), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        textView.text = spannable
    }

    private fun initListener() {
        // ★ 뒤로가기 버튼(상단 아이콘) 클릭 시 로직
        binding.libDetailBackIv.setOnClickListener {
            if (isReviewSubmitted) {
                goToLibrary()
            } else {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }

        setupStarRating(binding.libDetailRateList, isBookRating = true)
        setupStarRating(binding.libPartnerRateList, isBookRating = false)
        setupTagSelection()

        binding.libReviewAddBtn.setOnClickListener { submitReview() }
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
                if (isReviewSubmitted) return@setOnClickListener // 전송 후 수정 방지

                val targetHalf = index + 0.5
                val targetFull = index + 1.0
                currentContainerRating = if (currentContainerRating == targetHalf) targetFull else targetHalf
                if (isBookRating) bookRating = currentContainerRating else partnerRating = currentContainerRating
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
        if (isReviewSubmitted) return // 전송 후 수정 방지

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
        if (isReviewSubmitted) {
            binding.libReviewAddBtn.isEnabled = false
            return
        }

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
        if (userBookId == -1) return

        val badgeCodes = selectedTags.map { mapUiTextToBadgeCode(it.text.toString()) }
        val request = RelayReviewRequest(
            bookRating = bookRating,
            bookComment = binding.libWriteReviewEt.text.toString(),
            partnerRating = partnerRating,
            partnerComment = binding.libWritePartnerReviewEt.text.toString(),
            badgeCodes = badgeCodes
        )

        loadingDialog.show()

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().postRelayReview(userBookId, request)

                if (loadingDialog.isShowing) loadingDialog.dismiss()
                if (!isAdded || activity == null) return@launch

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(context, "리뷰 작성이 완료되었습니다.", Toast.LENGTH_SHORT).show()

                    // ★ [수정] 복잡한 이동 로직 제거 -> 단순히 뒤로가기
                    // 서재 화면이 onResume 등에서 데이터를 다시 불러오도록 설계되어 있다면 목록이 갱신됩니다.
                    // 만약 갱신이 필요하다면 setFragmentResult를 사용합니다.
                    requireActivity().supportFragmentManager.setFragmentResult("REFRESH_LIBRARY", Bundle())
                    requireActivity().supportFragmentManager.popBackStack()

                } else {
                    val msg = response.body()?.message ?: "등록 실패"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
                e.printStackTrace()
                Toast.makeText(context, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun mapUiTextToBadgeCode(text: String): String {
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