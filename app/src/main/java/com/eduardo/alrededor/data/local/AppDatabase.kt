package com.eduardo.alrededor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PlaceEntity::class, SummaryEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun summaryDao(): SummaryDao
}
