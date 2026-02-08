package com.bookiibookii.bookiibookii.myPage.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentMypInformationBinding

class MypInformationFragment : Fragment() {
    private var _binding: FragmentMypInformationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 뒤로가기 버튼
        binding.mypInfoBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 2. 웹뷰 설정 및 로드
        initWebView()
    }

    private fun initWebView() {
        binding.webView.apply {
            // 노션 페이지는 자바스크립트가 켜져 있어야 내용이 보입니다.
            settings.javaScriptEnabled = true

            // 렌더링 성능을 위해 DOM 스토리지 켜기 (노션 호환성 향상)
            settings.domStorageEnabled = true

            // 중요: 이 설정이 없으면 앱이 아니라 크롬 브라우저 새 창이 뜹니다.
            webViewClient = WebViewClient()

            // 여기에 복사해둔 노션 페이지 URL을 넣으세요.
            loadUrl("https://flower-ulna-374.notion.site/2fe09cc7cdf48040a148f837278c7f83?source=copy_link")
        }
    }

    override fun onResume() {
        super.onResume()
        // 바텀 네비게이션 숨기기 (기존 코드 유지)
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}