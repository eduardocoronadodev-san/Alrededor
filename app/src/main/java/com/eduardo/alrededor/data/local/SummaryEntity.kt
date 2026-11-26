package com.eduardo.alrededor.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "resumenes",
    foreignKeys = [
        ForeignKey(
            entity = PlaceEntity::class,
            parentColumns = ["id_pagina"],
            childColumns = ["id_pagina"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SummaryEntity(
    @PrimaryKey @ColumnInfo(name = "id_pagina") val placeId: Long,
    @ColumnInfo(name = "extracto") val extract: String?,
    @ColumnInfo(name = "fecha_actualizacion") val updatedAt: Long
)
