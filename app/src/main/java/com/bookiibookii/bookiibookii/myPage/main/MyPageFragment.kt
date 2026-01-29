package com.bookiibookii.bookiibookii.myPage.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.MypMyReviewFragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.MypGroup
import com.bookiibookii.bookiibookii.bookData.Data.MypLateBook
import com.bookiibookii.bookiibookii.bookData.Data.MypReview
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.databinding.FragmentMypBinding
import com.bookiibookii.bookiibookii.databinding.FragmentMypSetBinding
import com.bookiibookii.bookiibookii.myPage.profile.MypSetFragment

class MypageFragment : Fragment() {

    private var _binding: FragmentMypBinding? = null
    private val binding get() = _binding!!

    private val viewModel : MyPageViewModel by activityViewModels()
    // 그룹 리스트가 펼쳐져 있는지 확인하는 변수
    private var isGroupExpanded = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initReviewRecyclerView()    // 1. 획득한 후기
        initGroupRecyclerView()     // 2. 주최한 그룹
        initLateBookRecyclerView()  // 3. 최근 읽은 책

        // 접기/펴기 기능 초기화
        initGroupToggle()
        initNavigation()
        observeViewModel()
//        with(binding.layoutProfile) {
//            mypNameTv.text = "noshel"
//            mypTempTv.text = "37.5°C"
//            mypAllBookTv.text = "7"
//            mypReadBookTv.text = "5"
//            mypBookCardTv.text = "16"
//        }
    }

    override fun onResume() {
        super.onResume()
        setBottomNavVisibility(true)
    }

    private fun initNavigation(){
        binding.mypTopLayout.findViewById<View>(R.id.myp_setting_iv).setOnClickListener {
            navigateToFragment(MypSetFragment())
        }
        binding.mypMyReviewIv.findViewById<View>(R.id.myp_my_review_iv).setOnClickListener {
            navigateToFragment(MypMyReviewFragment())
        }
    }

    // 접기/펴기 로직 함수
    private fun initGroupToggle() {
        binding.mypGroupIv.setOnClickListener {
            // 1. 상태 반전 (true -> false, false -> true)
            isGroupExpanded = !isGroupExpanded

            if (isGroupExpanded) {
                // 펼치기 상태
                binding.mypGroupsRv.visibility = View.VISIBLE

                // 화살표 원상복구 (0도 회전)
                binding.mypGroupIv.animate().rotation(0f).setDuration(200).start()
            } else {
                // 접기 상태
                binding.mypGroupsRv.visibility = View.GONE

                // 화살표 뒤집기 (180도 회전)
                binding.mypGroupIv.animate().rotation(180f).setDuration(200).start()
            }
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setBottomNavVisibility(visible: Boolean) {
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNav)
        bottomNav?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun observeViewModel() {
        viewModel.profileData.observe(viewLifecycleOwner) { data ->
            binding.layoutProfile.mypNameTv.text = data.nickname
        }
    }


    // 1. 획득한 후기
    private fun initReviewRecyclerView() {
        val reviewList = listOf(
            MypReview("친절하고 매너가 좋아요", 8),
            MypReview("응답이 빨라요", 5),
            MypReview("시간 약속을 잘 지켜요", 3),
            MypReview("설명이 자세해요", 2)
        )

        val adapter = MypReviewAdapter(reviewList)
        binding.mypReviewsRv.apply {
            this.adapter = adapter
            layoutManager = GridLayoutManager(requireContext(), 2)
            if (itemDecorationCount > 0) removeItemDecorationAt(0)
            val hSpace = dpToPx(12)
            val vSpace = dpToPx(8)
            addItemDecoration(MypReviewGridDecoration(2, hSpace, vSpace, false))
        }
    }

    // 2. 주최한 그룹
    private fun initGroupRecyclerView() {
        val groupList = listOf(
            MypGroup("괴테는 모든 것을 말했다", "스즈키 유이", true, listOf("인사이트", "메모환영")),
            MypGroup("총 균 쇠", "제러드 다이아몬드", false, listOf("벽돌깨기", "완독도전")),
            MypGroup("코스모스", "칼 세이건", false, listOf("과학", "천문학", "토론"))
        )

        val adapter = MypGroupAdapter(groupList)
        binding.mypGroupsRv.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    // 3. 최근 읽은 책
    private fun initLateBookRecyclerView() {
        val bookList = listOf(
            MypLateBook("해리포터와 마법사의 돌", 5),
            MypLateBook("자바의 정석", 3),
            MypLateBook("클린 코드", 4)
        )

        val adapter = MypLateBookAdapter(bookList)
        binding.rvBooks.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}