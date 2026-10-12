package pl.literusie.domain.learning

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.literusie.domain.model.Word
import pl.literusie.domain.typing.TypingEngine
import pl.literusie.domain.typing.TypingResult
import pl.literusie.domain.typing.TypingState

data class SessionState(
    val typing: TypingState?,
    val index: Int,
    val total: Int,
    /** Runda skończona – wszystkie słowa wpisane. */
    val roundFinished: Boolean,
    /** Po wpisaniu ostatniej litery klawisze są blokowane aż do przejścia do następnego słowa. */
    val inputLocked: Boolean,
)

/** Jedna runda nauki. Zapisuje postęp po każdym ukończonym słowie, więc zamknięcie aplikacji niczego nie gubi. */
class LearningSession(
    private val words: List<Word>,
    private val store: ProgressStore,
    private val clock: Clock = Clock.System,
    initialProgress: Map<String, WordProgress> = emptyMap(),
) {
    private val progress = initialProgress.toMutableMap()
    private var index = 0
    private val engine = TypingEngine(words.firstOrNull() ?: EMPTY)

    private val _state = MutableStateFlow(snapshot())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    val currentWord: Word? get() = words.getOrNull(index)

    fun onLetter(c: Char): TypingResult {
        if (_state.value.roundFinished || _state.value.inputLocked) return TypingResult.Ignored
        val r = engine.type(c)
        publish()
        return r
    }

    fun onBackspace(): TypingResult {
        if (_state.value.roundFinished || _state.value.inputLocked) return TypingResult.Ignored
        val r = engine.backspace()
        publish()
        return r
    }

    /** Zapisuje ukończone słowo i przechodzi do następnego (lub kończy rundę). Wołane po krótkiej pauzie sukcesu. */
    suspend fun completeWordAndAdvance() {
        val s = engine.state
        val word = currentWord ?: return
        if (!s.finished) return
        val updated = Mastery.afterCompletion(progress[word.id], word.id, s.totalErrors, clock.now())
        progress[word.id] = updated
        // Awaria zapisu nie może przerwać nauki – postęp zostaje w pamięci bieżącej rundy.
        try { store.save(updated) } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) { /* ignorujemy */ }
        if (index + 1 < words.size) {
            index++
            engine.reset(words[index])
        } else {
            index = words.size
        }
        publish()
    }

    private fun publish() { _state.value = snapshot() }

    private fun snapshot(): SessionState {
        val finishedRound = index >= words.size
        val t = if (finishedRound) null else engine.state
        return SessionState(t, minOf(index, words.size), words.size, finishedRound, inputLocked = t?.finished == true)
    }

    private companion object {
        val EMPTY = Word("", "", "", null, 1, "")
    }
}
