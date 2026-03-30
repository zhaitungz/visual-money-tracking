package com.example.visualmoneytracker.domain.model

data class Category(
    val id: Long,
    val name: String,
    val isPreset: Boolean,
    val icon: String?
)
