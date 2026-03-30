package com.example.visualmoneytracker.data.local.db

import com.example.visualmoneytracker.domain.model.Category
import com.example.visualmoneytracker.domain.model.PRESET_CATEGORIES
import com.example.visualmoneytracker.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) : CategoryRepository {

    override fun getAll(): Flow<List<Category>> =
        categoryDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Category? =
        categoryDao.getById(id)?.toDomain()

    override suspend fun insert(category: Category): Long =
        categoryDao.insert(category.toEntity())

    override suspend fun update(category: Category) =
        categoryDao.update(category.toEntity())

    override suspend fun delete(id: Long) =
        categoryDao.delete(id)

    override suspend fun reassignToFallback(fromCategoryId: Long, fallbackId: Long) =
        transactionDao.reassignCategory(fromCategoryId, fallbackId)

    override suspend fun seedPresets() {
        val existing = categoryDao.getAll().first()
        if (existing.isEmpty()) {
            PRESET_CATEGORIES.forEach { name ->
                categoryDao.insert(
                    CategoryEntity(
                        id = 0,
                        name = name,
                        isPreset = true,
                        icon = null
                    )
                )
            }
        }
    }
}
