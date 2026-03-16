package com.bookiibookii.bookiibookii.common

import android.view.View
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R

// 하단 네비게이션을 숨겨야 하는 상세 화면들은 이 Fragment를 상속받습니다.
abstract class BaseDetailFragment : Fragment() {
    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.VISIBLE
    }
}