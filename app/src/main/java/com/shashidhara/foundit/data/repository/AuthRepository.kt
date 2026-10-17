package com.shashidhara.foundit.data.repository

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
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

    companion object {
        private const val TAG = "FoundIT_Auth"
        private const val FALLBACK_WEB_CLIENT_ID = "240086993355-hh49c99jdif89av7vb5gj4umiepnshin.apps.googleusercontent.com"
    }

    private fun Context.findActivity(): Activity? {
        var ctx: Context? = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    /**
     * Signs in with Google using modern Android Credential Manager and exchanges with Firebase Auth.
     * Follows the recommended fallback pattern:
     * 1. Try authorized accounts first (seamless sign-in for returning users).
     * 2. If NoCredentialException is thrown, fall back to all eligible accounts on the device.
     */
    suspend fun signInWithGoogle(context: Context): Result<UserProfile> {
        val activityContext = context.findActivity() ?: context
        val serverClientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            FALLBACK_WEB_CLIENT_ID
        }.ifBlank { FALLBACK_WEB_CLIENT_ID }

        Log.d(TAG, "Starting Google Sign-In with serverClientId: $serverClientId")

        return try {
            val credentialManager = CredentialManager.create(activityContext)

            val response = try {
                // Step 1: Attempt sign-in with already-authorized accounts (instant sign-in)
                Log.d(TAG, "Attempting sign-in with setFilterByAuthorizedAccounts(true)")
                val authorizedOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val authorizedRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(authorizedOption)
                    .build()

                credentialManager.getCredential(request = authorizedRequest, context = activityContext)
            } catch (e: NoCredentialException) {
                // Step 2: Fallback to all eligible Google accounts present on the device
                Log.d(TAG, "No pre-authorized account found. Falling back to setFilterByAuthorizedAccounts(false)", e)
                val allAccountsOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val allAccountsRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(allAccountsOption)
                    .build()

                credentialManager.getCredential(request = allAccountsRequest, context = activityContext)
            }

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                Log.d(TAG, "Google ID token received. Exchanging with Firebase Authentication...")
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user is null after authentication")

                Log.d(TAG, "Firebase Authentication successful: uid=${firebaseUser.uid}, email=${firebaseUser.email}")
                val profile = syncUserProfile(firebaseUser)
                _authState.value = AuthState.Authenticated(profile)
                Result.success(profile)
            } else {
                val errorMsg = "Unsupported credential received: ${credential.type}"
                Log.e(TAG, errorMsg)
                Result.failure(IllegalArgumentException(errorMsg))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.i(TAG, "User cancelled Google Sign-In: ${e.message}", e)
            Result.failure(Exception("Sign-in was cancelled."))
        } catch (e: NoCredentialException) {
            Log.e(TAG, "NoCredentialException: No Google accounts available on this device", e)
            Result.failure(Exception("No Google accounts found on this device. Please ensure a Google account is added in device Settings."))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "GetCredentialException [${e.type}]: ${e.message}", e)
            Result.failure(Exception("Google Sign-In failed [${e.type}]: ${e.message ?: "Please check Google Play Services."}", e))
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "FirebaseAuthException [${e.errorCode}]: ${e.message}", e)
            Result.failure(Exception("Firebase Authentication failed [${e.errorCode}]: ${e.localizedMessage}", e))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In: ${e.message}", e)
            val message = e.localizedMessage ?: "Google Sign-In failed: ${e::class.simpleName}"
            Result.failure(Exception(message, e))
        }
    }

    suspend fun signOut(context: Context) {
        auth.signOut()
        try {
            val activityContext = context.findActivity() ?: context
            val credentialManager = CredentialManager.create(activityContext)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Log.d(TAG, "CredentialManager state cleared successfully.")
        } catch (e: Exception) {
            Log.w(TAG, "Non-critical error clearing credential state: ${e.message}", e)
        }
        _authState.value = AuthState.Unauthenticated
    }
}
