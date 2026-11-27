package io.github.itslanmi.withdrawwise.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.itslanmi.withdrawwise.feature.history.HistoryScreen
import io.github.itslanmi.withdrawwise.feature.insights.InsightsScreen
import io.github.itslanmi.withdrawwise.feature.map.MapScreen
import io.github.itslanmi.withdrawwise.feature.profile.ProfileScreen
import io.github.itslanmi.withdrawwise.ui.components.LocalBottomBarPadding
import io.github.itslanmi.withdrawwise.ui.navigation.FloatingNavBar
import io.github.itslanmi.withdrawwise.ui.navigation.FloatingNavBarHeight
import io.github.itslanmi.withdrawwise.ui.navigation.HistoryRoute
import io.github.itslanmi.withdrawwise.ui.navigation.InsightsRoute
import io.github.itslanmi.withdrawwise.ui.navigation.MapRoute
import io.github.itslanmi.withdrawwise.ui.navigation.ProfileRoute
import io.github.itslanmi.withdrawwise.ui.navigation.TopLevelDestination

private val NavBarBottomGap = 12.dp

@Composable
fun WithdrawWiseApp(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val selected =
        TopLevelDestination.entries.firstOrNull { destination ->
            currentDestination?.hierarchy?.any { it.hasRoute(destination.routeClass) } == true
        }

    val systemBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomBarPadding = systemBottom + NavBarBottomGap + FloatingNavBarHeight + 12.dp

    CompositionLocalProvider(LocalBottomBarPadding provides bottomBarPadding) {
        Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = MapRoute,
                    enterTransition = { fadeIn() },
                    exitTransition = { fadeOut() }
                ) {
                    composable<MapRoute> { MapScreen() }
                    composable<HistoryRoute> { HistoryScreen() }
                    composable<InsightsRoute> { InsightsScreen() }
                    composable<ProfileRoute> { ProfileScreen() }
                }

                FloatingNavBar(
                    selected = selected,
                    onSelect = { destination -> navController.navigateToTopLevel(destination) },
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = systemBottom + NavBarBottomGap)
                )
            }
        }
    }
}

fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
