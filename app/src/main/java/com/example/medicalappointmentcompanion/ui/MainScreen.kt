package com.example.medicalappointmentcompanion.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.medicalappointmentcompanion.model.Appointment
import com.example.medicalappointmentcompanion.model.AppointmentStatus
import com.example.medicalappointmentcompanion.model.AppState
import java.text.SimpleDateFormat
import java.util.*

// ============================================================================
// ACCESSIBILITY-FOCUSED COLOR SCHEME (Light Theme - WCAG 2.2)
// ============================================================================

// Primary blue - good contrast on white
private val PrimaryBlue = Color(0xFF1976D2)
private val PrimaryBlueDark = Color(0xFF1565C0)
private val PrimaryBlueLight = Color(0xFFE3F2FD)

// Accent colors for actions
private val AccentRed = Color(0xFFE53935)       // Stop/Delete actions
private val AccentGreen = Color(0xFF43A047)     // Success/Save
private val AccentAmber = Color(0xFFFFA000)     // Warnings

// Backgrounds - light and accessible
private val BackgroundWhite = Color(0xFFFAFAFA)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val CardBorder = Color(0xFFE0E0E0)

// Text colors - high contrast
private val TextPrimary = Color(0xFF212121)
private val TextSecondary = Color(0xFF616161)
private val TextHint = Color(0xFF9E9E9E)

// ============================================================================
// MAIN SCREEN - Navigation Hub
// ============================================================================

@Composable
fun MainScreen(
    state: AppState,
    onRetryModelLoad: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onSelectAppointment: (String) -> Unit,
    onDeleteAppointment: (String) -> Unit,
    onClearAppointment: () -> Unit,
    onClearError: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }
    
    var showPastSummaries by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    
    // Show model dialog if not loaded
    LaunchedEffect(state.isModelLoaded, state.isModelLoading) {
        if (!state.isModelLoaded && !state.isModelLoading && state.modelError != null) {
            showModelDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        when {
            // Recording in progress
            state.isRecording -> {
                RecordingScreen(
                    recordingDuration = state.recordingDuration,
                    isTranscribing = state.isTranscribing,
                    onStop = onStopRecording,
                    onCancel = onCancelRecording
                )
            }
            
            // Transcribing
            state.isTranscribing -> {
                ProcessingScreen()
            }
            
            // Viewing appointment detail
            state.currentAppointment != null -> {
                SummaryScreen(
                    appointment = state.currentAppointment,
                    onBack = onClearAppointment,
                    onDelete = { onDeleteAppointment(state.currentAppointment.id) }
                )
            }
            
            // Viewing past summaries list
            showPastSummaries -> {
                PastSummariesScreen(
                    appointments = state.appointments,
                    onSelect = onSelectAppointment,
                    onBack = { showPastSummaries = false }
                )
            }
            
            // Home screen (default)
            else -> {
                HomeScreen(
                    isModelLoaded = state.isModelLoaded,
                    isModelLoading = state.isModelLoading,
                    hasPermission = hasPermission,
                    onStartRecording = onStartRecording,
                    onViewPastSummaries = { showPastSummaries = true },
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    onOpenSettings = { showSettingsDialog = true },
                    onLoadModel = { showModelDialog = true }
                )
            }
        }
        
        // Error snackbar
        state.errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = onClearError) {
                        Text("Dismiss", color = PrimaryBlue)
                    }
                },
                containerColor = SurfaceWhite
            ) {
                Text(error, color = TextPrimary)
            }
        }
        
        state.modelError?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = onClearError) {
                        Text("Dismiss", color = PrimaryBlue)
                    }
                },
                containerColor = Color(0xFFFFEBEE)
            ) {
                Text(error, color = AccentRed)
            }
        }
    }
    
    // Settings dialog
    if (showSettingsDialog) {
        SettingsDialog(onDismiss = { showSettingsDialog = false })
    }
    
    // Model setup dialog
    if (showModelDialog) {
        ModelSetupDialog(
            onDismiss = { showModelDialog = false },
            onRetry = {
                showModelDialog = false
                onRetryModelLoad()
            }
        )
    }
}

// ============================================================================
// HOME SCREEN
// ============================================================================

@Composable
private fun HomeScreen(
    isModelLoaded: Boolean,
    isModelLoading: Boolean,
    hasPermission: Boolean,
    onStartRecording: () -> Unit,
    onViewPastSummaries: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onLoadModel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Title - LARGE for accessibility
        Text(
            text = "Medical Appointment Companion",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Subtitle - readable size
        Text(
            text = "Helping Patients Remember What Matters",
            fontSize = 20.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Start Recording Button (Primary action) - LARGE tap target
        val canRecord = isModelLoaded && hasPermission
        
        Button(
            onClick = {
                when {
                    !isModelLoaded -> onLoadModel()
                    !hasPermission -> onRequestPermission()
                    else -> onStartRecording()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = !isModelLoading
        ) {
            if (isModelLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = "🎤  Start Recording",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        // Status message under button
        if (!isModelLoaded && !isModelLoading) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "⚠️ Tap to set up speech model first",
                fontSize = 16.sp,
                color = AccentAmber
            )
        } else if (!hasPermission) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "⚠️ Microphone permission required",
                fontSize = 16.sp,
                color = AccentAmber
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // View Past Summaries Button (Secondary action) - LARGE tap target
        OutlinedButton(
            onClick = onViewPastSummaries,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PrimaryBlue
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(listOf(PrimaryBlueLight, PrimaryBlueLight))
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "📋  View Past Summaries",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryBlue
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Privacy notice at bottom - readable size
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "🔒",
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "All processing happens privately on your device.",
                fontSize = 18.sp,
                color = TextSecondary
            )
        }
        
        // Settings link - larger tap target
        TextButton(
            onClick = onOpenSettings,
            modifier = Modifier.height(48.dp)
        ) {
            Text(
                text = "Privacy & Settings",
                fontSize = 18.sp,
                color = PrimaryBlue
            )
        }
    }
}

// ============================================================================
// RECORDING SCREEN 
// ============================================================================

@Composable
private fun RecordingScreen(
    recordingDuration: Long,
    isTranscribing: Boolean,
    onStop: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Recording indicator with pulse animation
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎤",
                fontSize = 36.sp,
                modifier = Modifier.graphicsLayer(alpha = alpha)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Recording in Progress",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Duration display - LARGE
        Text(
            text = formatDuration(recordingDuration),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Live transcript preview area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Doctor: Take two tablets...",
                    fontSize = 20.sp,
                    color = TextSecondary.copy(alpha = 0.6f),
                    fontStyle = FontStyle.Italic
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "AI is listening and transcribing...",
                    fontSize = 18.sp,
                    color = TextHint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Stop & Generate Summary Button - LARGE
        Button(
            onClick = onStop,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentRed
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "⏹  Stop & Generate Summary",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Cancel option - larger tap target
        TextButton(
            onClick = onCancel,
            modifier = Modifier.height(48.dp)
        ) {
            Text(
                text = "Cancel Recording",
                fontSize = 18.sp,
                color = TextSecondary
            )
        }
    }
}

// ============================================================================
// PROCESSING SCREEN
// ============================================================================

@Composable
private fun ProcessingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(80.dp),
            color = PrimaryBlue,
            strokeWidth = 6.dp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Generating Your Summary...",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Processing locally on your device",
            fontSize = 20.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// ============================================================================
// SUMMARY SCREEN 
// ============================================================================

@Composable
private fun SummaryScreen(
    appointment: Appointment,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        // Header
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
                    tint = PrimaryBlue
                )
            }
            
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = AccentRed
                )
            }
        }
        
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Title with checkmark - LARGE
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "✅",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Your Appointment Summary",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
            
            // Show title if custom
            if (appointment.title != "New Appointment") {
                Text(
                    text = "(${appointment.title})",
                    fontSize = 18.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 42.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Show extraction or transcription
            appointment.extraction?.let { extraction ->
                
                // MEDICATION SECTION
                if (extraction.medicationInstructions.isNotEmpty()) {
                    SummarySection(
                        emoji = "💊",
                        title = "Medication"
                    ) {
                        extraction.medicationInstructions.forEach { med ->
                            BulletPoint(
                                text = buildString {
                                    append(med.medicineName)
                                    med.dosage?.let { append(" $it") }
                                    med.frequency?.let { append(" $it") }
                                    med.specialInstructions?.let { append(", $it") }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // SAFETY ADVICE / RED FLAGS
                if (extraction.safetyAdvice.isNotEmpty()) {
                    SummarySection(
                        emoji = "⚠️",
                        title = "Red Flags"
                    ) {
                        extraction.safetyAdvice.forEach { warning ->
                            BulletPoint(
                                text = warning.warning,
                                isWarning = true
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // TESTS AND REFERRALS
                if (extraction.testsAndReferrals.isNotEmpty()) {
                    SummarySection(
                        emoji = "🔬",
                        title = "Tests & Referrals"
                    ) {
                        extraction.testsAndReferrals.forEach { test ->
                            BulletPoint(
                                text = buildString {
                                    append(test.testOrReferralType)
                                    test.urgency?.let { append(" ($it)") }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // FOLLOW-UP
                extraction.followUp?.let { followUp ->
                    SummarySection(
                        emoji = "📅",
                        title = "Follow-Up"
                    ) {
                        BulletPoint(
                            text = buildString {
                                append("Return")
                                followUp.timeframe?.let { append(" $it") }
                                followUp.locationOrMethod?.let { append(" ($it)") }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // ADDITIONAL NOTES
                if (extraction.additionalNotes.isNotEmpty()) {
                    SummarySection(
                        emoji = "📝",
                        title = "Additional Notes"
                    ) {
                        extraction.additionalNotes.forEach { note ->
                            BulletPoint(text = note)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Show transcription if available
            appointment.transcription?.let { transcription ->
                if (transcription.fullText.isNotEmpty()) {
                    SummarySection(
                        emoji = "📄",
                        title = "Full Transcription"
                    ) {
                        Text(
                            text = transcription.fullText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Bottom action buttons - LARGE tap targets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Save button
            Button(
                onClick = { /* Already saved automatically */ },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("💾  Save", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }
            
            // Share button
            OutlinedButton(
                onClick = { showShareDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("🤝  Share", fontSize = 20.sp, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recording?", color = TextPrimary) },
            text = { 
                Text(
                    "This will permanently delete this recording and its summary.",
                    color = TextSecondary
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceWhite
        )
    }
    
    // Share dialog - Matches Mockup 5
    if (showShareDialog) {
        ShareDialog(onDismiss = { showShareDialog = false })
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
                fontWeight = FontWeight.Bold,
                color = TextPrimary
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
            color = if (isWarning) AccentAmber else TextPrimary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 20.sp,
            color = if (isWarning) AccentAmber else TextPrimary,
            lineHeight = 28.sp
        )
    }
}

// ============================================================================
// PAST SUMMARIES SCREEN
// ============================================================================

@Composable
private fun PastSummariesScreen(
    appointments: List<Appointment>,
    onSelect: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        // Header - LARGE back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Past Summaries",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }
        
        if (appointments.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📋",
                    fontSize = 80.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "No recordings yet",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your appointment summaries will appear here",
                    fontSize = 18.sp,
                    color = TextHint,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(appointments) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onClick = { onSelect(appointment.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: Appointment,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon - LARGE
            val statusEmoji = when (appointment.status) {
                AppointmentStatus.DRAFT -> "📝"
                AppointmentStatus.TRANSCRIBED -> "📄"
                AppointmentStatus.PROCESSED -> "✅"
                AppointmentStatus.ARCHIVED -> "📦"
            }
            Text(text = statusEmoji, fontSize = 36.sp)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatDate(appointment.dateTime),
                    fontSize = 16.sp,
                    color = TextSecondary
                )
                
                appointment.transcription?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it.fullText.take(50) + if (it.fullText.length > 50) "..." else "",
                        fontSize = 16.sp,
                        color = TextHint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextHint,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// ============================================================================
// SHARE DIALOG 
// ============================================================================

@Composable
private fun ShareDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🤝", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Share with a Trusted Person",
                    fontSize = 22.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    "Choose how to share this summary:",
                    fontSize = 18.sp,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Secure link option - LARGE
                OutlinedButton(
                    onClick = { /* Not yet implemented */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🔗  Secure link (expires in 24h)", fontSize = 18.sp, color = PrimaryBlue)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Email PDF option - LARGE
                OutlinedButton(
                    onClick = { /* Not yet implemented */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("📧  Email encrypted PDF", fontSize = 18.sp, color = PrimaryBlue)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Accessibility note
                Text(
                    text = "Accessibility: Large tap targets, icon + label, clear affordances support motor and cognitive accessibility.",
                    fontSize = 14.sp,
                    color = TextHint,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(48.dp)
            ) {
                Text("❌  Cancel", fontSize = 18.sp, color = AccentRed)
            }
        },
        containerColor = SurfaceWhite
    )
}

// ============================================================================
// SETTINGS DIALOG 
// ============================================================================

@Composable
private fun SettingsDialog(onDismiss: () -> Unit) {
    var saveTranscripts by remember { mutableStateOf(true) }
    var carerMode by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🔒", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Privacy & Settings",
                    fontSize = 22.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                // Privacy guarantees (read-only checkmarks)
                PrivacyCheckItem(
                    checked = true,
                    text = "All processing is done offline"
                )
                PrivacyCheckItem(
                    checked = true,
                    text = "No audio is stored unless you choose to save"
                )
                PrivacyCheckItem(
                    checked = true,
                    text = "GDPR & EU AI Act compliant"
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = CardBorder)
                Spacer(modifier = Modifier.height(20.dp))
                
                // User settings (toggleable) - LARGE tap targets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Save transcripts locally",
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Checkbox(
                        checked = saveTranscripts,
                        onCheckedChange = { saveTranscripts = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PrimaryBlue
                        ),
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Enable Carer Mode",
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Checkbox(
                        checked = carerMode,
                        onCheckedChange = { carerMode = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PrimaryBlue
                        ),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(48.dp)
            ) {
                Text("Done", fontSize = 18.sp, color = PrimaryBlue)
            }
        },
        dismissButton = {},
        containerColor = SurfaceWhite
    )
}

@Composable
private fun PrivacyCheckItem(checked: Boolean, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (checked) "✅" else "⬜",
            fontSize = 22.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 18.sp,
            color = TextPrimary,
            lineHeight = 24.sp
        )
    }
}

// ============================================================================
// MODEL SETUP DIALOG
// ============================================================================

@Composable
private fun ModelSetupDialog(
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Speech Model Required",
                fontSize = 22.sp,
                color = PrimaryBlue,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "The Whisper speech model needs to be installed once for offline transcription.",
                    fontSize = 18.sp,
                    color = TextPrimary,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "Setup Instructions:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "1. Download ggml-tiny.bin (~75MB)\n" +
                           "   from huggingface.co/ggerganov/whisper.cpp\n\n" +
                           "2. Connect device via USB\n\n" +
                           "3. Run in terminal:\n" +
                           "   adb push ggml-tiny.bin /sdcard/Download/",
                    fontSize = 16.sp,
                    color = TextSecondary,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "The app will auto-detect the model.",
                    fontSize = 16.sp,
                    color = TextHint
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onRetry,
                modifier = Modifier.height(48.dp)
            ) {
                Text("Retry Detection", fontSize = 18.sp, color = PrimaryBlue)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(48.dp)
            ) {
                Text("Close", fontSize = 18.sp, color = TextSecondary)
            }
        },
        containerColor = SurfaceWhite
    )
}

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

private fun formatDuration(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    val hours = ms / (1000 * 60 * 60)
    
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
