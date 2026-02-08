package com.bookiibookii.bookiibookii.myPage.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentMypServiceBinding

class MypServiceFragment : Fragment() {
    private var _binding: FragmentMypServiceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypServiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 뒤로가기
        binding.mypServiceBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 2. 웹뷰 초기화
        initWebView()
    }

    private fun initWebView() {
        binding.webView.apply {
            // 노션 호환성 설정
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            // 앱 내에서 열리도록 설정
            webViewClient = WebViewClient()

            loadUrl("https://flower-ulna-374.notion.site/2fe09cc7cdf4808abadbdf8c0e471ea1?source=copy_link")
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