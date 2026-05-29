package com.bookiibookii.bookiibookii.library.feat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.library.ui.LibraryScreen
import com.bookiibookii.bookiibookii.mypage.MypageFragment
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

class LibraryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                LibraryScreen(
                    onProfileClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, MypageFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onBookmarkClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, LibraryBookmarkFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onBookClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, LibraryDetailFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                )
            }
        }
    }
}
