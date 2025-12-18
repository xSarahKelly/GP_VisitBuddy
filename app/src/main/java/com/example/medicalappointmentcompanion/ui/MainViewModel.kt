package com.example.medicalappointmentcompanion.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicalappointmentcompanion.audio.AudioRecorder
import com.example.medicalappointmentcompanion.audio.WaveHelper
import com.example.medicalappointmentcompanion.extraction.SchemaGuidedExtractor
import com.example.medicalappointmentcompanion.model.AppState
import com.example.medicalappointmentcompanion.model.Appointment
import com.example.medicalappointmentcompanion.model.AppointmentStatus
import com.example.medicalappointmentcompanion.model.Transcription
import com.example.medicalappointmentcompanion.model.TranscriptionSegmentData
import com.example.medicalappointmentcompanion.storage.LocalStorage
import com.example.medicalappointmentcompanion.whisper.WhisperContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

private const val LOG_TAG = "MainViewModel"

/**
 * ViewModel for the main screen
 * 
 * Manages:
 * - Whisper model loading
 * - Audio recording
 * - Transcription
 * - Appointment storage
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val storage = LocalStorage(application)
    private val recorder = AudioRecorder()
    
    private var whisperContext: WhisperContext? = null
    private var recordingTimerJob: Job? = null
    
    private var currentAppointmentId: String? = null
    private var currentAudioFile: File? = null
    
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()
    
    init {
        loadAppointments()
        autoLoadModel()
    }
    
    /**
     * Automatically try to load model from known locations
     */
    private fun autoLoadModel() {
        viewModelScope.launch {
            val modelLocations = listOf(
                // App's internal storage (preferred)
                File(storage.getModelDirectory(), "ggml-tiny.bin"),
                File(storage.getModelDirectory(), "ggml-base.bin"),
                File(storage.getModelDirectory(), "ggml-small.bin"),
                // Common external locations
                File("/sdcard/Download/ggml-tiny.bin"),
                File("/sdcard/Download/ggml-base.bin"),
                File("/storage/emulated/0/Download/ggml-tiny.bin"),
                File("/storage/emulated/0/Download/ggml-base.bin"),
                // Data local tmp (where we pushed it via adb)
                File("/data/local/tmp/ggml-tiny.bin")
            )
            
            for (location in modelLocations) {
                if (location.exists() && location.length() > 0) {
                    Log.d(LOG_TAG, "Found model at: ${location.absolutePath}")
                    loadModel(location.absolutePath)
                    return@launch
                }
            }
            
            Log.d(LOG_TAG, "No model found in common locations")
        }
    }
    
    // ========================================================================
    // Model Management
    // ========================================================================
    
    /**
     * Load the whisper model from storage
     * 
     * @param modelPath Path to the .bin model file
     */
    fun loadModel(modelPath: String) {
        viewModelScope.launch {
            _state.update { it.copy(isModelLoading = true, modelError = null) }
            
            try {
                Log.d(LOG_TAG, "Loading model from: $modelPath")
                
                withContext(Dispatchers.IO) {
                    val file = File(modelPath)
                    if (!file.exists()) {
                        throw IllegalArgumentException("Model file not found: $modelPath")
                    }
                    
                    whisperContext = WhisperContext.createFromFile(modelPath)
                }
                
                val systemInfo = WhisperContext.getSystemInfo()
                Log.d(LOG_TAG, "Model loaded. System info: $systemInfo")
                
                _state.update { 
                    it.copy(
                        isModelLoaded = true, 
                        isModelLoading = false,
                        systemInfo = systemInfo
                    ) 
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to load model", e)
                _state.update { 
                    it.copy(
                        isModelLoading = false,
                        modelError = "Failed to load model: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Load model from app assets
     */
    fun loadModelFromAsset(assetPath: String) {
        viewModelScope.launch {
            _state.update { it.copy(isModelLoading = true, modelError = null) }
            
            try {
                Log.d(LOG_TAG, "Loading model from asset: $assetPath")
                
                withContext(Dispatchers.IO) {
                    whisperContext = WhisperContext.createFromAsset(
                        getApplication<Application>().assets,
                        assetPath
                    )
                }
                
                val systemInfo = WhisperContext.getSystemInfo()
                Log.d(LOG_TAG, "Model loaded from asset. System info: $systemInfo")
                
                _state.update { 
                    it.copy(
                        isModelLoaded = true, 
                        isModelLoading = false,
                        systemInfo = systemInfo
                    ) 
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to load model from asset", e)
                _state.update { 
                    it.copy(
                        isModelLoading = false,
                        modelError = "Failed to load model: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Get the model storage directory
     */
    fun getModelDirectory(): File = storage.getModelDirectory()
    
    /**
     * Check if a model exists in storage
     */
    fun modelExists(modelName: String): Boolean = storage.modelExists(modelName)
    
    /**
     * Retry model loading (called from UI)
     */
    fun retryModelLoad() {
        autoLoadModel()
    }
    
    // ========================================================================
    // Recording
    // ========================================================================
    
    /**
     * Start recording a new appointment
     */
    fun startRecording(title: String = "New Appointment") {
        if (!_state.value.isModelLoaded) {
            _state.update { it.copy(errorMessage = "Please load a model first") }
            return
        }
        
        viewModelScope.launch {
            try {
                // Create new appointment
                currentAppointmentId = UUID.randomUUID().toString()
                val audioPath = storage.createAudioFilePath(currentAppointmentId!!)
                currentAudioFile = File(audioPath)
                
                val appointment = Appointment(
                    id = currentAppointmentId!!,
                    title = title,
                    audioFilePath = audioPath,
                    status = AppointmentStatus.DRAFT
                )
                
                storage.saveAppointment(appointment)
                
                _state.update { 
                    it.copy(
                        isRecording = true, 
                        recordingDuration = 0,
                        currentAppointment = appointment
                    ) 
                }
                
                // Start audio recording
                recorder.startRecording(currentAudioFile!!) { error ->
                    Log.e(LOG_TAG, "Recording error", error)
                    _state.update { 
                        it.copy(
                            errorMessage = "Recording error: ${error.message}",
                            isRecording = false
                        ) 
                    }
                }
                
                // Start duration timer
                startRecordingTimer()
                
                Log.d(LOG_TAG, "Recording started: $currentAppointmentId")
                
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to start recording", e)
                _state.update { it.copy(errorMessage = "Failed to start recording: ${e.message}") }
            }
        }
    }
    
    /**
     * Stop recording and transcribe
     */
    fun stopRecording() {
        viewModelScope.launch {
            recordingTimerJob?.cancel()
            
            val audioData = recorder.stopRecording()
            val duration = _state.value.recordingDuration
            
            _state.update { it.copy(isRecording = false, isTranscribing = true) }
            
            if (audioData != null && audioData.isNotEmpty()) {
                transcribeAudio(audioData, duration)
            } else {
                _state.update { 
                    it.copy(
                        isTranscribing = false,
                        errorMessage = "No audio recorded"
                    ) 
                }
            }
        }
    }
    
    /**
     * Cancel recording without saving
     */
    fun cancelRecording() {
        viewModelScope.launch {
            recordingTimerJob?.cancel()
            recorder.cancelRecording()
            
            // Delete the draft appointment
            currentAppointmentId?.let { storage.deleteAppointment(it) }
            
            currentAppointmentId = null
            currentAudioFile = null
            
            _state.update { 
                it.copy(
                    isRecording = false, 
                    recordingDuration = 0,
                    currentAppointment = null
                ) 
            }
            
            loadAppointments()
        }
    }
    
    private fun startRecordingTimer() {
        recordingTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _state.update { it.copy(recordingDuration = it.recordingDuration + 1000) }
            }
        }
    }
    
    // ========================================================================
    // Transcription
    // ========================================================================
    
    private suspend fun transcribeAudio(audioData: FloatArray, durationMs: Long) {
        try {
            // Diagnostic: Check audio data quality
            val minVal = audioData.minOrNull() ?: 0f
            val maxVal = audioData.maxOrNull() ?: 0f
            val avgAbs = audioData.map { kotlin.math.abs(it) }.average()
            val nonZeroCount = audioData.count { it != 0f }
            
            Log.d(LOG_TAG, "Audio diagnostics:")
            Log.d(LOG_TAG, "  - Samples: ${audioData.size}")
            Log.d(LOG_TAG, "  - Duration: ${audioData.size / 16000f}s")
            Log.d(LOG_TAG, "  - Min: $minVal, Max: $maxVal")
            Log.d(LOG_TAG, "  - Avg absolute: $avgAbs")
            Log.d(LOG_TAG, "  - Non-zero samples: $nonZeroCount (${(nonZeroCount * 100f / audioData.size)}%)")
            
            if (avgAbs < 0.001f) {
                Log.w(LOG_TAG, "WARNING: Audio appears to be silent or very quiet!")
            }
            
            val context = whisperContext ?: throw IllegalStateException("Model not loaded")
            
            val segments = withContext(Dispatchers.Default) {
                context.transcribeWithSegments(audioData)
            }
            
            val fullText = segments.joinToString(" ") { it.text }
            
            val transcription = Transcription(
                fullText = fullText,
                segments = segments.map { 
                    TranscriptionSegmentData(it.text, it.startMs, it.endMs) 
                }
            )
            
            // Extract medical info using schema-guided extraction
            // (Calgary-Cambridge model aligned, no inference, exact phrases only)
            val extraction = SchemaGuidedExtractor.extract(
                transcript = fullText,
                recordingDurationSeconds = (durationMs / 1000).toInt()
            )
            
            // Update appointment
            val updatedAppointment = _state.value.currentAppointment?.copy(
                transcription = transcription,
                extraction = extraction,
                durationMs = durationMs,
                status = AppointmentStatus.PROCESSED
            )
            
            if (updatedAppointment != null) {
                storage.saveAppointment(updatedAppointment)
            }
            
            _state.update { 
                it.copy(
                    isTranscribing = false,
                    currentAppointment = updatedAppointment
                ) 
            }
            
            loadAppointments()
            
            Log.d(LOG_TAG, "Transcription complete: ${fullText.length} chars")
            
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Transcription failed", e)
            _state.update { 
                it.copy(
                    isTranscribing = false,
                    errorMessage = "Transcription failed: ${e.message}"
                ) 
            }
        }
    }
    
    /**
     * Transcribe an existing audio file
     */
    fun transcribeFile(file: File) {
        viewModelScope.launch {
            _state.update { it.copy(isTranscribing = true) }
            
            try {
                val audioData = withContext(Dispatchers.IO) {
                    WaveHelper.decodeWaveFile(file)
                }
                
                val duration = WaveHelper.getDuration(audioData.size) * 1000
                transcribeAudio(audioData, duration.toLong())
                
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to transcribe file", e)
                _state.update { 
                    it.copy(
                        isTranscribing = false,
                        errorMessage = "Failed to transcribe: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    // ========================================================================
    // Appointments
    // ========================================================================
    
    private fun loadAppointments() {
        viewModelScope.launch {
            val appointments = withContext(Dispatchers.IO) {
                storage.loadAllAppointments()
            }
            _state.update { it.copy(appointments = appointments) }
        }
    }
    
    fun selectAppointment(id: String) {
        viewModelScope.launch {
            val appointment = withContext(Dispatchers.IO) {
                storage.loadAppointment(id)
            }
            _state.update { it.copy(currentAppointment = appointment) }
        }
    }
    
    fun deleteAppointment(id: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                storage.deleteAppointment(id)
            }
            if (_state.value.currentAppointment?.id == id) {
                _state.update { it.copy(currentAppointment = null) }
            }
            loadAppointments()
        }
    }
    
    fun clearCurrentAppointment() {
        _state.update { it.copy(currentAppointment = null) }
    }
    
    // ========================================================================
    // Utility
    // ========================================================================
    
    fun clearError() {
        _state.update { it.copy(errorMessage = null, modelError = null) }
    }
    
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            whisperContext?.release()
        }
    }
}

