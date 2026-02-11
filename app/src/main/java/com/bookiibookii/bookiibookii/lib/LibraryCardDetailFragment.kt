package com.bookiibookii.bookiibookii.lib

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.common.LoadingDialog // ★ 로딩 다이얼로그 import
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.PostCommentRequest
import com.bookiibookii.bookiibookii.databinding.FragmentLibCardBinding
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch

class LibraryCardDetailFragment : Fragment() {

    private var _binding: FragmentLibCardBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog // ★ 로딩 선언

    private var cardId: Long = -1L
    private var isMine: Boolean = false
    private var writerName: String = ""
    private var writerProfileUrl: String? = null

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
            isMine = it.getBoolean("isMine", false)
            writerName = it.getString("writerName", "Unknown") ?: ""
            writerProfileUrl = it.getString("writerProfileUrl", null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext()) // ★ 초기화

        try {
            initView()
            initBottomSheet()
            initListeners()

            // ★ 두 API를 하나의 로딩 안에서 처리
            loadInitialData()
        } catch (e: Exception) {
            Toast.makeText(context, "화면을 불러오는 중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initView() {
        if (isMine) {
            binding.libCardEditIv.visibility = View.VISIBLE
            binding.libCardTrashIv.visibility = View.VISIBLE
        } else {
            binding.libCardEditIv.visibility = View.GONE
            binding.libCardTrashIv.visibility = View.GONE
        }

        binding.libCardProfileTv.text = writerName
        Glide.with(this).load(writerProfileUrl).placeholder(R.drawable.bg_round_10dp_gray300)
            .error(R.drawable.img_profile_default).circleCrop().into(binding.libCardProfileIv)

        chatAdapter = LibraryChatAdapter(emptyList())
        binding.includeChatBottom.libCardChatRv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }
    }

    private fun initBottomSheet() {
        val bottomSheet = binding.bottomSheetContainer
        val displayMetrics = resources.displayMetrics
        val maxHeight = (displayMetrics.heightPixels * 0.4).toInt()
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = maxHeight
        bottomSheet.layoutParams = layoutParams

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.apply {
            peekHeight = dpToPx(80)
            state = BottomSheetBehavior.STATE_COLLAPSED
            isFitToContents = true
            isHideable = false
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
            else Toast.makeText(context, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleBottomSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    // ★ 두 API를 순차적으로 부르고 로딩창 끄는 통합 함수
    private fun loadInitialData() {
        if (cardId == -1L) return
        lifecycleScope.launch {
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

    // ★ suspend 적용
// ★ suspend 적용
    private suspend fun fetchCardDetail() {
        val response = RetrofitClient.api().getCardDetail(cardId)
        if (response.isSuccessful && response.body()?.isSuccess == true) {
            val result = response.body()?.result ?: return

            currentMemo = result.memo
            currentPage = result.page
            currentImageUrl = result.cardImage?.presignedGetUrl
            currentBookTitle = result.bookTitle

            isBookmarked = result.isBookmarked ?: false
            binding.libCardBookIv.setImageResource(if (isBookmarked) R.drawable.ic_bookmark_orange else R.drawable.ic_bookmark_gray)

            with(binding) {
                libCardBookTitleTv.text = result.bookTitle
                libCardBookPageTv.text = "p.${result.page}"
                if (result.createdAt.length >= 10) libCardBookDateTv.text = result.createdAt.substring(0, 10).replace("-", ".")
                libCardContentTv.text = result.memo

                // ★ [추가된 부분] 서버에서 받아온 상세 정보로 프로필/이름 덮어쓰기
                // 주의: result.creatorName, result.creatorProfileImageUrl 은 임의로 적은 것입니다.
                // 실제 서버 API 응답 모델(DTO)에 있는 필드명으로 꼭 맞춰서 변경해 주세요!
                val apiWriterName = result.creatorName ?: writerName
                val apiProfileUrl = result.writerProfile ?: writerProfileUrl

                libCardProfileTv.text = apiWriterName

                Glide.with(requireContext())
                    .load(apiProfileUrl)
                    .placeholder(R.drawable.bg_round_10dp_gray300)
                    .error(R.drawable.img_profile_default)
                    .circleCrop()
                    .into(libCardProfileIv)

                // (아래는 기존 카드 이미지 처리 코드 그대로 유지)
                if (!currentImageUrl.isNullOrEmpty()) {
                    libCardImageIv.visibility = View.VISIBLE
                    Glide.with(requireContext()).load(currentImageUrl)
                        .apply(RequestOptions.bitmapTransform(MultiTransformation(CenterCrop(), RoundedCorners(dpToPx(20)))))
                        .placeholder(R.drawable.bg_round_20dp_gray200).into(libCardImageIv)
                } else {
                    libCardImageIv.visibility = View.GONE
                }
            }
        }
    }
    // ★ suspend 적용
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
        lifecycleScope.launch {
            loadingDialog.show() // ★ 댓글 작성 로딩 시작
            try {
                val request = PostCommentRequest(content)
                val response = RetrofitClient.api().postCardComment(cardId, request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    binding.includeChatBottom.etInput.setText("")
                    hideKeyboard()
                    fetchComments() // 댓글 다시 불러오기
                } else {
                    Toast.makeText(context, "댓글 작성 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) { e.printStackTrace() }
            finally { if (loadingDialog.isShowing) loadingDialog.dismiss() } // ★ 로딩 끝
        }
    }

    private fun toggleBookmark() {
        if (cardId == -1L) return
        lifecycleScope.launch {
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
            title = "카드 삭제",
            subtitle = "",
            content = "정말로 이 카드를 삭제하시겠습니까?\n삭제 후에는 복구할 수 없습니다.",
            confirmBtnText = "삭제",
            confirmBtnColor = R.color.ui_point_red,
            onConfirmClick = { deleteCard() }
        ).show()
    }

    private fun deleteCard() {
        if (cardId == -1L) return
        lifecycleScope.launch {
            loadingDialog.show() // ★ 삭제 로딩 시작
            try {
                val response = RetrofitClient.api().deleteCard(cardId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(context, "카드가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss() // ★ 삭제 로딩 끝
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.includeChatBottom.etInput.windowToken, 0)
        binding.includeChatBottom.etInput.clearFocus()
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}