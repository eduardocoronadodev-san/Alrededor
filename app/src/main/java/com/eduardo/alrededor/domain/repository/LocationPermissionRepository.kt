package com.eduardo.alrededor.domain.repository

import com.eduardo.alrededor.domain.model.LocationGrant

/**
 * Contrato que usa el dominio. La capa de datos lo implementa
 * (principio de inversión de dependencias).
 */
interface LocationPermissionRepository {
    /** Lo que el sistema permite ahora mismo. */
    fun currentGrant(): LocationGrant

    /** Si la app ya mostró alguna vez el diálogo de permiso. */
    suspend fun hasRequestedBefore(): Boolean

    /** Registra que ya se pidió el permiso. */
    suspend fun markRequested()
}