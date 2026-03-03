package com.example.medicalappointmentcompanion.auth

import android.content.Context
import android.util.Log
import com.example.medicalappointmentcompanion.model.AccountInfo
import com.example.medicalappointmentcompanion.model.UserSession
import com.example.medicalappointmentcompanion.model.UserType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

private const val LOG_TAG = "AuthRepository"
private const val PREFS_NAME = "gp_visitbuddy_auth"
private const val KEY_ACCOUNTS = "accounts"
private const val KEY_CURRENT_ACCOUNT_ID = "current_account_id"

private data class StoredAccount(
    val accountId: String,
    val username: String,
    val displayName: String,
    val passwordHash: String,
    val userType: UserType,
    val createdByAccountId: String? = null,  // Carer who added this Patient account
    val dateOfBirth: String? = null,
    val currentMedications: String? = null,
    val setupComplete: Boolean = true  // false for new sign-ups until they complete setup
)

/**
 * Multi-account auth. Users can have Patient and Carer accounts.
 * Carer verifies patient's password and switches to that account to access their data.
 */
class AuthRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _userSession = MutableStateFlow<UserSession?>(null)
    val userSession: StateFlow<UserSession?> = _userSession.asStateFlow()

    init {
        loadCurrentSession()
    }

    private fun loadCurrentSession() {
        val accountId = prefs.getString(KEY_CURRENT_ACCOUNT_ID, null) ?: return
        val account = loadAccounts().find { it.accountId == accountId } ?: return
        _userSession.value = toSession(account)
    }

    fun getCurrentSession(): UserSession? = _userSession.value

    /**
     * Returns accounts the user should see:
     * - Carer: own account + accounts they created
     * - Managed Patient: same list as their Carer (Carer + accounts Carer created)
     */
    fun getAccountsForUser(accountId: String): List<AccountInfo> {
        val accounts = loadAccounts()
        val current = accounts.find { it.accountId == accountId } ?: return emptyList()
        val listOwnerId = current.createdByAccountId ?: accountId
        return accounts
            .filter { acc -> acc.accountId == listOwnerId || acc.createdByAccountId == listOwnerId }
            .map { AccountInfo(it.accountId, it.username, it.displayName, it.userType, it.dateOfBirth, it.currentMedications) }
    }

    fun getAllAccounts(): List<AccountInfo> {
        return loadAccounts().map {
            AccountInfo(it.accountId, it.username, it.displayName, it.userType, it.dateOfBirth, it.currentMedications)
        }
    }

    private fun toSession(account: StoredAccount): UserSession = UserSession(
        accountId = account.accountId,
        username = account.username,
        displayName = account.displayName,
        userType = account.userType,
        createdByAccountId = account.createdByAccountId,
        setupComplete = account.setupComplete
    )

    fun signIn(username: String, password: String): Boolean {
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()
        if (trimmedUsername.isBlank() || trimmedPassword.isBlank()) return false

        val account = loadAccounts().find { it.username.equals(trimmedUsername, ignoreCase = true) }
            ?: return false
        if (account.passwordHash != hashPassword(trimmedPassword)) {
            Log.d(LOG_TAG, "Sign in failed: wrong password")
            return false
        }

        prefs.edit().putString(KEY_CURRENT_ACCOUNT_ID, account.accountId).apply()
        _userSession.value = toSession(account)
        Log.d(LOG_TAG, "Signed in: ${account.username} (${account.userType.name})")
        return true
    }

    fun signUp(userType: UserType, username: String, password: String): Boolean {
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()
        if (trimmedUsername.isBlank() || trimmedPassword.isBlank()) return false

        val accounts = loadAccounts().toMutableList()
        if (accounts.any { it.username.equals(trimmedUsername, ignoreCase = true) }) {
            Log.d(LOG_TAG, "Sign up failed: username already exists")
            return false
        }

        val accountId = UUID.randomUUID().toString()
        val account = StoredAccount(
            accountId = accountId,
            username = trimmedUsername,
            displayName = trimmedUsername,
            passwordHash = hashPassword(trimmedPassword),
            userType = userType,
            setupComplete = false
        )
        accounts.add(account)
        saveAccounts(accounts)
        prefs.edit().putString(KEY_CURRENT_ACCOUNT_ID, accountId).apply()
        _userSession.value = toSession(account)
        Log.d(LOG_TAG, "Signed up: $trimmedUsername (${userType.name})")
        return true
    }

    fun addAccount(
        userType: UserType,
        username: String,
        password: String,
        displayName: String,
        createdByAccountId: String? = null,
        dateOfBirth: String? = null,
        currentMedications: String? = null
    ) {
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()
        val trimmedDisplay = displayName.trim()
        if (trimmedUsername.isBlank() || trimmedPassword.isBlank()) return

        val accounts = loadAccounts().toMutableList()
        if (accounts.any { it.username.equals(trimmedUsername, ignoreCase = true) }) {
            throw IllegalArgumentException("Username already exists")
        }

        val accountId = UUID.randomUUID().toString()
        val account = StoredAccount(
            accountId = accountId,
            username = trimmedUsername,
            displayName = trimmedDisplay.ifEmpty { trimmedUsername },
            passwordHash = hashPassword(trimmedPassword),
            userType = userType,
            createdByAccountId = createdByAccountId,
            dateOfBirth = dateOfBirth?.trim().takeIf { it?.isNotEmpty() == true },
            currentMedications = currentMedications?.trim().takeIf { it?.isNotEmpty() == true }
        )
        accounts.add(account)
        saveAccounts(accounts)
        prefs.edit().putString(KEY_CURRENT_ACCOUNT_ID, accountId).apply()
        _userSession.value = toSession(account)
        Log.d(LOG_TAG, "Added account: $trimmedUsername (${userType.name})")
    }

    fun completeAccountSetup(displayName: String, dateOfBirth: String? = null, currentMedications: String? = null) {
        val session = _userSession.value ?: return
        val accounts = loadAccounts().toMutableList()
        val idx = accounts.indexOfFirst { it.accountId == session.accountId }
        if (idx < 0) return
        val acc = accounts[idx]
        accounts[idx] = acc.copy(
            displayName = displayName.trim().ifEmpty { acc.username },
            dateOfBirth = dateOfBirth?.trim().takeIf { it?.isNotEmpty() == true },
            currentMedications = currentMedications?.trim().takeIf { it?.isNotEmpty() == true },
            setupComplete = true
        )
        saveAccounts(accounts)
        _userSession.value = session.copy(displayName = accounts[idx].displayName, setupComplete = true)
    }

    fun updateDisplayName(displayName: String) {
        val session = _userSession.value ?: return
        val accounts = loadAccounts().toMutableList()
        val idx = accounts.indexOfFirst { it.accountId == session.accountId }
        if (idx < 0) return
        val acc = accounts[idx]
        accounts[idx] = acc.copy(displayName = displayName.trim().ifEmpty { acc.username })
        saveAccounts(accounts)
        _userSession.value = session.copy(displayName = accounts[idx].displayName)
    }

    fun updateProfile(displayName: String, dateOfBirth: String?, currentMedications: String?) {
        val session = _userSession.value ?: return
        val accounts = loadAccounts().toMutableList()
        val idx = accounts.indexOfFirst { it.accountId == session.accountId }
        if (idx < 0) return
        val acc = accounts[idx]
        accounts[idx] = acc.copy(
            displayName = displayName.trim().ifEmpty { acc.username },
            dateOfBirth = dateOfBirth?.trim().takeIf { it?.isNotEmpty() == true },
            currentMedications = currentMedications?.trim().takeIf { it?.isNotEmpty() == true }
        )
        saveAccounts(accounts)
        _userSession.value = session.copy(displayName = accounts[idx].displayName)
    }

    fun signOut() {
        prefs.edit().remove(KEY_CURRENT_ACCOUNT_ID).apply()
        _userSession.value = null
        Log.d(LOG_TAG, "Signed out")
    }

    fun switchAccount(accountId: String) {
        val account = loadAccounts().find { it.accountId == accountId } ?: return
        prefs.edit().putString(KEY_CURRENT_ACCOUNT_ID, accountId).apply()
        _userSession.value = toSession(account)
        Log.d(LOG_TAG, "Switched to account: ${account.username}")
    }

    fun verifyAccountPassword(accountId: String, password: String): Boolean {
        val account = loadAccounts().find { it.accountId == accountId } ?: return false
        return account.passwordHash == hashPassword(password.trim())
    }

    fun deleteAccount(accountId: String) {
        val accounts = loadAccounts().toMutableList()
        val idx = accounts.indexOfFirst { it.accountId == accountId }
        if (idx < 0) return
        accounts.removeAt(idx)
        saveAccounts(accounts)
        val currentId = prefs.getString(KEY_CURRENT_ACCOUNT_ID, null)
        if (currentId == accountId) {
            val switchTo = accounts.firstOrNull()
            if (switchTo != null) {
                prefs.edit().putString(KEY_CURRENT_ACCOUNT_ID, switchTo.accountId).apply()
                _userSession.value = toSession(switchTo)
            } else {
                prefs.edit().remove(KEY_CURRENT_ACCOUNT_ID).apply()
                _userSession.value = null
            }
        }
        Log.d(LOG_TAG, "Deleted account: $accountId")
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun loadAccounts(): List<StoredAccount> {
        return try {
            val json = prefs.getString(KEY_ACCOUNTS, "[]") ?: "[]"
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.optJSONObject(i) ?: return@mapNotNull null
                StoredAccount(
                    accountId = obj.getString("accountId"),
                    username = obj.getString("username"),
                    displayName = obj.optString("displayName", obj.getString("username")),
                    passwordHash = obj.optString("passwordHash", "").ifEmpty { return@mapNotNull null },
                    userType = obj.optString("userType", "Patient").let {
                        try { UserType.valueOf(it) } catch (_: Exception) { UserType.Patient }
                    },
                    createdByAccountId = obj.optString("createdByAccountId", "").takeIf { it.isNotEmpty() },
                    dateOfBirth = obj.optString("dateOfBirth", "").takeIf { it.isNotEmpty() },
                    currentMedications = obj.optString("currentMedications", "").takeIf { it.isNotEmpty() },
                    setupComplete = obj.optBoolean("setupComplete", true)
                )
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to load accounts", e)
            emptyList()
        }
    }

    private fun saveAccounts(accounts: List<StoredAccount>) {
        val arr = JSONArray()
        accounts.forEach { acc ->
            arr.put(JSONObject().apply {
                put("accountId", acc.accountId)
                put("username", acc.username)
                put("displayName", acc.displayName)
                put("passwordHash", acc.passwordHash)
                put("userType", acc.userType.name)
                acc.createdByAccountId?.let { put("createdByAccountId", it) }
                acc.dateOfBirth?.let { put("dateOfBirth", it) }
                acc.currentMedications?.let { put("currentMedications", it) }
                if (!acc.setupComplete) put("setupComplete", false)
            })
        }
        prefs.edit().putString(KEY_ACCOUNTS, arr.toString()).apply()
    }
}
