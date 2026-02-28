package com.example.medicalappointmentcompanion.extraction

import com.example.medicalappointmentcompanion.extraction.WordVariations
import com.example.medicalappointmentcompanion.model.*

/**
 * Pulls out meds, tests, follow-ups, safety stuff from transcript.
 * Only what's actually said – no guessing.
 */
object SchemaGuidedExtractor {
    
    // meds
    private val COMMON_MEDICATIONS = listOf(
        // Pain relief
        "paracetamol", "ibuprofen", "aspirin", "codeine", "tramadol",
        "co-codamol", "solpadol", "difene", "diclofenac", "naproxen",
        "ponstan", "mefenamic acid", "co-dydramol", "nurofen",
        
        // Antibiotics 
        "amoxicillin",
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
        "mirena", "kyleena", "jaydess", "copper coil", "Yazmin", "Naproxen", "Provera",
        "norethisterone", "provera", "tranexamic acid",
        "evorel", "estradot", "elleste", "femoston", "kliovance", "oestrogel",
        "vagifem", "ovestin",
        "clomid", "clomiphene",
        "fluconazole", "canesten",
        
        // Supplements 
        "folic acid", "vitamin D", "desunin", "iron", "ferrous fumarate",
        "ferrous sulfate", "calcichew", "adcal"
    )

    // Vitamins/supplements – don't pull dosage from following sentences (avoids 500mg from next med)
    private val VITAMINS_SUPPLEMENTS = setOf(
        "vitamin D", "vitamin C", "vitamin B", "vitamin B12", "vitamin B6",
        "folic acid", "iron", "ferrous fumarate", "ferrous sulfate",
        "calcichew", "adcal", "desunin"
    )
    
    // Frequency patterns 
    private val FREQUENCY_PATTERNS = listOf(
        "once a day", "twice a day", "three times a day", "four times a day",
        "once daily", "twice daily", "three times daily",
        "\\d+ times a day", "\\d+ times daily",
        "every morning", "every evening", "every night", "at night", "at bedtime",
        "every \\d+ hours?", "every \\d+ to \\d+ hours?",
        "in the morning", "in the evening", "with breakfast", "with lunch", "with dinner",
        "with your evening meal", "with your morning meal",
        "with food", "with meals", "after food", "before food", "before breakfast",
        "on an empty stomach",
        "as needed", "when needed", "when required", "as required", "prn",
        "tds", "bd", "od", "qds", "mane", "nocte"
    )
    
    // Duration patterns  
    private val DURATION_PATTERNS = listOf(
        "for \\d+ days?", "for \\d+ weeks?", "for \\d+ months?",
        "for next \\d+ days?", "for the next \\d+ days?",
        "for next (one|two|three|four|five|six|seven) days?",
        "for the next (one|two|three|four|five|six|seven) days?",
        "only for next \\d+ days?", "only for the next \\d+ days?",
        "only for next (one|two|three|four|five|six|seven) days?",
        "only for the next (one|two|three|four|five|six|seven) days?",
        "for a week", "for two weeks", "for a month",
        "until finished", "until gone", "until the course is complete",
        "until you feel better", "until symptoms improve",
        "(?:don't|dont|do not) take it longer than that",
        "(?:don't|dont|do not) take .+ longer than that",
        "(?:don't|dont|do not) take .+ longer than \\d+ days?",
        "long term", "ongoing", "indefinitely", "permanently"
    )
    
    // Test/Referral triggers
    private val TEST_REFERRAL_TRIGGERS = listOf(
        "blood test", "blood tests", "bloods",
        "swabs", "swab",
        "ultrasound scan", "ct scan", "mri scan", "x-ray scan",
        "x-ray", "xray", "scan", "mri", "ultrasound",
        "ecg", "ekg", "echocardiogram",
        "urine test", "urine sample", "stool sample",
        "biopsy", "endoscopy", "colonoscopy",
        "referral", "referring", "refer", "specialist",
        "hospital", "consultant",
        "vasectomy", "gynaecology", "gynecology"
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
        "come back", "come back to", "return", "revisit", "see you"
    )
    
    // Timeframe patterns
    private val TIMEFRAME_PATTERNS = listOf(
        "in \\d+ days?", "in \\d+ weeks?", "in \\d+ months?",
        "in a week", "in two weeks", "in three weeks", "in four weeks",
        "in five weeks", "in six weeks", "in a month", "in two months",
        "in three months", "in four months", "in six months", "in a fortnight",
        "in a couple of weeks", "in a few days", "in a couple of days",
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

    // Safety verbs – "if you develop/get X"
    private val SAFETY_VERBS = listOf(
        "develop", "get", "experience", "have", "notice", "see"
    )

    // Reason phrases for tests – "blood test to check X"
    private val TEST_REASON_PHRASES = listOf(
        "to check", "to assess", "to monitor", "to see", "to look at",
        "for", "because of", "to rule out"
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
        val normalizedTranscript = WordVariations.normalizePhrases(transcript.lowercase())
        val sentences = splitIntoSentences(normalizedTranscript)
        val lowerTranscript = normalizedTranscript
        
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
        for ((i, sentence) in sentences.withIndex()) {
            val lowerSentence = sentence.lowercase()
            
            if (!WordVariations.isMedicationContext(lowerSentence)) continue
            
            // fix phrases then med spellings
            val textForExtraction = normaliseForMedExtraction(lowerSentence)
            
            val medicationName = findMedicationName(textForExtraction)
            
            if (medicationName != null) {
                val med = buildMedicationFromSentences(
                    medicationName, sentence.trim(), sentences, i
                )
                medications.add(med)
            }
        }
        
        // second pass in case "I'm prescribing" was in prev sentence
        for (i in sentences.indices) {
            val sentence = sentences[i]
            val lowerSentence = sentence.lowercase()
            val textForExtraction = normaliseForMedExtraction(lowerSentence)
            
            val medicationName = findMedicationName(textForExtraction)
            
            if (medicationName != null) {
                val alreadyExtracted = medications.any { 
                    it.medicineName.equals(medicationName, ignoreCase = true) 
                }
                
                if (!alreadyExtracted) {
                    val normSentence = textForExtraction
                    val hasDosageOrFrequency = extractDosage(normSentence) != null || 
                                              extractFrequency(normSentence) != null
                    val prevSentenceHasTrigger = i > 0 && WordVariations.isMedicationContext(
                        sentences[i - 1].lowercase()
                    )
                    
                    if (hasDosageOrFrequency || prevSentenceHasTrigger) {
                        val med = buildMedicationFromSentences(
                            medicationName, sentence.trim(), sentences, i
                        )
                        medications.add(med)
                    }
                }
            }
        }
        
        return medications.distinctBy { it.medicineName.lowercase() }
    }
    
    /** phrases first, then med aliases – same string used for matching + regex */
    private fun normaliseForMedExtraction(sentence: String): String {
        val phraseNorm = WordVariations.normalizePhrases(sentence.lowercase())
        return if (WordVariations.isMedicationContext(phraseNorm)) {
            WordVariations.applyMedicationAliases(phraseNorm)
        } else phraseNorm
    }
    
    /** grab dosage/freq etc from next 1–2 sentences if not in same one */
    private fun buildMedicationFromSentences(
        medicationName: String,
        verbatimQuote: String,
        sentences: List<String>,
        sentenceIndex: Int
    ): MedicationInstruction {
        val norm = { s: String -> normaliseForMedExtraction(s) }
        val isVitaminOrSupplement = VITAMINS_SUPPLEMENTS.any { 
            medicationName.equals(it, ignoreCase = true) 
        }
        var dosage = extractDosage(norm(sentences[sentenceIndex]))
        var frequency = extractFrequency(norm(sentences[sentenceIndex]))
        var duration = extractDuration(norm(sentences[sentenceIndex]))
        var specialInstructions = extractSpecialInstructions(norm(sentences[sentenceIndex]))
        
        if (!isVitaminOrSupplement) {
            for (j in (sentenceIndex + 1)..minOf(sentenceIndex + 2, sentences.size - 1)) {
                val next = norm(sentences[j])
                if (dosage == null) dosage = extractDosage(next)
                if (frequency == null) frequency = extractFrequency(next)
                if (duration == null) duration = extractDuration(next)
                if (specialInstructions == null) specialInstructions = extractSpecialInstructions(next)
            }
        }
        
        return MedicationInstruction(
            medicineName = medicationName,
            dosage = dosage,
            frequency = frequency,
            duration = duration,
            specialInstructions = specialInstructions,
            verbatimQuote = verbatimQuote
        )
    }
    
    private fun extractDosage(sentence: String): String? {
        // Pattern: number + unit (mg, ml, tablets, iu, etc.)
        val dosagePattern = Regex(
            """(\d+(?:\.\d+)?)\s*(mg|milligrams?|mcg|micrograms?|ml|millilitres?|tablets?|pills?|capsules?|iu|international\s*units?|units?)""",
            RegexOption.IGNORE_CASE
        )
        dosagePattern.find(sentence)?.let { return it.value.trim() }
        // Word numbers + units: "one tablet", "two tablets"
        val wordDosage = Regex(
            """(one|two|three|four|five|half)(?:\s+a)?\s+(tablets?|pills?|capsules?|mg|ml)""",
            RegexOption.IGNORE_CASE
        )
        return wordDosage.find(sentence)?.value
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
            "before breakfast", "with breakfast", "with your evening meal",
            "with lunch", "with dinner",
            "on an empty stomach", "with water", "with plenty of water",
            "do not crush", "do not chew", "swallow whole",
            "avoid alcohol", "take it with food"
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
            
            val testType = TEST_REFERRAL_TRIGGERS.firstOrNull { 
                lowerSentence.contains(it) 
            } ?: continue
            
            if (listOf("hospital", "consultant", "specialist").contains(testType) &&
                (lowerSentence.contains("referral to") || lowerSentence.contains("refer to") || 
                 lowerSentence.contains("referring to") || lowerSentence.startsWith("to the ") || lowerSentence.startsWith("to "))) {
                continue
            }
            
            val urgency = URGENCY_INDICATORS.firstOrNull { 
                lowerSentence.contains(it) 
            }
            
            val reason = extractTestReason(lowerSentence, testType)
            var destination = extractReferralDestination(lowerSentence, testType)
            if (destination == null && listOf("vasectomy", "gynaecology", "gynecology").contains(testType)) {
                destination = extractSpecialtyDestination(lowerSentence, testType)
            }
            var displayType = when {
                testType == "refer" && destination != null -> "Referral"
                listOf("vasectomy", "gynaecology", "gynecology").contains(testType) -> "Referral"
                testType == "scan" -> extractScanType(lowerSentence) ?: "Scan"
                else -> testType.replaceFirstChar { it.uppercase() }
            }
            
            testsAndReferrals.add(
                TestOrReferral(
                    testOrReferralType = displayType,
                    reasonIfStated = reason,
                    destinationIfStated = destination,
                    urgency = urgency,
                    verbatimQuote = sentence.trim()
                )
            )
        }
        
        return testsAndReferrals.distinctBy { 
            "${it.testOrReferralType.lowercase()}-${it.destinationIfStated?.lowercase() ?: ""}-${it.reasonIfStated?.lowercase() ?: ""}" 
        }
    }
    
    private fun extractScanType(sentence: String): String? {
        val scanTypeRegex = Regex(
            """(\w+(?:-\w+)?)\s+scan""",
            RegexOption.IGNORE_CASE
        )
        return scanTypeRegex.find(sentence)?.groupValues?.getOrNull(1)?.let { type ->
            "${type.replaceFirstChar { it.uppercase() }} scan"
        }
    }
    
    private fun extractTestReason(sentence: String, testType: String): String? {
        val phraseRegex = Regex(
            """(?:${TEST_REASON_PHRASES.joinToString("|") { Regex.escape(it) }})\s+(.+?)(?:\.|$|,)""",
            RegexOption.IGNORE_CASE
        )
        phraseRegex.find(sentence)?.groupValues?.getOrNull(1)?.let { captured ->
            val trimmed = captured.trim()
            if (trimmed.length in 3..150) return trimmed
        }
        val forRegex = Regex(
            """(?:blood test|bloods|test|scan|x-ray|swab|referral)\s+for\s+(.+?)(?:\.|$|,)""",
            RegexOption.IGNORE_CASE
        )
        forRegex.find(sentence)?.groupValues?.getOrNull(1)?.let { captured ->
            val trimmed = captured.trim()
            if (trimmed.length in 3..150) return trimmed
        }
        return null
    }
    
    private fun extractReferralDestination(sentence: String, testType: String): String? {
        if (!sentence.contains("refer") && !sentence.contains("referral") && !sentence.contains("referring")) return null
        val toRegex = Regex(
            """(?:referral|referring|refer)\s+(?:you)?\s*to\s+(.+?)(?:\.|$|,| for| and)""",
            RegexOption.IGNORE_CASE
        )
        toRegex.find(sentence)?.groupValues?.getOrNull(1)?.let { captured ->
            val trimmed = captured.trim()
            if (trimmed.length in 2..80) return trimmed.replaceFirstChar { it.uppercase() }
        }
        return null
    }
    
    private fun extractSpecialtyDestination(sentence: String, testType: String): String? {
        val pattern = Regex(
            """(${Regex.escape(testType)}\s+(?:clinic|department|unit|service|team))""",
            RegexOption.IGNORE_CASE
        )
        pattern.find(sentence)?.groupValues?.getOrNull(1)?.let { captured ->
            val trimmed = captured.trim()
            if (trimmed.length in 3..80) return trimmed.replaceFirstChar { it.uppercase() }
        }
        return testType.replaceFirstChar { it.uppercase() }
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
            
            val hasTimeframe = timeframe != null
            val hasConditionalReturn = lowerSentence.contains(" if ")
            if (!hasTimeframe && !hasConditionalReturn) continue
            
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
            
            val hasSafetyTrigger = SAFETY_TRIGGERS.any { lowerSentence.contains(it) }
            val hasSafetyCondition = SAFETY_CONDITIONS.any { lowerSentence.contains(it) }
            val hasSafetyVerb = SAFETY_VERBS.any { lowerSentence.contains(it) }
            
            if (hasSafetyTrigger && (hasSafetyCondition || hasSafetyVerb)) {
                warnings.add(
                    SafetyWarning(
                        warning = sentence.trim(),
                        verbatimQuote = sentence.trim()
                    )
                )
            }
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
                val alreadyCaptured = WordVariations.isMedicationContext(lowerSentence) ||
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
        // don't split on Dr. Mr. e.g. etc
        val abbrevPlaceholder = "§ABBREV§"
        val protected = text.replace(
            Regex("""\b(Dr|Mr|Mrs|Ms|Prof)\.\s+""", RegexOption.IGNORE_CASE)
        ) { it.value.replace(". ", "$abbrevPlaceholder ") }
            .replace(
                Regex("""\b(e\.g\.|i\.e\.)\s+""", RegexOption.IGNORE_CASE)
            ) { it.value.replace(". ", "$abbrevPlaceholder ") }
        return protected
            .replace(Regex("""([.!?])\s+"""), "$1|||")
            .replace("$abbrevPlaceholder ", ". ")
            .split("|||")
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.length > 3 }
    }
    
    /** match med name – exact first, then fuzzy if in med context */
    private fun findMedicationName(sentence: String): String? {
        val lowerSentence = sentence.lowercase()
        val inMedContext = WordVariations.isMedicationContext(lowerSentence)

        val exactMatch = COMMON_MEDICATIONS.firstOrNull {
            lowerSentence.contains(it)
        }
        if (exactMatch != null) {
            return exactMatch.replaceFirstChar { it.uppercase() }
        }

        if (inMedContext) {
            WordVariations.findMedicationAliasIn(lowerSentence)?.let { canonical ->
                return canonical.replaceFirstChar { it.uppercase() }
            }
        }

        val normalized = lowerSentence
            .replace(Regex("\\b(a|an|the)\\s+"), "")
            .replace(" ", "")

        for (medication in COMMON_MEDICATIONS) {
            val medNormalized = medication.replace(" ", "").lowercase()
            if (normalized.contains(medNormalized)) {
                return medication.replaceFirstChar { it.uppercase() }
            }
        }

        if (inMedContext) {
            for (medication in COMMON_MEDICATIONS) {
                val medNormalized = medication.replace(" ", "").lowercase()
                if (fuzzyMatch(medNormalized, normalized)) {
                    return medication.replaceFirstChar { it.uppercase() }
                }
            }
        }

        return null
    }
    
    /** e.g. moxosilin -> amoxicillin */
    private fun fuzzyMatch(medication: String, text: String): Boolean {
        if (medication.length < 6) return false
        if (text.contains(medication)) return true
        val similarity = calculateSimilarity(medication, text)
        return similarity >= 0.75
    }
    
    private fun calculateSimilarity(str1: String, str2: String): Double {
        if (str1.isEmpty() || str2.isEmpty()) return 0.0
        val longer = if (str1.length > str2.length) str1 else str2
        val shorter = if (str1.length > str2.length) str2 else str1
        
        if (longer.contains(shorter)) {
            return shorter.length.toDouble() / longer.length
        }
        
        val lcsLength = longestCommonSubsequence(shorter, longer)
        val similarity = lcsLength.toDouble() / longer.length
        
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

