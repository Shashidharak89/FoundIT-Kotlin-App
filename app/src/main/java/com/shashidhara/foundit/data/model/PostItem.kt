package com.shashidhara.foundit.data.model

import com.google.firebase.Timestamp

/**
 * Represents a community Lost or Found post stored in Cloud Firestore.
 * Keeps imageUrl and imagePublicId for full backward compatibility,
 * while supporting videoUrl and mediaUrl for image/video media from Cloudinary.
 */
data class PostItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Other",
    val location: String = "",
    val postType: String = "LOST", // "LOST" or "FOUND"
    val userId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val imageUrl: String = "",       // Kept compatible with legacy rules
    val imagePublicId: String = "",  // Kept compatible with legacy rules
    val videoUrl: String = "",       // Populated when resourceType == "video"
    val mediaUrl: String = "",       // Canonical media secure_url from Cloudinary
    val mediaPublicId: String = "",  // Canonical media public_id from Cloudinary
    val resourceType: String = "image", // "image" or "video"
    val createdAt: Timestamp? = null
) {
    val isVideo: Boolean
        get() = resourceType == "video"

    val displayMediaUrl: String
        get() = mediaUrl.ifBlank { if (isVideo) videoUrl else imageUrl }
}
