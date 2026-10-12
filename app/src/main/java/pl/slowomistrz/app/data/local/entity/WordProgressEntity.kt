package pl.slowomistrz.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.slowomistrz.app.domain.model.WordLearningStatus
import pl.slowomistrz.app.domain.model.WordProgress

@Entity(tableName = "word_progress")
data class WordProgressEntity(
    @PrimaryKey
    val wordId: String,
    val attemptsCount: Int,
    val correctCount: Int,
    val errorsCount: Int,
    val status: String,
    val lastPracticedTimestamp: Long
) {
    fun toDomain(): WordProgress {
        return WordProgress(
            wordId = wordId,
            attemptsCount = attemptsCount,
            correctCount = correctCount,
            errorsCount = errorsCount,
            status = try {
                WordLearningStatus.valueOf(status)
            } catch (e: Exception) {
                WordLearningStatus.NEW
            },
            lastPracticedTimestamp = lastPracticedTimestamp
        )
    }

    companion object {
        fun fromDomain(progress: WordProgress): WordProgressEntity {
            return WordProgressEntity(
                wordId = progress.wordId,
                attemptsCount = progress.attemptsCount,
                correctCount = progress.correctCount,
                errorsCount = progress.errorsCount,
                status = progress.status.name,
                lastPracticedTimestamp = progress.lastPracticedTimestamp
            )
        }
    }
}
