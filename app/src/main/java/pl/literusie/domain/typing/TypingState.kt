package pl.literusie.domain.typing

import pl.literusie.domain.model.Word

/** Niezmienny stan wpisywania jednego słowa – to, co rysuje ekran. */
data class TypingState(
    val word: Word,
    val typed: Int = 0,
    val finished: Boolean = false,
    /** Błędy pod rząd od ostatniej poprawnej litery. */
    val streakErrors: Int = 0,
    /** Wszystkie błędne litery w tym słowie. */
    val totalErrors: Int = 0,
    val lastWrong: Char? = null,
) {
    val length: Int get() = word.text.length
    val expected: Char? get() = if (typed < length) word.text[typed] else null
    val showHint: Boolean get() = streakErrors >= HINT_AFTER_ERRORS && !finished

    companion object {
        const val HINT_AFTER_ERRORS = 2
    }
}
