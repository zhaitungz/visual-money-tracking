package com.example.visualmoneytracker.domain.repository

import com.example.visualmoneytracker.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactionsByMonth(year: Int, month: Int): Flow<List<Transaction>>
    fun getTransactionsByMonthAndWallet(year: Int, month: Int, walletId: Long): Flow<List<Transaction>>
    suspend fun saveTransaction(transaction: Transaction): Long
    suspend fun deleteTransaction(id: Long)
    suspend fun getAllTransactions(): List<Transaction>
    suspend fun getTransactionsByWallet(walletId: Long): List<Transaction>
    suspend fun reassignWallet(fromWalletId: Long, toWalletId: Long)
    suspend fun deleteTransactionsByWallet(walletId: Long)
}
