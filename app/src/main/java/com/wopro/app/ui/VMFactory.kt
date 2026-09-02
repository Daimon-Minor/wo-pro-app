package com.wopro.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wopro.app.data.repository.WOProRepository
import com.wopro.app.security.EncryptionManager

/**
 * Tiny ViewModel factory for manual DI. Tries, in order:
 *  1. constructor(WOProRepository, EncryptionManager)
 *  2. constructor(WOProRepository)
 *  3. no-arg constructor
 */
class VMFactory(
    private val repo: WOProRepository,
    private val encryption: EncryptionManager? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return try {
            if (encryption != null) {
                try {
                    modelClass.getConstructor(WOProRepository::class.java, EncryptionManager::class.java)
                        .newInstance(repo, encryption)
                } catch (e: NoSuchMethodException) {
                    modelClass.getConstructor(WOProRepository::class.java).newInstance(repo)
                }
            } else {
                modelClass.getConstructor(WOProRepository::class.java).newInstance(repo)
            }
        } catch (e: NoSuchMethodException) {
            modelClass.getDeclaredConstructor().newInstance()
        }
    }
}
