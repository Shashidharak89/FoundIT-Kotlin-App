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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.shashidhara.foundit.ui.theme.AppSurfaceWhite
import com.shashidhara.foundit.ui.theme.BrandMintBase
import com.shashidhara.foundit.ui.theme.BrandMintBg
import com.shashidhara.foundit.ui.theme.BrandTealBase
import com.shashidhara.foundit.ui.theme.ColorPureWhite
import com.shashidhara.foundit.ui.theme.TextMutedSlate
import com.shashidhara.foundit.ui.theme.TextPrimaryDark
import com.shashidhara.foundit.ui.theme.TextSecondarySlate
import com.shashidhara.foundit.ui.viewmodel.PostViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    postViewModel: PostViewModel = viewModel(),
    currentUser: UserProfile? = null,
    onNavigateToCreatePost: (String) -> Unit = {},
    onNavigateToAccount: () -> Unit = {},
    onNavigateToMyPosts: () -> Unit = {},
    onSignInClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val feedState by postViewModel.feedUiState.collectAsState()
    val currentRequest by postViewModel.currentPostRequest.collectAsState()
    val isSubmittingRequest by postViewModel.isSubmittingRequest.collectAsState()
    val requestFeedback by postViewModel.requestFeedback.collectAsState()

    var selectedPostForDetails by remember { mutableStateOf<PostItem?>(null) }
    var postToEdit by remember { mutableStateOf<PostItem?>(null) }
    var postToDelete by remember { mutableStateOf<PostItem?>(null) }

    val listState = rememberLazyListState()

    // Local debounced search query state
    var searchInput by remember(feedState.searchQuery) { mutableStateOf(feedState.searchQuery) }

    LaunchedEffect(searchInput) {
        if (searchInput != feedState.searchQuery) {
            delay(350) // 350ms debounce
            postViewModel.onSearchQueryChanged(searchInput)
        }
    }

    val categories = remember {
        listOf("All Items", "Electronics", "Wallets & Cards", "Keys", "Bags", "Documents", "Clothing", "Other")
    }

    // Trigger pagination when scrolling near the bottom (20 items/page cursor pagination)
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

    // Automatically check request status when post details opens or when user signs in
    LaunchedEffect(selectedPostForDetails, currentUser?.uid) {
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
            // Hero Landing Section
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
                                    text = if (currentUser != null) "LOGGED IN • NETWORK ACTIVE" else "PUBLIC COMMUNITY FEED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorPureWhite,
                                    letterSpacing = 1.sp
                                )
                            }

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
                                onClick = {
                                    if (currentUser == null) {
                                        onSignInClick?.invoke() ?: onNavigateToAccount()
                                    } else {
                                        onNavigateToCreatePost("LOST")
                                    }
                                },
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
                                onClick = {
                                    if (currentUser == null) {
                                        onSignInClick?.invoke() ?: onNavigateToAccount()
                                    } else {
                                        onNavigateToCreatePost("FOUND")
                                    }
                                },
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

            // Search Bar with Debounce and Clear Button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = { searchInput = it },
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
                            if (searchInput.isNotBlank()) {
                                Icon(
                                    imageVector = FoundITIcons.Close,
                                    contentDescription = "Clear Search",
                                    tint = TextSecondarySlate,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            searchInput = ""
                                            postViewModel.onSearchQueryChanged("")
                                        }
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
                                color = if (isSelected) BrandMintBase else TextSecondarySlate
                            )
                        }
                    }
                }
            }

            // Category Chips (Horizontal Scrollable)
            item {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = feedState.selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) BrandTealBase else AppSurfaceSubtle)
                                .border(1.dp, if (isSelected) BrandTealBase else AppBorderLight, RoundedCornerShape(14.dp))
                                .clickable { postViewModel.onCategorySelected(category) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = category,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BrandMintBase else TextPrimaryDark
                            )
                        }
                    }
                }
            }

            // Status Filter Chips: ALL / OPEN / RESOLVED
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL" to "All Statuses", "OPEN" to "Active / Open", "RESOLVED" to "Resolved").forEach { (statusKey, label) ->
                        val isSelected = feedState.selectedStatus == statusKey
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) BrandTealBase else ColorPureWhite)
                                .border(1.dp, if (isSelected) BrandTealBase else AppBorderLight, RoundedCornerShape(14.dp))
                                .clickable { postViewModel.onStatusFilterChanged(statusKey) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) BrandMintBase else TextSecondarySlate
                            )
                        }
                    }
                }
            }

            // Section Header: Recent Posts
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT COMMUNITY POSTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMutedSlate,
                        letterSpacing = 1.2.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (feedState.posts.isNotEmpty()) {
                            Text(
                                text = "${feedState.posts.size} items",
                                fontSize = 11.sp,
                                color = TextMutedSlate,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

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

            // Render Recent Post Cards
            if (feedState.posts.isNotEmpty()) {
                items(feedState.posts, key = { it.id }) { post ->
                    RecentPostCard(
                        post = post,
                        currentUser = currentUser,
                        onClick = { selectedPostForDetails = post }
                    )
                }

                // Cursor Pagination Footer (20 posts per page)
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
                // Empty State for Public Visitors & Users
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

                            Text(
                                text = if (feedState.searchQuery.isNotBlank()) "No Matching Items Found" else "No Community Posts Yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (feedState.searchQuery.isNotBlank()) {
                                    "No posts match \"${feedState.searchQuery}\". Try searching another keyword or clearing your filter."
                                } else {
                                    "Be the first to publish a lost or found report to help the community."
                                },
                                fontSize = 13.sp,
                                color = TextSecondarySlate,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (feedState.searchQuery.isNotBlank()) {
                                Button(
                                    onClick = {
                                        searchInput = ""
                                        postViewModel.onSearchQueryChanged("")
                                    },
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
                                    onClick = {
                                        if (currentUser == null) {
                                            onSignInClick?.invoke() ?: onNavigateToAccount()
                                        } else {
                                            onNavigateToCreatePost("LOST")
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandTealBase,
                                        contentColor = ColorPureWhite
                                    )
                                ) {
                                    Text(
                                        text = "Report First Item",
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
            onClick = {
                if (currentUser == null) {
                    onSignInClick?.invoke() ?: onNavigateToAccount()
                } else {
                    onNavigateToCreatePost("LOST")
                }
            },
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

        // Complete Post Details Modal Dialog with Context-Aware Actions
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
                onSignInClick = {
                    onSignInClick?.invoke() ?: onNavigateToAccount()
                },
                onEditPost = {
                    postToEdit = post
                },
                onDeletePost = {
                    postToDelete = post
                },
                onStatusChange = { newStatus ->
                    postViewModel.updatePostStatus(
                        post = post,
                        newStatus = newStatus,
                        onSuccess = {
                            selectedPostForDetails = post.copy(status = newStatus)
                            Toast.makeText(context, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onTrackingChange = { newTracking ->
                    postViewModel.updatePostTrackingStatus(
                        post = post,
                        newTrackingStatus = newTracking,
                        onSuccess = {
                            selectedPostForDetails = post.copy(trackingStatus = newTracking)
                            Toast.makeText(context, "Tracking updated", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onNavigateToMyPosts = onNavigateToMyPosts,
                onSubmitRequest = { message ->
                    if (currentUser != null) {
                        postViewModel.submitPostRequest(
                            postId = post.id,
                            postOwnerUid = post.ownerUid,
                            currentUser = currentUser,
                            message = message,
                            onSuccess = {
                                postViewModel.checkUserRequestForPost(post.id, currentUser.uid)
                                Toast.makeText(context, "Request submitted successfully", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                onCancelRequest = {
                    if (currentUser != null && currentRequest != null) {
                        postViewModel.cancelPostRequest(
                            requestId = currentRequest!!.id,
                            postId = post.id,
                            currentUserUid = currentUser.uid,
                            onSuccess = {
                                postViewModel.checkUserRequestForPost(post.id, currentUser.uid)
                                Toast.makeText(context, "Request cancelled", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            )
        }

        // Edit Post Dialog
        postToEdit?.let { post ->
            EditPostDialog(
                post = post,
                onDismiss = { postToEdit = null },
                onSave = { title, desc, cat, loc ->
                    postViewModel.updatePost(
                        postId = post.id,
                        title = title,
                        description = desc,
                        category = cat,
                        location = loc,
                        status = post.status,
                        onSuccess = {
                            postToEdit = null
                            selectedPostForDetails = selectedPostForDetails?.copy(
                                title = title,
                                description = desc,
                                category = cat,
                                location = loc
                            )
                            Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show()
                        },
                        onError = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }

        // Delete Post Confirmation Dialog
        postToDelete?.let { post ->
            DeletePostConfirmDialog(
                postTitle = post.title,
                onDismiss = { postToDelete = null },
                onConfirm = {
                    postViewModel.deletePost(
                        postId = post.id,
                        onSuccess = {
                            postToDelete = null
                            selectedPostForDetails = null
                            Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                        },
                        onError = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }
    }
}

/**
 * Recent Post Card with Context-Aware Action Badges:
 * - Guest: "View Details"
 * - Owner: "Your Post"
 * - Another user: "Claim" or "Respond"
 * - Resolved/Closed: "Resolved" / "Closed"
 */
@Composable
private fun RecentPostCard(
    post: PostItem,
    currentUser: UserProfile?,
    onClick: () -> Unit
) {
    val isFound = post.postType.equals("FOUND", ignoreCase = true)
    val mediaUrl = post.displayMediaUrl
    val isOwner = currentUser?.uid != null && currentUser.uid == post.ownerUid
    val isTerminalStatus = post.status.equals("RESOLVED", ignoreCase = true) || post.status.equals("CLOSED", ignoreCase = true)

    val formattedDate = remember(post.createdAt) {
        post.createdAt?.toDate()?.let {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
        } ?: "Recent"
    }

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
            // Media Thumbnail Container (with video play badge if video)
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
                // Title and Lost/Found Badge
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Type Badge (FOUND in soft mint pill, LOST in clean neutral pill)
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

                // Location and Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = FoundITIcons.Location,
                            contentDescription = "Location",
                            tint = BrandTealBase,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = post.location.ifBlank { "Location unspecified" },
                            fontSize = 11.sp,
                            color = TextSecondarySlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = formattedDate,
                        fontSize = 10.sp,
                        color = TextMutedSlate
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Row: Category + Contextual Action Label
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

                    // Contextual Action/Status Label
                    if (isTerminalStatus) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = post.status.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondarySlate
                            )
                        }
                    } else if (isOwner) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BrandMintBg)
                                .border(1.dp, BrandMintBase, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Your Post",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandTealBase
                            )
                        }
                    } else if (currentUser == null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AppSurfaceSubtle)
                                .border(1.dp, AppBorderLight, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "View Details",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandTealBase
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BrandTealBase)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isFound) "Claim" else "Respond",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandMintBase
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Context-Aware Post Details Dialog:
 * - Public View: High-res photo/video, title, desc, category, location, date, status.
 * - Case A (Guest): Sign in to respond/claim button with seamless in-place authentication.
 * - Case B (Owner): Edit post, delete post, change status (Resolved/Closed), view received claims.
 * - Case C (Other User): Submit claim / response form with validation, prevention of duplicates.
 * - Case D (Admin): Administrator indicator and case management shortcuts.
 */
@Composable
private fun PostDetailsDialog(
    post: PostItem,
    currentUser: UserProfile?,
    currentRequest: PostRequest?,
    isSubmittingRequest: Boolean,
    onDismiss: () -> Unit,
    onSignInClick: () -> Unit,
    onEditPost: () -> Unit,
    onDeletePost: () -> Unit,
    onStatusChange: (newStatus: String) -> Unit,
    onTrackingChange: (newTracking: String) -> Unit,
    onNavigateToMyPosts: () -> Unit,
    onSubmitRequest: (message: String) -> Unit,
    onCancelRequest: () -> Unit
) {
    val isFound = post.postType.equals("FOUND", ignoreCase = true)
    val mediaUrl = post.displayMediaUrl
    val isOwner = currentUser?.uid != null && currentUser.uid == post.ownerUid
    val isAdmin = currentUser?.role.equals("ADMIN", ignoreCase = true)

    var requestMessage by remember { mutableStateOf("") }
    var isWritingResponse by remember { mutableStateOf(false) }

    val formattedDate = remember(post.createdAt) {
        post.createdAt?.toDate()?.let {
            SimpleDateFormat("MMMM dd, yyyy • hh:mm a", Locale.getDefault()).format(it)
        } ?: "Recently posted"
    }

    // Modal popup width goes up to 97% of the screen width with structured padding
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ColorPureWhite,
            modifier = Modifier
                .fillMaxWidth(0.97f)
                .fillMaxHeight(0.94f)
                .border(1.dp, AppBorderLight, RoundedCornerShape(24.dp))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Top Header: Type Badge, Role Badge, and Close Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isFound) BrandMintBg else AppSurfaceSubtle)
                                    .border(1.dp, if (isFound) BrandMintBase else AppBorderLight, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isFound) "FOUND ITEM" else "LOST ITEM",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isFound) BrandTealBase else TextPrimaryDark
                                )
                            }

                            if (isOwner) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BrandMintBg)
                                        .border(1.dp, BrandMintBase, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "YOUR POST",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandTealBase
                                    )
                                }
                            } else if (isAdmin) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BrandTealBase)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "ADMIN VIEW",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandMintBase
                                    )
                                }
                            }
                        }

                        Icon(
                            imageVector = FoundITIcons.Close,
                            contentDescription = "Close",
                            tint = TextSecondarySlate,
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .clickable { onDismiss() }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // ========================================================
                // 1. READONLY STATUS FIELD (Structured prominent display)
                // ========================================================
                item {
                    val statusUpper = post.status.uppercase()
                    val statusBg = when (statusUpper) {
                        "OPEN" -> Color(0xFFF0FDF4)
                        "PENDING" -> Color(0xFFFEF3C7)
                        "RESOLVED" -> Color(0xFFEFF6FF)
                        else -> AppSurfaceSubtle
                    }
                    val statusBorder = when (statusUpper) {
                        "OPEN" -> Color(0xFFBBF7D0)
                        "PENDING" -> Color(0xFFFDE68A)
                        "RESOLVED" -> Color(0xFFBFDBFE)
                        else -> AppBorderLight
                    }
                    val statusTextColor = when (statusUpper) {
                        "OPEN" -> Color(0xFF15803D)
                        "PENDING" -> Color(0xFFB45309)
                        "RESOLVED" -> Color(0xFF1D4ED8)
                        else -> TextPrimaryDark
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusBg)
                            .border(1.dp, statusBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "POST STATUS (READ-ONLY)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMutedSlate,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = when (statusUpper) {
                                        "OPEN" -> "Active • Open for Requests"
                                        "PENDING" -> "Claim Pending • Verification in Progress"
                                        "RESOLVED" -> "Resolved • Handed Over / Completed"
                                        "CLOSED" -> "Closed"
                                        else -> statusUpper
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusTextColor
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ColorPureWhite)
                                    .border(1.dp, statusBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = statusUpper,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = statusTextColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // ========================================================
                // 2. PRODUCT TRACKING (3 STAGES: Stranger, Security, Owner)
                // ========================================================
                item {
                    val currentTracking = post.currentTracking
                    val currentStepIndex = when (currentTracking) {
                        PostItem.TRACKING_WITH_SECURITY -> 1
                        PostItem.TRACKING_WITH_OWNER -> 2
                        else -> 0 // WITH_STRANGER
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppSurfaceSubtle)
                            .border(1.dp, AppBorderLight, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PRODUCT TRACKING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark,
                                letterSpacing = 1.sp
                            )

                            Text(
                                text = "WHERE IS THE PRODUCT?",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandTealBase
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3-Stage Visual Stepper
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val steps = listOf(
                                "With Stranger/Finder" to PostItem.TRACKING_WITH_STRANGER,
                                "With Security" to PostItem.TRACKING_WITH_SECURITY,
                                "Owner Received" to PostItem.TRACKING_WITH_OWNER
                            )

                            steps.forEachIndexed { index, (label, _) ->
                                val isPassed = index <= currentStepIndex
                                val isCurrent = index == currentStepIndex

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isPassed) BrandTealBase else AppSurfaceWhite
                                            )
                                            .border(
                                                1.5.dp,
                                                if (isPassed) BrandTealBase else AppBorderLight,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isPassed) {
                                            Icon(
                                                imageVector = FoundITIcons.Check,
                                                contentDescription = null,
                                                tint = BrandMintBase,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextSecondarySlate
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCurrent) BrandTealBase else TextSecondarySlate,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 13.sp
                                    )
                                }

                                if (index < steps.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .weight(0.5f)
                                            .height(2.dp)
                                            .background(if (index < currentStepIndex) BrandTealBase else AppBorderLight)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Description
                        Text(
                            text = when (currentTracking) {
                                PostItem.TRACKING_WITH_SECURITY ->
                                    "Current Location: Deposited with campus security / lost-and-found desk."
                                PostItem.TRACKING_WITH_OWNER ->
                                    "Current Location: Handed over and received by the verified owner."
                                else ->
                                    "Current Location: In possession of the stranger / reporter who found/posted it."
                            },
                            fontSize = 11.sp,
                            color = TextSecondarySlate
                        )

                        // If Admin: Provide buttons to update product tracking
                        if (isAdmin) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Update Product Location (Admin Only):",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMutedSlate
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "Stranger" to PostItem.TRACKING_WITH_STRANGER,
                                    "Security" to PostItem.TRACKING_WITH_SECURITY,
                                    "Owner Got" to PostItem.TRACKING_WITH_OWNER
                                ).forEach { (chipLabel, trackingVal) ->
                                    val isSelected = currentTracking == trackingVal
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) BrandTealBase else ColorPureWhite)
                                            .border(1.dp, if (isSelected) BrandTealBase else AppBorderLight, RoundedCornerShape(8.dp))
                                            .clickable { onTrackingChange(trackingVal) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = chipLabel,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) BrandMintBase else TextPrimaryDark
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // ========================================================
                // 3. MEDIA CONTAINER (With or without image)
                // ========================================================
                item {
                    if (mediaUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AppSurfaceSubtle)
                                .border(1.dp, AppBorderLight, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
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
                        }
                    } else {
                        // Clean placeholder when user posted without image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AppSurfaceSubtle)
                                .border(1.dp, AppBorderLight, RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = FoundITIcons.Search,
                                    contentDescription = null,
                                    tint = BrandTealBase,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "No Photo or Video Attached",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )
                                    Text(
                                        text = "This item was posted with text description and location details.",
                                        fontSize = 11.sp,
                                        color = TextSecondarySlate
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // ========================================================
                // 4. TITLE, DESCRIPTION, LOCATION, CATEGORY & DATE
                // ========================================================
                item {
                    Text(
                        text = post.title,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimaryDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = FoundITIcons.Location,
                            contentDescription = "Location",
                            tint = BrandTealBase,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = post.location.ifBlank { "Location unspecified" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondarySlate
                        )
                    }

                    if (post.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = post.description,
                            fontSize = 13.sp,
                            color = TextSecondarySlate,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
                            Text(text = "Date Posted", fontSize = 10.sp, color = TextMutedSlate)
                            Text(
                                text = formattedDate,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ========================================================
                // 5. CONTEXT-AWARE ACTIONS & CLAIM LIFECYCLE
                // ========================================================
                item {
                    if (currentUser == null) {
                        // CASE A: GUEST USER (Unauthenticated)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AppSurfaceSubtle)
                                .border(1.dp, AppBorderLight, RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isFound) "Think this is your item?" else "Did you find this item?",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sign in with Google to request for this product or submit a claim directly.",
                                    fontSize = 12.sp,
                                    color = TextSecondarySlate,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onSignInClick,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandTealBase,
                                        contentColor = ColorPureWhite
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isFound) "Sign in to Claim (It's Mine)" else "Sign in to Respond (I Found It)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else if (isAdmin) {
                        // CASE B1: ADMIN CONTROLS (Full management of both statuses + edit & delete)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AppSurfaceSubtle)
                                .border(1.dp, AppBorderLight, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Admin Case & Status Controls",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BrandTealBase)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ADMIN ONLY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BrandMintBase)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "1. Post Status (3 Options):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMutedSlate
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "OPEN" to "OPEN",
                                    "PENDING" to "PENDING",
                                    "RESOLVED" to "RESOLVED"
                                ).forEach { (label, statusVal) ->
                                    val isSelected = post.status.equals(statusVal, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) BrandTealBase else ColorPureWhite)
                                            .border(1.dp, if (isSelected) BrandTealBase else AppBorderLight, RoundedCornerShape(8.dp))
                                            .clickable { onStatusChange(statusVal) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isSelected) BrandMintBase else TextPrimaryDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "2. Product Tracking Status (3 Options):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMutedSlate
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "Stranger" to PostItem.TRACKING_WITH_STRANGER,
                                    "Security" to PostItem.TRACKING_WITH_SECURITY,
                                    "Owner Got" to PostItem.TRACKING_WITH_OWNER
                                ).forEach { (label, trackVal) ->
                                    val isSelected = post.currentTracking == trackVal
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) BrandTealBase else ColorPureWhite)
                                            .border(1.dp, if (isSelected) BrandTealBase else AppBorderLight, RoundedCornerShape(8.dp))
                                            .clickable { onTrackingChange(trackVal) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isSelected) BrandMintBase else TextPrimaryDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onEditPost,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Text("Edit Post", fontSize = 11.sp, color = BrandTealBase, fontWeight = FontWeight.SemiBold)
                                }

                                OutlinedButton(
                                    onClick = onDeletePost,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Text("Delete Post", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    } else if (isOwner) {
                        // CASE B2: REGULAR POST OWNER (Can ONLY Edit post details and Delete post. Status & tracking updates are left to Admin.)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AppSurfaceSubtle)
                                .border(1.dp, AppBorderLight, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "Post Management (Owner Controls)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "You can edit your post description, location, or delete this post. Status updates and product tracking verification are handled by campus administrators.",
                                fontSize = 11.sp,
                                color = TextSecondarySlate,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onEditPost,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                                ) {
                                    Text("Edit Post Details", fontSize = 12.sp, color = BrandTealBase, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = onDeletePost,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                                ) {
                                    Text("Delete Post", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    onDismiss()
                                    onNavigateToMyPosts()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Text("View Received Claims in Dashboard", fontSize = 11.sp, color = BrandTealBase)
                            }
                        }
                    } else if (currentRequest != null && currentRequest.status == PostRequest.STATUS_PENDING) {
                        // CASE C: USER WHO HAS REQUESTED THIS PRODUCT (Can cancel request -> post reverts to OPEN)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFEF3C7))
                                .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Your Submitted Request",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(ColorPureWhite)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "PENDING REVIEW",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "\"${currentRequest.message}\"",
                                    fontSize = 12.sp,
                                    color = Color(0xFF78350F)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Post status is locked while pending.",
                                        fontSize = 10.sp,
                                        color = Color(0xFF92400E)
                                    )

                                    Button(
                                        onClick = onCancelRequest,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFDC2626),
                                            contentColor = ColorPureWhite
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("Cancel Request", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else if (post.status.equals("PENDING", ignoreCase = true)) {
                        // CASE D: OTHER USER VIEWING WHEN POST IS ALREADY REQUESTED (LOCKED STATE)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AppSurfaceSubtle)
                                .border(1.dp, AppBorderLight, RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = FoundITIcons.Lock,
                                    contentDescription = "Locked",
                                    tint = BrandTealBase,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Product Request Under Review",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "A user has already requested this product. In this stage, new requests are locked. If the request is cancelled or rejected, this item will reopen to the community.",
                                    fontSize = 11.sp,
                                    color = TextSecondarySlate,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    } else if (!post.status.equals("OPEN", ignoreCase = true)) {
                        // CASE E: POST IS RESOLVED OR CLOSED
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppSurfaceSubtle)
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "This post is currently ${post.status.uppercase()} and is no longer accepting requests.",
                                fontSize = 12.sp,
                                color = TextMutedSlate,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // CASE F: ELIGIBLE USER VIEWING AN OPEN POST (Can request)
                        if (!isWritingResponse) {
                            Button(
                                onClick = { isWritingResponse = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandTealBase,
                                    contentColor = ColorPureWhite
                                )
                            ) {
                                Text(
                                    text = if (isFound) "Request for Product (This is My Product)" else "I Found This Product (Respond)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandMintBase
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AppSurfaceSubtle)
                                    .border(1.dp, AppBorderLight, RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = if (isFound) "Request Product (Claim Ownership)" else "Report Product Found",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (isFound) "Once submitted, the post status will change to Pending and will lock new requests."
                                    else "Once submitted, the post status will change to Pending for owner verification.",
                                    fontSize = 11.sp,
                                    color = TextSecondarySlate
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = requestMessage,
                                    onValueChange = { requestMessage = it },
                                    placeholder = {
                                        Text(
                                            text = if (isFound) "Describe unique identifiable marks or proof of ownership..."
                                            else "Describe where and how you found this item...",
                                            fontSize = 12.sp,
                                            color = TextMutedSlate
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
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
                                    TextButton(onClick = { isWritingResponse = false }) {
                                        Text("Cancel", fontSize = 12.sp, color = TextSecondarySlate)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            if (requestMessage.trim().isNotBlank()) {
                                                onSubmitRequest(requestMessage.trim())
                                                isWritingResponse = false
                                            }
                                        },
                                        enabled = requestMessage.trim().isNotBlank() && !isSubmittingRequest,
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
                                            Text(
                                                text = if (isFound) "Submit Product Request" else "Submit Found Report",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BrandMintBase
                                            )
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

/**
 * Edit Post Dialog allowing post owners to update their title, description, category, and location.
 */
@Composable
private fun EditPostDialog(
    post: PostItem,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, category: String, location: String) -> Unit
) {
    var title by remember { mutableStateOf(post.title) }
    var description by remember { mutableStateOf(post.description) }
    var category by remember { mutableStateOf(post.category) }
    var location by remember { mutableStateOf(post.location) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Electronics", "Wallets & Cards", "Keys", "Bags", "Documents", "Clothing", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Edit Post", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorText = null },
                    label = { Text("Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it; errorText = null },
                    label = { Text("Location *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) BrandTealBase else AppSurfaceSubtle)
                                .border(1.dp, if (isSelected) BrandTealBase else AppBorderLight, RoundedCornerShape(8.dp))
                                .clickable { category = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BrandMintBase else TextPrimaryDark
                            )
                        }
                    }
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorText.orEmpty(), color = Color(0xFFDC2626), fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.trim().isBlank() || location.trim().isBlank()) {
                        errorText = "Title and Location are required."
                        return@Button
                    }
                    onSave(title.trim(), description.trim(), category, location.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandTealBase)
            ) {
                Text("Save Changes", color = BrandMintBase)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondarySlate)
            }
        }
    )
}

/**
 * Delete Post Confirmation Dialog.
 */
@Composable
private fun DeletePostConfirmDialog(
    postTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Delete Post", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        },
        text = {
            Text(
                text = "Are you sure you want to permanently delete \"$postTitle\"? This action cannot be undone.",
                fontSize = 13.sp,
                color = TextSecondarySlate
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("Delete", color = ColorPureWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondarySlate)
            }
        }
    )
}
