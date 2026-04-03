package com.bookiibookii.bookiibookii.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.MypReview
import com.bookiibookii.bookiibookii.data.model.ProfileResult
import com.bookiibookii.bookiibookii.databinding.FragmentOtherProfileBinding
import com.bookiibookii.bookiibookii.myPage.main.MypGroupAdapter
import com.bookiibookii.bookiibookii.myPage.main.MypLateBookAdapter
import com.bookiibookii.bookiibookii.myPage.main.MypReviewAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OtherProfileFragment : Fragment(R.layout.fragment_other_profile) {

    private var _binding: FragmentOtherProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(ProfileRepository())
    }

    private var isGroupExpanded = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOtherProfileBinding.bind(view)

        val nickname = arguments?.getString(ARG_NICKNAME).orEmpty()
        if (nickname.isEmpty()) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        setupToolbar(nickname)
        setupGroupToggle()
        setupRecyclerViews()
        observeState()

        viewModel.loadProfile(nickname)
    }

    private fun setupToolbar(nickname: String) {
        binding.toolbar.title = "${nickname}님의 프로필"
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupGroupToggle() {
        // 초기 상태: 마이페이지와 동일하게 펼침
        binding.mypGroupsRv.visibility = View.VISIBLE
        binding.mypGroupIv.rotation = 0f

        binding.mypGroupIv.setOnClickListener {
            isGroupExpanded = !isGroupExpanded
            if (isGroupExpanded) {
                binding.mypGroupsRv.visibility = View.VISIBLE
                binding.mypGroupIv.animate().rotation(0f).setDuration(200).start()
            } else {
                binding.mypGroupsRv.visibility = View.GONE
                binding.mypGroupIv.animate().rotation(180f).setDuration(200).start()
            }
        }
    }

    private fun setupRecyclerViews() {
        binding.mypReviewsRv.layoutManager = FlexboxLayoutManager(context).apply {
            flexWrap = FlexWrap.WRAP
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.FLEX_START
        }
        binding.mypGroupsRv.layoutManager = LinearLayoutManager(context)
        binding.mypGroupsRv.isNestedScrollingEnabled = false
        binding.rvBooks.layoutManager = LinearLayoutManager(context)
        binding.rvBooks.isNestedScrollingEnabled = false
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                state.profile?.let { bindProfile(it) }
            }
        }
    }

    private fun bindProfile(result: ProfileResult) {
        val cardBinding = binding.layoutHomeProfile

        // 이름
        cardBinding.mypNameTv.text = result.nickname

        // 매너온도 (°C 포함, 마이페이지와 동일)
        cardBinding.mypTempTv.text = "${String.format("%.1f", result.manner)}°C"

        // 책 통계
        cardBinding.mypAllBookTv.text = result.completeBook.toString()
        cardBinding.mypReadBookTv.text = result.readingGroup.toString()
        cardBinding.mypBookCardTv.text = result.togetherGroup.toString()

        // 태그 (마이페이지와 동일한 색상 적용)
        cardBinding.mypTagsLayout.removeAllViews()
        val tags = result.topTags

        if (tags.isEmpty()) {
            cardBinding.mypTagsLayout.visibility = View.GONE
        } else {
            cardBinding.mypTagsLayout.visibility = View.VISIBLE
            tags.forEach { tagText ->
                val tv = TextView(requireContext()).apply {
                    text = "#${translateTag(tagText)}"
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.ui_main_sub))
                    setBackgroundResource(R.drawable.bg_round_8dp_gray300)
                    backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.ui_main_sub_pale)
                    )
                    val pH = dpToPx(8)
                    val pV = dpToPx(4)
                    setPadding(pH, pV, pH, pV)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { marginEnd = dpToPx(10) }
                }
                cardBinding.mypTagsLayout.addView(tv)
            }
        }

        // 프로필 이미지 (마이페이지와 동일하게 Glide 사용)
        Glide.with(this)
            .load(result.profileImageUrl)
            .placeholder(R.drawable.img_profile_default)
            .error(R.drawable.img_profile_default)
            .fallback(R.drawable.img_profile_default)
            .transform(CenterCrop(), RoundedCorners(dpToPx(60)))
            .into(cardBinding.mypProfileIv)

        // 획득한 후기
        val badgeList = result.userBadges.map {
            MypReview(content = translateBadge(it.userBadge), count = it.count)
        }
        binding.mypReviewsRv.adapter = MypReviewAdapter(badgeList)

        // 주최한 그룹
        binding.mypGroupsRv.adapter = MypGroupAdapter(result.groups)

        // 최근 읽은 책
        binding.rvBooks.adapter = MypLateBookAdapter(result.books)
    }

    private fun translateBadge(raw: String): String {
        return when (raw.trim().uppercase()) {
            "KINDNESS" -> "친절하고 매너가 좋아요"
            "GOOD_HANDWRITING" -> "글씨가 예뻐요"
            "SWEET_COMMENT" -> "코멘트가 다정해요"
            "INSIGHTFUL" -> "책에 대한 인사이트가 넘쳐요"
            "FAST_SHIPPING" -> "책을 빠르게 보내줬어요"
            "FUNNY" -> "코멘트가 재미있어요"
            "CLEAN_CONDITION" -> "책을 깨끗하고 깔끔하게 읽어요"
            "PUNCTUAL" -> "약속을 잘 지켜요"
            else -> raw
        }
    }

    private fun translateTag(raw: String): String {
        return when (raw.trim().uppercase()) {
            "MEMO" -> "메모환영"
            "POSTIT" -> "포스트잇"
            "CLEAN" -> "깔끔"
            "SERIOUS" -> "진지함"
            "LIGHT_FUN" -> "재미있게"
            "INSIGHT" -> "인사이트"
            else -> raw
        }
    }

    private fun dpToPx(dp: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        resources.displayMetrics
    ).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_NICKNAME = "arg_nickname"
    }
}
