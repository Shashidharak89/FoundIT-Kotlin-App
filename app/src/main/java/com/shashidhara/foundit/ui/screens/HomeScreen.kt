package com.shashidhara.foundit.ui.screens

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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shashidhara.foundit.ui.components.FoundITIcons
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
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    val categories = remember {
        listOf("All Items", "Electronics", "Wallets & Cards", "Keys", "Bags")
    }

    val mockItems = remember {
        listOf(
            FoundItem(
                id = "1",
                title = "Sony WH-1000XM5 Headphones",
                category = "Electronics",
                location = "Central Station Platform 3",
                timeAgo = "15 mins ago",
                status = ItemStatus.FOUND,
                icon = FoundITIcons.Devices
            ),
            FoundItem(
                id = "2",
                title = "Leather Cardholder Wallet",
                category = "Wallets & Cards",
                location = "Metropolitan Coffee Co.",
                timeAgo = "1 hour ago",
                status = ItemStatus.LOST,
                icon = FoundITIcons.Wallet
            ),
            FoundItem(
                id = "3",
                title = "Car Keys with Cyber Lanyard",
                category = "Keys",
                location = "City Library 2nd Floor",
                timeAgo = "3 hours ago",
                status = ItemStatus.FOUND,
                icon = FoundITIcons.Key
            ),
            FoundItem(
                id = "4",
                title = "MacBook Pro M3 Charger & Hub",
                category = "Electronics",
                location = "Tech Park Gate 4",
                timeAgo = "5 hours ago",
                status = ItemStatus.FOUND,
                icon = FoundITIcons.Devices
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberTealDarkest),
        contentPadding = PaddingValues(bottom = 32.dp)
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
                                CyberTealBase,
                                AquaFoamBase.copy(alpha = 0.5f)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
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
                            text = "SMART RECOVERY ACTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaFoamLighter,
                            letterSpacing = 1.sp
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
                        text = "FoundIT connects community members to safely locate and recover lost belongings in real time.",
                        fontSize = 13.sp,
                        color = AquaFoamMedium,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { },
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
                            onClick = { },
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
                    number = "1,428",
                    label = "Items Reunited",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    number = "34",
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

        // Category Filter Chips (using robust horizontal scroll)
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

        // Section Title: Recent Reports
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT COMMUNITY DISCOVERIES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTealSoft,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "View All (48)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AquaFoamBase,
                    modifier = Modifier.clickable { }
                )
            }
        }

        // List of recent items with stable keys
        items(mockItems, key = { it.id }) { item ->
            ItemDiscoveryCard(item = item)
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
