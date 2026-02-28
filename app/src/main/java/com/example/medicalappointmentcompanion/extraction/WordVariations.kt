package com.example.medicalappointmentcompanion.extraction

/**
 * Fixes transcription mistakes – phrases (once aday -> once a day) and med spellings (parasetamol -> paracetamol).
 * Med aliases only when sentence looks like med instructions.
 */
object WordVariations {

    /** words that mean we're talking about meds – prescribe, take, mg, tablet, etc */
    val MEDICATION_CONTEXT_TRIGGERS = listOf(
        "prescribe", "prescribed", "prescribing",
        "start you on", "starting you on", "start on",
        "put you on", "putting you on",
        "continue taking", "continuing taking", "keep taking",
        "increase your", "increasing your", "reduce your", "reducing your",
        "take", "taking",
        "medication", "medicine",
        "mg", "milligram", "milligrams", "mcg", "microgram",
        "iu", "international unit",
        "tablet", "tablets", "capsule", "capsules",
        "inhaler", "cream", "pill", "pills"
    )  // units help but we also catch "prescribe you X" without them

    fun isMedicationContext(sentence: String): Boolean {
        val lower = sentence.lowercase()
        return MEDICATION_CONTEXT_TRIGGERS.any { lower.contains(it) }
    }

    private val PHRASE_NORMALISATION: Map<String, String> = mapOf(
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
        "tree times a day" to "three times a day",
        "three times aday" to "three times a day",
        "tree times a day" to "three times a day",

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
        "when kneaded" to "when needed",

        // word form -> digit so duration regex works
        "for a weak" to "for a week",
        "for 1 week" to "for a week",
        "for two weeks" to "for 2 weeks",
        "for three weeks" to "for 3 weeks",
        "for four weeks" to "for 4 weeks",
        "for five weeks" to "for 5 weeks",
        "for six weeks" to "for 6 weeks",
        "for seven days" to "for 7 days",
        "for five days" to "for 5 days",
        "for six days" to "for 6 days",
        "for ten days" to "for 10 days",
        "in one week" to "in 1 week",
        "in two weeks" to "in 2 weeks",
        "in four weeks" to "in 4 weeks",
        "for three months" to "for 3 months",
        "for six months" to "for 6 months",

        // Until phrases
        "until its gone" to "until gone",
        "until it's gone" to "until gone",
        "until the course is completed" to "until the course is complete",
        "until symptoms get better" to "until symptoms improve",

        // A&E
        "go to any" to "go to A&E",
        "attend any" to "attend A&E",

        // X-ray


        // Referral specialties
        "gynocology" to "gynaecology",
        "gynacology" to "gynaecology",
        "vasectomoy" to "vasectomy",
        "vasectimy" to "vasectomy",
        "vasecty" to "vasectomy",
        "utrasound" to "ultrasound",

        // Long term
        "longturn" to "long term",
        "long-term" to "long term",
        "on going" to "ongoing",
        "in definitely" to "indefinitely",
        "indefinately" to "indefinitely",
        "perminently" to "permanently"
    )

    fun normalizePhrases(text: String): String {
        var normalized = text.lowercase()
        PHRASE_NORMALISATION.forEach { (variant, canonical) ->
            val pattern = Regex("\\b${Regex.escape(variant)}\\b")
            normalized = normalized.replace(pattern, canonical)
        }
        return normalized
    }


    private val MEDICATION_ALIASES: Map<String, String> = mapOf(
        // Amoxicillin
        "amoxosilin" to "amoxicillin",
        "amoxisilin" to "amoxicillin",
        "amoxcillin" to "amoxicillin",
        "a moxosilin" to "amoxicillin",
        "a moxicillin" to "amoxicillin",
        "a moxisillin" to "amoxicillin",
        "a moxasillin" to "amoxicillin",
        "a moxacillin" to "amoxicillin",
        "a moxisilin" to "amoxicillin",
        "a maxasillin" to "amoxicillin",
        "a maxisillin" to "amoxicillin",
        "a moxocillin" to "amoxicillin",
        "a marxicillin" to "amoxicillin",
        "a marxacillin" to "amoxicillin",
        "amoxacillin" to "amoxicillin",
        "amoxocillin" to "amoxicillin",
        "amoxisillin" to "amoxicillin",
        "moxosilin" to "amoxicillin",
        "moxocillin" to "amoxicillin",
        "moxicillin" to "amoxicillin",

        // Pain relief
        "para cetamol" to "paracetamol",
        "paracetemol" to "paracetamol",
        "paracetimol" to "paracetamol",
        "piracy's small" to "paracetamol",
        "parasetamol" to "paracetamol",
        "parcetamol" to "paracetamol",
        "parsacetamol" to "paracetamol",
        "para setamol" to "paracetamol",
        "co codamol" to "co-codamol",
        "cocodamol" to "co-codamol",
        "mefenamicacid" to "mefenamic acid",
        "metphonomic acid" to "mefenamic acid",
        "metphanomic acid" to "mefenamic acid",
        "mefenamicacid" to "mefenamic acid",
        "co dydramol" to "co-dydramol",
        "codydramol" to "co-dydramol",
        "sol padol" to "solpadol",
        "solpadole" to "solpadol",
        "ibruprofen" to "ibuprofen",
        "ibrofen" to "ibuprofen",
        "ib prophi[n" to "ibuprofen",
        "ibuprofene" to "ibuprofen",

        // Antibiotics
        "co amoxiclav" to "co-amoxiclav",
        "coamoxiclav" to "co-amoxiclav",
        "doxycyclin" to "doxycycline",
        "clarithromicin" to "clarithromycin",
        "azithromicin" to "azithromycin",
        "metronidazol" to "metronidazole",
        "penicilin" to "penicillin",
        "augmentine" to "augmentin",
        "flucloxacilin" to "flucloxacillin",

        // Stomach/acid/nausea
        "omeprazol" to "omeprazole",
        "ameprazol" to "omeprazole",
        "ameprazole" to "omeprazole",
        "lansoprazol" to "lansoprazole",
        "esomeprazol" to "esomeprazole",
        "pantoprazol" to "pantoprazole",
        "domperidon" to "domperidone",
        "cyclizin" to "cyclizine",
        "prochlorperazin" to "prochlorperazine",
        "omeperazole" to "omeprazole",
        "omperazole" to "omeprazole",
        "ventoline" to "ventolin",
        "ventolen" to "ventolin",

        // Diabetes
        "metformine" to "metformin",
        "gliclazid" to "gliclazide",

        // Blood pressure/heart
        "amlodipin" to "amlodipine",
        "a melodopine" to "amlodipine",
        "furosemid" to "furosemide",
        "bendroflumethiazid" to "bendroflumethiazide",
        "lisinopryl" to "lisinopril",
        "lysinopril" to "lisinopril",
        "ramapril" to "ramipril",
        "ramepril" to "ramipril",
        "ramapro" to "ramipril",
        "atorvastatine" to "atorvastatin",
        "simvastatine" to "simvastatin",

        // Mental health
        "sertralin" to "sertraline",
        "search routine" to "sertraline",
        "fluoxetin" to "fluoxetine",
        "venlafaxin" to "venlafaxine",
        "mirtazapin" to "mirtazapine",
        "duloxetin" to "duloxetine",
        "amitriptylin" to "amitriptyline",
        "escitralopram" to "escitalopram",
        "citalapram" to "citalopram",

        // Respiratory
        "beclometason" to "beclometasone",
        "prednisolon" to "prednisolone",
        "prednison" to "prednisone",
        "salbutamole" to "salbutamol",
        "seritide" to "seretide",

        // Thyroid
        "levothyroxin" to "levothyroxine",
        "thyroxin" to "thyroxine",
        "levothyroxen" to "levothyroxine",

        // Nerve pain/epilepsy
        "carbamazepin" to "carbamazepine",
        "gabapentine" to "gabapentin",
        "pregabaline" to "pregabalin",

        // Sedatives/anxiety
        "zopiclon" to "zopiclone",

        // Allergy/antihistamine
        "cetirizin" to "cetirizine",
        "loratadin" to "loratadine",
        "fexofenadin" to "fexofenadine",
        "chlorphenamin" to "chlorphenamine",

        // Skin
        "hydrocortison" to "hydrocortisone",
        "hydrocortizone" to "hydrocortisone",
        "fusidicacid" to "fusidic acid",
        "canestencream" to "canesten cream",

        // Eye/ear
        "locortenvioform" to "locorten vioform",
        "hypromellos" to "hypromellose",
        "hylotear" to "hylo-tear",
        "hylo tear" to "hylo-tear",

        // Gout
        "colchicin" to "colchicine",

        // Men's health/prostate
        "finasterid" to "finasteride",
        "dutasterid" to "dutasteride",

        // Women's health
        "micro gynon" to "microgynon",
        "microgynen" to "microgynon",
        "coppercoil" to "copper coil",
        "norethisteron" to "norethisterone",
        "neurothistorone" to "norethisterone",
        "norhists to roam" to "norethisterone",
        "proveria" to "provera",
        "tranexamicacid" to "tranexamic acid",
        "clomiphen" to "clomiphene",
        "fluconazol" to "fluconazole",
        "fluconasol" to "fluconazole",
        "flu conazol" to "fluconazole",
        "flu canasol" to "fluconazole",
        "flu condosol" to "fluconazole",
        "Yasmin" to "Yazmin",
        "naproxin" to "Naproxen",
        "Pervira" to "Provera",
        "Provira" to "Provera",
        "Provirah" to "Provera",

        // Supplements
        "folicacid" to "folic acid",
        "vitamind" to "vitamin D",
        "ferrousfumarate" to "ferrous fumarate",
        "ferroussulfate" to "ferrous sulfate"
    )

    /** parasetamol -> paracetamol etc. Same string used for matching + regex. */
    fun applyMedicationAliases(text: String): String {
        var result = text.lowercase()
        // longer matches first
        MEDICATION_ALIASES.entries
            .sortedByDescending { it.key.length }
            .forEach { (variant, canonical) ->
                val pattern = Regex("\\b${Regex.escape(variant)}\\b")
                result = result.replace(pattern, canonical)
            }
        return result
    }

    fun findMedicationAliasIn(text: String): String? {
        val lower = text.lowercase()
        for ((variant, canonical) in MEDICATION_ALIASES) {
            if (lower.contains(variant)) return canonical
        }
        val normalized = lower.replace(Regex("\\b(a|an|the)\\s+"), "").replace(" ", "")
        for ((variant, canonical) in MEDICATION_ALIASES) {
            val normVariant = variant.replace(" ", "").lowercase()
            if (normalized.contains(normVariant)) return canonical
        }
        return null
    }
}
