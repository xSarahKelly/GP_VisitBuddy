package com.example.medicalappointmentcompanion.whisper

import android.content.res.AssetManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.concurrent.Executors

private const val LOG_TAG = "WhisperContext"

/**
 * Kotlin wrapper for whisper.cpp context.
 * 
 * Provides a coroutine-based interface for speech-to-text transcription.
 * 
 * Whisper contexts are NOT thread-safe - all operations on a context
 * must be serialized. This class handles serialization automatically.
 */
class WhisperContext private constructor(private var ptr: Long) {
    
    // Single-threaded dispatcher to ensure thread safety
    private val scope: CoroutineScope = CoroutineScope(
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    )
    
    /**
     * Transcribe audio data to text
     * 
     * @param data Float array of audio samples (16kHz, mono, normalized to [-1, 1])
     * @param includeTimestamps Whether to include timestamps in the output
     * @return Transcribed text
     */
    suspend fun transcribe(
        data: FloatArray, 
        includeTimestamps: Boolean = false
    ): String = withContext(scope.coroutineContext) {
        require(ptr != 0L) { "WhisperContext has been released" }
        
        val numThreads = WhisperCpuConfig.preferredThreadCount
        Log.d(LOG_TAG, "Transcribing with $numThreads threads, ${data.size} samples")
        
        WhisperLib.fullTranscribe(ptr, numThreads, data)
        
        val segmentCount = WhisperLib.getTextSegmentCount(ptr)
        Log.d(LOG_TAG, "Transcription complete: $segmentCount segments")
        
        buildString {
            for (i in 0 until segmentCount) {
                val text = WhisperLib.getTextSegment(ptr, i).trim()
                
                // Skip blank audio segments
                if (text.isEmpty() || 
                    text.equals("[BLANK_AUDIO]", ignoreCase = true) ||
                    text.equals("BLANK_AUDIO", ignoreCase = true) ||
                    text.equals("[BLANK]", ignoreCase = true)) {
                    continue
                }
                
                if (includeTimestamps) {
                    val t0 = formatTimestamp(WhisperLib.getTextSegmentT0(ptr, i))
                    val t1 = formatTimestamp(WhisperLib.getTextSegmentT1(ptr, i))
                    append("[$t0 --> $t1]: $text\n")
                } else {
                    if (isNotEmpty()) append(" ")
                    append(text)
                }
            }
        }
    }
    
    /**
     * Get transcription segments with timing information
     */
    suspend fun transcribeWithSegments(data: FloatArray): List<TranscriptionSegment> = 
        withContext(scope.coroutineContext) {
            require(ptr != 0L) { "WhisperContext has been released" }
            
            val numThreads = WhisperCpuConfig.preferredThreadCount
            WhisperLib.fullTranscribe(ptr, numThreads, data)
            
            val segmentCount = WhisperLib.getTextSegmentCount(ptr)
            (0 until segmentCount)
                .map { i ->
                    TranscriptionSegment(
                        text = WhisperLib.getTextSegment(ptr, i),
                        startMs = WhisperLib.getTextSegmentT0(ptr, i) * 10,
                        endMs = WhisperLib.getTextSegmentT1(ptr, i) * 10
                    )
                }
                .filter { segment ->
                    // Filter out blank audio segments and empty/whitespace-only segments
                    val text = segment.text.trim()
                    text.isNotEmpty() && 
                    !text.equals("[BLANK_AUDIO]", ignoreCase = true) &&
                    !text.equals("BLANK_AUDIO", ignoreCase = true) &&
                    !text.equals("[BLANK]", ignoreCase = true)
                }
        }
    
    /**
     * Benchmark memory copy performance
     */
    suspend fun benchMemory(nthreads: Int): String = withContext(scope.coroutineContext) {
        WhisperLib.benchMemcpy(nthreads)
    }
    
    /**
     * Benchmark matrix multiplication performance
     */
    suspend fun benchGgmlMulMat(nthreads: Int): String = withContext(scope.coroutineContext) {
        WhisperLib.benchGgmlMulMat(nthreads)
    }
    
    /**
     * Release native resources
     * 
     * After calling this method, the context cannot be used.
     */
    suspend fun release() = withContext(scope.coroutineContext) {
        if (ptr != 0L) {
            Log.d(LOG_TAG, "Releasing WhisperContext")
            WhisperLib.freeContext(ptr)
            ptr = 0
        }
    }
    
    /**
     * Check if context is still valid
     */
    val isValid: Boolean get() = ptr != 0L
    
    protected fun finalize() {
        runBlocking {
            release()
        }
    }
    
    companion object {
        /**
         * Create context from a model file path
         */
        fun createFromFile(filePath: String): WhisperContext {
            Log.d(LOG_TAG, "Creating context from file: $filePath")
            val ptr = WhisperLib.initContext(filePath)
            if (ptr == 0L) {
                throw RuntimeException("Failed to create WhisperContext from file: $filePath")
            }
            return WhisperContext(ptr)
        }
        
        /**
         * Create context from an InputStream
         */
        fun createFromInputStream(stream: InputStream): WhisperContext {
            Log.d(LOG_TAG, "Creating context from InputStream")
            val ptr = WhisperLib.initContextFromInputStream(stream)
            if (ptr == 0L) {
                throw RuntimeException("Failed to create WhisperContext from InputStream")
            }
            return WhisperContext(ptr)
        }
        
        /**
         * Create context from an APK asset
         */
        fun createFromAsset(assetManager: AssetManager, assetPath: String): WhisperContext {
            Log.d(LOG_TAG, "Creating context from asset: $assetPath")
            val ptr = WhisperLib.initContextFromAsset(assetManager, assetPath)
            if (ptr == 0L) {
                throw RuntimeException("Failed to create WhisperContext from asset: $assetPath")
            }
            return WhisperContext(ptr)
        }
        
        /**
         * Get whisper system info string
         */
        fun getSystemInfo(): String = WhisperLib.getSystemInfo()
    }
    
    private fun formatTimestamp(t: Long, comma: Boolean = false): String {
        var msec = t * 10
        val hr = msec / (1000 * 60 * 60)
        msec -= hr * (1000 * 60 * 60)
        val min = msec / (1000 * 60)
        msec -= min * (1000 * 60)
        val sec = msec / 1000
        msec -= sec * 1000
        
        val delimiter = if (comma) "," else "."
        return String.format("%02d:%02d:%02d%s%03d", hr, min, sec, delimiter, msec)
    }
}

/**
 * Represents a transcribed segment with timing
 */
data class TranscriptionSegment(
    val text: String,
    val startMs: Long,
    val endMs: Long
)

