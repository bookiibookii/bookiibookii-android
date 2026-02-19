package com.bookiibookii.bookiibookii.trkHost

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentHostGroupManageBottomDialogBinding
import com.bookiibookii.bookiibookii.group.GroupDetailActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class HostGroupManageBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentHostGroupManageBottomDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog

    private val groupId: Long by lazy { requireArguments().getLong(ARG_GROUP_ID) }
    private val bookTitle: String by lazy { requireArguments().getString(ARG_BOOK_TITLE).orEmpty() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHostGroupManageBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        binding.tvGroupDetail.setOnClickListener {
            val intent = Intent(requireContext(), GroupDetailActivity::class.java).apply {
                putExtra("GROUP_ID", groupId)
                putExtra("GROUP_TYPE", "RELAY")
            }
            startActivity(intent)
            dismiss()
        }

        binding.tvGroupDelete.setOnClickListener {
            showDeleteConfirmDialog()
        }
    }

    private fun showDeleteConfirmDialog() {
        CommonDialog(
            context = requireContext(),
            title = "그룹 삭제",
            subtitle = bookTitle,
            content = "그룹을 정말 삭제하시겠습니까?\n삭제하면 그룹이 영구적으로 제거됩니다.",
            confirmBtnText = "삭제",
            confirmBtnColor = R.color.ui_point_red,
            onConfirmClick = { deleteGroupByGroupId() }
        ).show()
    }

    private fun deleteGroupByGroupId() {
        if (groupId <= 0) return

        lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().deleteGroup(groupId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.isSuccess) {
                        Toast.makeText(context, "그룹이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.setFragmentResult(RESULT_DELETE_DONE, Bundle())
                        dismiss()
                    } else {
                        Toast.makeText(context, body?.message ?: "삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "HostGroupManageBottomDialogFragment"

        private const val ARG_GROUP_ID = "arg_group_id"
        private const val ARG_BOOK_TITLE = "arg_book_title"

        const val RESULT_DELETE_DONE = "RESULT_DELETE_DONE"

        fun newInstance(groupId: Long, bookTitle: String = "") =
            HostGroupManageBottomDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_GROUP_ID, groupId)
                    putString(ARG_BOOK_TITLE, bookTitle)
                }
            }
    }
}
