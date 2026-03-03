package com.example.medicalappointmentcompanion.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicalappointmentcompanion.auth.AuthRepository
import com.example.medicalappointmentcompanion.audio.AudioRecorder
import com.example.medicalappointmentcompanion.audio.WaveHelper
import com.example.medicalappointmentcompanion.extraction.SchemaGuidedExtractor
import com.example.medicalappointmentcompanion.model.AppState
import com.example.medicalappointmentcompanion.model.Appointment
import com.example.medicalappointmentcompanion.model.AppointmentStatus
import com.example.medicalappointmentcompanion.model.Transcription
import com.example.medicalappointmentcompanion.model.TranscriptionSegmentData
import com.example.medicalappointmentcompanion.model.UserType
import com.example.medicalappointmentcompanion.storage.LocalStorage
import com.example.medicalappointmentcompanion.whisper.WhisperContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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
 * - Appointment storage (account-scoped)
 * - Multi-account auth (Patient / Carer with account switching)
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = LocalStorage(application)
    private val authRepository = AuthRepository(application)
    private val recorder = AudioRecorder(application)

    private var whisperContext: WhisperContext? = null
    private var recordingTimerJob: Job? = null

    private var currentAppointmentId: String? = null
    private var currentAudioFile: File? = null

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = AppState()
    )

    init {
        viewModelScope.launch {
            authRepository.userSession.collect { session ->
                _state.update { it.copy(userSession = session) }
                loadAppointments()
            }
        }
        autoLoadModel()
    }

    fun signIn(username: String, password: String) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) { authRepository.signIn(username, password) }
            if (!success) {
                _state.update { it.copy(authError = "Incorrect username or password") }
            } else {
                _state.update { it.copy(authError = null) }
            }
        }
    }

    fun signUp(userType: UserType, username: String, password: String) {
        viewModelScope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    authRepository.signUp(userType, username, password)
                }
                if (!success) {
                    _state.update { it.copy(authError = "Username already exists") }
                } else {
                    _state.update { it.copy(authError = null) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(authError = e.message ?: "Sign up failed") }
            }
        }
    }

    fun addAccount(username: String, password: String, displayName: String, dateOfBirth: String? = null, currentMedications: String? = null) {
        viewModelScope.launch {
            try {
                val session = _state.value.userSession
                val createdBy = if (session?.userType == UserType.Carer) session.accountId else null
                withContext(Dispatchers.IO) {
                    authRepository.addAccount(
                        UserType.Patient,
                        username,
                        password,
                        displayName,
                        createdByAccountId = createdBy,
                        dateOfBirth = dateOfBirth,
                        currentMedications = currentMedications
                    )
                }
                _state.update { it.copy(authError = null) }
            } catch (e: Exception) {
                _state.update { it.copy(authError = e.message ?: "Could not add account") }
            }
        }
    }

    fun completeAccountSetup(displayName: String, dateOfBirth: String? = null, currentMedications: String? = null) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                authRepository.completeAccountSetup(displayName, dateOfBirth, currentMedications)
            }
        }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                storage.deleteAccountData(accountId)
                authRepository.deleteAccount(accountId)
            }
        }
    }

    fun updateDisplayName(displayName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { authRepository.updateDisplayName(displayName) }
        }
    }

    fun updateProfile(displayName: String, dateOfBirth: String?, currentMedications: String?) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                authRepository.updateProfile(displayName, dateOfBirth, currentMedications)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { authRepository.signOut() }
            _state.update { it.copy(userSession = null, authError = null) }
        }
    }

    fun switchAccount(accountId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { authRepository.switchAccount(accountId) }
        }
    }

    /**
     * Verify password for an account and switch to it if correct.
     * Used when Carer accesses another account's data.
     */
    fun verifyAndSwitchAccount(accountId: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                authRepository.verifyAccountPassword(accountId, password)
            }
            if (ok) {
                withContext(Dispatchers.IO) { authRepository.switchAccount(accountId) }
            }
            withContext(Dispatchers.Main.immediate) {
                onResult(ok)
            }
        }
    }

    fun getAccountsForUser() = authRepository.getAccountsForUser(_state.value.userSession?.accountId ?: "")

    fun getAllAccounts() = authRepository.getAllAccounts()

    fun clearAuthError() {
        _state.update { it.copy(authError = null) }
    }

    private fun autoLoadModel() {
        viewModelScope.launch {
            _state.update { it.copy(isModelLoading = true, modelError = null) }
            try {
                val modelDir = storage.getModelDirectory()
                val modelNames = listOf("ggml-small.en.bin", "ggml-base.en.bin", "ggml-tiny.bin", "ggml-base.bin")
                for (modelName in modelNames) {
                    val modelFile = File(modelDir, modelName)
                    if (modelFile.exists() && modelFile.length() > 0) {
                        Log.d(LOG_TAG, "Found model at: ${modelFile.absolutePath}")
                        loadModel(modelFile.absolutePath)
                        return@launch
                    }
                }
                val assets = getApplication<Application>().assets
                for (modelName in modelNames) {
                    try {
                        assets.open(modelName).use { inputStream ->
                            val outputFile = File(modelDir, modelName)
                            modelDir.mkdirs()
                            outputFile.outputStream().use { outputStream ->
                                val buffer = ByteArray(8192)
                                var bytesRead: Int
                                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                    outputStream.write(buffer, 0, bytesRead)
                                }
                            }
                            if (outputFile.exists() && outputFile.length() > 0) {
                                loadModel(outputFile.absolutePath)
                                return@launch
                            }
                        }
                    } catch (_: java.io.FileNotFoundException) { }
                    catch (e: Exception) { Log.w(LOG_TAG, "Error copying model $modelName", e) }
                }
                val externalLocations = listOf(
                    File("/sdcard/Download/ggml-small.en.bin"),
                    File("/sdcard/Download/ggml-base.en.bin"),
                    File("/storage/emulated/0/Download/ggml-small.en.bin")
                )
                for (location in externalLocations) {
                    if (location.exists() && location.length() > 0) {
                        loadModel(location.absolutePath)
                        return@launch
                    }
                }
                _state.update {
                    it.copy(
                        isModelLoading = false,
                        modelError = "Model file not found. Place ggml-small.en.bin in app/src/main/assets/."
                    )
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error in autoLoadModel", e)
                _state.update { it.copy(isModelLoading = false, modelError = "Error loading model: ${e.message}") }
            }
        }
    }

    private suspend fun downloadFromUrl(url: String, outputFile: File) = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder().followRedirects(true).build()
        val response = client.newCall(Request.Builder().url(url).addHeader("User-Agent", "GP-VisitBuddy/1.0").build()).execute()
        if (!response.isSuccessful) throw Exception("Download failed: HTTP ${response.code}")
        val body = response.body ?: throw Exception("No response body")
        val contentLength = body.contentLength()
        body.byteStream().use { input ->
            FileOutputStream(outputFile).use { output ->
                val buffer = ByteArray(8192)
                var total = 0L
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    total += bytesRead
                    if (contentLength > 0) {
                        _state.update { it.copy(modelDownloadProgress = (total.toFloat() / contentLength).coerceIn(0f, 1f)) }
                    }
                }
            }
        }
    }

    fun loadModel(modelPath: String) {
        viewModelScope.launch {
            _state.update { it.copy(isModelLoading = true, modelError = null) }
            try {
                withContext(Dispatchers.IO) {
                    val file = File(modelPath)
                    if (!file.exists()) throw IllegalArgumentException("Model file not found: $modelPath")
                    whisperContext = WhisperContext.createFromFile(modelPath)
                }
                _state.update { it.copy(isModelLoaded = true, isModelLoading = false, systemInfo = WhisperContext.getSystemInfo()) }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to load model", e)
                _state.update { it.copy(isModelLoading = false, modelError = "Failed to load model: ${e.message}") }
            }
        }
    }

    fun loadModelFromAsset(assetPath: String) {
        viewModelScope.launch {
            _state.update { it.copy(isModelLoading = true, modelError = null) }
            try {
                withContext(Dispatchers.IO) {
                    whisperContext = WhisperContext.createFromAsset(getApplication<Application>().assets, assetPath)
                }
                _state.update { it.copy(isModelLoaded = true, isModelLoading = false, systemInfo = WhisperContext.getSystemInfo()) }
            } catch (e: Exception) {
                _state.update { it.copy(isModelLoading = false, modelError = "Failed to load model: ${e.message}") }
            }
        }
    }

    fun getModelDirectory(): File = storage.getModelDirectory()
    fun modelExists(modelName: String): Boolean = storage.modelExists(modelName)
    fun retryModelLoad() { autoLoadModel() }

    fun startRecording(title: String = "New Appointment") {
        if (!_state.value.isModelLoaded) {
            _state.update { it.copy(errorMessage = "Please load a model first") }
            return
        }
        val accountId = _state.value.userSession?.accountId ?: run {
            _state.update { it.copy(errorMessage = "Please sign in first") }
            return
        }
        viewModelScope.launch {
            try {
                currentAppointmentId = UUID.randomUUID().toString()
                val audioPath = storage.createAudioFilePath(currentAppointmentId!!, accountId)
                currentAudioFile = File(audioPath)
                val appointment = Appointment(
                    id = currentAppointmentId!!,
                    title = title,
                    audioFilePath = audioPath,
                    status = AppointmentStatus.DRAFT
                )
                storage.saveAppointment(appointment, accountId)
                _state.update { it.copy(isRecording = true, recordingDuration = 0, currentAppointment = appointment) }
                recorder.startRecording(currentAudioFile!!) { error ->
                    Log.e(LOG_TAG, "Recording error", error)
                    _state.update { it.copy(errorMessage = "Recording error: ${error.message}", isRecording = false) }
                }
                startRecordingTimer()
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to start recording", e)
                _state.update { it.copy(errorMessage = "Failed to start recording: ${e.message}") }
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            recordingTimerJob?.cancel()
            val audioData = recorder.stopRecording()
            val duration = _state.value.recordingDuration
            _state.update { it.copy(isRecording = false, isTranscribing = true) }
            if (audioData != null && audioData.isNotEmpty()) {
                val maxAmplitude = (audioData.map { kotlin.math.abs(it) }.maxOrNull() ?: 0f) * 32767
                if (maxAmplitude < 100) {
                    _state.update { it.copy(isTranscribing = false, errorMessage = "Audio too quiet. Please speak clearly.") }
                } else {
                    transcribeAudio(audioData, duration)
                }
            } else {
                _state.update { it.copy(isTranscribing = false, errorMessage = "No audio recorded") }
            }
        }
    }

    fun cancelRecording() {
        viewModelScope.launch {
            recordingTimerJob?.cancel()
            recorder.cancelRecording()
            currentAppointmentId?.let { id ->
                withContext(Dispatchers.IO) { storage.deleteAppointment(id, _state.value.userSession?.accountId) }
            }
            currentAppointmentId = null
            currentAudioFile = null
            _state.update { it.copy(isRecording = false, recordingDuration = 0, currentAppointment = null) }
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

    private suspend fun transcribeAudio(audioData: FloatArray, durationMs: Long) {
        try {
            val context = whisperContext ?: throw IllegalStateException("Model not loaded")
            val segments = withContext(Dispatchers.Default) { context.transcribeWithSegments(audioData) }
            val fullText = segments.joinToString(" ") { it.text }
            val transcription = Transcription(fullText, segments.map { TranscriptionSegmentData(it.text, it.startMs, it.endMs) })
            val extraction = SchemaGuidedExtractor.extract(fullText, (durationMs / 1000).toInt())
            val updatedAppointment = _state.value.currentAppointment?.copy(
                transcription = transcription,
                extraction = extraction,
                durationMs = durationMs,
                status = AppointmentStatus.PROCESSED
            )
            if (updatedAppointment != null) {
                withContext(Dispatchers.IO) {
                    storage.saveAppointment(updatedAppointment, _state.value.userSession?.accountId)
                }
            }
            _state.update { it.copy(isTranscribing = false, currentAppointment = updatedAppointment) }
            loadAppointments()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Transcription failed", e)
            _state.update { it.copy(isTranscribing = false, errorMessage = "Transcription failed: ${e.message}") }
        }
    }

    fun transcribeFile(file: File) {
        viewModelScope.launch {
            _state.update { it.copy(isTranscribing = true) }
            try {
                val audioData = withContext(Dispatchers.IO) { WaveHelper.decodeWaveFile(file) }
                transcribeAudio(audioData, (WaveHelper.getDuration(audioData.size) * 1000).toLong())
            } catch (e: Exception) {
                _state.update { it.copy(isTranscribing = false, errorMessage = "Failed to transcribe: ${e.message}") }
            }
        }
    }

    private fun loadAppointments() {
        viewModelScope.launch {
            val accountId = _state.value.userSession?.accountId
            val appointments = withContext(Dispatchers.IO) { storage.loadAllAppointments(accountId) }
            _state.update { it.copy(appointments = appointments) }
        }
    }

    fun selectAppointment(id: String) {
        viewModelScope.launch {
            val accountId = _state.value.userSession?.accountId
            val appointment = withContext(Dispatchers.IO) { storage.loadAppointment(id, accountId) }
            _state.update { it.copy(currentAppointment = appointment) }
        }
    }

    fun deleteAppointment(id: String) {
        viewModelScope.launch {
            val accountId = _state.value.userSession?.accountId
            withContext(Dispatchers.IO) { storage.deleteAppointment(id, accountId) }
            if (_state.value.currentAppointment?.id == id) {
                _state.update { it.copy(currentAppointment = null) }
            }
            loadAppointments()
        }
    }

    fun clearCurrentAppointment() {
        _state.update { it.copy(currentAppointment = null) }
    }

    fun updateAppointment(appointment: Appointment) {
        viewModelScope.launch {
            val accountId = _state.value.userSession?.accountId
            withContext(Dispatchers.IO) { storage.saveAppointment(appointment, accountId) }
            _state.update { it.copy(currentAppointment = appointment) }
            loadAppointments()
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null, modelError = null) }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { whisperContext?.release() }
    }
}
