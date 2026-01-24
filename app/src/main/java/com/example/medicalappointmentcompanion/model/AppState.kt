package com.example.medicalappointmentcompanion.model

/**
 * Main application UI state
 */
data class AppState(
    val isModelLoaded: Boolean = false,
    val isModelLoading: Boolean = false,
    val isModelDownloading: Boolean = false,
    val modelLoadProgress: Float = 0f,
    val modelDownloadProgress: Float = 0f,
    val modelError: String? = null,
    
    val isRecording: Boolean = false,
    val recordingDuration: Long = 0,
    
    val isTranscribing: Boolean = false,
    val transcriptionProgress: Float = 0f,
    
    val currentAppointment: Appointment? = null,
    val appointments: List<Appointment> = emptyList(),
    
    val systemInfo: String = "",
    val errorMessage: String? = null
)

/**
 * Events that can occur in the app
 */
sealed class AppEvent {
    // Model events
    object LoadModel : AppEvent()
    data class ModelLoaded(val systemInfo: String) : AppEvent()
    data class ModelError(val message: String) : AppEvent()
    
    // Recording events
    object StartRecording : AppEvent()
    object StopRecording : AppEvent()
    object CancelRecording : AppEvent()
    data class RecordingError(val message: String) : AppEvent()
    
    // Transcription events
    data class TranscriptionComplete(val transcription: Transcription) : AppEvent()
    data class TranscriptionError(val message: String) : AppEvent()
    
    // Appointment events
    data class SaveAppointment(val appointment: Appointment) : AppEvent()
    data class DeleteAppointment(val id: String) : AppEvent()
    data class SelectAppointment(val id: String) : AppEvent()
    
    // General
    object ClearError : AppEvent()
}

/**
 * Result wrapper for operations
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

