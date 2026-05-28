package com.bookiibookii.bookiibookii.mypage

import android.view.View
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R

abstract class BaseMypageFragment : Fragment() {

    override fun onResume() {
        super.onResume()
        // 즉시 숨기기 (대부분의 경우 커버)
        hideBottomNav()
        // Activity의 탭 전환 동기 코드가 nav를 VISIBLE로 override할 수 있으므로
        // 현재 메시지 큐가 처리된 다음 프레임에도 한 번 더 적용
        requireActivity().window.decorView.post {
            if (isAdded && !isDetached) hideBottomNav()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // nav 복원은 onDetach에서만 처리 (백스택 보존 시엔 onDetach가 호출되지 않음)
    }

    override fun onDetach() {
        super.onDetach()
        // Fragment가 백스택에서 pop되거나 완전히 제거될 때 nav 복원
        // 백스택 이동(replace+addToBackStack) 시엔 onDetach가 호출되지 않으므로 안전
        if (activity?.isFinishing == false) {
            activity?.findViewById<View>(R.id.bottomNav)?.visibility = View.VISIBLE
        }
    }

    private fun hideBottomNav() {
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }
}
