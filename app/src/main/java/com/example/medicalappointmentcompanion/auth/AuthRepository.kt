package com.example.medicalappointmentcompanion.auth

import android.content.Context
import android.util.Log
import com.example.medicalappointmentcompanion.model.UserSession
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

private const val LOG_TAG = "AuthRepository"
private const val PREFS_NAME = "gp_visitbuddy_auth"
private const val KEY_USERS = "users"
private const val KEY_CURRENT_USERNAME = "current_username"

private data class StoredUser(
    val username: String,
    val name: String,
    val passwordHash: String
)

/**
 * Local-only auth - no backend. Stores users in SharedPreferences.
 * Sign up creates an account with username + password; login verifies credentials.
 */
class AuthRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Get the currently logged-in user, if any
     */
    fun getCurrentSession(): UserSession? {
        val username = prefs.getString(KEY_CURRENT_USERNAME, null) ?: return null
        return loadUsers().find { it.username.equals(username, ignoreCase = true) }
            ?.let { UserSession(username = it.username, name = it.name) }
    }

    /**
     * Sign up a new user (name + username + password). Returns the session or null if username already exists.
     */
    fun signUp(name: String, username: String, password: String): UserSession? {
        val trimmedName = name.trim()
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()
        if (trimmedName.isBlank() || trimmedUsername.isBlank() || trimmedPassword.isBlank()) return null

        val users = loadUsers().toMutableList()
        if (users.any { it.username.equals(trimmedUsername, ignoreCase = true) }) {
            Log.d(LOG_TAG, "Sign up failed: username already exists")
            return null
        }

        val passwordHash = hashPassword(trimmedPassword)
        val storedUser = StoredUser(
            username = trimmedUsername,
            name = trimmedName,
            passwordHash = passwordHash
        )
        users.add(storedUser)
        saveUsers(users)
        prefs.edit().putString(KEY_CURRENT_USERNAME, trimmedUsername).apply()
        Log.d(LOG_TAG, "Signed up: $trimmedUsername")
        return UserSession(username = trimmedUsername, name = trimmedName)
    }

    /**
     * Log in with username and password. Returns session if credentials match, null otherwise.
     */
    fun login(username: String, password: String): UserSession? {
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()
        if (trimmedUsername.isBlank() || trimmedPassword.isBlank()) return null

        val storedUser = loadUsers().find { it.username.equals(trimmedUsername, ignoreCase = true) }
            ?: return null

        val passwordHash = hashPassword(trimmedPassword)
        if (storedUser.passwordHash != passwordHash) {
            Log.d(LOG_TAG, "Login failed: wrong password")
            return null
        }

        prefs.edit().putString(KEY_CURRENT_USERNAME, trimmedUsername).apply()
        Log.d(LOG_TAG, "Logged in: $trimmedUsername")
        return UserSession(username = storedUser.username, name = storedUser.name)
    }

    /**
     * Log out the current user
     */
    fun logout() {
        prefs.edit().remove(KEY_CURRENT_USERNAME).apply()
        Log.d(LOG_TAG, "Logged out")
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun loadUsers(): List<StoredUser> {
        return try {
            val json = prefs.getString(KEY_USERS, "[]") ?: "[]"
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.optJSONObject(i) ?: return@mapNotNull null
                StoredUser(
                    username = obj.getString("username"),
                    name = obj.getString("name"),
                    passwordHash = obj.optString("passwordHash", "").ifEmpty { return@mapNotNull null }
                )
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to load users", e)
            emptyList()
        }
    }

    private fun saveUsers(users: List<StoredUser>) {
        val arr = JSONArray()
        users.forEach { user ->
            arr.put(JSONObject().apply {
                put("username", user.username)
                put("name", user.name)
                put("passwordHash", user.passwordHash)
            })
        }
        prefs.edit().putString(KEY_USERS, arr.toString()).apply()
    }
}
