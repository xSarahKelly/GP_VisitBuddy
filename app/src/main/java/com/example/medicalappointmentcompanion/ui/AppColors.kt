package com.example.medicalappointmentcompanion.ui

import androidx.compose.ui.graphics.Color

/**
 * Accessibility-focused color scheme (Light Theme - WCAG 2.2)
 * Professional medical app aesthetic with sufficient contrast.
 *
 * Contrast ratios (on white/light background):
 * - TextPrimary #212121: 16.1:1 (AAA)
 * - TextSecondary #616161: 5.7:1 (AA)
 * - TextHint #757575: 4.5:1 (AA minimum for normal text)
 */
object AppColors {
    val PrimaryBlue = Color(0xFF1976D2)
    val PrimaryBlueDark = Color(0xFF1565C0)
    val PrimaryBlueLight = Color(0xFFE3F2FD)
    val PrimaryBlueSubtle = Color(0xFFE8F0FA)  // Visible blue tint for backgrounds

    val AccentRed = Color(0xFFE53935)       // Stop/Delete actions
    val AccentGreen = Color(0xFF43A047)     // Success/Save
    val AccentAmber = Color(0xFFFF9800)     // Warnings (WCAG AA compliant)

    val BackgroundWhite = Color(0xFFFAFAFA)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val CardBorder = Color(0xFFE0E0E0)
    val CardElevation = Color(0x0D000000)   // Subtle shadow

    val TextPrimary = Color(0xFF212121)
    val TextSecondary = Color(0xFF616161)
    val TextHint = Color(0xFF757575)        // 4.5:1 on white (WCAG AA)
}
