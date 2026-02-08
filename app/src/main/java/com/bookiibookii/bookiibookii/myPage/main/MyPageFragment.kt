package com.bookiibookii.bookiibookii.myPage

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
import com.bookiibookii.bookiibookii.MypMyReviewFragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.MypReview
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.MypageResult
import com.bookiibookii.bookiibookii.databinding.FragmentMypBinding
import com.bookiibookii.bookiibookii.databinding.LayoutMypProfileCardBinding
import com.bookiibookii.bookiibookii.myPage.main.MypGroupAdapter
import com.bookiibookii.bookiibookii.myPage.main.MypLateBookAdapter
import com.bookiibookii.bookiibookii.myPage.main.MypReviewAdapter
import com.bookiibookii.bookiibookii.myPage.profile.MypProfileEditFragment
import com.bookiibookii.bookiibookii.myPage.set.MypSetFragment
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class MypageFragment : Fragment() {

    private var _binding: FragmentMypBinding? = null
    private val binding get() = _binding!!

    // 프로필 카드 바인딩 (include 레이아웃)
    private var _profileBinding: LayoutMypProfileCardBinding? = null
    private val profileBinding get() = _profileBinding!!

    private val viewModel: MyPageViewModel by activityViewModels()
    private var isGroupExpanded = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypBinding.inflate(inflater, container, false)

        // include된 레이아웃 바인딩
        _profileBinding = LayoutMypProfileCardBinding.bind(binding.layoutProfile.root)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        initNavigation()    // 버튼 클릭 리스너
        initGroupToggle()   // 접기/펴기 로직
        observeViewModel()  // [복구됨] 뷰모델 관찰
        fetchMypageData()   // 서버 데이터 요청
    }

    // 1. 네비게이션 설정 (기존 유지)
    private fun initNavigation() {
        binding.mypSettingIv.setOnClickListener {
            navigateToFragment(MypSetFragment())
        }

        binding.mypMyReviewIv.setOnClickListener {
            navigateToFragment(MypMyReviewFragment())
        }

        profileBinding.mypEditIv.setOnClickListener {
            navigateToFragment(MypProfileEditFragment())
        }
    }

    // 2. 그룹 리스트 접기/펴기 (기존 유지)
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

    // 3. 리사이클러뷰 설정 (기존 유지)
    private fun setupRecyclerViews() {
        binding.mypReviewsRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.mypGroupsRv.layoutManager = LinearLayoutManager(context)
        binding.mypGroupsRv.isNestedScrollingEnabled = false // 스크롤 중첩 방지

        binding.rvBooks.layoutManager = LinearLayoutManager(context)
        binding.rvBooks.isNestedScrollingEnabled = false // 스크롤 중첩 방지
    }

    // 4. API 데이터로 UI 업데이트 (이미지 로드 추가됨)
    private fun updateUI(data: MypageResult) {
        // [Profile Layout Binding]
        with(profileBinding) {
            mypNameTv.text = data.nickname
            mypTempTv.text = "${data.manner}°C"
            mypAllBookTv.text = data.completeBook.toString()
            mypReadBookTv.text = data.relayGroup.toString()
            mypBookCardTv.text = data.togetherGroup.toString()

            // [추가된 부분] 이미지 로드 (Glide)
            val imageUrl = data.userImage?.s3Key
            Glide.with(root.context)
                .load(imageUrl)
                .placeholder(R.drawable.img_profile_default) // 기본 이미지
                .error(R.drawable.img_profile_default)       // 에러 시 기본 이미지
                .fallback(R.drawable.img_profile_default)    // null일 때 기본 이미지
                .circleCrop()
                .into(mypProfileIv)

            // 프로필 태그 동적 추가 (기존 로직 + dpToPx 적용)
            mypTagsLayout.removeAllViews()
            data.topTags.forEach { tagText ->
                val textView = TextView(root.context).apply {
                    text = "#$tagText"
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f) // 12sp로 명시
                    setTextColor(ContextCompat.getColor(context, R.color.ui_main_sub))
                    setBackgroundResource(R.drawable.bg_round_8dp_gray300)
                    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.ui_main_sub_pale))

                    // 패딩 (helper 함수 사용)
                    val pH = dpToPx(8)
                    val pV = dpToPx(4)
                    setPadding(pH, pV, pH, pV)

                    // 마진
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd = dpToPx(10)
                    }
                }
                mypTagsLayout.addView(textView)
            }
        }

        // 획득한 후기 리스트 매핑 (UserBadge -> MypReview)
        val badgeList = data.userBadge?.map {
            MypReview(content = it.userBadge, count = it.count)
        } ?: emptyList()
        binding.mypReviewsRv.adapter = MypReviewAdapter(badgeList)

        // 주최한 그룹 & 최근 읽은 책 (null 안전 처리)
        binding.mypGroupsRv.adapter = MypGroupAdapter(data.groups ?: emptyList())
        binding.rvBooks.adapter = MypLateBookAdapter(data.books ?: emptyList())
    }

    // 5. API 호출 (기존 유지 + null 체크 강화)
    private fun fetchMypageData() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getMypage()
                Log.d("MYPAGE_DEBUG", "전체 응답: ${response.body()}")

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
            }
        }
    }

    // [복구됨] 뷰모델 관찰
    private fun observeViewModel() {
        viewModel.profileData.observe(viewLifecycleOwner) { data ->
            // 프로필 수정 등 로컬 변경사항이 있을 때 즉시 반영
            profileBinding.mypNameTv.text = data.nickname
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    // dp -> px 변환 유틸 함수 (기존 유지)
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.VISIBLE
        fetchMypageData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _profileBinding = null
    }
}