package com.example.visualmoneytracker.data.local.file

import android.net.Uri

interface ImageCompressor {
    suspend fun compressAndSave(sourceUri: Uri): Result<String>
}
