package com.bookiibookii.bookiibookii.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailBinding
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.coroutines.launch

class LibraryBookDetailFragment : Fragment() {

    private var _binding: FragmentLibBookDetailBinding? = null
    private val binding get() = _binding!!

    // ★ [1] ViewModel 공유 (MyPageViewModel)
    private val myPageViewModel: MyPageViewModel by activityViewModels()

    private var groupId: Int = -1
    private var userBookId: Int = -1
    private var bookTitle = ""
    private var bookAuthor = ""
    private var bookCover = ""
    private var hostName = ""
    private var hostProfileUrl = ""

    // ★ [추가] 날짜 및 평점 정보
    private var startDate = ""
    private var endDate = ""
    private var rating: Double = 0.0

    // ★ [2] 내 닉네임 변수 (기본값 설정)
    private var myNickname = "나"

    private lateinit var cardAdapter: LibraryReviewAdapter
    private var originalList: List<CardItem> = emptyList()

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

            // ★ [추가] 전달받은 데이터 꺼내기
            startDate = it.getString("startDate", "") ?: ""
            endDate = it.getString("endDate", "") ?: ""
            rating = it.getDouble("rating", 0.0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ★ [3] 뷰모델에서 내 닉네임 가져오기
        setupMyProfileData()

        initView()
        initRecyclerView()
        initListeners()
        fetchData()
    }

    // ★ [4] 닉네임 로드 함수
    private fun setupMyProfileData() {
        myPageViewModel.profileData.value?.let { myNickname = it.nickname }
        myPageViewModel.profileData.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                myNickname = profile.nickname
                if (_binding != null) binding.libDetailReviewName1Tv.text = myNickname
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

        // 마키 효과
        binding.libDetailBookTitleTv.isSelected = true
        binding.libDetailBookAuthorTv.isSelected = true

        Glide.with(this).load(bookCover).placeholder(R.drawable.bg_round_20dp_gray200)
            .transform(CenterCrop(), RoundedCorners(dpToPx(10))).into(binding.libDetailImageIv)

        Glide.with(this)
            .load(hostProfileUrl)
            .placeholder(R.drawable.bg_round_20dp_gray200)
            .error(R.drawable.img_profile_default)
            .circleCrop()
            .into(binding.libDetailProfileIv)

        binding.libDetailProfileTv.text = hostName

        // ★ [핵심] 평점 유무에 따른 UI 분기 (0.0이면 아예 안 보이게)
        if (rating > 0.0) {
            // [상태 A] 후기 있음 (완료됨) -> 별점 리스트 표시
            binding.libDetailDateTv.text = if (endDate.isNotEmpty()) "$startDate ~ $endDate" else "$startDate ~"

            binding.libDetailRateList.visibility = View.VISIBLE
            setRatingStars(rating)
        } else {
            // [상태 B] 후기 없음 (진행 중) -> 별점 리스트 숨김 (GONE)
            binding.libDetailDateTv.text = "$startDate ~"

            binding.libDetailRateList.visibility = View.GONE
        }

        // 초기 뷰 상태 설정
        binding.groupReviewSection.visibility = View.GONE
        binding.bookDetailNoCardHost.visibility = View.GONE
        binding.bookDetailNoCardGuest.visibility = View.GONE
        binding.libReviewAddBtn.visibility = View.GONE
    }

    // 별점 채우기 함수
    private fun setRatingStars(score: Double) {
        val scoreInt = score.toInt()
        val container = binding.libDetailRateList

        for (i in 0 until container.childCount) {
            val star = container.getChildAt(i) as? ImageView
            if (i < scoreInt) {
                star?.setImageResource(R.drawable.ic_star_filled)
            } else {
                star?.setImageResource(R.drawable.ic_star_none)
            }
        }
    }

    private fun fetchData() {
        if (groupId == -1) return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getGroupCards(groupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result ?: return@launch

                    binding.libDetailReviewName1Tv.text = myNickname
                    binding.libDetailReviewText1Tv.text = result.myComment ?: "\"아직 한줄 평을 남기지 않았어요.\""

                    binding.libDetailReviewName2Tv.text = hostName
                    binding.libDetailReviewText2Tv.text = result.partnerComment ?: "\"아직 한줄 평을 남기지 않았어요.\""

                    val apiCards = result.cards
                    originalList = apiCards
                    cardAdapter.submitList(originalList.sortedByDescending { it.createdAt })
                    binding.libDetailTotalTv.text = "${apiCards.size}개"

                    if (apiCards.isNotEmpty()) {
                        binding.groupReviewSection.visibility = View.VISIBLE
                        binding.libReviewAddBtn.visibility = View.VISIBLE
                        binding.bookDetailNoCardHost.visibility = View.GONE
                        binding.bookDetailNoCardGuest.visibility = View.GONE
                    } else {
                        binding.groupReviewSection.visibility = View.GONE
                        binding.libReviewAddBtn.visibility = View.GONE

                        val ownerNickname = result.currentBookOwner?.nickname
                        val isMyTurn = (ownerNickname != null && ownerNickname == myNickname)

                        if (isMyTurn) {
                            binding.bookDetailNoCardHost.visibility = View.VISIBLE
                            binding.bookDetailNoCardGuest.visibility = View.GONE
                        } else {
                            binding.bookDetailNoCardHost.visibility = View.GONE
                            binding.bookDetailNoCardGuest.visibility = View.VISIBLE
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun initRecyclerView() {
        cardAdapter = LibraryReviewAdapter(
            onItemClick = { clickedCard ->
                val detailFragment = LibraryCardDetailFragment().apply {
                    arguments = Bundle().apply {
                        putLong("cardId", clickedCard.cardId.toLong())
                        val isMine = clickedCard.creatorName == myNickname
                        putBoolean("isMine", isMine)
                        putString("writerName", clickedCard.creatorName)
                    }
                }
                parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, detailFragment).addToBackStack(null).commit()
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

        binding.libDetailLatelyTv.setOnClickListener { cardAdapter.submitList(originalList.sortedByDescending { it.createdAt }) }
        binding.libDetailPageTv.setOnClickListener { cardAdapter.submitList(originalList.sortedBy { it.page }) }

        val goAdd = View.OnClickListener {
            val fragment = LibraryAddCardFragment().apply { arguments = Bundle().apply { putInt("userBookId", userBookId) } }
            parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).addToBackStack(null).commit()
        }
        binding.libReviewAddBtn.setOnClickListener(goAdd)
        binding.libReviewNoAddBtn.setOnClickListener(goAdd)
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
                } else {
                    Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show()
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