package com.example.visualmoneytracker.domain.model

import java.time.LocalDateTime

data class Wallet(
    val id: Long,
    val name: String,
    val openingBalance: Double,
    val createdAt: LocalDateTime
)
