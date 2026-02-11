package com.bookiibookii.bookiibookii.lib

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.CardItem
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailIngBinding
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.coroutines.launch

class LibraryBookDetailIngFragment : Fragment() {

    private var _binding: FragmentLibBookDetailIngBinding? = null
    private val binding get() = _binding!!

    private var userBookId: Int = -1
    private var groupId: Int = -1
    private var bookTitle = ""
    private var bookAuthor = ""
    private var bookCover = ""
    private var hostName = ""
    private var hostProfileUrl = ""
    private var startDate = ""

    private lateinit var cardAdapter: LibraryReviewAdapter
    private var originalList: List<CardItem> = emptyList()

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

        // 데이터 호출
        fetchCardList()
        fetchProgress()
    }

    private fun initView() {
        binding.libDetailBookTitleTv.text = bookTitle
        binding.libDetailBookAuthorTv.text = bookAuthor
        binding.libDetailTitleTv.text = bookTitle
        binding.libDetailDateTv.text = "$startDate ~"

        binding.libDetailBookTitleTv.isSelected = true
        binding.libDetailBookAuthorTv.isSelected = true

        Glide.with(this)
            .load(bookCover)
            .transform(CenterCrop(), RoundedCorners(dpToPx(10)))
            .into(binding.libDetailImageIv)

        binding.libDetailProfileTv.text = hostName
        Glide.with(this)
            .load(hostProfileUrl)
            .placeholder(R.drawable.bg_circle_gray500)
            .error(R.drawable.img_profile_default)
            .circleCrop()
            .into(binding.libDetailProfileIv)

        updateProgressBar(0, 0)

        binding.libWriteDoneBtn.visibility = View.VISIBLE
        binding.libWriteReviewBtn.visibility = View.GONE
        binding.groupDataExist.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
    }

    private fun fetchProgress() {
        if (groupId == -1) {
            Log.e("ProgressDebug", "groupId가 -1입니다.")
            return
        }

        lifecycleScope.launch {
            try {
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateProgressBar(myProgress: Int, groupProgress: Int) {
        binding.readingProgressBar.progress = myProgress
        binding.readingProgressBar.secondaryProgress = groupProgress
        binding.myPercent.text = "$myProgress%"
        binding.avgPercent.text = "$groupProgress%"

        val constraintLayout = binding.progressLayout
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        constraintSet.setGuidelinePercent(R.id.guideline_my_progress, myProgress / 100f)
        constraintSet.setGuidelinePercent(R.id.guideline_group_progress, groupProgress / 100f)
        constraintSet.applyTo(constraintLayout)
    }

    private fun fetchCardList() {
        if (groupId == -1) return
        lifecycleScope.launch {
            try {
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
                    // 독서 중에는 카드 추가 가능
                    binding.libReviewAddBtn.visibility = View.VISIBLE
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun initRecyclerView() {
        cardAdapter = LibraryReviewAdapter(
            onItemClick = { clickedCard ->
                // ★ [디버깅] 클릭 이벤트 로그
                Log.d("CardClickError", "카드 클릭됨: ID=${clickedCard.cardId}, Writer=${clickedCard.creatorName}")

                try {
                    val detailFragment = LibraryCardDetailFragment().apply {
                        arguments = Bundle().apply {
                            putLong("cardId", clickedCard.cardId.toLong())

                            // [수정 포인트] 내 카드인지 여부 판별
                            // 현재는 true로 고정되어 있으나, 추후 내 닉네임과 비교 필요
                            // val isMyCard = clickedCard.creatorName == myNickname
                            putBoolean("isMine", true)

                            putString("writerName", clickedCard.creatorName)
                            putString("writerProfileUrl", null) // 프로필 URL 필드가 있다면 추가
                        }
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, detailFragment)
                        .addToBackStack(null)
                        .commit()

                } catch (e: Exception) {
                    // ★ [핵심] 여기서 에러가 잡힌다면 프래그먼트 전환 문제
                    Log.e("CardClickError", "화면 이동 중 에러 발생", e)
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
        lifecycleScope.launch {
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
        binding.libDetailBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.libDetailMoreIv.setOnClickListener {
            LibraryGroupDeleteBottomSheet { showDeleteConfirmDialog() }.show(parentFragmentManager, "GroupDeleteSheet")
        }

        binding.libWriteDoneBtn.setOnClickListener {
            CommonDialog(
                context = requireContext(),
                title = "독서 종료",
                subtitle = "",
                content = "독서를 종료하면 더 이상 독서카드를 추가할 수 없어요. 독서를 종료할까요?",
                confirmBtnText = "확인",
                confirmBtnColor = R.color.grey_900,
                onConfirmClick = {
                    requestCompleteReading()
                }
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
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }

        val goAdd = View.OnClickListener {
            val fragment = LibraryAddCardFragment().apply {
                arguments = Bundle().apply { putInt("userBookId", userBookId) }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.libReviewAddBtn.setOnClickListener(goAdd)

        binding.libDetailLatelyTv.setOnClickListener { cardAdapter.submitList(originalList.sortedByDescending { it.createdAt }) }
        binding.libDetailPageTv.setOnClickListener { cardAdapter.submitList(originalList.sortedBy { it.page }) }
    }

    private fun requestCompleteReading() {
        if (groupId == -1) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().completeReading(groupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(context, "완독 처리가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                    binding.libWriteDoneBtn.visibility = View.GONE
                    binding.libWriteReviewBtn.visibility = View.VISIBLE
                    binding.libReviewAddBtn.visibility = View.GONE

                    val currentGroupRate = binding.avgPercent.text.toString().replace("%", "").toIntOrNull() ?: 0
                    updateProgressBar(100, currentGroupRate)
                } else {
                    Toast.makeText(context, "완독 처리에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
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
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().deleteGroup(userBookId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(context, "그룹이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
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