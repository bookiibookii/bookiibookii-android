package com.bookiibookii.bookiibookii.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.LibReview
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailTogetherBinding // ★ Together 바인딩
import kotlinx.coroutines.launch

class LibraryBookDetailTogetherFragment : Fragment() {

    private var _binding: FragmentLibBookDetailTogetherBinding? = null
    private val binding get() = _binding!!

    // 데이터
    private var userBookId: Int = -1
    private var bookTitle = ""
    private var bookAuthor = ""
    private var bookCover = ""

    private lateinit var groupReviewAdapter: LibraryCardReviewAdapter
    private lateinit var cardAdapter: LibraryReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userBookId = it.getInt("userBookId", -1)
            bookTitle = it.getString("bookTitle", "") ?: ""
            bookAuthor = it.getString("bookAuthor", "") ?: ""
            bookCover = it.getString("bookCover", "") ?: ""
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // fragment_lib_book_detail_together.xml 사용
        _binding = FragmentLibBookDetailTogetherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initAdapters()
        initListeners()
        fetchData()
        // 이 화면에 진입했다는 것은 이미 리뷰 작성이 완료되었다는 뜻이므로,
        // 내 리뷰 정보를 가져오는 API를 호출하거나 서버가 카드 리스트에 포함해주어야 합니다.
    }

    private fun initView() {
        binding.libDetailTitleTv.text = bookTitle
        binding.libDetailBookTitleTv.text = bookTitle
        binding.libDetailBookAuthorTv.text = bookAuthor
        Glide.with(this).load(bookCover).into(binding.libDetailImageIv)

        // 상단 카드 리뷰 리스트 (초기 숨김)
        binding.bookDetailReviewsRv.visibility = View.GONE
        binding.bookDetailDownArrowIv.rotation = 0f
    }

    private fun initAdapters() {
        groupReviewAdapter = LibraryCardReviewAdapter()
        binding.bookDetailReviewsRv.layoutManager = LinearLayoutManager(context)
        binding.bookDetailReviewsRv.adapter = groupReviewAdapter

        cardAdapter = LibraryReviewAdapter(
            onItemClick = { /* 상세 이동 */ },
            onBookmarkClick = { _, _ -> }
        )
        binding.libReviewListRv.layoutManager = GridLayoutManager(context, 2)
        binding.libReviewListRv.adapter = cardAdapter
        binding.libReviewListRv.addItemDecoration(LibDetailGridDecoration(2, dpToPx(10), dpToPx(20), false))
    }

    private fun fetchData() {
        if (userBookId == -1) return
        lifecycleScope.launch {
            try {
                // 1. 독서카드 데이터
                val response = RetrofitClient.api().getBookCards(userBookId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val apiCards = response.body()?.result?.cards ?: emptyList()
                    val uiList = apiCards.map { card ->
                        LibReview(
                            id = card.cardId.toLong(),
                            userName = "User",
                            content = card.memo,
                            page = card.page,
                            reviewImageUri = card.cardImage?.presignedGetUrl,
                            profileImage = null,
                            isMine = true,
                            isBookmarked = card.isBookmarked,
                            date = card.createdAt
                        )
                    }
                    cardAdapter.submitList(uiList)
                    binding.libDetailTotalTv.text = "${uiList.size}개"
                }

                // 2. 내 리뷰 정보 (별점, 한줄평) 가져오기
                // 별도 API가 없다면 getBookCards 응답이나 책 정보 API에서 가져와서
                // binding.libDetailReviewText1Tv.text = "내 리뷰 내용"
                // binding.libDetailReviewName1Tv.text = "나"
                // binding.libDetailRateList... (별점 세팅)
                // 등을 처리해야 합니다.

            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun initListeners() {
        binding.libDetailBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 화살표 토글
        binding.bookDetailDownArrowIv.setOnClickListener {
            if (binding.bookDetailReviewsRv.isVisible) {
                binding.bookDetailReviewsRv.visibility = View.GONE
                binding.bookDetailDownArrowIv.rotation = 0f
            } else {
                binding.bookDetailReviewsRv.visibility = View.VISIBLE
                binding.bookDetailDownArrowIv.rotation = 180f
            }
        }

        // 카드 추가
        binding.libReviewAddBtn.setOnClickListener {
            val fragment = LibraryAddCardFragment().apply {
                arguments = Bundle().apply { putInt("userBookId", userBookId) }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}