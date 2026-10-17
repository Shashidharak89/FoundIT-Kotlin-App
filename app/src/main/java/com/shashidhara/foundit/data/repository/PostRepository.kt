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
     * Creates a new post in Firestore under collection 'posts'.
     * Sets serverTimestamp for createdAt.
     * Retains imageUrl and imagePublicId alongside videoUrl and mediaUrl.
     */
    suspend fun createPost(post: PostItem): Result<String> {
        return try {
            val postData = hashMapOf(
                "title" to post.title.trim(),
                "description" to post.description.trim(),
                "category" to post.category,
                "location" to post.location.trim(),
                "postType" to post.postType,
                "userId" to post.userId,
                "authorName" to post.authorName,
                "authorPhotoUrl" to post.authorPhotoUrl,
                "imageUrl" to post.imageUrl,
                "imagePublicId" to post.imagePublicId,
                "videoUrl" to post.videoUrl,
                "mediaUrl" to post.mediaUrl,
                "mediaPublicId" to post.mediaPublicId,
                "resourceType" to post.resourceType,
                "createdAt" to FieldValue.serverTimestamp()
            )

            val docRef = firestore.collection(POSTS_COLLECTION).add(postData).await()
            Log.d(TAG, "Post created successfully with ID: ${docRef.id}")
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
                                title = doc.getString("title").orEmpty(),
                                description = doc.getString("description").orEmpty(),
                                category = doc.getString("category") ?: "Other",
                                location = doc.getString("location").orEmpty(),
                                postType = doc.getString("postType") ?: "LOST",
                                userId = doc.getString("userId").orEmpty(),
                                authorName = doc.getString("authorName").orEmpty(),
                                authorPhotoUrl = doc.getString("authorPhotoUrl").orEmpty(),
                                imageUrl = doc.getString("imageUrl").orEmpty(),
                                imagePublicId = doc.getString("imagePublicId").orEmpty(),
                                videoUrl = doc.getString("videoUrl").orEmpty(),
                                mediaUrl = doc.getString("mediaUrl").orEmpty(),
                                mediaPublicId = doc.getString("mediaPublicId").orEmpty(),
                                resourceType = doc.getString("resourceType") ?: "image",
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
     * Deletes a post from Firestore (allowed by security rules if userId matches).
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
