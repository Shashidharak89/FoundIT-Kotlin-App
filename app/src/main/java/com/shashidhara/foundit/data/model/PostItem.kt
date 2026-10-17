package com.shashidhara.foundit.data.model

import com.google.firebase.Timestamp

/**
 * Represents a community Lost or Found post in Cloud Firestore.
 * Strictly adheres to the 10 allowed fields in Firestore security rules:
 * - ownerUid
 * - type (LOST or FOUND)
 * - title
 * - description
 * - category
 * - location
 * - imageUrl
 * - imagePublicId
 * - status (OPEN)
 * - createdAt
 */
data class PostItem(
    val id: String = "",
    val ownerUid: String = "",
    val type: String = "LOST", // "LOST" or "FOUND"
    val title: String = "",
    val description: String = "",
    val category: String = "Other",
    val location: String = "",
    val imageUrl: String = "",
    val imagePublicId: String = "",
    val status: String = "OPEN",
    val createdAt: Timestamp? = null
) {
    // UI convenience properties
    val isVideo: Boolean
        get() = imageUrl.contains("/video/") ||
                imageUrl.endsWith(".mp4", ignoreCase = true) ||
                imageUrl.endsWith(".mov", ignoreCase = true) ||
                imageUrl.endsWith(".webm", ignoreCase = true) ||
                imageUrl.endsWith(".mkv", ignoreCase = true)

    val displayMediaUrl: String
        get() = imageUrl

    val postType: String
        get() = type

    val userId: String
        get() = ownerUid

    val authorName: String
        get() = if (ownerUid.isNotBlank()) "User #${ownerUid.take(6)}" else "Community Member"
}
