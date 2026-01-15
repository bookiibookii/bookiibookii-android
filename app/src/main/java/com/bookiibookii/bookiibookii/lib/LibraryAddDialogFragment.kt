package com.bookiibookii.bookiibookii.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.LibReview
import com.bookiibookii.bookiibookii.bookData.viewModel.ReviewModel
import com.bookiibookii.bookiibookii.databinding.FragmentLibAddDialogBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LibraryAddDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentLibAddDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReviewModel by activityViewModels()

    private var currentReviewId: Long = -1

    companion object {
        fun newInstance(reviewId: Long): LibraryAddDialogFragment {
            val fragment = LibraryAddDialogFragment()
            val args = Bundle()
            args.putLong("review_id", reviewId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentReviewId = arguments?.getLong("review_id", -1) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibAddDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reviewData = viewModel.getReviewById(currentReviewId)

        if (reviewData != null) {
            bindData(reviewData)
            setupOwnership(reviewData.isMine)
        } else {
            dismiss()
        }

        initListeners()
    }

    private fun bindData(data: LibReview) {
        binding.libAddDialogNickTv.text = data.userName
        binding.libAddDialogPageTv.text = "p.${data.page}"
        binding.libAddDialogDateTv.text = data.date
        binding.libAddDialogMemoTv.text = data.content

        if (data.reviewImageUri != null) {
            binding.libAddDialogImageIv.visibility = View.VISIBLE
            Glide.with(this).load(data.reviewImageUri).into(binding.libAddDialogImageIv)
        } else {
            binding.libAddDialogImageIv.visibility = View.GONE
        }

        if (data.profileImage != null) {
            binding.libAddDialogProfileIv.setImageResource(data.profileImage)
        }
    }

    private fun setupOwnership(isMine: Boolean) {
        val visibility = if (isMine) View.VISIBLE else View.GONE
        binding.libAddDialogTrashIv.visibility = visibility
        binding.libAddDialogModifyIv.visibility = visibility
    }

    private fun initListeners() {
        // 수정 버튼 클릭
        binding.libAddDialogModifyIv.setOnClickListener {
            dismiss() // 다이얼로그 닫기

            // [오류 수정 3] 수정 버튼을 누르면 '글쓰기 화면(AddCard)'으로 이동해야 함
            // 여기서 LibraryAddCardFragment는 '글쓰기/수정 화면'을 의미한다고 가정합니다.
            val fragment = LibraryAddDialogFragment() // <-- 글쓰기용 프래그먼트 클래스 이름 확인 필요!
            val args = Bundle()
            args.putLong("edit_review_id", currentReviewId)
            fragment.arguments = args

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }

        // 삭제 버튼 클릭
        binding.libAddDialogTrashIv.setOnClickListener {
            viewModel.deleteReview(currentReviewId)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}