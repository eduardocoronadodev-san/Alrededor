package com.eduardo.alrededor.domain.repository

import com.eduardo.alrededor.domain.model.SpeechState
import kotlinx.coroutines.flow.StateFlow

/** Contrato de la lectura en voz alta. El dominio no conoce TextToSpeech. */
interface SpeechController {
    val state: StateFlow<SpeechState>
    fun speak(text: String)
    fun stop()
    /** Libera los recursos. Debe llamarse al terminar de usarlo. */
    fun release()
}
