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
import com.bookiibookii.bookiibookii.common.LoadingDialog // ★ 로딩 다이얼로그 import
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentMypNoticeBinding
import kotlinx.coroutines.launch

class MypNoticeFragment : Fragment() {

    private var _binding: FragmentMypNoticeBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog // ★ 로딩 선언

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypNoticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext()) // ★ 로딩 초기화

        binding.mypNoticeBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        fetchNoticeList()
    }

    private fun fetchNoticeList() {
        lifecycleScope.launch {
            loadingDialog.show() // ★ API 호출 전 로딩 시작
            try {
                val response = RetrofitClient.api().getNoticeList()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val noticeList = response.body()!!.result

                    val adapter = MypNoticeAdapter(noticeList) { noticeId ->
                        val fragment = MypNoticeDetailFragment().apply {
                            arguments = Bundle().apply { putInt("noticeId", noticeId) }
                        }
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit()
                    }

                    binding.rvNoticeList.adapter = adapter
                    binding.rvNoticeList.layoutManager = LinearLayoutManager(context)

                } else {
                    Log.e("Notice", "리스트 조회 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("Notice", "네트워크 오류", e)
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss() // ★ 무조건 로딩 끝내기
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