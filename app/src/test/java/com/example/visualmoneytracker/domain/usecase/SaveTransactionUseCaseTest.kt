package com.example.visualmoneytracker.domain.usecase

import android.net.Uri
import com.example.visualmoneytracker.data.local.file.ImageCompressor
import com.example.visualmoneytracker.domain.model.TransactionType
import com.example.visualmoneytracker.domain.repository.TransactionRepository
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*

// Validates: Requirements 2.1.2, 2.5.1
class SaveTransactionUseCaseTest : FreeSpec({

    val repo = mockk<TransactionRepository>()
    val imageCompressor = mockk<ImageCompressor>()
    val useCase = SaveTransactionUseCase(repo, imageCompressor)
    val mockUri = mockk<Uri>()

    afterEach {
        clearMocks(repo, imageCompressor)
    }

    "save successfully returns valid ID" {
        coEvery { imageCompressor.compressAndSave(mockUri) } returns Result.success("/path/to/image.webp")
        coEvery { repo.saveTransaction(any()) } returns 42L

        val result = useCase(
            rawImageUri = mockUri,
            amount = 100.0,
            type = TransactionType.EXPENSE,
            categoryId = 1L,
            walletId = 1L
        )

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe 42L
        coVerify(exactly = 1) { repo.saveTransaction(any()) }
    }

    "compression failure returns Result.failure and does not call repo.saveTransaction" {
        coEvery { imageCompressor.compressAndSave(mockUri) } returns Result.failure(RuntimeException("OOM"))

        val result = useCase(
            rawImageUri = mockUri,
            amount = 100.0,
            type = TransactionType.EXPENSE,
            categoryId = 1L,
            walletId = 1L
        )

        result.isFailure shouldBe true
        coVerify(exactly = 0) { repo.saveTransaction(any()) }
    }

    "amount = 0 returns Result.failure without calling imageCompressor or repo" {
        val result = useCase(
            rawImageUri = mockUri,
            amount = 0.0,
            type = TransactionType.EXPENSE,
            categoryId = 1L,
            walletId = 1L
        )

        result.isFailure shouldBe true
        coVerify(exactly = 0) { imageCompressor.compressAndSave(any()) }
        coVerify(exactly = 0) { repo.saveTransaction(any()) }
    }
})
