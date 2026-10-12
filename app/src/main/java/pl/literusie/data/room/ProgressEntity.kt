package pl.literusie.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.literusie.domain.learning.WordProgress
import pl.literusie.domain.learning.WordStatus

@Entity(tableName = "word_progress")
data class ProgressEntity(
    @PrimaryKey val wordId: String,
    val attempts: Int,
    val completions: Int,
    val errors: Int,
    val box: Int,
    val status: String,
    val lastPracticed: Long,
    val nextDue: Long,
) {
    fun toDomain() = WordProgress(
        wordId, attempts, completions, errors, box,
        runCatching { WordStatus.valueOf(status) }.getOrDefault(WordStatus.NEW), lastPracticed, nextDue,
    )

    companion object {
        fun from(p: WordProgress) =
            ProgressEntity(p.wordId, p.attempts, p.completions, p.errors, p.box, p.status.name, p.lastPracticed, p.nextDue)
    }
}
