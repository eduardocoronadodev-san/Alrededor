package com.eduardo.alrededor.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lugares",
    indices = [
        Index(value = ["latitud", "longitud"], name = "idx_lugares_ubicacion"),
        Index(value = ["fecha_actualizacion"], name = "idx_lugares_antiguedad")
    ]
)
data class PlaceEntity(
    @PrimaryKey @ColumnInfo(name = "id_pagina") val id: Long,
    @ColumnInfo(name = "titulo") val title: String,
    @ColumnInfo(name = "latitud") val lat: Double,
    @ColumnInfo(name = "longitud") val lon: Double,
    @ColumnInfo(name = "descripcion") val description: String?,
    @ColumnInfo(name = "url_miniatura") val thumbnailUrl: String?,
    @ColumnInfo(name = "url_articulo") val articleUrl: String,
    @ColumnInfo(name = "es_relevante") val isRelevant: Boolean,
    @ColumnInfo(name = "fecha_actualizacion") val updatedAt: Long
)
