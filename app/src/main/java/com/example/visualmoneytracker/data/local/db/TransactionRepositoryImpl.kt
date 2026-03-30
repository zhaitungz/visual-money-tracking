package com.example.visualmoneytracker.data.local.db

import com.example.visualmoneytracker.domain.model.Transaction
import com.example.visualmoneytracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getTransactionsByMonth(year: Int, month: Int): Flow<List<Transaction>> =
        dao.getByMonth(year.toString(), month.toString().padStart(2, '0'))
            .map { list -> list.map { it.toDomain() } }

    override fun getTransactionsByMonthAndWallet(year: Int, month: Int, walletId: Long): Flow<List<Transaction>> =
        dao.getByMonthAndWallet(year.toString(), month.toString().padStart(2, '0'), walletId)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun saveTransaction(transaction: Transaction): Long =
        dao.insert(transaction.toEntity())

    override suspend fun deleteTransaction(id: Long) {
        dao.delete(
            TransactionEntity(
                id = id,
                type = "",
                amount = 0.0,
                categoryId = 0,
                walletId = 0,
                imagePath = "",
                timestamp = 0
            )
        )
    }

    override suspend fun getAllTransactions(): List<Transaction> =
        dao.getAll().map { it.toDomain() }

    override suspend fun getTransactionsByWallet(walletId: Long): List<Transaction> =
        dao.getByWallet(walletId).map { it.toDomain() }

    override suspend fun reassignWallet(fromWalletId: Long, toWalletId: Long) =
        dao.reassignWallet(fromWalletId, toWalletId)

    override suspend fun deleteTransactionsByWallet(walletId: Long) =
        dao.deleteByWallet(walletId)
}
