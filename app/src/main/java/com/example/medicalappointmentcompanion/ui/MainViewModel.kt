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
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
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
    private val recorder = AudioRecorder(application)
    
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
            // First, check if model already exists in internal storage
            val modelDir = storage.getModelDirectory()
            val modelNames = listOf("ggml-tiny.bin", "ggml-base.bin", "ggml-small.bin")
            
            for (modelName in modelNames) {
                val modelFile = File(modelDir, modelName)
                if (modelFile.exists() && modelFile.length() > 0) {
                    Log.d(LOG_TAG, "Found model at: ${modelFile.absolutePath}")
                    loadModel(modelFile.absolutePath)
                    return@launch
                }
            }
            
            // If not found, try to copy from assets
            try {
                val assets = getApplication<Application>().assets
                for (modelName in modelNames) {
                    try {
                        assets.open(modelName).use { inputStream ->
                            val outputFile = File(modelDir, modelName)
                            outputFile.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                            Log.d(LOG_TAG, "Copied model from assets to: ${outputFile.absolutePath}")
                            loadModel(outputFile.absolutePath)
                            return@launch
                        }
                    } catch (e: Exception) {
                        // Model not in assets, try next one
                        Log.d(LOG_TAG, "Model $modelName not found in assets, trying next")
                    }
                }
            } catch (e: Exception) {
                Log.w(LOG_TAG, "Failed to load model from assets", e)
            }
            
            // Fallback: check external locations
            val externalLocations = listOf(
                File("/sdcard/Download/ggml-tiny.bin"),
                File("/sdcard/Download/ggml-base.bin"),
                File("/storage/emulated/0/Download/ggml-tiny.bin"),
                File("/storage/emulated/0/Download/ggml-base.bin"),
                File("/data/local/tmp/ggml-tiny.bin")
            )
            
            for (location in externalLocations) {
                if (location.exists() && location.length() > 0) {
                    Log.d(LOG_TAG, "Found model at: ${location.absolutePath}")
                    loadModel(location.absolutePath)
                    return@launch
                }
            }
            
            Log.d(LOG_TAG, "No model found in any location, attempting automatic download")
            // Automatically download the model
            try {
                downloadModel("ggml-tiny.bin")
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Exception starting download", e)
                _state.update {
                    it.copy(
                        modelError = "Failed to start download: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Helper function to download from a specific URL
     */
    private suspend fun downloadFromUrl(url: String, outputFile: File) = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
        
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "GP-VisitBuddy/1.0")
            .build()
        
        Log.d(LOG_TAG, "Starting download request to: $url")
        val response = client.newCall(request).execute()
        
        Log.d(LOG_TAG, "Response code: ${response.code}, message: ${response.message}")
        
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "No error body"
            Log.e(LOG_TAG, "Download failed. Response body: $errorBody")
            throw Exception("Failed to download model: HTTP ${response.code} ${response.message}")
        }
        
        val body = response.body
        val contentLength = body?.contentLength() ?: -1L
        val inputStream = body?.byteStream()
        
        if (inputStream == null) {
            throw Exception("Failed to get response body")
        }
        
        FileOutputStream(outputFile).use { outputStream ->
            val buffer = ByteArray(8192)
            var totalBytesRead = 0L
            var bytesRead: Int
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                
                // Update progress
                if (contentLength > 0) {
                    val progress = (totalBytesRead.toFloat() / contentLength).coerceIn(0f, 1f)
                    _state.update { it.copy(modelDownloadProgress = progress) }
                    
                    // Log progress every 10%
                    val progressPercent = (progress * 100).toInt()
                    if (progressPercent % 10 == 0 && progressPercent > 0) {
                        Log.d(LOG_TAG, "Download progress: $progressPercent%")
                    }
                }
            }
        }
        
        Log.d(LOG_TAG, "Model downloaded successfully: ${outputFile.absolutePath}")
    }
    
    /**
     * Download model from HuggingFace automatically
     */
    private fun downloadModel(modelName: String) {
        viewModelScope.launch {
            Log.d(LOG_TAG, "downloadModel() called for: $modelName")
            _state.update { 
                it.copy(
                    isModelDownloading = true,
                    modelDownloadProgress = 0f,
                    modelError = null
                ) 
            }
            
            try {
                val modelDir = storage.getModelDirectory()
                Log.d(LOG_TAG, "Model directory: ${modelDir.absolutePath}")
                val outputFile = File(modelDir, modelName)
                
                // Create directory if it doesn't exist
                if (!modelDir.exists()) {
                    val created = modelDir.mkdirs()
                    Log.d(LOG_TAG, "Created model directory: $created")
                }
                
                // HuggingFace direct download URL - try multiple formats
                val urls = listOf(
                    "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/$modelName",
                    "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/$modelName?download=true"
                )
                
                var lastException: Exception? = null
                for (url in urls) {
                    try {
                        Log.d(LOG_TAG, "Attempting download from: $url")
                        downloadFromUrl(url, outputFile)
                        Log.d(LOG_TAG, "Download successful from: $url")
                        break // Success, exit loop
                    } catch (e: Exception) {
                        Log.w(LOG_TAG, "Download failed from $url: ${e.message}")
                        lastException = e
                        if (url != urls.last()) {
                            Log.d(LOG_TAG, "Trying next URL...")
                            continue
                        }
                    }
                }
                
                if (lastException != null && !outputFile.exists()) {
                    throw lastException ?: Exception("All download URLs failed")
                }
                
                // Verify file was downloaded
                if (!outputFile.exists() || outputFile.length() == 0L) {
                    throw Exception("Downloaded file is empty or missing")
                }
                
                Log.d(LOG_TAG, "Model file size: ${outputFile.length() / (1024 * 1024)} MB")
                
                // Load the downloaded model
                _state.update { 
                    it.copy(
                        isModelDownloading = false,
                        modelDownloadProgress = 1f
                    ) 
                }
                
                loadModel(outputFile.absolutePath)
                
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to download model", e)
                e.printStackTrace()
                val errorMessage = when {
                    e.message?.contains("Unable to resolve host") == true -> 
                        "No internet connection. Please check your network and try again."
                    e.message?.contains("timeout") == true || e.message?.contains("Timeout") == true ->
                        "Download timed out. Please check your internet connection and try again."
                    e.message?.contains("HTTP") == true ->
                        "Server error: ${e.message}. Please try again later."
                    else ->
                        "Failed to download model: ${e.message ?: "Unknown error"}. Please check your internet connection and try again."
                }
                _state.update { 
                    it.copy(
                        isModelDownloading = false,
                        modelDownloadProgress = 0f,
                        modelError = errorMessage
                    ) 
                }
            }
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
                // Check if audio is too quiet (likely silent/blank)
                val maxVal = audioData.maxOrNull() ?: 0f
                val minVal = audioData.minOrNull() ?: 0f
                val maxAmplitude = maxOf(kotlin.math.abs(maxVal), kotlin.math.abs(minVal))
                
                // Normalize to 16-bit range for comparison (audioData is normalized to [-1, 1])
                val maxAmplitudeShort = (maxAmplitude * 32767).toInt()
                
                Log.d(LOG_TAG, "Audio check: max amplitude = $maxAmplitudeShort (normalized: $maxAmplitude)")
                
                if (maxAmplitudeShort < 100) {
                    // Audio is too quiet - likely no actual recording
                    _state.update { 
                        it.copy(
                            isTranscribing = false,
                            errorMessage = "Audio too quiet (amplitude: $maxAmplitudeShort). " +
                                    "Please check microphone permission and speak clearly. " +
                                    "Expected amplitude: 1000-20000 for normal speech."
                        ) 
                    }
                    Log.w(LOG_TAG, "Rejecting recording: amplitude too low ($maxAmplitudeShort)")
                } else {
                    transcribeAudio(audioData, duration)
                }
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

