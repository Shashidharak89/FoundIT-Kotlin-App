package com.shashidhara.foundit.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.shashidhara.foundit.data.model.PostItem
import com.shashidhara.foundit.data.model.PostRequest
import com.shashidhara.foundit.data.repository.CloudinaryRepository
import com.shashidhara.foundit.data.repository.PostRepository
import com.shashidhara.foundit.data.repository.RequestRepository
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DashboardTab(val title: String) {
    MY_POSTS("My Posts"),
    RECEIVED_REQUESTS("Requests Received"),
    SENT_REQUESTS("My Sent Requests")
}

data class MyPostsUiState(
    val posts: List<PostItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val page: Int = 1,
    val searchQuery: String = "",
    val selectedCategory: String = "All Items",
    val selectedType: String = "ALL", // "ALL", "LOST", "FOUND"
    val selectedStatus: String = "ALL", // "ALL", "OPEN", "PENDING", "RESOLVED", "CLOSED"
    val error: String? = null
)

data class DashboardUiState(
    val selectedTab: DashboardTab = DashboardTab.MY_POSTS,
    val myPostsState: MyPostsUiState = MyPostsUiState(),
    val receivedRequests: List<PostRequest> = emptyList(),
    val sentRequests: List<PostRequest> = emptyList(),
    val isLoadingRequests: Boolean = false,
    val actionError: String? = null,
    val actionSuccessMessage: String? = null
)

class DashboardViewModel(
    private val postRepository: PostRepository = PostRepository(),
    private val requestRepository: RequestRepository = RequestRepository(),
    private val cloudinaryRepository: CloudinaryRepository = CloudinaryRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "FoundIT_DashboardVM"
    }

    private var myPostsLastSnapshot: DocumentSnapshot? = null

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        if (!currentUid.isNullOrBlank()) {
            loadInitialMyPosts(currentUid)
            observeRequests(currentUid)
        }
    }

    private var receivedRequestsJob: Job? = null
    private var sentRequestsJob: Job? = null

    fun onUserChanged(uid: String?) {
        receivedRequestsJob?.cancel()
        sentRequestsJob?.cancel()
        if (!uid.isNullOrBlank()) {
            loadInitialMyPosts(uid)
            observeRequests(uid)
        } else {
            _uiState.update {
                DashboardUiState()
            }
        }
    }

    fun selectTab(tab: DashboardTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    // --- My Posts Management ---

    fun onSearchQueryChanged(query: String) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (_uiState.value.myPostsState.searchQuery == query) return
        _uiState.update {
            it.copy(myPostsState = it.myPostsState.copy(searchQuery = query))
        }
        loadInitialMyPosts(currentUid)
    }

    fun onCategorySelected(category: String) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (_uiState.value.myPostsState.selectedCategory == category) return
        _uiState.update {
            it.copy(myPostsState = it.myPostsState.copy(selectedCategory = category))
        }
        loadInitialMyPosts(currentUid)
    }

    fun onTypeFilterSelected(type: String) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (_uiState.value.myPostsState.selectedType == type) return
        _uiState.update {
            it.copy(myPostsState = it.myPostsState.copy(selectedType = type))
        }
        loadInitialMyPosts(currentUid)
    }

    fun onStatusFilterSelected(status: String) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (_uiState.value.myPostsState.selectedStatus == status) return
        _uiState.update {
            it.copy(myPostsState = it.myPostsState.copy(selectedStatus = status))
        }
        loadInitialMyPosts(currentUid)
    }

    fun refreshMyPosts() {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        loadInitialMyPosts(currentUid)
    }

    fun loadNextMyPostsPage() {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val currentMyPosts = _uiState.value.myPostsState
        if (currentMyPosts.isLoading || currentMyPosts.isLoadingMore || !currentMyPosts.hasMore) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(myPostsState = it.myPostsState.copy(isLoadingMore = true))
            }
            val nextPage = currentMyPosts.page + 1

            val result = postRepository.getMyPostsPaginated(
                ownerUid = currentUid,
                page = nextPage,
                pageSize = 20,
                searchQuery = currentMyPosts.searchQuery,
                category = currentMyPosts.selectedCategory,
                typeFilter = currentMyPosts.selectedType,
                statusFilter = currentMyPosts.selectedStatus,
                lastSnapshot = myPostsLastSnapshot
            )

            if (result.isSuccess) {
                val paginated = result.getOrThrow()
                myPostsLastSnapshot = paginated.lastSnapshot
                _uiState.update {
                    it.copy(
                        myPostsState = it.myPostsState.copy(
                            posts = it.myPostsState.posts + paginated.posts,
                            isLoadingMore = false,
                            hasMore = paginated.hasMore,
                            page = nextPage,
                            error = null
                        )
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        myPostsState = it.myPostsState.copy(
                            isLoadingMore = false,
                            error = result.exceptionOrNull()?.message
                        )
                    )
                }
            }
        }
    }

    private fun loadInitialMyPosts(ownerUid: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    myPostsState = it.myPostsState.copy(
                        isLoading = true,
                        page = 1,
                        hasMore = true,
                        error = null
                    )
                )
            }
            myPostsLastSnapshot = null

            val currentMyPosts = _uiState.value.myPostsState
            val result = postRepository.getMyPostsPaginated(
                ownerUid = ownerUid,
                page = 1,
                pageSize = 20,
                searchQuery = currentMyPosts.searchQuery,
                category = currentMyPosts.selectedCategory,
                typeFilter = currentMyPosts.selectedType,
                statusFilter = currentMyPosts.selectedStatus,
                lastSnapshot = null
            )

            if (result.isSuccess) {
                val paginated = result.getOrThrow()
                myPostsLastSnapshot = paginated.lastSnapshot
                _uiState.update {
                    it.copy(
                        myPostsState = it.myPostsState.copy(
                            posts = paginated.posts,
                            isLoading = false,
                            hasMore = paginated.hasMore,
                            error = null
                        )
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        myPostsState = it.myPostsState.copy(
                            isLoading = false,
                            posts = emptyList(),
                            error = result.exceptionOrNull()?.message
                        )
                    )
                }
            }
        }
    }

    // --- Edit & Delete Post ---

    fun updatePostDetails(
        postId: String,
        title: String,
        description: String,
        category: String,
        location: String,
        status: String,
        type: String? = null,
        existingImageUrl: String? = null,
        existingImagePublicId: String? = null,
        newMediaUri: Uri? = null,
        clearMedia: Boolean = false,
        context: Context? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            var finalImageUrl = existingImageUrl
            var finalPublicId = existingImagePublicId

            if (clearMedia) {
                finalImageUrl = ""
                finalPublicId = ""
            } else if (newMediaUri != null && context != null) {
                val uploadResult = cloudinaryRepository.uploadMedia(context, newMediaUri, isVideo = false)
                if (uploadResult.isSuccess) {
                    val media = uploadResult.getOrNull()
                    if (media != null) {
                        finalImageUrl = media.secureUrl
                        finalPublicId = media.publicId
                    }
                } else {
                    _uiState.update {
                        it.copy(actionError = "Media upload failed: ${uploadResult.exceptionOrNull()?.message}")
                    }
                    return@launch
                }
            }

            val result = postRepository.updatePost(
                postId = postId,
                title = title,
                description = description,
                category = category,
                location = location,
                status = status,
                type = type,
                imageUrl = finalImageUrl,
                imagePublicId = finalPublicId
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(actionSuccessMessage = "Post updated successfully!")
                }
                refreshMyPosts()
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(actionError = result.exceptionOrNull()?.message ?: "Failed to update post")
                }
            }
        }
    }

    fun deletePost(postId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = postRepository.deletePost(postId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(actionSuccessMessage = "Post deleted successfully")
                }
                refreshMyPosts()
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(actionError = result.exceptionOrNull()?.message ?: "Failed to delete post")
                }
            }
        }
    }

    // --- Request Management for Post Owners ---

    private fun observeRequests(uid: String) {
        receivedRequestsJob = requestRepository.getReceivedRequestsFlow(uid)
            .catch { e -> Log.w(TAG, "Stream error on received requests: ${e.message}") }
            .onEach { requests ->
                _uiState.update { it.copy(receivedRequests = requests) }
            }
            .launchIn(viewModelScope)

        sentRequestsJob = requestRepository.getSentRequestsFlow(uid)
            .catch { e -> Log.w(TAG, "Stream error on sent requests: ${e.message}") }
            .onEach { requests ->
                _uiState.update { it.copy(sentRequests = requests) }
            }
            .launchIn(viewModelScope)
    }

    fun acceptRequest(request: PostRequest, markPostPending: Boolean = true) {
        viewModelScope.launch {
            val targetPostStatus = if (markPostPending) "PENDING" else null
            val result = requestRepository.updateRequestStatus(
                requestId = request.id,
                newStatus = PostRequest.STATUS_ACCEPTED,
                postId = request.postId,
                updatePostStatus = targetPostStatus
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(actionSuccessMessage = "Request accepted! Post status updated to PENDING handover.")
                }
                refreshMyPosts()
            } else {
                _uiState.update {
                    it.copy(actionError = result.exceptionOrNull()?.message ?: "Failed to accept request")
                }
            }
        }
    }

    fun rejectRequest(request: PostRequest) {
        viewModelScope.launch {
            val result = requestRepository.updateRequestStatus(
                requestId = request.id,
                newStatus = PostRequest.STATUS_REJECTED,
                postId = request.postId,
                updatePostStatus = null
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(actionSuccessMessage = "Request rejected.")
                }
            } else {
                _uiState.update {
                    it.copy(actionError = result.exceptionOrNull()?.message ?: "Failed to reject request")
                }
            }
        }
    }

    fun cancelSentRequest(requestId: String) {
        viewModelScope.launch {
            val result = requestRepository.cancelRequest(requestId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(actionSuccessMessage = "Request cancelled.")
                }
            } else {
                _uiState.update {
                    it.copy(actionError = result.exceptionOrNull()?.message ?: "Failed to cancel request")
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update {
            it.copy(actionError = null, actionSuccessMessage = null)
        }
    }
}
