package com.example.visualmoneytracker.domain.usecase

import com.example.visualmoneytracker.domain.repository.TransactionRepository
import com.example.visualmoneytracker.domain.repository.WalletRepository
import javax.inject.Inject

class DeleteWalletUseCase @Inject constructor(
    private val walletRepo: WalletRepository,
    private val transactionRepo: TransactionRepository
) {
    suspend operator fun invoke(walletId: Long, reassignToWalletId: Long?): Result<Unit> {
        return try {
            if (reassignToWalletId != null) {
                transactionRepo.reassignWallet(walletId, reassignToWalletId)
            } else {
                transactionRepo.deleteTransactionsByWallet(walletId)
            }
            walletRepo.delete(walletId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
