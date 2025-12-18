package com.example.medicalappointmentcompanion.whisper

import android.util.Log
import java.io.BufferedReader
import java.io.FileReader

private const val LOG_TAG = "WhisperCpuConfig"

/**
 * CPU configuration helper for optimal whisper performance
 * 
 * Detects high-performance CPU cores and recommends thread count
 * for best transcription performance on heterogeneous ARM processors.
 */
object WhisperCpuConfig {
    
    /**
     * Number of threads to use for whisper operations.
     * 
     * On big.LITTLE architectures, returns the count of high-performance cores.
     * Always returns at least 2 threads.
     */
    val preferredThreadCount: Int
        get() = CpuInfo.getHighPerfCpuCount().coerceAtLeast(2)
    
    /**
     * Total number of CPU cores available
     */
    val totalCpuCount: Int
        get() = Runtime.getRuntime().availableProcessors()
}

private class CpuInfo(private val lines: List<String>) {
    
    private fun getHighPerfCpuCount(): Int = try {
        getHighPerfCpuCountByFrequencies()
    } catch (e: Exception) {
        Log.d(LOG_TAG, "Couldn't read CPU frequencies", e)
        getHighPerfCpuCountByVariant()
    }
    
    private fun getHighPerfCpuCountByFrequencies(): Int =
        getCpuValues(property = "processor") { getMaxCpuFrequency(it.toInt()) }
            .also { Log.d(LOG_TAG, "CPU frequencies (freq, count): ${it.binnedValues()}") }
            .countDroppingMin()
    
    private fun getHighPerfCpuCountByVariant(): Int =
        getCpuValues(property = "CPU variant") { it.substringAfter("0x").toInt(radix = 16) }
            .also { Log.d(LOG_TAG, "CPU variants (variant, count): ${it.binnedValues()}") }
            .countKeepingMin()
    
    private fun List<Int>.binnedValues() = groupingBy { it }.eachCount()
    
    private fun getCpuValues(property: String, mapper: (String) -> Int) = lines
        .asSequence()
        .filter { it.startsWith(property) }
        .map { mapper(it.substringAfter(':').trim()) }
        .sorted()
        .toList()
    
    private fun List<Int>.countDroppingMin(): Int {
        if (isEmpty()) return 0
        val min = min()
        return count { it > min }
    }
    
    private fun List<Int>.countKeepingMin(): Int {
        if (isEmpty()) return 0
        val min = min()
        return count { it == min }
    }
    
    companion object {
        fun getHighPerfCpuCount(): Int = try {
            readCpuInfo().getHighPerfCpuCount()
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Couldn't read CPU info", e)
            // Fallback: assume half the cores are high-performance
            (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(2)
        }
        
        private fun readCpuInfo() = CpuInfo(
            BufferedReader(FileReader("/proc/cpuinfo"))
                .useLines { it.toList() }
        )
        
        private fun getMaxCpuFrequency(cpuIndex: Int): Int {
            val path = "/sys/devices/system/cpu/cpu${cpuIndex}/cpufreq/cpuinfo_max_freq"
            val maxFreq = BufferedReader(FileReader(path)).use { it.readLine() }
            return maxFreq.toInt()
        }
    }
}

