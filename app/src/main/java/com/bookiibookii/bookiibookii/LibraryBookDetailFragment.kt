package com.bookiibookii.bookiibookii

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bookiibookii.bookiibookii.bookData.Data.LibReview
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailBinding

class LibraryBookDetailFragment : Fragment() {

    private var _binding: FragmentLibBookDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var reviewAdapter: LibraryReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initRecyclerView()
        loadDummyData() // 테스트용 데이터
    }

    private fun initView() {
        binding.libDetailBackIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 더보기 버튼 등 다른 리스너 구현
    }

    private fun initRecyclerView() {
        reviewAdapter = LibraryReviewAdapter(emptyList())

        binding.libReviewListRv.apply {
            layoutManager = GridLayoutManager(context, 2)

            adapter = reviewAdapter

            val spacingHorizontal = dpToPx(10)
            val spacingVertical = dpToPx(12)
            addItemDecoration(LibDetailGridDecoration(2, spacingHorizontal, spacingVertical, false))
        }
    }

    private fun loadDummyData() {
        // 더미 데이터 생성
        val dummyList = listOf(
            LibReview("kanghunsim", "여기서 개뿔을.. 도망가길 뭘 도망가 그만 웃겨라ㅜㅜ 아니 그리고 노엘...", null, R.drawable.bg_round_8dp_gray300),
            LibReview("kanghunsim", "여기서 개뿔을.. 도망가길 뭘 도망가 그만 웃겨라ㅜㅜ 아니 그리고 노엘...", null, R.drawable.bg_round_8dp_gray300),
            LibReview("kanghunsim", "여기서 개뿔을.. 도망가길 뭘 도망가 그만 웃겨라ㅜㅜ 아니 그리고 노엘...", null, R.drawable.bg_round_8dp_gray300),
            LibReview("kanghunsim", "여기서 개뿔을.. 도망가길 뭘 도망가 그만 웃겨라ㅜㅜ 아니 그리고 노엘...", null, R.drawable.bg_round_8dp_gray300)
        )

        reviewAdapter.submitList(dummyList)

        binding.libDetailTotalTv.text = "${dummyList.size}개"
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