package com.example.visualmoneytracker.data.remote.cloud

import android.content.Context
import com.example.visualmoneytracker.data.local.db.AppDatabase
import com.example.visualmoneytracker.data.local.db.toDomain
import com.example.visualmoneytracker.domain.model.CloudProvider
import com.example.visualmoneytracker.domain.repository.SyncRepository
import com.example.visualmoneytracker.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SyncManifest(
    val exportedAt: String,
    val transactions: List<SyncTransactionItem>
)

@Serializable
data class SyncTransactionItem(
    val id: Long,
    val type: String,
    val amount: Double,
    val categoryId: Long,
    val walletId: Long,
    val imageFile: String,
    val timestamp: String
)

@Singleton
class BoxSyncRepository @Inject constructor(
    private val boxSyncManager: BoxSyncManager,
    private val transactionRepository: TransactionRepository,
    private val context: Context
) : SyncRepository {

    override suspend fun authenticate(provider: CloudProvider): Result<Unit> {
        // Authentication is handled via OAuth flow in UI — this is a no-op here
        return Result.success(Unit)
    }

    override suspend fun syncToCloud(provider: CloudProvider): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (!boxSyncManager.isAuthenticated) {
                return@withContext Result.failure(Exception("Not authenticated with Box"))
            }
            try {
                val transactions = transactionRepository.getAllTransactions()
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

                val manifest = SyncManifest(
                    exportedAt = timestamp,
                    transactions = transactions.map { tx ->
                        SyncTransactionItem(
                            id = tx.id,
                            type = tx.type.name,
                            amount = tx.amount,
                            categoryId = tx.categoryId,
                            walletId = tx.walletId,
                            imageFile = File(tx.imagePath).name,
                            timestamp = tx.timestamp.toString()
                        )
                    }
                )

                // Create zip with manifest + images
                val zipFile = File(context.cacheDir, "vmt_backup_$timestamp.zip")
                ZipOutputStream(zipFile.outputStream()).use { zip ->
                    // Add manifest
                    zip.putNextEntry(ZipEntry("manifest.json"))
                    zip.write(Json.encodeToString(manifest).toByteArray())
                    zip.closeEntry()

                    // Add images
                    transactions.forEach { tx ->
                        val imgFile = File(tx.imagePath)
                        if (imgFile.exists()) {
                            zip.putNextEntry(ZipEntry("images/${imgFile.name}"))
                            imgFile.inputStream().use { it.copyTo(zip) }
                            zip.closeEntry()
                        }
                    }
                }

                // Upload to Box
                val uploadResult = boxSyncManager.uploadFile(zipFile, zipFile.name)
                zipFile.delete()

                if (uploadResult.isFailure) return@withContext Result.failure(uploadResult.exceptionOrNull() ?: Exception("Upload failed"))

                // Apply retention policy: keep last 5 backups
                applyRetentionPolicy(keepCount = 5)

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private suspend fun applyRetentionPolicy(keepCount: Int) {
        val files = boxSyncManager.listFiles().getOrNull() ?: return
        val sorted = files.sortedByDescending { it.createdAt }
        sorted.drop(keepCount).forEach { boxSyncManager.deleteFile(it.id) }
    }
}
