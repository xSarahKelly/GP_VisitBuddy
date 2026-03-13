package com.example.medicalappointmentcompanion.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicalappointmentcompanion.model.AccountInfo
import com.example.medicalappointmentcompanion.model.UserSession

@Composable
fun HomeScreen(
    userSession: UserSession?,
    isModelLoaded: Boolean,
    isModelLoading: Boolean,
    isModelDownloading: Boolean,
    modelDownloadProgress: Float,
    hasPermission: Boolean,
    onStartRecording: () -> Unit,
    onViewPastSummaries: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onLoadModel: () -> Unit,
    onShowConsent: () -> Unit,
    onLogout: () -> Unit,
    onShowAccount: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AppColors.PrimaryBlue, AppColors.PrimaryBlueDark)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GP VisitBuddy",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "An Edge-AI solution for patient recall",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
                userSession?.let { session ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Logged in as: ${session.username}",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                when {
                    !isModelLoaded -> onLoadModel()
                    !hasPermission -> onRequestPermission()
                    else -> onShowConsent()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryBlue),
            shape = RoundedCornerShape(16.dp),
            enabled = !isModelLoading && !isModelDownloading
        ) {
            if (isModelLoading || isModelDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = "🎤  Start Recording",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        if (isModelDownloading) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator(
                    progress = { modelDownloadProgress },
                    modifier = Modifier.size(48.dp),
                    color = AppColors.PrimaryBlue,
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Downloading speech model... ${(modelDownloadProgress * 100).toInt()}%",
                    fontSize = 16.sp,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else if (!isModelLoaded && !isModelLoading) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "⚠️ Tap to set up speech model first",
                fontSize = 16.sp,
                color = AppColors.AccentAmber
            )
        } else if (!hasPermission) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "⚠️ Microphone permission required",
                fontSize = 16.sp,
                color = AppColors.AccentAmber
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onViewPastSummaries,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.PrimaryBlue),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(listOf(AppColors.PrimaryBlueLight, AppColors.PrimaryBlueLight))
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "📋  View Past Summaries",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.PrimaryBlue
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(text = "🔒", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "All processing happens privately on your device.",
                fontSize = 18.sp,
                color = AppColors.TextSecondary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onShowAccount) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Person, contentDescription = "Account", tint = AppColors.PrimaryBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Account", fontSize = 14.sp, color = AppColors.PrimaryBlue)
                }
            }
            TextButton(onClick = onOpenSettings) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Settings, contentDescription = "Privacy & Settings", tint = AppColors.PrimaryBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Privacy & Settings", fontSize = 14.sp, color = AppColors.PrimaryBlue)
                }
            }
            TextButton(onClick = onLogout) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out", tint = AppColors.AccentRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Log out", fontSize = 14.sp, color = AppColors.AccentRed)
                }
            }
        }
        }
    }
}

@Composable
private fun AccountCard(
    account: AccountInfo,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isCurrent) Modifier.border(2.dp, AppColors.PrimaryBlue, RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isCurrent) {
                Text(text = "✓", fontSize = 20.sp, color = AppColors.PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.displayName.ifEmpty { account.username },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary
                )
                val ageStr = account.age?.let { "$it years" } ?: "—"
                val dobStr = account.dateOfBirth ?: "—"
                val medsStr = account.currentMedications?.take(60)?.let { if ((account.currentMedications?.length ?: 0) > 60) "$it…" else it } ?: "—"
                Text(
                    text = "Age: $ageStr · DOB: $dobStr",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
                Text(
                    text = "Medications: $medsStr",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun AccountSetupScreen(
    username: String,
    onComplete: (displayName: String, dateOfBirth: String?, currentMedications: String?) -> Unit
) {
    var displayName by remember { mutableStateOf(username) }
    var dateOfBirth by remember { mutableStateOf("") }
    var currentMedications by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Account setup",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryBlue,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add your details to get started",
            fontSize = 18.sp,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.PrimaryBlue,
                unfocusedBorderColor = AppColors.CardBorder
            ),
            shape = RoundedCornerShape(12.dp)
        )
        DatePickerField(
            value = dateOfBirth,
            onValueChange = { dateOfBirth = it },
            label = "Date of birth",
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )
        OutlinedTextField(
            value = currentMedications,
            onValueChange = { currentMedications = it },
            label = { Text("Current medications") },
            placeholder = { Text("e.g. Aspirin, Metformin") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            minLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.PrimaryBlue,
                unfocusedBorderColor = AppColors.CardBorder
            ),
            shape = RoundedCornerShape(12.dp)
        )
        Button(
            onClick = {
                onComplete(
                    displayName.trim().ifEmpty { username },
                    dateOfBirth.trim().takeIf { it.isNotEmpty() },
                    currentMedications.trim().takeIf { it.isNotEmpty() }
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun AccountScreen(
    currentAccount: AccountInfo,
    accounts: List<AccountInfo>,
    currentAccountId: String,
    isCarer: Boolean,
    onBack: () -> Unit,
    onSaveProfile: (displayName: String, dateOfBirth: String?, currentMedications: String?) -> Unit,
    onSwitchAccount: (accountId: String) -> Unit,
    onVerifyAndSwitchAccount: (accountId: String, password: String, onResult: (Boolean) -> Unit) -> Unit,
    onShowAddAccount: () -> Unit,
    onDeleteAccount: (accountId: String) -> Unit
) {
    var displayName by remember { mutableStateOf(currentAccount.displayName.ifEmpty { currentAccount.username }) }
    var dateOfBirth by remember { mutableStateOf(currentAccount.dateOfBirth ?: "") }
    var currentMedications by remember { mutableStateOf(currentAccount.currentMedications ?: "") }
    var accountToVerify by remember { mutableStateOf<AccountInfo?>(null) }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(currentAccount) {
        displayName = currentAccount.displayName.ifEmpty { currentAccount.username }
        dateOfBirth = currentAccount.dateOfBirth ?: ""
        currentMedications = currentAccount.currentMedications ?: ""
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.PrimaryBlue
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Account",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryBlue
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Your details",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.PrimaryBlue,
                    unfocusedBorderColor = AppColors.CardBorder
                ),
                shape = RoundedCornerShape(12.dp)
            )
            DatePickerField(
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                label = "Date of birth",
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
            OutlinedTextField(
                value = currentMedications,
                onValueChange = { currentMedications = it },
                label = { Text("Current medications") },
                placeholder = { Text("e.g. Aspirin, Metformin") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.PrimaryBlue,
                    unfocusedBorderColor = AppColors.CardBorder
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Button(
                onClick = {
                    onSaveProfile(
                        displayName.trim().ifEmpty { currentAccount.username },
                        dateOfBirth.trim().takeIf { it.isNotEmpty() },
                        currentMedications.trim().takeIf { it.isNotEmpty() }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))
            TextButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete account", fontSize = 16.sp, color = AppColors.AccentRed)
            }

            if (isCarer || accounts.size > 1) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Switch account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                accounts.forEach { acc ->
                    val isCurrent = acc.accountId == currentAccountId
                    AccountCard(
                        account = acc,
                        isCurrent = isCurrent,
                        onClick = {
                            if (isCurrent) {
                                onSwitchAccount(acc.accountId)
                                onBack()
                            } else {
                                accountToVerify = acc
                                password = ""
                                error = null
                            }
                        },
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                if (isCarer) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onShowAddAccount,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.PrimaryBlue),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Add another account", fontSize = 18.sp)
                    }
                }
            }
        }
    }

    accountToVerify?.let { acc ->
        AlertDialog(
            onDismissRequest = { accountToVerify = null },
            title = { Text("Enter password", color = AppColors.TextPrimary) },
            text = {
                Column {
                    Text(
                        "Password for ${acc.displayName.ifEmpty { acc.username }}",
                        color = AppColors.TextSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.PrimaryBlue,
                            unfocusedBorderColor = AppColors.CardBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    error?.let { Text(it, color = AppColors.AccentRed, modifier = Modifier.padding(top = 8.dp)) }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (password.isBlank()) {
                            error = "Enter password"
                            return@TextButton
                        }
                        onVerifyAndSwitchAccount(acc.accountId, password) { ok ->
                            if (ok) {
                                accountToVerify = null
                                onSwitchAccount(acc.accountId)
                                onBack()
                            } else {
                                error = "Incorrect password"
                            }
                        }
                    }
                ) {
                    Text("Switch", color = AppColors.PrimaryBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { accountToVerify = null }) {
                    Text("Cancel", color = AppColors.TextSecondary)
                }
            },
            containerColor = AppColors.SurfaceWhite
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete account?", color = AppColors.TextPrimary) },
            text = {
                Text(
                    "This will permanently remove your account and all associated data. This cannot be undone.",
                    color = AppColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteAccount(currentAccountId)
                        onBack()
                    }
                ) {
                    Text("Delete", color = AppColors.AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = AppColors.TextSecondary)
                }
            },
            containerColor = AppColors.SurfaceWhite
        )
    }
}

@Composable
fun SwitchAccountDialog(
    accounts: List<AccountInfo>,
    currentAccountId: String,
    onDismiss: () -> Unit,
    onVerifyAndSwitch: (accountId: String, password: String, onResult: (Boolean) -> Unit) -> Unit
) {
    var selectedAccount by remember { mutableStateOf<AccountInfo?>(null) }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val otherAccounts = accounts.filter { it.accountId != currentAccountId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Switch Account", color = AppColors.TextPrimary) },
        text = {
            Column {
                Text(
                    "Enter the password for the account you want to switch to.",
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                otherAccounts.forEach { acc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedAccount?.accountId == acc.accountId,
                            onClick = { selectedAccount = acc; error = null },
                            colors = RadioButtonDefaults.colors(selectedColor = AppColors.PrimaryBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${acc.displayName} (${acc.username}) - ${acc.userType.name}",
                            color = AppColors.TextPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    label = { Text("Password") },
                    placeholder = { Text("Account password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.PrimaryBlue,
                        unfocusedBorderColor = AppColors.CardBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                error?.let { Text(it, color = AppColors.AccentRed, modifier = Modifier.padding(top = 8.dp)) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val acc = selectedAccount
                    if (acc == null) {
                        error = "Select an account"
                        return@TextButton
                    }
                    if (password.isBlank()) {
                        error = "Enter password"
                        return@TextButton
                    }
                    onVerifyAndSwitch(acc.accountId, password) { ok ->
                        if (ok) onDismiss()
                        else error = "Incorrect password"
                    }
                }
            ) {
                Text("Switch", color = AppColors.PrimaryBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppColors.TextSecondary)
            }
        },
        containerColor = AppColors.SurfaceWhite
    )
}

@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onAddAccount: (username: String, password: String, displayName: String, dateOfBirth: String?, currentMedications: String?) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var currentMedications by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Patient Account", color = AppColors.TextPrimary) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Add a patient's account to access their summaries.",
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.PrimaryBlue,
                        unfocusedBorderColor = AppColors.CardBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Name") },
                    placeholder = { Text("e.g. Mum") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.PrimaryBlue,
                        unfocusedBorderColor = AppColors.CardBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                DatePickerField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = "Date of birth",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = currentMedications,
                    onValueChange = { currentMedications = it },
                    label = { Text("Current medications") },
                    placeholder = { Text("e.g. Aspirin, Metformin") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.PrimaryBlue,
                        unfocusedBorderColor = AppColors.CardBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.PrimaryBlue,
                        unfocusedBorderColor = AppColors.CardBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (username.isNotBlank() && password.isNotBlank()) {
                        onAddAccount(
                            username.trim(),
                            password,
                            displayName.trim().ifEmpty { username.trim() },
                            dateOfBirth.trim().takeIf { it.isNotEmpty() },
                            currentMedications.trim().takeIf { it.isNotEmpty() }
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("Add", color = AppColors.PrimaryBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppColors.TextSecondary)
            }
        },
        containerColor = AppColors.SurfaceWhite
    )
}
