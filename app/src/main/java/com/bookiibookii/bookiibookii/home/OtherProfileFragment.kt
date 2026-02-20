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
        observeState()

        viewModel.loadProfile(nickname)
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
        // include 된 카드 루트
        val cardRoot = binding.root.findViewById<View>(R.id.layout_home_profile)

        val ivProfile = cardRoot.findViewById<android.widget.ImageView>(R.id.myp_profile_iv)
        val ivEdit = cardRoot.findViewById<android.widget.ImageView>(R.id.myp_edit_iv)

        val tvName = cardRoot.findViewById<android.widget.TextView>(R.id.myp_name_tv)
        val tvTemp = cardRoot.findViewById<android.widget.TextView>(R.id.myp_temp_tv)

        val tagLayout = cardRoot.findViewById<android.widget.LinearLayout>(R.id.myp_tags_layout)

        val tvAllBook = cardRoot.findViewById<android.widget.TextView>(R.id.myp_all_book_tv)
        val tvReadBook = cardRoot.findViewById<android.widget.TextView>(R.id.myp_read_book_tv)
        val tvTogetherBook = cardRoot.findViewById<android.widget.TextView>(R.id.myp_book_card_tv)

        // 다른 유저 프로필이므로 편집 아이콘 숨김
        ivEdit.visibility = View.GONE

        // 이름
        tvName.text = result.nickname

        // 온도/매너 (서버 필드명이 manner로 온다고 했으니 여기 반영)
        // 필요하면 소수점 한 자리로 정리
        tvTemp.text = String.format("%.1f", result.manner)

        // 카운트
        tvAllBook.text = result.completeBook.toString()
        tvReadBook.text = result.readingGroup.toString()
        tvTogetherBook.text = result.togetherGroup.toString()

        // 태그 렌더링 (topTags)
        tagLayout.removeAllViews()
        val tags = result.topTags.orEmpty()

        if (tags.isEmpty()) {
            tagLayout.visibility = View.GONE
        } else {
            tagLayout.visibility = View.VISIBLE

            for (i in 0 until minOf(tags.size, 2)) {
                val tv = android.widget.TextView(requireContext())
                tv.text = "#${tags[i]}"
                tv.setTextAppearance(R.style.medium14)
                tv.setTextColor(resources.getColor(R.color.pre_main, null))

                tv.setPadding(
                    resources.getDimensionPixelSize(R.dimen.spacing_8),
                    resources.getDimensionPixelSize(R.dimen.spacing_4),
                    resources.getDimensionPixelSize(R.dimen.spacing_8),
                    resources.getDimensionPixelSize(R.dimen.spacing_4)
                )
                tv.background = resources.getDrawable(R.drawable.bg_round_10dp_gray300, null)
                tv.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    resources.getColor(R.color.pre_main_pale, null)
                )

                val lp = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                if (i > 0) lp.marginStart = resources.getDimensionPixelSize(R.dimen.spacing_8)
                tv.layoutParams = lp

                tagLayout.addView(tv)
            }
        }

        // 프로필 이미지 로딩 (Coil 이미 쓰는 프로젝트라면)
        // profileImageUrl이 null이면 기본 이미지 유지
        val url = result.profileImageUrl
        if (!url.isNullOrEmpty()) {
            // 예시: coil.load 사용 (의존성/확장함수 존재할 때만)
            // ivProfile.load(url)
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