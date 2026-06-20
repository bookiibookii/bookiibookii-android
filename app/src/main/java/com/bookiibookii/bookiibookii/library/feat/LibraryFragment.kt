package com.bookiibookii.bookiibookii.library.feat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.library.nav.LibraryDestinations
import com.bookiibookii.bookiibookii.library.nav.LibraryNavHost
import com.bookiibookii.bookiibookii.mypage.MypageFragment
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 서재 모듈의 단일 진입점 — 트래커(tracker) 모듈과 동일한 패턴.
// 내부 화면 전환은 모두 LibraryNavHost(Compose Navigation)가 담당하고,
// 이 Fragment는 모듈 밖으로 나가는 네비게이션(마이페이지 등)과 시작 라우트 지정만 담당한다.
class LibraryFragment : Fragment() {

    private val startDestination: String
        get() = arguments?.getString(ARG_START_DESTINATION) ?: LibraryDestinations.MAIN

    // MainActivity.refreshBottomNavVisibility()가 "이 Fragment가 탑레벨(목록) 화면인지"를
    // 판단할 때 사용. 단일 Fragment 구조라 LibraryFragment 자체는 항상 같은 클래스이므로,
    // 내부적으로 어떤 라우트로 시작했는지(MAIN vs 딥링크인 상세 등)로 구분해야 한다.
    fun isAtMainRoute(): Boolean = startDestination == LibraryDestinations.MAIN

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                LibraryNavHost(
                    onExitLibrary = { parentFragmentManager.popBackStack() },
                    onProfileClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, MypageFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    startDestination = startDestination,
                )
            }
        }
    }

    companion object {
        private const val ARG_START_DESTINATION = "arg_start_destination"

        // 트래커 모듈 등 외부에서 "독서카드 작성" 흐름으로 서재 상세에 바로 진입할 때 사용
        // (구 LibraryDetailFragment.newInstance를 직접 호출하던 자리를 대체)
        fun newInstanceAtDetail(
            groupId: Int,
            memberBookId: Int,
            groupName: String = "",
            bookTitle: String = "",
            author: String = "",
            genre: String = "",
            coverUrl: String = "",
            startDate: String = "",
            endDate: String = "",
            completedAt: String = "",
            rating: Double = 0.0,
            isDone: Boolean = false,
            progressRate: Int = 0,
            totalPages: Int = 0,
        ) = LibraryFragment().apply {
            arguments = Bundle().apply {
                putString(
                    ARG_START_DESTINATION,
                    LibraryDestinations.detail(
                        groupId = groupId,
                        memberBookId = memberBookId,
                        groupName = groupName,
                        bookTitle = bookTitle,
                        author = author,
                        genre = genre,
                        coverUrl = coverUrl,
                        startDate = startDate,
                        endDate = endDate,
                        completedAt = completedAt,
                        rating = rating,
                        isDone = isDone,
                        progressRate = progressRate,
                        totalPages = totalPages,
                    ),
                )
            }
        }
    }
}
