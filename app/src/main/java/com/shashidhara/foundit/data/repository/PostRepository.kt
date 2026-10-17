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
     * Creates a new post document in Firestore strictly conforming to the security rules.
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
                "Submitting Firestore post payload: ownerUid=${post.ownerUid}, type=${post.type}, title=${post.title}"
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
     * Updates an existing post. Protected fields (ownerUid, createdAt) are never changed.
     * Supports updating title, description, category, location, status, type, and media.
     */
    suspend fun updatePost(
        postId: String,
        title: String,
        description: String,
        category: String,
        location: String,
        status: String,
        type: String? = null,
        imageUrl: String? = null,
        imagePublicId: String? = null
    ): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "title" to title.trim(),
                "description" to description.trim(),
                "category" to category,
                "location" to location.trim(),
                "status" to status,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!type.isNullOrBlank()) {
                updates["type"] = type
            }
            if (imageUrl != null) {
                updates["imageUrl"] = imageUrl
            }
            if (imagePublicId != null) {
                updates["imagePublicId"] = imagePublicId
            }

            firestore.collection(POSTS_COLLECTION).document(postId).update(updates).await()
            Log.d(TAG, "Post $postId updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update post $postId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches a single post by ID.
     */
    suspend fun getPostById(postId: String): Result<PostItem?> {
        return try {
            val doc = firestore.collection(POSTS_COLLECTION).document(postId).get().await()
            if (doc.exists()) {
                Result.success(parsePost(doc))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get post $postId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches a paginated page of community posts for the Home feed.
     * Supports search query, category filtering, and Lost/Found type filters.
     * Uses cursor-based pagination with DocumentSnapshot.
     */
    suspend fun getPostsPaginated(
        page: Int = 1,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        searchQuery: String = "",
        category: String? = null,
        typeFilter: String? = null, // "ALL", "LOST", "FOUND"
        statusFilter: String? = "OPEN", // Default to OPEN posts
        lastSnapshot: DocumentSnapshot? = null
    ): Result<PaginatedPosts> {
        return try {
            val cleanQuery = searchQuery.trim()
            val cleanCategory = if (category == "All Items" || category.isNullOrBlank()) null else category
            val cleanType = if (typeFilter == "ALL" || typeFilter.isNullOrBlank()) null else typeFilter
            val cleanStatus = if (statusFilter == "ALL" || statusFilter.isNullOrBlank()) null else statusFilter

            var query: Query = firestore.collection(POSTS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            if (page > 1 && lastSnapshot != null) {
                query = query.startAfter(lastSnapshot)
            }

            // Buffer factor when applying client search/category filters without composite index
            val needsFiltering = cleanQuery.isNotBlank() || cleanCategory != null || cleanType != null || cleanStatus != null
            val fetchLimit = if (needsFiltering) {
                (pageSize * 3).toLong()
            } else {
                pageSize.toLong()
            }

            val snapshot = query.limit(fetchLimit).get().await()
            val rawDocs = snapshot.documents
            val allPosts = rawDocs.mapNotNull { parsePost(it) }

            // Filter in-memory for flexible combination
            val filteredPosts = allPosts.filter { post ->
                val matchesCategory = cleanCategory == null || post.category.equals(cleanCategory, ignoreCase = true)
                val matchesType = cleanType == null || post.type.equals(cleanType, ignoreCase = true)
                val matchesStatus = cleanStatus == null || post.status.equals(cleanStatus, ignoreCase = true)
                val matchesSearch = cleanQuery.isBlank() ||
                        post.title.contains(cleanQuery, ignoreCase = true) ||
                        post.description.contains(cleanQuery, ignoreCase = true) ||
                        post.location.contains(cleanQuery, ignoreCase = true) ||
                        post.type.contains(cleanQuery, ignoreCase = true)

                matchesCategory && matchesType && matchesStatus && matchesSearch
            }

            val newLastDoc = rawDocs.lastOrNull()
            val hasMore = rawDocs.size.toLong() >= fetchLimit

            Log.d(TAG, "Home Feed: Page=$page, Filtered=${filteredPosts.size}, Raw=${rawDocs.size}, hasMore=$hasMore")
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
     * Post Owner Dashboard: Queries ONLY posts where ownerUid == currentUser.uid.
     * Strictly fulfills requirement:
     * "Query only the current user's posts using ownerUid == currentUser.uid.
     *  Never fetch all posts and filter them locally to implement My Posts."
     */
    suspend fun getMyPostsPaginated(
        ownerUid: String,
        page: Int = 1,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        searchQuery: String = "",
        category: String? = null,
        typeFilter: String? = null,
        statusFilter: String? = null,
        lastSnapshot: DocumentSnapshot? = null
    ): Result<PaginatedPosts> {
        return try {
            require(ownerUid.isNotBlank()) { "Owner UID is required to load My Posts" }

            val cleanQuery = searchQuery.trim()
            val cleanCategory = if (category == "All Items" || category.isNullOrBlank()) null else category
            val cleanType = if (typeFilter == "ALL" || typeFilter.isNullOrBlank()) null else typeFilter
            val cleanStatus = if (statusFilter == "ALL" || statusFilter.isNullOrBlank()) null else statusFilter

            // Direct server-side filter by ownerUid (uses single-field index, no composite index required)
            val snapshot = firestore.collection(POSTS_COLLECTION)
                .whereEqualTo("ownerUid", ownerUid)
                .get()
                .await()

            var rawDocs = snapshot.documents
            // Fallback for legacy records created with "userId" instead of "ownerUid"
            if (rawDocs.isEmpty()) {
                val legacySnapshot = firestore.collection(POSTS_COLLECTION)
                    .whereEqualTo("userId", ownerUid)
                    .get()
                    .await()
                if (!legacySnapshot.isEmpty) {
                    rawDocs = legacySnapshot.documents
                }
            }

            // Parse and sort by createdAt descending in memory (immune to missing composite index)
            val allMyPosts = rawDocs.mapNotNull { parsePost(it) }
                .sortedByDescending { it.createdAt?.seconds ?: 0L }

            val filtered = allMyPosts.filter { post ->
                val matchesCategory = cleanCategory == null || post.category.equals(cleanCategory, ignoreCase = true)
                val matchesType = cleanType == null || post.type.equals(cleanType, ignoreCase = true)
                val matchesStatus = cleanStatus == null || post.status.equals(cleanStatus, ignoreCase = true)
                val matchesSearch = cleanQuery.isBlank() ||
                        post.title.contains(cleanQuery, ignoreCase = true) ||
                        post.description.contains(cleanQuery, ignoreCase = true) ||
                        post.location.contains(cleanQuery, ignoreCase = true)

                matchesCategory && matchesType && matchesStatus && matchesSearch
            }

            val fromIndex = ((page - 1) * pageSize).coerceAtLeast(0)
            val toIndex = (fromIndex + pageSize).coerceAtMost(filtered.size)
            val pageItems = if (fromIndex < filtered.size) filtered.subList(fromIndex, toIndex) else emptyList()
            val hasMore = toIndex < filtered.size

            Log.d(TAG, "My Posts: Page=$page, FilteredTotal=${filtered.size}, PageCount=${pageItems.size}, hasMore=$hasMore")
            Result.success(
                PaginatedPosts(
                    posts = pageItems,
                    lastSnapshot = null,
                    hasMore = hasMore,
                    page = page
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch My Posts: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time Flow of user's posts.
     */
    fun getMyPostsFlow(ownerUid: String): Flow<List<PostItem>> = callbackFlow {
        if (ownerUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection(POSTS_COLLECTION)
            .whereEqualTo("ownerUid", ownerUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to user posts: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents
                    ?.mapNotNull { parsePost(it) }
                    ?.sortedByDescending { it.createdAt?.seconds ?: 0L }
                    ?: emptyList()
                trySend(posts)
            }

        awaitClose { registration.remove() }
    }

    /**
     * Deletes a post from Firestore (allowed by security rules if ownerUid matches).
     */
    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            firestore.collection(POSTS_COLLECTION).document(postId).delete().await()
            Log.d(TAG, "Post $postId deleted successfully")
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
                createdAt = doc.getTimestamp("createdAt"),
                updatedAt = doc.getTimestamp("updatedAt")
            )
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing post document ${doc.id}", e)
            null
        }
    }
}
