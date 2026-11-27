package com.eduardo.alrededor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PlaceEntity::class, SummaryEntity::class, BookmarkEntity::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun summaryDao(): SummaryDao
    abstract fun bookmarkDao(): BookmarkDao
}
