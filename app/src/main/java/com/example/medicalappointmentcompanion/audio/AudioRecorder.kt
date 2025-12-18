package com.example.medicalappointmentcompanion.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private const val LOG_TAG = "AudioRecorder"

/**
 * Sample rate required by Whisper (16kHz)
 */
const val WHISPER_SAMPLE_RATE = 16000

/**
 * Audio recorder optimized for whisper.cpp transcription
 * 
 * Records audio at 16kHz mono PCM, which is the format required by whisper.
 * Can save to WAV file or return raw audio data for direct transcription.
 */
class AudioRecorder {
    
    private val scope: CoroutineScope = CoroutineScope(
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    )
    
    private var recordThread: AudioRecordThread? = null
    
    /**
     * Check if currently recording
     */
    val isRecording: Boolean get() = recordThread?.isRecording == true
    
    /**
     * Start recording audio to a file
     * 
     * @param outputFile File to save the WAV recording
     * @param onError Callback for recording errors
     */
    suspend fun startRecording(
        outputFile: File, 
        onError: (Exception) -> Unit = {}
    ) = withContext(scope.coroutineContext) {
        if (recordThread?.isRecording == true) {
            Log.w(LOG_TAG, "Already recording")
            return@withContext
        }
        
        Log.d(LOG_TAG, "Starting recording to: ${outputFile.absolutePath}")
        recordThread = AudioRecordThread(outputFile, onError)
        recordThread?.start()
    }
    
    /**
     * Stop recording and return the audio data
     * 
     * @return Float array of normalized audio samples, or null if not recording
     */
    suspend fun stopRecording(): FloatArray? = withContext(scope.coroutineContext) {
        val thread = recordThread ?: return@withContext null
        
        Log.d(LOG_TAG, "Stopping recording")
        thread.stopRecording()
        
        @Suppress("BlockingMethodInNonBlockingContext")
        thread.join()
        
        val audioData = thread.getAudioData()
        recordThread = null
        
        Log.d(LOG_TAG, "Recording stopped: ${audioData?.size ?: 0} samples")
        audioData
    }
    
    /**
     * Cancel recording without saving
     */
    suspend fun cancelRecording() = withContext(scope.coroutineContext) {
        recordThread?.stopRecording()
        
        @Suppress("BlockingMethodInNonBlockingContext")
        recordThread?.join()
        
        recordThread = null
        Log.d(LOG_TAG, "Recording cancelled")
    }
}

/**
 * Internal thread for audio recording
 */
private class AudioRecordThread(
    private val outputFile: File,
    private val onError: (Exception) -> Unit
) : Thread("AudioRecordThread") {
    
    private val quit = AtomicBoolean(false)
    private var audioData: FloatArray? = null
    
    val isRecording: Boolean get() = isAlive && !quit.get()
    
    @SuppressLint("MissingPermission")
    override fun run() {
        try {
            val bufferSize = AudioRecord.getMinBufferSize(
                WHISPER_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ) * 4  // Use 4x minimum for smoother recording
            
            val buffer = ShortArray(bufferSize / 2)
            
            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                WHISPER_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            
            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                throw RuntimeException("AudioRecord failed to initialize")
            }
            
            try {
                audioRecord.startRecording()
                Log.d(LOG_TAG, "Recording started at ${WHISPER_SAMPLE_RATE}Hz")
                
                val allData = mutableListOf<Short>()
                
                var maxAmplitude: Short = 0
                var totalRead = 0
                
                while (!quit.get()) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        totalRead += read
                        for (i in 0 until read) {
                            allData.add(buffer[i])
                            val abs = if (buffer[i] < 0) (-buffer[i]).toShort() else buffer[i]
                            if (abs > maxAmplitude) maxAmplitude = abs
                        }
                        // Log progress every second
                        if (totalRead % WHISPER_SAMPLE_RATE < read) {
                            Log.d(LOG_TAG, "Recording... ${totalRead / WHISPER_SAMPLE_RATE}s, max amp: $maxAmplitude")
                        }
                    } else if (read < 0) {
                        throw RuntimeException("AudioRecord.read returned error: $read")
                    }
                }
                
                Log.d(LOG_TAG, "Recording finished. Max amplitude: $maxAmplitude")
                
                audioRecord.stop()
                
                // Save to WAV file
                val shortArray = allData.toShortArray()
                WaveHelper.encodeWaveFile(outputFile, shortArray)
                
                // Convert to float array for whisper
                audioData = WaveHelper.shortToFloat(shortArray)
                
                Log.d(LOG_TAG, "Recording saved: ${allData.size} samples, " +
                        "${allData.size / WHISPER_SAMPLE_RATE.toFloat()}s")
                
            } finally {
                audioRecord.release()
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Recording error", e)
            onError(e)
        }
    }
    
    fun stopRecording() {
        quit.set(true)
    }
    
    fun getAudioData(): FloatArray? = audioData
}

