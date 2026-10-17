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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.shashidhara.foundit.ui.components.FoundITSidebar
import com.shashidhara.foundit.ui.components.FoundITTopBar
import com.shashidhara.foundit.ui.navigation.Screen
import com.shashidhara.foundit.ui.screens.AccountScreen
import com.shashidhara.foundit.ui.screens.HomeScreen
import com.shashidhara.foundit.ui.theme.CyberTealDarkest
import kotlinx.coroutines.launch

/**
 * Root Composable for FoundIT.
 * Hosts the global Navbar, Sidebar (ModalNavigationDrawer), and page content.
 * Sidebar opens via the hamburger button, and closes on clicking 'X' or outside (scrim).
 */
@Composable
fun FoundITApp() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // Intercept back button safely only when an OnBackPressedDispatcherOwner is present
    val backDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
    if (backDispatcherOwner != null) {
        BackHandler(enabled = drawerState.isOpen) {
            scope.launch {
                drawerState.close()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // Closes automatically when user taps anywhere outside the sidebar (scrim tap)
        gesturesEnabled = true,
        scrimColor = CyberTealDarkest.copy(alpha = 0.75f),
        drawerContent = {
            FoundITSidebar(
                currentScreen = currentScreen,
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
            containerColor = CyberTealDarkest
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(CyberTealDarkest)
            ) {
                Crossfade(
                    targetState = currentScreen,
                    animationSpec = tween(durationMillis = 250),
                    label = "pageTransition"
                ) { screen ->
                    when (screen) {
                        is Screen.Home -> HomeScreen()
                        is Screen.MyAccount -> AccountScreen()
                    }
                }
            }
        }
    }
}
