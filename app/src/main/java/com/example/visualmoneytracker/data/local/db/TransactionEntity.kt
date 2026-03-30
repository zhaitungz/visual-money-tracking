package com.example.visualmoneytracker.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,           // "INCOME" | "EXPENSE"
    val amount: Double,
    val categoryId: Long,
    val walletId: Long,
    val imagePath: String,
    val timestamp: Long         // epoch millis
)
