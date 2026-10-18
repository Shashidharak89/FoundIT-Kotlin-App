package com.shashidhara.foundit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.shashidhara.foundit.R
import com.shashidhara.foundit.data.model.UserProfile
import com.shashidhara.foundit.ui.navigation.Screen
import com.shashidhara.foundit.ui.theme.AppBorderLight
import com.shashidhara.foundit.ui.theme.AppSurfaceSubtle
import com.shashidhara.foundit.ui.theme.BrandMintBase
import com.shashidhara.foundit.ui.theme.BrandMintBg
import com.shashidhara.foundit.ui.theme.BrandTealBase
import com.shashidhara.foundit.ui.theme.BrandTealLight
import com.shashidhara.foundit.ui.theme.ColorPureWhite
import com.shashidhara.foundit.ui.theme.TextMutedSlate
import com.shashidhara.foundit.ui.theme.TextPrimaryDark
import com.shashidhara.foundit.ui.theme.TextSecondarySlate

/**
 * Global Sidebar (Navigation Drawer) for FoundIT
 * Strictly styled using Cyber Teal (#6A00FA) and Aqua Foam (#FFD6A5) shades.
 */
@Composable
fun FoundITSidebar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    userProfile: UserProfile? = null,
    onSignOut: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    ModalDrawerSheet(
        modifier = modifier
            .width(310.dp)
            .fillMaxHeight(),
        drawerContainerColor = ColorPureWhite,
        drawerShape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(ColorPureWhite)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Scrollable Section: Header + Navigation Items
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
                        Image(
                            painter = painterResource(id = R.drawable.foundit_logo),
                            contentDescription = "FoundIT Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Found",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = "IT",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrandTealBase
                                )
                            }
                            Text(
                                text = "Lost & Found Network",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextSecondarySlate
                            )
                        }
                    }

                    // 'X' Close Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AppSurfaceSubtle)
                            .border(1.dp, AppBorderLight, CircleShape)
                            .clickable { onCloseClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FoundITIcons.Close,
                            contentDescription = "Close Sidebar",
                            tint = TextPrimaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Clean divider line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AppBorderLight)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Section Label
                Text(
                    text = "MAIN MENU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMutedSlate,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )

                // Navigation Items: Title only, no subtitles, ordered with Admin above MyAccount, and Login/Register for guests
                Screen.getSidebarItemsForRole(userProfile?.role).forEach { screen ->
                    val isSelected = currentScreen.route == screen.route
                    val isAccountItem = screen == Screen.MyAccount
                    val itemTitle = if (isAccountItem && userProfile == null) "Login / Register" else screen.title
                    val itemIcon = if (isAccountItem && userProfile == null) FoundITIcons.Account else screen.icon

                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) AppSurfaceSubtle else Color.Transparent,
                        label = "sidebarItemBg"
                    )

                    val borderColor = if (isSelected) AppBorderLight else Color.Transparent

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(backgroundColor)
                            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                            .clickable {
                                onNavigate(screen)
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Icon Box
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) BrandTealBase else AppSurfaceSubtle
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) BrandTealLight else AppBorderLight,
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = itemIcon,
                                    contentDescription = itemTitle,
                                    tint = if (isSelected) BrandMintBase else TextSecondarySlate,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = itemTitle,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) BrandTealBase else TextPrimaryDark,
                                modifier = Modifier.weight(1f)
                            )

                            if (isSelected) {
                                // Active indicator badge in Mint
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(BrandMintBase)
                                )
                            }
                        }
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
                        .background(AppBorderLight)
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (userProfile != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // User Avatar & Name / Email -> Clicking navigates to My Account
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNavigate(Screen.MyAccount) }
                                .padding(vertical = 4.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BrandTealBase)
                                    .border(1.5.dp, BrandMintBase, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!userProfile.photoUrl.isNullOrBlank()) {
                                    SubcomposeAsyncImage(
                                        model = userProfile.photoUrl,
                                        contentDescription = "User Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape),
                                        error = {
                                            Text(
                                                text = userProfile.name.take(2).uppercase().ifBlank { "FI" },
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ColorPureWhite
                                            )
                                        }
                                    )
                                } else {
                                    Text(
                                        text = userProfile.name.take(2).uppercase().ifBlank { "FI" },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorPureWhite
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userProfile.name.ifBlank { "FoundIT User" },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimaryDark,
                                    maxLines = 1
                                )
                                Text(
                                    text = userProfile.email.ifBlank { "Verified Member" },
                                    fontSize = 11.sp,
                                    color = TextMutedSlate,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Sign out icon button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppSurfaceSubtle)
                                .border(1.dp, AppBorderLight, RoundedCornerShape(10.dp))
                                .clickable { onSignOut() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FoundITIcons.SignOut,
                                contentDescription = "Sign Out",
                                tint = TextSecondarySlate,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    // Guest user prompt: clicking navigates to Account
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandMintBg)
                            .border(1.dp, BrandMintBase, RoundedCornerShape(12.dp))
                            .clickable { onNavigate(Screen.MyAccount) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = FoundITIcons.Account,
                                contentDescription = null,
                                tint = BrandTealBase,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sign In / Register",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandTealBase
                            )
                        }
                        Icon(
                            imageVector = FoundITIcons.ChevronRight,
                            contentDescription = null,
                            tint = BrandTealBase,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

