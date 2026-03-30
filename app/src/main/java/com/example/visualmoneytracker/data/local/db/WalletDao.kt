package com.example.visualmoneytracker.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets ORDER BY createdAt ASC")
    fun getAll(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE id = :id")
    suspend fun getById(id: Long): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WalletEntity): Long

    @Update
    suspend fun update(entity: WalletEntity)

    @Query("DELETE FROM wallets WHERE id = :id")
    suspend fun delete(id: Long)
}
