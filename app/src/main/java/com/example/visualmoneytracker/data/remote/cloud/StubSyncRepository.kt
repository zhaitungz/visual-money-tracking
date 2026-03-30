package com.example.visualmoneytracker.data.remote.cloud

import com.example.visualmoneytracker.domain.model.CloudProvider
import com.example.visualmoneytracker.domain.repository.SyncRepository
import javax.inject.Inject

class StubSyncRepository @Inject constructor() : SyncRepository {
    override suspend fun syncToCloud(provider: CloudProvider): Result<Unit> = Result.success(Unit)
    override suspend fun authenticate(provider: CloudProvider): Result<Unit> = Result.success(Unit)
}
