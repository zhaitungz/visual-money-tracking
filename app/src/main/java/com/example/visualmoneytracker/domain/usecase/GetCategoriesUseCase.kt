package com.example.visualmoneytracker.domain.usecase

import com.example.visualmoneytracker.domain.model.Category
import com.example.visualmoneytracker.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repo: CategoryRepository
) {
    operator fun invoke(): Flow<List<Category>> = repo.getAll()
}
