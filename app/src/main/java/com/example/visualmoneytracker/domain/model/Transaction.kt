package com.example.visualmoneytracker.domain.model

import java.time.LocalDateTime

data class Transaction(
    val id: Long,
    val type: TransactionType,
    val amount: Double,
    val categoryId: Long,
    val walletId: Long,
    val imagePath: String,
    val timestamp: LocalDateTime
)
