package com.bookiibookii.bookiibookii.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.notification.nav.NotificationDestinations
import com.bookiibookii.bookiibookii.notification.nav.NotificationNavHost
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class NotificationFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        // Fragment view 생명주기에 맞춰 컴포지션 폐기 (백스택 복귀 시 누수 방지)
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        val startDestination =
            arguments?.getString(ARG_START_DESTINATION) ?: NotificationDestinations.MAIN
        setContent {
            BookiiBookiiTheme {
                NotificationNavHost(
                    startDestination = startDestination,
                    onExit = { parentFragmentManager.popBackStack() },
                    onRedirect = { (activity as? MainActivity)?.dispatchNotificationRedirect(it) },
                )
            }
        }
    }

    companion object {
        private const val ARG_START_DESTINATION = "startDestination"

        fun newInstance(startDestination: String = NotificationDestinations.MAIN) =
            NotificationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_START_DESTINATION, startDestination)
                }
            }
    }
}
