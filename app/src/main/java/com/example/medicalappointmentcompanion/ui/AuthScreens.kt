package com.example.medicalappointmentcompanion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.medicalappointmentcompanion.model.UserType

@Composable
fun LoginScreen(
    onLogin: (username: String, password: String) -> Unit,
    onSwitchToSignUp: () -> Unit,
    authError: String?
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundWhite),
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
                Text("GP VisitBuddy", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Sign in to continue", fontSize = 16.sp, color = Color.White.copy(alpha = 0.9f))
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    placeholder = { Text("Enter your username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.PrimaryBlue,
                        unfocusedBorderColor = AppColors.CardBorder,
                        focusedLabelColor = AppColors.PrimaryBlue,
                        unfocusedLabelColor = AppColors.TextSecondary,
                        focusedTextColor = AppColors.TextPrimary,
                        unfocusedTextColor = AppColors.TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("Enter your password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.PrimaryBlue,
                        unfocusedBorderColor = AppColors.CardBorder,
                        focusedLabelColor = AppColors.PrimaryBlue,
                        unfocusedLabelColor = AppColors.TextSecondary,
                        focusedTextColor = AppColors.TextPrimary,
                        unfocusedTextColor = AppColors.TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                authError?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        fontSize = 16.sp,
                        color = AppColors.AccentRed,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { onLogin(username, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Sign In", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onSwitchToSignUp) {
            Text(
                text = "Don't have an account? Sign up",
                fontSize = 16.sp,
                color = AppColors.PrimaryBlue
            )
        }
        }
    }
}

@Composable
fun SignUpScreen(
    onSignUp: (userType: UserType, username: String, password: String) -> Unit,
    onSwitchToLogin: () -> Unit,
    authError: String?
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf(UserType.Patient) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundWhite),
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
                Text("Create Account", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Enter your details to get started", fontSize = 16.sp, color = Color.White.copy(alpha = 0.9f))
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Account type",
                    fontSize = 16.sp,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .selectable(
                        selected = userType == UserType.Patient,
                        onClick = { userType = UserType.Patient }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = userType == UserType.Patient,
                    onClick = { userType = UserType.Patient },
                    colors = RadioButtonDefaults.colors(selectedColor = AppColors.PrimaryBlue)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Patient", color = AppColors.TextPrimary)
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .selectable(
                        selected = userType == UserType.Carer,
                        onClick = { userType = UserType.Carer }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = userType == UserType.Carer,
                    onClick = { userType = UserType.Carer },
                    colors = RadioButtonDefaults.colors(selectedColor = AppColors.PrimaryBlue)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Carer", color = AppColors.TextPrimary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    placeholder = { Text("Choose a username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.PrimaryBlue,
                unfocusedBorderColor = AppColors.CardBorder,
                focusedLabelColor = AppColors.PrimaryBlue,
                unfocusedLabelColor = AppColors.TextSecondary,
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary
            ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("Choose a password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.PrimaryBlue,
                unfocusedBorderColor = AppColors.CardBorder,
                focusedLabelColor = AppColors.PrimaryBlue,
                unfocusedLabelColor = AppColors.TextSecondary,
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary
            ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    placeholder = { Text("Re-enter your password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.PrimaryBlue,
                        unfocusedBorderColor = AppColors.CardBorder,
                        focusedLabelColor = AppColors.PrimaryBlue,
                        unfocusedLabelColor = AppColors.TextSecondary,
                        focusedTextColor = AppColors.TextPrimary,
                        unfocusedTextColor = AppColors.TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                val signUpError = authError ?: if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword)
                    "Passwords do not match" else null
                signUpError?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        fontSize = 16.sp,
                        color = AppColors.AccentRed,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (password != confirmPassword) return@Button
                        onSignUp(userType, username, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Create Account", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onSwitchToLogin) {
            Text(
                text = "Already have an account? Sign in",
                fontSize = 16.sp,
                color = AppColors.PrimaryBlue
            )
        }
        }
    }
}
