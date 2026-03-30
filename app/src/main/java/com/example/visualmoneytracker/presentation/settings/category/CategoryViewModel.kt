package com.example.visualmoneytracker.presentation.settings.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoneytracker.domain.model.Category
import com.example.visualmoneytracker.domain.usecase.DeleteCategoryUseCase
import com.example.visualmoneytracker.domain.usecase.GetCategoriesUseCase
import com.example.visualmoneytracker.domain.usecase.SaveCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val getCategories: GetCategoriesUseCase,
    private val saveCategory: SaveCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase
) : ViewModel() {

    val categories: StateFlow<List<Category>> = getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onAddCategory(name: String) {
        viewModelScope.launch {
            saveCategory(Category(id = 0, name = name, isPreset = false, icon = null))
        }
    }

    fun onRenameCategory(id: Long, newName: String) {
        viewModelScope.launch {
            val category = categories.value.find { it.id == id } ?: return@launch
            saveCategory(category.copy(name = newName))
        }
    }

    fun onDeleteCategory(id: Long) {
        viewModelScope.launch {
            deleteCategory(id)
        }
    }
}
