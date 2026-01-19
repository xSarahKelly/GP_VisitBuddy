package com.example.medicalappointmentcompanion.extraction

import com.example.medicalappointmentcompanion.model.*

/**
 * Schema-Guided Medical Information Extractor
 * 
 * Grounded in Calgary-Cambridge model (stages 4 and 5)
 * 
 * DESIGN PRINCIPLES:
 * 1. Extract ONLY explicitly stated information (NO inference)
 * 2. Copy EXACT phrases from transcript
 * 3. Prioritise patient recall and safety
 * 4. Remain explainable and auditable
 * 5. Be usable by patients and carers
 * 
 * EXPLICIT EXCLUSIONS (NEVER extracted):
 * - Diagnoses
 * - Interpretations  
 * - Treatment reasoning
 * - Inferred intent
 * - Medical opinions
 */
object SchemaGuidedExtractor {
    
    // ========================================================================
    // KEYWORD LISTS - Frozen, no scope creep
    // ========================================================================
    
    // Medication trigger phrases
    private val MEDICATION_TRIGGERS = listOf(
        "take", "taking", "prescribe", "prescribed", "prescribing",
        "start", "starting", "begin", "beginning",
        "continue", "continuing", "keep taking",
        "medication", "medicine", "tablet", "tablets", "pill", "pills",
        "capsule", "capsules", "dose", "dosage",
        "milligrams", "mg", "micrograms", "mcg", "millilitres", "ml"
    )
    
    // Common medications prescribed in Ireland
    private val COMMON_MEDICATIONS = listOf(
        // Pain relief
        "paracetamol", "ibuprofen", "aspirin", "codeine", "tramadol",
        "co-codamol", "solpadol", "difene", "diclofenac", "naproxen",
        "ponstan", "mefenamic acid", "co-dydramol", "nurofen",
        
        // Antibiotics 
        "amoxicillin", "amoxosilin", "a moxosilin", "a moxicillin", "amoxacillin",
        "augmentin", "co-amoxiclav", "flucloxacillin",
        "doxycycline", "clarithromycin", "azithromycin", "metronidazole",
        "trimethoprim", "nitrofurantoin", "ciprofloxacin", "penicillin",
        
        // Stomach/acid/nausea
        "omeprazole", "lansoprazole", "esomeprazole", "pantoprazole",
        "domperidone", "motilium", "gaviscon", "buscopan",
        "cyclizine", "prochlorperazine", "stemetil", "ondansetron",
        
        // Diabetes
        "metformin", "gliclazide", "insulin", "sitagliptin", "empagliflozin",
        
        // Blood pressure/heart
        "lisinopril", "ramipril", "perindopril", "amlodipine", 
        "bisoprolol", "atenolol", "diltiazem", "verapamil",
        "losartan", "candesartan", "furosemide", "bendroflumethiazide",
        
        // Cholesterol
        "atorvastatin", "rosuvastatin", "simvastatin", "pravastatin",
        
        // Mental health
        "sertraline", "escitalopram", "citalopram", "fluoxetine",
        "venlafaxine", "mirtazapine", "duloxetine", "amitriptyline",
        
        // Respiratory
        "salbutamol", "ventolin", "beclometasone", "seretide", "symbicort",
        "montelukast", "prednisolone", "prednisone",
        
        // Thyroid
        "levothyroxine", "eltroxin", "thyroxine",
        
        // Blood thinners
        "warfarin", "apixaban", "rivaroxaban", "dabigatran", "clopidogrel",
        
        // Nerve pain/epilepsy
        "gabapentin", "pregabalin", "carbamazepine",
        
        // Sedatives/anxiety
        "diazepam", "alprazolam", "zopiclone", "lorazepam",
        
        // Allergies/antihistamines
        "cetirizine", "loratadine", "fexofenadine", "piriton", "chlorphenamine",
        "beconase", "avamys", "nasonex", "dymista",
        
        // Skin conditions
        "hydrocortisone", "betnovate", "eumovate", "dermovate", "elocon",
        "fucidin", "fusidic acid", "fucibet", "daktacort",
        "daktarin", "canesten cream", "lamisil",
        "diprobase", "epaderm", "dermol", "doublebase", "cetraben",
        "duac", "differin", "epiduo", "zineryt",
        
        // Eye/ear
        "chloramphenicol", "fucithalmic", "maxitrol",
        "otomize", "sofradex", "locorten vioform",
        "hypromellose", "hylo-tear",
        
        // Gout
        "allopurinol", "colchicine", "febuxostat",
        
        // Men's health/prostate
        "tamsulosin", "alfuzosin", "finasteride", "dutasteride",
        "sildenafil", "tadalafil",
        
        // Viral infections
        "aciclovir", "valaciclovir",
        
        // Women's health
        "microgynon", "cilest", "yasmin", "dianette", "cerazette", "noriday",
        "mirena", "kyleena", "jaydess", "copper coil",
        "norethisterone", "provera", "tranexamic acid",
        "evorel", "estradot", "elleste", "femoston", "kliovance", "oestrogel",
        "vagifem", "ovestin",
        "clomid", "clomiphene",
        "fluconazole", "canesten",
        
        // Supplements 
        "folic acid", "vitamin d", "desunin", "iron", "ferrous fumarate",
        "ferrous sulfate", "calcichew", "adcal"
    )
    
    // Frequency patterns
    private val FREQUENCY_PATTERNS = listOf(
        "once a day", "twice a day", "three times a day", "four times a day",
        "once daily", "twice daily", "three times daily",
        "every morning", "every evening", "every night", "at night", "at bedtime",
        "every \\d+ hours?", "every \\d+ to \\d+ hours?",
        "in the morning", "in the evening", "with breakfast", "with lunch", "with dinner",
        "with food", "with meals", "after food", "before food", "on an empty stomach",
        "as needed", "when needed", "when required", "as required", "prn"
    )
    
    // Duration patterns  
    private val DURATION_PATTERNS = listOf(
        "for \\d+ days?", "for \\d+ weeks?", "for \\d+ months?",
        "for a week", "for two weeks", "for a month",
        "until finished", "until gone", "until the course is complete",
        "until you feel better", "until symptoms improve",
        "long term", "ongoing", "indefinitely", "permanently"
    )
    
    // Test/Referral triggers
    private val TEST_REFERRAL_TRIGGERS = listOf(
        "blood test", "blood tests", "bloods",
        "x-ray", "xray", "scan", "ct scan", "mri", "ultrasound",
        "ecg", "ekg", "echocardiogram",
        "urine test", "urine sample", "stool sample",
        "biopsy", "endoscopy", "colonoscopy",
        "refer", "referral", "referring", "specialist",
        "hospital", "clinic", "consultant"
    )
    
    // Urgency indicators
    private val URGENCY_INDICATORS = listOf(
        "urgent", "urgently", "as soon as possible", "asap",
        "immediately", "straight away", "right away",
        "today", "tomorrow", "this week",
        "priority", "fast track", "two week wait"
    )
    
    // Follow-up triggers
    private val FOLLOWUP_TRIGGERS = listOf(
        "come back", "see you", "follow up", "follow-up", "followup",
        "book", "appointment", "review",
        "check", "check-up", "checkup",
        "return", "revisit"
    )
    
    // Timeframe patterns
    private val TIMEFRAME_PATTERNS = listOf(
        "in \\d+ days?", "in \\d+ weeks?", "in \\d+ months?",
        "in a week", "in two weeks", "in a month", "in a fortnight",
        "next week", "next month",
        "after \\d+ days?", "after \\d+ weeks?"
    )
    
    // Safety/Warning triggers - CRITICAL
    private val SAFETY_TRIGGERS = listOf(
        "if you", "should you", "in case",
        "watch out for", "look out for", "be aware",
        "warning sign", "red flag",
        "go to a&e", "go to hospital", "call 999", "call an ambulance",
        "emergency", "seek help", "get help",
        "don't", "do not", "avoid", "stop taking if",
        "allergic", "reaction", "side effect"
    )
    
    // Safety condition words
    private val SAFETY_CONDITIONS = listOf(
        "fever", "temperature", "breathing", "breathless",
        "chest pain", "severe pain", "worse", "worsens",
        "bleeding", "blood", "swelling", "swollen",
        "rash", "hives", "dizzy", "faint", "collapse",
        "vomiting", "diarrhoea", "diarrhea",
        "confused", "confusion", "drowsy"
    )
    
    // ========================================================================
    // MAIN EXTRACTION FUNCTION
    // ========================================================================
    
    /**
     * Extract medical information using schema-guided approach
     * 
     * @param transcript The full transcript text
     * @param recordingDurationSeconds Optional recording duration
     * @return MedicalExtraction with only explicitly stated information
     */
    fun extract(
        transcript: String,
        recordingDurationSeconds: Int? = null
    ): MedicalExtraction {
        val lowerTranscript = transcript.lowercase()
        val sentences = splitIntoSentences(transcript)
        
        return MedicalExtraction(
            appointmentMetadata = AppointmentMetadata(
                recordingDurationSeconds = recordingDurationSeconds
            ),
            medicationInstructions = extractMedications(sentences, lowerTranscript),
            testsAndReferrals = extractTestsAndReferrals(sentences, lowerTranscript),
            followUp = extractFollowUp(sentences, lowerTranscript),
            safetyAdvice = extractSafetyAdvice(sentences, lowerTranscript),
            additionalNotes = extractAdditionalNotes(sentences, lowerTranscript)
        )
    }
    
    // ========================================================================
    // MEDICATION EXTRACTION - HIGHEST PRIORITY
    // ========================================================================
    
    private fun extractMedications(
        sentences: List<String>,
        lowerTranscript: String
    ): List<MedicationInstruction> {
        val medications = mutableListOf<MedicationInstruction>()
        
        // First pass: Look for medications with triggers in same sentence
        for (sentence in sentences) {
            val lowerSentence = sentence.lowercase()
            
            // Check if sentence contains medication triggers
            val hasTrigger = MEDICATION_TRIGGERS.any { lowerSentence.contains(it) }
            if (!hasTrigger) continue
            
            // Find medication name - handle transcription errors (spaces, misspellings)
            val medicationName = findMedicationName(lowerSentence)
            
            if (medicationName != null) {
                medications.add(
                    MedicationInstruction(
                        medicineName = medicationName,
                        dosage = extractDosage(lowerSentence),
                        frequency = extractFrequency(lowerSentence),
                        duration = extractDuration(lowerSentence),
                        specialInstructions = extractSpecialInstructions(lowerSentence),
                        verbatimQuote = sentence.trim()
                    )
                )
            }
        }
        
        // Second pass: Look for medications even without explicit triggers
        // (in case trigger is in previous sentence, e.g., "I'm prescribing..." then "amoxicillin 500mg...")
        for (i in sentences.indices) {
            val sentence = sentences[i]
            val lowerSentence = sentence.lowercase()
            
            // Check if this sentence contains a medication name - handle transcription errors
            val medicationName = findMedicationName(lowerSentence)
            
            if (medicationName != null) {
                // Check if we already extracted this medication
                val alreadyExtracted = medications.any { 
                    it.medicineName.equals(medicationName, ignoreCase = true) 
                }
                
                if (!alreadyExtracted) {
                    // Check if previous sentence had a trigger, or if this sentence has dosage/frequency
                    val hasDosageOrFrequency = extractDosage(lowerSentence) != null || 
                                              extractFrequency(lowerSentence) != null
                    val prevSentenceHasTrigger = i > 0 && MEDICATION_TRIGGERS.any { 
                        sentences[i - 1].lowercase().contains(it) 
                    }
                    
                    // Extract if there's dosage/frequency (strong indicator) or trigger in previous sentence
                    if (hasDosageOrFrequency || prevSentenceHasTrigger) {
                        medications.add(
                            MedicationInstruction(
                                medicineName = medicationName,
                                dosage = extractDosage(lowerSentence),
                                frequency = extractFrequency(lowerSentence),
                                duration = extractDuration(lowerSentence),
                                specialInstructions = extractSpecialInstructions(lowerSentence),
                                verbatimQuote = sentence.trim()
                            )
                        )
                    }
                }
            }
        }
        
        return medications.distinctBy { it.medicineName.lowercase() }
    }
    
    private fun extractDosage(sentence: String): String? {
        // Pattern: number + unit (mg, ml, tablets, etc.)
        val dosagePattern = Regex(
            """(\d+(?:\.\d+)?)\s*(mg|milligrams?|mcg|micrograms?|ml|millilitres?|tablets?|pills?|capsules?)""",
            RegexOption.IGNORE_CASE
        )
        return dosagePattern.find(sentence)?.value
    }
    
    private fun extractFrequency(sentence: String): String? {
        for (pattern in FREQUENCY_PATTERNS) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            regex.find(sentence)?.let { return it.value }
        }
        return null
    }
    
    private fun extractDuration(sentence: String): String? {
        for (pattern in DURATION_PATTERNS) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            regex.find(sentence)?.let { return it.value }
        }
        return null
    }
    
    private fun extractSpecialInstructions(sentence: String): String? {
        val instructions = listOf(
            "with food", "with meals", "after food", "before food",
            "on an empty stomach", "with water", "with plenty of water",
            "do not crush", "do not chew", "swallow whole"
        )
        return instructions.firstOrNull { sentence.contains(it) }
    }
    
    // ========================================================================
    // TESTS AND REFERRALS EXTRACTION
    // ========================================================================
    
    private fun extractTestsAndReferrals(
        sentences: List<String>,
        lowerTranscript: String
    ): List<TestOrReferral> {
        val testsAndReferrals = mutableListOf<TestOrReferral>()
        
        for (sentence in sentences) {
            val lowerSentence = sentence.lowercase()
            
            // Find test/referral type
            val testType = TEST_REFERRAL_TRIGGERS.firstOrNull { 
                lowerSentence.contains(it) 
            }
            
            if (testType != null) {
                // Check for urgency - ONLY if explicitly stated
                val urgency = URGENCY_INDICATORS.firstOrNull { 
                    lowerSentence.contains(it) 
                }
                
                testsAndReferrals.add(
                    TestOrReferral(
                        testOrReferralType = testType.replaceFirstChar { it.uppercase() },
                        reasonIfStated = null, // Only extract if explicitly stated with "because", "for", etc.
                        urgency = urgency,
                        verbatimQuote = sentence.trim()
                    )
                )
            }
        }
        
        return testsAndReferrals.distinctBy { it.testOrReferralType.lowercase() }
    }
    
    // ========================================================================
    // FOLLOW-UP EXTRACTION
    // ========================================================================
    
    private fun extractFollowUp(
        sentences: List<String>,
        lowerTranscript: String
    ): FollowUpInstruction? {
        for (sentence in sentences) {
            val lowerSentence = sentence.lowercase()
            
            val hasFollowUpTrigger = FOLLOWUP_TRIGGERS.any { lowerSentence.contains(it) }
            if (!hasFollowUpTrigger) continue
            
            // Extract timeframe if stated
            var timeframe: String? = null
            for (pattern in TIMEFRAME_PATTERNS) {
                val regex = Regex(pattern, RegexOption.IGNORE_CASE)
                regex.find(lowerSentence)?.let { 
                    timeframe = it.value
                }
            }
            
            // Extract location/method if stated
            val locationMethod = when {
                lowerSentence.contains("reception") -> "reception"
                lowerSentence.contains("online") -> "online"
                lowerSentence.contains("phone") || lowerSentence.contains("call") -> "phone"
                lowerSentence.contains("gp") || lowerSentence.contains("surgery") -> "GP surgery"
                else -> null
            }
            
            return FollowUpInstruction(
                followUpRequired = true,
                timeframe = timeframe,
                locationOrMethod = locationMethod,
                verbatimQuote = sentence.trim()
            )
        }
        
        return null
    }
    
    // ========================================================================
    // SAFETY ADVICE EXTRACTION
    // ========================================================================
    
    private fun extractSafetyAdvice(
        sentences: List<String>,
        lowerTranscript: String
    ): List<SafetyWarning> {
        val warnings = mutableListOf<SafetyWarning>()
        
        for (sentence in sentences) {
            val lowerSentence = sentence.lowercase()
            
            // Check for safety trigger phrases
            val hasSafetyTrigger = SAFETY_TRIGGERS.any { lowerSentence.contains(it) }
            val hasSafetyCondition = SAFETY_CONDITIONS.any { lowerSentence.contains(it) }
            
            // Must have both a trigger and a condition for high confidence
            if (hasSafetyTrigger && hasSafetyCondition) {
                warnings.add(
                    SafetyWarning(
                        warning = sentence.trim(),
                        verbatimQuote = sentence.trim()
                    )
                )
            }
            // Or strong emergency triggers alone
            else if (lowerSentence.contains("a&e") || 
                     lowerSentence.contains("999") ||
                     lowerSentence.contains("emergency") ||
                     lowerSentence.contains("ambulance")) {
                warnings.add(
                    SafetyWarning(
                        warning = sentence.trim(),
                        verbatimQuote = sentence.trim()
                    )
                )
            }
        }
        
        return warnings.distinctBy { it.warning.lowercase() }
    }
    
    // ========================================================================
    // ADDITIONAL NOTES - CATCH-ALL (Prevents schema breakage)
    // ========================================================================
    
    private fun extractAdditionalNotes(
        sentences: List<String>,
        lowerTranscript: String
    ): List<String> {
        val notes = mutableListOf<String>()
        
        // Lifestyle advice patterns
        val lifestylePatterns = listOf(
            "exercise", "walk", "walking", "activity",
            "diet", "eat", "eating", "food", "drink", "water", "alcohol",
            "sleep", "rest", "relax",
            "stress", "work", "smoking", "smoke", "quit"
        )
        
        // Reassurance patterns
        val reassurancePatterns = listOf(
            "nothing to worry", "don't worry", "not serious",
            "common", "normal", "expected", "should improve",
            "good news", "looking good"
        )
        
        for (sentence in sentences) {
            val lowerSentence = sentence.lowercase()
            
            // Check for lifestyle advice
            val hasLifestyle = lifestylePatterns.any { lowerSentence.contains(it) }
            val hasReassurance = reassurancePatterns.any { lowerSentence.contains(it) }
            
            if (hasLifestyle || hasReassurance) {
                // Only add if not already captured elsewhere
                val alreadyCaptured = MEDICATION_TRIGGERS.any { lowerSentence.contains(it) } ||
                        TEST_REFERRAL_TRIGGERS.any { lowerSentence.contains(it) } ||
                        FOLLOWUP_TRIGGERS.any { lowerSentence.contains(it) } ||
                        SAFETY_TRIGGERS.any { lowerSentence.contains(it) }
                
                if (!alreadyCaptured) {
                    notes.add(sentence.trim())
                }
            }
        }
        
        return notes.take(5) // Limit to prevent noise
    }
    
    // ========================================================================
    // UTILITY FUNCTIONS
    // ========================================================================
    
    private fun splitIntoSentences(text: String): List<String> {
        // Split on sentence boundaries while preserving the content
        return text
            .replace(Regex("""([.!?])\s+"""), "$1|||")
            .split("|||")
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.length > 3 }
    }
    
    /**
     * Find medication name with fuzzy matching to handle transcription errors
     * Handles common issues like:
     * - Spaces inserted: "a moxosilin" -> "amoxicillin"
     * - Misspellings: "moxosilin" -> "amoxicillin"
     * - Case variations
     * - Articles: "a amoxicillin" -> "amoxicillin"
     */
    private fun findMedicationName(sentence: String): String? {
        // First try exact match
        val exactMatch = COMMON_MEDICATIONS.firstOrNull { 
            sentence.contains(it) 
        }
        if (exactMatch != null) {
            return exactMatch.replaceFirstChar { it.uppercase() }
        }
        
        // Normalize sentence: remove common articles and extra spaces
        val normalized = sentence
            .replace(Regex("\\b(a|an|the)\\s+"), "") // Remove articles
            .replace(" ", "") // Remove all spaces
            .lowercase()
        
        // Check each medication
        for (medication in COMMON_MEDICATIONS) {
            val medNormalized = medication.replace(" ", "").lowercase()
            
            // Exact match after normalization
            if (normalized.contains(medNormalized)) {
                return medication.replaceFirstChar { it.uppercase() }
            }
            
            // Fuzzy match for transcription errors (e.g., "moxosilin" -> "amoxicillin")
            if (fuzzyMatch(medNormalized, normalized)) {
                return medication.replaceFirstChar { it.uppercase() }
            }
        }
        
        return null
    }
    
    /**
     * Simple fuzzy matching - checks if medication name appears in text
     * with allowance for transcription errors (missing/extra characters)
     * Handles cases like "moxosilin" -> "amoxicillin"
     */
    private fun fuzzyMatch(medication: String, text: String): Boolean {
        // If medication is short, require exact match
        if (medication.length < 6) return false
        
        // Check if medication appears as substring
        if (text.contains(medication)) return true
        
        // Check for common transcription errors in medication names
        // Map common misspellings
        val commonErrors = mapOf(
            "moxosilin" to "amoxicillin",
            "moxocillin" to "amoxicillin",
            "a moxosilin" to "amoxicillin",
            "a moxocillin" to "amoxicillin",
            "moxicillin" to "amoxicillin",
            "amoxacillin" to "amoxicillin",
            "amoxocillin" to "amoxicillin"
        )
        
        // Check if text contains a common misspelling that maps to this medication
        for ((misspelling, correct) in commonErrors) {
            if (text.contains(misspelling) && medication == correct) {
                return true
            }
        }
        
        // Check similarity - if 75%+ of characters match in order, consider it a match
        val similarity = calculateSimilarity(medication, text)
        return similarity >= 0.75
    }
    
    /**
     * Calculate similarity between two strings (simple version)
     * Returns value between 0.0 and 1.0
     * Handles cases like "moxosilin" vs "amoxicillin"
     */
    private fun calculateSimilarity(str1: String, str2: String): Double {
        if (str1.isEmpty() || str2.isEmpty()) return 0.0
        
        // Find longest common substring
        val longer = if (str1.length > str2.length) str1 else str2
        val shorter = if (str1.length > str2.length) str2 else str1
        
        // Check if shorter is contained in longer
        if (longer.contains(shorter)) {
            return shorter.length.toDouble() / longer.length
        }
        
        // Check for longest common subsequence (characters in order, not necessarily consecutive)
        // This handles "moxosilin" vs "amoxicillin" better
        val lcsLength = longestCommonSubsequence(shorter, longer)
        val similarity = lcsLength.toDouble() / longer.length
        
        // Also check if they start/end similarly (strong indicator)
        val startSimilar = if (shorter.length >= 3 && longer.length >= 3) {
            shorter.take(3) == longer.take(3)
        } else false
        
        val endSimilar = if (shorter.length >= 3 && longer.length >= 3) {
            shorter.takeLast(3) == longer.takeLast(3)
        } else false
        
        // Boost similarity if start or end matches
        val boostedSimilarity = if (startSimilar || endSimilar) {
            similarity + 0.1
        } else {
            similarity
        }
        
        return minOf(boostedSimilarity, 1.0)
    }
    
    /**
     * Calculate longest common subsequence length
     * Handles cases where characters match but aren't consecutive
     */
    private fun longestCommonSubsequence(str1: String, str2: String): Int {
        val m = str1.length
        val n = str2.length
        val dp = Array(m + 1) { IntArray(n + 1) }
        
        for (i in 1..m) {
            for (j in 1..n) {
                if (str1[i - 1] == str2[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1] + 1
                } else {
                    dp[i][j] = maxOf(dp[i - 1][j], dp[i][j - 1])
                }
            }
        }
        
        return dp[m][n]
    }
}

