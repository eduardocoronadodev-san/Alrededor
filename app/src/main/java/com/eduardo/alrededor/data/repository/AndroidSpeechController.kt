package com.eduardo.alrededor.data.repository

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.eduardo.alrededor.domain.model.SpeechState
import com.eduardo.alrededor.domain.repository.SpeechController
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject

/** Lectura en voz alta con la voz del sistema, en español. */
class AndroidSpeechController @Inject constructor(
    @ApplicationContext context: Context
) : SpeechController {

    private val _state = MutableStateFlow(SpeechState.IDLE)
    override val state: StateFlow<SpeechState> = _state.asStateFlow()

    private var tts: TextToSpeech? = null
    private var ready = false
    private var pendingText: String? = null

    init {
        try {
            tts = TextToSpeech(context) { status -> onInit(status) }
        } catch (_: Exception) {
            _state.value = SpeechState.UNAVAILABLE
        }
    }

    private fun onInit(status: Int) {
        val engine = tts
        if (status != TextToSpeech.SUCCESS || engine == null || !selectSpanish(engine)) {
            _state.value = SpeechState.UNAVAILABLE
            return
        }
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { _state.value = SpeechState.SPEAKING }
            override fun onDone(utteranceId: String?) { _state.value = SpeechState.IDLE }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { _state.value = SpeechState.IDLE }
        })
        ready = true
        // Si el usuario pulsó "Escuchar" antes de que la voz estuviera lista
        pendingText?.let { speak(it) }
        pendingText = null
    }

    /** Intenta español de México, luego español genérico. */
    private fun selectSpanish(engine: TextToSpeech): Boolean =
        listOf(Locale("es", "MX"), Locale("es")).any { locale ->
            val result = engine.setLanguage(locale)
            result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        }

    override fun speak(text: String) {
        if (_state.value == SpeechState.UNAVAILABLE) return
        if (!ready) {
            pendingText = text
            return
        }
        _state.value = SpeechState.SPEAKING
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    override fun stop() {
        pendingText = null
        tts?.stop()
        if (_state.value == SpeechState.SPEAKING) _state.value = SpeechState.IDLE
    }

    override fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }

    private companion object {
        const val UTTERANCE_ID = "resumen"
    }
}
