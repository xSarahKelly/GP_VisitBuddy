package com.example.medicalappointmentcompanion.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.medicalappointmentcompanion.model.Appointment
import com.example.medicalappointmentcompanion.model.MedicationInstruction
import com.example.medicalappointmentcompanion.model.MedicationReminder
import com.example.medicalappointmentcompanion.reminder.ReminderNotificationHelper
import java.util.Calendar

@Composable
fun SummaryScreen(
    appointment: Appointment,
    accountId: String,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onReview: () -> Unit,
    onSave: (Appointment) -> Unit,
    onAddToCalendar: () -> Unit = {},
    canSwitchAccount: Boolean = false,
    onOpenAccount: () -> Unit = {},
    onScheduleReminder: (accountId: String, appointmentId: String, medicationName: String, dosage: String?, hour: Int, minute: Int) -> Unit = { _, _, _, _, _, _ -> },
    existingReminders: List<MedicationReminder> = emptyList(),
    onAddToCurrentMedications: (String) -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var transcriptSaved by remember { mutableStateOf(false) }
    var medicationForReminder by remember { mutableStateOf<MedicationInstruction?>(null) }
    var calendarLaunchError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val hasNotificationPermission = remember {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
    }
    var notificationPermissionGranted by remember(hasNotificationPermission) { mutableStateOf(hasNotificationPermission) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationPermissionGranted = granted
        if (!granted) medicationForReminder = null
    }

    LaunchedEffect(medicationForReminder) {
        val med = medicationForReminder ?: return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionGranted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
                if (canSwitchAccount) {
                    TextButton(onClick = onOpenAccount) {
                        Text("Switch account", fontSize = 16.sp, color = AppColors.PrimaryBlue)
                    }
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
                                val medText = buildString {
                                    append(med.medicineName)
                                    med.dosage?.let { append(" $it") }
                                    med.frequency?.let { append(" $it") }
                                    med.duration?.let { append(", $it") }
                                    med.specialInstructions?.let { append(" ($it)") }
                                }
                                val hasReminder = existingReminders.any { it.medicationName.equals(med.medicineName, ignoreCase = true) }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Text("•", fontSize = 20.sp, color = AppColors.TextPrimary)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = medText,
                                            fontSize = 20.sp,
                                            color = AppColors.TextPrimary,
                                            lineHeight = 28.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.padding(start = 30.dp, top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TextButton(
                                            onClick = { onAddToCurrentMedications(medText) }
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Add to current medications", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Add to current meds", fontSize = 13.sp, color = AppColors.PrimaryBlue)
                                        }
                                        if (hasReminder) {
                                            Text("Reminder set", fontSize = 13.sp, color = AppColors.TextSecondary, modifier = Modifier.align(Alignment.CenterVertically))
                                        } else {
                                            TextButton(
                                                onClick = { medicationForReminder = med }
                                            ) {
                                                Icon(Icons.Default.Notifications, contentDescription = "Set reminder", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Set reminder", fontSize = 13.sp, color = AppColors.PrimaryBlue)
                                            }
                                        }
                                    }
                                }
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
                            val followUpText = followUp.verbatimQuote?.takeIf { it.isNotBlank() }
                                ?: buildString {
                                    append("Return")
                                    followUp.timeframe?.let { append(" $it") }
                                    followUp.locationOrMethod?.let { append(" ($it)") }
                                }
                            BulletPoint(
                                text = followUpText
                            )
                            TextButton(
                                onClick = {
                                    val beginAt = Calendar.getInstance().apply {
                                        add(Calendar.DAY_OF_YEAR, 7)
                                    }.timeInMillis
                                    val intent = Intent(Intent.ACTION_INSERT).apply {
                                        data = CalendarContract.Events.CONTENT_URI
                                        putExtra(CalendarContract.Events.TITLE, "GP Follow-up")
                                        putExtra(
                                            CalendarContract.Events.DESCRIPTION,
                                            buildString {
                                                append("From GP VisitBuddy summary")
                                                append("\n\n")
                                                append(followUpText)
                                                appointment.extraction?.additionalNotes
                                                    ?.takeIf { it.isNotEmpty() }
                                                    ?.let {
                                                        append("\n\nNotes:\n")
                                                        append(it.joinToString("\n- ", prefix = "- "))
                                                    }
                                            }
                                        )
                                        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginAt)
                                        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, beginAt + (30 * 60 * 1000))
                                    }
                                    try {
                                        context.startActivity(intent)
                                        onAddToCalendar()
                                    } catch (_: ActivityNotFoundException) {
                                        calendarLaunchError = true
                                    }
                                }
                            ) {
                                Text("Add follow-up to calendar", fontSize = 13.sp, color = AppColors.PrimaryBlue)
                            }
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
                                fontSize = 18.sp,
                                lineHeight = 28.sp,
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

    val canShowReminderPicker = medicationForReminder != null &&
        (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || notificationPermissionGranted)
    if (canShowReminderPicker) {
        val med = medicationForReminder!!
        val cal = Calendar.getInstance()
        var reminderHour by remember { mutableStateOf(cal.get(Calendar.HOUR_OF_DAY)) }
        var reminderMinute by remember { mutableStateOf((cal.get(Calendar.MINUTE) / 5) * 5) }
        AlertDialog(
            onDismissRequest = { medicationForReminder = null },
            title = { Text("Set reminder for ${med.medicineName}", color = AppColors.TextPrimary) },
            text = {
                Column {
                    Text("Choose a time for your medication reminder", color = AppColors.TextSecondary, modifier = Modifier.padding(bottom = 16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hour", fontSize = 14.sp, color = AppColors.TextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { reminderHour = (reminderHour - 1 + 24) % 24 }) { Text("−") }
                                Text("${reminderHour.toString().padStart(2, '0')}", fontSize = 24.sp, modifier = Modifier.padding(horizontal = 16.dp))
                                TextButton(onClick = { reminderHour = (reminderHour + 1) % 24 }) { Text("+") }
                            }
                        }
                        Text(":", fontSize = 24.sp)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Minute", fontSize = 14.sp, color = AppColors.TextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { reminderMinute = (reminderMinute - 5 + 60) % 60 }) { Text("−") }
                                Text("${reminderMinute.toString().padStart(2, '0')}", fontSize = 24.sp, modifier = Modifier.padding(horizontal = 16.dp))
                                TextButton(onClick = { reminderMinute = (reminderMinute + 5) % 60 }) { Text("+") }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        ReminderNotificationHelper.showReminderNotification(
                            context = context,
                            reminderId = "${appointment.id}:${med.medicineName}",
                            medicationName = med.medicineName,
                            dosage = med.dosage
                        )
                    }
                ) {
                    Text("Test alert now", color = AppColors.PrimaryBlue)
                }
                TextButton(
                    onClick = {
                        onScheduleReminder(
                            accountId,
                            appointment.id,
                            med.medicineName,
                            med.dosage,
                            reminderHour,
                            reminderMinute
                        )
                        medicationForReminder = null
                    }
                ) {
                    Text("Set reminder", color = AppColors.PrimaryBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { medicationForReminder = null }) {
                    Text("Cancel", color = AppColors.TextSecondary)
                }
            },
            containerColor = AppColors.SurfaceWhite
        )
    }
    if (calendarLaunchError) {
        AlertDialog(
            onDismissRequest = { calendarLaunchError = false },
            title = { Text("Calendar app not found", color = AppColors.TextPrimary) },
            text = {
                Text(
                    "Could not open a calendar app on this device.",
                    color = AppColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { calendarLaunchError = false }) {
                    Text("OK", color = AppColors.PrimaryBlue)
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
