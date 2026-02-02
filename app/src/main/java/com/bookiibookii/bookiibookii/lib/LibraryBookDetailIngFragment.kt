package com.bookiibookii.bookiibookii.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.ReviewModel
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailIngBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LibraryBookDetailIngFragment : Fragment() {

    private var _binding: FragmentLibBookDetailIngBinding? = null
    private val binding get() = _binding!!

    // ViewModel 연결
    private val viewModel: ReviewModel by activityViewModels()
    private lateinit var reviewAdapter: LibraryReviewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibBookDetailIngBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initRecyclerView()
        initListeners()
        observeViewModel()

        // [추가] 뷰가 다 그려진 후 프로그레스 바 점 위치 계산
        binding.readingProgressBar.post {
            updateProgressDots()
        }
    }

    private fun initView() {
        // 예시 데이터 설정 (XML에 79, 81로 되어있지만 코드에서 설정하는 것이 안전함)
        binding.readingProgressBar.progress = 79
        binding.readingProgressBar.secondaryProgress = 81

        binding.myPercent.text = "나의 독서율"
        binding.libReviewAddBtn.visibility = View.GONE
    }

    // [핵심 기능] 프로그레스 바 점 위치 이동 로직
    private fun updateProgressDots() {
        if (_binding == null) return

        val progressBar = binding.readingProgressBar
        val width = progressBar.width.toFloat() // 바 전체 너비
        val max = progressBar.max.toFloat()     // 최대값 (100)

        // 1. 나의 독서율 점 이동 (Progress)
        val myProgress = progressBar.progress.toFloat()
        val myDot = binding.myProgressDot
        // 위치 계산: (전체너비 * 비율) - (점 너비의 절반) -> 점의 중심이 끝에 오도록
        val myX = (width * (myProgress / max)) - (myDot.width / 2f)
        myDot.translationX = myX

        // 2. 그룹 평균 독서율 점 이동 (Secondary Progress)
        val groupProgress = progressBar.secondaryProgress.toFloat()
        val groupDot = binding.groupAvgDot
        val groupX = (width * (groupProgress / max)) - (groupDot.width / 2f)
        groupDot.translationX = groupX
    }

    private fun initRecyclerView() {
        reviewAdapter = LibraryReviewAdapter(
            onItemClick = { clickedItem ->
                // 상세 화면 이동
                val detailFragment = LibraryCardDetailFragment().apply {
                    arguments = Bundle().apply {
                        putLong("cardId", clickedItem.id)
                        putBoolean("isMine", clickedItem.isMine)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, detailFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onBookmarkClick = { item, position ->
                item.isBookmarked = !(item.isBookmarked ?: false)
                reviewAdapter.notifyItemChangedAt(position)
            }
        )

        binding.libReviewListRv.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = reviewAdapter
            while (itemDecorationCount > 0) removeItemDecorationAt(0)
            addItemDecoration(LibDetailGridDecoration(2, dpToPx(10), dpToPx(12), false))
        }
    }

    private fun observeViewModel() {
        viewModel.reviewList.observe(viewLifecycleOwner) { list ->
            reviewAdapter.submitList(list.toList())
            binding.libDetailTotalTv.text = "${list.size}개"
        }
    }

    private fun initListeners() {
        binding.libDetailBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.libDetailMoreIv.setOnClickListener {
            LibraryGroupDeleteBottomSheet {
                showDeleteConfirmDialog()
            }.show(parentFragmentManager, "GroupDeleteSheet")
        }

        binding.libWriteDoneBtn.setOnClickListener { showFinishReadingDialog() }

        binding.libWriteReviewBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LibraryWriteReviewFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.libReviewAddBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LibraryAddCardFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showFinishReadingDialog() {
        val dialog = CommonDialog(
            context = requireContext(),
            title = "독서 종료",
            subtitle = "",
            content = "이 책을 다 읽으셨나요?\n종료 후에는 되돌릴 수 없어요.",
            confirmBtnText = "확인",
            confirmBtnColor = R.color.grey_900,
            onConfirmClick = { finishReadingLogic() }
        )
        dialog.show()
    }

    private fun finishReadingLogic() {
        binding.readingProgressBar.progress = 100
        // 점 위치도 100%로 다시 이동시켜야 함
        binding.readingProgressBar.post { updateProgressDots() }

        binding.libWriteDoneBtn.visibility = View.GONE
        binding.libWriteReviewBtn.visibility = View.VISIBLE
        binding.libReviewAddBtn.visibility = View.VISIBLE

        val currentDate = SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault()).format(Date())
        val originalText = binding.libDetailDateTv.text.toString()
        binding.libDetailDateTv.text = "$originalText $currentDate"
    }

    private fun showDeleteConfirmDialog() {
        val dialog = CommonDialog(
            context = requireContext(),
            title = "그룹 삭제",
            subtitle = "",
            content = "정말로 이 그룹을 삭제하시겠습니까?\n나에게서만 삭제됩니다.",
            confirmBtnText = "삭제",
            confirmBtnColor = R.color.ui_point_red,
            onConfirmClick = { parentFragmentManager.popBackStack() }
        )
        dialog.show()
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    // [바텀 네비게이션 제어]
    // 화면에 들어올 때 숨김
    override fun onResume() {
        super.onResume()
        setBottomNavVisibility(false)
    }

    // 화면이 파괴될 때(뒤로가기 등) 다시 보임
    override fun onDestroyView() {
        super.onDestroyView()
        setBottomNavVisibility(true)
        _binding = null
    }

    private fun setBottomNavVisibility(isVisible: Boolean) {
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNav)
        bottomNav?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}