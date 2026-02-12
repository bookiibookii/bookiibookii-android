package com.bookiibookii.bookiibookii.trkHost

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
import com.bookiibookii.bookiibookii.databinding.FragmentHostTradeFinishBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import kotlin.jvm.java

class HostTradeFinishBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentHostTradeFinishBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHostTradeFinishBottomDialogBinding.inflate(inflater, container, false)
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
                // 1. API 호출
                val response = RetrofitClient.api().getLibraryBooks()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val list = response.body()?.result ?: emptyList()

                    // 2. 현재 가지고 있는 groupId와 일치하는 항목 찾기
                    val targetBook = list.find { it.groupId.toLong() == groupId }

                    if (targetBook != null) {
                        moveToRelayWrite(targetBook.userBookId)
                    } else {
                        Toast.makeText(context, "해당 그룹의 책 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
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
        // 3. MainActivity로 이동 (CLEAR_TOP을 사용하여 기존 MainActivity를 재사용)
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("NAV_ACTION", "OPEN_RELAY_WRITE") // 액션 구분용 키
            putExtra("target_group_id", groupId)       // Long
            putExtra("target_user_book_id", userBookId) // Int
        }
        startActivity(intent)

        // 현재 떠 있는 HostActivity 종료
        requireActivity().finish()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "TradeFinishFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) = HostTradeFinishBottomDialogFragment().apply {
            arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
        }
    }

    override fun getTheme(): Int = R.style.Theme_Bookii_BottomSheet_NoDim
}
