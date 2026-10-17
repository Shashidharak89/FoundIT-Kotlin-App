package com.shashidhara.foundit.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.shashidhara.foundit.data.model.AuditLogEntry
import com.shashidhara.foundit.data.model.CaseLifecycle
import com.shashidhara.foundit.data.model.HandoverRecord
import com.shashidhara.foundit.data.model.PostItem
import com.shashidhara.foundit.data.model.PostRequest
import com.shashidhara.foundit.data.model.UserProfile
import com.shashidhara.foundit.data.repository.AdminMetrics
import com.shashidhara.foundit.data.repository.AdminRepository
import com.shashidhara.foundit.data.repository.CloudinaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AdminTab(val title: String) {
    DASHBOARD("Overview"),
    CASES("Cases"),
    REQUESTS("Requests"),
    USERS("Users"),
    AUDIT("Audit Logs")
}

data class AdminUiState(
    val selectedTab: AdminTab = AdminTab.DASHBOARD,

    // Metrics Overview
    val metrics: AdminMetrics? = null,
    val isLoadingMetrics: Boolean = false,
    val metricsError: String? = null,

    // Cases
    val cases: List<PostItem> = emptyList(),
    val isLoadingCases: Boolean = false,
    val isLoadingMoreCases: Boolean = false,
    val hasMoreCases: Boolean = true,
    val casesPage: Int = 1,
    val casesSearchQuery: String = "",
    val casesTypeFilter: String = "ALL",
    val casesStatusFilter: String = "ALL",
    val casesError: String? = null,

    // Requests
    val requests: List<PostRequest> = emptyList(),
    val isLoadingRequests: Boolean = false,
    val isLoadingMoreRequests: Boolean = false,
    val hasMoreRequests: Boolean = true,
    val requestsPage: Int = 1,
    val requestsSearchQuery: String = "",
    val requestsStatusFilter: String = "ALL",
    val requestsError: String? = null,

    // Users
    val users: List<UserProfile> = emptyList(),
    val isLoadingUsers: Boolean = false,
    val isLoadingMoreUsers: Boolean = false,
    val hasMoreUsers: Boolean = true,
    val usersPage: Int = 1,
    val usersSearchQuery: String = "",
    val usersError: String? = null,

    // Audit Logs
    val auditLogs: List<AuditLogEntry> = emptyList(),
    val isLoadingLogs: Boolean = false,
    val isLoadingMoreLogs: Boolean = false,
    val hasMoreLogs: Boolean = true,
    val logsPage: Int = 1,
    val logsError: String? = null,

    // Dialog & Feedback
    val selectedCaseForStatus: PostItem? = null,
    val selectedCaseForReceipt: PostItem? = null,
    val selectedCaseForReturn: PostItem? = null,
    val selectedCaseForTimeline: PostItem? = null,
    val timelineRecords: List<HandoverRecord> = emptyList(),
    val isLoadingTimeline: Boolean = false,
    val selectedRequestForOverride: PostRequest? = null,
    val isActionInProgress: Boolean = false,
    val actionMessage: String? = null,
    val actionError: String? = null
)

class AdminViewModel(
    private val adminRepository: AdminRepository = AdminRepository(),
    private val cloudinaryRepository: CloudinaryRepository = CloudinaryRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    companion object {
        private const val TAG = "FoundIT_AdminVM"
    }

    private var casesLastSnapshot: DocumentSnapshot? = null
    private var requestsLastSnapshot: DocumentSnapshot? = null
    private var usersLastSnapshot: DocumentSnapshot? = null
    private var logsLastSnapshot: DocumentSnapshot? = null

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadMetrics()
        loadCases(reset = true)
    }

    fun selectTab(tab: AdminTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        when (tab) {
            AdminTab.DASHBOARD -> loadMetrics()
            AdminTab.CASES -> if (_uiState.value.cases.isEmpty()) loadCases(reset = true)
            AdminTab.REQUESTS -> if (_uiState.value.requests.isEmpty()) loadRequests(reset = true)
            AdminTab.USERS -> if (_uiState.value.users.isEmpty()) loadUsers(reset = true)
            AdminTab.AUDIT -> if (_uiState.value.auditLogs.isEmpty()) loadAuditLogs(reset = true)
        }
    }

    // ==========================================
    // METRICS
    // ==========================================

    fun loadMetrics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMetrics = true, metricsError = null) }
            adminRepository.getDashboardMetrics()
                .onSuccess { metrics ->
                    _uiState.update { it.copy(metrics = metrics, isLoadingMetrics = false) }
                }
                .onFailure { error ->
                    Log.e(TAG, "Error loading metrics: ${error.message}", error)
                    _uiState.update { it.copy(isLoadingMetrics = false, metricsError = error.localizedMessage ?: "Failed to load metrics") }
                }
        }
    }

    // ==========================================
    // CASES
    // ==========================================

    fun onSearchCases(query: String) {
        _uiState.update { it.copy(casesSearchQuery = query) }
        loadCases(reset = true)
    }

    fun onFilterCasesType(type: String) {
        _uiState.update { it.copy(casesTypeFilter = type) }
        loadCases(reset = true)
    }

    fun onFilterCasesStatus(status: String) {
        _uiState.update { it.copy(casesStatusFilter = status) }
        loadCases(reset = true)
    }

    fun loadCases(reset: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val targetPage = if (reset) 1 else currentState.casesPage + 1
            if (reset) {
                casesLastSnapshot = null
                _uiState.update { it.copy(isLoadingCases = true, casesError = null) }
            } else {
                if (currentState.isLoadingMoreCases || !currentState.hasMoreCases) return@launch
                _uiState.update { it.copy(isLoadingMoreCases = true) }
            }

            adminRepository.getAllCasesPaginated(
                page = targetPage,
                searchQuery = _uiState.value.casesSearchQuery,
                typeFilter = _uiState.value.casesTypeFilter,
                statusFilter = _uiState.value.casesStatusFilter,
                lastSnapshot = casesLastSnapshot
            ).onSuccess { result ->
                casesLastSnapshot = result.lastSnapshot
                _uiState.update { state ->
                    state.copy(
                        cases = if (reset) result.items else state.cases + result.items,
                        casesPage = targetPage,
                        hasMoreCases = result.hasMore,
                        isLoadingCases = false,
                        isLoadingMoreCases = false
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "Error loading cases: ${error.message}", error)
                _uiState.update {
                    it.copy(
                        isLoadingCases = false,
                        isLoadingMoreCases = false,
                        casesError = error.localizedMessage ?: "Failed to load cases"
                    )
                }
            }
        }
    }

    // ==========================================
    // CASE STATUS & HANDOVER ACTIONS
    // ==========================================

    fun selectCaseForStatusChange(caseItem: PostItem?) {
        _uiState.update { it.copy(selectedCaseForStatus = caseItem) }
    }

    fun selectCaseForReceipt(caseItem: PostItem?) {
        _uiState.update { it.copy(selectedCaseForReceipt = caseItem) }
    }

    fun selectCaseForReturn(caseItem: PostItem?) {
        _uiState.update { it.copy(selectedCaseForReturn = caseItem) }
    }

    fun selectCaseForTimeline(caseItem: PostItem?) {
        _uiState.update { it.copy(selectedCaseForTimeline = caseItem, timelineRecords = emptyList()) }
        if (caseItem != null) {
            loadTimelineForCase(caseItem.id)
        }
    }

    private fun loadTimelineForCase(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTimeline = true) }
            adminRepository.getHandoverTimelineForPost(postId).collect { records ->
                _uiState.update { it.copy(timelineRecords = records, isLoadingTimeline = false) }
            }
        }
    }

    fun updateCaseStatus(postId: String, previousStatus: String, newStatus: String, reason: String) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, actionError = null) }
            adminRepository.updateCaseStatus(
                postId = postId,
                previousStatus = previousStatus,
                newStatus = newStatus,
                reason = reason,
                adminUid = currentUser.uid,
                adminName = currentUser.displayName ?: "Admin"
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        selectedCaseForStatus = null,
                        actionMessage = "Status successfully updated to ${CaseLifecycle.getStatusLabel(newStatus)}"
                    )
                }
                loadCases(reset = true)
                loadMetrics()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        actionError = error.localizedMessage ?: "Failed to update status"
                    )
                }
            }
        }
    }

    fun submitSecurityReceipt(
        postId: String,
        itemTitle: String,
        location: String,
        staffName: String,
        staffId: String,
        itemCondition: String,
        accessories: String,
        notes: String,
        referenceNumber: String,
        evidenceUri: Uri?,
        context: Context?
    ) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, actionError = null) }

            var photoUrl = ""
            var photoPublicId = ""

            if (evidenceUri != null && context != null) {
                val uploadResult = cloudinaryRepository.uploadMedia(context, evidenceUri, isVideo = false)
                if (uploadResult.isSuccess) {
                    val media = uploadResult.getOrNull()
                    photoUrl = media?.secureUrl.orEmpty()
                    photoPublicId = media?.publicId.orEmpty()
                }
            }

            val handover = HandoverRecord(
                postId = postId,
                itemTitle = itemTitle,
                type = HandoverRecord.TYPE_RECEIPT,
                location = location,
                staffName = staffName,
                staffId = staffId,
                itemCondition = itemCondition,
                accessories = accessories,
                notes = notes,
                referenceNumber = referenceNumber,
                evidencePhotoUrl = photoUrl,
                evidencePhotoPublicId = photoPublicId,
                actorUid = currentUser.uid
            )

            adminRepository.recordSecurityReceipt(
                handover = handover,
                adminUid = currentUser.uid,
                adminName = currentUser.displayName ?: "Admin"
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        selectedCaseForReceipt = null,
                        actionMessage = "Security receipt recorded into custody"
                    )
                }
                loadCases(reset = true)
                loadMetrics()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        actionError = error.localizedMessage ?: "Failed to record security receipt"
                    )
                }
            }
        }
    }

    fun submitOwnerReturn(
        postId: String,
        itemTitle: String,
        location: String,
        staffName: String,
        staffId: String,
        itemCondition: String,
        accessories: String,
        notes: String,
        referenceNumber: String,
        verifiedOwnerName: String,
        verifiedOwnerUid: String,
        verificationMethod: String,
        recipientConfirmation: String,
        evidenceUri: Uri?,
        context: Context?
    ) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, actionError = null) }

            var photoUrl = ""
            var photoPublicId = ""

            if (evidenceUri != null && context != null) {
                val uploadResult = cloudinaryRepository.uploadMedia(context, evidenceUri, isVideo = false)
                if (uploadResult.isSuccess) {
                    val media = uploadResult.getOrNull()
                    photoUrl = media?.secureUrl.orEmpty()
                    photoPublicId = media?.publicId.orEmpty()
                }
            }

            val handover = HandoverRecord(
                postId = postId,
                itemTitle = itemTitle,
                type = HandoverRecord.TYPE_RETURN,
                location = location,
                staffName = staffName,
                staffId = staffId,
                itemCondition = itemCondition,
                accessories = accessories,
                notes = notes,
                referenceNumber = referenceNumber,
                evidencePhotoUrl = photoUrl,
                evidencePhotoPublicId = photoPublicId,
                verifiedOwnerName = verifiedOwnerName,
                verifiedOwnerUid = verifiedOwnerUid,
                verificationMethod = verificationMethod,
                recipientConfirmation = recipientConfirmation,
                actorUid = currentUser.uid
            )

            adminRepository.recordOwnerReturn(
                handover = handover,
                adminUid = currentUser.uid,
                adminName = currentUser.displayName ?: "Admin"
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        selectedCaseForReturn = null,
                        actionMessage = "Item handover returned to owner recorded"
                    )
                }
                loadCases(reset = true)
                loadMetrics()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        actionError = error.localizedMessage ?: "Failed to record owner return"
                    )
                }
            }
        }
    }

    // ==========================================
    // REQUESTS
    // ==========================================

    fun onSearchRequests(query: String) {
        _uiState.update { it.copy(requestsSearchQuery = query) }
        loadRequests(reset = true)
    }

    fun onFilterRequestsStatus(status: String) {
        _uiState.update { it.copy(requestsStatusFilter = status) }
        loadRequests(reset = true)
    }

    fun loadRequests(reset: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val targetPage = if (reset) 1 else currentState.requestsPage + 1
            if (reset) {
                requestsLastSnapshot = null
                _uiState.update { it.copy(isLoadingRequests = true, requestsError = null) }
            } else {
                if (currentState.isLoadingMoreRequests || !currentState.hasMoreRequests) return@launch
                _uiState.update { it.copy(isLoadingMoreRequests = true) }
            }

            adminRepository.getAllRequestsPaginated(
                page = targetPage,
                statusFilter = _uiState.value.requestsStatusFilter,
                searchQuery = _uiState.value.requestsSearchQuery,
                lastSnapshot = requestsLastSnapshot
            ).onSuccess { result ->
                requestsLastSnapshot = result.lastSnapshot
                _uiState.update { state ->
                    state.copy(
                        requests = if (reset) result.items else state.requests + result.items,
                        requestsPage = targetPage,
                        hasMoreRequests = result.hasMore,
                        isLoadingRequests = false,
                        isLoadingMoreRequests = false
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "Error loading requests: ${error.message}", error)
                _uiState.update {
                    it.copy(
                        isLoadingRequests = false,
                        isLoadingMoreRequests = false,
                        requestsError = error.localizedMessage ?: "Failed to load requests"
                    )
                }
            }
        }
    }

    fun selectRequestForOverride(request: PostRequest?) {
        _uiState.update { it.copy(selectedRequestForOverride = request) }
    }

    fun submitRequestOverride(
        requestId: String,
        postId: String,
        previousStatus: String,
        newStatus: String,
        reason: String
    ) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, actionError = null) }
            adminRepository.adminOverrideRequest(
                requestId = requestId,
                postId = postId,
                previousStatus = previousStatus,
                newStatus = newStatus,
                reason = reason,
                adminUid = currentUser.uid,
                adminName = currentUser.displayName ?: "Admin"
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        selectedRequestForOverride = null,
                        actionMessage = "Request status overridden to $newStatus"
                    )
                }
                loadRequests(reset = true)
                loadMetrics()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        actionError = error.localizedMessage ?: "Failed to override request"
                    )
                }
            }
        }
    }

    // ==========================================
    // USERS
    // ==========================================

    fun onSearchUsers(query: String) {
        _uiState.update { it.copy(usersSearchQuery = query) }
        loadUsers(reset = true)
    }

    fun loadUsers(reset: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val targetPage = if (reset) 1 else currentState.usersPage + 1
            if (reset) {
                usersLastSnapshot = null
                _uiState.update { it.copy(isLoadingUsers = true, usersError = null) }
            } else {
                if (currentState.isLoadingMoreUsers || !currentState.hasMoreUsers) return@launch
                _uiState.update { it.copy(isLoadingMoreUsers = true) }
            }

            adminRepository.getAllUsersPaginated(
                page = targetPage,
                searchQuery = _uiState.value.usersSearchQuery,
                lastSnapshot = usersLastSnapshot
            ).onSuccess { result ->
                usersLastSnapshot = result.lastSnapshot
                _uiState.update { state ->
                    state.copy(
                        users = if (reset) result.items else state.users + result.items,
                        usersPage = targetPage,
                        hasMoreUsers = result.hasMore,
                        isLoadingUsers = false,
                        isLoadingMoreUsers = false
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "Error loading users: ${error.message}", error)
                _uiState.update {
                    it.copy(
                        isLoadingUsers = false,
                        isLoadingMoreUsers = false,
                        usersError = error.localizedMessage ?: "Failed to load users"
                    )
                }
            }
        }
    }

    // ==========================================
    // AUDIT LOGS
    // ==========================================

    fun loadAuditLogs(reset: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val targetPage = if (reset) 1 else currentState.logsPage + 1
            if (reset) {
                logsLastSnapshot = null
                _uiState.update { it.copy(isLoadingLogs = true, logsError = null) }
            } else {
                if (currentState.isLoadingMoreLogs || !currentState.hasMoreLogs) return@launch
                _uiState.update { it.copy(isLoadingMoreLogs = true) }
            }

            adminRepository.getAuditLogsPaginated(
                page = targetPage,
                lastSnapshot = logsLastSnapshot
            ).onSuccess { result ->
                logsLastSnapshot = result.lastSnapshot
                _uiState.update { state ->
                    state.copy(
                        auditLogs = if (reset) result.items else state.auditLogs + result.items,
                        logsPage = targetPage,
                        hasMoreLogs = result.hasMore,
                        isLoadingLogs = false,
                        isLoadingMoreLogs = false
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "Error loading audit logs: ${error.message}", error)
                _uiState.update {
                    it.copy(
                        isLoadingLogs = false,
                        isLoadingMoreLogs = false,
                        logsError = error.localizedMessage ?: "Failed to load audit logs"
                    )
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(actionMessage = null, actionError = null) }
    }
}
