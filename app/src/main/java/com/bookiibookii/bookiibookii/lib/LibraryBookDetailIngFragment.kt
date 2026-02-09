package com.bookiibookii.bookiibookii.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.LibReview
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailIngBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LibraryBookDetailIngFragment : Fragment() {

    private var _binding: FragmentLibBookDetailIngBinding? = null
    private val binding get() = _binding!!

    // 데이터
    private var userBookId: Int = -1
    private var bookTitle = ""
    private var bookAuthor = ""
    private var bookCover = ""

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
        _binding = FragmentLibBookDetailIngBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initRecyclerView()
        initListeners()
        fetchCardData() // 독서카드 목록 조회

        // 뷰가 그려진 후 프로그레스 바 점 위치 계산 (기존 기능)
        binding.readingProgressBar.post {
            updateProgressDots()
        }
    }

    private fun initView() {
        binding.libDetailTitleTv.text = bookTitle
        binding.libDetailBookTitleTv.text = bookTitle
        binding.libDetailBookAuthorTv.text = bookAuthor
        Glide.with(this).load(bookCover).into(binding.libDetailImageIv)

        // 프로그레스바 초기값 (더미 혹은 API 연동)
        binding.readingProgressBar.progress = 79
        binding.readingProgressBar.secondaryProgress = 81
        binding.myPercent.text = "나의 독서율"

        // ★ [버튼 초기 상태]: '다 읽었어요' 보임, '후기 작성' 숨김
        binding.libWriteDoneBtn.visibility = View.VISIBLE
        binding.libWriteReviewBtn.visibility = View.GONE

        // 카드 추가 버튼은 카드가 있을 때만 보이도록 초기엔 숨김 처리
        binding.libReviewAddBtn.visibility = View.GONE
    }

    private fun initRecyclerView() {
        cardAdapter = LibraryReviewAdapter(
            onItemClick = { clickedItem ->
                val detailFragment = LibraryCardDetailFragment().apply {
                    arguments = Bundle().apply {
                        putLong("cardId", clickedItem.id)
                        putBoolean("isMine", clickedItem.isMine)
                        putString("writerName", clickedItem.userName)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, detailFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onBookmarkClick = { item, position ->
                item.isBookmarked = !(item.isBookmarked ?: false)
                cardAdapter.notifyItemChangedAt(position)
            }
        )

        binding.libReviewListRv.layoutManager = GridLayoutManager(context, 2)
        binding.libReviewListRv.adapter = cardAdapter
        binding.libReviewListRv.addItemDecoration(LibDetailGridDecoration(2, dpToPx(10), dpToPx(12), false))
    }

    private fun initListeners() {
        binding.libDetailBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // [1] 다 읽었어요 -> 확인 -> 후기 작성 버튼 노출
        binding.libWriteDoneBtn.setOnClickListener {
            CommonDialog(
                context = requireContext(),
                title = "독서 종료",
                subtitle = "",
                content = "이 책을 다 읽으셨나요?\n종료 후에는 되돌릴 수 없어요.",
                confirmBtnText = "확인",
                confirmBtnColor = R.color.grey_900,
                onConfirmClick = { finishReadingLogic() }
            ).show()
        }

        // [2] 후기 작성하기 -> WriteReviewFragment 이동
        binding.libWriteReviewBtn.setOnClickListener {
            val writeFragment = LibraryWriteReviewFragment().apply {
                arguments = Bundle().apply {
                    putInt("userBookId", userBookId)
                    putString("bookTitle", bookTitle)
                    putString("bookAuthor", bookAuthor)
                    putString("bookCover", bookCover)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, writeFragment)
                .addToBackStack(null)
                .commit()
        }

        // [3] 독서카드 추가
        binding.libReviewAddBtn.setOnClickListener {
            val fragment = LibraryAddCardFragment().apply {
                arguments = Bundle().apply { putInt("userBookId", userBookId) }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }

        // 더보기 버튼 (삭제 등)
        binding.libDetailMoreIv.setOnClickListener {
            LibraryGroupDeleteBottomSheet { /* 삭제 로직 */ }.show(parentFragmentManager, "GroupDeleteSheet")
        }
    }

    private fun finishReadingLogic() {
        binding.readingProgressBar.progress = 100
        binding.readingProgressBar.post { updateProgressDots() }

        // ★ 버튼 교체 로직
        binding.libWriteDoneBtn.visibility = View.GONE
        binding.libWriteReviewBtn.visibility = View.VISIBLE

        // 카드 추가 버튼도 활성화 (필요하다면)
        binding.libReviewAddBtn.visibility = View.VISIBLE

        val currentDate = SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault()).format(Date())
        val originalText = binding.libDetailDateTv.text.toString()
        binding.libDetailDateTv.text = "$originalText $currentDate"
    }

    private fun fetchCardData() {
        if (userBookId == -1) return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getBookCards(userBookId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    val apiCards = result?.cards ?: emptyList()

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
                            date = card.createdAt // ★ Date 파라미터 매핑 필수
                        )
                    }
                    cardAdapter.submitList(uiList)
                    binding.libDetailTotalTv.text = "${uiList.size}개"

                    if (uiList.isNotEmpty()) {
                        binding.libReviewAddBtn.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun updateProgressDots() {
        if (_binding == null) return
        val progressBar = binding.readingProgressBar
        val width = progressBar.width.toFloat()
        val max = progressBar.max.toFloat()
        val marginPixel = dpToPx(1).toFloat()

        val myProgress = progressBar.progress.toFloat()
        val myDot = binding.myProgressDot
        val myX = (width * (myProgress / max)) - marginPixel - myDot.width
        myDot.translationX = myX.coerceAtLeast(0f)

        val groupProgress = progressBar.secondaryProgress.toFloat()
        val groupDot = binding.groupAvgDot
        val groupX = (width * (groupProgress / max)) - marginPixel - groupDot.width
        groupDot.translationX = groupX.coerceAtLeast(0f)
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}