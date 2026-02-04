package com.bookiibookii.bookiibookii.myPage.Notice

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentMypNoticeBinding // 공지 리스트용 XML (이름 확인 필요)
import kotlinx.coroutines.launch

class MypNoticeFragment : Fragment() {

    private var _binding: FragmentMypNoticeBinding? = null // XML 이름이 다르면 수정하세요
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypNoticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mypNoticeBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        fetchNoticeList()
    }

    private fun fetchNoticeList() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(requireContext()).getNoticeList()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val noticeList = response.body()!!.result

                    val adapter = MypNoticeAdapter(noticeList) { noticeId ->
                        // 클릭 시 상세 화면으로 이동 (ID 전달)
                        val fragment = MypNoticeDetailFragment().apply {
                            arguments = Bundle().apply {
                                putInt("noticeId", noticeId)
                            }
                        }

                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit()
                    }

                    binding.rvNoticeList.adapter = adapter // XML 리사이클러뷰 ID 확인
                    binding.rvNoticeList.layoutManager = LinearLayoutManager(context)

                } else {
                    Log.e("Notice", "리스트 조회 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("Notice", "네트워크 오류", e)
            }
        }
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