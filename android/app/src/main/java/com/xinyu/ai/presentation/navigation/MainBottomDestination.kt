package com.xinyu.ai.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.ui.graphics.vector.ImageVector

data class MainBottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val mainBottomDestinations = listOf(
    MainBottomDestination(
        route = AppDestination.RoleList.route,
        label = "角色",
        icon = Icons.Outlined.AutoAwesome,
    ),
    MainBottomDestination(
        route = AppDestination.Memory.route,
        label = "记忆",
        icon = Icons.Outlined.FavoriteBorder,
    ),
    MainBottomDestination(
        route = AppDestination.Profile.route,
        label = "我的",
        icon = Icons.Outlined.PersonOutline,
    ),
)
