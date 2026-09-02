package com.wopro.app

import android.app.Application
import android.os.Build
import com.wopro.app.data.local.AppDatabase
import com.wopro.app.data.repository.WOProRepository
import com.wopro.app.security.EncryptionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}

class WOProApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Seed super user admin/admin on first launch (idempotent).
        container.appScope.launch {
            try {
                container.repository.seedAdminUser()
                if (!container.encryptionManager.hasPassword(WOProRepository.ADMIN_EMAIL)) {
                    container.encryptionManager.savePasswordHash(WOProRepository.ADMIN_EMAIL, "admin")
                }
            } catch (t: Throwable) {
                // Never crash startup because of seeding.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    android.util.Log.e("WOProApp", "Seed admin failed", t)
                }
            }
        }
    }
}
