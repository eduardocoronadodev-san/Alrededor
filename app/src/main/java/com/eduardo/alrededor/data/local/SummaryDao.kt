package com.eduardo.alrededor.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface SummaryDao {

    @Upsert
    suspend fun upsert(summary: SummaryEntity)

    @Query("SELECT * FROM resumenes WHERE id_pagina = :placeId")
    suspend fun findByPlaceId(placeId: Long): SummaryEntity?
}
