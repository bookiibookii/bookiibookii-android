package com.bookiibookii.bookiibookii.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.CardItem
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailTogetherBinding
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.coroutines.launch

class LibraryBookDetailTogetherFragment : Fragment() {

    private var _binding: FragmentLibBookDetailTogetherBinding? = null
    private val binding get() = _binding!!

    // 내 닉네임 가져오기용
    private val myPageViewModel: MyPageViewModel by activityViewModels()

    private var userBookId: Int = -1
    private var groupId: Int = -1
    private var bookTitle = ""
    private var bookAuthor = ""
    private var bookCover = ""
    private var hostName = ""
    private var hostProfileUrl = ""
    private var myNickname = "나"
    private var startDate = "2025. 12. 18.~"
    private var endDate = ""

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
            endDate = it.getString("endDate", "") ?: ""
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibBookDetailTogetherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMyProfileData()

        initView()
        initRecyclerView()
        initListeners()
        fetchData()
    }

    private fun setupMyProfileData() {
        myPageViewModel.profileData.value?.let { myNickname = it.nickname }
        myPageViewModel.profileData.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                myNickname = profile.nickname
                if (_binding != null) binding.libDetailReviewName1Tv.text = myNickname
            }
        }
    }

    private fun initView() {
        // 책 정보
        binding.libDetailBookTitleTv.text = bookTitle
        binding.libDetailBookAuthorTv.text = bookAuthor
        binding.libDetailTitleTv.text = bookTitle
        val dateText = if (endDate.isNotEmpty()) {
            "$startDate ~ $endDate"
        } else {
            "$startDate ~"
        }
        binding.libDetailDateTv.text = dateText

        // 마키 효과
        binding.libDetailBookTitleTv.isSelected = true
        binding.libDetailBookAuthorTv.isSelected = true

        Glide.with(this).load(bookCover).transform(CenterCrop(), RoundedCorners(dpToPx(10))).into(binding.libDetailImageIv)

        // 호스트 정보
        binding.libDetailProfileTv.text = hostName
        Glide.with(this)
            .load(hostProfileUrl)
            .placeholder(R.drawable.bg_circle_gray500)
            .error(R.drawable.img_profile_default)
            .circleCrop()
            .into(binding.libDetailProfileIv)

        // 초기 뷰 상태
        binding.groupPartnerReview.visibility = View.GONE
        binding.bookDetailDownArrowIv.rotation = 0f

        binding.groupDataExist.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
    }

    private fun initRecyclerView() {
        cardAdapter = LibraryReviewAdapter(
            onItemClick = { clickedCard ->
                val detailFragment = LibraryCardDetailFragment().apply {
                    arguments = Bundle().apply {
                        putLong("cardId", clickedCard.cardId.toLong())
                        putBoolean("isMine", true)
                        putString("writerName", clickedCard.creatorName)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, detailFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onBookmarkClick = { card, _ -> toggleBookmark(card) }
        )
        binding.libReviewListRv.layoutManager = GridLayoutManager(context, 2)
        binding.libReviewListRv.adapter = cardAdapter
        binding.libReviewListRv.addItemDecoration(LibDetailGridDecoration(2, dpToPx(10), dpToPx(12), false))
    }

    private fun fetchData() {
        if (groupId == -1) return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getGroupCards(groupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result

                    // 한줄평 바인딩
                    binding.libDetailReviewName1Tv.text = myNickname
                    binding.libDetailReviewText1Tv.text = result?.myComment ?: "\"아직 한줄 평을 남기지 않았어요.\""

                    binding.libDetailReviewName2Tv.text = "상대방"
                    binding.libDetailReviewText2Tv.text = result?.partnerComment ?: "작성된 내용이 없습니다."

                    // 리스트 데이터
                    val apiCards = result?.cards ?: emptyList()
                    originalList = apiCards
                    cardAdapter.submitList(originalList.sortedByDescending { it.createdAt })
                    binding.libDetailTotalTv.text = "${apiCards.size}개"

                    // 분기 처리
                    if (apiCards.isNotEmpty()) {
                        binding.groupDataExist.visibility = View.VISIBLE
                        binding.layoutEmpty.visibility = View.GONE
                    } else {
                        binding.groupDataExist.visibility = View.GONE
                        binding.layoutEmpty.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
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

        // [삭제] 더보기 버튼
        binding.libDetailMoreIv.setOnClickListener {
            LibraryGroupDeleteBottomSheet { showDeleteConfirmDialog() }.show(parentFragmentManager, "GroupDeleteSheet")
        }

        // [토글] 상대방 리뷰 보기
        binding.bookDetailDownArrowIv.setOnClickListener {
            if (binding.groupPartnerReview.visibility == View.GONE) {
                binding.groupPartnerReview.visibility = View.VISIBLE
                binding.bookDetailDownArrowIv.animate().rotation(180f).setDuration(200).start()
            } else {
                binding.groupPartnerReview.visibility = View.GONE
                binding.bookDetailDownArrowIv.animate().rotation(0f).setDuration(200).start()
            }
        }

        binding.libDetailLatelyTv.setOnClickListener { cardAdapter.submitList(originalList.sortedByDescending { it.createdAt }) }
        binding.libDetailPageTv.setOnClickListener { cardAdapter.submitList(originalList.sortedBy { it.page }) }
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