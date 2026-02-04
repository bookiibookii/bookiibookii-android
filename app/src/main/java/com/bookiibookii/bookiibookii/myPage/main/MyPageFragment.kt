package com.bookiibookii.bookiibookii.myPage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.MypMyReviewFragment
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.bookData.Data.MypLateBook
import com.bookiibookii.bookiibookii.bookData.Data.MypReview
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
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

    // 4. 데이터 UI 반영 (바인딩 사용)
    private fun updateUI(data: com.bookiibookii.bookiibookii.data.model.MypageResult) {
        // 프로필 카드 업데이트
        with(profileBinding) {
            mypNameTv.text = data.nickname
            mypTempTv.text = "${data.manner}°C"
            mypReadBookTv.text = data.completeBook.toString()
            mypAllBookTv.text = data.books.size.toString()
            mypBookCardTv.text = "0"

            if (!data.userImage?.s3Key.isNullOrEmpty()) {
                Glide.with(root.context).load(data.userImage?.s3Key).circleCrop().into(mypProfileIv)
            } else {
                mypProfileIv.setImageResource(R.drawable.bg_myp_profile)
            }

            // 태그 매핑
            val tagsLayout = mypTagsLayout
            val userTags = data.userImage?.user?.userTags ?: emptyList()

            for (i in 0 until tagsLayout.childCount) {
                val tagView = tagsLayout.getChildAt(i) as? TextView
                if (i < userTags.size) {
                    tagView?.text = "#${userTags[i].tag?.code ?: ""}"
                    tagView?.visibility = View.VISIBLE
                } else {
                    tagView?.visibility = View.GONE
                }
            }
        }

        // 리스트 업데이트
        val reviewList = data.topTags.map { MypReview(content = it, count = 0) }
        binding.mypReviewsRv.adapter = MypReviewAdapter(reviewList)

        binding.mypGroupsRv.adapter = MypGroupAdapter(data.groups)

        val bookList = data.books.map { MypLateBook(title = it.bookTitle, rating = it.rating.toInt()) }
        binding.rvBooks.adapter = MypLateBookAdapter(bookList)
    }

    // 5. API 호출
    private fun fetchMypageData() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(requireContext()).getMypage()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    updateUI(response.body()!!.result)
                } else {
                    Log.e("Mypage", "API Error: ${response.code()}")
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