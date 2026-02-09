package com.bookiibookii.bookiibookii.lib

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.CardDetailResult
import com.bookiibookii.bookiibookii.data.model.CommentItem
import com.bookiibookii.bookiibookii.databinding.FragmentLibCardBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch

class LibraryCardDetailFragment : Fragment() {

    private var _binding: FragmentLibCardBinding? = null
    private val binding get() = _binding!!

    // 이전 화면에서 받아올 데이터
    private var cardId: Long = -1L
    private var isMine: Boolean = false
    private var writerName: String = ""
    private var writerProfileUrl: String? = null

    // 로컬 상태
    private var isBookmarked = false

    // 바텀시트 및 어댑터
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var chatAdapter: LibraryChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Argument 수신 (가정: 리스트 화면에서 이 정보들을 넘겨줌)
        arguments?.let {
            cardId = it.getLong("cardId", -1L)
            isMine = it.getBoolean("isMine", false)
            writerName = it.getString("writerName", "Unknown")
            writerProfileUrl = it.getString("writerProfileUrl", null)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()        // 기본 UI 설정 (수정/삭제 버튼 등)
        initBottomSheet() // ★ 바텀시트 높이 제한 설정
        initListeners()   // 클릭 리스너 연결

        // 데이터 로드
        fetchCardDetail()
        fetchComments()
    }

    // [1] 기본 뷰 초기화 (기존 로직 유지)
    private fun initView() {
        // 내 글이면 수정/삭제 보임
        if (isMine) {
            binding.libCardEditIv.visibility = View.VISIBLE
            binding.libCardTrashIv.visibility = View.VISIBLE
        } else {
            binding.libCardEditIv.visibility = View.GONE
            binding.libCardTrashIv.visibility = View.GONE
        }

        // 이전 화면에서 받은 작성자 정보 우선 바인딩
        binding.libCardProfileTv.text = writerName
        Glide.with(this)
            .load(writerProfileUrl)
            .placeholder(R.drawable.bg_round_10dp_gray300)
            .circleCrop()
            .into(binding.libCardProfileIv)

        // 댓글 어댑터 초기화
        chatAdapter = LibraryChatAdapter(emptyList())
        binding.includeChatBottom.libCardChatRv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }
    }

    // [2] 바텀시트 초기화 (★ 높이 제한 로직 적용)
    private fun initBottomSheet() {
        val bottomSheet = binding.bottomSheetContainer

        // 1. 화면 높이 구하기
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels

        // 2. 바텀시트의 최대 높이를 화면의 60%로 강제 설정 (절반보다 조금 더 위)
        val targetHeight = (screenHeight * 0.6).toInt()

        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = targetHeight
        bottomSheet.layoutParams = layoutParams

        // 3. Behavior 설정
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.apply {
            // 접혔을 때 높이 (핸들바 + 제목 정도만 보이게) - XML의 peekHeight와 맞춤
            peekHeight = dpToPx(80)
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

    // [3] 리스너 설정 (기존 기능 유지)
    private fun initListeners() {
        // 뒤로가기
        binding.libCardBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 수정 버튼
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

        // 삭제 버튼
        binding.libCardTrashIv.setOnClickListener {
            showDeleteDialog()
        }

        // 공유 버튼
        binding.libCardShareIv.setOnClickListener {
            val shareDialog = LibraryShareFragment()
            shareDialog.show(parentFragmentManager, "ShareDialog")
        }

        // 북마크 토글 (로컬 상태)
        binding.libCardBookIv.setOnClickListener {
            isBookmarked = !isBookmarked
            binding.libCardBookIv.setImageResource(
                if (isBookmarked) R.drawable.ic_bookmark_orange else R.drawable.ic_bookmark_gray
            )
        }

        // 바텀시트 제어 (핸들바, 제목 클릭)
        binding.includeChatBottom.libDialogHandler.setOnClickListener { toggleBottomSheet() }
        binding.includeChatBottom.libCardChatTv.setOnClickListener { toggleBottomSheet() }

        // 댓글 입력창 포커스 시 자동으로 펼치기
        binding.includeChatBottom.etInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    // [4] API 연동: 카드 상세 정보
    private fun fetchCardDetail() {
        if (cardId == -1L) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getCardDetail(cardId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result ?: return@launch

                    with(binding) {
                        libCardBookTitleTv.text = result.bookTitle
                        libCardBookPageTv.text = "p.${result.page}"
                        // 날짜 포맷팅 (예: 2026-02-06T... -> 2026.02.06)
                        libCardBookDateTv.text = result.createdAt.substring(0, 10).replace("-", ".")

                        // ※ 중요: 본문(memo)을 표시할 TextView가 XML에 없어서 추가가 필요할 수 있습니다.
                        // 만약 libCardBookTitleTv 아래 등에 표시한다면:
                        // libCardMemoTv.text = result.memo

                        // 카드 이미지
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // [5] API 연동: 댓글 목록
    private fun fetchComments() {
        if (cardId == -1L) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getCardComments(cardId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result ?: return@launch

                    // 댓글 개수 갱신
                    binding.includeChatBottom.libCardChatTotalTv.text = "${result.totalCount}"

                    // 리스트 갱신
                    chatAdapter.submitList(result.comments)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
            onConfirmClick = {
                // TODO: 삭제 API 호출 필요
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