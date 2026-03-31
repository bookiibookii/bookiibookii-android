package com.bookiibookii.bookiibookii.lib

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

        val formattedStart = DateUtils.formatDate(startDate)
        val formattedEnd = DateUtils.formatDate(endDate)
        binding.libDetailDateTv.text = if (formattedEnd.isNotEmpty()) "$formattedStart ~ $formattedEnd" else "$formattedStart ~"

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
            try {
                fetchProgress()
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
        Log.d("FetchProgress", "========== 진행률 조회 시작 (GroupID: $groupId) ==========")
        val response = RetrofitClient.api().getMyTrackers()
        Log.d("FetchProgress", "Response Code: ${response.code()}")

        if (response.isSuccessful && response.body()?.isSuccess == true) {
            val trackerList = response.body()?.result ?: emptyList()
            Log.d("FetchProgress", "받아온 전체 트래커 개수: ${trackerList.size}")

            val myTracker = trackerList.find { it.groupId == this@LibraryBookDetailIngFragment.groupId }

            if (myTracker != null) {
                Log.d("FetchProgress", ">> 내 트래커 찾음!")
                val detail = myTracker.togetherDetail
                if (detail != null) {
                    val myRate = detail.myReadingRate
                    val groupRate = detail.groupReadingRate

                    Log.d("FetchProgress", "   - 내 진행률: $myRate%")
                    Log.d("FetchProgress", "   - 그룹 평균: $groupRate%")

                    updateProgressBar(myRate, groupRate)

                    if (myRate >= 100) {
                        Log.d("FetchProgress", "   -> 100% 달성! 버튼 상태 변경")
                        binding.libWriteDoneBtn.visibility = View.GONE
                        binding.libWriteReviewBtn.visibility = View.VISIBLE
                        binding.libReviewAddBtn.visibility = View.GONE
                    } else {
                        Log.d("FetchProgress", "   -> 아직 100% 미만")
                    }
                } else {
                    Log.e("FetchProgress", "!! togetherDetail 데이터가 null입니다.")
                }
            } else {
                Log.e("FetchProgress", "!! 현재 그룹ID($groupId)와 일치하는 트래커를 목록에서 찾을 수 없습니다.")
            }
        } else {
            val msg = response.body()?.message ?: "메시지 없음"
            Log.e("FetchProgress", "API 요청 실패: $msg")
        }
    }

    private suspend fun fetchCardList() {
        val response = RetrofitClient.api().getGroupCards(groupId)
        if (response.isSuccessful && response.body()?.isSuccess == true) {
            val result = response.body()?.result
            val apiCards = result?.cards ?: emptyList()

            originalList = apiCards
            cardAdapter.submitList(originalList.sortedByDescending { it.createdAt })
            binding.libDetailTotalTv.text = "${apiCards.size}개"

            if (apiCards.isNotEmpty()) {
                binding.groupDataExist.visibility = View.VISIBLE
                binding.layoutEmpty.visibility = View.GONE
            } else {
                binding.groupDataExist.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
            }
            binding.libReviewAddBtn.visibility = View.VISIBLE
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
                    Toast.makeText(context, "상세 화면으로 이동할 수 없습니다.", Toast.LENGTH_SHORT).show()
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
                onDeleteClick = {
                    showDeleteConfirmDialog()
                }
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
        if (groupId == -1) {
            Toast.makeText(context, "그룹 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("CompleteReading", "완독 요청 시작 - GroupID: $groupId")

        viewLifecycleOwner.lifecycleScope.launch {
            if (!isAdded) return@launch
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().completeReading(groupId)

                Log.d("CompleteReading", "Response Code: ${response.code()}")
                Log.d("CompleteReading", "Response Body: ${response.body()}")

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d("CompleteReading", "완독 처리 성공!")
                    Toast.makeText(context, "완독 처리가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                    binding.libWriteDoneBtn.visibility = View.GONE
                    binding.libWriteReviewBtn.visibility = View.VISIBLE
                    binding.libReviewAddBtn.visibility = View.GONE

                    requireActivity().supportFragmentManager.setFragmentResult("REFRESH_LIBRARY", Bundle())

                    loadInitialData()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val msg = response.body()?.message ?: "서버 메시지 없음"

                    Log.e("CompleteReading", "완독 처리 실패 - 메시지: $msg")
                    Log.e("CompleteReading", "Error Body: $errorBody")

                    Toast.makeText(context, "완독 처리에 실패했습니다: $msg", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("CompleteReading", "Exception 발생", e)
                Toast.makeText(context, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
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
            content = "그룹을 정말 삭제하시겠습니까?\n이 작업은 되돌릴 수 없고, 내 서재에서만 삭제됩니다.",
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
                    Toast.makeText(context, "그룹이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                }
            } catch (e: Exception) { e.printStackTrace() }
            finally { if (loadingDialog.isShowing) loadingDialog.dismiss() }
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}