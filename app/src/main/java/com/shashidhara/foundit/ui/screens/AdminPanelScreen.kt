package com.shashidhara.foundit.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.shashidhara.foundit.data.model.AuditLogEntry
import com.shashidhara.foundit.data.model.CaseLifecycle
import com.shashidhara.foundit.data.model.HandoverRecord
import com.shashidhara.foundit.data.model.PostItem
import com.shashidhara.foundit.data.model.PostRequest
import com.shashidhara.foundit.data.model.UserProfile
import com.shashidhara.foundit.ui.components.FoundITIcons
import com.shashidhara.foundit.ui.theme.AppBackgroundWhite
import com.shashidhara.foundit.ui.theme.AppBorderLight
import com.shashidhara.foundit.ui.theme.AppSurfaceSubtle
import com.shashidhara.foundit.ui.theme.AppSurfaceWhite
import com.shashidhara.foundit.ui.theme.BrandMintBase
import com.shashidhara.foundit.ui.theme.BrandMintBg
import com.shashidhara.foundit.ui.theme.BrandTealBase
import com.shashidhara.foundit.ui.theme.BrandTealLight
import com.shashidhara.foundit.ui.theme.ColorPureBlack
import com.shashidhara.foundit.ui.theme.ColorPureWhite
import com.shashidhara.foundit.ui.theme.TextMutedSlate
import com.shashidhara.foundit.ui.theme.TextPrimaryDark
import com.shashidhara.foundit.ui.theme.TextSecondarySlate
import com.shashidhara.foundit.ui.viewmodel.AdminTab
import com.shashidhara.foundit.ui.viewmodel.AdminUiState
import com.shashidhara.foundit.ui.viewmodel.AdminViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AdminPanelScreen(
    adminViewModel: AdminViewModel = viewModel(),
    currentUser: UserProfile?,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by adminViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                adminViewModel.clearFeedback()
            }
        }
    }

    LaunchedEffect(uiState.actionError) {
        uiState.actionError?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                adminViewModel.clearFeedback()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundWhite)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar
            AdminHeader(
                selectedTab = uiState.selectedTab,
                onRefresh = {
                    when (uiState.selectedTab) {
                        AdminTab.DASHBOARD -> adminViewModel.loadMetrics()
                        AdminTab.CASES -> adminViewModel.loadCases(reset = true)
                        AdminTab.REQUESTS -> adminViewModel.loadRequests(reset = true)
                        AdminTab.USERS -> adminViewModel.loadUsers(reset = true)
                        AdminTab.AUDIT -> adminViewModel.loadAuditLogs(reset = true)
                    }
                }
            )

            // Tabs Selector
            AdminTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelected = { adminViewModel.selectTab(it) }
            )

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (uiState.selectedTab) {
                    AdminTab.DASHBOARD -> AdminDashboardOverview(
                        uiState = uiState,
                        onTabSelect = { adminViewModel.selectTab(it) },
                        onRefresh = { adminViewModel.loadMetrics() }
                    )
                    AdminTab.CASES -> AdminCasesView(
                        uiState = uiState,
                        onSearch = { adminViewModel.onSearchCases(it) },
                        onFilterType = { adminViewModel.onFilterCasesType(it) },
                        onFilterStatus = { adminViewModel.onFilterCasesStatus(it) },
                        onLoadMore = { adminViewModel.loadCases(reset = false) },
                        onStatusClick = { adminViewModel.selectCaseForStatusChange(it) },
                        onReceiptClick = { adminViewModel.selectCaseForReceipt(it) },
                        onReturnClick = { adminViewModel.selectCaseForReturn(it) },
                        onTimelineClick = { adminViewModel.selectCaseForTimeline(it) }
                    )
                    AdminTab.REQUESTS -> AdminRequestsView(
                        uiState = uiState,
                        onSearch = { adminViewModel.onSearchRequests(it) },
                        onFilterStatus = { adminViewModel.onFilterRequestsStatus(it) },
                        onLoadMore = { adminViewModel.loadRequests(reset = false) },
                        onOverrideClick = { adminViewModel.selectRequestForOverride(it) }
                    )
                    AdminTab.USERS -> AdminUsersView(
                        uiState = uiState,
                        onSearch = { adminViewModel.onSearchUsers(it) },
                        onLoadMore = { adminViewModel.loadUsers(reset = false) }
                    )
                    AdminTab.AUDIT -> AdminAuditLogsView(
                        uiState = uiState,
                        onLoadMore = { adminViewModel.loadAuditLogs(reset = false) }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Status Change Dialog
        uiState.selectedCaseForStatus?.let { caseItem ->
            StatusChangeDialog(
                caseItem = caseItem,
                isActionInProgress = uiState.isActionInProgress,
                onDismiss = { adminViewModel.selectCaseForStatusChange(null) },
                onConfirm = { newStatus, reason ->
                    adminViewModel.updateCaseStatus(
                        postId = caseItem.id,
                        previousStatus = caseItem.status,
                        newStatus = newStatus,
                        reason = reason
                    )
                }
            )
        }

        // Security Receipt Dialog
        uiState.selectedCaseForReceipt?.let { caseItem ->
            SecurityReceiptDialog(
                caseItem = caseItem,
                isActionInProgress = uiState.isActionInProgress,
                onDismiss = { adminViewModel.selectCaseForReceipt(null) },
                onSubmit = { staffName, staffId, location, condition, accessories, notes, refNum, photoUri ->
                    adminViewModel.submitSecurityReceipt(
                        postId = caseItem.id,
                        itemTitle = caseItem.title,
                        location = location,
                        staffName = staffName,
                        staffId = staffId,
                        itemCondition = condition,
                        accessories = accessories,
                        notes = notes,
                        referenceNumber = refNum,
                        evidenceUri = photoUri,
                        context = context
                    )
                }
            )
        }

        // Owner Return Dialog
        uiState.selectedCaseForReturn?.let { caseItem ->
            OwnerReturnDialog(
                caseItem = caseItem,
                isActionInProgress = uiState.isActionInProgress,
                onDismiss = { adminViewModel.selectCaseForReturn(null) },
                onSubmit = { ownerName, ownerUid, verificationMethod, staffName, staffId, location, condition, accessories, notes, refNum, confirmation, photoUri ->
                    adminViewModel.submitOwnerReturn(
                        postId = caseItem.id,
                        itemTitle = caseItem.title,
                        location = location,
                        staffName = staffName,
                        staffId = staffId,
                        itemCondition = condition,
                        accessories = accessories,
                        notes = notes,
                        referenceNumber = refNum,
                        verifiedOwnerName = ownerName,
                        verifiedOwnerUid = ownerUid,
                        verificationMethod = verificationMethod,
                        recipientConfirmation = confirmation,
                        evidenceUri = photoUri,
                        context = context
                    )
                }
            )
        }

        // Custody Timeline Dialog
        uiState.selectedCaseForTimeline?.let { caseItem ->
            CustodyTimelineDialog(
                caseItem = caseItem,
                records = uiState.timelineRecords,
                isLoading = uiState.isLoadingTimeline,
                onDismiss = { adminViewModel.selectCaseForTimeline(null) }
            )
        }

        // Request Override Dialog
        uiState.selectedRequestForOverride?.let { request ->
            RequestOverrideDialog(
                request = request,
                isActionInProgress = uiState.isActionInProgress,
                onDismiss = { adminViewModel.selectRequestForOverride(null) },
                onConfirm = { newStatus, reason ->
                    adminViewModel.submitRequestOverride(
                        requestId = request.id,
                        postId = request.postId,
                        previousStatus = request.status,
                        newStatus = newStatus,
                        reason = reason
                    )
                }
            )
        }
    }
}

@Composable
private fun AdminHeader(
    selectedTab: AdminTab,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppSurfaceWhite)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(BrandTealBase),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FoundITIcons.Shield,
                        contentDescription = "Admin Shield",
                        tint = BrandMintBase,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Security & Admin Panel",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            }
            Text(
                text = "Authorized administrative custody and case control",
                fontSize = 11.sp,
                color = TextSecondarySlate
            )
        }

        // Refresh Button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AppSurfaceSubtle)
                .border(1.dp, AppBorderLight, CircleShape)
                .clickable { onRefresh() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = FoundITIcons.Search, // clean icon representation
                contentDescription = "Refresh",
                tint = BrandTealBase,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AdminTabRow(
    selectedTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppSurfaceWhite)
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AdminTab.values().forEach { tab ->
            val isSelected = tab == selectedTab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) BrandTealBase else AppSurfaceSubtle)
                    .border(
                        1.dp,
                        if (isSelected) BrandTealLight else AppBorderLight,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = tab.title,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) BrandMintBase else TextSecondarySlate
                )
            }
        }
    }
}

// ==========================================
// TAB 1: DASHBOARD OVERVIEW
// ==========================================

@Composable
private fun AdminDashboardOverview(
    uiState: AdminUiState,
    onTabSelect: (AdminTab) -> Unit,
    onRefresh: () -> Unit
) {
    if (uiState.isLoadingMetrics) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandTealBase)
        }
        return
    }

    val metrics = uiState.metrics ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Free tier Spark efficiency banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(BrandMintBg)
                .border(1.dp, BrandMintBase.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = FoundITIcons.Check,
                    contentDescription = null,
                    tint = BrandTealBase,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Firebase Spark Aggregation: Server counts calculated instantly without loading full documents. 100% free-tier optimized.",
                    fontSize = 11.sp,
                    color = BrandTealBase,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "CASE SUMMARY METRICS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMutedSlate,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Metrics Grid (Rows of 2 cards)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(
                title = "Total Posts",
                count = metrics.totalPosts,
                subtitle = "All registered reports",
                modifier = Modifier.weight(1f),
                accentColor = BrandTealBase
            )
            MetricCard(
                title = "Open Reports",
                count = metrics.openReports,
                subtitle = "Active for claims",
                modifier = Modifier.weight(1f),
                accentColor = Color(0xFF0284C7)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(
                title = "Pending Claims",
                count = metrics.pendingClaims,
                subtitle = "Awaiting match review",
                modifier = Modifier.weight(1f),
                accentColor = Color(0xFFD97706)
            )
            MetricCard(
                title = "Awaiting Handover",
                count = metrics.awaitingHandover,
                subtitle = "To be given to security",
                modifier = Modifier.weight(1f),
                accentColor = Color(0xFFEA580C)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(
                title = "With Security",
                count = metrics.withSecurity,
                subtitle = "Items in physical custody",
                modifier = Modifier.weight(1f),
                accentColor = BrandTealBase
            )
            MetricCard(
                title = "Awaiting Collection",
                count = metrics.awaitingCollection,
                subtitle = "Owner notified to collect",
                modifier = Modifier.weight(1f),
                accentColor = Color(0xFF4F46E5)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(
                title = "Returned to Owner",
                count = metrics.returnedToOwner,
                subtitle = "Verified owner return",
                modifier = Modifier.weight(1f),
                accentColor = Color(0xFF059669)
            )
            MetricCard(
                title = "Resolved Cases",
                count = metrics.resolvedCases,
                subtitle = "Case lifecycle closed",
                modifier = Modifier.weight(1f),
                accentColor = Color(0xFF10B981)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(
                title = "Pending Requests",
                count = metrics.pendingRequests,
                subtitle = "User claims awaiting review",
                modifier = Modifier.weight(1f),
                accentColor = Color(0xFFF59E0B)
            )
            MetricCard(
                title = "Rejected Requests",
                count = metrics.rejectedRequests,
                subtitle = "Denied claim attempts",
                modifier = Modifier.weight(1f),
                accentColor = Color(0xFFEF4444)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "QUICK ADMINISTRATIVE ACTIONS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMutedSlate,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        AdminQuickActionRow(
            title = "Manage All Cases",
            subtitle = "Filter, verify status, and audit custody for all lost and found reports",
            onClick = { onTabSelect(AdminTab.CASES) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        AdminQuickActionRow(
            title = "Review User Requests",
            subtitle = "Inspect and override claims across all campus posts",
            onClick = { onTabSelect(AdminTab.REQUESTS) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        AdminQuickActionRow(
            title = "Browse Registered Users",
            subtitle = "Search accounts and view participation across cases",
            onClick = { onTabSelect(AdminTab.USERS) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        AdminQuickActionRow(
            title = "Audit History Log",
            subtitle = "Inspect privileged changes and custody tracking entries",
            onClick = { onTabSelect(AdminTab.AUDIT) }
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun MetricCard(
    title: String,
    count: Long,
    subtitle: String,
    modifier: Modifier = Modifier,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurfaceWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AppBorderLight))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondarySlate
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextMutedSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdminQuickActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppSurfaceWhite)
            .border(1.dp, AppBorderLight, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondarySlate
                )
            }
            Icon(
                imageVector = FoundITIcons.ChevronRight,
                contentDescription = null,
                tint = BrandTealBase,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ==========================================
// TAB 2: CASES VIEW
// ==========================================

@Composable
private fun AdminCasesView(
    uiState: AdminUiState,
    onSearch: (String) -> Unit,
    onFilterType: (String) -> Unit,
    onFilterStatus: (String) -> Unit,
    onLoadMore: () -> Unit,
    onStatusClick: (PostItem) -> Unit,
    onReceiptClick: (PostItem) -> Unit,
    onReturnClick: (PostItem) -> Unit,
    onTimelineClick: (PostItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppSurfaceWhite)
                .padding(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.casesSearchQuery,
                onValueChange = onSearch,
                placeholder = { Text("Search by title, location, UID, or Post ID...", fontSize = 12.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = FoundITIcons.Search, contentDescription = null, tint = TextMutedSlate, modifier = Modifier.size(18.dp))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandTealBase,
                    unfocusedBorderColor = AppBorderLight
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Type filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", "LOST", "FOUND").forEach { type ->
                    val isSelected = uiState.casesTypeFilter == type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) BrandTealBase else AppSurfaceSubtle)
                            .border(1.dp, if (isSelected) BrandTealLight else AppBorderLight, RoundedCornerShape(8.dp))
                            .clickable { onFilterType(type) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (type == "ALL") "All Types" else type,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) BrandMintBase else TextSecondarySlate
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status filter chips
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val statuses = listOf("ALL") + CaseLifecycle.ALL_STATUSES
                statuses.forEach { st ->
                    val isSelected = uiState.casesStatusFilter == st
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) BrandTealBase else AppSurfaceSubtle)
                            .border(1.dp, if (isSelected) BrandTealLight else AppBorderLight, RoundedCornerShape(8.dp))
                            .clickable { onFilterStatus(st) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (st == "ALL") "All Statuses" else CaseLifecycle.getStatusLabel(st),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) BrandMintBase else TextSecondarySlate
                        )
                    }
                }
            }
        }

        // Cases List
        if (uiState.isLoadingCases) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandTealBase)
            }
        } else if (uiState.cases.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No cases match the selected search and filter criteria.",
                    fontSize = 13.sp,
                    color = TextSecondarySlate,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.cases, key = { it.id }) { caseItem ->
                    AdminCaseCard(
                        caseItem = caseItem,
                        onStatusClick = { onStatusClick(caseItem) },
                        onReceiptClick = { onReceiptClick(caseItem) },
                        onReturnClick = { onReturnClick(caseItem) },
                        onTimelineClick = { onTimelineClick(caseItem) }
                    )
                }

                if (uiState.hasMoreCases) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isLoadingMoreCases) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BrandTealBase)
                            } else {
                                OutlinedButton(
                                    onClick = onLoadMore,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandTealBase)
                                ) {
                                    Text("Load More Cases", fontSize = 12.sp)
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
private fun AdminCaseCard(
    caseItem: PostItem,
    onStatusClick: () -> Unit,
    onReceiptClick: () -> Unit,
    onReturnClick: () -> Unit,
    onTimelineClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurfaceWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AppBorderLight))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Type Badge + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isLost = caseItem.type.equals("LOST", ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isLost) Color(0xFFFEE2E2) else BrandMintBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = caseItem.type.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLost) Color(0xFFDC2626) else BrandTealBase
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = caseItem.category,
                        fontSize = 11.sp,
                        color = TextMutedSlate
                    )
                }

                // Status Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(getStatusColor(caseItem.status).copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = CaseLifecycle.getStatusLabel(caseItem.status),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = getStatusColor(caseItem.status)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body: Thumbnail + Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                if (caseItem.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = caseItem.imageUrl,
                        contentDescription = caseItem.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, AppBorderLight, RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = caseItem.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = caseItem.description,
                        fontSize = 12.sp,
                        color = TextSecondarySlate,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = FoundITIcons.Location, contentDescription = null, tint = TextMutedSlate, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = caseItem.location.ifBlank { "Location unspecified" },
                            fontSize = 11.sp,
                            color = TextMutedSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Meta Info: Post ID + Owner UID
            Text(
                text = "ID: ${caseItem.id.take(8)}... | Owner: ${caseItem.ownerUid.take(8)}...",
                fontSize = 10.sp,
                color = TextMutedSlate
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onStatusClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text("Change Status", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = BrandTealBase)
                }

                // Show Receipt action if case is awaiting handover or in an open/pending stage
                if (caseItem.status == CaseLifecycle.STATUS_AWAITING_SECURITY_HANDOVER ||
                    caseItem.status == CaseLifecycle.STATUS_OPEN ||
                    caseItem.status == CaseLifecycle.STATUS_PENDING
                ) {
                    Button(
                        onClick = onReceiptClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandTealBase),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Text("Security Receipt", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandMintBase)
                    }
                } else if (caseItem.status == CaseLifecycle.STATUS_WITH_SECURITY ||
                    caseItem.status == CaseLifecycle.STATUS_AWAITING_OWNER_COLLECTION
                ) {
                    // Show Return action if with security
                    Button(
                        onClick = onReturnClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Text("Return to Owner", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorPureWhite)
                    }
                }

                OutlinedButton(
                    onClick = onTimelineClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text("Custody", fontSize = 10.sp, color = TextSecondarySlate)
                }
            }
        }
    }
}

// ==========================================
// TAB 3: REQUESTS REVIEW VIEW
// ==========================================

@Composable
private fun AdminRequestsView(
    uiState: AdminUiState,
    onSearch: (String) -> Unit,
    onFilterStatus: (String) -> Unit,
    onLoadMore: () -> Unit,
    onOverrideClick: (PostRequest) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppSurfaceWhite)
                .padding(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.requestsSearchQuery,
                onValueChange = onSearch,
                placeholder = { Text("Search by requester, UID, post ID...", fontSize = 12.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = FoundITIcons.Search, contentDescription = null, tint = TextMutedSlate, modifier = Modifier.size(18.dp))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandTealBase,
                    unfocusedBorderColor = AppBorderLight
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", PostRequest.STATUS_PENDING, PostRequest.STATUS_ACCEPTED, PostRequest.STATUS_REJECTED, PostRequest.STATUS_CANCELLED).forEach { st ->
                    val isSelected = uiState.requestsStatusFilter == st
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) BrandTealBase else AppSurfaceSubtle)
                            .border(1.dp, if (isSelected) BrandTealLight else AppBorderLight, RoundedCornerShape(8.dp))
                            .clickable { onFilterStatus(st) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (st == "ALL") "All Requests" else st,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) BrandMintBase else TextSecondarySlate
                        )
                    }
                }
            }
        }

        if (uiState.isLoadingRequests) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandTealBase)
            }
        } else if (uiState.requests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No user claims found.", fontSize = 13.sp, color = TextSecondarySlate)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.requests, key = { it.id }) { req ->
                    AdminRequestCard(
                        request = req,
                        onOverrideClick = { onOverrideClick(req) }
                    )
                }

                if (uiState.hasMoreRequests) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            if (uiState.isLoadingMoreRequests) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BrandTealBase)
                            } else {
                                OutlinedButton(onClick = onLoadMore, shape = RoundedCornerShape(10.dp)) {
                                    Text("Load More Requests", fontSize = 12.sp)
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
private fun AdminRequestCard(
    request: PostRequest,
    onOverrideClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurfaceWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AppBorderLight))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.requesterName.ifBlank { "User" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (request.status) {
                                PostRequest.STATUS_ACCEPTED -> Color(0xFFD1FAE5)
                                PostRequest.STATUS_REJECTED -> Color(0xFFFEE2E2)
                                else -> Color(0xFFFEF3C7)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = request.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (request.status) {
                            PostRequest.STATUS_ACCEPTED -> Color(0xFF065F46)
                            PostRequest.STATUS_REJECTED -> Color(0xFF991B1B)
                            else -> Color(0xFF92400E)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "\"${request.message}\"",
                fontSize = 12.sp,
                color = TextSecondarySlate
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Post ID: ${request.postId.take(8)}... | Requester UID: ${request.requesterUid.take(8)}...",
                fontSize = 10.sp,
                color = TextMutedSlate
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onOverrideClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Admin Override", fontSize = 10.sp, color = BrandTealBase, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ==========================================
// TAB 4: USERS DIRECTORY VIEW
// ==========================================

@Composable
private fun AdminUsersView(
    uiState: AdminUiState,
    onSearch: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppSurfaceWhite)
                .padding(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.usersSearchQuery,
                onValueChange = onSearch,
                placeholder = { Text("Search users by name, email, or UID...", fontSize = 12.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = FoundITIcons.Search, contentDescription = null, tint = TextMutedSlate, modifier = Modifier.size(18.dp))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandTealBase,
                    unfocusedBorderColor = AppBorderLight
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (uiState.isLoadingUsers) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandTealBase)
            }
        } else if (uiState.users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No users found matching query.", fontSize = 13.sp, color = TextSecondarySlate)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.users, key = { it.uid }) { user ->
                    AdminUserCard(user = user)
                }

                if (uiState.hasMoreUsers) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            if (uiState.isLoadingMoreUsers) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BrandTealBase)
                            } else {
                                OutlinedButton(onClick = onLoadMore, shape = RoundedCornerShape(10.dp)) {
                                    Text("Load More Users", fontSize = 12.sp)
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
private fun AdminUserCard(user: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurfaceWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AppBorderLight))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppSurfaceSubtle)
                    .border(1.dp, AppBorderLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = FoundITIcons.Account,
                    contentDescription = null,
                    tint = BrandTealBase,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.name.ifBlank { "User" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val isAdmin = user.role.equals("ADMIN", ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isAdmin) BrandTealBase else AppSurfaceSubtle)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isAdmin) "ADMIN" else "USER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAdmin) BrandMintBase else TextSecondarySlate
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = user.email,
                    fontSize = 11.sp,
                    color = TextSecondarySlate
                )
                Text(
                    text = "UID: ${user.uid.take(12)}...",
                    fontSize = 10.sp,
                    color = TextMutedSlate
                )
            }
        }
    }
}

// ==========================================
// TAB 5: AUDIT LOGS VIEW
// ==========================================

@Composable
private fun AdminAuditLogsView(
    uiState: AdminUiState,
    onLoadMore: () -> Unit
) {
    if (uiState.isLoadingLogs) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandTealBase)
        }
    } else if (uiState.auditLogs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No administrative audit logs recorded yet.", fontSize = 13.sp, color = TextSecondarySlate)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.auditLogs, key = { it.id }) { log ->
                AdminAuditCard(log = log)
            }

            if (uiState.hasMoreLogs) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        if (uiState.isLoadingMoreLogs) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BrandTealBase)
                        } else {
                            OutlinedButton(onClick = onLoadMore, shape = RoundedCornerShape(10.dp)) {
                                Text("Load More Audit Logs", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminAuditCard(log: AuditLogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurfaceWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AppBorderLight))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BrandMintBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = log.action,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandTealBase
                    )
                }

                val dateStr = remember(log.createdAt) {
                    log.createdAt?.toDate()?.let {
                        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(it)
                    } ?: "Recent"
                }

                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = TextMutedSlate
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (log.previousStatus.isNotBlank() || log.newStatus.isNotBlank()) {
                Text(
                    text = "${log.previousStatus} ➔ ${log.newStatus}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = "Reason: ${log.reason}",
                fontSize = 12.sp,
                color = TextSecondarySlate
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Actor: ${log.actorName} (${log.actorUid.take(8)}...) | Target: ${log.entityType} #${log.entityId.take(8)}...",
                fontSize = 10.sp,
                color = TextMutedSlate
            )
        }
    }
}

// ==========================================
// DIALOGS: STATUS, HANDOVER, OVERRIDE
// ==========================================

@Composable
private fun StatusChangeDialog(
    caseItem: PostItem,
    isActionInProgress: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (newStatus: String, reason: String) -> Unit
) {
    val allowedTransitions = remember(caseItem.status) {
        CaseLifecycle.getAllowedTransitions(caseItem.status)
    }

    var selectedStatus by remember { mutableStateOf(allowedTransitions.firstOrNull() ?: "") }
    var reason by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Update Case Status", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Post: ${caseItem.title}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondarySlate
                )
                Text(
                    text = "Current Status: ${CaseLifecycle.getStatusLabel(caseItem.status)}",
                    fontSize = 11.sp,
                    color = TextMutedSlate
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (allowedTransitions.isEmpty()) {
                    Text(
                        text = "This case has reached a final state (${caseItem.status}). No standard transitions permitted.",
                        fontSize = 12.sp,
                        color = Color(0xFFDC2626)
                    )
                } else {
                    Text(
                        text = "Permitted Next Transitions:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )

                    allowedTransitions.forEach { targetStatus ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStatus = targetStatus }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedStatus == targetStatus,
                                onClick = { selectedStatus = targetStatus },
                                colors = RadioButtonDefaults.colors(selectedColor = BrandTealBase)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = CaseLifecycle.getStatusLabel(targetStatus),
                                fontSize = 12.sp,
                                color = TextPrimaryDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        validationError = null
                    },
                    label = { Text("Administrative Reason *", fontSize = 11.sp) },
                    placeholder = { Text("e.g. Owner verified custody on campus", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = validationError != null,
                    supportingText = validationError?.let { { Text(it, color = Color(0xFFDC2626), fontSize = 10.sp) } }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedStatus.isBlank()) {
                        validationError = "Please select a target status"
                        return@Button
                    }
                    if (reason.trim().isBlank()) {
                        validationError = "Reason is mandatory for status change audit"
                        return@Button
                    }
                    onConfirm(selectedStatus, reason.trim())
                },
                enabled = !isActionInProgress && allowedTransitions.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandTealBase)
            ) {
                if (isActionInProgress) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrandMintBase)
                } else {
                    Text("Confirm", color = BrandMintBase)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isActionInProgress) {
                Text("Cancel", color = TextSecondarySlate)
            }
        }
    )
}

@Composable
private fun SecurityReceiptDialog(
    caseItem: PostItem,
    isActionInProgress: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        staffName: String,
        staffId: String,
        location: String,
        condition: String,
        accessories: String,
        notes: String,
        refNum: String,
        photoUri: Uri?
    ) -> Unit
) {
    var staffName by remember { mutableStateOf("") }
    var staffId by remember { mutableStateOf("") }
    var location by remember { mutableStateOf(caseItem.location) }
    var condition by remember { mutableStateOf("Good / Intact") }
    var accessories by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var refNum by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedPhotoUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Security Receipt into Custody", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Case: ${caseItem.title} (${caseItem.id.take(8)})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandTealBase
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = staffName,
                    onValueChange = { staffName = it; validationError = null },
                    label = { Text("Receiving Security Staff Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = staffId,
                    onValueChange = { staffId = it },
                    label = { Text("Staff Badge / Officer ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Custody Storage Location (e.g. Desk A)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = condition,
                    onValueChange = { condition = it },
                    label = { Text("Physical Condition") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = accessories,
                    onValueChange = { accessories = it },
                    label = { Text("Contents / Accessories Received") },
                    placeholder = { Text("e.g. Charging cable, phone case") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = refNum,
                    onValueChange = { refNum = it },
                    label = { Text("Custody Reference / Tag #") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Administrative Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Evidence Photo Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedPhotoUri != null) "Evidence photo attached" else "Optional evidence photo",
                        fontSize = 11.sp,
                        color = if (selectedPhotoUri != null) Color(0xFF059669) else TextSecondarySlate
                    )

                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (selectedPhotoUri != null) "Change Photo" else "Select Photo", fontSize = 11.sp)
                    }
                }

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(validationError.orEmpty(), color = Color(0xFFDC2626), fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (staffName.trim().isBlank()) {
                        validationError = "Staff name is required for physical custody record"
                        return@Button
                    }
                    onSubmit(
                        staffName.trim(),
                        staffId.trim(),
                        location.trim(),
                        condition.trim(),
                        accessories.trim(),
                        notes.trim(),
                        refNum.trim(),
                        selectedPhotoUri
                    )
                },
                enabled = !isActionInProgress,
                colors = ButtonDefaults.buttonColors(containerColor = BrandTealBase)
            ) {
                if (isActionInProgress) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrandMintBase)
                } else {
                    Text("Confirm Receipt", color = BrandMintBase)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isActionInProgress) {
                Text("Cancel", color = TextSecondarySlate)
            }
        }
    )
}

@Composable
private fun OwnerReturnDialog(
    caseItem: PostItem,
    isActionInProgress: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        ownerName: String,
        ownerUid: String,
        verificationMethod: String,
        staffName: String,
        staffId: String,
        location: String,
        condition: String,
        accessories: String,
        notes: String,
        refNum: String,
        confirmation: String,
        photoUri: Uri?
    ) -> Unit
) {
    var ownerName by remember { mutableStateOf("") }
    var ownerUid by remember { mutableStateOf("") }
    var verificationMethod by remember { mutableStateOf("University Photo ID Match") }
    var staffName by remember { mutableStateOf("") }
    var staffId by remember { mutableStateOf("") }
    var location by remember { mutableStateOf(caseItem.location) }
    var condition by remember { mutableStateOf("Good / Verified") }
    var accessories by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var refNum by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("Owner confirmed receipt on site") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedPhotoUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Record Handover to Owner", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Case: ${caseItem.title}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandTealBase
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it; validationError = null },
                    label = { Text("Verified Owner Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = verificationMethod,
                    onValueChange = { verificationMethod = it; validationError = null },
                    label = { Text("Verification Method *") },
                    placeholder = { Text("e.g. Student ID, device pin unlock") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = staffName,
                    onValueChange = { staffName = it; validationError = null },
                    label = { Text("Handing Over Staff Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = ownerUid,
                    onValueChange = { ownerUid = it },
                    label = { Text("Owner UID (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = condition,
                    onValueChange = { condition = it },
                    label = { Text("Item Condition at Handover") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmation,
                    onValueChange = { confirmation = it },
                    label = { Text("Recipient Confirmation / Receipt Note") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Evidence Photo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedPhotoUri != null) "Handover photo attached" else "Optional return photo",
                        fontSize = 11.sp,
                        color = if (selectedPhotoUri != null) Color(0xFF059669) else TextSecondarySlate
                    )

                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (selectedPhotoUri != null) "Change Photo" else "Select Photo", fontSize = 11.sp)
                    }
                }

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(validationError.orEmpty(), color = Color(0xFFDC2626), fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ownerName.trim().isBlank() || verificationMethod.trim().isBlank() || staffName.trim().isBlank()) {
                        validationError = "Owner Name, Verification Method, and Staff Name are mandatory"
                        return@Button
                    }
                    onSubmit(
                        ownerName.trim(),
                        ownerUid.trim(),
                        verificationMethod.trim(),
                        staffName.trim(),
                        staffId.trim(),
                        location.trim(),
                        condition.trim(),
                        accessories.trim(),
                        notes.trim(),
                        refNum.trim(),
                        confirmation.trim(),
                        selectedPhotoUri
                    )
                },
                enabled = !isActionInProgress,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
            ) {
                if (isActionInProgress) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ColorPureWhite)
                } else {
                    Text("Confirm Return to Owner", color = ColorPureWhite)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isActionInProgress) {
                Text("Cancel", color = TextSecondarySlate)
            }
        }
    )
}

@Composable
private fun CustodyTimelineDialog(
    caseItem: PostItem,
    records: List<HandoverRecord>,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Custody & Handover Timeline", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Case: ${caseItem.title} (${caseItem.id.take(8)})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandTealBase
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandTealBase)
                    }
                } else if (records.isEmpty()) {
                    Text(
                        text = "No physical custody events recorded yet for this item.",
                        fontSize = 12.sp,
                        color = TextSecondarySlate
                    )
                } else {
                    records.forEachIndexed { index, record ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Timeline Node
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (record.isReturn) Color(0xFF059669) else BrandTealBase)
                                )
                                if (index < records.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(50.dp)
                                            .background(AppBorderLight)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (record.isReceipt) "Security Receipt: In Custody" else "Returned to Owner",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (record.isReturn) Color(0xFF059669) else BrandTealBase
                                )
                                Text(
                                    text = "Staff: ${record.staffName} | Location: ${record.location}",
                                    fontSize = 11.sp,
                                    color = TextSecondarySlate
                                )
                                if (record.isReturn) {
                                    Text(
                                        text = "Verified Owner: ${record.verifiedOwnerName} (${record.verificationMethod})",
                                        fontSize = 11.sp,
                                        color = TextPrimaryDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (record.evidencePhotoUrl.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    AsyncImage(
                                        model = record.evidencePhotoUrl,
                                        contentDescription = "Handover Photo",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = BrandTealBase)
            }
        }
    )
}

@Composable
private fun RequestOverrideDialog(
    request: PostRequest,
    isActionInProgress: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (newStatus: String, reason: String) -> Unit
) {
    val statuses = listOf(PostRequest.STATUS_ACCEPTED, PostRequest.STATUS_REJECTED, PostRequest.STATUS_CANCELLED)
    var selectedStatus by remember { mutableStateOf(statuses.first()) }
    var reason by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Admin Override Claim", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Requester: ${request.requesterName} | Current: ${request.status}",
                    fontSize = 12.sp,
                    color = TextSecondarySlate
                )

                Spacer(modifier = Modifier.height(10.dp))

                statuses.forEach { st ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStatus = st }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedStatus == st,
                            onClick = { selectedStatus = st },
                            colors = RadioButtonDefaults.colors(selectedColor = BrandTealBase)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = st, fontSize = 12.sp, color = TextPrimaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it; validationError = null },
                    label = { Text("Override Justification *") },
                    placeholder = { Text("e.g. Fraudulent claim verified with security") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(validationError.orEmpty(), color = Color(0xFFDC2626), fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.trim().isBlank()) {
                        validationError = "Reason is mandatory for administrative override"
                        return@Button
                    }
                    onConfirm(selectedStatus, reason.trim())
                },
                enabled = !isActionInProgress,
                colors = ButtonDefaults.buttonColors(containerColor = BrandTealBase)
            ) {
                if (isActionInProgress) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrandMintBase)
                } else {
                    Text("Apply Override", color = BrandMintBase)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isActionInProgress) {
                Text("Cancel", color = TextSecondarySlate)
            }
        }
    )
}

private fun getStatusColor(status: String): Color {
    return when (status.uppercase()) {
        CaseLifecycle.STATUS_OPEN -> Color(0xFF0284C7)
        CaseLifecycle.STATUS_PENDING -> Color(0xFFD97706)
        CaseLifecycle.STATUS_AWAITING_SECURITY_HANDOVER -> Color(0xFFEA580C)
        CaseLifecycle.STATUS_WITH_SECURITY -> BrandTealBase
        CaseLifecycle.STATUS_AWAITING_OWNER_COLLECTION -> Color(0xFF4F46E5)
        CaseLifecycle.STATUS_RETURNED_TO_OWNER -> Color(0xFF059669)
        CaseLifecycle.STATUS_RESOLVED -> Color(0xFF10B981)
        CaseLifecycle.STATUS_CLOSED -> Color(0xFF6B7280)
        else -> BrandTealBase
    }
}
