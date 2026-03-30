package com.example.visualmoneytracker.di

import android.content.Context
import androidx.work.WorkManager
import com.example.visualmoneytracker.data.local.file.ImageCompressor
import com.example.visualmoneytracker.data.local.file.ImageCompressorImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindImageCompressor(impl: ImageCompressorImpl): ImageCompressor

    companion object {
        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
            WorkManager.getInstance(context)
    }
}
