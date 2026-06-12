package com.bookiibookii.bookiibookii.mypage

import android.view.View
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.MainActivity
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
        // Fragment가 백스택에서 pop될 때 바텀네비 복원.
        // 무조건 VISIBLE로 덮으면 그룹 에디터처럼 비-top-level 화면으로 복귀 시
        // 시스템 백 콜백 순서 레이스로 바텀네비가 잘못 노출됨 → 복귀 화면 기준으로 재계산.
        if (activity?.isFinishing == false) {
            (activity as? MainActivity)?.refreshBottomNavVisibility()
        }
    }

    private fun hideBottomNav() {
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }
}
