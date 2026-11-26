package com.eduardo.alrededor.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface PlaceDao {

    @Upsert
    suspend fun upsertAll(places: List<PlaceEntity>)

    @Query(
        """
        SELECT * FROM lugares
        WHERE latitud BETWEEN :minLat AND :maxLat
          AND longitud BETWEEN :minLon AND :maxLon
        """
    )
    suspend fun findInBounds(
        minLat: Double, maxLat: Double, minLon: Double, maxLon: Double
    ): List<PlaceEntity>

    @Query("SELECT * FROM lugares WHERE id_pagina = :id")
    suspend fun findById(id: Long): PlaceEntity?

    @Query("DELETE FROM lugares")
    suspend fun deleteAll()
}
