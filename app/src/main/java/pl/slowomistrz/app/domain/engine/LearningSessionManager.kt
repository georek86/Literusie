package pl.slowomistrz.app.domain.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.slowomistrz.app.data.local.dao.WordProgressDao
import pl.slowomistrz.app.data.local.entity.WordProgressEntity
import pl.slowomistrz.app.domain.model.WordInputState
import pl.slowomistrz.app.domain.model.WordItem
import pl.slowomistrz.app.domain.model.WordLearningStatus
import pl.slowomistrz.app.domain.model.WordProgress

class LearningSessionManager(
    private val words: List<WordItem>,
    private val progressDao: WordProgressDao? = null,
    startIndex: Int = 0
) {
    private var currentIndex = if (words.isNotEmpty()) startIndex.coerceIn(0, words.size - 1) else 0
    private var typingEngine: TypingEngine = TypingEngine(
        if (words.isNotEmpty()) words[currentIndex] else WordItem("none", "", category = "", difficulty = pl.slowomistrz.app.domain.model.DifficultyLevel.LEVEL_1, imageId = "")
    )

    private val _inputState = MutableStateFlow(typingEngine.getState())
    val inputState: StateFlow<WordInputState> = _inputState.asStateFlow()

    private val _currentProgress = MutableStateFlow(
        WordProgress(words.getOrNull(currentIndex)?.id ?: "")
    )
    val currentProgress: StateFlow<WordProgress> = _currentProgress.asStateFlow()

    val currentWordIndex: Int get() = currentIndex
    val totalWordsCount: Int get() = words.size
    val currentWord: WordItem? get() = words.getOrNull(currentIndex)

    fun onCharTyped(char: Char): TypingResult {
        val result = typingEngine.onCharTyped(char)
        _inputState.value = typingEngine.getState()
        return result
    }

    fun onBackspace(): TypingResult {
        val result = typingEngine.onBackspace()
        _inputState.value = typingEngine.getState()
        return result
    }

    suspend fun completeCurrentWordAndAdvance(): Boolean {
        val word = currentWord ?: return false
        val now = System.currentTimeMillis()

        val updatedProgress = _currentProgress.value.copy(
            wordId = word.id,
            attemptsCount = _currentProgress.value.attemptsCount + 1,
            correctCount = _currentProgress.value.correctCount + 1,
            status = if (_currentProgress.value.correctCount + 1 >= 2) WordLearningStatus.MASTERED else WordLearningStatus.LEARNING,
            lastPracticedTimestamp = now
        )
        _currentProgress.value = updatedProgress

        progressDao?.insertOrUpdateProgress(WordProgressEntity.fromDomain(updatedProgress))

        if (currentIndex + 1 < words.size) {
            currentIndex++
            val nextWord = words[currentIndex]
            typingEngine.resetWithWord(nextWord)
            _inputState.value = typingEngine.getState()
            _currentProgress.value = WordProgress(nextWord.id)
            return true
        }
        return false // dotarto do końca zestawu
    }

    fun jumpToWord(index: Int) {
        if (index in words.indices) {
            currentIndex = index
            val word = words[currentIndex]
            typingEngine.resetWithWord(word)
            _inputState.value = typingEngine.getState()
            _currentProgress.value = WordProgress(word.id)
        }
    }
}
