package com.bookiibookii.bookiibookii.myPage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.bookData.Data.MypLateBook
import com.bookiibookii.bookiibookii.bookData.Data.MypReview
import com.bookiibookii.bookiibookii.databinding.FragmentMypBinding
import com.bookiibookii.bookiibookii.databinding.LayoutMypProfileCardBinding
import com.bookiibookii.bookiibookii.myPage.main.MypGroupAdapter
import com.bookiibookii.bookiibookii.myPage.main.MypLateBookAdapter
import com.bookiibookii.bookiibookii.myPage.main.MypReviewAdapter
import com.bookiibookii.bookiibookii.myPage.set.MypSetFragment
import kotlinx.coroutines.launch

class MypageFragment : Fragment() {

    private var _binding: FragmentMypBinding? = null
    private val binding get() = _binding!!

    // 프로필 카드 바인딩
    private var _profileBinding: LayoutMypProfileCardBinding? = null
    private val profileBinding get() = _profileBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypBinding.inflate(inflater, container, false)
        // include 레이아웃 바인딩 연결
        _profileBinding = LayoutMypProfileCardBinding.bind(binding.layoutProfile.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        //fetchMypageData() //삭제권장

        binding.mypSettingIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MypSetFragment())
                .addToBackStack(null) // 뒤로가기 하면 다시 마이페이지로 오기 위해 필수
                .commit()
        }
    }

    private fun setupRecyclerViews() {
        binding.mypReviewsRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.mypGroupsRv.layoutManager = LinearLayoutManager(context)
        binding.rvBooks.layoutManager = LinearLayoutManager(context)
    }

    private fun fetchMypageData() {
        lifecycleScope.launch {
            try {
                // [수정 전] 옛날 방식 (토큰 없음)
                // val response = RetrofitClient.apiService.getMypage()

                // [수정 후] ★★★ requireContext()를 넣어서 토큰 기능 활성화! ★★★
                val response = RetrofitClient.getInstance(requireContext()).getMypage()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()!!.result
                    updateUI(result)
                } else {
                    Log.e("Mypage", "API Error: ${response.code()}") // 이제 200이 뜰 겁니다!
                }
            } catch (e: Exception) {
                Log.e("Mypage", "Network Error", e)
            }
        }
    }

    private fun updateUI(data: com.bookiibookii.bookiibookii.data.model.MypageResult) {
        // 1. 프로필 카드 설정 (제공해주신 XML ID 사용)
        with(profileBinding) {
            // 닉네임 & 매너온도
            mypNameTv.text = data.nickname
            mypTempTv.text = "${data.manner}°C"

            // 통계 (전체, 완독, 독서카드)
            // API에 '전체', '독서카드' 데이터가 명확하지 않아 '완독'만 매핑하고 나머지는 임시 처리
            mypReadBookTv.text = data.completeBook.toString() // 완독

            // '전체'는 최근 읽은 책 개수 혹은 0으로 표시 (필요시 로직 변경)
            mypAllBookTv.text = data.books.size.toString()

            // '독서카드'는 데이터가 없어 0으로 표시 (필요시 로직 변경)
            mypBookCardTv.text = "0"

            // 프로필 이미지 (Glide)
            if (!data.userImage?.s3Key.isNullOrEmpty()) {
                Glide.with(root.context)
                    .load(data.userImage?.s3Key)
                    .circleCrop()
                    .into(mypProfileIv)
            }

            // 프로필 하단 태그 (#인사이트, #깔끔 등)
            val tagsLayout = mypTagsLayout
            val userTags = data.userImage?.user?.userTags ?: emptyList()

            // XML에 있는 3개의 TextView를 순회하며 태그 입력
            for (i in 0 until tagsLayout.childCount) {
                val tagView = tagsLayout.getChildAt(i) as? TextView
                if (i < userTags.size) {
                    tagView?.text = "#${userTags[i].tag?.code ?: ""}"
                    tagView?.visibility = View.VISIBLE
                } else {
                    tagView?.visibility = View.GONE // 데이터 없으면 숨김
                }
            }
        }

        // 2. 획득한 후기 리스트
        val reviewList = data.topTags.map { MypReview(content = it, count = 0) }
        binding.mypReviewsRv.adapter = MypReviewAdapter(reviewList)

        // 3. 주최한 그룹 리스트
        binding.mypGroupsRv.adapter = MypGroupAdapter(data.groups)

        // 4. 최근 읽은 책 리스트
        val bookList = data.books.map { MypLateBook(title = it.bookTitle, rating = it.rating.toInt()) }
        binding.rvBooks.adapter = MypLateBookAdapter(bookList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _profileBinding = null
    }

    override fun onResume() {
        super.onResume()
        fetchMypageData() // 데이터 갱신

        // 설정 화면에서 숨겼던 하단바를, 마이페이지 돌아오면 다시 보이게!
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNav) // 혹은 R.id.bottomNav
        bottomNav?.visibility = View.VISIBLE}
}