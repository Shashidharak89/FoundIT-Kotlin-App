package com.shashidhara.foundit.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shashidhara.foundit.data.model.PostRequest
import com.shashidhara.foundit.data.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RequestRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "FoundIT_RequestRepo"
        const val REQUESTS_COLLECTION = "requests"
        const val POSTS_COLLECTION = "posts"
    }

    /**
     * Submits a claim/response request to a post.
     * Uses a deterministic document ID "${postId}_${requester.uid}" to strictly guarantee
     * uniqueness (no duplicate requests) and enforce Firestore security rules.
     */
    suspend fun submitRequest(
        postId: String,
        postOwnerUid: String,
        requester: UserProfile,
        message: String
    ): Result<PostRequest> {
        return try {
            require(requester.uid.isNotBlank()) { "User must be authenticated" }
            require(postOwnerUid.isNotBlank()) { "Post owner UID is required" }
            require(requester.uid != postOwnerUid) { "Users cannot submit requests to their own posts" }
            require(message.isNotBlank()) { "Request message cannot be blank" }

            val requestId = PostRequest.generateId(postId, requester.uid)
            val requestData = hashMapOf<String, Any>(
                "postId" to postId,
                "postOwnerUid" to postOwnerUid,
                "requesterUid" to requester.uid,
                "requesterName" to requester.name.ifBlank { "Community Member" },
                "requesterPhotoUrl" to requester.photoUrl,
                "message" to message.trim(),
                "status" to PostRequest.STATUS_PENDING,
                "createdAt" to FieldValue.serverTimestamp()
            )

            Log.d(TAG, "Submitting request $requestId for post $postId from requester ${requester.uid}")
            firestore.collection(REQUESTS_COLLECTION).document(requestId).set(requestData).await()

            val createdRequest = PostRequest(
                id = requestId,
                postId = postId,
                postOwnerUid = postOwnerUid,
                requesterUid = requester.uid,
                requesterName = requester.name.ifBlank { "Community Member" },
                requesterPhotoUrl = requester.photoUrl,
                message = message.trim(),
                status = PostRequest.STATUS_PENDING
            )

            Result.success(createdRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit request: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Directly checks if the current user has already submitted a request for a post.
     * O(1) single document fetch via deterministic ID.
     */
    suspend fun getRequestForPost(postId: String, requesterUid: String): Result<PostRequest?> {
        return try {
            if (requesterUid.isBlank() || postId.isBlank()) {
                return Result.success(null)
            }
            val requestId = PostRequest.generateId(postId, requesterUid)
            val doc = firestore.collection(REQUESTS_COLLECTION).document(requestId).get().await()
            if (doc.exists()) {
                Result.success(parseRequest(doc))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching request for post $postId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time stream of all requests submitted to posts owned by [ownerUid].
     */
    fun getReceivedRequestsFlow(ownerUid: String): Flow<List<PostRequest>> = callbackFlow {
        if (ownerUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection(REQUESTS_COLLECTION)
            .whereEqualTo("postOwnerUid", ownerUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to received requests: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                val requests = snapshot?.documents?.mapNotNull { parseRequest(it) }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()

                trySend(requests)
            }

        awaitClose { registration.remove() }
    }

    /**
     * Real-time stream of all requests sent by [requesterUid].
     */
    fun getSentRequestsFlow(requesterUid: String): Flow<List<PostRequest>> = callbackFlow {
        if (requesterUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection(REQUESTS_COLLECTION)
            .whereEqualTo("requesterUid", requesterUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to sent requests: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                val requests = snapshot?.documents?.mapNotNull { parseRequest(it) }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()

                trySend(requests)
            }

        awaitClose { registration.remove() }
    }

    /**
     * Real-time stream of requests for a specific post.
     */
    fun getRequestsForPostFlow(postId: String): Flow<List<PostRequest>> = callbackFlow {
        if (postId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection(REQUESTS_COLLECTION)
            .whereEqualTo("postId", postId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to post requests: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                val requests = snapshot?.documents?.mapNotNull { parseRequest(it) }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()

                trySend(requests)
            }

        awaitClose { registration.remove() }
    }

    /**
     * Post Owner accepts or rejects a request.
     * When accepting, updates the request status and conditionally marks the post as PENDING
     * (handover in progress) or RESOLVED.
     */
    suspend fun updateRequestStatus(
        requestId: String,
        newStatus: String,
        postId: String,
        updatePostStatus: String? = null
    ): Result<Unit> {
        return try {
            require(newStatus in listOf(PostRequest.STATUS_ACCEPTED, PostRequest.STATUS_REJECTED)) {
                "Invalid target status: $newStatus"
            }

            // Update Request Status
            val requestUpdates = hashMapOf<String, Any>(
                "status" to newStatus,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection(REQUESTS_COLLECTION).document(requestId).update(requestUpdates).await()

            // Update Post Status if requested (e.g. OPEN -> PENDING on acceptance)
            if (updatePostStatus != null && postId.isNotBlank()) {
                val postUpdates = hashMapOf<String, Any>(
                    "status" to updatePostStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection(POSTS_COLLECTION).document(postId).update(postUpdates).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update request $requestId to $newStatus: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Requester cancels their pending request.
     */
    suspend fun cancelRequest(requestId: String): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "status" to PostRequest.STATUS_CANCELLED,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection(REQUESTS_COLLECTION).document(requestId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel request $requestId: ${e.message}", e)
            Result.failure(e)
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
            Log.w(TAG, "Failed to parse PostRequest ${doc.id}", e)
            null
        }
    }
}
