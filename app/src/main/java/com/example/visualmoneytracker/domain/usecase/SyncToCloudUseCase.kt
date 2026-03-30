package com.example.visualmoneytracker.domain.usecase

import com.example.visualmoneytracker.domain.model.CloudProvider
import com.example.visualmoneytracker.domain.repository.SyncRepository
import javax.inject.Inject

class SyncToCloudUseCase @Inject constructor(
    private val syncRepo: SyncRepository
) {
    suspend operator fun invoke(provider: CloudProvider): Result<Unit> {
        return syncRepo.syncToCloud(provider)
    }
}
