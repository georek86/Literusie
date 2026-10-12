package pl.slowomistrz.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pl.slowomistrz.app.data.local.dao.WordProgressDao
import pl.slowomistrz.app.data.local.entity.WordProgressEntity

@Database(entities = [WordProgressEntity::class], version = 1, exportSchema = false)
abstract class SlowomistrzDatabase : RoomDatabase() {
    abstract fun wordProgressDao(): WordProgressDao

    companion object {
        @Volatile
        private var INSTANCE: SlowomistrzDatabase? = null

        fun getDatabase(context: Context): SlowomistrzDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SlowomistrzDatabase::class.java,
                    "slowomistrz_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
