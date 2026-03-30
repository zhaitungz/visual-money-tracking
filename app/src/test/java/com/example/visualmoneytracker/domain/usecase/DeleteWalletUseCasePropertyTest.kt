package com.example.visualmoneytracker.domain.usecase

// Feature: visual-money-tracker, Property 15: No orphan transactions after wallet delete

import com.example.visualmoneytracker.domain.model.Transaction
import com.example.visualmoneytracker.domain.model.TransactionType
import com.example.visualmoneytracker.domain.repository.TransactionRepository
import com.example.visualmoneytracker.domain.repository.WalletRepository
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import io.mockk.*
import java.time.LocalDateTime

/**
 * Property 15: No orphan transactions after wallet delete
 * Validates: Requirements 2.8.5
 */
class DeleteWalletUseCasePropertyTest : FreeSpec({

    val walletId = 1L
    val reassignTargetId = 2L

    val arbTransaction = Arb.bind(
        Arb.long(1L, 1000L),
        Arb.double(0.01, 999999.0),
        Arb.enum<TransactionType>(),
        Arb.long(1L, 10L)
    ) { id, amount, type, categoryId ->
        Transaction(
            id = id,
            type = type,
            amount = amount,
            categoryId = categoryId,
            walletId = walletId,
            imagePath = "img_$id.webp",
            timestamp = LocalDateTime.now()
        )
    }

    "No orphan transactions after wallet delete" {
        checkAll(100, Arb.list(arbTransaction, 1..30), Arb.boolean()) { _, shouldReassign ->
            val walletRepo = mockk<WalletRepository>()
            val transactionRepo = mockk<TransactionRepository>()

            coEvery { walletRepo.delete(any()) } just Runs
            coEvery { transactionRepo.reassignWallet(any(), any()) } just Runs
            coEvery { transactionRepo.deleteTransactionsByWallet(any()) } just Runs

            val useCase = DeleteWalletUseCase(walletRepo, transactionRepo)
            val reassignToWalletId = if (shouldReassign) reassignTargetId else null
            val result = useCase(walletId, reassignToWalletId)

            result.isSuccess shouldBe true
            coVerify(exactly = 1) { walletRepo.delete(walletId) }

            if (shouldReassign) {
                coVerify(exactly = 1) { transactionRepo.reassignWallet(walletId, reassignTargetId) }
                coVerify(exactly = 0) { transactionRepo.deleteTransactionsByWallet(any()) }
            } else {
                coVerify(exactly = 1) { transactionRepo.deleteTransactionsByWallet(walletId) }
                coVerify(exactly = 0) { transactionRepo.reassignWallet(any(), any()) }
            }
        }
    }
})
