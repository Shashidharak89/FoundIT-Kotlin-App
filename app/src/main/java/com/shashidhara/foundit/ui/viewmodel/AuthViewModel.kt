package com.shashidhara.foundit.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shashidhara.foundit.data.repository.AuthRepository
import com.shashidhara.foundit.data.repository.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    val authState: StateFlow<AuthState> = repository.authState

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _isSigningIn.value = true
            _errorMessage.value = null
            val result = repository.signInWithGoogle(context)
            _isSigningIn.value = false
            result.onFailure { error ->
                _errorMessage.value = error.message
            }
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            repository.signOut(context)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
