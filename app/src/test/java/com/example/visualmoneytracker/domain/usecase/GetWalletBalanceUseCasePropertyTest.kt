package com.example.visualmoneytracker.domain.usecase

// Feature: visual-money-tracker, Property 14: Wallet balance calculation correctness

import com.example.visualmoneytracker.domain.model.Transaction
import com.example.visualmoneytracker.domain.model.TransactionType
import com.example.visualmoneytracker.domain.model.Wallet
import com.example.visualmoneytracker.domain.repository.TransactionRepository
import com.example.visualmoneytracker.domain.repository.WalletRepository
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import io.mockk.*
import java.time.LocalDateTime
import kotlin.math.abs

/**
 * Property 14: Wallet balance calculation correctness
 * Validates: Requirements 2.8.2
 */
class GetWalletBalanceUseCasePropertyTest : FreeSpec({

    val walletId = 1L

    val arbTransaction = Arb.bind(
        Arb.long(1L, 10000L),
        Arb.double(0.01, 999999.0),
        Arb.enum<TransactionType>()
    ) { id, amount, type ->
        Transaction(
            id = id,
            type = type,
            amount = amount,
            categoryId = 1L,
            walletId = walletId,
            imagePath = "img_$id.webp",
            timestamp = LocalDateTime.now()
        )
    }

    "Wallet balance calculation correctness" {
        checkAll(100, Arb.double(0.0, 1_000_000.0), Arb.list(arbTransaction, 0..50)) { openingBalance, transactions ->
            val walletRepo = mockk<WalletRepository>()
            val transactionRepo = mockk<TransactionRepository>()

            coEvery { walletRepo.getById(walletId) } returns Wallet(
                id = walletId,
                name = "Test Wallet",
                openingBalance = openingBalance,
                createdAt = LocalDateTime.now()
            )
            coEvery { transactionRepo.getTransactionsByWallet(walletId) } returns transactions

            val useCase = GetWalletBalanceUseCase(walletRepo, transactionRepo)
            val result = useCase(walletId)

            result.isSuccess shouldBe true
            val actual = result.getOrThrow()
            val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val expected = openingBalance + income - expense

            abs(expected - actual) < 0.001 shouldBe true
        }
    }
})
