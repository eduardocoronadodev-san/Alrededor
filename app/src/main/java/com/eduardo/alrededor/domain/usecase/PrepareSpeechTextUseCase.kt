package com.eduardo.alrededor.domain.usecase

import com.eduardo.alrededor.domain.model.DetailDefaults
import javax.inject.Inject

/** Arma el texto que se lee: el título y las primeras oraciones del resumen. */
class PrepareSpeechTextUseCase @Inject constructor() {

    operator fun invoke(title: String, extract: String): String {
        val body = extract.trim()
            .split(SENTENCE_END)
            .filter { it.isNotBlank() }
            .take(DetailDefaults.SPEECH_SENTENCES)
            .joinToString(" ")
        return "$title. $body".take(DetailDefaults.SPEECH_MAX_CHARS)
    }

    private companion object {
        // Corta después de . ! ? seguidos de espacio o salto de línea
        val SENTENCE_END = Regex("(?<=[.!?])\\s+")
    }
}
