package com.bookiibookii.bookiibookii.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.ReviewModel
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailBinding

class LibraryBookDetailFragment : Fragment() {

    private var _binding: FragmentLibBookDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReviewModel by activityViewModels()

    // 어댑터 2종
    private lateinit var mainGridAdapter: LibraryReviewAdapter      // 하단 2열 그리드
    private lateinit var cardReviewAdapter: LibraryCardReviewAdapter // 상단 카드 내 리스트

    private val isHost = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initRecyclerViews()
        initListeners()
        observeViewModel()

        setFragmentResultListener("review_request") { _, bundle ->
            val content = bundle.getString("review_content")
            if (content != null) {
                binding.libDetailReviewText1Tv.text = content
                binding.libDetailReviewName1Tv.text = "Me"
                binding.bookDetailNoCardHost.visibility = View.GONE
                binding.groupReviewSection.visibility = View.VISIBLE
            }
        }
    }

    private fun initView() {
        // 초기 뷰 상태 설정
        binding.libReviewAddBtn.visibility = View.VISIBLE
        binding.libReviewListRv.visibility = View.VISIBLE
        binding.groupReviewSection.visibility = View.GONE
        binding.bookDetailReviewsRv.visibility = View.GONE // 카드 내 리스트는 처음에 숨김
    }

    private fun initRecyclerViews() {
        // 1. 상단 카드 내 리뷰 어댑터 설정
        cardReviewAdapter = LibraryCardReviewAdapter()
        binding.bookDetailReviewsRv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cardReviewAdapter
        }

        // 2. 하단 메인 그리드 어댑터 설정
        mainGridAdapter = LibraryReviewAdapter(
            onItemClick = { clickedItem ->
                val detailFragment = LibraryCardDetailFragment().apply {
                    arguments = Bundle().apply {
                        putLong("cardId", clickedItem.id)
                        putBoolean("isMine", clickedItem.isMine)
                    }
                }
                navigateTo(detailFragment)
            },
            onBookmarkClick = { item, position ->
                item.isBookmarked = !(item.isBookmarked ?: false)
                mainGridAdapter.notifyItemChangedAt(position)
            }
        )

        binding.libReviewListRv.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = mainGridAdapter
            while (itemDecorationCount > 0) removeItemDecorationAt(0)
            addItemDecoration(LibDetailGridDecoration(2, dpToPx(10), dpToPx(20), false))
        }
    }

    private fun observeViewModel() {
        viewModel.reviewList.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                binding.groupReviewSection.visibility = View.GONE
                if (isHost) {
                    binding.bookDetailNoCardHost.visibility = View.VISIBLE
                    binding.bookDetailNoCardGuest.visibility = View.GONE
                } else {
                    binding.bookDetailNoCardHost.visibility = View.GONE
                    binding.bookDetailNoCardGuest.visibility = View.VISIBLE
                }
            } else {
                binding.groupReviewSection.visibility = View.VISIBLE
                binding.bookDetailNoCardHost.visibility = View.GONE
                binding.bookDetailNoCardGuest.visibility = View.GONE

                mainGridAdapter.submitList(list.toList())
                cardReviewAdapter.submitList(list.toList()) // 카드 내 리스트에도 데이터 전달
                binding.libDetailTotalTv.text = "${list.size}개"
            }
        }
    }

    private fun initListeners() {
        binding.libDetailBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.libDetailMoreIv.setOnClickListener {
            LibraryGroupDeleteBottomSheet { showDeleteConfirmDialog() }.show(parentFragmentManager, "GroupDeleteSheet")
        }

        // [변경사항 1] 화살표 토글: 메인 리스트는 고정, 카드 내부 리스트만 토글
        binding.bookDetailDownArrowIv.setOnClickListener {
            if (binding.bookDetailReviewsRv.visibility == View.GONE) {
                binding.bookDetailReviewsRv.visibility = View.VISIBLE
                binding.bookDetailDownArrowIv.rotation = 180f
            } else {
                binding.bookDetailReviewsRv.visibility = View.GONE
                binding.bookDetailDownArrowIv.rotation = 0f
            }
        }

        // [변경사항 2] 독서카드 추가 버튼 클릭 시 이동
        val moveToAddCard = View.OnClickListener {
            navigateTo(LibraryAddCardFragment())
        }
        binding.libReviewAddBtn.setOnClickListener(moveToAddCard)
        binding.libReviewNoAddBtn.setOnClickListener(moveToAddCard)
    }

    private fun navigateTo(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showDeleteConfirmDialog() {
        com.bookiibookii.bookiibookii.common.CommonDialog(
            context = requireContext(),
            title = "그룹 삭제",
            subtitle = "",
            content = "정말로 이 그룹을 삭제하시겠습니까?\n나에게서만 삭제됩니다.",
            confirmBtnText = "삭제",
            confirmBtnColor = R.color.ui_point_red,
            onConfirmClick = { parentFragmentManager.popBackStack() }
        ).show()
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onResume() {
        super.onResume()
        hideBottomNavigation(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideBottomNavigation(false)
        _binding = null
    }

    private fun hideBottomNavigation(shouldHide: Boolean) {
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNav)
        bottomNav?.visibility = if (shouldHide) View.GONE else View.VISIBLE
    }
}