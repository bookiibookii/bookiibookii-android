package com.bookiibookii.bookiibookii.myPage.Notice

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.LoadingDialog // ★ 로딩 다이얼로그 import
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentMypNoticeDetailBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MypNoticeDetailFragment : Fragment() {
    private var _binding: FragmentMypNoticeDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog // ★ 로딩 선언
    private var noticeId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            noticeId = it.getInt("noticeId", -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypNoticeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext()) // ★ 로딩 초기화

        binding.mypNoticeBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        if (noticeId != -1) {
            fetchNoticeDetail(noticeId)
        } else {
            Log.e("NoticeDetail", "Invalid Notice ID")
        }
    }

    private fun fetchNoticeDetail(id: Int) {
        lifecycleScope.launch {
            loadingDialog.show() // ★ API 호출 전 로딩 시작
            try {
                val response = RetrofitClient.api().getNoticeDetail(id)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val detail = response.body()!!.result
                    binding.mypNoticeDetailDateTv.text = formatDate(detail.createdAt)
                    binding.mypNoticeContentTv.text = detail.content
                } else {
                    Log.e("NoticeDetail", "상세 조회 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("NoticeDetail", "네트워크 오류", e)
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss() // ★ 무조건 로딩 끝내기
            }
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("yyyy. MM. dd. HH:mm", Locale.getDefault())
            val date = parser.parse(dateString)
            formatter.format(date ?: return dateString)
        } catch (e: Exception) {
            dateString
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