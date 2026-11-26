package com.eduardo.alrededor.di

import android.content.Context
import androidx.room.Room
import com.eduardo.alrededor.data.local.AppDatabase
import com.eduardo.alrededor.data.local.PlaceDao
import com.eduardo.alrededor.data.local.SummaryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "guia.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun providePlaceDao(database: AppDatabase): PlaceDao = database.placeDao()

    @Provides
    fun provideSummaryDao(database: AppDatabase): SummaryDao = database.summaryDao()
}
