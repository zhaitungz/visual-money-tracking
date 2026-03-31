package com.example.visualmoneytracker.presentation.settings

import androidx.lifecycle.ViewModel
import com.example.visualmoneytracker.data.worker.ReminderFrequency
import com.example.visualmoneytracker.data.worker.ReminderSettings
import com.example.visualmoneytracker.domain.usecase.ScheduleReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val scheduleReminderUseCase: ScheduleReminderUseCase
) : ViewModel() {

    fun setReminder(enabled: Boolean) {
        scheduleReminderUseCase(
            ReminderSettings(
                enabled = enabled,
                frequency = ReminderFrequency.DAILY,
                hour = 20,
                minute = 0
            )
        )
    }
}
