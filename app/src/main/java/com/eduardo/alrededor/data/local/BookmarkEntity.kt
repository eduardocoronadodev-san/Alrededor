package com.eduardo.alrededor.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favoritos",
    foreignKeys = [
        ForeignKey(
            entity = PlaceEntity::class,
            parentColumns = ["id_pagina"],
            childColumns = ["id_pagina"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["fecha_guardado"], name = "idx_favoritos_fecha")
    ]
)
data class BookmarkEntity(
    @PrimaryKey @ColumnInfo(name = "id_pagina") val placeId: Long,
    @ColumnInfo(name = "fecha_guardado") val savedAt: Long,
    @ColumnInfo(name = "nota_personal") val personalNote: String? = null
)
