package com.example.medicalappointmentcompanion.whisper

import android.content.res.AssetManager
import android.os.Build
import android.util.Log
import java.io.File
import java.io.InputStream

private const val LOG_TAG = "WhisperLib"

/**
 * JNI bindings for whisper.cpp native library.
 */
internal class WhisperLib {
    companion object {
        init {
            Log.d(LOG_TAG, "Primary ABI: ${Build.SUPPORTED_ABIS[0]}")
            var loadVfpv4 = false
            var loadV8fp16 = false
            
            if (isArmEabiV7a()) {
                // armeabi-v7a needs runtime detection support
                val cpuInfo = cpuInfo()
                cpuInfo?.let {
                    Log.d(LOG_TAG, "CPU info: $cpuInfo")
                    if (cpuInfo.contains("vfpv4")) {
                        Log.d(LOG_TAG, "CPU supports vfpv4")
                        loadVfpv4 = true
                    }
                }
            } else if (isArmEabiV8a()) {
                // ARMv8.2a needs runtime detection support
                val cpuInfo = cpuInfo()
                cpuInfo?.let {
                    Log.d(LOG_TAG, "CPU info: $cpuInfo")
                    if (cpuInfo.contains("fphp")) {
                        Log.d(LOG_TAG, "CPU supports fp16 arithmetic")
                        loadV8fp16 = true
                    }
                }
            }
            
            when {
                loadVfpv4 -> {
                    Log.d(LOG_TAG, "Loading libwhisper_vfpv4.so")
                    System.loadLibrary("whisper_vfpv4")
                }
                loadV8fp16 -> {
                    Log.d(LOG_TAG, "Loading libwhisper_v8fp16_va.so")
                    System.loadLibrary("whisper_v8fp16_va")
                }
                else -> {
                    Log.d(LOG_TAG, "Loading libwhisper.so")
                    System.loadLibrary("whisper")
                }
            }
        }
        
        // JNI methods - Context management
        external fun initContextFromInputStream(inputStream: InputStream): Long
        external fun initContextFromAsset(assetManager: AssetManager, assetPath: String): Long
        external fun initContext(modelPath: String): Long
        external fun freeContext(contextPtr: Long)
        
        // JNI methods - Transcription
        external fun fullTranscribe(contextPtr: Long, numThreads: Int, audioData: FloatArray)
        
        // JNI methods - Results
        external fun getTextSegmentCount(contextPtr: Long): Int
        external fun getTextSegment(contextPtr: Long, index: Int): String
        external fun getTextSegmentT0(contextPtr: Long, index: Int): Long
        external fun getTextSegmentT1(contextPtr: Long, index: Int): Long
        
        // JNI methods - System info
        external fun getSystemInfo(): String
        external fun benchMemcpy(nthread: Int): String
        external fun benchGgmlMulMat(nthread: Int): String
        
        private fun isArmEabiV7a(): Boolean = Build.SUPPORTED_ABIS[0] == "armeabi-v7a"
        private fun isArmEabiV8a(): Boolean = Build.SUPPORTED_ABIS[0] == "arm64-v8a"
        
        private fun cpuInfo(): String? = try {
            File("/proc/cpuinfo").inputStream().bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.w(LOG_TAG, "Couldn't read /proc/cpuinfo", e)
            null
        }
    }
}

