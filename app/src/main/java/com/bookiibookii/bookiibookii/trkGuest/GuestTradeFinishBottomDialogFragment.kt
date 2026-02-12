package com.bookiibookii.bookiibookii.trkGuest

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
import com.bookiibookii.bookiibookii.databinding.FragmentGuestTradeFinishBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class GuestTradeFinishBottomDialogFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentGuestTradeFinishBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuestTradeFinishBottomDialogBinding.inflate(inflater, container, false)
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
                // 1. 내 라이브러리 목록 조회 API 호출
                val response = RetrofitClient.api().getLibraryBooks()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val list = response.body()?.result ?: emptyList()

                    // 2. 현재 groupId와 일치하는 책 찾기 (Long 타입 변환 주의!)
                    val targetBook = list.find { it.groupId.toLong() == groupId }

                    if (targetBook != null) {
                        // 3. 찾았으면 MainActivity로 이동
                        moveToMainActivity(targetBook.userBookId)
                    } else {
                        Toast.makeText(context, "해당 그룹의 책 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "데이터 조회 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun moveToMainActivity(userBookId: Int) {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            // 기존 MainActivity를 재활용하고 그 위에 쌓인 Activity(GuestActivity 등)를 제거
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            // MainActivity에서 받을 데이터
            putExtra("NAV_ACTION", "OPEN_RELAY_WRITE")
            putExtra("target_group_id", groupId)       // Long
            putExtra("target_user_book_id", userBookId) // Int
        }
        startActivity(intent)

        // 현재 다이얼로그 및 GuestActivity 종료
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "GuestTradeFinishFragment"

        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) = GuestTradeFinishBottomDialogFragment().apply {
            arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
        }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}