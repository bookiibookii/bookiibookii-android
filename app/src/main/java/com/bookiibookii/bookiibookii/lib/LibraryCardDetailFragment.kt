package com.bookiibookii.bookiibookii.lib

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.PostCommentRequest
import com.bookiibookii.bookiibookii.databinding.FragmentLibCardBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch

class LibraryCardDetailFragment : Fragment() {

    private var _binding: FragmentLibCardBinding? = null
    private val binding get() = _binding!!

    // 데이터
    private var cardId: Long = -1L
    private var isMine: Boolean = false
    private var writerName: String = ""
    private var writerProfileUrl: String? = null

    // 로컬 상태
    private var isBookmarked = false

    // 바텀시트
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

        initView()
        initBottomSheet()
        initListeners()

        // 데이터 로드
        fetchCardDetail()
        fetchComments()
    }

    private fun initView() {
        // 내 글이면 수정/삭제 노출
        if (isMine) {
            binding.libCardEditIv.visibility = View.VISIBLE
            binding.libCardTrashIv.visibility = View.VISIBLE
        } else {
            binding.libCardEditIv.visibility = View.GONE
            binding.libCardTrashIv.visibility = View.GONE
        }

        // 작성자 정보 우선 바인딩
        binding.libCardProfileTv.text = writerName
        Glide.with(this)
            .load(writerProfileUrl)
            .placeholder(R.drawable.bg_round_10dp_gray300)
            .circleCrop()
            .into(binding.libCardProfileIv)

        // 댓글 리스트 설정
        chatAdapter = LibraryChatAdapter(emptyList())
        binding.includeChatBottom.libCardChatRv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }
    }

    private fun initBottomSheet() {
        val bottomSheet = binding.bottomSheetContainer

        // 화면 높이의 60%로 바텀시트 최대 높이 설정
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val targetHeight = (screenHeight * 0.6).toInt()

        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = targetHeight
        bottomSheet.layoutParams = layoutParams

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.apply {
            peekHeight = dpToPx(80) // 핸들러와 제목만 보이는 높이
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun toggleBottomSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun initListeners() {
        binding.libCardBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 수정/삭제/공유/북마크 로직 (기존 유지)
        binding.libCardEditIv.setOnClickListener {
            val editFragment = LibraryAddCardFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isEdit", true)
                    putLong("cardId", cardId)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, editFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.libCardTrashIv.setOnClickListener { showDeleteDialog() }

        binding.libCardShareIv.setOnClickListener {
            LibraryShareFragment().show(parentFragmentManager, "ShareDialog")
        }

        binding.libCardBookIv.setOnClickListener {
            toggleBookmark()
        }

        // 바텀시트 제어
        binding.includeChatBottom.libDialogHandler.setOnClickListener { toggleBottomSheet() }
        binding.includeChatBottom.libCardChatTv.setOnClickListener { toggleBottomSheet() }

        // 댓글 입력창 포커스 시 바텀시트 확장
        binding.includeChatBottom.etInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        // ★ [추가] 댓글 전송 버튼 클릭
        binding.includeChatBottom.btnSend.setOnClickListener {
            val content = binding.includeChatBottom.etInput.text.toString()
            if (content.isNotBlank()) {
                postComment(content)
            } else {
                Toast.makeText(context, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleBookmark() {
        if (cardId == -1L) return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().toggleBookmark(cardId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    val newStatus = result?.bookmarked ?: !isBookmarked // 서버 응답 우선, 없으면 토글

                    isBookmarked = newStatus
                    binding.libCardBookIv.setImageResource(
                        if (isBookmarked) R.drawable.ic_bookmark_orange
                        else R.drawable.ic_bookmark_gray
                    )
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

// [API] 카드 상세 정보 조회
    private fun fetchCardDetail() {
        if (cardId == -1L) return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getCardDetail(cardId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result ?: return@launch

                    // [추가] 북마크 상태 동기화
                    isBookmarked = result.isBookmarked ?: false // 서버에서 null이면 false 처리
                    binding.libCardBookIv.setImageResource(
                        if (isBookmarked) R.drawable.ic_bookmark_orange
                        else R.drawable.ic_bookmark_gray
                    )

                    with(binding) {
                        libCardBookTitleTv.text = result.bookTitle
                        libCardBookPageTv.text = "p.${result.page}"
                        // 날짜 포맷팅
                        if (result.createdAt.length >= 10) {
                            libCardBookDateTv.text = result.createdAt.substring(0, 10).replace("-", ".")
                        }

                        // 메모 내용 바인딩 (XML ID 확인 필요, 보통 libCardMemoTv 같은 것이 있어야 함)
                        // binding.libCardContentTv.text = result.memo

                        // 이미지
                        if (result.cardImage != null) {
                            libCardImageIv.visibility = View.VISIBLE
                            Glide.with(requireContext())
                                .load(result.cardImage.presignedGetUrl)
                                .placeholder(R.drawable.bg_round_20dp_gray200)
                                .into(libCardImageIv)
                        } else {
                            libCardImageIv.visibility = View.GONE
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
    // [API] 댓글 목록 조회
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

    // [API] ★ 댓글 작성
    private fun postComment(content: String) {
        if (cardId == -1L) return
        lifecycleScope.launch {
            try {
                val request = PostCommentRequest(content)
                val response = RetrofitClient.api().postCardComment(cardId, request)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    // 1. 입력창 초기화
                    binding.includeChatBottom.etInput.setText("")

                    // 2. 키보드 내리기
                    hideKeyboard()

                    // 3. 목록 갱신 (새 댓글 포함)
                    fetchComments()
                } else {
                    Toast.makeText(context, "댓글 작성 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.includeChatBottom.etInput.windowToken, 0)
        binding.includeChatBottom.etInput.clearFocus()
    }

    private fun showDeleteDialog() {
        CommonDialog(
            context = requireContext(),
            title = "카드 삭제",
            subtitle = "",
            content = "정말로 이 카드를 삭제하시겠습니까?\n삭제 후에는 복구할 수 없습니다.",
            confirmBtnText = "삭제",
            confirmBtnColor = R.color.ui_point_red,
            onConfirmClick = {
                // TODO: 삭제 API 호출
                parentFragmentManager.popBackStack()
            }
        ).show()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}