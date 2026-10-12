package pl.slowomistrz.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.slowomistrz.app.data.local.entity.WordProgressEntity

@Dao
interface WordProgressDao {

    @Query("SELECT * FROM word_progress WHERE wordId = :wordId")
    suspend fun getProgressForWord(wordId: String): WordProgressEntity?

    @Query("SELECT * FROM word_progress")
    fun getAllProgress(): Flow<List<WordProgressEntity>>

    @Query("SELECT COUNT(*) FROM word_progress WHERE status = 'MASTERED'")
    fun getMasteredCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM word_progress WHERE status != 'NEW'")
    fun getPracticedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(entity: WordProgressEntity)
}
