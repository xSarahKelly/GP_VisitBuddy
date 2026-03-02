package com.example.medicalappointmentcompanion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
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
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "GP VisitBuddy",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryBlue,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "An Edge-AI solution for patient recall",
            fontSize = 20.sp,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

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
                Text(
                    text = "This will only happen once",
                    fontSize = 14.sp,
                    color = AppColors.TextHint,
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
            TextButton(
                onClick = onOpenSettings,
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = "Privacy & Settings",
                    fontSize = 18.sp,
                    color = AppColors.PrimaryBlue
                )
            }
            TextButton(
                onClick = onLogout,
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = "Log out",
                    fontSize = 18.sp,
                    color = AppColors.AccentRed
                )
            }
        }
    }
}
