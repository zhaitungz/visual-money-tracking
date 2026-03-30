package com.example.visualmoneytracker.di

import android.content.Context
import com.example.visualmoneytracker.data.local.db.CategoryRepositoryImpl
import com.example.visualmoneytracker.data.local.db.TransactionRepositoryImpl
import com.example.visualmoneytracker.data.local.db.WalletRepositoryImpl
import com.example.visualmoneytracker.data.remote.cloud.BoxSyncRepository
import com.example.visualmoneytracker.domain.repository.CategoryRepository
import com.example.visualmoneytracker.domain.repository.SyncRepository
import com.example.visualmoneytracker.domain.repository.TransactionRepository
import com.example.visualmoneytracker.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: BoxSyncRepository): SyncRepository
}
