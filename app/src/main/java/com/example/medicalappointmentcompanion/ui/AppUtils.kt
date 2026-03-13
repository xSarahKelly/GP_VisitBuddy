package com.example.medicalappointmentcompanion.ui

import java.text.SimpleDateFormat
import java.util.*

object AppUtils {
    private const val MIN_PASSWORD_LENGTH = 5

    const val PASSWORD_INSTRUCTION = "Password must be at least 5 characters long and contain at least one capital letter"

    /**
     * Validates password: at least 5 characters and must contain a capital letter.
     * Returns error message or null if valid.
     */
    fun validatePassword(password: String): String? {
        if (password.length < MIN_PASSWORD_LENGTH) {
            return "Password must be at least 5 characters"
        }
        if (!password.any { it.isUpperCase() }) {
            return "Password must contain a capital letter"
        }
        return null
    }

    fun formatDuration(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        val hours = ms / (1000 * 60 * 60)

        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
