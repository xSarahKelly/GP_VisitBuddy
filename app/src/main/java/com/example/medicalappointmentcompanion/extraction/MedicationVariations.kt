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
object MedicationVariations {
    
    /**
     * Map of transcription variations to correct medication spellings
     * Key: transcription variation (lowercase)
     * Value: correct medication name (as it appears in COMMON_MEDICATIONS)
     */
    val VARIATIONS: Map<String, String> = mapOf(
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
        "ferroussulfate" to "ferrous sulfate"
    )
}

