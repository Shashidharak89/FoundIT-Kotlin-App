package com.shashidhara.foundit.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shashidhara.foundit.data.repository.AuthState
import com.shashidhara.foundit.ui.components.FoundITSidebar
import com.shashidhara.foundit.ui.components.FoundITTopBar
import com.shashidhara.foundit.ui.navigation.Screen
import com.shashidhara.foundit.ui.screens.AccountScreen
import com.shashidhara.foundit.ui.screens.CreatePostScreen
import com.shashidhara.foundit.ui.screens.HomeScreen
import com.shashidhara.foundit.ui.theme.ColorPureBlack
import com.shashidhara.foundit.ui.theme.ColorPureWhite
import com.shashidhara.foundit.ui.viewmodel.AuthViewModel
import com.shashidhara.foundit.ui.viewmodel.PostViewModel
import kotlinx.coroutines.launch

/**
 * Root Composable for FoundIT.
 * Hosts the global Navbar, Sidebar (ModalNavigationDrawer), and page content.
 * Sidebar opens via the hamburger button, and closes on clicking 'X' or outside (scrim).
 */
@Composable
fun FoundITApp(
    authViewModel: AuthViewModel = viewModel(),
    postViewModel: PostViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var initialCreatePostType by remember { mutableStateOf("LOST") }

    val authState by authViewModel.authState.collectAsState()
    val currentUserProfile = (authState as? AuthState.Authenticated)?.profile

    // Intercept back button safely: close drawer first, or navigate to Home if on another screen
    val backDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
    if (backDispatcherOwner != null) {
        BackHandler(enabled = drawerState.isOpen || currentScreen != Screen.Home) {
            if (drawerState.isOpen) {
                scope.launch { drawerState.close() }
            } else {
                currentScreen = Screen.Home
            }
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            postViewModel.refreshFeed()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // Closes automatically when user taps anywhere outside the sidebar (scrim tap)
        gesturesEnabled = true,
        scrimColor = ColorPureBlack.copy(alpha = 0.5f),
        drawerContent = {
            FoundITSidebar(
                currentScreen = currentScreen,
                userProfile = currentUserProfile,
                onNavigate = { screen ->
                    currentScreen = screen
                    scope.launch {
                        drawerState.close()
                    }
                },
                onCloseClick = {
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                FoundITTopBar(
                    currentScreen = currentScreen,
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            },
            containerColor = ColorPureWhite
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(ColorPureWhite)
            ) {
                Crossfade(
                    targetState = currentScreen,
                    animationSpec = tween(durationMillis = 250),
                    label = "pageTransition"
                ) { screen ->
                    when (screen) {
                        is Screen.Home -> HomeScreen(
                            postViewModel = postViewModel,
                            onNavigateToCreatePost = { postType ->
                                initialCreatePostType = postType
                                currentScreen = Screen.CreatePost
                            },
                            onNavigateToAccount = {
                                currentScreen = Screen.MyAccount
                            }
                        )
                        is Screen.CreatePost -> CreatePostScreen(
                            postViewModel = postViewModel,
                            currentUser = currentUserProfile,
                            initialPostType = initialCreatePostType,
                            onPostCreated = {
                                currentScreen = Screen.Home
                            }
                        )
                        is Screen.MyAccount -> AccountScreen(authViewModel = authViewModel)
                    }
                }
            }
        }
    }
}
