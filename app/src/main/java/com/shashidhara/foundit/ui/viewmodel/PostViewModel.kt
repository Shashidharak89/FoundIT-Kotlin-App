package com.shashidhara.foundit.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.shashidhara.foundit.data.model.PostItem
import com.shashidhara.foundit.data.model.PostRequest
import com.shashidhara.foundit.data.model.UserProfile
import com.shashidhara.foundit.data.repository.CloudinaryRepository
import com.shashidhara.foundit.data.repository.CloudinaryUploadResult
import com.shashidhara.foundit.data.repository.PostRepository
import com.shashidhara.foundit.data.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SubmissionState {
    data object Idle : SubmissionState
    data class Uploading(val progress: Float) : SubmissionState
    data object SavingToFirestore : SubmissionState
    data object Success : SubmissionState
    data class Error(val message: String, val canRetrySave: Boolean) : SubmissionState
}

data class FeedUiState(
    val posts: List<PostItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val page: Int = 1,
    val pageSize: Int = 20,
    val searchQuery: String = "",
    val selectedCategory: String = "All Items",
    val selectedType: String = "ALL", // "ALL", "LOST", "FOUND"
    val selectedStatus: String = "ALL", // "ALL", "OPEN", "RESOLVED"
    val error: String? = null
)

class PostViewModel(
    private val postRepository: PostRepository = PostRepository(),
    private val cloudinaryRepository: CloudinaryRepository = CloudinaryRepository(),
    private val requestRepository: RequestRepository = RequestRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "FoundIT_PostVM"
    }

    private var lastSnapshot: DocumentSnapshot? = null

    // Real-time & paginated feed UI state
    private val _feedUiState = MutableStateFlow(FeedUiState())
    val feedUiState: StateFlow<FeedUiState> = _feedUiState.asStateFlow()

    // Backward-compatible posts flow for components observing posts directly
    val posts: StateFlow<List<PostItem>> = _feedUiState
        .map { it.posts }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Request State for Post Details
    private val _currentPostRequest = MutableStateFlow<PostRequest?>(null)
    val currentPostRequest: StateFlow<PostRequest?> = _currentPostRequest.asStateFlow()

    private val _isSubmittingRequest = MutableStateFlow(false)
    val isSubmittingRequest: StateFlow<Boolean> = _isSubmittingRequest.asStateFlow()

    private val _requestFeedback = MutableStateFlow<String?>(null)
    val requestFeedback: StateFlow<String?> = _requestFeedback.asStateFlow()

    init {
        loadInitialPosts()
    }

    fun onSearchQueryChanged(query: String) {
        if (_feedUiState.value.searchQuery == query) return
        _feedUiState.update { it.copy(searchQuery = query) }
        loadInitialPosts(search = query)
    }

    fun onCategorySelected(category: String) {
        if (_feedUiState.value.selectedCategory == category) return
        _feedUiState.update { it.copy(selectedCategory = category) }
        loadInitialPosts(category = category)
    }

    fun onTypeFilterChanged(type: String) {
        if (_feedUiState.value.selectedType == type) return
        _feedUiState.update { it.copy(selectedType = type) }
        loadInitialPosts(typeFilter = type)
    }

    fun onStatusFilterChanged(status: String) {
        if (_feedUiState.value.selectedStatus == status) return
        _feedUiState.update { it.copy(selectedStatus = status) }
        loadInitialPosts(statusFilter = status)
    }

    fun refreshFeed() {
        loadInitialPosts()
    }

    fun loadNextPage() {
        val currentState = _feedUiState.value
        if (currentState.isLoading || currentState.isLoadingMore || !currentState.hasMore) {
            return
        }

        viewModelScope.launch {
            _feedUiState.update { it.copy(isLoadingMore = true) }
            val nextPage = currentState.page + 1

            val result = postRepository.getPostsPaginated(
                page = nextPage,
                pageSize = currentState.pageSize,
                searchQuery = currentState.searchQuery,
                category = currentState.selectedCategory,
                typeFilter = currentState.selectedType,
                statusFilter = currentState.selectedStatus,
                lastSnapshot = lastSnapshot
            )

            if (result.isSuccess) {
                val paginated = result.getOrThrow()
                lastSnapshot = paginated.lastSnapshot
                _feedUiState.update {
                    it.copy(
                        posts = it.posts + paginated.posts,
                        isLoadingMore = false,
                        hasMore = paginated.hasMore,
                        page = nextPage,
                        error = null
                    )
                }
            } else {
                _feedUiState.update {
                    it.copy(
                        isLoadingMore = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    private fun loadInitialPosts(
        search: String = _feedUiState.value.searchQuery,
        category: String = _feedUiState.value.selectedCategory,
        typeFilter: String = _feedUiState.value.selectedType,
        statusFilter: String = _feedUiState.value.selectedStatus
    ) {
        viewModelScope.launch {
            _feedUiState.update {
                it.copy(
                    isLoading = true,
                    page = 1,
                    hasMore = true,
                    error = null
                )
            }
            lastSnapshot = null

            val result = postRepository.getPostsPaginated(
                page = 1,
                pageSize = 20,
                searchQuery = search,
                category = category,
                typeFilter = typeFilter,
                statusFilter = statusFilter,
                lastSnapshot = null
            )

            if (result.isSuccess) {
                val paginated = result.getOrThrow()
                lastSnapshot = paginated.lastSnapshot
                _feedUiState.update {
                    it.copy(
                        posts = paginated.posts,
                        isLoading = false,
                        hasMore = paginated.hasMore,
                        page = 1,
                        error = null
                    )
                }
            } else {
                _feedUiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    // --- Request / Response Actions for Post Details ---

    fun checkUserRequestForPost(postId: String, currentUserUid: String) {
        if (postId.isBlank() || currentUserUid.isBlank()) {
            _currentPostRequest.value = null
            return
        }

        viewModelScope.launch {
            val result = requestRepository.getRequestForPost(postId, currentUserUid)
            if (result.isSuccess) {
                _currentPostRequest.value = result.getOrNull()
            } else {
                _currentPostRequest.value = null
            }
        }
    }

    fun submitPostRequest(
        postId: String,
        postOwnerUid: String,
        currentUser: UserProfile,
        message: String,
        onSuccess: () -> Unit
    ) {
        if (message.isBlank()) {
            _requestFeedback.value = "Please enter a message for the post owner."
            return
        }

        viewModelScope.launch {
            _isSubmittingRequest.value = true
            _requestFeedback.value = null

            val result = requestRepository.submitRequest(
                postId = postId,
                postOwnerUid = postOwnerUid,
                requester = currentUser,
                message = message
            )

            _isSubmittingRequest.value = false

            if (result.isSuccess) {
                _currentPostRequest.value = result.getOrNull()
                _requestFeedback.value = "Request submitted successfully! The owner will be notified."
                onSuccess()
            } else {
                _requestFeedback.value = result.exceptionOrNull()?.message ?: "Failed to submit request."
            }
        }
    }

    fun clearRequestFeedback() {
        _requestFeedback.value = null
    }

    fun cancelPostRequest(requestId: String, postId: String, currentUserUid: String) {
        viewModelScope.launch {
            val result = requestRepository.cancelRequest(requestId)
            if (result.isSuccess) {
                _requestFeedback.value = "Your request was cancelled."
                checkUserRequestForPost(postId, currentUserUid)
            } else {
                _requestFeedback.value = result.exceptionOrNull()?.message ?: "Failed to cancel request."
            }
        }
    }

    // --- Post Details Selection & Owner Actions ---

    private val _selectedPostForDetails = MutableStateFlow<PostItem?>(null)
    val selectedPostForDetails: StateFlow<PostItem?> = _selectedPostForDetails.asStateFlow()

    fun selectPostForDetails(post: PostItem?, currentUserUid: String? = null) {
        _selectedPostForDetails.value = post
        if (post != null && !currentUserUid.isNullOrBlank()) {
            checkUserRequestForPost(post.id, currentUserUid)
        } else {
            _currentPostRequest.value = null
        }
    }

    fun clearSelectedPost() {
        _selectedPostForDetails.value = null
        _currentPostRequest.value = null
    }

    fun deletePost(postId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val result = postRepository.deletePost(postId)
            if (result.isSuccess) {
                _feedUiState.update { state ->
                    state.copy(posts = state.posts.filter { it.id != postId })
                }
                if (_selectedPostForDetails.value?.id == postId) {
                    _selectedPostForDetails.value = null
                }
                onSuccess()
            } else {
                val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Failed to delete post."
                onError(errorMsg)
            }
        }
    }

    fun updatePost(
        postId: String,
        title: String,
        description: String,
        category: String,
        location: String,
        status: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = postRepository.updatePost(
                postId = postId,
                title = title,
                description = description,
                category = category,
                location = location,
                status = status
            )
            if (result.isSuccess) {
                _feedUiState.update { state ->
                    state.copy(
                        posts = state.posts.map {
                            if (it.id == postId) {
                                it.copy(
                                    title = title,
                                    description = description,
                                    category = category,
                                    location = location,
                                    status = status
                                )
                            } else it
                        }
                    )
                }
                _selectedPostForDetails.update { current ->
                    if (current?.id == postId) {
                        current.copy(
                            title = title,
                            description = description,
                            category = category,
                            location = location,
                            status = status
                        )
                    } else current
                }
                onSuccess()
            } else {
                val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Failed to update post."
                onError(errorMsg)
            }
        }
    }

    fun updatePostStatus(
        post: PostItem,
        newStatus: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        updatePost(
            postId = post.id,
            title = post.title,
            description = post.description,
            category = post.category,
            location = post.location,
            status = newStatus,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    // --- Create Post Form State ---

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _category = MutableStateFlow("Electronics")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _location = MutableStateFlow("")
    val location: StateFlow<String> = _location.asStateFlow()

    private val _postType = MutableStateFlow("LOST") // "LOST" or "FOUND"
    val postType: StateFlow<String> = _postType.asStateFlow()

    private val _selectedMediaUri = MutableStateFlow<Uri?>(null)
    val selectedMediaUri: StateFlow<Uri?> = _selectedMediaUri.asStateFlow()

    private val _selectedMediaMimeType = MutableStateFlow<String?>(null)
    val selectedMediaMimeType: StateFlow<String?> = _selectedMediaMimeType.asStateFlow()

    private val _isVideo = MutableStateFlow(false)
    val isVideo: StateFlow<Boolean> = _isVideo.asStateFlow()

    private val _uploadedMedia = MutableStateFlow<CloudinaryUploadResult?>(null)
    val uploadedMedia: StateFlow<CloudinaryUploadResult?> = _uploadedMedia.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    private val _submissionState = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val submissionState: StateFlow<SubmissionState> = _submissionState.asStateFlow()

    fun setTitle(value: String) { _title.value = value }
    fun setDescription(value: String) { _description.value = value }
    fun setCategory(value: String) { _category.value = value }
    fun setLocation(value: String) { _location.value = value }
    fun setPostType(value: String) { _postType.value = value }

    fun onMediaSelected(uri: Uri, mimeType: String, isVid: Boolean) {
        _selectedMediaUri.value = uri
        _selectedMediaMimeType.value = mimeType
        _isVideo.value = isVid
        _uploadedMedia.value = null
        _uploadProgress.value = 0f
        _submissionState.value = SubmissionState.Idle
    }

    fun clearMedia() {
        _selectedMediaUri.value = null
        _selectedMediaMimeType.value = null
        _isVideo.value = false
        _uploadedMedia.value = null
        _uploadProgress.value = 0f
    }

    fun resetSubmissionState() {
        _submissionState.value = SubmissionState.Idle
    }

    fun resetForm() {
        _title.value = ""
        _description.value = ""
        _category.value = "Electronics"
        _location.value = ""
        _postType.value = "LOST"
        clearMedia()
        _submissionState.value = SubmissionState.Idle
    }

    /**
     * Submits a new post to Cloudinary and Cloud Firestore.
     */
    fun submitPost(context: Context, currentUser: UserProfile?) {
        val ownerUid = currentUser?.uid ?: FirebaseAuth.getInstance().currentUser?.uid
        if (ownerUid.isNullOrBlank()) {
            _submissionState.value = SubmissionState.Error(
                message = "You must be signed in with Google to create a post.",
                canRetrySave = false
            )
            return
        }

        val trimmedTitle = _title.value.trim()
        val trimmedLocation = _location.value.trim()

        if (trimmedTitle.isBlank()) {
            _submissionState.value = SubmissionState.Error(
                message = "Please provide an item title.",
                canRetrySave = false
            )
            return
        }

        if (trimmedLocation.isBlank()) {
            _submissionState.value = SubmissionState.Error(
                message = "Please provide the location where the item was lost or found.",
                canRetrySave = false
            )
            return
        }

        val mediaUri = _selectedMediaUri.value
        val existingUpload = _uploadedMedia.value

        if (mediaUri == null && existingUpload == null) {
            _submissionState.value = SubmissionState.Error(
                message = "Please select a photo or video proof from your device.",
                canRetrySave = false
            )
            return
        }

        viewModelScope.launch {
            var mediaResult = existingUpload

            // Step 1: Upload to Cloudinary (skip if already uploaded during a previous retry)
            if (mediaResult == null && mediaUri != null) {
                _submissionState.value = SubmissionState.Uploading(0.05f)
                val uploadResult = cloudinaryRepository.uploadMedia(
                    context = context,
                    uri = mediaUri,
                    isVideo = _isVideo.value,
                    onProgress = { progress ->
                        _uploadProgress.value = progress
                        _submissionState.value = SubmissionState.Uploading(progress)
                    }
                )

                if (uploadResult.isFailure) {
                    val error = uploadResult.exceptionOrNull()
                    Log.e(TAG, "Cloudinary upload failed", error)
                    _submissionState.value = SubmissionState.Error(
                        message = error?.localizedMessage ?: "Failed to upload media to Cloudinary.",
                        canRetrySave = false
                    )
                    return@launch
                }

                mediaResult = uploadResult.getOrNull()
                _uploadedMedia.value = mediaResult
            }

            if (mediaResult == null) {
                _submissionState.value = SubmissionState.Error(
                    message = "Media upload could not be verified.",
                    canRetrySave = false
                )
                return@launch
            }

            // Step 2: Save to Firestore matching the exact schema required by security rules
            _submissionState.value = SubmissionState.SavingToFirestore
            val cleanType = if (_postType.value.equals("FOUND", ignoreCase = true)) "FOUND" else "LOST"

            val postItem = PostItem(
                ownerUid = ownerUid,
                type = cleanType,
                title = trimmedTitle,
                description = _description.value.trim(),
                category = _category.value.ifBlank { "Other" },
                location = trimmedLocation,
                imageUrl = mediaResult.secureUrl,
                imagePublicId = mediaResult.publicId,
                status = "OPEN"
            )

            Log.d(TAG, "Initiating Firestore write for ownerUid=$ownerUid, type=$cleanType, status=OPEN")
            val firestoreResult = postRepository.createPost(postItem)
            if (firestoreResult.isSuccess) {
                Log.d(TAG, "Post saved to Firestore with ID: ${firestoreResult.getOrNull()}")
                _submissionState.value = SubmissionState.Success
                resetForm()
                refreshFeed()
            } else {
                val error = firestoreResult.exceptionOrNull()
                Log.e(TAG, "Failed to save post to Firestore", error)
                // Retain _uploadedMedia so user can retry without re-uploading
                _submissionState.value = SubmissionState.Error(
                    message = "Media uploaded, but failed to save post: ${error?.localizedMessage}. Tap 'Retry' to save without re-uploading.",
                    canRetrySave = true
                )
            }
        }
    }
}
