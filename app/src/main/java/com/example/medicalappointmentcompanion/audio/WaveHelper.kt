package com.example.medicalappointmentcompanion.audio

import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Helper for reading and writing WAV audio files
 * 
 * Handles the RIFF/WAVE format used for audio storage and
 * conversion to the float format required by whisper.cpp.
 */
object WaveHelper {
    
    /**
     * Decode a WAV file to float array for whisper
     * 
     * @param file WAV file to decode
     * @return Float array of normalized samples [-1.0, 1.0]
     */
    fun decodeWaveFile(file: File): FloatArray {
        val baos = ByteArrayOutputStream()
        file.inputStream().use { it.copyTo(baos) }
        
        val buffer = ByteBuffer.wrap(baos.toByteArray())
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        
        // Read channel count from WAV header (offset 22)
        val channels = buffer.getShort(22).toInt()
        
        // Skip header and read audio data (offset 44)
        buffer.position(44)
        val shortBuffer = buffer.asShortBuffer()
        val shortArray = ShortArray(shortBuffer.limit())
        shortBuffer.get(shortArray)
        
        // Convert to mono float
        return FloatArray(shortArray.size / channels) { index ->
            when (channels) {
                1 -> (shortArray[index] / 32767.0f).coerceIn(-1f, 1f)
                else -> {
                    // Mix stereo to mono
                    val left = shortArray[2 * index]
                    val right = shortArray[2 * index + 1]
                    ((left + right) / 32767.0f / 2.0f).coerceIn(-1f, 1f)
                }
            }
        }
    }
    
    /**
     * Encode audio data to a WAV file
     * 
     * @param file Output file
     * @param data Audio samples as 16-bit PCM
     * @param sampleRate Sample rate (default 16000 for whisper)
     */
    fun encodeWaveFile(
        file: File, 
        data: ShortArray,
        sampleRate: Int = WHISPER_SAMPLE_RATE
    ) {
        file.outputStream().use { output ->
            output.write(createWavHeader(data.size * 2, sampleRate))
            
            val buffer = ByteBuffer.allocate(data.size * 2)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            buffer.asShortBuffer().put(data)
            
            val bytes = ByteArray(buffer.limit())
            buffer.get(bytes)
            output.write(bytes)
        }
    }
    
    /**
     * Convert short array to float array for whisper
     */
    fun shortToFloat(data: ShortArray): FloatArray {
        return FloatArray(data.size) { i ->
            (data[i] / 32767.0f).coerceIn(-1f, 1f)
        }
    }
    
    /**
     * Convert float array back to short array
     */
    fun floatToShort(data: FloatArray): ShortArray {
        return ShortArray(data.size) { i ->
            (data[i] * 32767.0f).toInt().coerceIn(-32768, 32767).toShort()
        }
    }
    
    /**
     * Create a WAV file header
     */
    private fun createWavHeader(dataLength: Int, sampleRate: Int): ByteArray {
        val totalLength = dataLength + 44
        val byteRate = sampleRate * 2  // 16-bit mono
        
        return ByteBuffer.allocate(44).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            
            // RIFF header
            put('R'.code.toByte())
            put('I'.code.toByte())
            put('F'.code.toByte())
            put('F'.code.toByte())
            putInt(totalLength - 8)  // File size - 8
            
            // WAVE format
            put('W'.code.toByte())
            put('A'.code.toByte())
            put('V'.code.toByte())
            put('E'.code.toByte())
            
            // fmt subchunk
            put('f'.code.toByte())
            put('m'.code.toByte())
            put('t'.code.toByte())
            put(' '.code.toByte())
            putInt(16)              // Subchunk1 size (16 for PCM)
            putShort(1.toShort())   // Audio format (1 = PCM)
            putShort(1.toShort())   // Number of channels (1 = mono)
            putInt(sampleRate)      // Sample rate
            putInt(byteRate)        // Byte rate
            putShort(2.toShort())   // Block align (2 bytes per sample)
            putShort(16.toShort())  // Bits per sample
            
            // data subchunk
            put('d'.code.toByte())
            put('a'.code.toByte())
            put('t'.code.toByte())
            put('a'.code.toByte())
            putInt(dataLength)      // Data size
            
            position(0)
        }.let { buffer ->
            ByteArray(buffer.limit()).also { buffer.get(it) }
        }
    }
    
    /**
     * Get duration of audio in seconds
     */
    fun getDuration(samples: Int, sampleRate: Int = WHISPER_SAMPLE_RATE): Float {
        return samples.toFloat() / sampleRate
    }
    
    /**
     * Get duration of a WAV file in seconds
     */
    fun getFileDuration(file: File): Float {
        val data = decodeWaveFile(file)
        return getDuration(data.size)
    }
}

