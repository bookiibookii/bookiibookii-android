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
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentMypNoticeBinding
import kotlinx.coroutines.launch

class MypNoticeFragment : Fragment() {

    private var _binding: FragmentMypNoticeBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypNoticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        binding.mypNoticeBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        fetchNoticeList()
    }

    private fun fetchNoticeList() {
        lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.api().getNoticeList()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val noticeList = response.body()?.result

                    // ★ 핵심: 공지사항 데이터가 비어있는지 확인
                    if (noticeList.isNullOrEmpty()) {
                        // 비어있다면: 안내 문구 표시, 리스트 숨김
                        binding.mypNoNoticeCl.visibility = View.VISIBLE
                        binding.rvNoticeList.visibility = View.GONE
                    } else {
                        // 데이터가 있다면: 안내 문구 숨김, 리스트 표시
                        binding.mypNoNoticeCl.visibility = View.GONE
                        binding.rvNoticeList.visibility = View.VISIBLE

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
                    }

                } else {
                    Log.e("Notice", "리스트 조회 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("Notice", "네트워크 오류", e)
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.VISIBLE
        _binding = null
    }
}