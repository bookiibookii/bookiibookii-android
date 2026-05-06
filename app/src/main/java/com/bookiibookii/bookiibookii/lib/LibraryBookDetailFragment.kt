package com.bookiibookii.bookiibookii.lib

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.common.showCustomToast // ★ 커스텀 토스트 임포트
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.library.CardItem
import com.bookiibookii.bookiibookii.data.viewModel.LibraryCardViewModel
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailBinding
import com.bookiibookii.bookiibookii.group.GroupDetailActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.coroutines.launch

class LibraryBookDetailFragment : BaseDetailFragment<FragmentLibBookDetailBinding>() {

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLibBookDetailBinding {
        return FragmentLibBookDetailBinding.inflate(inflater, container, false)
    }

    private lateinit var loadingDialog: LoadingDialog
    private val myPageViewModel: MyPageViewModel by activityViewModels()

    private var groupId: Int = -1
    private var userBookId: Int = -1
    private var bookTitle = ""
    private var bookAuthor = ""
    private var bookCover = ""
    private var hostName = ""
    private var hostProfileUrl = ""
    private var startDate = ""
    private var endDate = ""
    private var rating: Double = 0.0
    private var myNickname = "나"

    private lateinit var cardAdapter: LibraryReviewAdapter
    private var originalList: List<CardItem> = emptyList()
    private val cardViewModel: LibraryCardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getInt("groupId", -1)
            userBookId = it.getInt("userBookId", -1)
            bookTitle = it.getString("bookTitle", "") ?: ""
            bookAuthor = it.getString("bookAuthor", "") ?: ""
            bookCover = it.getString("bookCover", "") ?: ""
            hostName = it.getString("hostName", "") ?: ""
            hostProfileUrl = it.getString("hostProfileUrl", "") ?: ""
            startDate = it.getString("startDate", "") ?: ""
            endDate = it.getString("endDate", "") ?: ""
            rating = it.getDouble("rating", 0.0)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        setupMyProfileData()
        initView()
        initRecyclerView()
        initListeners()
        setupObservers()
        cardViewModel.fetchGroupCards(groupId)
    }

    private fun setupMyProfileData() {
        myPageViewModel.profileData.value?.let { myNickname = it.nickname }
        myPageViewModel.profileData.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                myNickname = profile.nickname
                binding.libDetailReviewName1Tv.text = myNickname
            }
        }
        if (myPageViewModel.profileData.value == null) {
            myPageViewModel.fetchMypageData()
        }
    }

    private fun initView() {
        binding.libDetailTitleTv.text = bookTitle
        binding.libDetailBookTitleTv.text = bookTitle
        binding.libDetailBookAuthorTv.text = bookAuthor
        binding.libDetailBookTitleTv.isSelected = true
        binding.libDetailBookAuthorTv.isSelected = true

        Glide.with(this).load(bookCover).placeholder(R.drawable.bg_round_20dp_gray200)
            .transform(CenterCrop(), RoundedCorners(dpToPx(10))).into(binding.libDetailImageIv)

        Glide.with(this).load(hostProfileUrl).placeholder(R.drawable.bg_round_20dp_gray200)
            .error(R.drawable.img_profile_default).transform(CenterCrop(), RoundedCorners(dpToPx(8))).into(binding.libDetailProfileIv)

        binding.libDetailProfileTv.text = hostName

        val formattedStart = if (startDate.isNullOrBlank() || startDate.startsWith("0000")) "0000. 00. 00." else DateUtils.formatDate(startDate)
        val formattedEnd = if (endDate.isNullOrBlank() || endDate.startsWith("0000")) "0000. 00. 00." else DateUtils.formatDate(endDate)
        binding.libDetailDateTv.text = "$formattedStart ~ $formattedEnd"

        if (rating > 0.0) {
            binding.libDetailRateList.visibility = View.VISIBLE
            setRatingStars(rating)
        } else {
            binding.libDetailRateList.visibility = View.GONE
        }

        binding.groupReviewSection.visibility = View.GONE
        binding.bookDetailNoCardHost.visibility = View.GONE
        binding.bookDetailNoCardGuest.visibility = View.GONE
        binding.libReviewAddBtn.visibility = View.GONE
    }

    private fun setRatingStars(score: Double) {
        val scoreInt = score.toInt()
        val hasHalfStar = (score - scoreInt) >= 0.5
        val container = binding.libDetailRateList

        for (i in 0 until container.childCount) {
            val star = container.getChildAt(i) as? ImageView ?: continue
            when {
                i < scoreInt -> star.setImageResource(R.drawable.ic_star_filled)
                i == scoreInt && hasHalfStar -> star.setImageResource(R.drawable.ic_star_half)
                else -> star.setImageResource(R.drawable.ic_star_none)
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
                binding.groupReviewSection.visibility = View.VISIBLE
                binding.libReviewAddBtn.visibility = View.VISIBLE
                binding.bookDetailNoCardHost.visibility = View.GONE
                binding.bookDetailNoCardGuest.visibility = View.GONE
            } else {
                binding.groupReviewSection.visibility = View.GONE
                binding.libReviewAddBtn.visibility = View.GONE

                val isMyTurn = (cardViewModel.groupCardResult.value?.currentBookOwner?.nickname == myNickname)
                if (isMyTurn) {
                    binding.bookDetailNoCardHost.visibility = View.VISIBLE
                    binding.bookDetailNoCardGuest.visibility = View.GONE
                } else {
                    binding.bookDetailNoCardHost.visibility = View.GONE
                    binding.bookDetailNoCardGuest.visibility = View.VISIBLE
                }
            }
        }

        cardViewModel.groupCardResult.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe

            binding.libDetailReviewName1Tv.text = myNickname
            binding.libDetailReviewText1Tv.text = result.myComment ?: "\"아직 한줄 평을 남기지 않았어요.\""

            val partnerItem = result.togetherComments?.find { it.nickname != myNickname }
            val partnerName = partnerItem?.nickname ?: if (myNickname != hostName) hostName else "상대방"
            binding.libDetailReviewName2Tv.text = partnerName
            binding.libDetailReviewText2Tv.text = result.partnerComment ?: "\"아직 한줄 평을 남기지 않았어요.\""
        }
    }

    private fun sortCards(isLately: Boolean) {
        cardViewModel.sortCards(isLately)

        val activeColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.pre_main)
        val inactiveColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.grey_500)

        if (isLately) {
            binding.libDetailLatelyTv.setTextColor(activeColor)
            binding.libDetailPageTv.setTextColor(inactiveColor)
        } else {
            binding.libDetailLatelyTv.setTextColor(inactiveColor)
            binding.libDetailPageTv.setTextColor(activeColor)
        }
    }

    private fun initRecyclerView() {
        cardAdapter = LibraryReviewAdapter(
            onItemClick = { clickedCard ->
                val detailFragment = LibraryCardDetailFragment().apply {
                    arguments = Bundle().apply {
                        putLong("cardId", clickedCard.cardId.toLong())
                        putBoolean("isMine", clickedCard.creatorName == myNickname)
                        putString("writerName", clickedCard.creatorName)
                    }
                }
                requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, detailFragment).addToBackStack(null).commit()
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
        binding.libDetailBackIv.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.libDetailMoreIv.setOnClickListener {
            LibraryGroupDeleteBottomSheet(
                onDetailClick = {
                    val intent = Intent(requireContext(), GroupDetailActivity::class.java)
                    intent.putExtra("GROUP_ID", groupId.toLong())
                    intent.putExtra("GROUP_TYPE", "RELAY")
                    startActivity(intent)
                },
                onDeleteClick = {
                    showDeleteConfirmDialog()
                }
            ).show(requireActivity().supportFragmentManager, "GroupDeleteSheet")
        }

        binding.libDetailLatelyTv.setOnClickListener { sortCards(isLately = true) }
        binding.libDetailPageTv.setOnClickListener { sortCards(isLately = false) }

        val goAdd = View.OnClickListener {
            val fragment = LibraryAddCardFragment().apply { arguments = Bundle().apply { putInt("userBookId", userBookId) } }
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).addToBackStack(null).commit()
        }
        binding.libReviewAddBtn.setOnClickListener(goAdd)
        binding.libReviewNoAddBtn.setOnClickListener(goAdd)
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
                    requireContext().showCustomToast("그룹이 삭제되었습니다.", true)
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    requireContext().showCustomToast("삭제 실패", false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireContext().showCustomToast("오류가 발생했습니다.", false)
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}