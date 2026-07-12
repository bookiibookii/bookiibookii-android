package com.bookiibookii.bookiibookii.mypage.nav

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bookiibookii.bookiibookii.mypage.ui.main.MypageScreen
import com.bookiibookii.bookiibookii.mypage.ui.other.OtherUserBookshelfScreen
import com.bookiibookii.bookiibookii.mypage.vm.OtherUserProfileViewModel

private object OtherProfileDestinations {
    const val PROFILE = "otherProfile"
    const val BOOKSHELF = "otherBookshelf"
}

@Composable
fun OtherUserProfileNavHost(
    viewModel: OtherUserProfileViewModel,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val profile by viewModel.profile.observeAsState()

    NavHost(
        navController = navController,
        startDestination = OtherProfileDestinations.PROFILE,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(OtherProfileDestinations.PROFILE) {
            MypageScreen(
                profile = profile,
                isOwner = false,
                onBackClick = onExit,
                onBookshelfClick = { navController.navigate(OtherProfileDestinations.BOOKSHELF) },
            )
        }

        composable(OtherProfileDestinations.BOOKSHELF) {
            OtherUserBookshelfScreen(
                nickname = profile?.nickname ?: viewModel.nickname,
                userBooks = profile?.userBooks ?: emptyList(),
                onBack = { navController.popBackStack() },
            )
        }
    }
}
