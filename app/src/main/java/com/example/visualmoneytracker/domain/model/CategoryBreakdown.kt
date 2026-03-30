package com.example.visualmoneytracker.domain.model

data class CategoryBreakdown(
    val category: Category,
    val amount: Double,
    val percentage: Float
)
