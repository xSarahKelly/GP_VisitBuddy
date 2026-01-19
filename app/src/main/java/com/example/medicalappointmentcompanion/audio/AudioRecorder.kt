package com.example.medicalappointmentcompanion.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
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
class AudioRecorder(private val context: Context? = null) {
    
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
        
        // Configure audio mode for voice recording
        context?.let {
            try {
                val audioManager = it.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
                audioManager?.isSpeakerphoneOn = false
                Log.d(LOG_TAG, "Audio mode set to MODE_IN_COMMUNICATION")
            } catch (e: Exception) {
                Log.w(LOG_TAG, "Failed to configure audio mode", e)
            }
        }
        
        recordThread = AudioRecordThread(outputFile, context, onError)
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
    private val context: Context?,
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
            
            // Try multiple audio sources in order of preference
            val audioSources = listOf(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,  // Best for speech recognition
                MediaRecorder.AudioSource.MIC,                // Standard microphone
                MediaRecorder.AudioSource.VOICE_COMMUNICATION  // Fallback
            )
            
            var audioRecord: AudioRecord? = null
            var selectedSource = -1
            var bestSource: AudioRecord? = null
            var bestSourceId = -1
            var bestAmplitude: Short = 0
            
            // Test each audio source by doing a quick recording test
            for (source in audioSources) {
                try {
                    val testRecord = AudioRecord(
                        source,
                        WHISPER_SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize
                    )
                    
                    if (testRecord.state == AudioRecord.STATE_INITIALIZED) {
                        // Do a quick test recording to see which source actually captures audio
                        try {
                            testRecord.startRecording()
                            
                            // Small delay to let microphone initialize
                            Thread.sleep(50)
                            
                            // Read a small sample to test audio levels (0.2 seconds)
                            val testBuffer = ShortArray(WHISPER_SAMPLE_RATE / 5)
                            val read = testRecord.read(testBuffer, 0, testBuffer.size)
                            
                            if (read > 0) {
                                var testMax: Short = 0
                                for (i in 0 until read) {
                                    val abs = if (testBuffer[i] < 0) (-testBuffer[i]).toShort() else testBuffer[i]
                                    if (abs > testMax) testMax = abs
                                }
                                
                                Log.d(LOG_TAG, "Audio source $source test: max amplitude = $testMax")
                                
                                // Prefer source with highest amplitude
                                if (testMax > bestAmplitude) {
                                    bestSource?.stop()
                                    bestSource?.release()
                                    bestSource = testRecord
                                    bestSourceId = source
                                    bestAmplitude = testMax
                                } else {
                                    testRecord.stop()
                                    testRecord.release()
                                }
                            } else {
                                testRecord.stop()
                                testRecord.release()
                            }
                        } catch (e: Exception) {
                            Log.w(LOG_TAG, "Failed to test audio source $source", e)
                            testRecord.release()
                        }
                    } else {
                        testRecord.release()
                    }
                } catch (e: Exception) {
                    Log.w(LOG_TAG, "Audio source $source failed to initialize", e)
                }
            }
            
            // Use the source with best audio capture
            if (bestSource != null && bestAmplitude > 0) {
                audioRecord = bestSource
                selectedSource = bestSourceId
                Log.d(LOG_TAG, "Selected audio source: $selectedSource with test amplitude: $bestAmplitude")
                
                if (bestAmplitude < 100) {
                    Log.w(LOG_TAG, "WARNING: Selected audio source has very low test amplitude ($bestAmplitude). " +
                            "This may indicate microphone is not capturing audio properly. " +
                            "Check if another app is using the microphone or if microphone is blocked.")
                }
            } else {
                // Fallback: just use first available source
                for (source in audioSources) {
                    try {
                        val testRecord = AudioRecord(
                            source,
                            WHISPER_SAMPLE_RATE,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            bufferSize
                        )
                        
                        if (testRecord.state == AudioRecord.STATE_INITIALIZED) {
                            audioRecord = testRecord
                            selectedSource = source
                            Log.w(LOG_TAG, "Using audio source $source (no audio test available)")
                            break
                        } else {
                            testRecord.release()
                        }
                    } catch (e: Exception) {
                        // Continue trying
                    }
                }
            }
            
            if (audioRecord == null) {
                throw RuntimeException("Failed to initialize AudioRecord with any audio source")
            }
            
            val finalAudioRecord = audioRecord
            
            try {
                finalAudioRecord.startRecording()
                Log.d(LOG_TAG, "Recording started at ${WHISPER_SAMPLE_RATE}Hz with source $selectedSource")
                
                // Small delay to let microphone stabilize
                Thread.sleep(100)
                
                val allData = mutableListOf<Short>()
                
                var maxAmplitude: Short = 0
                var totalRead = 0
                
                while (!quit.get()) {
                    val read = finalAudioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        totalRead += read
                        for (i in 0 until read) {
                            allData.add(buffer[i])
                            val abs = if (buffer[i] < 0) (-buffer[i]).toShort() else buffer[i]
                            if (abs > maxAmplitude) maxAmplitude = abs
                        }
                        // Log progress every second
                        if (totalRead % WHISPER_SAMPLE_RATE < read) {
                            val seconds = totalRead / WHISPER_SAMPLE_RATE
                            Log.d(LOG_TAG, "Recording... ${seconds}s, max amp: $maxAmplitude")
                            
                            // Warn if amplitude is suspiciously low (likely no audio)
                            if (seconds >= 2 && maxAmplitude < 100) {
                                Log.w(LOG_TAG, "WARNING: Very low audio amplitude ($maxAmplitude). " +
                                        "Microphone may not be capturing audio properly. " +
                                        "Expected: 1000-20000 for normal speech.")
                            }
                        }
                    } else if (read < 0) {
                        throw RuntimeException("AudioRecord.read returned error: $read")
                    }
                }
                
                Log.d(LOG_TAG, "Recording finished. Max amplitude: $maxAmplitude")
                
                // Final warning if audio is too quiet
                if (maxAmplitude < 100) {
                    Log.e(LOG_TAG, "ERROR: Recording completed but audio amplitude is very low ($maxAmplitude). " +
                            "This recording will likely result in blank transcription. " +
                            "Audio source used: $selectedSource. " +
                            "Check microphone permission and ensure you're speaking clearly.")
                } else if (maxAmplitude < 1000) {
                    Log.w(LOG_TAG, "WARNING: Audio amplitude is low ($maxAmplitude). " +
                            "Transcription quality may be poor. Speak louder or closer to microphone.")
                } else {
                    Log.d(LOG_TAG, "Audio amplitude looks good ($maxAmplitude)")
                }
                
                finalAudioRecord.stop()
                
                // Save to WAV file
                val shortArray = allData.toShortArray()
                WaveHelper.encodeWaveFile(outputFile, shortArray)
                
                // Convert to float array for whisper
                audioData = WaveHelper.shortToFloat(shortArray)
                
                Log.d(LOG_TAG, "Recording saved: ${allData.size} samples, " +
                        "${allData.size / WHISPER_SAMPLE_RATE.toFloat()}s")
                
            } finally {
                finalAudioRecord.release()
                
                // Reset audio mode
                context?.let {
                    try {
                        val audioManager = it.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                        audioManager?.mode = AudioManager.MODE_NORMAL
                    } catch (e: Exception) {
                        Log.w(LOG_TAG, "Failed to reset audio mode", e)
                    }
                }
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

