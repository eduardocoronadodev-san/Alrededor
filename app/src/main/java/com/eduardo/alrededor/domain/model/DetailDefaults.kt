package com.eduardo.alrededor.domain.model

/** Reglas de negocio de la pantalla de detalle (valores iniciales ajustables). */
object DetailDefaults {
    /** Pasado este tiempo, el resumen guardado se considera viejo y se vuelve a pedir. */
    const val SUMMARY_MAX_AGE_MS = 30L * 24 * 60 * 60 * 1000   // 30 días
    const val SPEECH_SENTENCES = 3
    const val SPEECH_MAX_CHARS = 600
}
