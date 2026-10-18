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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shashidhara.foundit.data.repository.AuthState
import com.shashidhara.foundit.ui.components.FoundITSidebar
import com.shashidhara.foundit.ui.components.FoundITTopBar
import com.shashidhara.foundit.ui.navigation.Screen
import com.shashidhara.foundit.ui.screens.AccountScreen
import com.shashidhara.foundit.ui.screens.CreatePostScreen
import com.shashidhara.foundit.ui.screens.DashboardScreen
import com.shashidhara.foundit.ui.screens.HomeScreen
import com.shashidhara.foundit.ui.theme.ColorPureBlack
import com.shashidhara.foundit.ui.theme.ColorPureWhite
import com.shashidhara.foundit.ui.viewmodel.AuthViewModel
import com.shashidhara.foundit.ui.viewmodel.PostViewModel
import kotlinx.coroutines.launch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shashidhara.foundit.ui.components.BottomBarTab
import com.shashidhara.foundit.ui.components.FoundITBottomBar
import com.shashidhara.foundit.ui.components.FoundITIcons
import com.shashidhara.foundit.ui.theme.BrandTealBase

/**
 * Root Composable for FoundIT.
 * Hosts the global Navbar, Sidebar (ModalNavigationDrawer sliding from right corner), and page content.
 * Sidebar opens via clicking the profile avatar in navbar, and closes on clicking 'X' or outside (scrim).
 */
@Composable
fun FoundITApp(
    authViewModel: AuthViewModel = viewModel(),
    postViewModel: PostViewModel = viewModel()
) {
    val context = LocalContext.current
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

    // Sidebar slides from the right corner by setting RTL on the drawer wrapper
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            // Closes automatically when user taps anywhere outside the sidebar (scrim tap)
            gesturesEnabled = true,
            scrimColor = ColorPureBlack.copy(alpha = 0.5f),
            drawerContent = {
                // Restore LTR inside sidebar content
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
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
                        },
                        onSignOut = {
                            scope.launch {
                                drawerState.close()
                            }
                            authViewModel.signOut(context)
                            if (currentScreen is Screen.AdminPanel) {
                                currentScreen = Screen.Home
                            }
                        }
                    )
                }
            }
        ) {
            // Restore LTR inside main screen content
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    topBar = {
                        FoundITTopBar(
                            currentScreen = currentScreen,
                            userProfile = currentUserProfile,
                            onMenuClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        )
                    },
                    bottomBar = {
                        FoundITBottomBar(
                            currentScreen = currentScreen,
                            onTabSelected = { tab ->
                                when (tab) {
                                    BottomBarTab.HOME -> currentScreen = Screen.Home
                                    BottomBarTab.MY_POSTS -> currentScreen = Screen.MyPosts
                                    BottomBarTab.REQUESTS -> currentScreen = Screen.MyPosts
                                    BottomBarTab.PROFILE -> currentScreen = Screen.MyAccount
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        if (currentScreen is Screen.Home) {
                            androidx.compose.material3.Surface(
                                shape = RoundedCornerShape(50.dp),
                                shadowElevation = 6.dp,
                                color = BrandTealBase,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .clickable {
                                        if (currentUserProfile == null) {
                                            currentScreen = Screen.MyAccount
                                        } else {
                                            initialCreatePostType = "LOST"
                                            currentScreen = Screen.CreatePost
                                        }
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = FoundITIcons.Add,
                                        contentDescription = "Report Item",
                                        tint = ColorPureWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Report Item",
                                        color = ColorPureWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
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
                        is Screen.Home -> {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            HomeScreen(
                                postViewModel = postViewModel,
                                currentUser = currentUserProfile,
                                onNavigateToCreatePost = { postType ->
                                    if (currentUserProfile == null) {
                                        currentScreen = Screen.MyAccount
                                    } else {
                                        initialCreatePostType = postType
                                        currentScreen = Screen.CreatePost
                                    }
                                },
                                onNavigateToAccount = {
                                    currentScreen = Screen.MyAccount
                                },
                                onNavigateToMyPosts = {
                                    currentScreen = Screen.MyPosts
                                },
                                onSignInClick = {
                                    authViewModel.signInWithGoogle(context)
                                }
                            )
                        }
                        is Screen.CreatePost -> CreatePostScreen(
                            postViewModel = postViewModel,
                            currentUser = currentUserProfile,
                            initialPostType = initialCreatePostType,
                            onPostCreated = {
                                currentScreen = Screen.Home
                            }
                        )
                        is Screen.MyPosts -> DashboardScreen(
                            currentUser = currentUserProfile,
                            onNavigateToCreatePost = {
                                initialCreatePostType = "LOST"
                                currentScreen = Screen.CreatePost
                            },
                            onNavigateToSignIn = {
                                currentScreen = Screen.MyAccount
                            }
                        )
                        is Screen.MyAccount -> AccountScreen(
                            authViewModel = authViewModel,
                            onNavigateToMyPosts = { currentScreen = Screen.MyPosts }
                        )
                        is Screen.AdminPanel -> {
                            if (currentUserProfile?.role.equals("ADMIN", ignoreCase = true)) {
                                com.shashidhara.foundit.ui.screens.AdminPanelScreen(
                                    currentUser = currentUserProfile,
                                    onNavigateBack = { currentScreen = Screen.Home }
                                )
                            } else {
                                LaunchedEffect(Unit) {
                                    currentScreen = Screen.Home
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
}

