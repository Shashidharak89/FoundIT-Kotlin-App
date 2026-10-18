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

    data object MyPosts : Screen(
        route = "my_posts",
        title = "My Posts & Requests",
        subtitle = "Manage your posts & claims",
        icon = FoundITIcons.Schedule
    )

    data object MyAccount : Screen(
        route = "my_account",
        title = "My Account",
        subtitle = "Profile & preferences",
        icon = FoundITIcons.Account
    )

    data object AdminPanel : Screen(
        route = "admin_panel",
        title = "Admin Panel",
        subtitle = "Security & case management",
        icon = FoundITIcons.Shield
    )

    companion object {
        val sidebarItems: List<Screen>
            get() = listOf(Home, CreatePost, MyPosts, MyAccount)

        fun getSidebarItemsForRole(role: String?): List<Screen> {
            return if (role.equals("ADMIN", ignoreCase = true)) {
                listOf(Home, CreatePost, MyPosts, AdminPanel, MyAccount)
            } else {
                listOf(Home, CreatePost, MyPosts, MyAccount)
            }
        }
    }
}

