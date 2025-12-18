package com.example.medicalappointmentcompanion.storage

import android.content.Context
import android.util.Log
import com.example.medicalappointmentcompanion.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private const val LOG_TAG = "ExtractionStorage"

/**
 * Storage handler for MedicalExtraction schema.
 * 
 * Stores extractions as local JSON files.
 * Schema aligned with Calgary-Cambridge model.
 */
class ExtractionStorage(private val context: Context) {
    
    private val extractionsDir: File by lazy {
        File(context.filesDir, "extractions").also { it.mkdirs() }
    }
    
    /**
     * Save extraction to local JSON file
     */
    fun saveExtraction(appointmentId: String, extraction: MedicalExtraction): Boolean {
        return try {
            val file = File(extractionsDir, "$appointmentId.json")
            val json = extractionToJson(extraction)
            file.writeText(json.toString(2))
            Log.d(LOG_TAG, "Saved extraction for: $appointmentId")
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to save extraction", e)
            false
        }
    }
    
    /**
     * Load extraction from local storage
     */
    fun loadExtraction(appointmentId: String): MedicalExtraction? {
        return try {
            val file = File(extractionsDir, "$appointmentId.json")
            if (!file.exists()) return null
            jsonToExtraction(JSONObject(file.readText()))
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to load extraction: $appointmentId", e)
            null
        }
    }
    
    /**
     * Delete extraction
     */
    fun deleteExtraction(appointmentId: String): Boolean {
        return try {
            val file = File(extractionsDir, "$appointmentId.json")
            if (file.exists()) file.delete() else true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to delete extraction: $appointmentId", e)
            false
        }
    }
    
    // ========================================================================
    // JSON SERIALIZATION - Schema Aligned
    // ========================================================================
    
    private fun extractionToJson(extraction: MedicalExtraction): JSONObject {
        return JSONObject().apply {
            // Appointment metadata
            put("appointment_metadata", JSONObject().apply {
                put("date", extraction.appointmentMetadata.date ?: JSONObject.NULL)
                put("doctor_or_clinic", extraction.appointmentMetadata.doctorOrClinic ?: JSONObject.NULL)
                put("recording_duration", extraction.appointmentMetadata.recordingDurationSeconds ?: JSONObject.NULL)
            })
            
            // Medication instructions - MOST IMPORTANT
            put("medication_instructions", JSONArray().apply {
                extraction.medicationInstructions.forEach { med ->
                    put(JSONObject().apply {
                        put("medicine_name", med.medicineName)
                        put("dosage", med.dosage ?: "")
                        put("frequency", med.frequency ?: "")
                        put("duration", med.duration ?: "")
                        put("special_instructions", med.specialInstructions ?: "")
                        put("verbatim_quote", med.verbatimQuote ?: "")
                    })
                }
            })
            
            // Tests and referrals
            put("tests_and_referrals", JSONArray().apply {
                extraction.testsAndReferrals.forEach { test ->
                    put(JSONObject().apply {
                        put("test_or_referral_type", test.testOrReferralType)
                        put("reason_if_stated", test.reasonIfStated ?: "")
                        put("urgency", test.urgency ?: "")
                        put("verbatim_quote", test.verbatimQuote ?: "")
                    })
                }
            })
            
            // Follow-up
            put("follow_up", extraction.followUp?.let { followUp ->
                JSONObject().apply {
                    put("follow_up_required", followUp.followUpRequired)
                    put("timeframe", followUp.timeframe ?: "")
                    put("location_or_method", followUp.locationOrMethod ?: "")
                    put("verbatim_quote", followUp.verbatimQuote ?: "")
                }
            } ?: JSONObject.NULL)
            
            // Safety advice
            put("safety_advice", JSONArray().apply {
                extraction.safetyAdvice.forEach { warning ->
                    put(JSONObject().apply {
                        put("warning", warning.warning)
                        put("verbatim_quote", warning.verbatimQuote ?: "")
                    })
                }
            })
            
            // Additional notes
            put("additional_notes", JSONArray().apply {
                extraction.additionalNotes.forEach { note ->
                    put(note)
                }
            })
            
            // Metadata
            put("extraction_timestamp", extraction.extractionTimestamp)
        }
    }
    
    // ========================================================================
    // JSON DESERIALIZATION
    // ========================================================================
    
    private fun jsonToExtraction(json: JSONObject): MedicalExtraction {
        // Appointment metadata
        val metadataJson = json.optJSONObject("appointment_metadata")
        val metadata = AppointmentMetadata(
            date = metadataJson?.optString("date")?.takeIf { it.isNotEmpty() },
            doctorOrClinic = metadataJson?.optString("doctor_or_clinic")?.takeIf { it.isNotEmpty() },
            recordingDurationSeconds = metadataJson?.optInt("recording_duration")?.takeIf { it > 0 }
        )
        
        // Medication instructions
        val medications = json.optJSONArray("medication_instructions")?.let { arr ->
            (0 until arr.length()).map { i ->
                val med = arr.getJSONObject(i)
                MedicationInstruction(
                    medicineName = med.getString("medicine_name"),
                    dosage = med.optString("dosage").takeIf { it.isNotEmpty() },
                    frequency = med.optString("frequency").takeIf { it.isNotEmpty() },
                    duration = med.optString("duration").takeIf { it.isNotEmpty() },
                    specialInstructions = med.optString("special_instructions").takeIf { it.isNotEmpty() },
                    verbatimQuote = med.optString("verbatim_quote").takeIf { it.isNotEmpty() }
                )
            }
        } ?: emptyList()
        
        // Tests and referrals
        val tests = json.optJSONArray("tests_and_referrals")?.let { arr ->
            (0 until arr.length()).map { i ->
                val test = arr.getJSONObject(i)
                TestOrReferral(
                    testOrReferralType = test.getString("test_or_referral_type"),
                    reasonIfStated = test.optString("reason_if_stated").takeIf { it.isNotEmpty() },
                    urgency = test.optString("urgency").takeIf { it.isNotEmpty() },
                    verbatimQuote = test.optString("verbatim_quote").takeIf { it.isNotEmpty() }
                )
            }
        } ?: emptyList()
        
        // Follow-up
        val followUp = json.optJSONObject("follow_up")?.let { fu ->
            FollowUpInstruction(
                followUpRequired = fu.optBoolean("follow_up_required", true),
                timeframe = fu.optString("timeframe").takeIf { it.isNotEmpty() },
                locationOrMethod = fu.optString("location_or_method").takeIf { it.isNotEmpty() },
                verbatimQuote = fu.optString("verbatim_quote").takeIf { it.isNotEmpty() }
            )
        }
        
        // Safety advice
        val safety = json.optJSONArray("safety_advice")?.let { arr ->
            (0 until arr.length()).map { i ->
                val warning = arr.getJSONObject(i)
                SafetyWarning(
                    warning = warning.getString("warning"),
                    verbatimQuote = warning.optString("verbatim_quote").takeIf { it.isNotEmpty() }
                )
            }
        } ?: emptyList()
        
        // Additional notes
        val notes = json.optJSONArray("additional_notes")?.let { arr ->
            (0 until arr.length()).map { i -> arr.getString(i) }
        } ?: emptyList()
        
        return MedicalExtraction(
            appointmentMetadata = metadata,
            medicationInstructions = medications,
            testsAndReferrals = tests,
            followUp = followUp,
            safetyAdvice = safety,
            additionalNotes = notes,
            extractionTimestamp = json.optLong("extraction_timestamp", System.currentTimeMillis())
        )
    }
}

