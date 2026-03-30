package com.example.visualmoneytracker.domain.repository

import com.example.visualmoneytracker.domain.model.CloudProvider

interface SyncRepository {
    suspend fun syncToCloud(provider: CloudProvider): Result<Unit>
    suspend fun authenticate(provider: CloudProvider): Result<Unit>
}
