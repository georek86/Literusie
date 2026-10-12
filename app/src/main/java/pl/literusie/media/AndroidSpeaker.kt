package pl.literusie.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/** Polski TextToSpeech. Prośby zgłoszone przed końcem inicjalizacji nie przepadają – ostatnia jest odtwarzana po starcie. */
class AndroidSpeaker(context: Context) : Speaker, TextToSpeech.OnInitListener {
    private val _status = MutableStateFlow(SpeechStatus.STARTING)
    override val status: StateFlow<SpeechStatus> = _status.asStateFlow()

    private var tts: TextToSpeech? = null
    private var pending: String? = null

    init {
        tts = try { TextToSpeech(context.applicationContext, this) } catch (e: Exception) {
            Log.e(TAG, "TTS init", e); _status.value = SpeechStatus.UNAVAILABLE; null
        }
    }

    override fun onInit(code: Int) {
        val engine = tts
        if (code != TextToSpeech.SUCCESS || engine == null) {
            _status.value = SpeechStatus.UNAVAILABLE
            return
        }
        val lang = try { engine.setLanguage(Locale("pl", "PL")) } catch (e: Exception) { TextToSpeech.LANG_NOT_SUPPORTED }
        if (lang == TextToSpeech.LANG_MISSING_DATA || lang == TextToSpeech.LANG_NOT_SUPPORTED) {
            _status.value = SpeechStatus.NO_POLISH_VOICE
            return
        }
        engine.setSpeechRate(0.8f)
        engine.setAudioAttributes(
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).setUsage(AudioAttributes.USAGE_MEDIA).build()
        )
        _status.value = SpeechStatus.READY
        pending?.let { pending = null; speak(it) }
    }

    override fun speak(text: String) {
        when (_status.value) {
            SpeechStatus.STARTING -> pending = text
            SpeechStatus.READY -> try {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "word") // QUEUE_FLUSH: przerywa poprzednią wypowiedź
            } catch (e: Exception) { Log.w(TAG, "speak", e) }
            else -> Unit
        }
    }

    override fun stop() { try { tts?.stop() } catch (e: Exception) { Log.w(TAG, "stop", e) } }

    fun shutdown() {
        try { tts?.stop(); tts?.shutdown() } catch (e: Exception) { Log.w(TAG, "shutdown", e) }
        tts = null
    }

    private companion object { const val TAG = "AndroidSpeaker" }
}

/** Ciche, krótkie sygnały (ToneGenerator); gdy ustawienie wyłączone – cisza. */
class AndroidSoundEffects(private val settings: SettingsStore) : SoundEffects {
    private val tone: ToneGenerator? = try { ToneGenerator(AudioManager.STREAM_MUSIC, 25) } catch (e: Exception) { null }

    override fun success() { if (settings.soundEffects) tone?.startTone(ToneGenerator.TONE_PROP_ACK, 120) }
    override fun mistake() { if (settings.soundEffects) tone?.startTone(ToneGenerator.TONE_PROP_NACK, 80) }
    fun release() { tone?.release() }
}
