package com.example.medicalappointmentcompanion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModelSetupDialog(
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Speech Model Required",
                fontSize = 22.sp,
                color = AppColors.PrimaryBlue,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "The Whisper speech model needs to be installed once for offline transcription.",
                    fontSize = 18.sp,
                    color = AppColors.TextPrimary,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Setup Instructions:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryBlue
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "The model file (ggml-small.en.bin) should be included in the APK.\n\n" +
                            "If you're seeing this message, the model wasn't found in the app's assets.\n\n" +
                            "To fix:\n" +
                            "1. Ensure ggml-small.en.bin is in app/src/main/assets/\n" +
                            "2. Clean and rebuild the project\n" +
                            "3. Reinstall the APK\n\n" +
                            "The model file must be present when building the APK.",
                    fontSize = 16.sp,
                    color = AppColors.TextSecondary,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "The app will auto-detect the model.",
                    fontSize = 16.sp,
                    color = AppColors.TextHint
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onRetry,
                modifier = Modifier.height(48.dp)
            ) {
                Text("Retry Detection", fontSize = 18.sp, color = AppColors.PrimaryBlue)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(48.dp)
            ) {
                Text("Close", fontSize = 18.sp, color = AppColors.TextSecondary)
            }
        },
        containerColor = AppColors.SurfaceWhite
    )
}
