package com.shashidhara.foundit.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.shashidhara.foundit.data.model.PostItem
import com.shashidhara.foundit.data.model.UserProfile
import com.shashidhara.foundit.data.repository.CloudinaryRepository
import com.shashidhara.foundit.data.repository.CloudinaryUploadResult
import com.shashidhara.foundit.data.repository.PostRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
    val error: String? = null
)

class PostViewModel(
    private val postRepository: PostRepository = PostRepository(),
    private val cloudinaryRepository: CloudinaryRepository = CloudinaryRepository()
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

    init {
        loadInitialPosts()
    }

    fun onSearchQueryChanged(query: String) {
        if (_feedUiState.value.searchQuery == query) return
        _feedUiState.update { it.copy(searchQuery = query) }
        loadInitialPosts(search = query, category = _feedUiState.value.selectedCategory)
    }

    fun onCategorySelected(category: String) {
        if (_feedUiState.value.selectedCategory == category) return
        _feedUiState.update { it.copy(selectedCategory = category) }
        loadInitialPosts(search = _feedUiState.value.searchQuery, category = category)
    }

    fun refreshFeed() {
        loadInitialPosts(
            search = _feedUiState.value.searchQuery,
            category = _feedUiState.value.selectedCategory
        )
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
        category: String = _feedUiState.value.selectedCategory
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

    // Form states
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

    // Retained media upload result for safe retries without re-uploading
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

    fun onMediaSelected(uri: Uri, mimeType: String?, isVideo: Boolean) {
        _selectedMediaUri.value = uri
        _selectedMediaMimeType.value = mimeType
        _isVideo.value = isVideo
        // If a new media file was selected, clear the previous upload result
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
     * Submits the post:
     * 1. Validates inputs and authentication.
     * 2. Uploads media to Cloudinary if not already uploaded.
     * 3. Saves post to Firestore.
     * If Firestore saving fails, the uploaded media result is preserved so the user can retry without re-uploading.
     */
    fun submitPost(context: Context, currentUser: UserProfile? = null) {
        val authUser = FirebaseAuth.getInstance().currentUser
        if (authUser == null) {
            _submissionState.value = SubmissionState.Error(
                message = "Please sign in with Google to create a post.",
                canRetrySave = false
            )
            return
        }

        val ownerUid = authUser.uid
        val trimmedTitle = _title.value.trim()
        val trimmedLocation = _location.value.trim()

        if (trimmedTitle.isBlank()) {
            _submissionState.value = SubmissionState.Error(
                message = "Please enter an item title.",
                canRetrySave = false
            )
            return
        }

        if (trimmedLocation.isBlank()) {
            _submissionState.value = SubmissionState.Error(
                message = "Please specify the location.",
                canRetrySave = false
            )
            return
        }

        val mediaUri = _selectedMediaUri.value
        val existingUpload = _uploadedMedia.value

        if (mediaUri == null && existingUpload == null) {
            _submissionState.value = SubmissionState.Error(
                message = "Please select an image or video.",
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
                // Note: _uploadedMedia is retained here so user can retry saving without re-uploading!
                _submissionState.value = SubmissionState.Error(
                    message = "Media uploaded, but failed to save post: ${error?.localizedMessage}. Tap 'Retry' to save without re-uploading.",
                    canRetrySave = true
                )
            }
        }
    }
}
