package com.bookiibookii.bookiibookii.library.feat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.bookiibookii.bookiibookii.library.BaseLibraryFragment
import com.bookiibookii.bookiibookii.library.ui.AddCardMode
import com.bookiibookii.bookiibookii.library.ui.LibraryAddCardScreen
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class LibraryAddCardFragment : BaseLibraryFragment() {

    private val mode: AddCardMode
        get() = arguments?.getString(ARG_MODE)
            ?.let { AddCardMode.valueOf(it) }
            ?: AddCardMode.TEXT

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                LibraryAddCardScreen(
                    mode = mode,
                    onBackClick = { parentFragmentManager.popBackStack() },
                    onSubmit = { parentFragmentManager.popBackStack() },
                )
            }
        }
    }

    companion object {
        private const val ARG_MODE = "arg_mode"

        fun newInstance(mode: AddCardMode) = LibraryAddCardFragment().apply {
            arguments = Bundle().apply { putString(ARG_MODE, mode.name) }
        }
    }
}
