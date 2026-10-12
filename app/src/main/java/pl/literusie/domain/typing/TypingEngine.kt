package pl.literusie.domain.typing

import pl.literusie.domain.model.Word
import pl.literusie.domain.text.PolishNormalizer

sealed interface TypingResult {
    data class Correct(val typed: Int, val finished: Boolean) : TypingResult
    data class Wrong(val typed: Char, val expected: Char) : TypingResult
    data object Backspaced : TypingResult
    data object Ignored : TypingResult
}

/** Sekwencyjne sprawdzanie liter: K → O → T. Błędna litera nie niszczy postępu. */
class TypingEngine(word: Word) {
    var state: TypingState = TypingState(word)
        private set

    fun reset(word: Word) {
        state = TypingState(word)
    }

    fun type(c: Char): TypingResult {
        val s = state
        if (s.finished || !c.isLetter()) return TypingResult.Ignored
        val expected = s.expected ?: return TypingResult.Ignored
        return if (PolishNormalizer.matches(c, expected)) {
            val typed = s.typed + 1
            val done = typed == s.length
            state = s.copy(typed = typed, finished = done, streakErrors = 0, lastWrong = null)
            TypingResult.Correct(typed, done)
        } else {
            state = s.copy(streakErrors = s.streakErrors + 1, totalErrors = s.totalErrors + 1, lastWrong = c.uppercaseChar())
            TypingResult.Wrong(c.uppercaseChar(), expected)
        }
    }

    fun backspace(): TypingResult {
        val s = state
        if (s.finished || s.typed == 0) return TypingResult.Ignored
        state = s.copy(typed = s.typed - 1, streakErrors = 0, lastWrong = null)
        return TypingResult.Backspaced
    }
}
