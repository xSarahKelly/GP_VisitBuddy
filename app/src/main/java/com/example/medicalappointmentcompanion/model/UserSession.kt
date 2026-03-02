package com.example.medicalappointmentcompanion.model

/**
 * Represents a logged-in user session
 */
data class UserSession(
    val username: String,
    val name: String,
    val id: String = username
)
