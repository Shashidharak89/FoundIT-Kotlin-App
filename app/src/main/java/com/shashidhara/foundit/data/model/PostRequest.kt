package com.shashidhara.foundit.data.model

import com.google.firebase.Timestamp

/**
 * Represents a claim or response request submitted by a user for a Lost or Found post.
 * Uses a deterministic document ID: "${postId}_${requesterUid}" to guarantee exactly
 * one request per user per post and enforce uniqueness via Firestore security rules.
 */
data class PostRequest(
    val id: String = "",
    val postId: String = "",
    val postOwnerUid: String = "",
    val requesterUid: String = "",
    val requesterName: String = "",
    val requesterPhotoUrl: String = "",
    val message: String = "",
    val status: String = STATUS_PENDING,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_ACCEPTED = "ACCEPTED"
        const val STATUS_REJECTED = "REJECTED"
        const val STATUS_CANCELLED = "CANCELLED"

        fun generateId(postId: String, requesterUid: String): String {
            return "${postId}_${requesterUid}"
        }
    }

    val isPending: Boolean
        get() = status.equals(STATUS_PENDING, ignoreCase = true)

    val isAccepted: Boolean
        get() = status.equals(STATUS_ACCEPTED, ignoreCase = true)

    val isRejected: Boolean
        get() = status.equals(STATUS_REJECTED, ignoreCase = true)

    val isCancelled: Boolean
        get() = status.equals(STATUS_CANCELLED, ignoreCase = true)
}
