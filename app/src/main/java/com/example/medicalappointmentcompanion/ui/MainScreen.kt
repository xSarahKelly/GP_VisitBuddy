package com.example.medicalappointmentcompanion.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.medicalappointmentcompanion.model.Appointment
import com.example.medicalappointmentcompanion.model.AppState

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
    onClearError: () -> Unit,
    onUpdateAppointment: (Appointment) -> Unit,
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
    var showConsentDialog by remember { mutableStateOf(false) }
    var showReviewScreen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(state.isModelLoaded, state.isModelLoading, state.isModelDownloading) {
        if (!state.isModelLoaded && !state.isModelLoading && !state.isModelDownloading && state.modelError != null) {
            showModelDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundWhite)
    ) {
        when {
            state.isRecording -> {
                RecordingScreen(
                    recordingDuration = state.recordingDuration,
                    isTranscribing = state.isTranscribing,
                    onStop = onStopRecording,
                    onCancel = onCancelRecording
                )
            }

            state.isTranscribing -> {
                ProcessingScreen()
            }

            showReviewScreen && state.currentAppointment != null -> {
                ReviewScreen(
                    appointment = state.currentAppointment!!,
                    onSave = { updated ->
                        onUpdateAppointment(updated.copy(isLocked = true))
                        showReviewScreen = false
                    },
                    onBack = { showReviewScreen = false }
                )
            }

            state.currentAppointment != null -> {
                SummaryScreen(
                    appointment = state.currentAppointment!!,
                    onBack = onClearAppointment,
                    onDelete = { onDeleteAppointment(state.currentAppointment!!.id) },
                    onReview = { showReviewScreen = true },
                    onSave = { state.currentAppointment?.let { onUpdateAppointment(it) } }
                )
            }

            showPastSummaries -> {
                PastSummariesScreen(
                    appointments = state.appointments,
                    onSelect = onSelectAppointment,
                    onBack = { showPastSummaries = false }
                )
            }

            else -> {
                HomeScreen(
                    isModelLoaded = state.isModelLoaded,
                    isModelLoading = state.isModelLoading,
                    isModelDownloading = state.isModelDownloading,
                    modelDownloadProgress = state.modelDownloadProgress,
                    hasPermission = hasPermission,
                    onStartRecording = onStartRecording,
                    onViewPastSummaries = { showPastSummaries = true },
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    onOpenSettings = { showSettingsDialog = true },
                    onLoadModel = { showModelDialog = true },
                    onShowConsent = { showConsentDialog = true }
                )
            }
        }

        state.errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = onClearError) {
                        Text("Dismiss", color = AppColors.PrimaryBlue)
                    }
                },
                containerColor = AppColors.SurfaceWhite
            ) {
                Text(error, color = AppColors.TextPrimary)
            }
        }

        state.modelError?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = onClearError) {
                        Text("Dismiss", color = AppColors.PrimaryBlue)
                    }
                },
                containerColor = Color(0xFFFFEBEE)
            ) {
                Text(error, color = AppColors.AccentRed)
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showModelDialog) {
        ModelSetupDialog(
            onDismiss = { showModelDialog = false },
            onRetry = {
                showModelDialog = false
                onRetryModelLoad()
            }
        )
    }

    if (showConsentDialog) {
        AlertDialog(
            onDismissRequest = { showConsentDialog = false },
            title = { Text("GP Consent", color = AppColors.TextPrimary) },
            text = {
                Text(
                    "I confirm that I consent to the recording of this consultation.",
                    color = AppColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConsentDialog = false
                        onStartRecording()
                    }
                ) {
                    Text("I Confirm", color = AppColors.PrimaryBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConsentDialog = false }) {
                    Text("Cancel", color = AppColors.TextSecondary)
                }
            },
            containerColor = AppColors.SurfaceWhite
        )
    }
}
