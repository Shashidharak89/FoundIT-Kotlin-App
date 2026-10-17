package com.shashidhara.foundit.data.repository

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.shashidhara.foundit.R
import com.shashidhara.foundit.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val profile: UserProfile) : AuthState
    data class Error(val message: String) : AuthState
}

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val repositoryScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                repositoryScope.launch {
                    try {
                        val profile = syncUserProfile(user)
                        _authState.value = AuthState.Authenticated(profile)
                    } catch (e: Exception) {
                        // Fallback to basic profile from FirebaseUser if Firestore read fails initially (e.g. offline)
                        val fallbackProfile = UserProfile(
                            uid = user.uid,
                            name = user.displayName ?: "FoundIT Member",
                            email = user.email ?: "",
                            photoUrl = user.photoUrl?.toString() ?: "",
                            role = "USER",
                            createdAt = Timestamp.now()
                        )
                        _authState.value = AuthState.Authenticated(fallbackProfile)
                    }
                }
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * Creates or safely maintains the user profile document in Firestore:
     * - New user: sets document with role="USER" and server timestamp.
     * - Existing user: preserves existing role (never overwritten) and createdAt timestamp.
     */
    suspend fun syncUserProfile(user: FirebaseUser): UserProfile {
        val userDocRef = firestore.collection("users").document(user.uid)
        val snapshot = userDocRef.get().await()

        return if (!snapshot.exists()) {
            val newProfileData = hashMapOf(
                "uid" to user.uid,
                "name" to (user.displayName ?: "FoundIT Member"),
                "email" to (user.email ?: ""),
                "photoUrl" to (user.photoUrl?.toString() ?: ""),
                "role" to "USER",
                "createdAt" to FieldValue.serverTimestamp()
            )
            userDocRef.set(newProfileData).await()
            UserProfile(
                uid = user.uid,
                name = user.displayName ?: "FoundIT Member",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString() ?: "",
                role = "USER",
                createdAt = Timestamp.now()
            )
        } else {
            val existingRole = snapshot.getString("role") ?: "USER"
            val existingCreatedAt = snapshot.getTimestamp("createdAt")

            val updates = mutableMapOf<String, Any>()
            val currentDisplayName = user.displayName
            if (!currentDisplayName.isNullOrEmpty() && currentDisplayName != snapshot.getString("name")) {
                updates["name"] = currentDisplayName
            }
            val currentEmail = user.email
            if (!currentEmail.isNullOrEmpty() && currentEmail != snapshot.getString("email")) {
                updates["email"] = currentEmail
            }
            val currentPhoto = user.photoUrl?.toString()
            if (!currentPhoto.isNullOrEmpty() && currentPhoto != snapshot.getString("photoUrl")) {
                updates["photoUrl"] = currentPhoto
            }

            if (updates.isNotEmpty()) {
                userDocRef.update(updates).await()
            }

            UserProfile(
                uid = user.uid,
                name = snapshot.getString("name") ?: (user.displayName ?: "FoundIT Member"),
                email = snapshot.getString("email") ?: (user.email ?: ""),
                photoUrl = snapshot.getString("photoUrl") ?: (user.photoUrl?.toString() ?: ""),
                role = existingRole,
                createdAt = existingCreatedAt
            )
        }
    }

    /**
     * Signs in with Google using modern Android Credential Manager and exchanges with Firebase Auth.
     */
    suspend fun signInWithGoogle(context: Context): Result<UserProfile> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val serverClientId = context.getString(R.string.default_web_client_id)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase authenticated user is null")
                val profile = syncUserProfile(firebaseUser)
                _authState.value = AuthState.Authenticated(profile)
                Result.success(profile)
            } else {
                throw IllegalArgumentException("Unsupported credential received: ${credential.type}")
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Sign-in cancelled by user."))
        } catch (e: NoCredentialException) {
            Result.failure(Exception("No Google accounts found. Please add a Google account to your device."))
        } catch (e: Exception) {
            val message = e.localizedMessage ?: "Google Sign-In failed. Please verify network and Google Services setup."
            Result.failure(Exception(message, e))
        }
    }

    suspend fun signOut(context: Context) {
        auth.signOut()
        try {
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
            // Non-critical credential clear failure
        }
        _authState.value = AuthState.Unauthenticated
    }
}
