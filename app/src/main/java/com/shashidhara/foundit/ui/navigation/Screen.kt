package com.shashidhara.foundit.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.shashidhara.foundit.ui.components.FoundITIcons

sealed class Screen(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    data object Home : Screen(
        route = "home",
        title = "Home",
        subtitle = "Discover & track items",
        icon = FoundITIcons.Home
    )

    data object CreatePost : Screen(
        route = "create_post",
        title = "Report Item",
        subtitle = "Publish lost or found post",
        icon = FoundITIcons.Add
    )

    data object MyAccount : Screen(
        route = "my_account",
        title = "My Account",
        subtitle = "Profile, settings & claims",
        icon = FoundITIcons.Account
    )

    companion object {
        val sidebarItems: List<Screen>
            get() = listOf(Home, CreatePost, MyAccount)
    }
}
