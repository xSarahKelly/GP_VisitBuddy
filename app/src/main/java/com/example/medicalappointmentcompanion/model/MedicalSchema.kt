package com.example.medicalappointmentcompanion.model

/**
 * Medical Extraction Schema
 * 
 * Grounded in the Calgary-Cambridge model (stages 4 and 5)
 * 
 * Design Principles:
 * - Extract only EXPLICITLY stated information (no inference)
 * - Copy EXACT phrases from the transcript
 * - Prioritise patient recall and safety
 * - Remain explainable and auditable
 * - Be usable by patients and carers
 * 
 * This schema justifies NOT using generative summarisation.
 * 
 * EXPLICIT EXCLUSIONS (not extracted):
 * - Diagnoses
 * - Interpretations
 * - Treatment reasoning
 * - Inferred intent
 * - Medical opinions
 */
data class MedicalExtraction(
    val appointmentMetadata: AppointmentMetadata = AppointmentMetadata(),
    val medicationInstructions: List<MedicationInstruction> = emptyList(),
    val testsAndReferrals: List<TestOrReferral> = emptyList(),
    val followUp: FollowUpInstruction? = null,
    val safetyAdvice: List<SafetyWarning> = emptyList(),
    val additionalNotes: List<String> = emptyList(),
    val extractionTimestamp: Long = System.currentTimeMillis()
)

/**
 * Appointment Metadata
 * 
 * Purpose: Context, NOT clinical decision-making
 * - Optional fields
 * - Not evaluated for accuracy
 * - Useful for user orientation
 */
data class AppointmentMetadata(
    val date: String? = null,
    val doctorOrClinic: String? = null,
    val recordingDurationSeconds: Int? = null
)

/**
 * Medication Instructions - MOST IMPORTANT
 * 
 * Highest patient recall failure rate
 * Highest safety risk
 * Strongest justification in literature
 * 
 * Extract ONLY if explicitly spoken, e.g.:
 * "Take amoxicillin 500 milligrams three times a day for seven days"
 * 
 * @param medicineName Exact name as spoken
 * @param dosage Exact dosage as spoken (e.g., "500 milligrams", "two tablets")
 * @param frequency Exact frequency as spoken (e.g., "three times a day", "every 8 hours")
 * @param duration Exact duration as spoken (e.g., "for seven days", "until finished")
 * @param specialInstructions Exact instructions (e.g., "with food", "before bed")
 * @param verbatimQuote The exact quote from transcript for auditability
 */
data class MedicationInstruction(
    val medicineName: String,
    val dosage: String? = null,
    val frequency: String? = null,
    val duration: String? = null,
    val specialInstructions: String? = null,
    val verbatimQuote: String? = null
)

/**
 * Tests and Referrals
 * 
 * Examples:
 * - "I'm referring you for a blood test"
 * - "You'll need an X-ray urgently"
 * 
 * No guessing if urgency is not stated.
 * 
 * @param testOrReferralType Exact type as spoken
 * @param reasonIfStated Only if EXPLICITLY stated
 * @param urgency Only if EXPLICITLY stated (null if not mentioned)
 * @param verbatimQuote The exact quote from transcript
 */
data class TestOrReferral(
    val testOrReferralType: String,
    val reasonIfStated: String? = null,
    val urgency: String? = null,
    val verbatimQuote: String? = null
)

/**
 * Follow-Up Instructions
 * 
 * Examples:
 * - "Come back in two weeks"
 * - "Book a follow-up with reception"
 * 
 * @param followUpRequired True if follow-up explicitly mentioned
 * @param timeframe Exact timeframe as spoken (e.g., "in two weeks")
 * @param locationOrMethod Exact location/method as spoken
 * @param verbatimQuote The exact quote from transcript
 */
data class FollowUpInstruction(
    val followUpRequired: Boolean = true,
    val timeframe: String? = null,
    val locationOrMethod: String? = null,
    val verbatimQuote: String? = null
)

/**
 * Safety Advice / Red Flags - ACADEMICALLY STRONG
 * 
 * Often missed by AI scribes - strong academic justification
 * 
 * Examples:
 * - "If you develop a fever"
 * - "Go to A&E if the pain gets worse"
 * 
 * @param warning Exact warning phrase as spoken
 * @param verbatimQuote The exact quote from transcript
 */
data class SafetyWarning(
    val warning: String,
    val verbatimQuote: String? = null
)

/**
 * Extraction confidence levels
 * Used internally to track extraction quality
 */
enum class ExtractionConfidence {
    HIGH,    // Clear, unambiguous match
    MEDIUM,  // Partial match, may need review
    LOW      // Weak match, flagged for user review
}

/**
 * Wrapper for extracted items with confidence
 */
data class ExtractedItem<T>(
    val item: T,
    val confidence: ExtractionConfidence,
    val sourceStartIndex: Int,
    val sourceEndIndex: Int
)

