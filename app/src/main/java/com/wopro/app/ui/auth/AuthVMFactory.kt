package com.wopro.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wopro.app.data.repository.WOProRepository
import com.wopro.app.security.EncryptionManager

/** Factory that can build AuthViewModel (needs repo + encryption manager). */
class AuthVMFactory(
    private val repo: WOProRepository,
    private val encryption: EncryptionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(repo, encryption) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
