package com.bookiibookii.bookiibookii.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.group.nav.GroupDestinations
import com.bookiibookii.bookiibookii.group.nav.GroupNavHost
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class GroupFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        // Fragment view 생명주기에 맞춰 컴포지션 폐기 (백스택 복귀 시 누수 방지)
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        val startDestination =
            arguments?.getString(ARG_START_DESTINATION) ?: GroupDestinations.SEARCH
        setContent {
            BookiiBookiiTheme {
                GroupNavHost(
                    startDestination = startDestination,
                    onExit = { parentFragmentManager.popBackStack() },
                )
            }
        }
    }

    companion object {
        private const val ARG_START_DESTINATION = "startDestination"

        fun newInstance(startDestination: String) = GroupFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_START_DESTINATION, startDestination)
            }
        }
    }
}
