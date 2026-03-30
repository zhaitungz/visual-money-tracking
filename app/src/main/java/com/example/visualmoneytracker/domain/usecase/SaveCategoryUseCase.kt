package com.example.visualmoneytracker.domain.usecase

import com.example.visualmoneytracker.domain.model.Category
import com.example.visualmoneytracker.domain.repository.CategoryRepository
import javax.inject.Inject

class SaveCategoryUseCase @Inject constructor(
    private val repo: CategoryRepository
) {
    suspend operator fun invoke(category: Category): Result<Long> {
        if (category.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Category name must not be blank"))
        }
        return try {
            if (category.id == 0L) {
                val id = repo.insert(category)
                Result.success(id)
            } else {
                repo.update(category)
                Result.success(category.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
