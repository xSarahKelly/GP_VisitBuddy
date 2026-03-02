package com.example.medicalappointmentcompanion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "GP VisitBuddy",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryBlue,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign in to continue",
            fontSize = 18.sp,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

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
                fontSize = 14.sp,
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

@Composable
fun SignUpScreen(
    onSignUp: (name: String, username: String, password: String) -> Unit,
    onSwitchToLogin: () -> Unit,
    authError: String?
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Create Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryBlue,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter your details to get started",
            fontSize = 18.sp,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            placeholder = { Text("Your name") },
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
                fontSize = 14.sp,
                color = AppColors.AccentRed,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (password != confirmPassword) return@Button
                onSignUp(name, username, password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Create Account", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
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
