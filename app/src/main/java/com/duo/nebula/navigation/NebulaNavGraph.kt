package com.duo.nebula.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.duo.nebula.ui.components.NebulaBottomBar
import com.duo.nebula.ui.screens.createpost.CreatePublicationScreen
import com.duo.nebula.ui.screens.feed.FeedScreen
import com.duo.nebula.ui.screens.login.LoginScreen
import com.duo.nebula.ui.screens.profile.ProfileScreen
import com.duo.nebula.ui.screens.register.RegisterScreen
import com.duo.nebula.ui.screens.splash.SplashScreen

private val tabbedRoutes = setOf(
    NebulaDestinations.FEED,
    NebulaDestinations.CREATE_PUBLICATION,
    NebulaDestinations.PROFILE
)

@Composable
fun NebulaNavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in tabbedRoutes) {
                NebulaBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NebulaDestinations.SPLASH,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(NebulaDestinations.SPLASH) {
                SplashScreen(
                    onNavigateToFeed = {
                        navController.navigate(NebulaDestinations.FEED) {
                            popUpTo(NebulaDestinations.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(NebulaDestinations.LOGIN) {
                            popUpTo(NebulaDestinations.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(NebulaDestinations.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(NebulaDestinations.FEED) {
                            popUpTo(NebulaDestinations.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(NebulaDestinations.REGISTER) }
                )
            }

            composable(NebulaDestinations.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(NebulaDestinations.FEED) {
                            popUpTo(NebulaDestinations.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(NebulaDestinations.FEED) {
                FeedScreen(
                    onAuthorClick = { uid -> navController.navigate(NebulaDestinations.authorProfile(uid)) }
                )
            }

            composable(NebulaDestinations.CREATE_PUBLICATION) {
                CreatePublicationScreen(
                    onPublicationCreated = {
                        navController.navigate(NebulaDestinations.FEED) {
                            popUpTo(NebulaDestinations.FEED) { inclusive = true }
                        }
                    }
                )
            }

            composable(NebulaDestinations.PROFILE) {
                ProfileScreen(
                    onLoggedOut = {
                        navController.navigate(NebulaDestinations.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = NebulaDestinations.AUTHOR_PROFILE,
                arguments = listOf(navArgument("uid") { type = NavType.StringType })
            ) {
                ProfileScreen(
                    onLoggedOut = {
                        navController.navigate(NebulaDestinations.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
