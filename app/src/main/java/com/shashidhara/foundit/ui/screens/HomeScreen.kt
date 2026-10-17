package com.shashidhara.foundit.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
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
import com.shashidhara.foundit.ui.components.FoundITIcons
import com.shashidhara.foundit.ui.components.FoundITVideoPlayer
import com.shashidhara.foundit.ui.theme.AquaFoamBase
import com.shashidhara.foundit.ui.theme.AquaFoamDark
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
import com.shashidhara.foundit.ui.theme.DeepTealDark
import com.shashidhara.foundit.ui.theme.DeepTealDarker
import com.shashidhara.foundit.ui.theme.DeepTealDarkest
import com.shashidhara.foundit.ui.theme.DeepTealElevated
import com.shashidhara.foundit.ui.theme.MintBase
import com.shashidhara.foundit.ui.theme.MintLighter
import com.shashidhara.foundit.ui.viewmodel.PostViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    postViewModel: PostViewModel = viewModel(),
    onNavigateToCreatePost: (String) -> Unit = {},
    onNavigateToAccount: () -> Unit = {}
) {
    val feedState by postViewModel.feedUiState.collectAsState()
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepTealDarkest)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Hero Landing Section with Brand Logo
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(DeepTealDark, DeepTealDarker)
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.linearGradient(
                                colors = listOf(DeepTealElevated, MintBase.copy(alpha = 0.5f))
                            ),
                            RoundedCornerShape(24.dp)
                        )
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
                                    .background(CyberTealMuted)
                                    .border(1.dp, MintBase.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MintBase)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "COMMUNITY RECOVERY ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MintLighter,
                                    letterSpacing = 1.sp
                                )
                            }

                            // Landing Page Official Logo
                            Image(
                                painter = painterResource(id = R.drawable.foundit_logo),
                                contentDescription = "FoundIT Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Find What You Lost.\nReturn What You Found.",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MintLighter,
                            lineHeight = 30.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "A secure community lost-and-found network. Real-time items posted directly by people around you.",
                            fontSize = 13.sp,
                            color = AquaFoamLight,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Buttons: I Lost Something / I Found Something
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onNavigateToCreatePost("LOST") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MintBase,
                                    contentColor = DeepTealDark
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
                                    .height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DeepTealElevated,
                                    contentColor = MintLighter
                                )
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

            // Search Bar with Clear Button
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
                                color = AquaFoamDark,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = FoundITIcons.Search,
                                contentDescription = "Search",
                                tint = MintBase,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (feedState.searchQuery.isNotBlank()) {
                                Icon(
                                    imageVector = FoundITIcons.Close,
                                    contentDescription = "Clear Search",
                                    tint = AquaFoamLight,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { postViewModel.onSearchQueryChanged("") }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DeepTealDarker),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintBase,
                            unfocusedBorderColor = DeepTealElevated,
                            focusedTextColor = MintLighter,
                            unfocusedTextColor = AquaFoamLight,
                            cursorColor = MintBase
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }
            }

            // Horizontal Category Filter Pills
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = feedState.selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) MintBase else DeepTealDark)
                                .border(
                                    1.dp,
                                    if (isSelected) MintLighter else DeepTealElevated,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { postViewModel.onCategorySelected(category) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) DeepTealDark else AquaFoamLight
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
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COMMUNITY DISCOVERIES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AquaFoamDark,
                        letterSpacing = 1.2.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (feedState.posts.isNotEmpty()) "${feedState.posts.size} Items (Page ${feedState.page})" else "0 Items",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MintBase
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = FoundITIcons.Refresh,
                            contentDescription = "Refresh Feed",
                            tint = MintBase,
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
                                color = MintBase,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Loading community posts...",
                                color = AquaFoamLight,
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
                                    color = MintBase,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Loading page ${feedState.page + 1}...",
                                    fontSize = 13.sp,
                                    color = AquaFoamLight
                                )
                            }
                        } else if (feedState.hasMore) {
                            Button(
                                onClick = { postViewModel.loadNextPage() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DeepTealDark,
                                    contentColor = MintBase
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, DeepTealElevated, RoundedCornerShape(12.dp))
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
                                color = DeepTealElevated,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else if (!feedState.isLoading) {
                // Empty State (Zero Dummy Data)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(DeepTealDarker)
                            .border(1.dp, DeepTealElevated, RoundedCornerShape(20.dp))
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(CyberTealMuted)
                                    .border(1.dp, MintBase, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (feedState.searchQuery.isNotBlank()) FoundITIcons.Search else FoundITIcons.Radar,
                                    contentDescription = null,
                                    tint = MintBase,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val isAuthRequired = feedState.error?.contains("PERMISSION_DENIED", ignoreCase = true) == true ||
                                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null

                            Text(
                                text = if (isAuthRequired) "Sign In to Browse Community Posts"
                                else if (feedState.searchQuery.isNotBlank()) "No Matching Items Found"
                                else "No Community Posts Yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MintLighter,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isAuthRequired) {
                                    "Sign in with Google to explore real-time lost and found items reported in your community."
                                } else if (feedState.searchQuery.isNotBlank()) {
                                    "No posts match \"${feedState.searchQuery}\" in category \"${feedState.selectedCategory}\". Try another keyword."
                                } else {
                                    "Be the first to publish a lost or found report to help the community."
                                },
                                fontSize = 13.sp,
                                color = AquaFoamLight,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            if (isAuthRequired) {
                                Button(
                                    onClick = { onNavigateToAccount() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MintBase,
                                        contentColor = DeepTealDark
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
                                        containerColor = DeepTealDark,
                                        contentColor = MintBase
                                    ),
                                    modifier = Modifier.border(1.dp, MintBase.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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
                                        containerColor = MintBase,
                                        contentColor = DeepTealDark
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
            containerColor = MintBase,
            contentColor = DeepTealDark,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .border(1.5.dp, MintLighter, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = FoundITIcons.Add,
                    contentDescription = "Report Item",
                    tint = DeepTealDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Report",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = DeepTealDark
                )
            }
        }

        // Post Details Modal Dialog with ExoPlayer video playback or image viewer
        selectedPostForDetails?.let { post ->
            PostDetailsDialog(
                post = post,
                onDismiss = { selectedPostForDetails = null }
            )
        }
    }
}

/**
 * Live Post Card featuring Cloudinary media thumbnail, video indicator, and metadata.
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
            .clip(RoundedCornerShape(18.dp))
            .background(DeepTealDarker)
            .border(1.dp, DeepTealElevated, RoundedCornerShape(18.dp))
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
                    .clip(RoundedCornerShape(14.dp))
                    .background(DeepTealDark)
                    .border(1.dp, if (isFound) MintBase else DeepTealElevated, RoundedCornerShape(14.dp)),
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
                                    color = MintBase,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        error = {
                            Icon(
                                imageVector = if (post.isVideo) FoundITIcons.Video else FoundITIcons.MediaImage,
                                contentDescription = null,
                                tint = AquaFoamMedium,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )

                    // Video Play Badge Overlay
                    if (post.isVideo) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(DeepTealDarkest.copy(alpha = 0.8f))
                                .border(1.dp, MintBase, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FoundITIcons.Play,
                                contentDescription = "Play Video",
                                tint = MintLighter,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = if (post.isVideo) FoundITIcons.Video else FoundITIcons.MediaImage,
                        contentDescription = null,
                        tint = MintBase,
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
                        color = MintLighter,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )

                    // Status Badge (FOUND in Mint, LOST in Pine Tag)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isFound) CyberTealMuted else DeepTealDark)
                            .border(
                                1.dp,
                                if (isFound) MintBase else DeepTealElevated,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isFound) "FOUND" else "LOST",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFound) MintBase else AquaFoamLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = FoundITIcons.Location,
                        contentDescription = "Location",
                        tint = MintBase,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.location,
                        fontSize = 11.sp,
                        color = AquaFoamLight.copy(alpha = 0.85f),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = post.category,
                        fontSize = 11.sp,
                        color = AquaFoamDark
                    )

                    if (post.authorName.isNotBlank()) {
                        Text(
                            text = "by ${post.authorName.split(" ").firstOrNull() ?: post.authorName}",
                            fontSize = 10.sp,
                            color = AquaFoamLight.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Detailed Dialog displaying high-res Cloudinary photo or interactive ExoPlayer video player.
 */
@Composable
private fun PostDetailsDialog(
    post: PostItem,
    onDismiss: () -> Unit
) {
    val isFound = post.postType.equals("FOUND", ignoreCase = true)
    val mediaUrl = post.displayMediaUrl

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DeepTealDarker,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(
                    1.5.dp,
                    Brush.linearGradient(listOf(DeepTealElevated, MintBase.copy(alpha = 0.6f))),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isFound) CyberTealMuted else DeepTealDark)
                            .border(1.dp, MintBase, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isFound) "FOUND ITEM" else "LOST REPORT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFound) MintBase else MintLighter
                        )
                    }

                    Icon(
                        imageVector = FoundITIcons.Close,
                        contentDescription = "Close",
                        tint = AquaFoamLight,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High-Res Media Container (ExoPlayer for Video or AsyncImage for Photo)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DeepTealDarkest)
                        .border(1.dp, DeepTealElevated, RoundedCornerShape(16.dp)),
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
                                            color = MintBase,
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
                                            tint = AquaFoamMedium,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Failed to load media",
                                            fontSize = 12.sp,
                                            color = AquaFoamMedium
                                        )
                                    }
                                }
                            )
                        }
                    } else {
                        Icon(
                            imageVector = FoundITIcons.MediaImage,
                            contentDescription = null,
                            tint = AquaFoamBase,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = post.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MintLighter
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = FoundITIcons.Location,
                        contentDescription = "Location",
                        tint = MintBase,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.location,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AquaFoamLight
                    )
                }

                // Description (if present)
                if (post.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = post.description,
                        fontSize = 12.sp,
                        color = AquaFoamLight.copy(alpha = 0.9f),
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Metadata Footer: Category & Verified Cloudinary Media
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DeepTealDark)
                            .border(1.dp, DeepTealElevated, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = post.category,
                            fontSize = 11.sp,
                            color = MintBase
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MintBase)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cloudinary Verified",
                            fontSize = 10.sp,
                            color = AquaFoamLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Reporter profile row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepTealDark)
                        .border(1.dp, DeepTealElevated, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MintBase),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorName.take(1).uppercase().ifBlank { "U" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepTealDark
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Reported by ${post.authorName.ifBlank { "Community Member" }}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MintLighter
                        )
                        Text(
                            text = "FoundIT Community Network",
                            fontSize = 10.sp,
                            color = AquaFoamDark
                        )
                    }
                }
            }
        }
    }
}
