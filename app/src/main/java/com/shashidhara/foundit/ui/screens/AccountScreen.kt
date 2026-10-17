package com.shashidhara.foundit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shashidhara.foundit.ui.components.FoundITIcons
import com.shashidhara.foundit.ui.theme.AquaFoamBase
import com.shashidhara.foundit.ui.theme.AquaFoamDark
import com.shashidhara.foundit.ui.theme.AquaFoamLight
import com.shashidhara.foundit.ui.theme.AquaFoamLighter
import com.shashidhara.foundit.ui.theme.AquaFoamMedium
import com.shashidhara.foundit.ui.theme.CyberTealBase
import com.shashidhara.foundit.ui.theme.CyberTealDark
import com.shashidhara.foundit.ui.theme.CyberTealDarker
import com.shashidhara.foundit.ui.theme.CyberTealDarkest
import com.shashidhara.foundit.ui.theme.CyberTealElevated
import com.shashidhara.foundit.ui.theme.CyberTealLight
import com.shashidhara.foundit.ui.theme.CyberTealMuted
import com.shashidhara.foundit.ui.theme.CyberTealSoft

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier
) {
    var matchAlertsEnabled by remember { mutableStateOf(true) }
    var locationSharingEnabled by remember { mutableStateOf(true) }
    var twoFactorEnabled by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberTealDarkest),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Profile Header Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                CyberTealDark,
                                CyberTealDarker
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                CyberTealLight,
                                AquaFoamBase.copy(alpha = 0.5f)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar Box
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(CyberTealLight, CyberTealBase)
                                )
                            )
                            .border(2.5.dp, AquaFoamBase, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AM",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = AquaFoamLighter
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Alex Mercer",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaFoamLighter
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = FoundITIcons.Check,
                            contentDescription = "Verified Member",
                            tint = AquaFoamBase,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "alex.mercer@foundit.app",
                        fontSize = 13.sp,
                        color = AquaFoamMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberTealMuted)
                            .border(1.dp, AquaFoamBase.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "COMMUNITY RECOVERY HERO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaFoamBase,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Karma & Recovery Statistics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileMetric(
                            title = "Items Returned",
                            value = "3",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileMetric(
                            title = "Items Recovered",
                            value = "1",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileMetric(
                            title = "Karma Score",
                            value = "99.4",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section: Personal Information
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "PERSONAL INFORMATION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTealSoft,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            AccountCardGroup {
                AccountInfoRow(
                    icon = FoundITIcons.Email,
                    label = "Email Address",
                    value = "alex.mercer@foundit.app"
                )
                AccountInfoRow(
                    icon = FoundITIcons.Phone,
                    label = "Emergency Phone",
                    value = "+1 (555) 438-2910"
                )
                AccountInfoRow(
                    icon = FoundITIcons.Location,
                    label = "Primary City",
                    value = "San Francisco, CA",
                    showDivider = false
                )
            }
        }

        // Section: Preferences & Security
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "PREFERENCES & SECURITY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTealSoft,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            AccountCardGroup {
                AccountToggleRow(
                    icon = FoundITIcons.Notifications,
                    title = "Instant Match Alerts",
                    description = "Notify when someone reports an item matching yours",
                    checked = matchAlertsEnabled,
                    onCheckedChange = { matchAlertsEnabled = it }
                )
                AccountToggleRow(
                    icon = FoundITIcons.Shield,
                    title = "Radius Privacy Shield",
                    description = "Fuzz exact return spot until contact is verified",
                    checked = locationSharingEnabled,
                    onCheckedChange = { locationSharingEnabled = it }
                )
                AccountToggleRow(
                    icon = FoundITIcons.Lock,
                    title = "Two-Factor Auth (2FA)",
                    description = "Require authenticator confirmation on login",
                    checked = twoFactorEnabled,
                    onCheckedChange = { twoFactorEnabled = it },
                    showDivider = false
                )
            }
        }

        // Edit Profile & Action Buttons
        item {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberTealBase,
                    contentColor = AquaFoamLighter
                )
            ) {
                Icon(
                    imageVector = FoundITIcons.Edit,
                    contentDescription = null,
                    tint = AquaFoamLighter,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Account Profile",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.dp, CyberTealElevated, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberTealDark,
                    contentColor = AquaFoamDark
                )
            ) {
                Text(
                    text = "Sign Out from FoundIT",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ProfileMetric(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CyberTealDark)
            .border(1.dp, CyberTealElevated, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = AquaFoamBase
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = AquaFoamMedium,
                lineHeight = 11.sp
            )
        }
    }
}

@Composable
private fun AccountCardGroup(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CyberTealDarker)
            .border(1.dp, CyberTealElevated, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun AccountInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberTealDark)
                    .border(1.dp, CyberTealElevated, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = AquaFoamBase,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = CyberTealSoft
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AquaFoamLighter
                )
            }

            Icon(
                imageVector = FoundITIcons.ChevronRight,
                contentDescription = null,
                tint = CyberTealElevated,
                modifier = Modifier.size(18.dp)
            )
        }

        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CyberTealDark)
            )
        }
    }
}

@Composable
private fun AccountToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberTealDark)
                    .border(1.dp, CyberTealElevated, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = AquaFoamBase,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AquaFoamLighter
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = AquaFoamDark,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AquaFoamLighter,
                    checkedTrackColor = CyberTealBase,
                    uncheckedThumbColor = CyberTealSoft,
                    uncheckedTrackColor = CyberTealDark,
                    checkedBorderColor = AquaFoamBase,
                    uncheckedBorderColor = CyberTealElevated
                )
            )
        }

        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CyberTealDark)
            )
        }
    }
}
