package com.shashidhara.foundit.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shashidhara.foundit.data.model.PostItem
import com.shashidhara.foundit.data.model.UserProfile
import com.shashidhara.foundit.data.repository.CloudinaryRepository
import com.shashidhara.foundit.data.repository.CloudinaryUploadResult
import com.shashidhara.foundit.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SubmissionState {
    data object Idle : SubmissionState
    data class Uploading(val progress: Float) : SubmissionState
    data object SavingToFirestore : SubmissionState
    data object Success : SubmissionState
    data class Error(val message: String, val canRetrySave: Boolean) : SubmissionState
}

class PostViewModel(
    private val postRepository: PostRepository = PostRepository(),
    private val cloudinaryRepository: CloudinaryRepository = CloudinaryRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "FoundIT_PostVM"
    }

    // Community feed posts from Firestore
    val posts: StateFlow<List<PostItem>> = postRepository.getPostsFlow()
        .catch { e ->
            Log.e(TAG, "Error collecting posts feed", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
    fun submitPost(context: Context, currentUser: UserProfile?) {
        if (currentUser == null) {
            _submissionState.value = SubmissionState.Error(
                message = "Please sign in with Google to create a post.",
                canRetrySave = false
            )
            return
        }

        if (_title.value.isBlank()) {
            _submissionState.value = SubmissionState.Error(
                message = "Please enter an item title.",
                canRetrySave = false
            )
            return
        }

        if (_location.value.isBlank()) {
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

            // Step 2: Save to Firestore
            _submissionState.value = SubmissionState.SavingToFirestore
            val postItem = PostItem(
                title = _title.value,
                description = _description.value,
                category = _category.value,
                location = _location.value,
                postType = _postType.value,
                userId = currentUser.uid,
                authorName = currentUser.name.ifBlank { "Community Member" },
                authorPhotoUrl = currentUser.photoUrl,
                imageUrl = mediaResult.secureUrl,
                imagePublicId = mediaResult.publicId,
                videoUrl = if (mediaResult.resourceType == "video") mediaResult.secureUrl else "",
                mediaUrl = mediaResult.secureUrl,
                mediaPublicId = mediaResult.publicId,
                resourceType = mediaResult.resourceType
            )

            val firestoreResult = postRepository.createPost(postItem)
            if (firestoreResult.isSuccess) {
                Log.d(TAG, "Post saved to Firestore with ID: ${firestoreResult.getOrNull()}")
                _submissionState.value = SubmissionState.Success
                resetForm()
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
