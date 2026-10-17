package com.shashidhara.foundit.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shashidhara.foundit.data.model.PostItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PostRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "FoundIT_PostRepo"
        const val POSTS_COLLECTION = "posts"
    }

    /**
     * Creates a new post document in Firestore strictly containing the 10 allowed fields:
     * - ownerUid
     * - type (LOST or FOUND)
     * - title
     * - description
     * - category
     * - location
     * - imageUrl
     * - imagePublicId
     * - status (OPEN)
     * - createdAt (FieldValue.serverTimestamp())
     */
    suspend fun createPost(post: PostItem): Result<String> {
        return try {
            val postData = hashMapOf<String, Any>(
                "ownerUid" to post.ownerUid,
                "type" to post.type,
                "title" to post.title.trim(),
                "description" to post.description.trim(),
                "category" to post.category,
                "location" to post.location.trim(),
                "imageUrl" to post.imageUrl,
                "imagePublicId" to post.imagePublicId,
                "status" to "OPEN",
                "createdAt" to FieldValue.serverTimestamp()
            )

            Log.d(
                TAG,
                "Submitting Firestore post payload: keys=${postData.keys.sorted()}, ownerUid=${post.ownerUid}, type=${post.type}, status=${postData["status"]}"
            )

            val docRef = firestore.collection(POSTS_COLLECTION).add(postData).await()
            Log.d(TAG, "Post created successfully with Firestore document ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create post in Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Returns a real-time Flow of posts ordered newest first.
     */
    fun getPostsFlow(): Flow<List<PostItem>> = callbackFlow {
        val listenerRegistration = firestore.collection(POSTS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to posts collection: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val posts = snapshot.documents.mapNotNull { doc ->
                        try {
                            PostItem(
                                id = doc.id,
                                ownerUid = doc.getString("ownerUid") ?: doc.getString("userId").orEmpty(),
                                type = doc.getString("type") ?: doc.getString("postType") ?: "LOST",
                                title = doc.getString("title").orEmpty(),
                                description = doc.getString("description").orEmpty(),
                                category = doc.getString("category") ?: "Other",
                                location = doc.getString("location").orEmpty(),
                                imageUrl = doc.getString("imageUrl") ?: doc.getString("mediaUrl").orEmpty(),
                                imagePublicId = doc.getString("imagePublicId") ?: doc.getString("mediaPublicId").orEmpty(),
                                status = doc.getString("status") ?: "OPEN",
                                createdAt = doc.getTimestamp("createdAt")
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parsing post document ${doc.id}", e)
                            null
                        }
                    }
                    trySend(posts)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Deletes a post from Firestore (allowed by security rules if ownerUid matches).
     */
    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            firestore.collection(POSTS_COLLECTION).document(postId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete post $postId: ${e.message}", e)
            Result.failure(e)
        }
    }
}
