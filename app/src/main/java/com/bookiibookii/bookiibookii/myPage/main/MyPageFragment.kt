package com.bookiibookii.bookiibookii.myPage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.MypMyReviewFragment
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.MypLateBook
import com.bookiibookii.bookiibookii.bookData.Data.MypReview
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentMypBinding
import com.bookiibookii.bookiibookii.databinding.LayoutMypProfileCardBinding
import com.bookiibookii.bookiibookii.myPage.main.MypGroupAdapter
import com.bookiibookii.bookiibookii.myPage.main.MypLateBookAdapter
import com.bookiibookii.bookiibookii.myPage.main.MypReviewAdapter
import com.bookiibookii.bookiibookii.myPage.profile.MypProfileEditFragment
import com.bookiibookii.bookiibookii.myPage.set.MypSetFragment

import kotlinx.coroutines.launch

class MypageFragment : Fragment() {

    private var _binding: FragmentMypBinding? = null
    private val binding get() = _binding!!

    private var _profileBinding: LayoutMypProfileCardBinding? = null
    private val profileBinding get() = _profileBinding!!

    private val viewModel: MyPageViewModel by activityViewModels()
    private var isGroupExpanded = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypBinding.inflate(inflater, container, false)

        _profileBinding = LayoutMypProfileCardBinding.bind(binding.layoutProfile.root)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        initNavigation()    // 버튼 클릭 리스너
        initGroupToggle()   // 접기/펴기 로직
        observeViewModel()  // 뷰모델 관찰
        fetchMypageData()   // 서버 데이터 요청
    }

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

        // [그룹 더보기] (화살표는 initGroupToggle에서 처리)
        // 만약 화살표 말고 '주최한 그룹' 글자 클릭 시 이동이라면 여기에 추가
    }

    // 2. 그룹 리스트 접기/펴기
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

    // 3. 리사이클러뷰 설정
    private fun setupRecyclerViews() {
        binding.mypReviewsRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.mypGroupsRv.layoutManager = LinearLayoutManager(context)
        binding.mypGroupsRv.isNestedScrollingEnabled = false // 스크롤 중첩 방지

        binding.rvBooks.layoutManager = LinearLayoutManager(context)
        binding.rvBooks.isNestedScrollingEnabled = false // 스크롤 중첩 방지
    }

    private fun updateUI(data: com.bookiibookii.bookiibookii.data.model.MypageResult) {
        // [Profile Layout Binding]
        with(profileBinding) {
            mypNameTv.text = data.nickname
            mypTempTv.text = "${data.manner}°C"

            // 이미지 로드
            if (!data.userImage?.s3Key.isNullOrEmpty()) {
                Glide.with(root.context).load(data.userImage?.s3Key).circleCrop().into(mypProfileIv)
            } else {
                mypProfileIv.setImageResource(R.drawable.bg_myp_profile)
            }

            mypAllBookTv.text = data.completeBook.toString()
            mypReadBookTv.text = data.relayGroup.toString()
            mypBookCardTv.text = data.togetherGroup.toString()

            // 프로필 태그 (topTags -> myp_tags_layout) 동적 추가
            mypTagsLayout.removeAllViews() // 기존 뷰 제거
            data.topTags.forEach { tagText ->
                val textView = TextView(root.context).apply {
                    text = "#$tagText"

                    textSize = 12f
                    setTextColor(ContextCompat.getColor(context, R.color.grey_900))

                    // 배경 설정
                    setBackgroundResource(R.drawable.bg_round_20dp_white)

                    // 패딩
                    setPadding(dpToPx(20), dpToPx(12), dpToPx(20), dpToPx(12))

                    // 마진
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd = dpToPx(12)
                    }
                    layoutParams = params
                }
                mypTagsLayout.addView(textView)
            }
        }

        // 4. 획득한 후기 리스트 (userBadges 사용)
        val badgeList = data.userBadge?.map {
            // MypReview 객체로 변환 (기존 Adapter 사용을 위해)
            MypReview(content = it.userBadge, count = it.count)
        } ?: emptyList()

        binding.mypReviewsRv.adapter = MypReviewAdapter(badgeList)

        // 5. 주최한 그룹 & 최근 읽은 책
        binding.mypGroupsRv.adapter = MypGroupAdapter(data.groups)
        binding.rvBooks.adapter = MypLateBookAdapter(data.books)
    }

    // dp -> px 변환 유틸 함수 (Fragment 내에 추가하거나 유틸 클래스 사용)
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    // 5. API 호출
    private fun fetchMypageData() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getMypage()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()!!.result
                    // 데이터가 null이 아닐 때 UI 업데이트
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

    private fun observeViewModel() {
        viewModel.profileData.observe(viewLifecycleOwner) { data ->
            profileBinding.mypNameTv.text = data.nickname
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
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