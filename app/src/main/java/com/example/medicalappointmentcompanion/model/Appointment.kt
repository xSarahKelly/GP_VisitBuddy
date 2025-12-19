package com.example.medicalappointmentcompanion.model

import java.util.UUID

/**
 * Represents a medical appointment recording
 */
data class Appointment(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val doctorName: String? = null,
    val specialty: String? = null,
    val location: String? = null,
    val dateTime: Long = System.currentTimeMillis(),
    val durationMs: Long = 0,
    val audioFilePath: String? = null,
    val transcription: Transcription? = null,
    val extraction: MedicalExtraction? = null,  // Calgary-Cambridge aligned schema
    val notes: String? = null,
    val status: AppointmentStatus = AppointmentStatus.DRAFT
)

/**
 * Status of an appointment recording
 */
enum class AppointmentStatus {
    DRAFT,          // Recording in progress or not yet processed
    TRANSCRIBED,    // Audio has been transcribed
    PROCESSED,      // Medical info has been extracted
    ARCHIVED        // Completed and archived
}

/**
 * Represents a complete transcription
 */
data class Transcription(
    val fullText: String,
    val segments: List<TranscriptionSegmentData> = emptyList(),
    val language: String = "en",
    val processedAt: Long = System.currentTimeMillis()
)

/**
 * A segment of transcription with timing
 */
data class TranscriptionSegmentData(
    val text: String,
    val startMs: Long,
    val endMs: Long,
    val speaker: Speaker? = null
)

/**
 * Speaker identification (for future diarization support)
 */
enum class Speaker {
    DOCTOR,
    PATIENT,
    OTHER,
    UNKNOWN
}

// Medical extraction models are defined in MedicalSchema.kt
// Using Calgary-Cambridge aligned schema (no diagnoses - patient safety)

