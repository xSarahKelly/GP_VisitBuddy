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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicalappointmentcompanion.model.Appointment
import kotlinx.coroutines.delay

@Composable
fun SummaryScreen(
    appointment: Appointment,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onReview: () -> Unit,
    onSave: (Appointment) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var transcriptSaved by remember { mutableStateOf(false) }
    var showSavedMessage by remember { mutableStateOf(false) }

    LaunchedEffect(showSavedMessage) {
        if (showSavedMessage) {
            delay(3000)
            showSavedMessage = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.PrimaryBlue
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = AppColors.AccentRed
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✅", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Your Appointment Summary",
                        fontSize = 26.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = AppColors.PrimaryBlue
                    )
                }

                if (appointment.title != "New Appointment") {
                    Text(
                        text = "(${appointment.title})",
                        fontSize = 18.sp,
                        color = AppColors.TextSecondary,
                        modifier = Modifier.padding(start = 42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                appointment.extraction?.let { extraction ->
                    if (extraction.medicationInstructions.isNotEmpty()) {
                        SummarySection(emoji = "💊", title = "Medication") {
                            extraction.medicationInstructions.forEach { med ->
                                BulletPoint(
                                    text = buildString {
                                        append(med.medicineName)
                                        med.dosage?.let { append(" $it") }
                                        med.frequency?.let { append(" $it") }
                                        med.duration?.let { append(", $it") }
                                        med.specialInstructions?.let { append(" ($it)") }
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (extraction.safetyAdvice.isNotEmpty()) {
                        SummarySection(emoji = "⚠️", title = "Safety Warnings") {
                            extraction.safetyAdvice.forEach { warning ->
                                BulletPoint(text = warning.warning, isWarning = true)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (extraction.testsAndReferrals.isNotEmpty()) {
                        SummarySection(emoji = "🔬", title = "Tests & Referrals") {
                            extraction.testsAndReferrals.forEach { test ->
                                BulletPoint(
                                    text = buildString {
                                        append(test.testOrReferralType)
                                        test.destinationIfStated?.let { append(" to $it") }
                                        test.reasonIfStated?.let { append(" – $it") }
                                        test.urgency?.let { append(" ($it)") }
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    extraction.followUp?.let { followUp ->
                        SummarySection(emoji = "📅", title = "Follow-Up") {
                            BulletPoint(
                                text = followUp.verbatimQuote?.takeIf { it.isNotBlank() }
                                    ?: buildString {
                                        append("Return")
                                        followUp.timeframe?.let { append(" $it") }
                                        followUp.locationOrMethod?.let { append(" ($it)") }
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (extraction.additionalNotes.isNotEmpty()) {
                        SummarySection(emoji = "📝", title = "Additional Notes") {
                            extraction.additionalNotes.forEach { note ->
                                BulletPoint(text = note)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                appointment.transcription?.let { transcription ->
                    if (transcription.fullText.isNotEmpty()) {
                        SummarySection(emoji = "📄", title = "Full Transcription") {
                            Text(
                                text = transcription.fullText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!appointment.isLocked) {
                    Button(
                        onClick = onReview,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryBlue),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("✏️  GP Review & Edit", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (!transcriptSaved) {
                    Button(
                        onClick = {
                            onSave(appointment)
                            transcriptSaved = true
                            showSavedMessage = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("💾  Save", fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    }
                }
            }
        }

        if (showSavedMessage) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = AppColors.AccentGreen
            ) {
                Text("Transcript saved!", color = Color.White)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recording?", color = AppColors.TextPrimary) },
            text = {
                Text(
                    "This will permanently delete this recording and its summary.",
                    color = AppColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = AppColors.AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = AppColors.TextSecondary)
                }
            },
            containerColor = AppColors.SurfaceWhite
        )
    }
}

@Composable
private fun SummarySection(
    emoji: String,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 26.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = AppColors.TextPrimary
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(modifier = Modifier.padding(start = 36.dp)) {
            content()
        }
    }
}

@Composable
private fun BulletPoint(
    text: String,
    isWarning: Boolean = false
) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            fontSize = 20.sp,
            color = if (isWarning) AppColors.AccentAmber else AppColors.TextPrimary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 20.sp,
            color = if (isWarning) AppColors.AccentAmber else AppColors.TextPrimary,
            lineHeight = 28.sp
        )
    }
}
