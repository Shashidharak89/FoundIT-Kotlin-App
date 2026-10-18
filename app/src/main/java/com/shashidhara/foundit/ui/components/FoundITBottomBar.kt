package com.shashidhara.foundit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shashidhara.foundit.ui.navigation.Screen
import com.shashidhara.foundit.ui.theme.AppBorderLight
import com.shashidhara.foundit.ui.theme.BrandMintBg
import com.shashidhara.foundit.ui.theme.BrandTealBase
import com.shashidhara.foundit.ui.theme.ColorPureWhite
import com.shashidhara.foundit.ui.theme.TextSecondarySlate

enum class BottomBarTab(
    val title: String,
    val icon: ImageVector
) {
    HOME("Home", FoundITIcons.Home),
    MY_POSTS("My Posts", FoundITIcons.Document),
    REQUESTS("Requests", FoundITIcons.Chat),
    PROFILE("Profile", FoundITIcons.Account)
}

/**
 * Full-width Bottom Navigation Bar matching the reference UI design.
 */
@Composable
fun FoundITBottomBar(
    currentScreen: Screen,
    onTabSelected: (BottomBarTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTab = when (currentScreen) {
        is Screen.Home -> BottomBarTab.HOME
        is Screen.MyPosts -> BottomBarTab.MY_POSTS
        is Screen.MyAccount -> BottomBarTab.PROFILE
        else -> BottomBarTab.HOME
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ColorPureWhite)
            .navigationBarsPadding()
    ) {
        // Subtle top divider line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AppBorderLight)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarTab.entries.forEach { tab ->
                val isSelected = activeTab == tab

                val pillBg by animateColorAsState(
                    targetValue = if (isSelected) BrandMintBg else Color.Transparent,
                    label = "bottomBarTabBg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(pillBg)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) BrandTealBase else TextSecondarySlate,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tab.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) BrandTealBase else TextSecondarySlate
                        )
                    }
                }
            }
        }
    }
}
