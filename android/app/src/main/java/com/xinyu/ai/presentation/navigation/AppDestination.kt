package com.xinyu.ai.presentation.navigation

sealed class AppDestination(val route: String) {
    data object Splash : AppDestination("splash")
    data object Login : AppDestination("login")
    data object Register : AppDestination("register")
    data object AgeGate : AppDestination("age_gate")
    data object RoleList : AppDestination("role_list")
    data object RoleDetail : AppDestination("role_detail/{roleId}") {
        fun createRoute(roleId: String): String = "role_detail/$roleId"
    }

    data object Chat : AppDestination("chat/{roleId}") {
        fun createRoute(roleId: String): String = "chat/$roleId"
    }

    data object CreateRole : AppDestination("create_role")
    data object Memory : AppDestination("memory")
    data object MoodCheckIn : AppDestination("mood_checkin")
    data object Profile : AppDestination("profile")
    data object Membership : AppDestination("membership")
    data object Settings : AppDestination("settings")
}
