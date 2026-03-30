package com.example.visualmoneytracker.data.local.db

import com.example.visualmoneytracker.domain.model.Category
import com.example.visualmoneytracker.domain.model.Transaction
import com.example.visualmoneytracker.domain.model.TransactionType
import com.example.visualmoneytracker.domain.model.Wallet
import java.time.LocalDateTime

// TransactionEntity ↔ Transaction
fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    type = TransactionType.valueOf(type),
    amount = amount,
    categoryId = categoryId,
    walletId = walletId,
    imagePath = imagePath,
    timestamp = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, java.time.ZoneOffset.UTC)
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    type = type.name,
    amount = amount,
    categoryId = categoryId,
    walletId = walletId,
    imagePath = imagePath,
    timestamp = timestamp.toEpochSecond(java.time.ZoneOffset.UTC) * 1000
)

// CategoryEntity ↔ Category
fun CategoryEntity.toDomain(): Category = Category(id, name, isPreset, icon)
fun Category.toEntity(): CategoryEntity = CategoryEntity(id, name, isPreset, icon)

// WalletEntity ↔ Wallet
fun WalletEntity.toDomain(): Wallet = Wallet(
    id = id,
    name = name,
    openingBalance = openingBalance,
    createdAt = LocalDateTime.ofEpochSecond(createdAt / 1000, 0, java.time.ZoneOffset.UTC)
)

fun Wallet.toEntity(): WalletEntity = WalletEntity(
    id = id,
    name = name,
    openingBalance = openingBalance,
    createdAt = createdAt.toEpochSecond(java.time.ZoneOffset.UTC) * 1000
)
