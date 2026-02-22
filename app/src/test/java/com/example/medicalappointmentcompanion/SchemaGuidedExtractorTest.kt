package com.example.medicalappointmentcompanion

import com.example.medicalappointmentcompanion.extraction.SchemaGuidedExtractor
import com.example.medicalappointmentcompanion.model.*
import org.junit.Assert.*
import org.junit.Test

/** tests for extraction – meds, tests, follow-up, safety */
class SchemaGuidedExtractorTest {

    @Test
    fun extracts_paracetamol_instruction() {
        val transcript = "Take paracetamol 500mg two tablets every 6 hours with food for 7 days."
        val result = SchemaGuidedExtractor.extract(transcript)

        assertEquals(1, result.medicationInstructions.size)
        val med = result.medicationInstructions[0]
        assertEquals("Paracetamol", med.medicineName)
        assertNotNull(med.dosage)
        assertTrue(med.dosage!!.contains("500") || med.dosage!!.contains("two"))
        assertNotNull(med.frequency)
        assertNotNull(med.duration)
        assertNotNull(med.specialInstructions)
        assertTrue(med.specialInstructions!!.lowercase().contains("food"))
    }

    @Test
    fun extracts_amoxicillin_antibiotic() {
        val transcript = "I'm prescribing amoxicillin 500mg three times a day for 7 days with food."
        val result = SchemaGuidedExtractor.extract(transcript)

        assertEquals(1, result.medicationInstructions.size)
        val med = result.medicationInstructions[0]
        assertEquals("Amoxicillin", med.medicineName)
        assertNotNull(med.dosage)
        assertNotNull(med.frequency)
        assertTrue(med.frequency!!.lowercase().contains("three") || med.frequency!!.contains("3"))
        assertNotNull(med.duration)
        assertNotNull(med.specialInstructions)
    }

    @Test
    fun extracts_omeprazole_stomach() {
        val transcript = "Take omeprazole 20mg once a day in the morning before breakfast. Continue long term."
        val result = SchemaGuidedExtractor.extract(transcript)

        assertEquals(1, result.medicationInstructions.size)
        val med = result.medicationInstructions[0]
        assertEquals("Omeprazole", med.medicineName)
        assertNotNull(med.dosage)
        assertNotNull(med.frequency)
        assertNotNull(med.specialInstructions)
    }

    @Test
    fun extracts_ibuprofen_prn() {
        val transcript = "You can take ibuprofen 400mg up to three times a day as needed for pain."
        val result = SchemaGuidedExtractor.extract(transcript)

        assertEquals(1, result.medicationInstructions.size)
        val med = result.medicationInstructions[0]
        assertEquals("Ibuprofen", med.medicineName)
        assertNotNull(med.dosage)
        assertNotNull(med.frequency)
    }

    @Test
    fun extracts_blood_test_and_xray() {
        val transcript = "I'm referring you for a blood test to check your levels. We'll also need an X-ray of your chest."
        val result = SchemaGuidedExtractor.extract(transcript)

        assertTrue("Should extract blood test", result.testsAndReferrals.any { it.testOrReferralType.lowercase().contains("blood") })
        assertTrue("Should extract X-ray", result.testsAndReferrals.any { it.testOrReferralType.lowercase().contains("x-ray") || it.testOrReferralType.lowercase().contains("xray") })
    }

    @Test
    fun extracts_follow_up_with_timeframe() {
        val transcript = "Come back in 2 weeks for a review. Book at reception."
        val result = SchemaGuidedExtractor.extract(transcript)

        assertNotNull(result.followUp)
        assertTrue(result.followUp!!.followUpRequired)
        assertNotNull(result.followUp!!.timeframe)
        assertTrue(result.followUp!!.timeframe!!.contains("2") && result.followUp!!.timeframe!!.contains("week"))
    }

    @Test
    fun extracts_safety_warning() {
        val transcript = "Take the amoxicillin as directed. Stop taking if you get a rash or any allergic reaction."
        val result = SchemaGuidedExtractor.extract(transcript)

        assertEquals(1, result.medicationInstructions.size)
        assertTrue(result.safetyAdvice.isNotEmpty())
        assertTrue(result.safetyAdvice.any { it.warning.lowercase().contains("rash") || it.warning.lowercase().contains("allergic") })
    }

    @Test
    fun full_consultation_all_categories() {
        val transcript = """
            I'm going to prescribe you paracetamol 500mg, two tablets every 6 hours with food for 7 days.
            I'm also referring you for a blood test. Come back in 2 weeks for a review.
            If you get a high fever or breathing problems, go to A&E.
        """.trimIndent()

        val result = SchemaGuidedExtractor.extract(transcript)

        assertTrue("Medication", result.medicationInstructions.isNotEmpty())
        assertTrue("Tests", result.testsAndReferrals.any { it.testOrReferralType.lowercase().contains("blood") })
        assertNotNull("Follow-up", result.followUp)
        assertTrue("Safety", result.safetyAdvice.isNotEmpty())
    }

    @Test
    fun parasetamol_alias_normalised() {
        val transcript = "Take parasetamol 500mg twice a day."
        val result = SchemaGuidedExtractor.extract(transcript)

        assertEquals(1, result.medicationInstructions.size)
        assertEquals("Paracetamol", result.medicationInstructions[0].medicineName)
    }

    @Test
    fun metformin_diabetes_category() {
        val transcript = "Continue taking metformin 500mg twice daily with your meals."
        val result = SchemaGuidedExtractor.extract(transcript)

        assertEquals(1, result.medicationInstructions.size)
        val med = result.medicationInstructions[0]
        assertEquals("Metformin", med.medicineName)
        assertNotNull(med.frequency)
    }

    @Test
    fun salbutamol_respiratory_category() {
        val transcript = "Use your salbutamol inhaler two puffs when needed for breathlessness."
        val result = SchemaGuidedExtractor.extract(transcript)

        assertEquals(1, result.medicationInstructions.size)
        assertEquals("Salbutamol", result.medicationInstructions[0].medicineName)
    }
}
