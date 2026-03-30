package com.example.visualmoneytracker.domain.repository

import com.example.visualmoneytracker.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAll(): Flow<List<Category>>
    suspend fun getById(id: Long): Category?
    suspend fun insert(category: Category): Long
    suspend fun update(category: Category)
    suspend fun delete(id: Long)
    suspend fun reassignToFallback(fromCategoryId: Long, fallbackId: Long)
    suspend fun seedPresets()
}
