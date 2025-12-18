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

/**
 * Extracted medical information from transcription
 */
data class MedicalInfo(
    val diagnoses: List<String> = emptyList(),
    val medications: List<Medication> = emptyList(),
    val instructions: List<String> = emptyList(),
    val followUpDate: String? = null,
    val vitalSigns: VitalSigns? = null,
    val symptoms: List<String> = emptyList(),
    val tests: List<MedicalTest> = emptyList(),
    val warnings: List<String> = emptyList(),
    val questions: List<String> = emptyList()
)

/**
 * Medication information
 */
data class Medication(
    val name: String,
    val dosage: String? = null,
    val frequency: String? = null,
    val duration: String? = null,
    val instructions: String? = null,
    val isNew: Boolean = true
)

/**
 * Vital signs recorded during appointment
 */
data class VitalSigns(
    val bloodPressure: String? = null,
    val heartRate: Int? = null,
    val temperature: Float? = null,
    val weight: Float? = null,
    val height: Float? = null,
    val oxygenSaturation: Int? = null
)

/**
 * Medical test information
 */
data class MedicalTest(
    val name: String,
    val type: TestType = TestType.OTHER,
    val scheduledDate: String? = null,
    val location: String? = null,
    val preparation: String? = null,
    val result: String? = null
)

/**
 * Types of medical tests
 */
enum class TestType {
    BLOOD_TEST,
    IMAGING,
    PROCEDURE,
    SCREENING,
    OTHER
}

