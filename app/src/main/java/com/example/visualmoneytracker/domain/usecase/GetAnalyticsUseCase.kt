package com.example.visualmoneytracker.domain.usecase

import com.example.visualmoneytracker.domain.model.CategoryBreakdown
import com.example.visualmoneytracker.domain.model.TransactionType
import com.example.visualmoneytracker.domain.repository.CategoryRepository
import com.example.visualmoneytracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetAnalyticsUseCase @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val categoryRepo: CategoryRepository
) {
    suspend operator fun invoke(
        year: Int,
        month: Int,
        type: TransactionType,
        walletId: Long? = null
    ): List<CategoryBreakdown> {
        val transactions = if (walletId != null) {
            transactionRepo.getTransactionsByMonthAndWallet(year, month, walletId).first()
        } else {
            transactionRepo.getTransactionsByMonth(year, month).first()
        }

        val filtered = transactions.filter { it.type == type }
        val grouped = filtered.groupBy { it.categoryId }
        val total = filtered.sumOf { it.amount }

        if (total == 0.0) return emptyList()

        return grouped.mapNotNull { (categoryId, txList) ->
            val category = categoryRepo.getById(categoryId) ?: return@mapNotNull null
            val amount = txList.sumOf { it.amount }
            val percentage = ((amount / total) * 100).toFloat()
            CategoryBreakdown(category = category, amount = amount, percentage = percentage)
        }.sortedByDescending { it.amount }
    }
}
