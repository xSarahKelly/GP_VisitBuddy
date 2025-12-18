package com.example.medicalappointmentcompanion.storage

import android.content.Context
import android.util.Log
import com.example.medicalappointmentcompanion.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private const val LOG_TAG = "LocalStorage"

/**
 * Local storage manager for appointments and recordings
 * 
 * All data is stored locally on device - no cloud, no APIs.
 * Uses JSON for simple persistence without external dependencies.
 * 
 * Schema aligned with Calgary-Cambridge model extraction.
 */
class LocalStorage(private val context: Context) {
    
    private val storageDir: File by lazy {
        File(context.filesDir, "appointments").also { it.mkdirs() }
    }
    
    private val audioDir: File by lazy {
        File(context.filesDir, "audio").also { it.mkdirs() }
    }
    
    private val modelDir: File by lazy {
        File(context.filesDir, "models").also { it.mkdirs() }
    }
    
    /**
     * Get the directory for storing whisper models
     */
    fun getModelDirectory(): File = modelDir
    
    /**
     * Get path for a model file
     */
    fun getModelPath(modelName: String): String {
        return File(modelDir, modelName).absolutePath
    }
    
    /**
     * Check if a model file exists
     */
    fun modelExists(modelName: String): Boolean {
        return File(modelDir, modelName).exists()
    }
    
    /**
     * Get the directory for audio recordings
     */
    fun getAudioDirectory(): File = audioDir
    
    /**
     * Create a new audio file path for recording
     */
    fun createAudioFilePath(appointmentId: String): String {
        return File(audioDir, "${appointmentId}.wav").absolutePath
    }
    
    /**
     * Save an appointment to local storage
     */
    fun saveAppointment(appointment: Appointment): Boolean {
        return try {
            val file = File(storageDir, "${appointment.id}.json")
            file.writeText(appointmentToJson(appointment).toString(2))
            Log.d(LOG_TAG, "Saved appointment: ${appointment.id}")
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to save appointment", e)
            false
        }
    }
    
    /**
     * Load an appointment by ID
     */
    fun loadAppointment(id: String): Appointment? {
        return try {
            val file = File(storageDir, "$id.json")
            if (!file.exists()) return null
            jsonToAppointment(JSONObject(file.readText()))
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to load appointment: $id", e)
            null
        }
    }
    
    /**
     * Load all appointments
     */
    fun loadAllAppointments(): List<Appointment> {
        return try {
            storageDir.listFiles { file -> file.extension == "json" }
                ?.mapNotNull { file ->
                    try {
                        jsonToAppointment(JSONObject(file.readText()))
                    } catch (e: Exception) {
                        Log.w(LOG_TAG, "Failed to parse appointment: ${file.name}", e)
                        null
                    }
                }
                ?.sortedByDescending { it.dateTime }
                ?: emptyList()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to load appointments", e)
            emptyList()
        }
    }
    
    /**
     * Delete an appointment and its audio file
     */
    fun deleteAppointment(id: String): Boolean {
        return try {
            val jsonFile = File(storageDir, "$id.json")
            val audioFile = File(audioDir, "$id.wav")
            
            var success = true
            if (jsonFile.exists()) {
                success = jsonFile.delete() && success
            }
            if (audioFile.exists()) {
                success = audioFile.delete() && success
            }
            
            Log.d(LOG_TAG, "Deleted appointment: $id, success=$success")
            success
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to delete appointment: $id", e)
            false
        }
    }
    
    /**
     * Get total storage used by appointments (in bytes)
     */
    fun getStorageUsed(): Long {
        val jsonSize = storageDir.listFiles()?.sumOf { it.length() } ?: 0L
        val audioSize = audioDir.listFiles()?.sumOf { it.length() } ?: 0L
        return jsonSize + audioSize
    }
    
    /**
     * Clear all local data (use with caution!)
     */
    fun clearAllData(): Boolean {
        return try {
            storageDir.listFiles()?.forEach { it.delete() }
            audioDir.listFiles()?.forEach { it.delete() }
            Log.d(LOG_TAG, "Cleared all local data")
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to clear data", e)
            false
        }
    }
    
    // ========================================================================
    // JSON Serialization
    // ========================================================================
    
    private fun appointmentToJson(appointment: Appointment): JSONObject {
        return JSONObject().apply {
            put("id", appointment.id)
            put("title", appointment.title)
            put("doctorName", appointment.doctorName)
            put("specialty", appointment.specialty)
            put("location", appointment.location)
            put("dateTime", appointment.dateTime)
            put("durationMs", appointment.durationMs)
            put("audioFilePath", appointment.audioFilePath)
            put("notes", appointment.notes)
            put("status", appointment.status.name)
            
            appointment.transcription?.let { put("transcription", transcriptionToJson(it)) }
            appointment.extraction?.let { put("extraction", extractionToJson(it)) }
        }
    }
    
    private fun transcriptionToJson(transcription: Transcription): JSONObject {
        return JSONObject().apply {
            put("fullText", transcription.fullText)
            put("language", transcription.language)
            put("processedAt", transcription.processedAt)
            put("segments", JSONArray().apply {
                transcription.segments.forEach { segment ->
                    put(JSONObject().apply {
                        put("text", segment.text)
                        put("startMs", segment.startMs)
                        put("endMs", segment.endMs)
                        segment.speaker?.let { put("speaker", it.name) }
                    })
                }
            })
        }
    }
    
    private fun extractionToJson(extraction: MedicalExtraction): JSONObject {
        return JSONObject().apply {
            // Appointment metadata
            put("appointment_metadata", JSONObject().apply {
                put("date", extraction.appointmentMetadata.date ?: JSONObject.NULL)
                put("doctor_or_clinic", extraction.appointmentMetadata.doctorOrClinic ?: JSONObject.NULL)
                put("recording_duration", extraction.appointmentMetadata.recordingDurationSeconds ?: JSONObject.NULL)
            })
            
            // Medication instructions
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
            put("follow_up", extraction.followUp?.let { fu ->
                JSONObject().apply {
                    put("follow_up_required", fu.followUpRequired)
                    put("timeframe", fu.timeframe ?: "")
                    put("location_or_method", fu.locationOrMethod ?: "")
                    put("verbatim_quote", fu.verbatimQuote ?: "")
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
                extraction.additionalNotes.forEach { note -> put(note) }
            })
            
            put("extraction_timestamp", extraction.extractionTimestamp)
        }
    }
    
    // ========================================================================
    // JSON Deserialization
    // ========================================================================
    
    private fun jsonToAppointment(json: JSONObject): Appointment {
        return Appointment(
            id = json.getString("id"),
            title = json.getString("title"),
            doctorName = json.optString("doctorName").takeIf { it.isNotEmpty() },
            specialty = json.optString("specialty").takeIf { it.isNotEmpty() },
            location = json.optString("location").takeIf { it.isNotEmpty() },
            dateTime = json.getLong("dateTime"),
            durationMs = json.optLong("durationMs", 0),
            audioFilePath = json.optString("audioFilePath").takeIf { it.isNotEmpty() },
            notes = json.optString("notes").takeIf { it.isNotEmpty() },
            status = AppointmentStatus.valueOf(json.optString("status", "DRAFT")),
            transcription = json.optJSONObject("transcription")?.let { jsonToTranscription(it) },
            extraction = json.optJSONObject("extraction")?.let { jsonToExtraction(it) }
        )
    }
    
    private fun jsonToTranscription(json: JSONObject): Transcription {
        val segments = json.optJSONArray("segments")?.let { arr ->
            (0 until arr.length()).map { i ->
                val seg = arr.getJSONObject(i)
                TranscriptionSegmentData(
                    text = seg.getString("text"),
                    startMs = seg.getLong("startMs"),
                    endMs = seg.getLong("endMs"),
                    speaker = seg.optString("speaker").takeIf { it.isNotEmpty() }
                        ?.let { Speaker.valueOf(it) }
                )
            }
        } ?: emptyList()
        
        return Transcription(
            fullText = json.getString("fullText"),
            segments = segments,
            language = json.optString("language", "en"),
            processedAt = json.optLong("processedAt", System.currentTimeMillis())
        )
    }
    
    private fun jsonToExtraction(json: JSONObject): MedicalExtraction {
        // Appointment metadata
        val metadataJson = json.optJSONObject("appointment_metadata")
        val metadata = AppointmentMetadata(
            date = metadataJson?.optString("date")?.takeIf { it.isNotEmpty() && it != "null" },
            doctorOrClinic = metadataJson?.optString("doctor_or_clinic")?.takeIf { it.isNotEmpty() && it != "null" },
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
