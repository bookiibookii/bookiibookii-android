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
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.PostCommentRequest
import com.bookiibookii.bookiibookii.databinding.FragmentLibCardBinding
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch

class LibraryCardDetailFragment : Fragment() {

    private var _binding: FragmentLibCardBinding? = null
    private val binding get() = _binding!!

    // ==========================================
    // 1. 데이터 변수
    // ==========================================
    private var cardId: Long = -1L
    private var isMine: Boolean = false
    private var writerName: String = ""
    private var writerProfileUrl: String? = null

    // ★ 수정/공유를 위해 현재 데이터를 임시 저장하는 변수들 (누락 없음)
    private var currentMemo: String = ""
    private var currentPage: Int = 0
    private var currentImageUrl: String? = null
    private var currentBookTitle: String = ""

    // 로컬 상태 변수
    private var isBookmarked = false

    // 바텀시트 & 어댑터
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

        try {
            initView()        // UI 기본 설정
            initBottomSheet() // 바텀시트 설정 (40% 높이 유지)
            initListeners()   // 버튼 리스너

            // 데이터 로드
            fetchCardDetail()
            fetchComments()
        } catch (e: Exception) {
            Log.e("DetailError", "화면 초기화 중 오류 발생", e)
            Toast.makeText(context, "화면을 불러오는 중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // ==========================================
    // 2. UI 초기화
    // ==========================================
    private fun initView() {
        // [권한 체크] 내 글이면 수정/삭제 버튼 보이기
        if (isMine) {
            binding.libCardEditIv.visibility = View.VISIBLE
            binding.libCardTrashIv.visibility = View.VISIBLE
        } else {
            binding.libCardEditIv.visibility = View.GONE
            binding.libCardTrashIv.visibility = View.GONE
        }

        // 작성자 정보 바인딩
        binding.libCardProfileTv.text = writerName
        Glide.with(this)
            .load(writerProfileUrl)
            .placeholder(R.drawable.bg_round_10dp_gray300)
            .error(R.drawable.img_profile_default)
            .circleCrop()
            .into(binding.libCardProfileIv)

        // 댓글 어댑터 설정
        chatAdapter = LibraryChatAdapter(emptyList())
        binding.includeChatBottom.libCardChatRv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }
    }

    private fun initBottomSheet() {
        val bottomSheet = binding.bottomSheetContainer

        // 화면 높이의 40%로 설정 (기존 코드 유지)
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val maxHeight = (screenHeight * 0.4).toInt()

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

    // ==========================================
    // 3. 리스너 설정
    // ==========================================
    private fun initListeners() {
        binding.libCardBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // [수정] AddCardFragment로 데이터 전달 (누락 없음)
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
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, editFragment)
                .addToBackStack(null)
                .commit()
        }

        // [삭제]
        binding.libCardTrashIv.setOnClickListener { showDeleteDialog() }

        // [공유] ShareFragment로 데이터 전달 (누락 없음)
        binding.libCardShareIv.setOnClickListener {
            Log.d("ShareDebug", "공유 시도 - URL: $currentImageUrl")
            val shareFragment = LibraryShareFragment().apply {
                arguments = Bundle().apply {
                    putString("bookTitle", currentBookTitle)
                    putString("content", currentMemo)
                    putString("author", writerName)
                    putString("imageUrl", currentImageUrl)
                    putLong("cardId", cardId)
                }
            }
            shareFragment.show(parentFragmentManager, "ShareDialog")
        }

        // [북마크]
        binding.libCardBookIv.setOnClickListener { toggleBookmark() }

        // [바텀시트 제어]
        binding.includeChatBottom.libDialogHandler.setOnClickListener { toggleBottomSheet() }
        binding.includeChatBottom.libCardChatTv.setOnClickListener { toggleBottomSheet() }

        binding.includeChatBottom.etInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        // [댓글 전송]
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

    // ==========================================
    // 4. API 통신 로직
    // ==========================================

    // [상세 조회]
    private fun fetchCardDetail() {
        if (cardId == -1L) return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getCardDetail(cardId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result ?: return@launch

                    // ★ 데이터 저장 (수정/공유용)
                    currentMemo = result.memo
                    currentPage = result.page
                    currentImageUrl = result.cardImage?.presignedGetUrl
                    currentBookTitle = result.bookTitle

                    // 북마크 상태
                    isBookmarked = result.isBookmarked ?: false
                    binding.libCardBookIv.setImageResource(
                        if (isBookmarked) R.drawable.ic_bookmark_orange
                        else R.drawable.ic_bookmark_gray
                    )

                    // 화면 바인딩
                    with(binding) {
                        libCardBookTitleTv.text = result.bookTitle
                        libCardBookPageTv.text = "p.${result.page}"
                        if (result.createdAt.length >= 10) {
                            libCardBookDateTv.text = result.createdAt.substring(0, 10).replace("-", ".")
                        }

                        // 메모 표시
                        libCardContentTv.text = result.memo

                        // 이미지 표시 (Radius 적용)
                        if (!currentImageUrl.isNullOrEmpty()) {
                            libCardImageIv.visibility = View.VISIBLE
                            Glide.with(requireContext())
                                .load(currentImageUrl)
                                .apply(RequestOptions.bitmapTransform(
                                    MultiTransformation(CenterCrop(), RoundedCorners(dpToPx(20)))
                                ))
                                .placeholder(R.drawable.bg_round_20dp_gray200)
                                .into(libCardImageIv)
                        } else {
                            libCardImageIv.visibility = View.GONE
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DetailError", "상세 정보 로드 실패", e)
            }
        }
    }

    // [댓글 목록]
    private fun fetchComments() {
        if (cardId == -1L) return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getCardComments(cardId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result ?: return@launch
                    binding.includeChatBottom.libCardChatTotalTv.text = "${result.totalCount}"
                    chatAdapter.submitList(result.comments)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // [댓글 작성]
    private fun postComment(content: String) {
        if (cardId == -1L) return
        lifecycleScope.launch {
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
        }
    }

    // [북마크 토글]
    private fun toggleBookmark() {
        if (cardId == -1L) return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().toggleBookmark(cardId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    val newStatus = result?.bookmarked ?: !isBookmarked
                    isBookmarked = newStatus
                    binding.libCardBookIv.setImageResource(
                        if (isBookmarked) R.drawable.ic_bookmark_orange
                        else R.drawable.ic_bookmark_gray
                    )
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // [삭제] - 로그 강화됨
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
        if (cardId == -1L) {
            Toast.makeText(context, "카드 정보 오류", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // API 호출
                val response = RetrofitClient.api().deleteCard(cardId)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Toast.makeText(context, "카드가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    // ★ [수정됨] 에러 메시지 파싱 로직 추가
                    var errorMessage = "삭제 실패" // 기본 메시지

                    val errorBodyString = response.errorBody()?.string() // 에러 바디 읽기

                    if (!errorBodyString.isNullOrEmpty()) {
                        try {
                            // JSON 파싱해서 "message" 부분만 꺼내기
                            val jsonObject = org.json.JSONObject(errorBodyString)
                            val serverMessage = jsonObject.optString("message")
                            if (serverMessage.isNotEmpty()) {
                                errorMessage = serverMessage
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    Log.e("DeleteDebug", "삭제 실패: $errorMessage, Raw: $errorBodyString")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DeleteDebug", "삭제 중 예외 발생", e)
                Toast.makeText(context, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // ==========================================
    // 5. 유틸리티 함수
    // ==========================================
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.includeChatBottom.etInput.windowToken, 0)
        binding.includeChatBottom.etInput.clearFocus()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}