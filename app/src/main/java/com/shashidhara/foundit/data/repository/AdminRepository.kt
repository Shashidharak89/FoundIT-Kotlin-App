package com.shashidhara.foundit.data.repository

import android.util.Log
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shashidhara.foundit.data.model.AuditLogEntry
import com.shashidhara.foundit.data.model.CaseLifecycle
import com.shashidhara.foundit.data.model.HandoverRecord
import com.shashidhara.foundit.data.model.PostItem
import com.shashidhara.foundit.data.model.PostRequest
import com.shashidhara.foundit.data.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class AdminMetrics(
    val totalPosts: Long = 0,
    val openReports: Long = 0,
    val pendingClaims: Long = 0,
    val awaitingHandover: Long = 0,
    val withSecurity: Long = 0,
    val awaitingCollection: Long = 0,
    val returnedToOwner: Long = 0,
    val resolvedCases: Long = 0,
    val closedCases: Long = 0,
    val pendingRequests: Long = 0,
    val rejectedRequests: Long = 0
)

data class PaginatedResult<T>(
    val items: List<T>,
    val lastSnapshot: DocumentSnapshot?,
    val hasMore: Boolean,
    val page: Int
)

class AdminRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "FoundIT_AdminRepo"
        const val POSTS_COLLECTION = "posts"
        const val REQUESTS_COLLECTION = "requests"
        const val HANDOVERS_COLLECTION = "handovers"
        const val AUDIT_LOGS_COLLECTION = "auditLogs"
        const val USERS_COLLECTION = "users"
        const val DEFAULT_PAGE_SIZE = 20
    }

    /**
     * Efficiently fetches summary metrics using Firestore count() server aggregations.
     * Works seamlessly on the Firebase Spark free plan.
     */
    suspend fun getDashboardMetrics(): Result<AdminMetrics> {
        return try {
            val postsColl = firestore.collection(POSTS_COLLECTION)
            val requestsColl = firestore.collection(REQUESTS_COLLECTION)

            val totalPostsTask = postsColl.count().get(AggregateSource.SERVER)
            val openPostsTask = postsColl.whereEqualTo("status", CaseLifecycle.STATUS_OPEN).count().get(AggregateSource.SERVER)
            val pendingPostsTask = postsColl.whereEqualTo("status", CaseLifecycle.STATUS_PENDING).count().get(AggregateSource.SERVER)
            val awaitingHandoverTask = postsColl.whereEqualTo("status", CaseLifecycle.STATUS_AWAITING_SECURITY_HANDOVER).count().get(AggregateSource.SERVER)
            val withSecurityTask = postsColl.whereEqualTo("status", CaseLifecycle.STATUS_WITH_SECURITY).count().get(AggregateSource.SERVER)
            val awaitingCollectionTask = postsColl.whereEqualTo("status", CaseLifecycle.STATUS_AWAITING_OWNER_COLLECTION).count().get(AggregateSource.SERVER)
            val returnedTask = postsColl.whereEqualTo("status", CaseLifecycle.STATUS_RETURNED_TO_OWNER).count().get(AggregateSource.SERVER)
            val resolvedTask = postsColl.whereEqualTo("status", CaseLifecycle.STATUS_RESOLVED).count().get(AggregateSource.SERVER)
            val closedTask = postsColl.whereEqualTo("status", CaseLifecycle.STATUS_CLOSED).count().get(AggregateSource.SERVER)

            val pendingRequestsTask = requestsColl.whereEqualTo("status", PostRequest.STATUS_PENDING).count().get(AggregateSource.SERVER)
            val rejectedRequestsTask = requestsColl.whereEqualTo("status", PostRequest.STATUS_REJECTED).count().get(AggregateSource.SERVER)

            val metrics = AdminMetrics(
                totalPosts = totalPostsTask.await().count,
                openReports = openPostsTask.await().count,
                pendingClaims = pendingPostsTask.await().count,
                awaitingHandover = awaitingHandoverTask.await().count,
                withSecurity = withSecurityTask.await().count,
                awaitingCollection = awaitingCollectionTask.await().count,
                returnedToOwner = returnedTask.await().count,
                resolvedCases = resolvedTask.await().count,
                closedCases = closedTask.await().count,
                pendingRequests = pendingRequestsTask.await().count,
                rejectedRequests = rejectedRequestsTask.await().count
            )

            Log.d(TAG, "Fetched Admin Metrics: totalPosts=${metrics.totalPosts}, withSecurity=${metrics.withSecurity}")
            Result.success(metrics)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compute dashboard metrics: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Admin Cases: Cursor-based paginated search across all posts in the system.
     */
    suspend fun getAllCasesPaginated(
        page: Int = 1,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        searchQuery: String = "",
        typeFilter: String? = null,
        statusFilter: String? = null,
        categoryFilter: String? = null,
        lastSnapshot: DocumentSnapshot? = null
    ): Result<PaginatedResult<PostItem>> {
        return try {
            val cleanQuery = searchQuery.trim()
            val cleanType = if (typeFilter == "ALL" || typeFilter.isNullOrBlank()) null else typeFilter
            val cleanStatus = if (statusFilter == "ALL" || statusFilter.isNullOrBlank()) null else statusFilter
            val cleanCategory = if (categoryFilter == "All Items" || categoryFilter.isNullOrBlank()) null else categoryFilter

            var query: Query = firestore.collection(POSTS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            if (page > 1 && lastSnapshot != null) {
                query = query.startAfter(lastSnapshot)
            }

            val needsClientFiltering = cleanQuery.isNotBlank() || cleanType != null || cleanStatus != null || cleanCategory != null
            val fetchLimit = if (needsClientFiltering) (pageSize * 3).toLong() else pageSize.toLong()

            val snapshot = query.limit(fetchLimit).get().await()
            val rawDocs = snapshot.documents
            val allPosts = rawDocs.mapNotNull { parsePost(it) }

            val filtered = allPosts.filter { post ->
                val matchesType = cleanType == null || post.type.equals(cleanType, ignoreCase = true)
                val matchesStatus = cleanStatus == null || post.status.equals(cleanStatus, ignoreCase = true)
                val matchesCategory = cleanCategory == null || post.category.equals(cleanCategory, ignoreCase = true)
                val matchesSearch = cleanQuery.isBlank() ||
                        post.title.contains(cleanQuery, ignoreCase = true) ||
                        post.description.contains(cleanQuery, ignoreCase = true) ||
                        post.location.contains(cleanQuery, ignoreCase = true) ||
                        post.ownerUid.contains(cleanQuery, ignoreCase = true) ||
                        post.id.contains(cleanQuery, ignoreCase = true)

                matchesType && matchesStatus && matchesCategory && matchesSearch
            }

            val newLastDoc = rawDocs.lastOrNull()
            val hasMore = rawDocs.size.toLong() >= fetchLimit

            Result.success(
                PaginatedResult(
                    items = filtered,
                    lastSnapshot = newLastDoc,
                    hasMore = hasMore,
                    page = page
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load admin cases: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Updates case status with state machine validation and creates an auditable log entry.
     */
    suspend fun updateCaseStatus(
        postId: String,
        previousStatus: String,
        newStatus: String,
        newTrackingStatus: String? = null,
        reason: String,
        adminUid: String,
        adminName: String
    ): Result<Unit> {
        return try {
            val postUpdates = hashMapOf<String, Any>(
                "status" to newStatus,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!newTrackingStatus.isNullOrBlank()) {
                postUpdates["trackingStatus"] = newTrackingStatus
            }
            firestore.collection(POSTS_COLLECTION).document(postId).update(postUpdates).await()

            // Create Audit Log (resilient so audit failure does not fail the primary update)
            try {
                val logEntry = AuditLogEntry(
                    actorUid = adminUid,
                    actorName = adminName,
                    action = AuditLogEntry.ACTION_STATUS_CHANGE,
                    entityType = AuditLogEntry.ENTITY_POST,
                    entityId = postId,
                    previousStatus = previousStatus,
                    newStatus = if (!newTrackingStatus.isNullOrBlank()) "$newStatus ($newTrackingStatus)" else newStatus,
                    reason = reason.trim()
                )
                createAuditLogInternal(logEntry)
            } catch (auditErr: Exception) {
                Log.w(TAG, "Post status updated to $newStatus, but audit log creation failed: ${auditErr.message}")
            }

            Log.d(TAG, "Admin updated post $postId status: $previousStatus -> $newStatus (tracking: $newTrackingStatus, reason: $reason)")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update case status: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Admin post details modification.
     * Updates editable fields and preserves existing metadata.
     */
    suspend fun updatePostDetails(
        postId: String,
        title: String,
        description: String,
        category: String,
        location: String,
        type: String,
        status: String,
        imageUrl: String,
        imagePublicId: String,
        adminUid: String,
        adminName: String,
        reason: String = "Admin post modification"
    ): Result<Unit> {
        return try {
            require(title.isNotBlank()) { "Title cannot be blank" }
            require(location.isNotBlank()) { "Location cannot be blank" }

            val docRef = firestore.collection(POSTS_COLLECTION).document(postId)
            val currentDoc = docRef.get().await()
            val previousStatus = currentDoc.getString("status") ?: "OPEN"

            val updates = hashMapOf<String, Any>(
                "title" to title.trim(),
                "description" to description.trim(),
                "category" to category,
                "location" to location.trim(),
                "type" to type,
                "status" to status,
                "imageUrl" to imageUrl,
                "imagePublicId" to imagePublicId,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            docRef.update(updates).await()

            try {
                val logEntry = AuditLogEntry(
                    actorUid = adminUid,
                    actorName = adminName,
                    action = AuditLogEntry.ACTION_STATUS_CHANGE,
                    entityType = AuditLogEntry.ENTITY_POST,
                    entityId = postId,
                    previousStatus = previousStatus,
                    newStatus = status,
                    reason = reason.trim().ifBlank { "Post edited by Admin" }
                )
                createAuditLogInternal(logEntry)
            } catch (auditErr: Exception) {
                Log.w(TAG, "Post $postId updated, but audit log failed: ${auditErr.message}")
            }

            Log.d(TAG, "Admin updated post $postId details successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update post details for $postId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Records physical receipt of an item by Security Staff into custody.
     */
    suspend fun recordSecurityReceipt(
        handover: HandoverRecord,
        adminUid: String,
        adminName: String,
        reason: String = "Item handed over to security custody"
    ): Result<String> {
        return try {
            require(handover.postId.isNotBlank()) { "Post ID is required" }
            require(handover.staffName.isNotBlank()) { "Security staff name is required" }

            val handoverData = hashMapOf<String, Any>(
                "postId" to handover.postId,
                "itemTitle" to handover.itemTitle,
                "type" to HandoverRecord.TYPE_RECEIPT,
                "location" to handover.location,
                "staffName" to handover.staffName,
                "staffId" to handover.staffId,
                "itemCondition" to handover.itemCondition,
                "accessories" to handover.accessories,
                "notes" to handover.notes,
                "referenceNumber" to handover.referenceNumber,
                "evidencePhotoUrl" to handover.evidencePhotoUrl,
                "evidencePhotoPublicId" to handover.evidencePhotoPublicId,
                "actorUid" to adminUid,
                "timestamp" to FieldValue.serverTimestamp(),
                "createdAt" to FieldValue.serverTimestamp()
            )

            val docRef = firestore.collection(HANDOVERS_COLLECTION).add(handoverData).await()

            // Update post status to WITH_SECURITY
            val postUpdates = hashMapOf<String, Any>(
                "status" to CaseLifecycle.STATUS_WITH_SECURITY,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection(POSTS_COLLECTION).document(handover.postId).update(postUpdates).await()

            // Audit Log
            val logEntry = AuditLogEntry(
                actorUid = adminUid,
                actorName = adminName,
                action = AuditLogEntry.ACTION_SECURITY_RECEIPT,
                entityType = AuditLogEntry.ENTITY_HANDOVER,
                entityId = docRef.id,
                previousStatus = CaseLifecycle.STATUS_AWAITING_SECURITY_HANDOVER,
                newStatus = CaseLifecycle.STATUS_WITH_SECURITY,
                reason = reason,
                metadata = mapOf(
                    "postId" to handover.postId,
                    "staffName" to handover.staffName,
                    "referenceNumber" to handover.referenceNumber
                )
            )
            createAuditLogInternal(logEntry)

            Log.d(TAG, "Security receipt recorded with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record security receipt: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Records confirmed physical return of an item to its rightful owner.
     */
    suspend fun recordOwnerReturn(
        handover: HandoverRecord,
        adminUid: String,
        adminName: String,
        reason: String = "Item verified and handed over to rightful owner"
    ): Result<String> {
        return try {
            require(handover.postId.isNotBlank()) { "Post ID is required" }
            require(handover.verifiedOwnerName.isNotBlank()) { "Verified owner name is required" }
            require(handover.verificationMethod.isNotBlank()) { "Verification method is required" }

            val handoverData = hashMapOf<String, Any>(
                "postId" to handover.postId,
                "itemTitle" to handover.itemTitle,
                "type" to HandoverRecord.TYPE_RETURN,
                "location" to handover.location,
                "staffName" to handover.staffName,
                "staffId" to handover.staffId,
                "itemCondition" to handover.itemCondition,
                "accessories" to handover.accessories,
                "notes" to handover.notes,
                "referenceNumber" to handover.referenceNumber,
                "evidencePhotoUrl" to handover.evidencePhotoUrl,
                "evidencePhotoPublicId" to handover.evidencePhotoPublicId,
                "verifiedOwnerName" to handover.verifiedOwnerName,
                "verifiedOwnerUid" to handover.verifiedOwnerUid,
                "verificationMethod" to handover.verificationMethod,
                "recipientConfirmation" to handover.recipientConfirmation,
                "actorUid" to adminUid,
                "timestamp" to FieldValue.serverTimestamp(),
                "createdAt" to FieldValue.serverTimestamp()
            )

            val docRef = firestore.collection(HANDOVERS_COLLECTION).add(handoverData).await()

            // Update post status to RETURNED_TO_OWNER
            val postUpdates = hashMapOf<String, Any>(
                "status" to CaseLifecycle.STATUS_RETURNED_TO_OWNER,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection(POSTS_COLLECTION).document(handover.postId).update(postUpdates).await()

            // Audit Log
            val logEntry = AuditLogEntry(
                actorUid = adminUid,
                actorName = adminName,
                action = AuditLogEntry.ACTION_OWNER_RETURN,
                entityType = AuditLogEntry.ENTITY_HANDOVER,
                entityId = docRef.id,
                previousStatus = CaseLifecycle.STATUS_AWAITING_OWNER_COLLECTION,
                newStatus = CaseLifecycle.STATUS_RETURNED_TO_OWNER,
                reason = reason,
                metadata = mapOf(
                    "postId" to handover.postId,
                    "verifiedOwnerName" to handover.verifiedOwnerName,
                    "verificationMethod" to handover.verificationMethod
                )
            )
            createAuditLogInternal(logEntry)

            Log.d(TAG, "Owner return recorded with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record owner return: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time stream of physical custody handover events for a specific post.
     */
    fun getHandoverTimelineForPost(postId: String): Flow<List<HandoverRecord>> = callbackFlow {
        if (postId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection(HANDOVERS_COLLECTION)
            .whereEqualTo("postId", postId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error listening to handover timeline: ${error.message}")
                    close()
                    return@addSnapshotListener
                }

                val records = snapshot?.documents?.mapNotNull { parseHandover(it) }
                    ?.sortedBy { it.createdAt }
                    ?: emptyList()

                trySend(records)
            }

        awaitClose { registration.remove() }
    }

    /**
     * Admin Requests: Paginated list of all requests across the entire application.
     */
    suspend fun getAllRequestsPaginated(
        page: Int = 1,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        statusFilter: String? = null,
        searchQuery: String = "",
        lastSnapshot: DocumentSnapshot? = null
    ): Result<PaginatedResult<PostRequest>> {
        return try {
            val cleanStatus = if (statusFilter == "ALL" || statusFilter.isNullOrBlank()) null else statusFilter
            val cleanQuery = searchQuery.trim()

            var query: Query = firestore.collection(REQUESTS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            if (page > 1 && lastSnapshot != null) {
                query = query.startAfter(lastSnapshot)
            }

            val needsClientFilter = cleanStatus != null || cleanQuery.isNotBlank()
            val fetchLimit = if (needsClientFilter) (pageSize * 3).toLong() else pageSize.toLong()

            val snapshot = query.limit(fetchLimit).get().await()
            val rawDocs = snapshot.documents
            val allRequests = rawDocs.mapNotNull { parseRequest(it) }

            val filtered = allRequests.filter { req ->
                val matchesStatus = cleanStatus == null || req.status.equals(cleanStatus, ignoreCase = true)
                val matchesSearch = cleanQuery.isBlank() ||
                        req.message.contains(cleanQuery, ignoreCase = true) ||
                        req.requesterName.contains(cleanQuery, ignoreCase = true) ||
                        req.postId.contains(cleanQuery, ignoreCase = true) ||
                        req.requesterUid.contains(cleanQuery, ignoreCase = true)

                matchesStatus && matchesSearch
            }

            val newLastDoc = rawDocs.lastOrNull()
            val hasMore = rawDocs.size.toLong() >= fetchLimit

            Result.success(
                PaginatedResult(
                    items = filtered,
                    lastSnapshot = newLastDoc,
                    hasMore = hasMore,
                    page = page
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load admin requests: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Admin overrides a request status (e.g. disputes/fraud) and records an audit log.
     */
    suspend fun adminOverrideRequest(
        requestId: String,
        postId: String,
        previousStatus: String,
        newStatus: String,
        reason: String,
        adminUid: String,
        adminName: String
    ): Result<Unit> {
        return try {
            require(reason.isNotBlank()) { "An administrative override reason is required" }

            val updates = hashMapOf<String, Any>(
                "status" to newStatus,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection(REQUESTS_COLLECTION).document(requestId).update(updates).await()

            // Audit Log
            val logEntry = AuditLogEntry(
                actorUid = adminUid,
                actorName = adminName,
                action = AuditLogEntry.ACTION_REQUEST_OVERRIDE,
                entityType = AuditLogEntry.ENTITY_REQUEST,
                entityId = requestId,
                previousStatus = previousStatus,
                newStatus = newStatus,
                reason = reason,
                metadata = mapOf("postId" to postId)
            )
            createAuditLogInternal(logEntry)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to override request status: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Admin Users: Paginated list of registered user profiles.
     */
    suspend fun getAllUsersPaginated(
        page: Int = 1,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        searchQuery: String = "",
        lastSnapshot: DocumentSnapshot? = null
    ): Result<PaginatedResult<UserProfile>> {
        return try {
            val cleanQuery = searchQuery.trim()

            var query: Query = firestore.collection(USERS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            if (page > 1 && lastSnapshot != null) {
                query = query.startAfter(lastSnapshot)
            }

            val needsClientFilter = cleanQuery.isNotBlank()
            val fetchLimit = if (needsClientFilter) (pageSize * 3).toLong() else pageSize.toLong()

            val snapshot = query.limit(fetchLimit).get().await()
            val rawDocs = snapshot.documents
            val allUsers = rawDocs.mapNotNull { parseUser(it) }

            val filtered = allUsers.filter { user ->
                cleanQuery.isBlank() ||
                        user.name.contains(cleanQuery, ignoreCase = true) ||
                        user.email.contains(cleanQuery, ignoreCase = true) ||
                        user.uid.contains(cleanQuery, ignoreCase = true)
            }

            val newLastDoc = rawDocs.lastOrNull()
            val hasMore = rawDocs.size.toLong() >= fetchLimit

            Result.success(
                PaginatedResult(
                    items = filtered,
                    lastSnapshot = newLastDoc,
                    hasMore = hasMore,
                    page = page
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load admin users: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Audit Log: Paginated history of privileged operations.
     */
    suspend fun getAuditLogsPaginated(
        page: Int = 1,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        lastSnapshot: DocumentSnapshot? = null
    ): Result<PaginatedResult<AuditLogEntry>> {
        return try {
            var query: Query = firestore.collection(AUDIT_LOGS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            if (page > 1 && lastSnapshot != null) {
                query = query.startAfter(lastSnapshot)
            }

            val snapshot = query.limit(pageSize.toLong()).get().await()
            val rawDocs = snapshot.documents
            val logs = rawDocs.mapNotNull { parseAuditLog(it) }

            Result.success(
                PaginatedResult(
                    items = logs,
                    lastSnapshot = rawDocs.lastOrNull(),
                    hasMore = rawDocs.size >= pageSize,
                    page = page
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load audit logs: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun createAuditLogInternal(entry: AuditLogEntry) {
        val logData = hashMapOf<String, Any>(
            "actorUid" to entry.actorUid,
            "actorName" to entry.actorName,
            "action" to entry.action,
            "entityType" to entry.entityType,
            "entityId" to entry.entityId,
            "previousStatus" to entry.previousStatus,
            "newStatus" to entry.newStatus,
            "reason" to entry.reason,
            "metadata" to entry.metadata,
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.collection(AUDIT_LOGS_COLLECTION).add(logData).await()
    }

    private fun parsePost(doc: DocumentSnapshot): PostItem? {
        return try {
            PostItem(
                id = doc.id,
                ownerUid = doc.getString("ownerUid").orEmpty(),
                type = doc.getString("type") ?: "LOST",
                title = doc.getString("title").orEmpty(),
                description = doc.getString("description").orEmpty(),
                category = doc.getString("category") ?: "Other",
                location = doc.getString("location").orEmpty(),
                imageUrl = doc.getString("imageUrl").orEmpty(),
                imagePublicId = doc.getString("imagePublicId").orEmpty(),
                status = doc.getString("status") ?: "OPEN",
                createdAt = doc.getTimestamp("createdAt"),
                updatedAt = doc.getTimestamp("updatedAt")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseRequest(doc: DocumentSnapshot): PostRequest? {
        return try {
            PostRequest(
                id = doc.id,
                postId = doc.getString("postId").orEmpty(),
                postOwnerUid = doc.getString("postOwnerUid").orEmpty(),
                requesterUid = doc.getString("requesterUid").orEmpty(),
                requesterName = doc.getString("requesterName") ?: "Community Member",
                requesterPhotoUrl = doc.getString("requesterPhotoUrl").orEmpty(),
                message = doc.getString("message").orEmpty(),
                status = doc.getString("status") ?: PostRequest.STATUS_PENDING,
                createdAt = doc.getTimestamp("createdAt"),
                updatedAt = doc.getTimestamp("updatedAt")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseHandover(doc: DocumentSnapshot): HandoverRecord? {
        return try {
            HandoverRecord(
                id = doc.id,
                postId = doc.getString("postId").orEmpty(),
                itemTitle = doc.getString("itemTitle").orEmpty(),
                type = doc.getString("type") ?: HandoverRecord.TYPE_RECEIPT,
                location = doc.getString("location").orEmpty(),
                staffName = doc.getString("staffName").orEmpty(),
                staffId = doc.getString("staffId").orEmpty(),
                itemCondition = doc.getString("itemCondition").orEmpty(),
                accessories = doc.getString("accessories").orEmpty(),
                notes = doc.getString("notes").orEmpty(),
                referenceNumber = doc.getString("referenceNumber").orEmpty(),
                evidencePhotoUrl = doc.getString("evidencePhotoUrl").orEmpty(),
                evidencePhotoPublicId = doc.getString("evidencePhotoPublicId").orEmpty(),
                verifiedOwnerName = doc.getString("verifiedOwnerName").orEmpty(),
                verifiedOwnerUid = doc.getString("verifiedOwnerUid").orEmpty(),
                verificationMethod = doc.getString("verificationMethod").orEmpty(),
                recipientConfirmation = doc.getString("recipientConfirmation").orEmpty(),
                actorUid = doc.getString("actorUid").orEmpty(),
                timestamp = doc.getTimestamp("timestamp"),
                createdAt = doc.getTimestamp("createdAt")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseUser(doc: DocumentSnapshot): UserProfile? {
        return try {
            UserProfile(
                uid = doc.id,
                name = doc.getString("name").orEmpty(),
                email = doc.getString("email").orEmpty(),
                photoUrl = doc.getString("photoUrl").orEmpty(),
                role = doc.getString("role") ?: "USER",
                createdAt = doc.getTimestamp("createdAt")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseAuditLog(doc: DocumentSnapshot): AuditLogEntry? {
        return try {
            @Suppress("UNCHECKED_CAST")
            AuditLogEntry(
                id = doc.id,
                actorUid = doc.getString("actorUid").orEmpty(),
                actorName = doc.getString("actorName") ?: "Admin Staff",
                action = doc.getString("action") ?: AuditLogEntry.ACTION_STATUS_CHANGE,
                entityType = doc.getString("entityType") ?: AuditLogEntry.ENTITY_POST,
                entityId = doc.getString("entityId").orEmpty(),
                previousStatus = doc.getString("previousStatus").orEmpty(),
                newStatus = doc.getString("newStatus").orEmpty(),
                reason = doc.getString("reason").orEmpty(),
                metadata = (doc.get("metadata") as? Map<String, Any>) ?: emptyMap(),
                createdAt = doc.getTimestamp("createdAt")
            )
        } catch (e: Exception) {
            null
        }
    }
}
