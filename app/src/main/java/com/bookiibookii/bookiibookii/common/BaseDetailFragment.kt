package com.bookiibookii.bookiibookii.common

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.viewbinding.ViewBinding
import com.bookiibookii.bookiibookii.R

// 하단 네비게이션을 숨겨야 하는 상세 화면들은 이 Fragment를 상속받습니다.
abstract class BaseDetailFragment<B : ViewBinding> : BaseFragment<B>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ★ 기기 하단의 시스템 뒤로 가기 버튼을 눌렀을 때의 동작 강제 지정
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뒤로 갈 화면(백 스택)이 남아있다면 정상적으로 프래그먼트 닫기
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    // 뒤로 갈 화면이 없다면 이 콜백을 끄고 기본 뒤로 가기(앱 종료 등) 실행
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // 공통으로 네비게이션 숨기기
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 공통으로 네비게이션 보이기
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.VISIBLE
    }
}