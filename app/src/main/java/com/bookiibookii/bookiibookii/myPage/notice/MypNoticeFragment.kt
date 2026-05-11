package com.bookiibookii.bookiibookii.myPage.notice

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentMypNoticeBinding
import kotlinx.coroutines.launch

// 1. 제네릭 타입 명시
class MypNoticeFragment : BaseDetailFragment<FragmentMypNoticeBinding>() {

    private lateinit var loadingDialog: LoadingDialog

    // 2. BaseFragment에서 요구하는 바인딩 인플레이트 함수 구현
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMypNoticeBinding {
        return FragmentMypNoticeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        // 이제부터 부모 클래스가 제공하는 'binding' 프로퍼티를 바로 사용합니다.
        binding.mypNoticeBackIv.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        fetchNoticeList()
    }

    private fun fetchNoticeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            loadingDialog.show()
            try {
                val response = RetrofitClient.mypApi().getNoticeList()
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
                        val prefs = requireContext().getSharedPreferences("NoticePrefs", Context.MODE_PRIVATE)

                        val adapter = MypNoticeAdapter(noticeList, prefs) { noticeId ->
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

    // onResume()과 onDestroyView()에서 불필요한 super 호출 및 _binding = null 처리는
    // BaseDetailFragment 및 BaseFragment에서 처리하므로 완전히 삭제했습니다.
}