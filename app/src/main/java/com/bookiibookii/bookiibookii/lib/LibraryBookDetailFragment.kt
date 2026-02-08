package com.bookiibookii.bookiibookii.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.CardItem
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailBinding
import kotlinx.coroutines.launch

class LibraryBookDetailFragment : Fragment() { // 이어읽기용

    private var _binding: FragmentLibBookDetailBinding? = null
    private val binding get() = _binding!!

    private var userBookId: Int = -1
    private var bookTitle = ""
    private var bookAuthor = ""
    private var bookCover = ""

    private lateinit var adapter: LibraryReviewAdapter
    private var cardList: List<CardItem> = emptyList() // 정렬용 원본 데이터

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userBookId = it.getInt("userBookId", -1)
            bookTitle = it.getString("bookTitle", "")
            bookAuthor = it.getString("bookAuthor", "")
            bookCover = it.getString("bookCover", "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initAdapter()
        fetchData()
    }

    private fun initView() {
        binding.libDetailTitleTv.text = bookTitle
        binding.libDetailBookTitleTv.text = bookTitle
        binding.libDetailBookAuthorTv.text = bookAuthor
        Glide.with(this).load(bookCover).into(binding.libDetailImageIv)

        binding.libDetailBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 독서카드 추가 버튼 (빈 화면용 & 리스트 화면용)
        val goAddCard = View.OnClickListener {
            navigateToAddCard()
        }
        binding.libReviewAddBtn.setOnClickListener(goAddCard)
        binding.libReviewNoAddBtn.setOnClickListener(goAddCard)

        // 정렬 버튼
        binding.libDetailLatelyTv.setOnClickListener { sortList(true) } // 최신순
        binding.libDetailPageTv.setOnClickListener { sortList(false) }  // 페이지순
    }

    private fun initAdapter() {
        adapter = LibraryReviewAdapter(
            onItemClick = { item ->
                // 상세 화면 이동 (기존 로직 유지)
                /* ... */
            },
            onBookmarkClick = { item, pos -> /* ... */ }
        )
        binding.libReviewListRv.layoutManager = GridLayoutManager(context, 2)
        binding.libReviewListRv.adapter = adapter
        // Decoration 추가...
    }

    private fun fetchData() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getBookCards(userBookId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    val cards = result?.cards ?: emptyList()
                    cardList = cards // 원본 저장

                    // 첫 번째 카드의 소유권 정보로 내 책 여부 판단 (모든 카드가 동일한 bookOwn을 가진다고 가정)
                    // 만약 카드가 0개라면 API에서 별도로 소유권 정보를 줘야 하지만,
                    // 현재 명세상 card 안에 bookOwn이 있으므로 카드가 없으면 판단 불가.
                    // -> 카드가 없을 땐 LibraryFragment에서 넘겨준 isMine 정보를 쓰거나
                    //    서버 응답의 root 레벨에 bookOwn이 있어야 함.
                    //    ★ 임시: 카드가 있으면 첫 번째 카드의 bookOwn 사용. 없으면 LibraryFragment 정보 사용.

                    val isMyBook = cards.firstOrNull()?.bookOwn?.my ?: true // 기본값 true(호스트)

                    updateUI(cards, isMyBook)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun updateUI(cards: List<CardItem>, isMyBook: Boolean) {
        binding.libDetailTotalTv.text = "${cards.size}개"

        if (cards.isNotEmpty()) {
            // [데이터 있음]
            binding.groupReviewSection.isVisible = true
            binding.bookDetailNoCardHost.isVisible = false
            binding.bookDetailNoCardGuest.isVisible = false

            // 상단 카드 코멘트 (이어읽기 전용 2줄)
            // 첫 번째 카드의 코멘트를 대표로 표시한다고 가정
            val firstCard = cards.first()
            binding.libDetailReviewName1Tv.text = "나의 한줄평" // 혹은 유저 이름
            binding.libDetailReviewText1Tv.text = firstCard.mycomment ?: "-"

            // XML에 partnerComment용 TextView가 없다면 추가 필요.
            // 여기서는 기존 Text1Tv 하나만 있다고 가정하고 이어 붙임 (임시)
            // binding.libDetailReviewText1Tv.text = "나: ${firstCard.mycomment}\n상대: ${firstCard.partnercomment}"

            // 리스트 갱신 (기본 최신순)
            sortList(true)

        } else {
            // [데이터 없음]
            binding.groupReviewSection.isVisible = false

            if (isMyBook) {
                binding.bookDetailNoCardHost.isVisible = true
                binding.bookDetailNoCardGuest.isVisible = false
            } else {
                binding.bookDetailNoCardHost.isVisible = false
                binding.bookDetailNoCardGuest.isVisible = true
            }
        }
    }

    private fun sortList(isLatest: Boolean) {
        val sorted = if (isLatest) {
            cardList.sortedByDescending { it.createdAt }
        } else {
            cardList.sortedBy { it.page }
        }
        // Adapter submitList (CardItem -> LibReview 매핑 필요)
        // adapter.submitList(...)
    }

    private fun navigateToAddCard() {
        val fragment = LibraryAddCardFragment().apply {
            arguments = Bundle().apply { putInt("userBookId", userBookId) }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}