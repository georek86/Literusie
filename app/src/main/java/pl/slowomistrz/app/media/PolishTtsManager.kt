package pl.slowomistrz.app.media

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class PolishTtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val tag = "PolishTtsManager"
    private var tts: TextToSpeech? = null

    private val _isLanguageAvailable = MutableStateFlow(false)
    val isLanguageAvailable: StateFlow<Boolean> = _isLanguageAvailable.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(tag, "Błąd inicjalizacji TTS", e)
            _errorMessage.value = "Nie udało się uruchomić silnika mowy."
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val polishLocale = Locale("pl", "PL")
            val result = tts?.setLanguage(polishLocale)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(tag, "Język polski nie jest obsługiwany przez zainstalowany silnik TTS.")
                _isLanguageAvailable.value = false
                _errorMessage.value = "Brak zainstalowanego polskiego głosu w ustawieniach Android TV."
            } else {
                Log.i(tag, "Polski TTS zainicjalizowany pomyślnie.")
                _isLanguageAvailable.value = true
                _errorMessage.value = null
                // Ustawienie spokojnego, wyraźnego tempa dla pięciolatka
                tts?.setSpeechRate(0.85f)
                tts?.setPitch(1.0f)
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    Log.w(tag, "Błąd odtwarzania mowy TTS, kod: $errorCode")
                }
            })
        } else {
            Log.e(tag, "Inicjalizacja TextToSpeech zakończona niepowodzeniem: $status")
            _isLanguageAvailable.value = false
            _errorMessage.value = "Błąd inicjalizacji odczytywania mowy."
        }
    }

    /**
     * Odczytuje słowo po polsku.
     * Zgodnie z wymaganiami: przerywa poprzednią wypowiedź, aby uniknąć nakładania się dźwięków.
     */
    fun speakWord(word: String) {
        val currentTts = tts ?: return
        if (!_isLanguageAvailable.value) {
            return
        }

        try {
            // QUEUE_FLUSH przerywa poprzednią wypowiedź natychmiast
            currentTts.stop()
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "word_${System.currentTimeMillis()}")
            }
            currentTts.speak(word, TextToSpeech.QUEUE_FLUSH, params, "word_utterance")
        } catch (e: Exception) {
            Log.e(tag, "Błąd podczas wywołania speakWord", e)
        }
    }

    fun stop() {
        try {
            tts?.stop()
            _isSpeaking.value = false
        } catch (e: Exception) {
            Log.e(tag, "Błąd podczas zatrzymywania TTS", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.e(tag, "Błąd podczas zamykania TTS", e)
        }
    }
}
