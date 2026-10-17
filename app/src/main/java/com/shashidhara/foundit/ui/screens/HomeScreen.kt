package com.shashidhara.foundit.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.shashidhara.foundit.R
import com.shashidhara.foundit.data.model.PostItem
import com.shashidhara.foundit.data.model.PostRequest
import com.shashidhara.foundit.data.model.UserProfile
import com.shashidhara.foundit.ui.components.FoundITIcons
import com.shashidhara.foundit.ui.components.FoundITVideoPlayer
import com.shashidhara.foundit.ui.theme.AppBackgroundWhite
import com.shashidhara.foundit.ui.theme.AppBorderLight
import com.shashidhara.foundit.ui.theme.AppSurfaceSubtle
import com.shashidhara.foundit.ui.theme.BrandMintBase
import com.shashidhara.foundit.ui.theme.BrandMintBg
import com.shashidhara.foundit.ui.theme.BrandTealBase
import com.shashidhara.foundit.ui.theme.ColorPureWhite
import com.shashidhara.foundit.ui.theme.TextMutedSlate
import com.shashidhara.foundit.ui.theme.TextPrimaryDark
import com.shashidhara.foundit.ui.theme.TextSecondarySlate
import com.shashidhara.foundit.ui.viewmodel.PostViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    postViewModel: PostViewModel = viewModel(),
    currentUser: UserProfile? = null,
    onNavigateToCreatePost: (String) -> Unit = {},
    onNavigateToAccount: () -> Unit = {}
) {
    val context = LocalContext.current
    val feedState by postViewModel.feedUiState.collectAsState()
    val currentRequest by postViewModel.currentPostRequest.collectAsState()
    val isSubmittingRequest by postViewModel.isSubmittingRequest.collectAsState()
    val requestFeedback by postViewModel.requestFeedback.collectAsState()

    var selectedPostForDetails by remember { mutableStateOf<PostItem?>(null) }
    val listState = rememberLazyListState()

    val categories = remember {
        listOf("All Items", "Electronics", "Wallets & Cards", "Keys", "Bags", "Documents", "Clothing", "Other")
    }

    // Trigger pagination when scrolling near the bottom
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 3 && lastVisibleItem >= totalItems - 2
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && feedState.hasMore && !feedState.isLoading && !feedState.isLoadingMore) {
            postViewModel.loadNextPage()
        }
    }

    LaunchedEffect(selectedPostForDetails) {
        val post = selectedPostForDetails
        if (post != null && currentUser != null) {
            postViewModel.checkUserRequestForPost(post.id, currentUser.uid)
        }
    }

    LaunchedEffect(requestFeedback) {
        requestFeedback?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackgroundWhite)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Hero Landing Section — The single branded anchor card using #06363A with crisp white typography
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(BrandTealBase)
                        .padding(20.dp)
                ) {
                    Column {
                        // Landing Header: Status Badge + In-App Logo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(ColorPureWhite.copy(alpha = 0.12f))
                                    .border(1.dp, BrandMintBase.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(BrandMintBase)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "COMMUNITY RECOVERY ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorPureWhite,
                                    letterSpacing = 1.sp
                                )
                            }

                            // Landing Page Official Logo
                            Image(
                                painter = painterResource(id = R.drawable.foundit_logo),
                                contentDescription = "FoundIT Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Find What You Lost.\nReturn What You Found.",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ColorPureWhite,
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "A secure community lost-and-found network. Real-time items posted directly by people around you.",
                            fontSize = 13.sp,
                            color = ColorPureWhite.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Action Buttons: I Lost Something / I Found Something
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onNavigateToCreatePost("LOST") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandMintBase,
                                    contentColor = BrandTealBase
                                )
                            ) {
                                Text(
                                    text = "I Lost Something",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Button(
                                onClick = { onNavigateToCreatePost("FOUND") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorPureWhite.copy(alpha = 0.15f),
                                    contentColor = ColorPureWhite
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ColorPureWhite.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "I Found Something",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar with Clear Button (Clean White Surface)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = feedState.searchQuery,
                        onValueChange = { postViewModel.onSearchQueryChanged(it) },
                        placeholder = {
                            Text(
                                text = "Search lost or found items, locations...",
                                color = TextMutedSlate,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = FoundITIcons.Search,
                                contentDescription = "Search",
                                tint = BrandTealBase,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (feedState.searchQuery.isNotBlank()) {
                                Icon(
                                    imageVector = FoundITIcons.Close,
                                    contentDescription = "Clear Search",
                                    tint = TextSecondarySlate,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { postViewModel.onSearchQueryChanged("") }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ColorPureWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandTealBase,
                            unfocusedBorderColor = AppBorderLight,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            cursorColor = BrandTealBase
                        ),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }
            }

            // Post Type Filter Chips: ALL / LOST / FOUND
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL" to "All Items", "LOST" to "Lost Items", "FOUND" to "Found Items").forEach { (typeKey, label) ->
                        val isSelected = feedState.selectedType == typeKey
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) BrandTealBase else ColorPureWhite)
                                .border(1.dp, if (isSelected) BrandTealBase else AppBorderLight, RoundedCornerShape(14.dp))
                                .clickable { postViewModel.onTypeFilterChanged(typeKey) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) ColorPureWhite else TextSecondarySlate
                            )
                        }
                    }
                }
            }

            // Horizontal Category Filter Pills (Minimalist Black & White + Accent Selection)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = feedState.selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) AppSurfaceSubtle else ColorPureWhite)
                                .border(
                                    1.dp,
                                    if (isSelected) BrandTealBase else AppBorderLight,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { postViewModel.onCategorySelected(category) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) BrandTealBase else TextSecondarySlate
                            )
                        }
                    }
                }
            }

            // Section Header: Discoveries & Pagination Stats
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COMMUNITY DISCOVERIES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondarySlate,
                        letterSpacing = 1.2.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (feedState.posts.isNotEmpty()) "${feedState.posts.size} Items (Page ${feedState.page})" else "0 Items",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandTealBase
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = FoundITIcons.Refresh,
                            contentDescription = "Refresh Feed",
                            tint = BrandTealBase,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { postViewModel.refreshFeed() }
                        )
                    }
                }
            }

            // Initial Loading Indicator
            if (feedState.isLoading && feedState.posts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = BrandTealBase,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Loading community posts...",
                                color = TextSecondarySlate,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Render Only Real User-Posted Items
            if (feedState.posts.isNotEmpty()) {
                items(feedState.posts, key = { it.id }) { post ->
                    PostItemCard(
                        post = post,
                        onClick = { selectedPostForDetails = post }
                    )
                }

                // Pagination Footer
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (feedState.isLoadingMore) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = BrandTealBase,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Loading page ${feedState.page + 1}...",
                                    fontSize = 13.sp,
                                    color = TextSecondarySlate
                                )
                            }
                        } else if (feedState.hasMore) {
                            Button(
                                onClick = { postViewModel.loadNextPage() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppSurfaceSubtle,
                                    contentColor = BrandTealBase
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, AppBorderLight, RoundedCornerShape(12.dp))
                            ) {
                                Text(
                                    text = "Load More Posts (Page ${feedState.page + 1})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                text = "All ${feedState.posts.size} community posts loaded",
                                fontSize = 12.sp,
                                color = TextMutedSlate,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else if (!feedState.isLoading) {
                // Empty State
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(ColorPureWhite)
                            .border(1.dp, AppBorderLight, RoundedCornerShape(18.dp))
                            .padding(26.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(BrandMintBg)
                                    .border(1.dp, BrandMintBase, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (feedState.searchQuery.isNotBlank()) FoundITIcons.Search else FoundITIcons.Radar,
                                    contentDescription = null,
                                    tint = BrandTealBase,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val isAuthRequired = feedState.error?.contains("PERMISSION_DENIED", ignoreCase = true) == true ||
                                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null

                            Text(
                                text = if (isAuthRequired) "Sign In to Browse Community Posts"
                                else if (feedState.searchQuery.isNotBlank()) "No Matching Items Found"
                                else "No Community Posts Yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isAuthRequired) {
                                    "Sign in with Google to explore real-time lost and found items reported in your community."
                                } else if (feedState.searchQuery.isNotBlank()) {
                                    "No posts match \"${feedState.searchQuery}\". Try another keyword or filter."
                                } else {
                                    "Be the first to publish a lost or found report to help the community."
                                },
                                fontSize = 13.sp,
                                color = TextSecondarySlate,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (isAuthRequired) {
                                Button(
                                    onClick = { onNavigateToAccount() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandTealBase,
                                        contentColor = ColorPureWhite
                                    )
                                ) {
                                    Text(
                                        text = "Sign In with Google",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            } else if (feedState.searchQuery.isNotBlank()) {
                                Button(
                                    onClick = { postViewModel.onSearchQueryChanged("") },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppSurfaceSubtle,
                                        contentColor = BrandTealBase
                                    ),
                                    modifier = Modifier.border(1.dp, AppBorderLight, RoundedCornerShape(12.dp))
                                ) {
                                    Text(
                                        text = "Clear Search",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { onNavigateToCreatePost("LOST") },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandTealBase,
                                        contentColor = ColorPureWhite
                                    )
                                ) {
                                    Text(
                                        text = "Create First Post",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to Report Item
        FloatingActionButton(
            onClick = { onNavigateToCreatePost("LOST") },
            containerColor = BrandTealBase,
            contentColor = ColorPureWhite,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = FoundITIcons.Add,
                    contentDescription = "Post Item",
                    tint = BrandMintBase,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Report Item",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ColorPureWhite
                )
            }
        }

        // Post Details Modal Dialog with ExoPlayer video playback or image viewer and Response Flow
        selectedPostForDetails?.let { post ->
            PostDetailsDialog(
                post = post,
                currentUser = currentUser,
                currentRequest = currentRequest,
                isSubmittingRequest = isSubmittingRequest,
                onDismiss = {
                    selectedPostForDetails = null
                    postViewModel.clearRequestFeedback()
                },
                onNavigateToSignIn = onNavigateToAccount,
                onSubmitRequest = { message ->
                    if (currentUser != null) {
                        postViewModel.submitPostRequest(
                            postId = post.id,
                            postOwnerUid = post.ownerUid,
                            currentUser = currentUser,
                            message = message,
                            onSuccess = {
                                postViewModel.checkUserRequestForPost(post.id, currentUser.uid)
                            }
                        )
                    }
                }
            )
        }
    }
}

/**
 * Live Post Card on Clean White Surface with Dark Crisp Text and Selective Mint/Teal Accents.
 */
@Composable
private fun PostItemCard(
    post: PostItem,
    onClick: () -> Unit
) {
    val isFound = post.postType.equals("FOUND", ignoreCase = true)
    val mediaUrl = post.displayMediaUrl

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ColorPureWhite)
            .border(1.dp, AppBorderLight, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Media Thumbnail Container
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppSurfaceSubtle)
                    .border(1.dp, AppBorderLight, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (mediaUrl.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = mediaUrl,
                        contentDescription = post.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    color = BrandTealBase,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        error = {
                            Icon(
                                imageVector = if (post.isVideo) FoundITIcons.Video else FoundITIcons.MediaImage,
                                contentDescription = null,
                                tint = TextSecondarySlate,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )

                    // Video Play Badge Overlay
                    if (post.isVideo) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(BrandTealBase.copy(alpha = 0.85f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FoundITIcons.Play,
                                contentDescription = "Play Video",
                                tint = BrandMintBase,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = if (post.isVideo) FoundITIcons.Video else FoundITIcons.MediaImage,
                        contentDescription = null,
                        tint = BrandTealBase,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = post.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Status Badge (FOUND in soft mint pill, LOST in clean neutral pill)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isFound) BrandMintBg else AppSurfaceSubtle)
                            .border(
                                1.dp,
                                if (isFound) BrandMintBase else AppBorderLight,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isFound) "FOUND" else "LOST",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFound) BrandTealBase else TextPrimaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = FoundITIcons.Location,
                        contentDescription = "Location",
                        tint = BrandTealBase,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.location,
                        fontSize = 11.sp,
                        color = TextSecondarySlate,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = post.category,
                        fontSize = 11.sp,
                        color = TextMutedSlate
                    )

                    if (post.authorName.isNotBlank()) {
                        Text(
                            text = "by ${post.authorName.split(" ").firstOrNull() ?: post.authorName}",
                            fontSize = 10.sp,
                            color = TextMutedSlate
                        )
                    }
                }
            }
        }
    }
}

/**
 * Detailed Dialog displaying high-res Cloudinary photo or interactive ExoPlayer video player,
 * with full Response / Claim Request flow.
 */
@Composable
private fun PostDetailsDialog(
    post: PostItem,
    currentUser: UserProfile?,
    currentRequest: PostRequest?,
    isSubmittingRequest: Boolean,
    onDismiss: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onSubmitRequest: (message: String) -> Unit
) {
    val isFound = post.postType.equals("FOUND", ignoreCase = true)
    val mediaUrl = post.displayMediaUrl
    val isOwner = currentUser?.uid == post.ownerUid

    var requestMessage by remember { mutableStateOf("") }
    var isWritingResponse by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ColorPureWhite,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(1.dp, AppBorderLight, RoundedCornerShape(20.dp))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Header with Close
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isFound) BrandMintBg else AppSurfaceSubtle)
                                .border(1.dp, if (isFound) BrandMintBase else AppBorderLight, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${post.status.uppercase()} • ${if (isFound) "FOUND" else "LOST"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFound) BrandTealBase else TextPrimaryDark
                            )
                        }

                        Icon(
                            imageVector = FoundITIcons.Close,
                            contentDescription = "Close",
                            tint = TextSecondarySlate,
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .clickable { onDismiss() }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // High-Res Media Container (ExoPlayer for Video or AsyncImage for Photo)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppSurfaceSubtle)
                            .border(1.dp, AppBorderLight, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (mediaUrl.isNotBlank()) {
                            if (post.isVideo) {
                                FoundITVideoPlayer(
                                    videoUrl = mediaUrl,
                                    modifier = Modifier.fillMaxSize(),
                                    autoPlay = true,
                                    useController = true
                                )
                            } else {
                                SubcomposeAsyncImage(
                                    model = mediaUrl,
                                    contentDescription = post.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    loading = {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(
                                                color = BrandTealBase,
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    },
                                    error = {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = FoundITIcons.MediaImage,
                                                contentDescription = null,
                                                tint = TextMutedSlate,
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Failed to load media",
                                                fontSize = 12.sp,
                                                color = TextSecondarySlate
                                            )
                                        }
                                    }
                                )
                            }
                        } else {
                            Icon(
                                imageVector = FoundITIcons.MediaImage,
                                contentDescription = null,
                                tint = BrandTealBase,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Title and Location
                item {
                    Text(
                        text = post.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimaryDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = FoundITIcons.Location,
                            contentDescription = "Location",
                            tint = BrandTealBase,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = post.location,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondarySlate
                        )
                    }

                    if (post.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = post.description,
                            fontSize = 12.sp,
                            color = TextSecondarySlate,
                            lineHeight = 17.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Category and Reporter Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppSurfaceSubtle)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Category", fontSize = 10.sp, color = TextMutedSlate)
                            Text(text = post.category, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Reported By", fontSize = 10.sp, color = TextMutedSlate)
                            Text(
                                text = if (isOwner) "You" else post.authorName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOwner) BrandTealBase else TextPrimaryDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- Request / Response Action Section ---
                item {
                    if (currentUser == null) {
                        // Unauthenticated prompt
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppSurfaceSubtle)
                                .border(1.dp, AppBorderLight, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Sign in to Respond to this Post",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onNavigateToSignIn,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandTealBase,
                                        contentColor = ColorPureWhite
                                    )
                                ) {
                                    Text("Sign In with Google", fontSize = 12.sp)
                                }
                            }
                        }
                    } else if (isOwner) {
                        // User is owner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(BrandMintBg)
                                .border(1.dp, BrandMintBase.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "You are the creator of this post. Check 'My Posts' to manage requests.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = BrandTealBase,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (currentRequest != null) {
                        // User already requested
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppSurfaceSubtle)
                                .border(1.dp, AppBorderLight, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Your Request Status:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (currentRequest.status) {
                                                    PostRequest.STATUS_ACCEPTED -> Color(0xFFDCFCE7)
                                                    PostRequest.STATUS_REJECTED -> Color(0xFFFEE2E2)
                                                    else -> BrandMintBg
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = currentRequest.status,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (currentRequest.status) {
                                                PostRequest.STATUS_ACCEPTED -> Color(0xFF166534)
                                                PostRequest.STATUS_REJECTED -> Color(0xFF991B1B)
                                                else -> BrandTealBase
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "\"${currentRequest.message}\"",
                                    fontSize = 12.sp,
                                    color = TextSecondarySlate
                                )
                            }
                        }
                    } else if (post.status.uppercase() != "OPEN") {
                        // Post is closed / resolved
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppSurfaceSubtle)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "This post is currently ${post.status.uppercase()} and is no longer accepting new responses.",
                                fontSize = 12.sp,
                                color = TextMutedSlate,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Eligible to submit request
                        if (!isWritingResponse) {
                            Button(
                                onClick = { isWritingResponse = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandTealBase,
                                    contentColor = ColorPureWhite
                                )
                            ) {
                                Text(
                                    text = if (isFound) "Claim This Item" else "I Found This Item",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Column {
                                OutlinedTextField(
                                    value = requestMessage,
                                    onValueChange = { requestMessage = it },
                                    placeholder = {
                                        Text(
                                            text = if (isFound) "Describe proof of ownership or identifiable marks..." else "Provide details on where and how you found this item...",
                                            fontSize = 12.sp,
                                            color = TextMutedSlate
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    minLines = 3,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandTealBase,
                                        unfocusedBorderColor = AppBorderLight
                                    )
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { isWritingResponse = false },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AppSurfaceSubtle,
                                            contentColor = TextSecondarySlate
                                        )
                                    ) {
                                        Text("Cancel", fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            if (requestMessage.isNotBlank()) {
                                                onSubmitRequest(requestMessage)
                                                isWritingResponse = false
                                            }
                                        },
                                        enabled = requestMessage.isNotBlank() && !isSubmittingRequest,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = BrandTealBase,
                                            contentColor = ColorPureWhite
                                        )
                                    ) {
                                        if (isSubmittingRequest) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = ColorPureWhite,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Text("Send Response", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
