package com.bookiibookii.bookiibookii.library.feat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.library.BaseLibraryFragment
import com.bookiibookii.bookiibookii.library.ui.GroupReviewScreen
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class GroupReviewFragment : BaseLibraryFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                GroupReviewScreen(
                    onBackClick = { parentFragmentManager.popBackStack() },
                    onEditClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, ReviewEditFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                )
            }
        }
    }
}
