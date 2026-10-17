package com.shashidhara.foundit.ui.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shashidhara.foundit.R
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
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
import com.shashidhara.foundit.ui.viewmodel.PostViewModel

data class FoundItem(
    val id: String,
    val title: String,
    val category: String,
    val location: String,
    val timeAgo: String,
    val status: ItemStatus,
    val icon: ImageVector
)

enum class ItemStatus(val label: String) {
    FOUND("FOUND"),
    LOST("LOST REPORT")
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    postViewModel: PostViewModel = viewModel(),
    onNavigateToCreatePost: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var selectedPostForDetails by remember { mutableStateOf<PostItem?>(null) }

    val livePosts by postViewModel.posts.collectAsState()

    val categories = remember {
        listOf("All Items", "Electronics", "Wallets & Cards", "Keys", "Bags", "Documents", "Clothing", "Other")
    }

    val mockItems = remember {
        listOf(
            FoundItem(
                id = "m1",
                title = "Sony WH-1000XM5 Headphones",
                category = "Electronics",
                location = "Central Station Platform 3",
                timeAgo = "15 mins ago",
                status = ItemStatus.FOUND,
                icon = FoundITIcons.Devices
            ),
            FoundItem(
                id = "m2",
                title = "Leather Cardholder Wallet",
                category = "Wallets & Cards",
                location = "Metropolitan Coffee Co.",
                timeAgo = "1 hour ago",
                status = ItemStatus.LOST,
                icon = FoundITIcons.Wallet
            ),
            FoundItem(
                id = "m3",
                title = "Car Keys with Cyber Lanyard",
                category = "Keys",
                location = "City Library 2nd Floor",
                timeAgo = "3 hours ago",
                status = ItemStatus.FOUND,
                icon = FoundITIcons.Key
            ),
            FoundItem(
                id = "m4",
                title = "MacBook Pro M3 Charger & Hub",
                category = "Electronics",
                location = "Tech Park Gate 4",
                timeAgo = "5 hours ago",
                status = ItemStatus.FOUND,
                icon = FoundITIcons.Devices
            )
        )
    }

    // Filter live posts by search query and category
    val filteredLivePosts = remember(livePosts, searchQuery, selectedCategoryIndex) {
        val selectedCategory = categories[selectedCategoryIndex]
        livePosts.filter { post ->
            val matchesCategory = selectedCategory == "All Items" || post.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    post.title.contains(searchQuery, ignoreCase = true) ||
                    post.location.contains(searchQuery, ignoreCase = true) ||
                    post.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    // Filter mock items if no live posts match
    val filteredMockItems = remember(mockItems, searchQuery, selectedCategoryIndex) {
        val selectedCategory = categories[selectedCategoryIndex]
        mockItems.filter { item ->
            val matchesCategory = selectedCategory == "All Items" || item.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.location.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Box(modifier = modifier.fillMaxSize().background(CyberTealDarkest)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Hero Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(CyberTealDark, CyberTealDarker)
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.linearGradient(
                                colors = listOf(CyberTealBase, AquaFoamBase.copy(alpha = 0.5f))
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        // Landing Page Header: Badge + Brand Logo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Badge
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(CyberTealMuted)
                                    .border(1.dp, AquaFoamBase.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AquaFoamBase)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "COMMUNITY RECOVERY ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AquaFoamLighter,
                                    letterSpacing = 1.sp
                                )
                            }

                            // Landing Page Logo
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
                            color = AquaFoamLighter,
                            lineHeight = 30.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Upload photos or videos to Cloudinary to document lost belongings and facilitate fast community returns.",
                            fontSize = 13.sp,
                            color = AquaFoamMedium,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action buttons connected to CreatePostScreen
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
                                    containerColor = CyberTealBase,
                                    contentColor = AquaFoamLighter
                                )
                            ) {
                                Icon(
                                    imageVector = FoundITIcons.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = AquaFoamLighter
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "I Lost Item",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { onNavigateToCreatePost("FOUND") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .border(1.dp, AquaFoamBase, RoundedCornerShape(14.dp)),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberTealMuted,
                                    contentColor = AquaFoamBase
                                )
                            ) {
                                Icon(
                                    imageVector = FoundITIcons.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = AquaFoamBase
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "I Found Item",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Stats Banner
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        number = "${1428 + livePosts.size}",
                        label = "Items Reunited",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        number = "${livePosts.size.coerceAtLeast(34)}",
                        label = "Live Alerts",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        number = "98%",
                        label = "Success Rate",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search Bar
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search keys, laptop, wallet, bag...",
                                color = AquaFoamDark,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = FoundITIcons.Search,
                                contentDescription = "Search",
                                tint = AquaFoamBase
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CyberTealDark),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AquaFoamBase,
                            unfocusedBorderColor = CyberTealElevated,
                            focusedTextColor = AquaFoamLighter,
                            unfocusedTextColor = AquaFoamLight,
                            cursorColor = AquaFoamBase
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }
            }

            // Category Filter Chips
            item {
                val chipsScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(chipsScrollState)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEachIndexed { index, category ->
                        val isSelected = selectedCategoryIndex == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) CyberTealBase else CyberTealDark)
                                .border(
                                    1.dp,
                                    if (isSelected) AquaFoamBase else CyberTealElevated,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedCategoryIndex = index }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AquaFoamLighter else AquaFoamLight
                            )
                        }
                    }
                }
            }

            // Section Header: Recent Community Discoveries
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COMMUNITY DISCOVERIES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTealSoft,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Live (${filteredLivePosts.size + (if (filteredLivePosts.isEmpty()) filteredMockItems.size else 0)})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AquaFoamBase
                    )
                }
            }

            // Render Live Posts with Cloudinary media
            if (filteredLivePosts.isNotEmpty()) {
                items(filteredLivePosts, key = { it.id }) { post ->
                    PostItemCard(
                        post = post,
                        onClick = { selectedPostForDetails = post }
                    )
                }
            } else {
                // Fallback to mock items when no posts are in Firestore
                items(filteredMockItems, key = { it.id }) { item ->
                    ItemDiscoveryCard(item = item)
                }
            }
        }

        // Floating Action Button to Report Item
        FloatingActionButton(
            onClick = { onNavigateToCreatePost("LOST") },
            containerColor = CyberTealBase,
            contentColor = AquaFoamLighter,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .border(1.5.dp, AquaFoamBase, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = FoundITIcons.Add,
                    contentDescription = "Report Item",
                    tint = AquaFoamLighter,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Report",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AquaFoamLighter
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
            .background(CyberTealDarker)
            .border(1.dp, CyberTealElevated, RoundedCornerShape(18.dp))
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
                    .background(CyberTealDark)
                    .border(1.dp, CyberTealBase, RoundedCornerShape(14.dp)),
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
                                    color = AquaFoamBase,
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
                                .background(CyberTealDarkest.copy(alpha = 0.8f))
                                .border(1.dp, AquaFoamBase, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FoundITIcons.Play,
                                contentDescription = "Play Video",
                                tint = AquaFoamLighter,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = if (post.isVideo) FoundITIcons.Video else FoundITIcons.MediaImage,
                        contentDescription = null,
                        tint = AquaFoamBase,
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
                        color = AquaFoamLighter,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )

                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isFound) AquaFoamMuted else CyberTealMuted)
                            .border(
                                1.dp,
                                if (isFound) AquaFoamBase else CyberTealLight,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isFound) "FOUND" else "LOST",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFound) AquaFoamLighter else CyberTealSoft
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = FoundITIcons.Location,
                        contentDescription = "Location",
                        tint = AquaFoamMedium,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.location,
                        fontSize = 11.sp,
                        color = AquaFoamLight.copy(alpha = 0.8f),
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
                        color = CyberTealSoft
                    )

                    if (post.authorName.isNotBlank()) {
                        Text(
                            text = "by ${post.authorName.split(" ").firstOrNull() ?: post.authorName}",
                            fontSize = 10.sp,
                            color = AquaFoamDark
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
            color = CyberTealDarker,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(
                    1.5.dp,
                    Brush.linearGradient(listOf(CyberTealBase, AquaFoamBase.copy(alpha = 0.6f))),
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
                            .background(if (isFound) AquaFoamMuted else CyberTealMuted)
                            .border(1.dp, AquaFoamBase, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isFound) "FOUND ITEM" else "LOST REPORT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaFoamLighter
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CyberTealDark)
                            .border(1.dp, CyberTealElevated, CircleShape)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FoundITIcons.Close,
                            contentDescription = "Close",
                            tint = AquaFoamBase,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Media Presentation: ExoPlayer for Video or SubcomposeAsyncImage for Photo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(CyberTealDarkest)
                        .border(1.dp, CyberTealElevated, RoundedCornerShape(18.dp))
                ) {
                    if (post.isVideo && mediaUrl.isNotBlank()) {
                        FoundITVideoPlayer(
                            videoUrl = mediaUrl,
                            modifier = Modifier.fillMaxSize(),
                            autoPlay = true,
                            useController = true
                        )
                    } else if (mediaUrl.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = mediaUrl,
                            contentDescription = post.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = FoundITIcons.MediaImage,
                                contentDescription = null,
                                tint = AquaFoamMedium,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Post Title
                Text(
                    text = post.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AquaFoamLighter
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Location & Category
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = FoundITIcons.Location,
                        contentDescription = "Location",
                        tint = AquaFoamBase,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.location,
                        fontSize = 12.sp,
                        color = AquaFoamLight,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "•  ${post.category}",
                        fontSize = 12.sp,
                        color = CyberTealSoft
                    )
                }

                // Description
                if (post.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = post.description,
                        fontSize = 12.sp,
                        color = AquaFoamLight.copy(alpha = 0.85f),
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Author Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberTealDark)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CyberTealBase),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorName.take(1).uppercase().ifBlank { "U" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaFoamLighter
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Reported by ${post.authorName.ifBlank { "Community Member" }}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AquaFoamLighter
                        )
                        Text(
                            text = "Cloudinary Verified Media",
                            fontSize = 10.sp,
                            color = CyberTealSoft
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    number: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CyberTealDark)
            .border(1.dp, CyberTealElevated, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = number,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = AquaFoamBase
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = AquaFoamMedium
            )
        }
    }
}

@Composable
private fun ItemDiscoveryCard(item: FoundItem) {
    val isFound = item.status == ItemStatus.FOUND

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(CyberTealDarker)
            .border(1.dp, CyberTealElevated, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberTealMedium.copy(alpha = 0.5f))
                    .border(1.dp, CyberTealBase, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = AquaFoamBase,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AquaFoamLighter,
                        modifier = Modifier.weight(1f)
                    )

                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isFound) AquaFoamMuted else CyberTealMuted)
                            .border(
                                1.dp,
                                if (isFound) AquaFoamBase else CyberTealLight,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.status.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFound) AquaFoamLighter else CyberTealSoft
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = FoundITIcons.Location,
                        contentDescription = "Location",
                        tint = AquaFoamMedium,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.location,
                        fontSize = 11.sp,
                        color = AquaFoamLight.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = FoundITIcons.Schedule,
                        contentDescription = "Time",
                        tint = CyberTealSoft,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.timeAgo,
                        fontSize = 11.sp,
                        color = CyberTealSoft
                    )
                }
            }
        }
    }
}
