package com.bookiibookii.bookiibookii.myPage.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}