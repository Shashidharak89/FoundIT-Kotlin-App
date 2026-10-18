package com.shashidhara.foundit.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.shashidhara.foundit.R
import com.shashidhara.foundit.data.repository.AuthState
import com.shashidhara.foundit.ui.components.FoundITIcons
import com.shashidhara.foundit.ui.theme.AppBackgroundWhite
import com.shashidhara.foundit.ui.theme.AppBorderLight
import com.shashidhara.foundit.ui.theme.AppSurfaceSubtle
import com.shashidhara.foundit.ui.theme.AppSurfaceWhite
import com.shashidhara.foundit.ui.theme.BrandMintBase
import com.shashidhara.foundit.ui.theme.BrandMintBg
import com.shashidhara.foundit.ui.theme.BrandTealBase
import com.shashidhara.foundit.ui.theme.ColorPureWhite
import com.shashidhara.foundit.ui.theme.TextMutedSlate
import com.shashidhara.foundit.ui.theme.TextPrimaryDark
import com.shashidhara.foundit.ui.theme.TextSecondarySlate
import com.shashidhara.foundit.ui.viewmodel.AuthViewModel

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    onNavigateToMyPosts: () -> Unit = {}
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val isSigningIn by authViewModel.isSigningIn.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    val currentProfile = (authState as? AuthState.Authenticated)?.profile

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackgroundWhite),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Profile Header Card (Clean White with Balanced Accents)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ColorPureWhite)
                    .border(1.dp, AppBorderLight, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val initials = currentProfile?.name
                        ?.split(" ")
                        ?.mapNotNull { it.firstOrNull()?.toString() }
                        ?.take(2)
                        ?.joinToString("")
                        ?.uppercase()
                        ?.ifEmpty { "FI" } ?: "G"

                    // Avatar Box
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(AppSurfaceSubtle)
                            .border(2.dp, BrandTealBase, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentProfile?.photoUrl?.isNotBlank() == true) {
                            SubcomposeAsyncImage(
                                model = currentProfile.photoUrl,
                                contentDescription = currentProfile.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                loading = {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = BrandTealBase,
                                        strokeWidth = 2.dp
                                    )
                                },
                                error = {
                                    Text(
                                        text = initials,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandTealBase
                                    )
                                }
                            )
                        } else if (currentProfile == null) {
                            Image(
                                painter = painterResource(id = R.drawable.foundit_logo),
                                contentDescription = "FoundIT Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(48.dp)
                            )
                        } else {
                            Text(
                                text = initials,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandTealBase
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentProfile?.name?.ifBlank { "FoundIT Member" } ?: "Guest Explorer",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        if (currentProfile != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = FoundITIcons.Check,
                                contentDescription = "Verified Member",
                                tint = BrandTealBase,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentProfile?.email?.ifBlank { "Sign in with Google to sync profile" } ?: "Sign in with Google to sync profile",
                        fontSize = 13.sp,
                        color = TextSecondarySlate
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandMintBg)
                            .border(1.dp, BrandMintBase.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (currentProfile != null) "ROLE: ${currentProfile.role.uppercase()}" else "AUTHENTICATION REQUIRED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTealBase,
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
                            value = if (currentProfile != null) "0" else "--",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileMetric(
                            title = "Items Recovered",
                            value = if (currentProfile != null) "0" else "--",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileMetric(
                            title = "Karma Score",
                            value = if (currentProfile != null) "100" else "--",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Quick Navigation to My Posts & Requests Dashboard
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ColorPureWhite)
                    .border(1.dp, AppBorderLight, RoundedCornerShape(16.dp))
                    .clickable { onNavigateToMyPosts() }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BrandMintBg)
                                .border(1.dp, BrandMintBase, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FoundITIcons.Schedule,
                                contentDescription = null,
                                tint = BrandTealBase,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "My Posts & Requests",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Manage your posts & review incoming claims",
                                fontSize = 11.sp,
                                color = TextSecondarySlate
                            )
                        }
                    }

                    Icon(
                        imageVector = FoundITIcons.ChevronRight,
                        contentDescription = null,
                        tint = TextMutedSlate,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }



        if (errorMessage != null) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ColorPureWhite)
                        .border(1.dp, Color(0xFFDC2626), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AUTHENTICATION ERROR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 12.sp,
                                color = TextSecondarySlate,
                                lineHeight = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AppSurfaceSubtle)
                                .clickable { authViewModel.clearError() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FoundITIcons.Close,
                                contentDescription = "Dismiss",
                                tint = TextSecondarySlate,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons: Sign In with Google OR Edit Profile + Sign Out
        item {
            Spacer(modifier = Modifier.height(24.dp))

            if (currentProfile == null) {
                Button(
                    onClick = { authViewModel.signInWithGoogle(context) },
                    enabled = !isSigningIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandTealBase,
                        contentColor = ColorPureWhite
                    )
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = ColorPureWhite,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Connecting with Google...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = FoundITIcons.Google,
                            contentDescription = "Google Sign In",
                            tint = ColorPureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sign in with Google",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Button(
                    onClick = { authViewModel.signOut(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFEF2F2),
                        contentColor = Color(0xFFDC2626)
                    )
                ) {
                    Icon(
                        imageVector = FoundITIcons.SignOut,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out from FoundIT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }
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
            .clip(RoundedCornerShape(12.dp))
            .background(AppSurfaceSubtle)
            .border(1.dp, AppBorderLight, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = BrandTealBase
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondarySlate,
                lineHeight = 11.sp
            )
        }
    }
}

