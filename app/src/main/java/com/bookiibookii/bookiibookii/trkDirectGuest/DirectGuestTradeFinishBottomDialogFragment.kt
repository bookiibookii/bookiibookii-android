package com.bookiibookii.bookiibookii.trkDirectGuest

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentDirectGuestTradeFinishBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class DirectGuestTradeFinishBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDirectGuestTradeFinishBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDirectGuestTradeFinishBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnWriteReview.setOnClickListener {
            fetchUserBookIdAndNavigate()
        }
    }

    private fun fetchUserBookIdAndNavigate() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getLibraryBooks()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val list = response.body()?.result ?: emptyList()

                    val targetBook = list.find { it.groupId.toLong() == groupId }

                    if (targetBook != null) {
                        moveToRelayWrite(targetBook.userBookId)
                    } else {
                        Toast.makeText(
                            context,
                            "해당 그룹의 책 정보를 찾을 수 없습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(context, "데이터 조회 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun moveToRelayWrite(userBookId: Int) {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("NAV_ACTION", "OPEN_RELAY_WRITE")
            putExtra("target_group_id", groupId)
            putExtra("target_user_book_id", userBookId)
        }
        startActivity(intent)

        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DirectGuestTradeFinishFragment"

        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            DirectGuestTradeFinishBottomDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_GROUP_ID, groupId)
                }
            }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}