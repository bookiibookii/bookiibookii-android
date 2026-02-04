package com.bookiibookii.bookiibookii.myPage.Notice

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentMypNoticeDetailBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MypNoticeDetailFragment : Fragment() {
    private var _binding: FragmentMypNoticeDetailBinding? = null
    private val binding get() = _binding!!

    private var noticeId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bundle에서 noticeId 꺼내기
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

        binding.mypNoticeBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        if (noticeId != -1) {
            fetchNoticeDetail(noticeId)
        } else {
            Log.e("NoticeDetail", "Invalid Notice ID")
        }
    }

    private fun fetchNoticeDetail(id: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getNoticeDetail(id)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val detail = response.body()!!.result

                    // UI 업데이트
                    // 상단 타이틀 (12월 업데이트 안내 -> API 제목)
                    // binding.topBarTitleTv.text = detail.title // (만약 상단바 제목도 바꾼다면)

                    // 제목은 XML에 ID가 없어서 상단바 텍스트뷰 ID를 확인해야 합니다.
                    // 예시: top_bar 안에 있는 TextView ID가 myp_notice_title_tv 라면:
                    // binding.mypNoticeTitleTv.text = detail.title

                    // 날짜
                    binding.mypNoticeDetailDateTv.text = formatDate(detail.createdAt)

                    // 내용
                    binding.mypNoticeContentTv.text = detail.content

                } else {
                    Log.e("NoticeDetail", "상세 조회 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("NoticeDetail", "네트워크 오류", e)
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