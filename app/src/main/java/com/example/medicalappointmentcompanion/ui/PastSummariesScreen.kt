package com.example.medicalappointmentcompanion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicalappointmentcompanion.model.Appointment
import com.example.medicalappointmentcompanion.model.AppointmentStatus

@Composable
fun PastSummariesScreen(
    appointments: List<Appointment>,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
    canSwitchAccount: Boolean = false,
    onOpenAccount: () -> Unit = {}
) {
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
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.PrimaryBlue,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Past Summaries",
                fontSize = 26.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = AppColors.PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
            if (canSwitchAccount) {
                TextButton(onClick = onOpenAccount) {
                    Text("Switch account", fontSize = 16.sp, color = AppColors.PrimaryBlue)
                }
            }
        }

        if (appointments.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "📋", fontSize = 80.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "No recordings yet",
                    fontSize = 24.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = AppColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your appointment summaries will appear here",
                    fontSize = 18.sp,
                    color = AppColors.TextHint,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = AppUtils.formatDate(appointment.dateTime),
                    fontSize = 16.sp,
                    color = AppColors.TextSecondary
                )

                appointment.transcription?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it.fullText.take(50) + if (it.fullText.length > 50) "..." else "",
                        fontSize = 16.sp,
                        color = AppColors.TextHint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppColors.TextHint,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
