package com.example.medicalappointmentcompanion.model

import java.util.Calendar

/**
 * Account type: Patient or Carer.
 * Carer can switch to a Patient account (with password verification) to view/record for them.
 */
enum class UserType {
    Patient,
    Carer
}

/**
 * Represents a logged-in user session (active account)
 */
data class UserSession(
    val accountId: String,
    val username: String,
    val displayName: String,
    val userType: UserType,
    val createdByAccountId: String? = null,  // For Patient accounts added by Carer
    val setupComplete: Boolean = true,
    val id: String = accountId
) {
    /** True if this user can switch between accounts (Carer or managed Patient) */
    val canSwitchAccount: Boolean
        get() = userType == UserType.Carer || createdByAccountId != null
}

private fun computeAgeFromDob(dob: String): Int? {
    return try {
        val parts = dob.split("-")
        if (parts.size != 3) return null
        val year = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val day = parts[2].toIntOrNull() ?: return null
        val today = Calendar.getInstance()
        var years = today.get(Calendar.YEAR) - year
        val hasNotHadBirthday = today.get(Calendar.MONTH) + 1 < month ||
            (today.get(Calendar.MONTH) + 1 == month && today.get(Calendar.DAY_OF_MONTH) < day)
        if (hasNotHadBirthday) years--
        years.takeIf { it >= 0 }
    } catch (_: Exception) { null }
}

/**
 * Summary of an account (for account switcher and home screen)
 */
data class AccountInfo(
    val accountId: String,
    val username: String,
    val displayName: String,
    val userType: UserType,
    val dateOfBirth: String? = null,
    val currentMedications: String? = null
) {
    /** Age in years, computed from dateOfBirth (yyyy-MM-dd). Null if DOB missing or invalid. */
    val age: Int?
        get() = dateOfBirth?.let { computeAgeFromDob(it) }
}
