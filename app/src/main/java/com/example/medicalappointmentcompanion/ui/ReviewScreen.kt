package com.example.medicalappointmentcompanion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicalappointmentcompanion.model.*

@Composable
fun ReviewScreen(
    appointment: Appointment,
    onSave: (Appointment) -> Unit,
    onBack: () -> Unit,
    onAddToCalendar: () -> Unit = {}
) {
    var transcriptText by remember(appointment.id) {
        mutableStateOf(appointment.transcription?.fullText ?: "")
    }
    var medLines by remember(appointment.id) {
        mutableStateOf(
            appointment.extraction?.medicationInstructions?.map { med ->
                buildString {
                    append(med.medicineName)
                    med.dosage?.let { append(" $it") }
                    med.frequency?.let { append(" $it") }
                    med.duration?.let { append(", $it") }
                    med.specialInstructions?.let { append(" ($it)") }
                }
            } ?: mutableListOf("")
        )
    }
    var safetyLines by remember(appointment.id) {
        mutableStateOf(
            appointment.extraction?.safetyAdvice?.map { it.warning }?.toMutableList()
                ?: mutableListOf("")
        )
    }
    var testLines by remember(appointment.id) {
        mutableStateOf(
            appointment.extraction?.testsAndReferrals?.map { t ->
                buildString {
                    append(t.testOrReferralType)
                    t.destinationIfStated?.let { append(" to $it") }
                    t.reasonIfStated?.let { append(" – $it") }
                    t.urgency?.let { append(" ($it)") }
                }
            }?.toMutableList() ?: mutableListOf("")
        )
    }
    var followUpText by remember(appointment.id) {
        mutableStateOf(
            appointment.extraction?.followUp?.verbatimQuote
                ?: appointment.extraction?.followUp?.let { f ->
                    buildString {
                        f.timeframe?.let { append(it) }
                        f.locationOrMethod?.let { append(" ($it)") }
                    }.takeIf { it.isNotBlank() } ?: ""
                } ?: ""
        )
    }
    var notesLines by remember(appointment.id) {
        mutableStateOf(
            appointment.extraction?.additionalNotes?.toMutableList()
                ?: mutableListOf("")
        )
    }

    fun buildExtraction(): MedicalExtraction {
        val meds = medLines.filter { it.isNotBlank() }.map { line ->
            MedicationInstruction(medicineName = line.trim())
        }
        val safety = safetyLines.filter { it.isNotBlank() }.map { line ->
            SafetyWarning(warning = line.trim())
        }
        val tests = testLines.filter { it.isNotBlank() }.map { line ->
            TestOrReferral(testOrReferralType = line.trim())
        }
        val followUp = followUpText.takeIf { it.isNotBlank() }?.let {
            FollowUpInstruction(verbatimQuote = it.trim())
        }
        val notes = notesLines.filter { it.isNotBlank() }
        return (appointment.extraction ?: MedicalExtraction()).copy(
            medicationInstructions = meds,
            safetyAdvice = safety,
            testsAndReferrals = tests,
            followUp = followUp,
            additionalNotes = notes
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.PrimaryBlue)
            }
            Text("Review & Edit", fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = AppColors.PrimaryBlue)
            Spacer(modifier = Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            EditableSection("Full transcript") {
                OutlinedTextField(
                    value = transcriptText,
                    onValueChange = { transcriptText = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    textStyle = TextStyle(fontSize = 18.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.PrimaryBlue,
                        unfocusedBorderColor = AppColors.CardBorder,
                        focusedTextColor = AppColors.TextPrimary,
                        unfocusedTextColor = AppColors.TextPrimary
                    )
                )
            }

            EditableSection("Medication") {
                medLines.forEachIndexed { i, line ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = line,
                            onValueChange = { medLines = medLines.toMutableList().apply { set(i, it) } },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 18.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.PrimaryBlue,
                                unfocusedBorderColor = AppColors.CardBorder,
                                focusedTextColor = AppColors.TextPrimary,
                                unfocusedTextColor = AppColors.TextPrimary
                            )
                        )
                        IconButton(onClick = { medLines = medLines.toMutableList().apply { removeAt(i) }.ifEmpty { mutableListOf("") } }) {
                            Icon(Icons.Default.Delete, "Remove", tint = AppColors.AccentRed, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                TextButton(onClick = { medLines = medLines.toMutableList().apply { add("") } }) {
                    Text("+ Add medication", color = AppColors.PrimaryBlue)
                }
            }

            EditableSection("Red flags / Safety") {
                safetyLines.forEachIndexed { i, line ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = line,
                            onValueChange = { safetyLines = safetyLines.toMutableList().apply { set(i, it) } },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 18.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.PrimaryBlue,
                                unfocusedBorderColor = AppColors.CardBorder,
                                focusedTextColor = AppColors.TextPrimary,
                                unfocusedTextColor = AppColors.TextPrimary
                            )
                        )
                        IconButton(onClick = { safetyLines = safetyLines.toMutableList().apply { removeAt(i) }.ifEmpty { mutableListOf("") } }) {
                            Icon(Icons.Default.Delete, "Remove", tint = AppColors.AccentRed, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                TextButton(onClick = { safetyLines = safetyLines.toMutableList().apply { add("") } }) {
                    Text("+ Add safety note", color = AppColors.PrimaryBlue)
                }
            }

            EditableSection("Tests & Referrals") {
                testLines.forEachIndexed { i, line ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = line,
                            onValueChange = { testLines = testLines.toMutableList().apply { set(i, it) } },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 18.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.PrimaryBlue,
                                unfocusedBorderColor = AppColors.CardBorder,
                                focusedTextColor = AppColors.TextPrimary,
                                unfocusedTextColor = AppColors.TextPrimary
                            )
                        )
                        IconButton(onClick = { testLines = testLines.toMutableList().apply { removeAt(i) }.ifEmpty { mutableListOf("") } }) {
                            Icon(Icons.Default.Delete, "Remove", tint = AppColors.AccentRed, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                TextButton(onClick = { testLines = testLines.toMutableList().apply { add("") } }) {
                    Text("+ Add test/referral", color = AppColors.PrimaryBlue)
                }
            }

            EditableSection("Follow-up") {
                OutlinedTextField(
                    value = followUpText,
                    onValueChange = { followUpText = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 18.sp),
                    placeholder = { Text("e.g. Come back in 2 weeks", color = AppColors.TextHint) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.PrimaryBlue,
                        unfocusedBorderColor = AppColors.CardBorder,
                        focusedTextColor = AppColors.TextPrimary,
                        unfocusedTextColor = AppColors.TextPrimary
                    )
                )
            }

            EditableSection("Additional notes") {
                notesLines.forEachIndexed { i, line ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = line,
                            onValueChange = { notesLines = notesLines.toMutableList().apply { set(i, it) } },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 18.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.PrimaryBlue,
                                unfocusedBorderColor = AppColors.CardBorder,
                                focusedTextColor = AppColors.TextPrimary,
                                unfocusedTextColor = AppColors.TextPrimary
                            )
                        )
                        IconButton(onClick = { notesLines = notesLines.toMutableList().apply { removeAt(i) }.ifEmpty { mutableListOf("") } }) {
                            Icon(Icons.Default.Delete, "Remove", tint = AppColors.AccentRed, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                TextButton(onClick = { notesLines = notesLines.toMutableList().apply { add("") } }) {
                    Text("+ Add note", color = AppColors.PrimaryBlue)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val updated = appointment.copy(
                        transcription = appointment.transcription?.copy(fullText = transcriptText) ?: Transcription(transcriptText),
                        extraction = buildExtraction(),
                        isLocked = true
                    )
                    onSave(updated)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save & Lock", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun EditableSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Text(title, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, color = AppColors.TextPrimary)
    Spacer(modifier = Modifier.height(8.dp))
    Column { content() }
    Spacer(modifier = Modifier.height(20.dp))
}
