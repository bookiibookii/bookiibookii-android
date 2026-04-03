package com.bookiibookii.bookiibookii.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.ProfileResult
import com.bookiibookii.bookiibookii.databinding.FragmentOtherProfileBinding
import kotlinx.coroutines.launch
import android.view.animation.Animation
import android.view.animation.RotateAnimation

class OtherProfileFragment : Fragment(R.layout.fragment_other_profile) {

    private var _binding: FragmentOtherProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(ProfileRepository())
    }

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
        observeState()

        viewModel.loadProfile(nickname)
    }

    private var isGroupExpanded = false

    private fun setupGroupToggle() {
        binding.mypGroupIv.setOnClickListener {
            isGroupExpanded = !isGroupExpanded

            val fromDeg = if (isGroupExpanded) 0f else 180f
            val toDeg = if (isGroupExpanded) 180f else 0f
            val anim = RotateAnimation(fromDeg, toDeg,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 200
                fillAfter = true
            }
            binding.mypGroupIv.startAnimation(anim)

            binding.mypGroupsRv.visibility =
                if (isGroupExpanded) View.VISIBLE else View.GONE
        }

        // 초기 상태: 그룹 목록 숨김
        binding.mypGroupsRv.visibility = View.GONE
    }

    private fun setupToolbar(nickname: String) {
        binding.toolbar.title = "${nickname}님의 프로필"
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                state.profile?.let { bindProfile(it) }
            }
        }
    }

    private fun bindProfile(result: ProfileResult) {
        val cardRoot = binding.layoutHomeProfile.root

        val ivProfile = cardRoot.findViewById<android.widget.ImageView>(R.id.myp_profile_iv)
        val tvName = cardRoot.findViewById<android.widget.TextView>(R.id.myp_name_tv)
        val tvTemp = cardRoot.findViewById<android.widget.TextView>(R.id.myp_temp_tv)
        val tagLayout = cardRoot.findViewById<android.widget.LinearLayout>(R.id.myp_tags_layout)

        val tvAllBook = cardRoot.findViewById<android.widget.TextView>(R.id.myp_all_book_tv)
        val tvReadBook = cardRoot.findViewById<android.widget.TextView>(R.id.myp_read_book_tv)
        val tvTogetherBook = cardRoot.findViewById<android.widget.TextView>(R.id.myp_book_card_tv)

        tvName.text = result.nickname
        tvTemp.text = String.format("%.1f", result.manner)

        tvAllBook.text = result.completeBook.toString()
        tvReadBook.text = result.readingGroup.toString()
        tvTogetherBook.text = result.togetherGroup.toString()

        // 태그 2개만
        tagLayout.removeAllViews()
        val tags = result.topTags

        if (tags.isEmpty()) {
            tagLayout.visibility = View.GONE
        } else {
            tagLayout.visibility = View.VISIBLE
            for (i in 0 until minOf(tags.size, 2)) {
                val tv = android.widget.TextView(requireContext()).apply {
                    text = "#${translateTag(tags[i])}"
                    setTextAppearance(R.style.medium14)
                    setTextColor(resources.getColor(R.color.pre_main, null))
                    setPadding(
                        resources.getDimensionPixelSize(R.dimen.spacing_8),
                        resources.getDimensionPixelSize(R.dimen.spacing_4),
                        resources.getDimensionPixelSize(R.dimen.spacing_8),
                        resources.getDimensionPixelSize(R.dimen.spacing_4)
                    )
                    background = resources.getDrawable(R.drawable.bg_round_10dp_gray300, null)
                    backgroundTintList = android.content.res.ColorStateList.valueOf(
                        resources.getColor(R.color.pre_main_pale, null)
                    )
                }

                val lp = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                if (i > 0) lp.marginStart = resources.getDimensionPixelSize(R.dimen.spacing_8)
                tv.layoutParams = lp

                tagLayout.addView(tv)
            }
        }

        // 프로필 이미지 (Coil 쓰면)
        val url = result.profileImageUrl
        if (!url.isNullOrBlank()) {
            // ivProfile.load(url) { placeholder(R.drawable.img_profile_default); error(R.drawable.img_profile_default) }
        } else {
            ivProfile.setImageResource(R.drawable.img_profile_default)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_NICKNAME = "arg_nickname"
    }
}