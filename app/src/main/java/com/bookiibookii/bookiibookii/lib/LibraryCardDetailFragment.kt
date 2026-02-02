package com.bookiibookii.bookiibookii.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.databinding.FragmentLibCardBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class LibraryCardDetailFragment : Fragment() {

    private var _binding: FragmentLibCardBinding? = null
    private val binding get() = _binding!!

    private var isMine = false
    private var isBookmarked = false

    // 바텀시트 Behavior 제어용 변수
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isMine = arguments?.getBoolean("isMine", false) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initBottomSheet() // 바텀시트 초기화
        initChatList()    // 댓글 리스트 초기화
        initListeners()
    }

    private fun initView() {
        if (isMine) {
            binding.libCardEditIv.visibility = View.VISIBLE
            binding.libCardTrashIv.visibility = View.VISIBLE
        } else {
            binding.libCardEditIv.visibility = View.GONE
            binding.libCardTrashIv.visibility = View.GONE
        }
    }

    private fun initBottomSheet() {
        // CoordinatorLayout 내의 FrameLayout(바텀시트 컨테이너) 가져오기
        val bottomSheet = binding.bottomSheetContainer
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        bottomSheetBehavior.apply {
            // 내부 콘텐츠 크기에 딱 맞게 펼쳐지도록 설정
            isFitToContents = true

            // 절반만 펼쳐지는 상태(Half Expanded)를 사용하지 않음
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    // 필요 시 상태 변경 로직
                }
                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }

        // 초기 상태: 접힘 (PeekHeight 만큼만 보임)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        binding.includeChatBottom.libDialogHandler.setOnClickListener {
            toggleBottomSheet()
        }
        binding.includeChatBottom.libCardChatTv.setOnClickListener {
            toggleBottomSheet()
        }
        binding.libCardShareIv.setOnClickListener {
            val shareDialog = LibraryShareFragment()
            // val bundle = Bundle()
            // bundle.putString("title", "책 제목")
            // shareDialog.arguments = bundle

            shareDialog.show(parentFragmentManager, "ShareDialog")
        }
    }

    private fun toggleBottomSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun initChatList() {
        // 더미 댓글 데이터 연결
        val chatList = listOf("정말 공감가는 구절이네요!", "글씨체가 너무 예쁘세요.")
        val adapter = LibraryChatAdapter(chatList)

        // include된 레이아웃에 접근 (binding.includeChatBottom.아이디)
        binding.includeChatBottom.libCardChatRv.layoutManager = LinearLayoutManager(context)
        binding.includeChatBottom.libCardChatRv.adapter = adapter
        binding.includeChatBottom.libCardChatTotalTv.text = "${chatList.size}"
    }

    private fun initListeners() {
        binding.libCardBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.libCardEditIv.setOnClickListener {
            val editFragment = LibraryAddCardFragment().apply {
                arguments = Bundle().apply { putBoolean("isEdit", true) }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, editFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.libCardTrashIv.setOnClickListener {
            showDeleteDialog()
        }

        binding.libCardBookIv.setOnClickListener {
            isBookmarked = !isBookmarked
            binding.libCardBookIv.setImageResource(
                if (isBookmarked) R.drawable.ic_bookmark_orange else R.drawable.ic_bookmark_gray
            )
        }

        // 댓글 입력창 포커스 시 자동으로 펼치기
        binding.includeChatBottom.etInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
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
            onConfirmClick = { parentFragmentManager.popBackStack() }
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}