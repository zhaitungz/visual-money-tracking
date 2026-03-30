package com.example.visualmoneytracker.domain.usecase

import android.net.Uri
import com.example.visualmoneytracker.data.local.file.ImageCompressor
import com.example.visualmoneytracker.domain.model.Transaction
import com.example.visualmoneytracker.domain.model.TransactionType
import com.example.visualmoneytracker.domain.repository.TransactionRepository
import java.time.LocalDateTime
import javax.inject.Inject

class SaveTransactionUseCase @Inject constructor(
    private val repo: TransactionRepository,
    private val imageCompressor: ImageCompressor
) {
    suspend operator fun invoke(
        rawImageUri: Uri,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        walletId: Long
    ): Result<Long> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be > 0"))
        }
        return try {
            val compressResult = imageCompressor.compressAndSave(rawImageUri)
            if (compressResult.isFailure) {
                return Result.failure(compressResult.exceptionOrNull() ?: Exception("Image compression failed"))
            }
            val compressedPath = compressResult.getOrThrow()
            val transaction = Transaction(
                id = 0,
                type = type,
                amount = amount,
                categoryId = categoryId,
                walletId = walletId,
                imagePath = compressedPath,
                timestamp = LocalDateTime.now()
            )
            val id = repo.saveTransaction(transaction)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
