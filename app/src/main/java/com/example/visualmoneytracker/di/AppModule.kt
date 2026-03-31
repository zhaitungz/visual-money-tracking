package com.example.visualmoneytracker.di

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.visualmoneytracker.data.local.file.ImageCompressor
import com.example.visualmoneytracker.data.local.file.ImageCompressorImpl
import com.example.visualmoneytracker.data.remote.cloud.BoxSyncManager
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
        fun provideWorkManager(
            @ApplicationContext context: Context,
            workerFactory: HiltWorkerFactory
        ): WorkManager {
            val config = Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
            WorkManager.initialize(context, config)
            return WorkManager.getInstance(context)
        }

        @Provides
        @Singleton
        fun provideBoxSyncManager(@ApplicationContext context: Context): BoxSyncManager =
            BoxSyncManager(context)
    }
}
