package com.wopro.app

import android.app.Application
import com.wopro.app.data.local.AppDatabase
import com.wopro.app.data.repository.WOProRepository
import com.wopro.app.security.EncryptionManager

/**
 * Manual DI container. Instantiated once in Application.onCreate.
 * Avoids Hilt/Koin so builds stay fast and deterministic.
 */
class AppContainer(application: Application) {

    val encryptionManager: EncryptionManager by lazy { EncryptionManager(application) }

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(application, encryptionManager.dbPassphrase)
    }

    val repository: WOProRepository by lazy { WOProRepository(database) }
}

class WOProApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
