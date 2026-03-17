package com.bookiibookii.bookiibookii.myPage.set

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.databinding.FragmentMypInformationBinding

class MypInformationFragment : BaseDetailFragment<FragmentMypInformationBinding>() {

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMypInformationBinding {
        return FragmentMypInformationBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로가기
        binding.mypInfoBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        // 웹뷰 초기화: assets 내의 HTML 로드
        initWebView()
    }

    private fun initWebView() {
        binding.webView.apply {
            setBackgroundColor(0)
            settings.javaScriptEnabled = false
            loadUrl("file:///android_asset/privacy_policy.html")
        }
    }
}