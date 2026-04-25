package com.xinyu.ai.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navArgument
import com.xinyu.ai.core.theme.XinyuTheme
import com.xinyu.ai.presentation.screen.agegate.AgeGateScreen
import com.xinyu.ai.presentation.screen.chat.ChatScreen
import com.xinyu.ai.presentation.screen.createrole.CreateRoleScreen
import com.xinyu.ai.presentation.screen.login.LoginScreen
import com.xinyu.ai.presentation.screen.membership.MembershipScreen
import com.xinyu.ai.presentation.screen.memory.MemoryScreen
import com.xinyu.ai.presentation.screen.moodcheckin.MoodCheckInScreen
import com.xinyu.ai.presentation.screen.profile.ProfileScreen
import com.xinyu.ai.presentation.screen.register.RegisterScreen
import com.xinyu.ai.presentation.screen.roledetail.RoleDetailScreen
import com.xinyu.ai.presentation.screen.rolelist.RoleListScreen
import com.xinyu.ai.presentation.screen.settings.SettingsScreen
import com.xinyu.ai.presentation.screen.splash.SplashScreen
import com.xinyu.ai.presentation.viewmodel.AppViewModel

@Composable
fun XinyuApp(
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val settings by appViewModel.settings.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val bottomBarRoutes = mainBottomDestinations.map { it.route }

    XinyuTheme(themeMode = settings.themeMode) {
        Scaffold(
            bottomBar = {
                if (currentDestination?.route in bottomBarRoutes) {
                    NavigationBar {
                        mainBottomDestinations.forEach { destination ->
                            NavigationBarItem(
                                selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(AppDestination.RoleList.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                },
                                icon = {
                                    androidx.compose.material3.Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.label,
                                    )
                                },
                                label = { Text(destination.label) },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppDestination.Splash.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(AppDestination.Splash.route) {
                    SplashScreen(
                        onResolved = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(AppDestination.Login.route) {
                    LoginScreen(
                        onLoginSuccess = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onRegisterClick = {
                            navController.navigate(AppDestination.Register.route)
                        },
                    )
                }
                composable(AppDestination.Register.route) {
                    RegisterScreen(
                        onBack = { navController.popBackStack() },
                        onRegistered = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(AppDestination.AgeGate.route) {
                    AgeGateScreen(
                        onComplete = {
                            navController.navigate(AppDestination.RoleList.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(AppDestination.RoleList.route) {
                    RoleListScreen(
                        onOpenRole = { roleId ->
                            navController.navigate(AppDestination.RoleDetail.createRoute(roleId))
                        },
                        onStartChat = { roleId ->
                            navController.navigate(AppDestination.Chat.createRoute(roleId))
                        },
                        onCreateRole = {
                            navController.navigate(AppDestination.CreateRole.route)
                        },
                    )
                }
                composable(
                    route = AppDestination.RoleDetail.route,
                    arguments = listOf(navArgument("roleId") { type = NavType.StringType }),
                ) {
                    RoleDetailScreen(
                        onBack = { navController.popBackStack() },
                        onStartChat = { roleId ->
                            navController.navigate(AppDestination.Chat.createRoute(roleId))
                        },
                    )
                }
                composable(
                    route = AppDestination.Chat.route,
                    arguments = listOf(navArgument("roleId") { type = NavType.StringType }),
                ) {
                    ChatScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(AppDestination.CreateRole.route) {
                    CreateRoleScreen(
                        onBack = { navController.popBackStack() },
                        onCreated = { roleId ->
                            navController.navigate(AppDestination.RoleDetail.createRoute(roleId)) {
                                popUpTo(AppDestination.RoleList.route) {
                                    inclusive = false
                                }
                            }
                        },
                    )
                }
                composable(AppDestination.Memory.route) {
                    MemoryScreen()
                }
                composable(AppDestination.MoodCheckIn.route) {
                    MoodCheckInScreen(onBack = { navController.popBackStack() })
                }
                composable(AppDestination.Profile.route) {
                    ProfileScreen(
                        onMembershipClick = { navController.navigate(AppDestination.Membership.route) },
                        onMoodCheckInClick = { navController.navigate(AppDestination.MoodCheckIn.route) },
                        onSettingsClick = { navController.navigate(AppDestination.Settings.route) },
                    )
                }
                composable(AppDestination.Membership.route) {
                    MembershipScreen(onBack = { navController.popBackStack() })
                }
                composable(AppDestination.Settings.route) {
                    SettingsScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
