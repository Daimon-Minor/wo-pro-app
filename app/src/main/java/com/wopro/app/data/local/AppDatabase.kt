package com.wopro.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SupportOpenHelperFactory

/**
 * Encrypted local database powered by SQLCipher.
 * The passphrase is derived from EncryptedSharedPreferences (see EncryptionManager),
 * so the DB file on disk is unreadable without the device-bound key.
 */
@Database(
    entities = [
        UserEntity::class,
        WorkOrderEntity::class,
        ProjectEntity::class,
        AuditReportEntity::class,
        AuditItemEntity::class,
        MeterReadingEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun workOrderDao(): WorkOrderDao
    abstract fun projectDao(): ProjectDao
    abstract fun auditDao(): AuditDao
    abstract fun meterDao(): MeterDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Creates (or returns) the SQLCipher-encrypted database.
         * `passphrase` must come from a device-bound encrypted store, never hardcoded.
         */
        fun getInstance(context: Context, passphrase: String): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wopro.db"
                )
                    .openHelperFactory(SupportOpenHelperFactory(passphrase.toCharArray()))
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        /** Test-only / rebuild helper: wipes the DB file. */
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
