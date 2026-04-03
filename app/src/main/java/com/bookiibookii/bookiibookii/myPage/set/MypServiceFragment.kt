package com.bookiibookii.bookiibookii.myPage.set

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.databinding.FragmentMypServiceBinding

class MypServiceFragment : BaseDetailFragment<FragmentMypServiceBinding>() {

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMypServiceBinding {
        return FragmentMypServiceBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로가기
        binding.mypServiceBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        // 웹뷰 초기화: assets 내의 HTML 로드
        initWebView()
    }

    private fun initWebView() {
        binding.webView.apply {
            setBackgroundColor(0) // 배경 투명 (CardView 배경색 적용)
            settings.javaScriptEnabled = false // 단순 텍스트라면 보안상 끄는 것이 좋음
            loadUrl("file:///android_asset/service_terms.html")
        }
    }
}