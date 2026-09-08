package com.bookiibookii.bookiibookii.mypage.nav

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.bookiibookii.bookiibookii.mypage.ui.main.MypageScreen
import com.bookiibookii.bookiibookii.mypage.ui.other.OtherUserBookshelfScreen
import com.bookiibookii.bookiibookii.mypage.vm.OtherUserBookshelfViewModel
import com.bookiibookii.bookiibookii.mypage.vm.OtherUserProfileViewModel
import com.bookiibookii.bookiibookii.ui.nav.AppNavigator
import com.bookiibookii.bookiibookii.ui.nav.Graph

internal object OtherProfileDestinations {
    const val ARG_NICKNAME = "nickname"
    const val PROFILE = "otherProfile/{$ARG_NICKNAME}"
    const val BOOKSHELF = "otherBookshelf"

    fun profile(nickname: String) = "otherProfile/${Uri.encode(nickname)}"
}

fun NavGraphBuilder.otherProfileGraph(
    navController: NavController,
    navigator: AppNavigator,
) {
    navigation(route = Graph.OTHER_PROFILE, startDestination = OtherProfileDestinations.PROFILE) {
        composable(
            OtherProfileDestinations.PROFILE,
            arguments = listOf(navArgument(OtherProfileDestinations.ARG_NICKNAME) { type = NavType.StringType }),
        ) { entry ->
            val vm = entry.otherProfileViewModel(navController)
            val profile by vm.profile.observeAsState()

            MypageScreen(
                profile = profile,
                isOwner = false,
                onBackClick = navigator::back,
                onBookshelfClick = { navController.navigate(OtherProfileDestinations.BOOKSHELF) },
            )
        }

        composable(OtherProfileDestinations.BOOKSHELF) { entry ->
            val vm = entry.otherProfileViewModel(navController)
            val profile by vm.profile.observeAsState()

            val bookshelfVm: OtherUserBookshelfViewModel = viewModel(
                factory = OtherUserBookshelfViewModel.Factory(vm.nickname),
            )
            val bookshelf by bookshelfVm.bookshelf.observeAsState()
            val isLoading by bookshelfVm.isLoading.observeAsState(true)
            val error by bookshelfVm.error.observeAsState()

            OtherUserBookshelfScreen(
                nickname = profile?.nickname ?: vm.nickname,
                bookshelf = bookshelf,
                isLoading = isLoading,
                error = error,
                onBack = navigator::back,
            )
        }
    }
}

// 그래프 스코프로 공유한다.
@androidx.compose.runtime.Composable
private fun androidx.navigation.NavBackStackEntry.otherProfileViewModel(
    navController: NavController,
): OtherUserProfileViewModel {
    val graphEntry = remember(this) { navController.getBackStackEntry(Graph.OTHER_PROFILE) }
    val nickname = graphEntry.arguments?.getString(OtherProfileDestinations.ARG_NICKNAME).orEmpty()
    return viewModel(graphEntry, factory = OtherUserProfileViewModel.Factory(nickname))
}
