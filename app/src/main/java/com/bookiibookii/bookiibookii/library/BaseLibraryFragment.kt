package com.bookiibookii.bookiibookii.library

import android.view.View
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R

abstract class BaseLibraryFragment : Fragment() {

    override fun onResume() {
        super.onResume()
        hideBottomNav()
        requireActivity().window.decorView.post {
            if (isAdded && !isDetached) hideBottomNav()
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (activity?.isFinishing == false) {
            activity?.findViewById<View>(R.id.bottomNav)?.visibility = View.VISIBLE
        }
    }

    private fun hideBottomNav() {
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }
}
