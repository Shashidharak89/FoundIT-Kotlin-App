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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shashidhara.foundit.ui.navigation.Screen
import com.shashidhara.foundit.ui.theme.AquaFoamBase
import com.shashidhara.foundit.ui.theme.AquaFoamLight
import com.shashidhara.foundit.ui.theme.AquaFoamLighter
import com.shashidhara.foundit.ui.theme.AquaFoamMedium
import com.shashidhara.foundit.ui.theme.AquaFoamMuted
import com.shashidhara.foundit.ui.theme.CyberTealBase
import com.shashidhara.foundit.ui.theme.CyberTealDark
import com.shashidhara.foundit.ui.theme.CyberTealDarker
import com.shashidhara.foundit.ui.theme.CyberTealDarkest
import com.shashidhara.foundit.ui.theme.CyberTealElevated
import com.shashidhara.foundit.ui.theme.CyberTealLight
import com.shashidhara.foundit.ui.theme.CyberTealMedium
import com.shashidhara.foundit.ui.theme.CyberTealMuted
import com.shashidhara.foundit.ui.theme.CyberTealSoft

/**
 * Global Sidebar (Navigation Drawer) for FoundIT
 * Strictly styled using Cyber Teal (#6A00FA) and Aqua Foam (#FFD6A5) shades.
 */
@Composable
fun FoundITSidebar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    ModalDrawerSheet(
        modifier = modifier
            .width(310.dp)
            .fillMaxHeight(),
        drawerContainerColor = CyberTealDarker,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CyberTealDarker,
                            CyberTealDarkest
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Scrollable Section: Header + Navigation Items + Radar Banner
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState)
            ) {
                // Header with App Title and 'X' Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo + Branding
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(CyberTealLight, CyberTealBase)
                                    )
                                )
                                .border(1.dp, AquaFoamBase, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FoundITIcons.Radar,
                                contentDescription = "FoundIT Logo",
                                tint = AquaFoamLighter,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Found",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AquaFoamLighter
                                )
                                Text(
                                    text = "IT",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AquaFoamBase
                                )
                            }
                            Text(
                                text = "Lost & Found Network",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = AquaFoamMedium
                            )
                        }
                    }

                    // 'X' Close Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CyberTealMuted)
                            .border(1.dp, CyberTealElevated, CircleShape)
                            .clickable { onCloseClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FoundITIcons.Close,
                            contentDescription = "Close Sidebar",
                            tint = AquaFoamBase,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Divider line with glow gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    CyberTealBase,
                                    AquaFoamBase.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Section Label
                Text(
                    text = "MAIN MENU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTealSoft,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )

                // Navigation Items: Home and My Account
                Screen.sidebarItems.forEach { screen ->
                    val isSelected = currentScreen.route == screen.route

                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) CyberTealDark else Color.Transparent,
                        label = "sidebarItemBg"
                    )

                    val borderColor = if (isSelected) AquaFoamBase.copy(alpha = 0.35f) else Color.Transparent

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(backgroundColor)
                            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                            .clickable {
                                onNavigate(screen)
                            }
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Icon Box
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) CyberTealBase else CyberTealMedium.copy(alpha = 0.4f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) AquaFoamBase else CyberTealElevated,
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    tint = if (isSelected) AquaFoamLighter else AquaFoamLight.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = screen.title,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AquaFoamLighter else AquaFoamLight
                                )
                                Text(
                                    text = screen.subtitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = if (isSelected) AquaFoamMedium else CyberTealSoft
                                )
                            }

                            if (isSelected) {
                                // Active indicator badge in Aqua Foam
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(AquaFoamBase)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Network quick stats card in Cyber Teal + Aqua Foam
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CyberTealDark)
                        .border(1.dp, CyberTealElevated, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = FoundITIcons.Shield,
                                contentDescription = "Active Guard",
                                tint = AquaFoamBase,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "COMMUNITY RADAR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AquaFoamBase,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Over 1,420 items matched this month with 98% return rate.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = AquaFoamLight.copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Bottom Profile / Version Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(CyberTealElevated)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyberTealBase)
                                .border(1.5.dp, AquaFoamBase, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "FI",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AquaFoamLighter
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "FoundIT App",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AquaFoamLight
                            )
                            Text(
                                text = "Cyber Edition v1.0",
                                fontSize = 11.sp,
                                color = CyberTealSoft
                            )
                        }
                    }

                    // Status pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AquaFoamMuted)
                            .border(1.dp, AquaFoamBase.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaFoamLighter
                        )
                    }
                }
            }
        }
    }
}
