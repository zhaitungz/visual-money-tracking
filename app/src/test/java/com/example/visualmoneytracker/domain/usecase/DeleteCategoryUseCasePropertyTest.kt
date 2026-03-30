package com.example.visualmoneytracker.domain.usecase

// Feature: visual-money-tracker, Property 12: Cascade delete reassigns all transactions to Khác

import com.example.visualmoneytracker.domain.model.Category
import com.example.visualmoneytracker.domain.model.Transaction
import com.example.visualmoneytracker.domain.model.TransactionType
import com.example.visualmoneytracker.domain.repository.CategoryRepository
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime

/**
 * Property 12: Cascade delete reassigns all transactions to "Khác"
 * Validates: Requirements 2.6.3
 */
class DeleteCategoryUseCasePropertyTest : FreeSpec({

    val kharCategory = Category(id = 99L, name = "Khác", isPreset = true, icon = null)

    val arbTransaction = Arb.bind(
        Arb.long(1L, 1000L),
        Arb.double(1.0, 100000.0),
        Arb.enum<TransactionType>(),
        Arb.long(1L, 10L)
    ) { id, amount, type, walletId ->
        Transaction(
            id = id,
            type = type,
            amount = amount,
            categoryId = 42L, // custom category
            walletId = walletId,
            imagePath = "path_$id.webp",
            timestamp = LocalDateTime.now()
        )
    }

    "Cascade delete reassigns all transactions to Khác" {
        checkAll(100, Arb.list(arbTransaction, 1..30)) { transactions ->
            val customCategory = Category(id = 42L, name = "Custom", isPreset = false, icon = null)
            val repo = mockk<CategoryRepository>()

            coEvery { repo.getAll() } returns flowOf(listOf(customCategory, kharCategory))
            coEvery { repo.reassignToFallback(any(), any()) } just Runs
            coEvery { repo.delete(any()) } just Runs

            val useCase = DeleteCategoryUseCase(repo)
            val result = useCase(42L)

            result.isSuccess shouldBe true
            coVerify(exactly = 1) { repo.reassignToFallback(42L, 99L) }
            coVerify(exactly = 1) { repo.delete(42L) }
        }
    }
})
