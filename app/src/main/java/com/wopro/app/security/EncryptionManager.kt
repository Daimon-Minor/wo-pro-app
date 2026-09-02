package com.wopro.app.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

class EncryptionManager(context: Context) {

    private val appContext = context.applicationContext

    private val masterKey: MasterKey = MasterKey.Builder(appContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        appContext,
        "wopro_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val dbPassphrase: String by lazy {
        prefs.getString(KEY_DB_PASSPHRASE, null) ?: generateRandomPassphrase().also {
            prefs.edit().putString(KEY_DB_PASSPHRASE, it).apply()
        }
    }

    fun saveAuthToken(token: String) = prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    fun getAuthToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)
    fun saveUserId(id: Long) = prefs.edit().putLong(KEY_USER_ID, id).apply()
    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)
    fun clearSession() = prefs.edit().remove(KEY_AUTH_TOKEN).remove(KEY_USER_ID).apply()
    fun isLoggedIn(): Boolean = getAuthToken() != null

    fun savePasswordHash(email: String, password: String) {
        val salt = generateSalt()
        val hash = sha256("$salt::$email::$password")
        prefs.edit().putString("pw_$email", "$salt:$hash").apply()
    }

    fun verifyPassword(email: String, password: String): Boolean {
        val stored = prefs.getString("pw_$email", null) ?: return false
        val parts = stored.split(":", limit = 2)
        if (parts.size != 2) return false
        val (salt, hash) = parts
        return sha256("$salt::$email::$password") == hash
    }

    fun hasPassword(email: String): Boolean = prefs.getString("pw_$email", null) != null

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun generateRandomPassphrase(): String {
        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789"
        val rand = SecureRandom()
        return (0 until 40).joinToString("") { alphabet[rand.nextInt(alphabet.length)].toString() }
    }

    private fun sha256(input: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_DB_PASSPHRASE = "db_passphrase"
        private const val KEY_AUTH_TOKEN = "auth_" + "token"
        private const val KEY_USER_ID = "user_id"
    }
}