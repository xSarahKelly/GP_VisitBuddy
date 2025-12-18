package com.example.medicalappointmentcompanion.extraction

import com.example.medicalappointmentcompanion.model.MedicalInfo
import com.example.medicalappointmentcompanion.model.MedicalTest
import com.example.medicalappointmentcompanion.model.Medication
import com.example.medicalappointmentcompanion.model.TestType
import com.example.medicalappointmentcompanion.model.VitalSigns

/**
 * Extracts medical information from transcribed text.
 * 
 * Uses pattern matching and keyword detection to identify:
 * - Medications and dosages
 * - Diagnoses and conditions
 * - Follow-up instructions
 * - Vital signs
 * - Scheduled tests
 */
object MedicalExtractor {
    
    // Common medication patterns
    private val medicationPatterns = listOf(
        // "take X mg of Y" or "X mg Y"
        Regex("""(?:take|prescribed?|start(?:ing)?)\s+(\d+(?:\.\d+)?)\s*(mg|mcg|ml|g)\s+(?:of\s+)?(\w+)""", RegexOption.IGNORE_CASE),
        // "Y X mg"
        Regex("""(\w+)\s+(\d+(?:\.\d+)?)\s*(mg|mcg|ml|g)""", RegexOption.IGNORE_CASE),
        // Common medication names followed by dosing
        Regex("""((?:aspirin|ibuprofen|acetaminophen|metformin|lisinopril|atorvastatin|omeprazole|amlodipine|metoprolol|losartan|gabapentin|hydrochlorothiazide|sertraline|fluoxetine|escitalopram|duloxetine|bupropion|trazodone|alprazolam|lorazepam|zolpidem|prednisone|amoxicillin|azithromycin|ciprofloxacin|levothyroxine))\s*(?:(\d+(?:\.\d+)?)\s*(mg|mcg|ml|g))?""", RegexOption.IGNORE_CASE)
    )
    
    // Frequency patterns
    private val frequencyPatterns = listOf(
        Regex("""(once|twice|three times|four times)\s+(a|per)\s+(day|daily|week|weekly|month)""", RegexOption.IGNORE_CASE),
        Regex("""(daily|weekly|monthly|every\s+\d+\s+hours?|every\s+\d+\s+days?|at\s+bedtime|in\s+the\s+morning|with\s+meals?|before\s+meals?|after\s+meals?)""", RegexOption.IGNORE_CASE),
        Regex("""(BID|TID|QID|PRN|QD|QHS|QAM|QPM)""", RegexOption.IGNORE_CASE)
    )
    
    // Diagnosis patterns
    private val diagnosisPatterns = listOf(
        Regex("""(?:diagnos(?:is|ed)|you have|suffering from|condition is|showing signs of)\s+(.+?)(?:\.|,|and|$)""", RegexOption.IGNORE_CASE),
        Regex("""(?:type\s*[12]?\s*diabetes|hypertension|high blood pressure|arthritis|asthma|COPD|depression|anxiety|migraine|allergies|infection|inflammation)""", RegexOption.IGNORE_CASE)
    )
    
    // Vital signs patterns
    private val vitalPatterns = mapOf(
        "bloodPressure" to Regex("""(?:blood pressure|BP)\s*(?:is|was|:)?\s*(\d{2,3})\s*[/\\]\s*(\d{2,3})""", RegexOption.IGNORE_CASE),
        "heartRate" to Regex("""(?:heart rate|pulse|HR)\s*(?:is|was|:)?\s*(\d{2,3})(?:\s*(?:bpm|beats))?""", RegexOption.IGNORE_CASE),
        "temperature" to Regex("""(?:temperature|temp)\s*(?:is|was|:)?\s*(\d{2,3}(?:\.\d)?)\s*(?:°?[FC])?""", RegexOption.IGNORE_CASE),
        "weight" to Regex("""(?:weight|weigh)\s*(?:is|was|:)?\s*(\d{2,3}(?:\.\d)?)\s*(?:lbs?|kg|pounds?|kilos?)""", RegexOption.IGNORE_CASE),
        "oxygen" to Regex("""(?:oxygen|O2|SpO2|saturation)\s*(?:is|was|:)?\s*(\d{2,3})\s*%?""", RegexOption.IGNORE_CASE)
    )
    
    // Follow-up patterns
    private val followUpPatterns = listOf(
        Regex("""(?:come back|follow[- ]?up|see (?:me|you)|return|schedule)\s*(?:in|after)?\s*(\d+)\s*(days?|weeks?|months?)""", RegexOption.IGNORE_CASE),
        Regex("""(?:next appointment|follow[- ]?up)\s*(?:on|is)?\s*(\w+\s+\d{1,2}(?:st|nd|rd|th)?(?:,?\s*\d{4})?)""", RegexOption.IGNORE_CASE)
    )
    
    // Test patterns
    private val testPatterns = listOf(
        Regex("""(?:need|order(?:ing)?|schedule|get)\s+(?:a\s+)?(?:an?\s+)?(blood test|X-ray|MRI|CT scan|ultrasound|EKG|ECG|mammogram|colonoscopy|biopsy|urine test|stool test)""", RegexOption.IGNORE_CASE),
        Regex("""(blood work|lab work|blood panel|CBC|metabolic panel|lipid panel|thyroid panel|A1C|hemoglobin)""", RegexOption.IGNORE_CASE)
    )
    
    // Warning/important patterns
    private val warningPatterns = listOf(
        Regex("""(?:if you (?:experience|notice|have)|watch (?:out )?for|warning signs?|go to (?:the )?(?:ER|emergency)|call (?:me|us|911)|seek (?:immediate )?(?:medical )?(?:attention|help))\s*[:\-]?\s*(.+?)(?:\.|$)""", RegexOption.IGNORE_CASE),
        Regex("""(?:don't|do not|avoid|stop)\s+(.+?)(?:\.|,|$)""", RegexOption.IGNORE_CASE)
    )
    
    // Instruction patterns
    private val instructionPatterns = listOf(
        Regex("""(?:make sure|be sure|remember|you should|I (?:want|need) you to|please)\s+(?:to\s+)?(.+?)(?:\.|$)""", RegexOption.IGNORE_CASE),
        Regex("""(?:rest|drink|eat|exercise|sleep|avoid|limit|increase|decrease)\s+(.+?)(?:\.|,|$)""", RegexOption.IGNORE_CASE)
    )
    
    /**
     * Extract medical information from transcription text
     */
    fun extract(text: String): MedicalInfo {
        return MedicalInfo(
            medications = extractMedications(text),
            diagnoses = extractDiagnoses(text),
            vitalSigns = extractVitalSigns(text),
            followUpDate = extractFollowUp(text),
            tests = extractTests(text),
            warnings = extractWarnings(text),
            instructions = extractInstructions(text),
            symptoms = extractSymptoms(text)
        )
    }
    
    private fun extractMedications(text: String): List<Medication> {
        val medications = mutableListOf<Medication>()
        val foundMeds = mutableSetOf<String>()
        
        for (pattern in medicationPatterns) {
            pattern.findAll(text).forEach { match ->
                val groups = match.groupValues
                if (groups.size >= 4) {
                    val name = groups[3].lowercase().replaceFirstChar { it.uppercase() }
                    if (name !in foundMeds && name.length > 2) {
                        foundMeds.add(name)
                        
                        // Try to find frequency
                        val nearbyText = text.substring(
                            maxOf(0, match.range.first - 50),
                            minOf(text.length, match.range.last + 100)
                        )
                        val frequency = frequencyPatterns
                            .firstNotNullOfOrNull { it.find(nearbyText)?.value }
                        
                        medications.add(
                            Medication(
                                name = name,
                                dosage = "${groups[1]} ${groups[2]}",
                                frequency = frequency
                            )
                        )
                    }
                }
            }
        }
        
        return medications.distinctBy { it.name.lowercase() }
    }
    
    private fun extractDiagnoses(text: String): List<String> {
        val diagnoses = mutableSetOf<String>()
        
        for (pattern in diagnosisPatterns) {
            pattern.findAll(text).forEach { match ->
                val diagnosis = match.groupValues.getOrNull(1) ?: match.value
                val cleaned = diagnosis.trim()
                    .replace(Regex("""^(a|an|the)\s+""", RegexOption.IGNORE_CASE), "")
                    .trim()
                if (cleaned.length in 3..100) {
                    diagnoses.add(cleaned)
                }
            }
        }
        
        return diagnoses.toList()
    }
    
    private fun extractVitalSigns(text: String): VitalSigns? {
        var bloodPressure: String? = null
        var heartRate: Int? = null
        var temperature: Float? = null
        var weight: Float? = null
        var oxygenSaturation: Int? = null
        
        vitalPatterns["bloodPressure"]?.find(text)?.let { match ->
            bloodPressure = "${match.groupValues[1]}/${match.groupValues[2]}"
        }
        
        vitalPatterns["heartRate"]?.find(text)?.let { match ->
            heartRate = match.groupValues[1].toIntOrNull()
        }
        
        vitalPatterns["temperature"]?.find(text)?.let { match ->
            temperature = match.groupValues[1].toFloatOrNull()
        }
        
        vitalPatterns["weight"]?.find(text)?.let { match ->
            weight = match.groupValues[1].toFloatOrNull()
        }
        
        vitalPatterns["oxygen"]?.find(text)?.let { match ->
            oxygenSaturation = match.groupValues[1].toIntOrNull()
        }
        
        return if (listOfNotNull(bloodPressure, heartRate, temperature, weight, oxygenSaturation).isNotEmpty()) {
            VitalSigns(
                bloodPressure = bloodPressure,
                heartRate = heartRate,
                temperature = temperature,
                weight = weight,
                oxygenSaturation = oxygenSaturation
            )
        } else null
    }
    
    private fun extractFollowUp(text: String): String? {
        for (pattern in followUpPatterns) {
            pattern.find(text)?.let { match ->
                return match.value
            }
        }
        return null
    }
    
    private fun extractTests(text: String): List<MedicalTest> {
        val tests = mutableListOf<MedicalTest>()
        val foundTests = mutableSetOf<String>()
        
        for (pattern in testPatterns) {
            pattern.findAll(text).forEach { match ->
                val testName = (match.groupValues.getOrNull(1) ?: match.value).trim()
                if (testName !in foundTests) {
                    foundTests.add(testName)
                    tests.add(
                        MedicalTest(
                            name = testName,
                            type = categorizeTest(testName)
                        )
                    )
                }
            }
        }
        
        return tests
    }
    
    private fun categorizeTest(testName: String): TestType {
        val lower = testName.lowercase()
        return when {
            lower.contains("blood") || lower.contains("cbc") || 
            lower.contains("panel") || lower.contains("a1c") -> TestType.BLOOD_TEST
            
            lower.contains("x-ray") || lower.contains("mri") || 
            lower.contains("ct") || lower.contains("ultrasound") || 
            lower.contains("mammogram") -> TestType.IMAGING
            
            lower.contains("biopsy") || lower.contains("colonoscopy") -> TestType.PROCEDURE
            
            lower.contains("screening") -> TestType.SCREENING
            
            else -> TestType.OTHER
        }
    }
    
    private fun extractWarnings(text: String): List<String> {
        val warnings = mutableListOf<String>()
        
        for (pattern in warningPatterns) {
            pattern.findAll(text).forEach { match ->
                val warning = match.groupValues.getOrNull(1) ?: match.value
                if (warning.length in 5..200) {
                    warnings.add(warning.trim())
                }
            }
        }
        
        return warnings.distinct()
    }
    
    private fun extractInstructions(text: String): List<String> {
        val instructions = mutableListOf<String>()
        
        for (pattern in instructionPatterns) {
            pattern.findAll(text).forEach { match ->
                val instruction = match.groupValues.getOrNull(1) ?: match.value
                if (instruction.length in 5..200) {
                    instructions.add(instruction.trim())
                }
            }
        }
        
        return instructions.distinct().take(10)
    }
    
    private fun extractSymptoms(text: String): List<String> {
        val symptomKeywords = listOf(
            "pain", "ache", "headache", "nausea", "fatigue", "tired",
            "fever", "cough", "shortness of breath", "dizziness", "weakness",
            "swelling", "rash", "itching", "numbness", "tingling",
            "chest pain", "back pain", "stomach pain", "joint pain"
        )
        
        val symptoms = mutableListOf<String>()
        val lowerText = text.lowercase()
        
        symptomKeywords.forEach { symptom ->
            if (lowerText.contains(symptom)) {
                symptoms.add(symptom.replaceFirstChar { it.uppercase() })
            }
        }
        
        return symptoms.distinct()
    }
}

