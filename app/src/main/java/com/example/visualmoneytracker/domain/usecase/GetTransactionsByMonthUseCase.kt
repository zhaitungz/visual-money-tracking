package com.example.visualmoneytracker.domain.usecase

import com.example.visualmoneytracker.domain.model.Transaction
import com.example.visualmoneytracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsByMonthUseCase @Inject constructor(
    private val repo: TransactionRepository
) {
    operator fun invoke(year: Int, month: Int, walletId: Long? = null): Flow<List<Transaction>> {
        return if (walletId != null) {
            repo.getTransactionsByMonthAndWallet(year, month, walletId)
        } else {
            repo.getTransactionsByMonth(year, month)
        }
    }
}
