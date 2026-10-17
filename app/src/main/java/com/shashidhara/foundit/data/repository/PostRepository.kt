package com.shashidhara.foundit.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shashidhara.foundit.data.model.PostItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class PaginatedPosts(
    val posts: List<PostItem>,
    val lastSnapshot: DocumentSnapshot?,
    val hasMore: Boolean,
    val page: Int
)

class PostRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "FoundIT_PostRepo"
        const val POSTS_COLLECTION = "posts"
        const val DEFAULT_PAGE_SIZE = 20
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
     * Fetches a paginated page of valid user posts from Firestore.
     * Supports search optimization and category filtering.
     * Default request: page = 1, size = 20, search = ""
     */
    suspend fun getPostsPaginated(
        page: Int = 1,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        searchQuery: String = "",
        category: String? = null,
        lastSnapshot: DocumentSnapshot? = null
    ): Result<PaginatedPosts> {
        return try {
            val cleanQuery = searchQuery.trim()
            val cleanCategory = if (category == "All Items" || category.isNullOrBlank()) null else category

            var query: Query = firestore.collection(POSTS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            if (page > 1 && lastSnapshot != null) {
                query = query.startAfter(lastSnapshot)
            }

            // Determine fetch limit (if filtering by search/category client-side, fetch buffer)
            val fetchLimit = if (cleanQuery.isNotBlank() || cleanCategory != null) {
                (pageSize * 3).toLong()
            } else {
                pageSize.toLong()
            }

            val snapshot = query.limit(fetchLimit).get().await()
            val rawDocs = snapshot.documents
            val allPosts = rawDocs.mapNotNull { parsePost(it) }

            // Apply search and category filtering if specified
            val filteredPosts = allPosts.filter { post ->
                val matchesCategory = cleanCategory == null || post.category.equals(cleanCategory, ignoreCase = true)
                val matchesSearch = cleanQuery.isBlank() ||
                        post.title.contains(cleanQuery, ignoreCase = true) ||
                        post.description.contains(cleanQuery, ignoreCase = true) ||
                        post.location.contains(cleanQuery, ignoreCase = true) ||
                        post.type.contains(cleanQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }

            val newLastDoc = rawDocs.lastOrNull()
            val hasMore = rawDocs.size.toLong() >= fetchLimit

            Log.d(TAG, "Fetched page=$page, size=${filteredPosts.size}, hasMore=$hasMore, search='$cleanQuery', category='$cleanCategory'")
            Result.success(
                PaginatedPosts(
                    posts = filteredPosts,
                    lastSnapshot = newLastDoc,
                    hasMore = hasMore,
                    page = page
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch paginated posts: ${e.message}", e)
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
                    val posts = snapshot.documents.mapNotNull { parsePost(it) }
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

    private fun parsePost(doc: DocumentSnapshot): PostItem? {
        return try {
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
}

