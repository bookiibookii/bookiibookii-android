package com.bookiibookii.bookiibookii.lib

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.common.showCustomToast // ★ 커스텀 토스트 import
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.CardItem
import com.bookiibookii.bookiibookii.data.viewModel.LibraryCardViewModel
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailIngBinding
import com.bookiibookii.bookiibookii.group.GroupDetailActivity
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.coroutines.launch

class LibraryBookDetailIngFragment : BaseDetailFragment<FragmentLibBookDetailIngBinding>() {

    private lateinit var loadingDialog: LoadingDialog

    private var userBookId: Int = -1
    private var groupId: Int = -1
    private var bookTitle = ""
    private var bookAuthor = ""
    private var bookCover = ""
    private var hostName = ""
    private var hostProfileUrl = ""
    private var startDate = ""
    private var endDate = ""

    private lateinit var cardAdapter: LibraryReviewAdapter
    private var originalList: List<CardItem> = emptyList()

    private val cardViewModel: LibraryCardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userBookId = it.getInt("userBookId", -1)
            groupId = it.getInt("groupId", -1)
            bookTitle = it.getString("bookTitle", "") ?: ""
            bookAuthor = it.getString("bookAuthor", "") ?: ""
            bookCover = it.getString("bookCover", "") ?: ""
            hostName = it.getString("hostName", "") ?: ""
            hostProfileUrl = it.getString("hostProfileUrl", "") ?: ""
            startDate = it.getString("startDate", "") ?: ""
            endDate = it.getString("endDate", "") ?: ""
        }
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLibBookDetailIngBinding {
        return FragmentLibBookDetailIngBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        initView()
        initRecyclerView()
        initListeners()
        setupObservers()
        loadInitialData()
    }

    private fun initView() {
        binding.libDetailBookTitleTv.text = bookTitle
        binding.libDetailBookAuthorTv.text = bookAuthor
        binding.libDetailTitleTv.text = bookTitle

        val formattedStart = if (startDate.isNullOrBlank() || startDate.startsWith("0000")) "0000. 00. 00." else DateUtils.formatDate(startDate)
        binding.libDetailDateTv.text = "$formattedStart ~"

        binding.libDetailBookTitleTv.isSelected = true
        binding.libDetailBookAuthorTv.isSelected = true

        Glide.with(this).load(bookCover).transform(CenterCrop(), RoundedCorners(dpToPx(10))).into(binding.libDetailImageIv)

        binding.libDetailProfileTv.text = hostName
        Glide.with(this).load(hostProfileUrl).placeholder(R.drawable.bg_circle_gray500)
            .error(R.drawable.img_profile_default).transform(CenterCrop(), RoundedCorners(dpToPx(8))).into(binding.libDetailProfileIv)

        updateProgressBar(0, 0)

        binding.libWriteDoneBtn.visibility = View.VISIBLE
        binding.libWriteReviewBtn.visibility = View.GONE
        binding.groupDataExist.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
    }

    private fun loadInitialData() {
        if (groupId == -1) return
        cardViewModel.fetchGroupCards(groupId)
        viewLifecycleOwner.lifecycleScope.launch {
            try { fetchProgress() } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun setupObservers() {
        cardViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                if (!loadingDialog.isShowing) loadingDialog.show()
            } else {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }

        cardViewModel.cardList.observe(viewLifecycleOwner) { cards ->
            cardAdapter.submitList(cards)
            binding.libDetailTotalTv.text = "${cards.size}개"

            if (cards.isNotEmpty()) {
                binding.groupDataExist.visibility = View.VISIBLE
                binding.layoutEmpty.visibility = View.GONE
            } else {
                binding.groupDataExist.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
            }
            binding.libReviewAddBtn.visibility = View.VISIBLE
        }
    }

    private suspend fun fetchProgress() {
        val response = RetrofitClient.api().getMyTrackers()
        if (response.isSuccessful && response.body()?.isSuccess == true) {
            val trackerList = response.body()?.result ?: emptyList()
            val myTracker = trackerList.find { it.groupId == this@LibraryBookDetailIngFragment.groupId }

            if (myTracker != null) {
                val detail = myTracker.togetherDetail
                if (detail != null) {
                    val myRate = detail.myReadingRate
                    val groupRate = detail.groupReadingRate
                    updateProgressBar(myRate, groupRate)

                    if (myRate >= 100) {
                        binding.libWriteDoneBtn.visibility = View.GONE
                        binding.libWriteReviewBtn.visibility = View.VISIBLE
                        binding.libReviewAddBtn.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun updateProgressBar(myProgress: Int, groupProgress: Int) {
        binding.myPercent.text = "$myProgress%"
        binding.avgPercent.text = "$groupProgress%"

        val constraintLayout = binding.progressLayout
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        constraintSet.setGuidelinePercent(R.id.guideline_my_progress, myProgress / 100f)
        constraintSet.setGuidelinePercent(R.id.guideline_group_progress, groupProgress / 100f)
        constraintSet.applyTo(constraintLayout)

        if (myProgress > groupProgress) {
            binding.barGroup.translationZ = 1f
            binding.barMine.translationZ = 0f
        } else {
            binding.barMine.translationZ = 1f
            binding.barGroup.translationZ = 0f
        }

        binding.myProgressDot.translationZ = 2f
        binding.groupAvgDot.translationZ = 2f
    }

    private fun initRecyclerView() {
        cardAdapter = LibraryReviewAdapter(
            onItemClick = { clickedCard ->
                try {
                    val detailFragment = LibraryCardDetailFragment().apply {
                        arguments = Bundle().apply {
                            putLong("cardId", clickedCard.cardId.toLong())
                            putBoolean("isMine", true)
                            putString("writerName", clickedCard.creatorName)
                        }
                    }
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, detailFragment)
                        .addToBackStack(null)
                        .commit()
                } catch (e: Exception) {
                    // ★ 커스텀 토스트 적용
                    requireContext().showCustomToast("상세 화면으로 이동할 수 없습니다.", false)
                }
            },
            onBookmarkClick = { card, _ -> toggleBookmark(card) }
        )
        binding.libReviewListRv.layoutManager = GridLayoutManager(context, 2)
        binding.libReviewListRv.adapter = cardAdapter
        binding.libReviewListRv.addItemDecoration(LibDetailGridDecoration(2, dpToPx(10), dpToPx(12), false))
    }

    private fun toggleBookmark(card: CardItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().toggleBookmark(card.cardId.toLong())
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val isBookmarked = response.body()?.result?.bookmarked ?: false
                    val newList = originalList.toMutableList()
                    val index = newList.indexOfFirst { it.cardId == card.cardId }
                    if (index != -1) {
                        newList[index] = newList[index].copy(isBookmarked = isBookmarked)
                        originalList = newList
                        cardAdapter.submitList(newList.sortedByDescending { it.createdAt })
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun initListeners() {
        binding.libDetailBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        binding.libDetailMoreIv.setOnClickListener {
            LibraryGroupDeleteBottomSheet(
                onDetailClick = {
                    val intent = Intent(requireContext(), GroupDetailActivity::class.java)
                    intent.putExtra("GROUP_ID", groupId.toLong())
                    intent.putExtra("GROUP_TYPE", "TOGETHER")
                    startActivity(intent)
                },
                onDeleteClick = { showDeleteConfirmDialog() }
            ).show(requireActivity().supportFragmentManager, "GroupDeleteSheet")
        }

        binding.libWriteDoneBtn.setOnClickListener {
            CommonDialog(
                context = requireContext(),
                title = "독서 종료",
                subtitle = "",
                content = "독서를 종료하면 더 이상 독서카드를 추가할 수 없어요. 독서를 종료할까요?",
                confirmBtnText = "확인",
                confirmBtnColor = R.color.grey_900,
                onConfirmClick = { requestCompleteReading() }
            ).show()
        }

        binding.libWriteReviewBtn.setOnClickListener {
            val fragment = LibraryWriteReviewFragment().apply {
                arguments = Bundle().apply {
                    putInt("userBookId", userBookId)
                    putInt("groupId", groupId)
                    putString("bookTitle", bookTitle)
                    putString("bookAuthor", bookAuthor)
                    putString("bookCover", bookCover)
                    putString("hostName", hostName)
                    putString("hostProfileUrl", hostProfileUrl)
                    putString("startDate", startDate)
                    putString("endDate", endDate)
                }
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }

        val goAdd = View.OnClickListener {
            val fragment = LibraryAddCardFragment().apply {
                arguments = Bundle().apply { putInt("userBookId", userBookId) }
            }
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).addToBackStack(null).commit()
        }
        binding.libReviewAddBtn.setOnClickListener(goAdd)

        binding.libDetailLatelyTv.setOnClickListener { sortCards(isLately = true) }
        binding.libDetailPageTv.setOnClickListener { sortCards(isLately = false) }
    }

    private fun sortCards(isLately: Boolean) {
        cardViewModel.sortCards(isLately)

        val context = requireContext()
        val activeColor = androidx.core.content.ContextCompat.getColor(context, R.color.pre_main)
        val inactiveColor = androidx.core.content.ContextCompat.getColor(context, R.color.grey_500)

        if (isLately) {
            binding.libDetailLatelyTv.setTextColor(activeColor)
            binding.libDetailPageTv.setTextColor(inactiveColor)
        } else {
            binding.libDetailLatelyTv.setTextColor(inactiveColor)
            binding.libDetailPageTv.setTextColor(activeColor)
        }
    }

    private fun requestCompleteReading() {
        if (groupId == -1) return

        viewLifecycleOwner.lifecycleScope.launch {
            if (!isAdded) return@launch
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().completeReading(groupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    requireContext().showCustomToast("완독 처리가 완료되었습니다.", true) // ★ 커스텀 토스트
                    binding.libWriteDoneBtn.visibility = View.GONE
                    binding.libWriteReviewBtn.visibility = View.VISIBLE
                    binding.libReviewAddBtn.visibility = View.GONE
                    requireActivity().supportFragmentManager.setFragmentResult("REFRESH_LIBRARY", Bundle())
                    loadInitialData()
                } else {
                    val msg = response.body()?.message ?: "서버 메시지 없음"
                    requireContext().showCustomToast(msg, false) // ★ 커스텀 토스트
                }
            } catch (e: Exception) {
                requireContext().showCustomToast("오류가 발생했습니다.", false) // ★ 커스텀 토스트
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }

    private fun showDeleteConfirmDialog() {
        CommonDialog(
            context = requireContext(),
            title = "서재 내 그룹 삭제",
            subtitle = bookTitle,
            content = "그룹을 정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없고, 내 서재에서만 삭제됩니다.",
            confirmBtnText = "삭제",
            confirmBtnColor = R.color.ui_point_red,
            onConfirmClick = { deleteGroup() }
        ).show()
    }

    private fun deleteGroup() {
        if (userBookId == -1) return
        viewLifecycleOwner.lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().deleteGroup(userBookId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    requireContext().showCustomToast("그룹이 삭제되었습니다.", true) // ★ 커스텀 토스트
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    requireContext().showCustomToast("삭제 실패", false) // ★ 커스텀 토스트 (혹시 몰라 추가)
                }
            } catch (e: Exception) { e.printStackTrace() }
            finally { if (loadingDialog.isShowing) loadingDialog.dismiss() }
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}