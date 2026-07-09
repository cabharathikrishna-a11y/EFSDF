package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun MiniCalendarDialog(
    currentSelectedDateStr: String,
    onDateSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sdfInput = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) }
    val calendar = remember {
        val cal = java.util.Calendar.getInstance()
        try {
            val d = sdfInput.parse(currentSelectedDateStr)
            if (d != null) cal.time = d
        } catch (_: Exception) {}
        cal
    }

    var currentYear by remember { mutableStateOf(calendar.get(java.util.Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(calendar.get(java.util.Calendar.MONTH)) } // 0-11

    // Calculate days grid
    val daysInMonth = remember(currentYear, currentMonth) {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.YEAR, currentYear)
        cal.set(java.util.Calendar.MONTH, currentMonth)
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday...
        val maxDays = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        Pair(firstDayOfWeek, maxDays)
    }

    val (firstDayOfWeek, maxDays) = daysInMonth
    val monthName = remember(currentMonth) {
        val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        monthNames[currentMonth]
    }

    val WaterBlue = Color(0xFF38BDF8)

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF151515)),
            border = BorderStroke(1.dp, Color(0xFF333333))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header of Calendar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentMonth == 0) {
                                currentMonth = 11
                                currentYear -= 1
                            } else {
                                currentMonth -= 1
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF222222))
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Previous Month",
                            tint = WaterBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "$monthName $currentYear",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    IconButton(
                        onClick = {
                            if (currentMonth == 11) {
                                currentMonth = 0
                                currentYear += 1
                            } else {
                                currentMonth += 1
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF222222))
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next Month",
                            tint = WaterBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Days of week header labels
                val weekdays = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekdays.forEach { day ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Days grid
                val totalSlots = 42
                val cols = 7
                val rows = totalSlots / cols

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (c in 0 until cols) {
                                val slotIndex = r * cols + c
                                val dayNumber = slotIndex - (firstDayOfWeek - 2)
                                val isValidDay = dayNumber in 1..maxDays

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isValidDay) {
                                        val dayStr = String.format("%04d-%02d-%02d", currentYear, currentMonth + 1, dayNumber)
                                        val isSelected = dayStr == currentSelectedDateStr

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize(0.85f)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) WaterBlue else Color.Transparent
                                                )
                                                .clickable {
                                                    onDateSelected(dayStr)
                                                    onDismissRequest()
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayNumber.toString(),
                                                color = if (isSelected) Color.Black else Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            val todayStrLocal = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                            onDateSelected(todayStrLocal)
                            onDismissRequest()
                        }
                    ) {
                        Text("Reset to Today", color = WaterBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Close", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
