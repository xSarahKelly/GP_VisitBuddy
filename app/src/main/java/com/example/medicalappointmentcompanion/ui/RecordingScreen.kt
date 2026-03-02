package com.example.medicalappointmentcompanion.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecordingScreen(
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

        Row(verticalAlignment = Alignment.CenterVertically) {
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
                color = AppColors.PrimaryBlue
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = AppUtils.formatDuration(recordingDuration),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Doctor: Take paracetamol 500mg...",
                    fontSize = 20.sp,
                    color = AppColors.TextSecondary.copy(alpha = 0.6f),
                    fontStyle = FontStyle.Italic
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "VisitBuddy is listening and transcribing...",
                    fontSize = 18.sp,
                    color = AppColors.TextHint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStop,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentRed),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "⏹  Stop & Generate Summary",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onCancel,
            modifier = Modifier.height(48.dp)
        ) {
            Text(
                text = "Cancel Recording",
                fontSize = 18.sp,
                color = AppColors.TextSecondary
            )
        }
    }
}
