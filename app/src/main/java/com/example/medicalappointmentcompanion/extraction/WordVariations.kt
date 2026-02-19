package com.example.medicalappointmentcompanion.extraction

/**
 * Medication Transcription Variations Map
 * 
 * Maps common transcription errors, misspellings, and pronunciation variations
 * to their correct medication spellings.
 * 
 * This handles:
 * - Different accents and pronunciations
 * - Common transcription errors (missing letters, wrong letters)
 * - Space insertions/removals
 * - Hyphen variations
 * - Articles with spaces (e.g., "a moxosilin")
 */
object WordVariations {
    
    /**
     * Master Map of transcription variations to correct word spellings
     * Key: transcription variation (lowercase)
     * Value: phrase expected by extraction regex
     */
    val VARIATIONS: Map<String, String> = mapOf(

        // ==============================
        // MEDICATION VARIATIONS
        // ==============================

        // Amoxicillin variations
        "amoxosilin" to "amoxicillin",
        "a moxosilin" to "amoxicillin",
        "a moxicillin" to "amoxicillin",
        "amoxacillin" to "amoxicillin",
        "amoxocillin" to "amoxicillin",
        "amoxisillin" to "amoxicillin",
        "moxosilin" to "amoxicillin",
        "moxocillin" to "amoxicillin",
        "moxicillin" to "amoxicillin",
        
        // Pain relief variations
        "para cetamol" to "paracetamol",
        "paracetemol" to "paracetamol",
        "co codamol" to "co-codamol",
        "cocodamol" to "co-codamol",
        "mefenamicacid" to "mefenamic acid",
        "co dydramol" to "co-dydramol",
        "codydramol" to "co-dydramol",
        
        // Antibiotic variations
        "co amoxiclav" to "co-amoxiclav",
        "coamoxiclav" to "co-amoxiclav",
        "doxycyclin" to "doxycycline",
        "clarithromicin" to "clarithromycin",
        "azithromicin" to "azithromycin",
        "metronidazol" to "metronidazole",
        "penicilin" to "penicillin",
        
        // Stomach/acid/nausea variations
        "omeprazol" to "omeprazole",
        "lansoprazol" to "lansoprazole",
        "esomeprazol" to "esomeprazole",
        "pantoprazol" to "pantoprazole",
        "domperidon" to "domperidone",
        "cyclizin" to "cyclizine",
        "prochlorperazin" to "prochlorperazine",
        
        // Diabetes variations
        "metformine" to "metformin",
        "gliclazid" to "gliclazide",
        
        // Blood pressure/heart variations
        "amlodipin" to "amlodipine",
        "furosemid" to "furosemide",
        "bendroflumethiazid" to "bendroflumethiazide",
        
        // Mental health variations
        "sertralin" to "sertraline",
        "fluoxetin" to "fluoxetine",
        "venlafaxin" to "venlafaxine",
        "mirtazapin" to "mirtazapine",
        "duloxetin" to "duloxetine",
        "amitriptylin" to "amitriptyline",
        
        // Respiratory variations
        "beclometason" to "beclometasone",
        "prednisolon" to "prednisolone",
        "prednison" to "prednisone",
        
        // Thyroid variations
        "levothyroxin" to "levothyroxine",
        "thyroxin" to "thyroxine",
        
        // Nerve pain/epilepsy variations
        "carbamazepin" to "carbamazepine",
        
        // Sedatives/anxiety variations
        "zopiclon" to "zopiclone",
        
        // Allergy/antihistamine variations
        "cetirizin" to "cetirizine",
        "loratadin" to "loratadine",
        "fexofenadin" to "fexofenadine",
        "chlorphenamin" to "chlorphenamine",
        
        // Skin condition variations
        "hydrocortison" to "hydrocortisone",
        "fusidicacid" to "fusidic acid",
        "canestencream" to "canesten cream",
        
        // Eye/ear variations
        "locortenvioform" to "locorten vioform",
        "hypromellos" to "hypromellose",
        "hylotear" to "hylo-tear",
        "hylo tear" to "hylo-tear",
        
        // Gout variations
        "colchicin" to "colchicine",
        
        // Men's health/prostate variations
        "finasterid" to "finasteride",
        "dutasterid" to "dutasteride",
        
        // Women's health variations
        "coppercoil" to "copper coil",
        "norethisteron" to "norethisterone",
        "tranexamicacid" to "tranexamic acid",
        "clomiphen" to "clomiphene",
        "fluconazol" to "fluconazole",
        
        // Supplement variations
        "folicacid" to "folic acid",
        "vitamind" to "vitamin d",
        "ferrousfumarate" to "ferrous fumarate",
        "ferroussulfate" to "ferrous sulfate",


        // ==============================
        // FREQUENCY VARIATIONS
        // ==============================

        // Once / daily
        "once aday" to "once a day",
        "once per day" to "once a day",
        "one a day" to "once a day",
        "1 a day" to "once a day",
        "ones a day" to "once a day",

        // Twice
        "twice aday" to "twice a day",
        "2 a day" to "twice a day",
        "two a day" to "twice a day",
        "twice today" to "twice a day",

        // Three times
        "3 times a day" to "three times a day",
        "three times aday" to "three times a day",

        // Four times
        "4 times a day" to "four times a day",
        "for times a day" to "four times a day",

        // Morning / evening / night
        "every warning" to "every morning",
        "in the warning" to "in the morning",
        "every nite" to "every night",
        "at nite" to "at night",
        "at bed time" to "at bedtime",
        "at bed-time" to "at bedtime",

        // Meals / food
        "with break fast" to "with breakfast",
        "with brekfast" to "with breakfast",
        "with diner" to "with dinner",
        "after meals" to "after food",
        "before meals" to "before food",
        "empty stomach" to "on an empty stomach",

        // Every X hours
        "every six hours" to "every 6 hours",
        "every six ours" to "every 6 hours",
        "every 6 hour" to "every 6 hours",
        "every eight hours" to "every 8 hours",
        "every 8 hour" to "every 8 hours",
        "every twelve hours" to "every 12 hours",
        "every 12 hour" to "every 12 hours",
        "every 2-4 hours" to "every 2 to 4 hours",
        "every two to four hours" to "every 2 to 4 hours",

        // PRN
        "p r n" to "prn",
        "p.r.n" to "prn",
        "pr n" to "prn",
        "as kneaded" to "as needed",
        "as need it" to "as needed",
        "when you need it" to "when needed",


        // ==============================
        // DURATION VARIATIONS
        // ==============================

        // Weeks / months
        "for a weak" to "for a week",
        "for 1 week" to "for a week",
        "for two weeks" to "for 2 weeks",
        "for three weeks" to "for 3 weeks",
        "for three months" to "for 3 months",
        "for six months" to "for 6 months",

        // Until phrases
        "until its gone" to "until gone",
        "until it's gone" to "until gone",
        "until the course is completed" to "until the course is complete",
        "until symptoms get better" to "until symptoms improve",

        // Long term
        "longturn" to "long term",
        "long-term" to "long term",
        "on going" to "ongoing",
        "in definitely" to "indefinitely",
        "indefinately" to "indefinitely",
        "perminently" to "permanently"

        )
    /**
     * Safe normalization using word boundaries.
     * Prevents replacing substrings inside other words.
     */
    fun normalize(text: String): String {
        var normalized = text.lowercase()

        VARIATIONS.forEach { (variant, canonical) ->
            val pattern = Regex("\\b${Regex.escape(variant)}\\b")
            normalized = normalized.replace(pattern, canonical)
        }

        return normalized
    }
}


