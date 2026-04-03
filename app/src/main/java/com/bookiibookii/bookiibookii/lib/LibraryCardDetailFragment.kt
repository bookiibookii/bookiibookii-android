package com.bookiibookii.bookiibookii.lib

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.common.showCustomToast // ★ 커스텀 토스트 임포트
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.PostCommentRequest
import com.bookiibookii.bookiibookii.databinding.FragmentLibCardBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class LibraryCardDetailFragment : BaseDetailFragment<FragmentLibCardBinding>() {

    private lateinit var loadingDialog: LoadingDialog

    private val myPageViewModel: MyPageViewModel by activityViewModels()
    private var myNickname: String = ""

    private var cardId: Long = -1L
    private var isMine: Boolean = false

    private var writerName: String = ""
    private var writerProfileUrl: String? = null
    private var cardCreatorName: String? = null

    private var currentMemo: String = ""
    private var currentPage: Int = 0
    private var currentImageUrl: String? = null
    private var currentBookTitle: String = ""
    private var isBookmarked = false

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var chatAdapter: LibraryChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cardId = it.getLong("cardId", -1L)
            writerName = it.getString("writerName", "Unknown") ?: ""
            writerProfileUrl = it.getString("writerProfileUrl", null)
        }
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLibCardBinding {
        return FragmentLibCardBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        initMyData()

        try {
            initView()
            initBottomSheet()
            initListeners()
            loadInitialData()
        } catch (e: Exception) {
            requireContext().showCustomToast("화면을 불러오는 중 문제가 발생했습니다.", false)
        }
    }

    private fun initMyData() {
        myNickname = myPageViewModel.confirmedNickname ?: ""

        myPageViewModel.profileData.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                myNickname = profile.nickname
                checkOwnership()
            }
        }

        if (myPageViewModel.profileData.value == null) {
            myPageViewModel.fetchMypageData()
        }
    }

    private fun initView() {
        binding.libCardEditIv.visibility = View.GONE
        binding.libCardTrashIv.visibility = View.GONE

        binding.libCardProfileTv.text = writerName
        loadProfileImage(writerProfileUrl)

        chatAdapter = LibraryChatAdapter(emptyList())
        binding.includeChatBottom.libCardChatRv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }
    }

    private fun checkOwnership() {
        if (myNickname.isNotEmpty() && !cardCreatorName.isNullOrEmpty()) {
            isMine = (myNickname == cardCreatorName)

            if (isMine) {
                binding.libCardEditIv.visibility = View.VISIBLE
                binding.libCardTrashIv.visibility = View.VISIBLE
            } else {
                binding.libCardEditIv.visibility = View.GONE
                binding.libCardTrashIv.visibility = View.GONE
            }
        }
    }

    private fun loadProfileImage(url: String?) {
        Glide.with(requireContext())
            .load(url)
            .placeholder(R.drawable.bg_round_10dp_gray300)
            .error(R.drawable.img_profile_default)
            .transform(CenterCrop(), RoundedCorners(dpToPx(10)))
            .into(binding.libCardProfileIv)
    }

    private fun initBottomSheet() {
        val bottomSheet = binding.bottomSheetContainer
        val displayMetrics = resources.displayMetrics

        val baseHeight = (displayMetrics.heightPixels * 0.45).toInt()

        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = baseHeight
        bottomSheet.layoutParams = layoutParams

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.apply {
            peekHeight = dpToPx(80)
            state = BottomSheetBehavior.STATE_COLLAPSED
            isFitToContents = true
            isHideable = false
        }

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeVisible = insets.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime()).bottom

            val params = bottomSheet.layoutParams

            if (imeVisible) {
                params.height = baseHeight + imeHeight
                bottomSheet.setPadding(0, 0, 0, imeHeight)

                if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            } else {
                params.height = baseHeight
                bottomSheet.setPadding(0, 0, 0, 0)
            }

            bottomSheet.layoutParams = params
            bottomSheet.requestLayout()
            insets
        }
    }


    private fun initListeners() {
        binding.libCardBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        binding.libCardEditIv.setOnClickListener {
            val editFragment = LibraryAddCardFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isEdit", true)
                    putLong("cardId", cardId)
                    putInt("page", currentPage)
                    putString("memo", currentMemo)
                    putString("imageUrl", currentImageUrl)
                }
            }
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, editFragment).addToBackStack(null).commit()
        }

        binding.libCardTrashIv.setOnClickListener { showDeleteDialog() }

        binding.libCardShareIv.setOnClickListener {
            val shareFragment = LibraryShareFragment().apply {
                arguments = Bundle().apply {
                    putString("bookTitle", currentBookTitle)
                    putString("content", currentMemo)
                    putString("author", writerName)
                    putString("imageUrl", currentImageUrl)
                    putLong("cardId", cardId)
                }
            }
            shareFragment.show(requireActivity().supportFragmentManager, "ShareDialog")
        }

        binding.libCardBookIv.setOnClickListener { toggleBookmark() }

        binding.includeChatBottom.libDialogHandler.setOnClickListener { toggleBottomSheet() }
        binding.includeChatBottom.libCardChatTv.setOnClickListener { toggleBottomSheet() }

        binding.includeChatBottom.etInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.includeChatBottom.btnSend.setOnClickListener {
            val content = binding.includeChatBottom.etInput.text.toString()
            if (content.isNotBlank()) postComment(content)
            else requireContext().showCustomToast("내용을 입력해주세요.", false)
        }
    }

    private fun toggleBottomSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun loadInitialData() {
        if (cardId == -1L) return
        viewLifecycleOwner.lifecycleScope.launch {
            loadingDialog.show()
            try {
                fetchCardDetail()
                fetchComments()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }

    private suspend fun fetchCardDetail() {
        val response = RetrofitClient.api().getCardDetail(cardId)
        if (response.isSuccessful && response.body()?.isSuccess == true) {
            val result = response.body()?.result ?: return

            currentMemo = result.memo
            currentPage = result.page
            currentImageUrl = result.cardImage?.presignedGetUrl
            currentBookTitle = result.bookTitle

            cardCreatorName = result.creatorName
            checkOwnership()

            isBookmarked = result.isBookmarked ?: false
            binding.libCardBookIv.setImageResource(if (isBookmarked) R.drawable.ic_bookmark_orange else R.drawable.ic_bookmark_gray)

            with(binding) {
                libCardBookTitleTv.text = result.bookTitle
                libCardBookPageTv.text = "p.${result.page}"
                libCardBookDateTv.text = calculateTimeAgo(result.createdAt)
                libCardContentTv.text = result.memo

                val apiWriterName = result.creatorName ?: writerName
                libCardProfileTv.text = apiWriterName
                this@LibraryCardDetailFragment.writerName = apiWriterName

                try {
                    val profileResponse = RetrofitClient.api().getUserProfile(apiWriterName)
                    if (profileResponse.isSuccessful && profileResponse.body()?.isSuccess == true) {
                        val finalProfileUrl = profileResponse.body()?.result?.profileImageUrl
                        loadProfileImage(finalProfileUrl)
                        writerProfileUrl = finalProfileUrl
                    } else {
                        loadProfileImage(writerProfileUrl)
                    }
                } catch (e: Exception) {
                    loadProfileImage(writerProfileUrl)
                }

                if (!currentImageUrl.isNullOrEmpty()) {
                    libCardImageIv.visibility = View.VISIBLE
                    Glide.with(requireContext())
                        .load(currentImageUrl)
                        .transform(CenterCrop(), RoundedCorners(dpToPx(20)))
                        .placeholder(R.drawable.bg_round_20dp_gray200)
                        .into(libCardImageIv)
                } else {
                    libCardImageIv.visibility = View.GONE
                }
            }
        }
    }

    private fun calculateTimeAgo(serverTime: String): String {
        if (serverTime.isEmpty()) return ""
        try {
            val format = if (serverTime.contains(".")) "yyyy-MM-dd'T'HH:mm:ss.SSS" else "yyyy-MM-dd'T'HH:mm:ss"
            val parser = SimpleDateFormat(format, Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")

            val date = parser.parse(serverTime) ?: return serverTime

            val now = System.currentTimeMillis()
            val diff = now - date.time
            val minutes = diff / (1000 * 60)
            val hours = minutes / 60

            return when {
                minutes < 1 -> "방금 전"
                minutes < 60 -> "${minutes}분 전"
                hours < 24 -> "${hours}시간 전"
                else -> {
                    val formatter = SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault())
                    formatter.timeZone = TimeZone.getDefault()
                    formatter.format(date)
                }
            }
        } catch (e: Exception) {
            return serverTime
        }
    }

    private suspend fun fetchComments() {
        val response = RetrofitClient.api().getCardComments(cardId)
        if (response.isSuccessful && response.body()?.isSuccess == true) {
            val result = response.body()?.result ?: return
            binding.includeChatBottom.libCardChatTotalTv.text = "${result.totalCount}"
            chatAdapter.submitList(result.comments)
        }
    }

    private fun postComment(content: String) {
        if (cardId == -1L) return
        viewLifecycleOwner.lifecycleScope.launch {
            loadingDialog.show()
            try {
                val request = PostCommentRequest(content)
                val response = RetrofitClient.api().postCardComment(cardId, request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    binding.includeChatBottom.etInput.setText("")
                    hideKeyboard()
                    fetchComments()
                } else {
                    requireContext().showCustomToast("댓글 작성 실패", false)
                }
            } catch (e: Exception) { e.printStackTrace() }
            finally { if (loadingDialog.isShowing) loadingDialog.dismiss() }
        }
    }

    private fun toggleBookmark() {
        if (cardId == -1L) return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().toggleBookmark(cardId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    val newStatus = result?.bookmarked ?: !isBookmarked
                    isBookmarked = newStatus
                    binding.libCardBookIv.setImageResource(if (isBookmarked) R.drawable.ic_bookmark_orange else R.drawable.ic_bookmark_gray)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun showDeleteDialog() {
        CommonDialog(
            context = requireContext(),
            title = "독서카드 삭제",
            subtitle = "",
            content = "독서 카드를 삭제할까요? 이 작업은 되돌릴 수 없고, 내 서재에서만 삭제됩니다.",
            confirmBtnText = "삭제",
            confirmBtnColor = R.color.ui_point_red,
            onConfirmClick = { deleteCard() }
        ).show()
    }

    private fun deleteCard() {
        if (cardId == -1L) return
        viewLifecycleOwner.lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().deleteCard(cardId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    requireContext().showCustomToast("카드가 삭제되었습니다.", true)
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    var errorMessage = "삭제 실패"
                    val errorBodyString = response.errorBody()?.string()
                    if (!errorBodyString.isNullOrEmpty()) {
                        try {
                            val jsonObject = org.json.JSONObject(errorBodyString)
                            val serverMessage = jsonObject.optString("message")
                            if (serverMessage.isNotEmpty()) errorMessage = serverMessage
                        } catch (e: Exception) {}
                    }
                    requireContext().showCustomToast(errorMessage, false)
                }
            } catch (e: Exception) {
                requireContext().showCustomToast("오류가 발생했습니다.", false)
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.includeChatBottom.etInput.windowToken, 0)
        binding.includeChatBottom.etInput.clearFocus()
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}