package com.bookiibookii.bookiibookii.tracker.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bookiibookii.bookiibookii.tracker.ui.main.TrackerMainRoute

@Composable
fun TrackerNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = TrackerDestinations.MAIN,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(TrackerDestinations.MAIN) {
            TrackerMainRoute(
                onProfileClick = {},
                onAlertClick = {},
                onCreateGroupClick = {},
                onPrimaryAction = {},
                onSecondaryAction = {},
            )
        }
    }
}
