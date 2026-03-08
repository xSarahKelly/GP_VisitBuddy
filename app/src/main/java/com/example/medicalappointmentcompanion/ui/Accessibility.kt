package com.example.medicalappointmentcompanion.ui

import androidx.compose.ui.unit.sp

/**
 * WCAG 2.2 accessibility typography (Section 3.5).
 * - Large text with sufficient contrast
 * - Minimum 16sp for body text (readability)
 * - 18sp+ for headings and important labels
 */
object Accessibility {
    /** Screen title (e.g. "GP VisitBuddy") */
    const val TitleSize = 28f
    val TitleSp = TitleSize.sp

    /** Section heading */
    const val HeadingSize = 26f
    val HeadingSp = HeadingSize.sp

    /** Subheading / card title */
    const val SubheadingSize = 22f
    val SubheadingSp = SubheadingSize.sp

    /** Body text - minimum for readability (WCAG) */
    const val BodySize = 18f
    val BodySp = BodySize.sp

    /** Secondary body / labels */
    const val BodySmallSize = 16f
    val BodySmallSp = BodySmallSize.sp

    /** Caption / hint - use sparingly, ensure 4.5:1 contrast */
    const val CaptionSize = 16f
    val CaptionSp = CaptionSize.sp
}
