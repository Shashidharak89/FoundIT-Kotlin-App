package com.shashidhara.foundit.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.shashidhara.foundit.data.model.UserProfile
import com.shashidhara.foundit.R
import com.shashidhara.foundit.ui.navigation.Screen
import com.shashidhara.foundit.ui.theme.AppBorderLight
import com.shashidhara.foundit.ui.theme.AppSurfaceSubtle
import com.shashidhara.foundit.ui.theme.BrandMintBase
import com.shashidhara.foundit.ui.theme.BrandTealBase
import com.shashidhara.foundit.ui.theme.ColorPureWhite
import com.shashidhara.foundit.ui.theme.TextPrimaryDark
import com.shashidhara.foundit.ui.theme.TextSecondarySlate

/**
 * Global Top Navbar for FoundIT matching the modern reference layout:
 * - Left: Logo + "FoundIT" with subtitle "Lost Today. Found Tomorrow."
 * - Right: Notification Bell button + User Profile Avatar (clicks open sidebar)
 */
@Composable
fun FoundITTopBar(
    currentScreen: Screen,
    userProfile: UserProfile? = null,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ColorPureWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Brand Emblem + Title + Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.foundit_logo),
                    contentDescription = "FoundIT Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Found",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimaryDark,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "IT",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandTealBase,
                            letterSpacing = (-0.5).sp
                        )
                    }
                    Text(
                        text = "Lost Today. Found Tomorrow.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondarySlate
                    )
                }
            }

            // Right: Notification Bell + Profile Avatar (Opens sidebar)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Notification Bell Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AppSurfaceSubtle)
                        .border(1.dp, AppBorderLight, CircleShape)
                        .clickable { onNotificationClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FoundITIcons.Notifications,
                        contentDescription = "Notifications",
                        tint = TextPrimaryDark,
                        modifier = Modifier.size(20.dp)
                    )

                    // Unread indicator dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 2.dp)
                            .clip(CircleShape)
                            .background(androidx.compose.ui.graphics.Color(0xFFEF4444))
                    )
                }

                // Profile Avatar Button (Opens sidebar when clicked)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BrandTealBase)
                        .border(1.5.dp, BrandMintBase, CircleShape)
                        .clickable { onMenuClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (!userProfile?.photoUrl.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = userProfile?.photoUrl,
                            contentDescription = "User Profile Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            error = {
                                Text(
                                    text = userProfile?.name?.take(2)?.uppercase()?.ifBlank { "FI" } ?: "FI",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorPureWhite
                                )
                            }
                        )
                    } else {
                        Text(
                            text = userProfile?.name?.take(2)?.uppercase()?.ifBlank { "FI" } ?: "FI",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPureWhite
                        )
                    }

                    // Active online green indicator dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(ColorPureWhite)
                            .padding(1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(androidx.compose.ui.graphics.Color(0xFF22C55E))
                        )
                    }
                }
            }
        }

        // Bottom clean divider line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AppBorderLight)
        )
    }
}
