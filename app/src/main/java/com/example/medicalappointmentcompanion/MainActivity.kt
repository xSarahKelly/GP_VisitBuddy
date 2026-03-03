package com.example.medicalappointmentcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicalappointmentcompanion.ui.MainScreen
import com.example.medicalappointmentcompanion.ui.MainViewModel
import com.example.medicalappointmentcompanion.ui.theme.MedicalAppointmentCompanionTheme

/**
 * Medical Appointment Companion
 * 
 * A local-only speech-to-text application for recording and 
 * transcribing medical appointments using whisper.cpp.
 * 
 * Features:
 * - Local transcription (no cloud, no APIs)
 * - Medical information extraction
 * - Appointment management
 * - Secure local storage
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MedicalAppointmentCompanionTheme {
                val viewModel: MainViewModel = viewModel()
                val state by viewModel.state.collectAsState()
                
                MainScreen(
                    state = state,
                    onRetryModelLoad = { viewModel.retryModelLoad() },
                    onStartRecording = { viewModel.startRecording() },
                    onStopRecording = { viewModel.stopRecording() },
                    onCancelRecording = { viewModel.cancelRecording() },
                    onSelectAppointment = { id -> viewModel.selectAppointment(id) },
                    onDeleteAppointment = { id -> viewModel.deleteAppointment(id) },
                    onClearAppointment = { viewModel.clearCurrentAppointment() },
                    onClearError = { viewModel.clearError() },
                    onUpdateAppointment = { viewModel.updateAppointment(it) },
                    onSignIn = { user, pass -> viewModel.signIn(user, pass) },
                    onSignUp = { userType, username, password ->
                        viewModel.signUp(userType, username, password)
                    },
                    onSignOut = { viewModel.signOut() },
                    onClearAuthError = { viewModel.clearAuthError() },
                    onAddAccount = { user, pass, name, dob, meds -> viewModel.addAccount(user, pass, name, dob, meds) },
                    onVerifyAndSwitchAccount = { id, pass, onResult ->
                        viewModel.verifyAndSwitchAccount(id, pass, onResult)
                    },
                    onSwitchAccount = { viewModel.switchAccount(it) },
                    onUpdateProfile = { name, dob, meds -> viewModel.updateProfile(name, dob, meds) },
                    onCompleteAccountSetup = { name, dob, meds ->
                        viewModel.completeAccountSetup(name, dob, meds)
                    },
                    onDeleteAccount = { id -> viewModel.deleteAccount(id) },
                    getAccountsForUser = { viewModel.getAccountsForUser() },
                )
            }
        }
    }
}
