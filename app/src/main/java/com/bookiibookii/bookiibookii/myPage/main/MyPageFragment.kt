package com.bookiibookii.bookiibookii.myPage.main

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.MypReview
import com.bookiibookii.bookiibookii.data.model.MypageResult
import com.bookiibookii.bookiibookii.databinding.FragmentMypBinding
import com.bookiibookii.bookiibookii.databinding.LayoutMypProfileCardBinding
import com.bookiibookii.bookiibookii.myPage.profile.MypProfileEditFragment
import com.bookiibookii.bookiibookii.myPage.set.MypSetFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.launch

class MypageFragment : Fragment() {

    private var _binding: FragmentMypBinding? = null
    private val binding get() = _binding!!

    private var _profileBinding: LayoutMypProfileCardBinding? = null
    private val profileBinding get() = _profileBinding!!

    private lateinit var loadingDialog: LoadingDialog
    private val viewModel: MyPageViewModel by activityViewModels()
    private var isGroupExpanded = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // include 레이아웃 바인딩 초기화
        _profileBinding = LayoutMypProfileCardBinding.bind(binding.layoutProfile.root)

        loadingDialog = LoadingDialog(requireContext())

        setupRecyclerViews()
        initNavigation()
        initGroupToggle()
        observeViewModel()
        fetchMypageData()
    }

    private fun initNavigation() {
        binding.mypSettingIv.setOnClickListener { navigateToFragment(MypSetFragment()) }
        binding.mypMyReviewIv.setOnClickListener { navigateToFragment(MypMyReviewFragment()) }
        profileBinding.mypEditIv.setOnClickListener { navigateToFragment(MypProfileEditFragment()) }
    }

    private fun initGroupToggle() {
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
            flexWrap = FlexWrap.WRAP // 공간이 모자라면 다음 줄로 넘김
            flexDirection = FlexDirection.ROW // 가로 방향으로 배치
            justifyContent = JustifyContent.FLEX_START // 왼쪽 정렬
        }

        binding.mypGroupsRv.layoutManager = LinearLayoutManager(context)
        binding.mypGroupsRv.isNestedScrollingEnabled = false
        binding.rvBooks.layoutManager = LinearLayoutManager(context)
        binding.rvBooks.isNestedScrollingEnabled = false
    }

    private fun translateBadge(englishText: String): String {
        return when (englishText.uppercase()) {
            "KINDNESS" -> "친절하고 매너가 좋아요"
            "GOOD_HANDWRITING" -> "글씨가 예뻐요"
            "SWEET_COMMENT" -> "코멘트가 다정해요"
            "INSIGHTFUL" -> "책에 대한 인사이트가 넘쳐요"
            "FAST_SHIPPING" -> "책을 빠르게 보내줬어요"
            "FUNNY" -> "코멘트가 재미있어요"
            "CLEAN_CONDITION" -> "책을 깨끗하고 깔끔하게 읽어요"
            "PUNCTUAL" -> "약속을 잘 지켜요"
            "MEMO" -> "메모환영"
            "POSTIT" -> "포스트잇"
            "CLEAN" -> "깔끔"
            "SERIOUS" -> "진지함"
            "LIGHT_FUN" -> "재미있게"
            "INSIGHT" -> "인사이트"
            else -> englishText
        }
    }

    private fun updateUI(data: MypageResult) {
        with(profileBinding) {
            mypNameTv.text = data.nickname
            mypTempTv.text = "${data.manner}°C"
            mypAllBookTv.text = data.completeBook.toString()
            mypReadBookTv.text = data.relayGroup.toString()
            mypBookCardTv.text = data.togetherGroup.toString()

            val imageUrl = data.profileImageUrl

            Glide.with(root.context)
                .load(imageUrl)
                .placeholder(R.drawable.img_profile_default)
                .error(R.drawable.img_profile_default)
                .fallback(R.drawable.img_profile_default)
                .transform(CenterCrop(), RoundedCorners(dpToPx(60)))
                .into(mypProfileIv)

            mypTagsLayout.removeAllViews()
            data.topTags.forEach { tagText ->
                val textView = TextView(root.context).apply {
                    text = "#${translateBadge(tagText)}"
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    setTextColor(ContextCompat.getColor(context, R.color.ui_main_sub))
                    setBackgroundResource(R.drawable.bg_round_8dp_gray300)
                    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.ui_main_sub_pale))

                    val pH = dpToPx(8)
                    val pV = dpToPx(4)
                    setPadding(pH, pV, pH, pV)

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { marginEnd = dpToPx(10) }
                }
                mypTagsLayout.addView(textView)
            }
        }

        val badgeList = data.userBadges?.map {
            MypReview(content = translateBadge(it.userBadge), count = it.count)
        } ?: emptyList()
        binding.mypReviewsRv.adapter = MypReviewAdapter(badgeList)
        binding.mypGroupsRv.adapter = MypGroupAdapter(data.groups ?: emptyList())
        binding.rvBooks.adapter = MypLateBookAdapter(data.books ?: emptyList())
    }

    private fun fetchMypageData() {
        viewLifecycleOwner.lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().getMypage()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()!!.result
                    if (result != null) {
                        updateUI(result)
                    }
                } else {
                    Log.e("Mypage", "API Error: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("Mypage", "Network Error", e)
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.profileData.observe(viewLifecycleOwner) { data ->
            profileBinding.mypNameTv.text = data.nickname
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    override fun onResume() {
        super.onResume()
        fetchMypageData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // ✅ 일반 Fragment이므로 두 바인딩을 모두 직접 메모리에서 해제해 주어야 합니다.
        _profileBinding = null
        _binding = null
    }
}