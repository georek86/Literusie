package pl.slowomistrz.app.domain.engine

import pl.slowomistrz.app.domain.model.WordInputState
import pl.slowomistrz.app.domain.model.WordItem
import pl.slowomistrz.app.domain.normalizer.PolishCharNormalizer

sealed class TypingResult {
    data class CorrectChar(val newTypedCount: Int, val isFinished: Boolean) : TypingResult()
    data class IncorrectChar(val inputChar: Char, val expectedChar: Char) : TypingResult()
    object Backspaced : TypingResult()
    object Ignored : TypingResult()
}

class TypingEngine(var currentWord: WordItem) {

    private var typedCount: Int = 0
    private var isFinished: Boolean = false
    private var errorStreak: Int = 0

    fun resetWithWord(word: WordItem) {
        currentWord = word
        typedCount = 0
        isFinished = false
        errorStreak = 0
    }

    fun getState(): WordInputState {
        return WordInputState(
            currentWord = currentWord,
            typedCount = typedCount,
            isWordCompleted = isFinished,
            lastInputWasError = errorStreak > 0,
            errorMessage = if (errorStreak > 0 && typedCount < currentWord.word.length) {
                "Spróbuj literkę: ${currentWord.word[typedCount]}"
            } else null
        )
    }

    fun onCharTyped(char: Char): TypingResult {
        if (isFinished) {
            return TypingResult.Ignored
        }

        // Ignorujemy znaki niedrukowalne lub sterujące
        if (!char.isLetter()) {
            return TypingResult.Ignored
        }

        if (typedCount >= currentWord.word.length) {
            return TypingResult.Ignored
        }

        val expected = currentWord.word[typedCount]
        return if (PolishCharNormalizer.isCharMatch(char, expected)) {
            typedCount++
            errorStreak = 0
            if (typedCount == currentWord.word.length) {
                isFinished = true
                TypingResult.CorrectChar(typedCount, isFinished = true)
            } else {
                TypingResult.CorrectChar(typedCount, isFinished = false)
            }
        } else {
            errorStreak++
            TypingResult.IncorrectChar(inputChar = char, expectedChar = expected)
        }
    }

    fun onBackspace(): TypingResult {
        if (isFinished) {
            return TypingResult.Ignored
        }
        if (typedCount > 0) {
            typedCount--
            errorStreak = 0
            return TypingResult.Backspaced
        }
        return TypingResult.Ignored
    }
}
