package pl.slowomistrz.app.domain.model

enum class WordLearningStatus {
    NEW,
    LEARNING,
    MASTERED
}

data class WordProgress(
    val wordId: String,
    val attemptsCount: Int = 0,
    val correctCount: Int = 0,
    val errorsCount: Int = 0,
    val status: WordLearningStatus = WordLearningStatus.NEW,
    val lastPracticedTimestamp: Long = 0L
)
