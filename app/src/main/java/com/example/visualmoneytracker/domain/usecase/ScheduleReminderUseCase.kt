package com.example.visualmoneytracker.domain.usecase

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.visualmoneytracker.data.worker.ReminderFrequency
import com.example.visualmoneytracker.data.worker.ReminderSettings
import com.example.visualmoneytracker.data.worker.ReminderWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val REMINDER_WORK_TAG = "reminder_work"

class ScheduleReminderUseCase @Inject constructor(
    private val workManager: WorkManager
) {
    operator fun invoke(settings: ReminderSettings): Result<Unit> {
        return try {
            if (settings.enabled) {
                val intervalHours = when (settings.frequency) {
                    ReminderFrequency.DAILY -> 24L
                    ReminderFrequency.WEEKLY -> 7 * 24L
                    ReminderFrequency.MONTHLY -> 30 * 24L
                }
                val workRequest: PeriodicWorkRequest =
                    PeriodicWorkRequestBuilder<ReminderWorker>(intervalHours, TimeUnit.HOURS)
                        .addTag(REMINDER_WORK_TAG)
                        .build()
                workManager.enqueueUniquePeriodicWork(
                    REMINDER_WORK_TAG,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
            } else {
                workManager.cancelAllWorkByTag(REMINDER_WORK_TAG)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
