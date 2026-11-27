package com.eduardo.alrededor.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Upsert
    suspend fun upsert(bookmark: BookmarkEntity)

    @Delete
    suspend fun delete(bookmark: BookmarkEntity)

    @Query("DELETE FROM favoritos WHERE id_pagina = :placeId")
    suspend fun deleteByPlaceId(placeId: Long)

    @Query("SELECT * FROM favoritos WHERE id_pagina = :placeId")
    suspend fun findByPlaceId(placeId: Long): BookmarkEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favoritos WHERE id_pagina = :placeId)")
    fun isBookmarkedFlow(placeId: Long): Flow<Boolean>

    @Query("SELECT * FROM favoritos ORDER BY fecha_guardado DESC")
    fun getAllBookmarksFlow(): Flow<List<BookmarkEntity>>

    @Query("DELETE FROM favoritos")
    suspend fun deleteAll()
}
