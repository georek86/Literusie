package pl.slowomistrz.app.domain.model

data class WordInputState(
    val currentWord: WordItem,
    val typedCount: Int = 0,
    val isWordCompleted: Boolean = false,
    val lastInputWasError: Boolean = false,
    val errorMessage: String? = null
) {
    val totalLength: Int get() = currentWord.word.length

    val expectedChar: Char? get() = if (typedCount < totalLength) currentWord.word[typedCount] else null

    fun isLetterFilled(index: Int): Boolean = index < typedCount

    fun getDisplayCharForSlot(index: Int): Char {
        return currentWord.word[index]
    }
}
