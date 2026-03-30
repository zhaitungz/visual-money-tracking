package com.example.visualmoneytracker.domain.repository

import com.example.visualmoneytracker.domain.model.Wallet
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun getAll(): Flow<List<Wallet>>
    suspend fun getById(id: Long): Wallet?
    suspend fun insert(wallet: Wallet): Long
    suspend fun update(wallet: Wallet)
    suspend fun delete(id: Long)
    suspend fun reassignTransactions(fromWalletId: Long, toWalletId: Long)
}
