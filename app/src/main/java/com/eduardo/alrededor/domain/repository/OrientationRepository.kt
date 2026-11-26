package com.eduardo.alrededor.domain.repository

import kotlinx.coroutines.flow.Flow

interface OrientationRepository {
    /**
     * Hacia dónde mira el teléfono, en grados de 0 a 360 (0 = norte magnético).
     * Si el dispositivo no tiene brújula, el flujo termina sin emitir nada.
     */
    fun azimuthUpdates(): Flow<Float>
}
