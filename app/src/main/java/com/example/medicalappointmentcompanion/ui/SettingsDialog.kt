package com.example.medicalappointmentcompanion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🔒", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Privacy Info",
                    fontSize = 22.sp,
                    color = AppColors.PrimaryBlue,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                PrivacyCheckItem(checked = true, text = "All processing is done offline")
                PrivacyCheckItem(checked = true, text = "No audio is stored unless you choose to save")
                PrivacyCheckItem(checked = true, text = "GDPR & EU AI Act compliant")
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(48.dp)
            ) {
                Text("Done", fontSize = 18.sp, color = AppColors.PrimaryBlue)
            }
        },
        dismissButton = {},
        containerColor = AppColors.SurfaceWhite
    )
}

@Composable
private fun PrivacyCheckItem(checked: Boolean, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (checked) "✅" else "⬜",
            fontSize = 22.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 18.sp,
            color = AppColors.TextPrimary,
            lineHeight = 24.sp
        )
    }
}
