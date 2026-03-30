package com.example.visualmoneytracker.domain.usecase

import com.example.visualmoneytracker.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(
    private val repo: CategoryRepository
) {
    suspend operator fun invoke(categoryId: Long): Result<Unit> {
        return try {
            val categories = repo.getAll().first()
            val kharCategory = categories.find { it.name == "Khác" }
                ?: return Result.failure(IllegalStateException("Fallback category 'Khác' not found"))
            repo.reassignToFallback(categoryId, kharCategory.id)
            repo.delete(categoryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
