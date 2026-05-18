package com.bookiibookii.bookiibookii.group.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bookiibookii.bookiibookii.group.ui.search.GroupSearchScreen

@Composable
fun GroupNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = GroupDestinations.SEARCH,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(GroupDestinations.SEARCH) {
            GroupSearchScreen()
        }
    }
}
