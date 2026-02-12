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
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.PostCommentRequest
import com.bookiibookii.bookiibookii.databinding.FragmentLibCardBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class LibraryCardDetailFragment : Fragment() {

    private var _binding: FragmentLibCardBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog

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
        loadingDialog = LoadingDialog(requireContext())

        try {
            initView()
            initBottomSheet()
            initListeners()
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

                // ★ [수정 1] UTC 시간 변환 및 상대 시간 계산 로직 적용
                libCardBookDateTv.text = calculateTimeAgo(result.createdAt)

                libCardContentTv.text = result.memo

                // 만약 서버에서 작성자/프로필 정보를 내려준다면 여기서 갱신
                val apiWriterName = result.creatorName ?: writerName
                val apiProfileUrl = result.writerProfile ?: writerProfileUrl
                libCardProfileTv.text = apiWriterName
                Glide.with(requireContext())
                    .load(apiProfileUrl)
                    .placeholder(R.drawable.bg_round_10dp_gray300)
                    .error(R.drawable.img_profile_default)
                    .circleCrop()
                    .into(libCardProfileIv)

                // ★ [수정 2] 이미지 라운드 처리 방식 개선
                if (!currentImageUrl.isNullOrEmpty()) {
                    libCardImageIv.visibility = View.VISIBLE
                    Glide.with(requireContext())
                        .load(currentImageUrl)
                        // .apply() 대신 .transform() 체이닝 사용 (가장 확실한 방법)
                        .transform(CenterCrop(), RoundedCorners(dpToPx(20)))
                        .placeholder(R.drawable.bg_round_20dp_gray200)
                        .into(libCardImageIv)
                } else {
                    libCardImageIv.visibility = View.GONE
                }
            }
        }
    }

    // ★ [추가] UTC 시간을 받아서 상대 시간(방금 전, N분 전, N시간 전, 날짜)으로 변환하는 함수
    private fun calculateTimeAgo(serverTime: String): String {
        if (serverTime.isEmpty()) return ""
        try {
            // 1. 서버 시간 파싱 (UTC 기준)
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(serverTime) ?: return serverTime

            // 2. 현재 시간과 차이 계산 (밀리초)
            val now = System.currentTimeMillis()
            val diff = now - date.time

            val minutes = diff / (1000 * 60)
            val hours = minutes / 60
            val days = hours / 24

            return when {
                minutes < 1 -> "방금 전"
                minutes < 60 -> "${minutes}분 전"
                hours < 24 -> "${hours}시간 전"
                else -> {
                    // 3. 24시간 이상이면 날짜로 표시 (한국 시간 기준)
                    val formatter = SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault())
                    formatter.timeZone = TimeZone.getDefault() // 내 폰 시간대(KST)
                    formatter.format(date)
                }
            }
        } catch (e: Exception) {
            // 파싱 실패 시 기본 날짜 형식으로 반환 시도 (밀리초 포함된 포맷 등 예외 대응)
            return try {
                val fallbackParser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fallbackDate = fallbackParser.parse(serverTime)
                val formatter = SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault())
                formatter.format(fallbackDate ?: return serverTime)
            } catch (e2: Exception) {
                serverTime // 최후의 수단: 원본 그대로 반환
            }
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
        lifecycleScope.launch {
            loadingDialog.show()
            try {
                val request = PostCommentRequest(content)
                val response = RetrofitClient.api().postCardComment(cardId, request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    binding.includeChatBottom.etInput.setText("")
                    hideKeyboard()
                    fetchComments()
                } else {
                    Toast.makeText(context, "댓글 작성 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) { e.printStackTrace() }
            finally { if (loadingDialog.isShowing) loadingDialog.dismiss() }
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
            loadingDialog.show()
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

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}