package com.example.visualmoneytracker.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE strftime('%Y', datetime(timestamp/1000,'unixepoch')) = :year AND strftime('%m', datetime(timestamp/1000,'unixepoch')) = :month ORDER BY timestamp DESC")
    fun getByMonth(year: String, month: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE strftime('%Y', datetime(timestamp/1000,'unixepoch')) = :year AND strftime('%m', datetime(timestamp/1000,'unixepoch')) = :month AND walletId = :walletId ORDER BY timestamp DESC")
    fun getByMonthAndWallet(year: String, month: String, walletId: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransactionEntity): Long

    @Delete
    suspend fun delete(entity: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    suspend fun getAll(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE walletId = :walletId")
    suspend fun getByWallet(walletId: Long): List<TransactionEntity>

    @Query("UPDATE transactions SET categoryId = :fallbackId WHERE categoryId = :fromId")
    suspend fun reassignCategory(fromId: Long, fallbackId: Long)

    @Query("UPDATE transactions SET walletId = :toWalletId WHERE walletId = :fromWalletId")
    suspend fun reassignWallet(fromWalletId: Long, toWalletId: Long)

    @Query("DELETE FROM transactions WHERE walletId = :walletId")
    suspend fun deleteByWallet(walletId: Long)
}
