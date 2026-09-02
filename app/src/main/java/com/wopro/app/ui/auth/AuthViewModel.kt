package com.wopro.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wopro.app.data.local.UserEntity
import com.wopro.app.data.repository.WOProRepository
import com.wopro.app.security.EncryptionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val loggedInUser: UserEntity? = null,
    val pendingEmail: String? = null,
    val otpSent: Boolean = false
)

/**
 * Demo auth backed by the encrypted local DB + salted hashes (no plaintext).
 * In production swap this for the REST API: POST /auth/login, store returned JWT.
 */
class AuthViewModel(
    private val repo: WOProRepository,
    private val encryption: EncryptionManager
) : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui

    fun login(email: String, password: String) {
        viewModelScope.launch {
            if (email.isBlank() || password.isBlank()) {
                _ui.value = AuthUiState(error = "Email and password are required")
                return@launch
            }
            _ui.value = AuthUiState(isLoading = true)
            val normalized = email.trim().lowercase()
            val user = repo.findUserByEmail(normalized)
            if (user == null) {
                _ui.value = AuthUiState(error = "Account not found. Please register first.")
            } else if (!encryption.hasPassword(normalized)) {
                _ui.value = AuthUiState(error = "No password set for this account. Use Forgot Password.")
            } else if (!encryption.verifyPassword(normalized, password)) {
                _ui.value = AuthUiState(error = "Incorrect password. Try again.")
            } else {
                encryption.saveAuthToken("demo-${System.currentTimeMillis()}")
                encryption.saveUserId(user.id)
                _ui.value = AuthUiState(loggedInUser = user)
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _ui.value = AuthUiState(error = "Full name is required")
                return@launch
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _ui.value = AuthUiState(error = "Enter a valid email address")
                return@launch
            }
            if (password.length < 6) {
                _ui.value = AuthUiState(error = "Password must be at least 6 characters")
                return@launch
            }
            _ui.value = AuthUiState(isLoading = true)
            val normalized = email.trim().lowercase()
            if (repo.findUserByEmail(normalized) != null) {
                _ui.value = AuthUiState(error = "An account with this email already exists. Please sign in.")
                return@launch
            }
            val user = UserEntity(name = name.trim(), email = normalized, role = "Engineer")
            val id = repo.createUser(user)
            encryption.savePasswordHash(normalized, password)
            encryption.saveAuthToken("demo-${System.currentTimeMillis()}")
            encryption.saveUserId(id)
            _ui.value = AuthUiState(loggedInUser = user.copy(id = id))
        }
    }

    /** Demo OTP: any 6-digit code works after sendOtp(). */
    fun sendOtp(email: String) {
        viewModelScope.launch {
            val normalized = email.trim().lowercase()
            if (repo.findUserByEmail(normalized) == null) {
                _ui.value = AuthUiState(error = "No account found with that email")
                return@launch
            }
            _ui.value = AuthUiState(pendingEmail = normalized, otpSent = true)
        }
    }

    fun verifyOtp(code: String, email: String) {
        viewModelScope.launch {
            if (code.length != 6) {
                _ui.value = AuthUiState(error = "OTP must be 6 digits", pendingEmail = email)
                return@launch
            }
            val user = repo.findUserByEmail(email)
            if (user == null) {
                _ui.value = AuthUiState(error = "User not found")
            } else {
                encryption.saveAuthToken("demo-${System.currentTimeMillis()}")
                encryption.saveUserId(user.id)
                _ui.value = AuthUiState(loggedInUser = user)
            }
        }
    }

    fun resetPassword(email: String, newPassword: String) {
        viewModelScope.launch {
            val normalized = email.trim().lowercase()
            if (newPassword.length < 6) {
                _ui.value = AuthUiState(error = "New password must be at least 6 characters")
                return@launch
            }
            val user = repo.findUserByEmail(normalized)
            if (user == null) {
                _ui.value = AuthUiState(error = "No account found with that email")
                return@launch
            }
            encryption.savePasswordHash(normalized, newPassword)
            _ui.value = AuthUiState(success = "Password updated. Please sign in.", pendingEmail = normalized)
        }
    }

    fun logout() {
        encryption.clearSession()
    }
}
