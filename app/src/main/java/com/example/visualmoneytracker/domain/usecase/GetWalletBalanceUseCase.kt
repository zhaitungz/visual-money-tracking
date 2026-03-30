package com.example.visualmoneytracker.domain.usecase

import com.example.visualmoneytracker.domain.model.TransactionType
import com.example.visualmoneytracker.domain.repository.TransactionRepository
import com.example.visualmoneytracker.domain.repository.WalletRepository
import javax.inject.Inject

class GetWalletBalanceUseCase @Inject constructor(
    private val walletRepo: WalletRepository,
    private val transactionRepo: TransactionRepository
) {
    suspend operator fun invoke(walletId: Long): Result<Double> {
        return try {
            val wallet = walletRepo.getById(walletId)
                ?: return Result.failure(IllegalArgumentException("Wallet not found: $walletId"))
            val transactions = transactionRepo.getTransactionsByWallet(walletId)
            val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val balance = wallet.openingBalance + income - expense
            Result.success(balance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
