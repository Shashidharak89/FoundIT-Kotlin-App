package com.shashidhara.foundit.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.shashidhara.foundit.data.model.PostItem
import com.shashidhara.foundit.data.model.PostRequest
import com.shashidhara.foundit.data.model.UserProfile
import com.shashidhara.foundit.ui.components.FoundITIcons
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
import com.shashidhara.foundit.ui.viewmodel.DashboardTab
import com.shashidhara.foundit.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    currentUser: UserProfile?,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by dashboardViewModel.uiState.collectAsState()

    var postToEdit by remember { mutableStateOf<PostItem?>(null) }
    var postToDelete by remember { mutableStateOf<PostItem?>(null) }

    LaunchedEffect(currentUser) {
        dashboardViewModel.onUserChanged(currentUser?.uid)
    }

    LaunchedEffect(uiState.actionSuccessMessage) {
        uiState.actionSuccessMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            dashboardViewModel.clearFeedback()
        }
    }

    LaunchedEffect(uiState.actionError) {
        uiState.actionError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            dashboardViewModel.clearFeedback()
        }
    }

    if (currentUser == null) {
        // Sign-in required prompt
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(AppBackgroundWhite)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(BrandMintBg)
                        .border(1.dp, BrandMintBase, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FoundITIcons.Account,
                        contentDescription = null,
                        tint = BrandTealBase,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sign In Required",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Sign in with your Google account to manage your reported posts and review received requests.",
                    fontSize = 13.sp,
                    color = TextSecondarySlate,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onNavigateToSignIn,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandTealBase,
                        contentColor = ColorPureWhite
                    )
                ) {
                    Text(text = "Sign In with Google", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackgroundWhite)
    ) {
        // Dashboard Tab Selector (My Posts | Requests Received | Sent Requests)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AppSurfaceSubtle)
                .border(1.dp, AppBorderLight, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DashboardTab.entries.forEach { tab ->
                val isSelected = uiState.selectedTab == tab
                val badgeCount = when (tab) {
                    DashboardTab.RECEIVED_REQUESTS -> uiState.receivedRequests.count { it.isPending }
                    DashboardTab.SENT_REQUESTS -> uiState.sentRequests.size
                    DashboardTab.MY_POSTS -> uiState.myPostsState.posts.size
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) BrandTealBase else Color.Transparent)
                        .clickable { dashboardViewModel.selectTab(tab) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (tab) {
                                DashboardTab.MY_POSTS -> "My Posts"
                                DashboardTab.RECEIVED_REQUESTS -> "Requests"
                                DashboardTab.SENT_REQUESTS -> "Sent"
                            },
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) ColorPureWhite else TextSecondarySlate
                        )

                        if (badgeCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) BrandMintBase else AppBorderLight)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "$badgeCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) BrandTealBase else TextSecondarySlate
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tab Content
        when (uiState.selectedTab) {
            DashboardTab.MY_POSTS -> {
                MyPostsTabContent(
                    uiState = uiState.myPostsState,
                    receivedRequests = uiState.receivedRequests,
                    onSearchQueryChanged = { dashboardViewModel.onSearchQueryChanged(it) },
                    onCategorySelected = { dashboardViewModel.onCategorySelected(it) },
                    onTypeFilterSelected = { dashboardViewModel.onTypeFilterSelected(it) },
                    onStatusFilterSelected = { dashboardViewModel.onStatusFilterSelected(it) },
                    onLoadNextPage = { dashboardViewModel.loadNextMyPostsPage() },
                    onRefresh = { dashboardViewModel.refreshMyPosts() },
                    onEditClick = { postToEdit = it },
                    onDeleteClick = { postToDelete = it },
                    onNavigateToCreatePost = onNavigateToCreatePost
                )
            }
            DashboardTab.RECEIVED_REQUESTS -> {
                ReceivedRequestsTabContent(
                    requests = uiState.receivedRequests,
                    onAccept = { request -> dashboardViewModel.acceptRequest(request, markPostPending = true) },
                    onReject = { request -> dashboardViewModel.rejectRequest(request) }
                )
            }
            DashboardTab.SENT_REQUESTS -> {
                SentRequestsTabContent(
                    requests = uiState.sentRequests,
                    onCancel = { request -> dashboardViewModel.cancelSentRequest(request.id) }
                )
            }
        }
    }

    // Edit Post Dialog
    postToEdit?.let { post ->
        EditPostDialog(
            post = post,
            onDismiss = { postToEdit = null },
            onSave = { title, desc, cat, loc, status ->
                dashboardViewModel.updatePostDetails(
                    postId = post.id,
                    title = title,
                    description = desc,
                    category = cat,
                    location = loc,
                    status = status,
                    onSuccess = { postToEdit = null }
                )
            }
        )
    }

    // Delete Confirmation Dialog
    postToDelete?.let { post ->
        AlertDialog(
            onDismissRequest = { postToDelete = null },
            title = {
                Text(
                    text = "Delete Post?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${post.title}\"? This action cannot be undone.",
                    fontSize = 13.sp,
                    color = TextSecondarySlate
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        dashboardViewModel.deletePost(post.id) {
                            postToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
                        contentColor = ColorPureWhite
                    )
                ) {
                    Text("Delete Post")
                }
            },
            dismissButton = {
                TextButton(onClick = { postToDelete = null }) {
                    Text("Cancel", color = TextSecondarySlate)
                }
            },
            containerColor = ColorPureWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun MyPostsTabContent(
    uiState: com.shashidhara.foundit.ui.viewmodel.MyPostsUiState,
    receivedRequests: List<PostRequest>,
    onSearchQueryChanged: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onTypeFilterSelected: (String) -> Unit,
    onStatusFilterSelected: (String) -> Unit,
    onLoadNextPage: () -> Unit,
    onRefresh: () -> Unit,
    onEditClick: (PostItem) -> Unit,
    onDeleteClick: (PostItem) -> Unit,
    onNavigateToCreatePost: () -> Unit
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 3 && lastVisibleItem >= totalItems - 2
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && uiState.hasMore && !uiState.isLoading && !uiState.isLoadingMore) {
            onLoadNextPage()
        }
    }

    val categories = remember {
        listOf("All Items", "Electronics", "Wallets & Cards", "Keys", "Bags", "Documents", "Clothing", "Other")
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Search Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = { Text("Search your posts...", color = TextMutedSlate, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = FoundITIcons.Search,
                            contentDescription = "Search",
                            tint = BrandTealBase,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ColorPureWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandTealBase,
                        unfocusedBorderColor = AppBorderLight,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }
        }

        // Filters: Type (ALL, LOST, FOUND) and Status (ALL, OPEN, PENDING, RESOLVED, CLOSED)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Type Chips
                listOf("ALL" to "All Types", "LOST" to "Lost", "FOUND" to "Found").forEach { (typeKey, label) ->
                    val isSelected = uiState.selectedType == typeKey
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) BrandTealBase else ColorPureWhite)
                            .border(1.dp, if (isSelected) BrandTealBase else AppBorderLight, RoundedCornerShape(16.dp))
                            .clickable { onTypeFilterSelected(typeKey) }
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

                Spacer(modifier = Modifier.width(6.dp))

                // Status Chips
                listOf("ALL" to "All Statuses", "OPEN" to "Open", "PENDING" to "Pending", "RESOLVED" to "Resolved").forEach { (statusKey, label) ->
                    val isSelected = uiState.selectedStatus == statusKey
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) BrandMintBg else ColorPureWhite)
                            .border(1.dp, if (isSelected) BrandMintBase else AppBorderLight, RoundedCornerShape(16.dp))
                            .clickable { onStatusFilterSelected(statusKey) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) BrandTealBase else TextSecondarySlate
                        )
                    }
                }
            }
        }

        // Category Horizontal Scroll
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = uiState.selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) AppSurfaceSubtle else ColorPureWhite)
                            .border(1.dp, if (isSelected) BrandTealBase else AppBorderLight, RoundedCornerShape(16.dp))
                            .clickable { onCategorySelected(cat) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) BrandTealBase else TextSecondarySlate
                        )
                    }
                }
            }
        }

        // Section Stats
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.posts.size} Posts Published",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )

                Icon(
                    imageVector = FoundITIcons.Refresh,
                    contentDescription = "Refresh",
                    tint = BrandTealBase,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onRefresh() }
                )
            }
        }

        if (uiState.isLoading && uiState.posts.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandTealBase, strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
                }
            }
        } else if (uiState.posts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColorPureWhite)
                        .border(1.dp, AppBorderLight, RoundedCornerShape(16.dp))
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Posts Found",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You haven't reported any lost or found items yet matching the current filters.",
                            fontSize = 12.sp,
                            color = TextSecondarySlate,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onNavigateToCreatePost,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandTealBase,
                                contentColor = ColorPureWhite
                            )
                        ) {
                            Text("Create a Post", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(uiState.posts, key = { it.id }) { post ->
                val postPendingRequestsCount = receivedRequests.count { it.postId == post.id && it.isPending }
                MyPostCard(
                    post = post,
                    pendingRequestsCount = postPendingRequestsCount,
                    onEdit = { onEditClick(post) },
                    onDelete = { onDeleteClick(post) }
                )
            }

            // Pagination Footer
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.isLoadingMore) {
                        CircularProgressIndicator(color = BrandTealBase, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else if (uiState.hasMore) {
                        OutlinedButton(
                            onClick = onLoadNextPage,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Load More Posts (Page ${uiState.page + 1})", fontSize = 12.sp, color = BrandTealBase)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MyPostCard(
    post: PostItem,
    pendingRequestsCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isFound = post.type.equals("FOUND", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ColorPureWhite)
            .border(1.dp, AppBorderLight, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Media Thumbnail
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppSurfaceSubtle)
                        .border(1.dp, AppBorderLight, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (post.imageUrl.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = post.imageUrl,
                            contentDescription = post.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = if (post.isVideo) FoundITIcons.Video else FoundITIcons.MediaImage,
                            contentDescription = null,
                            tint = BrandTealBase,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

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

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (post.status.uppercase()) {
                                        "OPEN" -> BrandMintBg
                                        "PENDING" -> Color(0xFFFEF9C3) // Light yellow
                                        "RESOLVED" -> Color(0xFFDCFCE7) // Light green
                                        else -> AppSurfaceSubtle
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = post.status.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (post.status.uppercase()) {
                                    "OPEN" -> BrandTealBase
                                    "PENDING" -> Color(0xFF854D0E)
                                    "RESOLVED" -> Color(0xFF166534)
                                    else -> TextSecondarySlate
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "${post.type} • ${post.category} • ${post.location}",
                        fontSize = 11.sp,
                        color = TextSecondarySlate,
                        maxLines = 1
                    )

                    if (pendingRequestsCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(BrandMintBase))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$pendingRequestsCount Pending ${if (pendingRequestsCount == 1) "Request" else "Requests"}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandTealBase
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Edit and Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(imageVector = FoundITIcons.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = BrandTealBase)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 11.sp, color = BrandTealBase)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Delete", fontSize = 11.sp, color = Color(0xFFDC2626))
                }
            }
        }
    }
}

@Composable
private fun ReceivedRequestsTabContent(
    requests: List<PostRequest>,
    onAccept: (PostRequest) -> Unit,
    onReject: (PostRequest) -> Unit
) {
    if (requests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No Requests Received Yet",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "When other users respond or submit claims for items you reported, they will appear here immediately.",
                    fontSize = 12.sp,
                    color = TextSecondarySlate,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        ) {
            items(requests, key = { it.id }) { request ->
                ReceivedRequestCard(
                    request = request,
                    onAccept = { onAccept(request) },
                    onReject = { onReject(request) }
                )
            }
        }
    }
}

@Composable
private fun ReceivedRequestCard(
    request: PostRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ColorPureWhite)
            .border(1.dp, AppBorderLight, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Requester Avatar
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(BrandMintBg)
                            .border(1.dp, BrandMintBase, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = request.requesterName.take(1).uppercase(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTealBase
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = request.requesterName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Post: #${request.postId.take(8)}",
                            fontSize = 10.sp,
                            color = TextMutedSlate
                        )
                    }
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (request.status) {
                                PostRequest.STATUS_ACCEPTED -> Color(0xFFDCFCE7)
                                PostRequest.STATUS_REJECTED -> Color(0xFFFEE2E2)
                                else -> BrandMintBg
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = request.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (request.status) {
                            PostRequest.STATUS_ACCEPTED -> Color(0xFF166534)
                            PostRequest.STATUS_REJECTED -> Color(0xFF991B1B)
                            else -> BrandTealBase
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Message Body
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppSurfaceSubtle)
                    .padding(10.dp)
            ) {
                Text(
                    text = "\"${request.message}\"",
                    fontSize = 12.sp,
                    color = TextPrimaryDark,
                    lineHeight = 16.sp
                )
            }

            // Decision Actions for Pending Requests
            if (request.isPending) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Reject", fontSize = 11.sp, color = Color(0xFFDC2626))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandTealBase,
                            contentColor = ColorPureWhite
                        )
                    ) {
                        Text("Accept & Arrange", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SentRequestsTabContent(
    requests: List<PostRequest>,
    onCancel: (PostRequest) -> Unit
) {
    if (requests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No Sent Requests",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "When you respond to items on the Home feed, your submitted requests will appear here so you can track their status in real time.",
                    fontSize = 12.sp,
                    color = TextSecondarySlate,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        ) {
            items(requests, key = { it.id }) { request ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ColorPureWhite)
                        .border(1.dp, AppBorderLight, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Post #${request.postId.take(10)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )

                            // Status Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (request.status) {
                                            PostRequest.STATUS_ACCEPTED -> Color(0xFFDCFCE7)
                                            PostRequest.STATUS_REJECTED -> Color(0xFFFEE2E2)
                                            PostRequest.STATUS_CANCELLED -> AppSurfaceSubtle
                                            else -> BrandMintBg
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = request.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (request.status) {
                                        PostRequest.STATUS_ACCEPTED -> Color(0xFF166534)
                                        PostRequest.STATUS_REJECTED -> Color(0xFF991B1B)
                                        PostRequest.STATUS_CANCELLED -> TextMutedSlate
                                        else -> BrandTealBase
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Your message: \"${request.message}\"",
                            fontSize = 12.sp,
                            color = TextSecondarySlate
                        )

                        if (request.isPending) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(
                                    onClick = { onCancel(request) },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Withdraw Request", fontSize = 11.sp, color = Color(0xFFDC2626))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditPostDialog(
    post: PostItem,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, category: String, location: String, status: String) -> Unit
) {
    var title by remember { mutableStateOf(post.title) }
    var description by remember { mutableStateOf(post.description) }
    var category by remember { mutableStateOf(post.category) }
    var location by remember { mutableStateOf(post.location) }
    var status by remember { mutableStateOf(post.status) }

    val categories = remember {
        listOf("Electronics", "Wallets & Cards", "Keys", "Bags", "Documents", "Clothing", "Other")
    }

    val statuses = remember {
        listOf("OPEN", "PENDING", "RESOLVED", "CLOSED")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ColorPureWhite,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Edit Post Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Status", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondarySlate)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statuses.forEach { s ->
                        val isSelected = status == s
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) BrandTealBase else AppSurfaceSubtle)
                                .clickable { status = s }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = s,
                                fontSize = 11.sp,
                                color = if (isSelected) ColorPureWhite else TextPrimaryDark,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondarySlate)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank() && location.isNotBlank()) {
                                onSave(title, description, category, location, status)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandTealBase,
                            contentColor = ColorPureWhite
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
