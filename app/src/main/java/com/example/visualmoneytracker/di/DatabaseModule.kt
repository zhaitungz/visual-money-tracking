package com.example.visualmoneytracker.di

import android.content.Context
import androidx.room.Room
import com.example.visualmoneytracker.data.local.db.AppDatabase
import com.example.visualmoneytracker.data.local.db.CategoryDao
import com.example.visualmoneytracker.data.local.db.TransactionDao
import com.example.visualmoneytracker.data.local.db.WalletDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "visual_money_tracker.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideWalletDao(db: AppDatabase): WalletDao = db.walletDao()
}
