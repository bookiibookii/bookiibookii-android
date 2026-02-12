package com.bookiibookii.bookiibookii.lib

import android.content.Intent
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
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.CardItem
import com.bookiibookii.bookiibookii.databinding.FragmentLibBookDetailBinding
import com.bookiibookii.bookiibookii.group.GroupDetailActivity
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class LibraryBookDetailFragment : Fragment() {

    private var _binding: FragmentLibBookDetailBinding? = null
    private val binding get() = _binding!!

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

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

        val formattedStart = formatDate(startDate)
        val formattedEnd = formatDate(endDate)

        binding.libDetailDateTv.text = if (formattedEnd.isNotEmpty()) "$formattedStart ~ $formattedEnd" else "$formattedStart ~"

        // 별점만 유무에 따라 띄웁니다.
        // 별점만 유무에 따라 띄웁니다.
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

    private fun formatDate(dateString: String): String {
        if (dateString.isEmpty()) return ""
        return try {
            // 1. 서버 UTC 시간 파싱
            val format = if (dateString.contains(".")) "yyyy-MM-dd'T'HH:mm:ss.SSS" else "yyyy-MM-dd'T'HH:mm:ss"
            val parser = SimpleDateFormat(format, Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(dateString) ?: return dateString

            // 2. 한국 시간 포맷으로 변환
            val formatter = SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault())
            formatter.timeZone = TimeZone.getDefault()
            formatter.format(date)
        } catch (e: Exception) {
            dateString // 변환 실패 시 원본 사용
        }
    }

    private fun fetchData() {
        if (groupId == -1) return
        lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().getGroupCards(groupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result ?: return@launch

                    // 1. 내 후기 바인딩
                    binding.libDetailReviewName1Tv.text = myNickname
                    binding.libDetailReviewText1Tv.text = result.myComment ?: "\"아직 한줄 평을 남기지 않았어요.\""

                    // 2. 상대방 후기 바인딩 (수정된 부분)
                    // togetherComments 리스트에서 '나'가 아닌 사람을 찾습니다.
                    val partnerItem = result.togetherComments?.find { it.nickname != myNickname }

                    // 파트너를 찾았으면 그 닉네임, 못 찾았는데 내가 호스트가 아니면 호스트 이름, 그것도 아니면 "상대방"
                    val partnerName = partnerItem?.nickname
                        ?: if (myNickname != hostName) hostName else "상대방"

                    binding.libDetailReviewName2Tv.text = partnerName
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
            finally { if (loadingDialog.isShowing) loadingDialog.dismiss() }
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
        binding.libDetailBackIv.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        binding.libDetailMoreIv.setOnClickListener {
            LibraryGroupDeleteBottomSheet(
                onDetailClick = {
                    val intent = Intent(requireContext(), GroupDetailActivity::class.java)
                    intent.putExtra("GROUP_ID", groupId.toLong())
                    intent.putExtra("GROUP_TYPE", "RELAY") // 이어읽기 완료이므로 RELAY
                    startActivity(intent)
                },
                onDeleteClick = {
                    showDeleteConfirmDialog()
                }
            ).show(requireActivity().supportFragmentManager, "GroupDeleteSheet")
        }

        binding.libDetailLatelyTv.setOnClickListener { cardAdapter.submitList(originalList.sortedByDescending { it.createdAt }) }
        binding.libDetailPageTv.setOnClickListener { cardAdapter.submitList(originalList.sortedBy { it.page }) }

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
            content = "그룹을 정말 삭제하시겠습니까?\n이 작업은 되돌릴 수 없고, 내 서재에서만 삭제됩니다.",
            confirmBtnText = "삭제",
            confirmBtnColor = R.color.ui_point_red,
            onConfirmClick = { deleteGroup() }
        ).show()
    }

    private fun deleteGroup() {
        if (userBookId == -1) return
        lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().deleteGroup(userBookId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(context, "그룹이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) { e.printStackTrace() }
            finally { if (loadingDialog.isShowing) loadingDialog.dismiss() }
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