package com.example.visualmoneytracker.domain.usecase

import com.example.visualmoneytracker.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repo: TransactionRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        return try {
            repo.deleteTransaction(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
