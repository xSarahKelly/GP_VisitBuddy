package com.example.medicalappointmentcompanion.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val displayFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

/**
 * Parses yyyy-MM-dd string to millis, or null if invalid.
 */
private fun parseDobToMillis(dob: String): Long? {
    if (dob.isBlank()) return null
    return try {
        dateFormat.parse(dob.trim())?.time
    } catch (_: Exception) { null }
}

/**
 * Formats millis to yyyy-MM-dd for storage.
 */
private fun millisToDob(millis: Long): String = dateFormat.format(Date(millis))

/**
 * Formats millis for display (e.g. "15 Mar 1985").
 */
private fun millisToDisplay(millis: Long): String = displayFormat.format(Date(millis))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Date of birth",
    modifier: Modifier = Modifier,
    placeholder: String = "Select date"
) {
    var showPicker by remember { mutableStateOf(false) }
    val initialMillis = remember(value) {
        parseDobToMillis(value) ?: run {
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, -30) // Default to 30 years ago
            cal.timeInMillis
        }
    }

    OutlinedTextField(
        value = value.let { if (it.isBlank()) "" else parseDobToMillis(it)?.let { m -> millisToDisplay(m) } ?: it },
        onValueChange = { },
        readOnly = true,
        label = { Text(label, fontSize = 18.sp) },
        placeholder = { Text(placeholder, fontSize = 18.sp, color = AppColors.TextHint) },
        modifier = modifier
            .fillMaxWidth()
            .clickable { showPicker = true },
        textStyle = TextStyle(fontSize = 18.sp, color = AppColors.TextPrimary),
        trailingIcon = {
            androidx.compose.material3.IconButton(onClick = { showPicker = true }) {
                androidx.compose.material3.Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = "Pick date",
                    tint = AppColors.PrimaryBlue
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.PrimaryBlue,
            unfocusedBorderColor = AppColors.CardBorder,
            focusedLabelColor = AppColors.PrimaryBlue,
            unfocusedLabelColor = AppColors.TextSecondary,
            focusedTextColor = AppColors.TextPrimary,
            unfocusedTextColor = AppColors.TextPrimary,
            cursorColor = AppColors.PrimaryBlue
        ),
        shape = RoundedCornerShape(12.dp)
    )

    if (showPicker) {
        val state = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = parseDobToMillis(value) ?: initialMillis,
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
        )
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            onValueChange(millisToDob(millis))
                        }
                        showPicker = false
                    }
                ) {
                    Text("OK", color = AppColors.PrimaryBlue)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showPicker = false }) {
                    Text("Cancel", color = AppColors.TextSecondary)
                }
            },
            colors = androidx.compose.material3.DatePickerDefaults.colors(
                containerColor = AppColors.SurfaceWhite,
                titleContentColor = AppColors.TextPrimary,
                headlineContentColor = AppColors.TextPrimary,
                weekdayContentColor = AppColors.TextSecondary,
                subheadContentColor = AppColors.TextSecondary,
                yearContentColor = AppColors.TextPrimary,
                currentYearContentColor = AppColors.PrimaryBlue,
                selectedYearContentColor = AppColors.SurfaceWhite,
                selectedYearContainerColor = AppColors.PrimaryBlue,
                dayContentColor = AppColors.TextPrimary,
                selectedDayContentColor = AppColors.SurfaceWhite,
                selectedDayContainerColor = AppColors.PrimaryBlue,
                todayContentColor = AppColors.PrimaryBlue,
                todayDateBorderColor = AppColors.PrimaryBlue
            )
        ) {
            androidx.compose.material3.DatePicker(state = state)
        }
    }
}
