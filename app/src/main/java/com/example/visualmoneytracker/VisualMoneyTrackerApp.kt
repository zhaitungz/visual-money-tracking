package com.example.visualmoneytracker

import android.app.Application
import com.example.visualmoneytracker.domain.repository.CategoryRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class VisualMoneyTrackerApp : Application() {

    @Inject
    lateinit var categoryRepository: CategoryRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            categoryRepository.seedPresets()
        }
    }
}
