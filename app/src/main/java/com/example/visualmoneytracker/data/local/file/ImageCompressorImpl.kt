package com.example.visualmoneytracker.data.local.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

class ImageCompressorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageCompressor {

    override suspend fun compressAndSave(sourceUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return@withContext Result.failure(IllegalArgumentException("Cannot open URI: $sourceUri"))

            val original = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (original == null) {
                return@withContext Result.failure(IllegalArgumentException("Cannot decode image from URI"))
            }

            val resized = if (original.width > 1440) {
                val scale = 1440f / original.width
                val newHeight = (original.height * scale).toInt()
                Bitmap.createScaledBitmap(original, 1440, newHeight, true).also {
                    if (it !== original) original.recycle()
                }
            } else {
                original
            }

            val imagesDir = File(context.filesDir, "images").also { it.mkdirs() }
            val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}.webp"
            val outputFile = File(imagesDir, fileName)

            FileOutputStream(outputFile).use { out ->
                @Suppress("DEPRECATION")
                resized.compress(Bitmap.CompressFormat.WEBP, 85, out)
            }

            if (resized !== original) resized.recycle()

            Result.success(outputFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
