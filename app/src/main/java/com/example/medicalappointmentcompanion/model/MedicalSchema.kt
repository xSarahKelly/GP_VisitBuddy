package com.example.medicalappointmentcompanion.model

/** What we pull from transcript – meds, tests, follow-ups, safety. Only what's actually said. */
data class MedicalExtraction(
    val appointmentMetadata: AppointmentMetadata = AppointmentMetadata(),
    val medicationInstructions: List<MedicationInstruction> = emptyList(),
    val testsAndReferrals: List<TestOrReferral> = emptyList(),
    val followUp: FollowUpInstruction? = null,
    val safetyAdvice: List<SafetyWarning> = emptyList(),
    val additionalNotes: List<String> = emptyList(),
    val extractionTimestamp: Long = System.currentTimeMillis()
)

/** date, doctor, duration – just for context */
data class AppointmentMetadata(
    val date: String? = null,
    val doctorOrClinic: String? = null,
    val recordingDurationSeconds: Int? = null
)

/** med name, dose, how often, how long, special instructions. Keep verbatim quote. */
data class MedicationInstruction(
    val medicineName: String,
    val dosage: String? = null,
    val frequency: String? = null,
    val duration: String? = null,
    val specialInstructions: String? = null,
    val verbatimQuote: String? = null
)

/** blood test, X-ray, etc. Urgency only if they said it. */
data class TestOrReferral(
    val testOrReferralType: String,
    val reasonIfStated: String? = null,
    val destinationIfStated: String? = null,
    val urgency: String? = null,
    val verbatimQuote: String? = null
)

/** come back in X weeks, book with reception, etc */
data class FollowUpInstruction(
    val followUpRequired: Boolean = true,
    val timeframe: String? = null,
    val locationOrMethod: String? = null,
    val verbatimQuote: String? = null
)

/** if you get X go to A&E, stop taking if, etc */
data class SafetyWarning(
    val warning: String,
    val verbatimQuote: String? = null
)


