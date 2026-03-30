package com.example.visualmoneytracker.data.local.db

import com.example.visualmoneytracker.domain.model.Wallet
import com.example.visualmoneytracker.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WalletRepositoryImpl @Inject constructor(
    private val walletDao: WalletDao,
    private val transactionDao: TransactionDao
) : WalletRepository {

    override fun getAll(): Flow<List<Wallet>> =
        walletDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Wallet? =
        walletDao.getById(id)?.toDomain()

    override suspend fun insert(wallet: Wallet): Long =
        walletDao.insert(wallet.toEntity())

    override suspend fun update(wallet: Wallet) =
        walletDao.update(wallet.toEntity())

    override suspend fun delete(id: Long) =
        walletDao.delete(id)

    override suspend fun reassignTransactions(fromWalletId: Long, toWalletId: Long) =
        transactionDao.reassignWallet(fromWalletId, toWalletId)
}
