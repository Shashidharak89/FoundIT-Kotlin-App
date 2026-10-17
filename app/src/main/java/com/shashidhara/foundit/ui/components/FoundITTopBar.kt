package com.shashidhara.foundit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shashidhara.foundit.ui.navigation.Screen
import com.shashidhara.foundit.ui.theme.AquaFoamBase
import com.shashidhara.foundit.ui.theme.AquaFoamLighter
import com.shashidhara.foundit.ui.theme.CyberTealBase
import com.shashidhara.foundit.ui.theme.CyberTealDark
import com.shashidhara.foundit.ui.theme.CyberTealDarker
import com.shashidhara.foundit.ui.theme.CyberTealElevated
import com.shashidhara.foundit.ui.theme.CyberTealLight
import com.shashidhara.foundit.ui.theme.CyberTealMuted
import com.shashidhara.foundit.ui.theme.CyberTealSoft

/**
 * Global Top Navbar for FoundIT
 * Styled purely with shades of Cyber Teal (#6A00FA) and Aqua Foam (#FFD6A5).
 */
@Composable
fun FoundITTopBar(
    currentScreen: Screen,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CyberTealDarker,
                        CyberTealDark
                    )
                )
            )

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Hamburger Menu Button
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberTealMuted)
                        .border(1.dp, CyberTealElevated, RoundedCornerShape(12.dp))
                        .clickable { onMenuClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FoundITIcons.Menu,
                        contentDescription = "Open Sidebar Navigation",
                        tint = AquaFoamBase,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Brand Emblem + Title
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(CyberTealLight, CyberTealBase)
                                )
                            )
                            .border(1.dp, AquaFoamBase.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FoundITIcons.Radar,
                            contentDescription = "FoundIT Radar",
                            tint = AquaFoamLighter,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Found",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AquaFoamLighter,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "IT",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = AquaFoamBase,
                                letterSpacing = (-0.5).sp
                            )
                        }
                        Text(
                            text = currentScreen.title.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyberTealSoft,
                            letterSpacing = 1.2.sp
                        )
                    }
                }
            }

            // Right: Status / Notification Badge
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyberTealMuted)
                        .border(1.dp, CyberTealElevated, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FoundITIcons.Notifications,
                        contentDescription = "Notifications",
                        tint = AquaFoamBase,
                        modifier = Modifier.size(20.dp)
                    )
                    // Notification indicator dot in Aqua Foam
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 2.dp)
                            .clip(CircleShape)
                            .background(AquaFoamBase)
                    )
                }
            }
        }

        // Bottom cyber border line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            CyberTealElevated.copy(alpha = 0.2f),
                            CyberTealLight.copy(alpha = 0.7f),
                            AquaFoamBase.copy(alpha = 0.5f),
                            CyberTealElevated.copy(alpha = 0.2f)
                        )
                    )
                )
        )
    }
}
