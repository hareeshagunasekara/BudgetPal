package com.example.budgetpal.budgetpal.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.budgetpal.budgetpal.data.model.User

class UserPreferences(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "user_preferences",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveUser(user: User) {
        with(sharedPreferences.edit()) {
            putString(KEY_USER_ID, user.id)
            putString(KEY_USERNAME, user.username)
            putString(KEY_EMAIL, user.email)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getUser(): User? {
        val userId = sharedPreferences.getString(KEY_USER_ID, null)
        val username = sharedPreferences.getString(KEY_USERNAME, null)
        val email = sharedPreferences.getString(KEY_EMAIL, null)

        return if (userId != null && username != null && email != null) {
            User(userId, username, email)
        } else {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearUser() {
        with(sharedPreferences.edit()) {
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }
} 