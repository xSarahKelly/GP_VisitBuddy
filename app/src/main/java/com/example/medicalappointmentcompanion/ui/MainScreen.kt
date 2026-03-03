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
    onSignIn: (username: String, password: String) -> Unit,
    onSignUp: (userType: com.example.medicalappointmentcompanion.model.UserType, username: String, password: String) -> Unit,
    onSignOut: () -> Unit,
    onClearAuthError: () -> Unit,
    onAddAccount: (username: String, password: String, displayName: String, dateOfBirth: String?, currentMedications: String?) -> Unit,
    onVerifyAndSwitchAccount: (accountId: String, password: String, onResult: (Boolean) -> Unit) -> Unit,
    onSwitchAccount: (accountId: String) -> Unit,
    onUpdateProfile: (displayName: String, dateOfBirth: String?, currentMedications: String?) -> Unit,
    onCompleteAccountSetup: (displayName: String, dateOfBirth: String?, currentMedications: String?) -> Unit,
    onDeleteAccount: (accountId: String) -> Unit,
    getAccountsForUser: () -> List<com.example.medicalappointmentcompanion.model.AccountInfo>,
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
    var showSignUp by remember { mutableStateOf(false) }
    var showAccountScreen by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }

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
            // Not logged in - show auth screens
            state.userSession == null -> {
                if (showSignUp) {
                    SignUpScreen(
                        onSignUp = onSignUp,
                        onSwitchToLogin = { showSignUp = false; onClearAuthError() },
                        authError = state.authError
                    )
                } else {
                    LoginScreen(
                        onLogin = onSignIn,
                        onSwitchToSignUp = { showSignUp = true; onClearAuthError() },
                        authError = state.authError
                    )
                }
            }
            // Account setup required after sign-up
            state.userSession != null && !state.userSession!!.setupComplete -> {
                AccountSetupScreen(
                    username = state.userSession!!.username,
                    onComplete = onCompleteAccountSetup
                )
            }
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
                    onSave = { state.currentAppointment?.let { onUpdateAppointment(it) } },
                    canSwitchAccount = state.userSession?.canSwitchAccount == true,
                    onOpenAccount = { showAccountScreen = true }
                )
            }

            showPastSummaries -> {
                PastSummariesScreen(
                    appointments = state.appointments,
                    onSelect = onSelectAppointment,
                    onBack = { showPastSummaries = false },
                    canSwitchAccount = state.userSession?.canSwitchAccount == true,
                    onOpenAccount = { showAccountScreen = true }
                )
            }

            showAccountScreen && state.userSession != null -> {
                val session = state.userSession!!
                val accounts = getAccountsForUser()
                val currentAccount = accounts.find { it.accountId == session.accountId }
                    ?: com.example.medicalappointmentcompanion.model.AccountInfo(
                        accountId = session.accountId,
                        username = session.username,
                        displayName = session.displayName,
                        userType = session.userType
                    )
                AccountScreen(
                    currentAccount = currentAccount,
                    accounts = accounts,
                    currentAccountId = state.userSession!!.accountId,
                    isCarer = state.userSession!!.userType == com.example.medicalappointmentcompanion.model.UserType.Carer,
                    onBack = { showAccountScreen = false },
                    onSaveProfile = { name, dob, meds ->
                        onUpdateProfile(name, dob, meds)
                    },
                    onSwitchAccount = { id ->
                        onSwitchAccount(id)
                        showAccountScreen = false
                    },
                    onVerifyAndSwitchAccount = onVerifyAndSwitchAccount,
                    onShowAddAccount = { showAddAccountDialog = true },
                    onDeleteAccount = { id ->
                        onDeleteAccount(id)
                        showAccountScreen = false
                    }
                )
            }

            else -> {
                HomeScreen(
                    userSession = state.userSession,
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
                    onShowConsent = { showConsentDialog = true },
                    onLogout = onSignOut,
                    onShowAccount = { showAccountScreen = true }
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

    if (showAddAccountDialog) {
        AddAccountDialog(
            onDismiss = { showAddAccountDialog = false },
            onAddAccount = { user, pass, name, dob, meds ->
                onAddAccount(user, pass, name, dob, meds)
                showAddAccountDialog = false
                showAccountScreen = false
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
