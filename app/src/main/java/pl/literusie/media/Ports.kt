package pl.literusie.media

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class SpeechStatus {
    /** Silnik się uruchamia. */
    STARTING,
    READY,
    /** Silnik działa, ale nie ma polskiego głosu – opiekun musi go doinstalować. */
    NO_POLISH_VOICE,
    /** Brak silnika TTS lub błąd inicjalizacji. */
    UNAVAILABLE,
}

/** Wymowa słów. Nowa wypowiedź zawsze przerywa poprzednią. */
interface Speaker {
    val status: StateFlow<SpeechStatus>
    fun speak(text: String)
    fun stop()
}

/** Dyskretne sygnały – domyślnie wyłączone w ustawieniach. */
interface SoundEffects {
    fun success()
    fun mistake()
}

object NoSoundEffects : SoundEffects {
    override fun success() = Unit
    override fun mistake() = Unit
}

/** Ustawienia opiekuna zapisywane lokalnie. */
interface SettingsStore {
    var soundEffects: Boolean
    var hints: Boolean
}

class InMemorySettings(override var soundEffects: Boolean = false, override var hints: Boolean = true) : SettingsStore

/** Atrapa na potrzeby testów i podglądu. */
class FakeSpeaker(initial: SpeechStatus = SpeechStatus.READY) : Speaker {
    override val status = MutableStateFlow(initial)
    val spoken = mutableListOf<String>()
    override fun speak(text: String) { spoken += text }
    override fun stop() = Unit
}
