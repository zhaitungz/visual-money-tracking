package com.example.visualmoneytracker.data.worker

import java.time.DayOfWeek

enum class ReminderFrequency { DAILY, WEEKLY, MONTHLY }

data class ReminderSettings(
    val enabled: Boolean,
    val frequency: ReminderFrequency,
    val hour: Int,
    val minute: Int,
    val dayOfWeek: DayOfWeek? = null,
    val dayOfMonth: Int? = null
)
