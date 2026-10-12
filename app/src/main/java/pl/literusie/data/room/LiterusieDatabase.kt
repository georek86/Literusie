package pl.literusie.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pl.literusie.domain.learning.ProgressStore
import pl.literusie.domain.learning.WordProgress

@Database(entities = [ProgressEntity::class], version = 1, exportSchema = false)
abstract class LiterusieDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao

    companion object {
        fun create(context: Context): LiterusieDatabase =
            Room.databaseBuilder(context.applicationContext, LiterusieDatabase::class.java, "literusie.db").build()
    }
}

/** Postępy zapisane lokalnie w Room; wywołania suspend Room same przenoszą pracę poza główny wątek. */
class RoomProgressStore(private val dao: ProgressDao) : ProgressStore {
    override suspend fun loadAll(): Map<String, WordProgress> = dao.all().associate { it.wordId to it.toDomain() }
    override suspend fun save(progress: WordProgress) = dao.upsert(ProgressEntity.from(progress))
    override suspend fun clear() = dao.clear()
}
