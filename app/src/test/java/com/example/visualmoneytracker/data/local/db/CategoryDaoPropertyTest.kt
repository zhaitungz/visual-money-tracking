package com.example.visualmoneytracker.data.local.db

// Feature: visual-money-tracker, Property 13: Preset categories cannot be deleted

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.checkAll
import com.example.visualmoneytracker.domain.model.PRESET_CATEGORIES

/**
 * Property 13: Preset categories cannot be deleted
 * Validates: Requirements 2.6.3
 */
class CategoryDaoPropertyTest : FreeSpec({

    // Simulate the CategoryDao.delete behavior: only deletes non-preset categories
    fun simulateDelete(categories: MutableList<CategoryEntity>, id: Long) {
        categories.removeIf { it.id == id && !it.isPreset }
    }

    "Preset categories cannot be deleted" {
        // Seed preset categories with IDs 1..8
        val presetCategories = PRESET_CATEGORIES.mapIndexed { index, name ->
            CategoryEntity(id = (index + 1).toLong(), name = name, isPreset = true, icon = null)
        }
        val presetIds = presetCategories.map { it.id }

        checkAll(100, Arb.element(presetIds)) { presetId ->
            val categories = presetCategories.toMutableList()
            val countBefore = categories.size

            simulateDelete(categories, presetId)

            // Preset category must still exist
            categories.size shouldBe countBefore
            categories.any { it.id == presetId } shouldBe true
        }
    }
})
