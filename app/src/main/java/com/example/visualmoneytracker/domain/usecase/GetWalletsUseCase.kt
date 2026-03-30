package com.example.visualmoneytracker.domain.usecase

import com.example.visualmoneytracker.domain.model.Wallet
import com.example.visualmoneytracker.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWalletsUseCase @Inject constructor(
    private val repo: WalletRepository
) {
    operator fun invoke(): Flow<List<Wallet>> = repo.getAll()
}
