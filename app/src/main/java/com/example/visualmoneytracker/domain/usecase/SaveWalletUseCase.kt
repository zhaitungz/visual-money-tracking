package com.example.visualmoneytracker.domain.usecase

import com.example.visualmoneytracker.domain.model.Wallet
import com.example.visualmoneytracker.domain.repository.WalletRepository
import javax.inject.Inject

class SaveWalletUseCase @Inject constructor(
    private val repo: WalletRepository
) {
    suspend operator fun invoke(wallet: Wallet): Result<Long> {
        if (wallet.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Wallet name must not be blank"))
        }
        return try {
            if (wallet.id == 0L) {
                val id = repo.insert(wallet)
                Result.success(id)
            } else {
                repo.update(wallet)
                Result.success(wallet.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
