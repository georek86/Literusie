package pl.slowomistrz.app.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import pl.slowomistrz.app.domain.model.DifficultyLevel
import pl.slowomistrz.app.domain.model.WordItem
import java.io.BufferedReader
import java.io.InputStreamReader

class WordRepository(private val context: Context) {

    private var cachedWords: List<WordItem>? = null

    suspend fun getAllWords(): List<WordItem> = withContext(Dispatchers.IO) {
        cachedWords?.let { return@withContext it }

        val list = mutableListOf<WordItem>()
        val assetManager = context.assets
        assetManager.open("words_database.json").use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val jsonString = reader.readText()
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val diffInt = obj.getInt("difficulty")
                val diffLevel = when (diffInt) {
                    1 -> DifficultyLevel.LEVEL_1
                    2 -> DifficultyLevel.LEVEL_2
                    3 -> DifficultyLevel.LEVEL_3
                    else -> DifficultyLevel.LEVEL_4
                }

                val item = WordItem(
                    id = obj.getString("id"),
                    word = obj.getString("word"),
                    displayWord = obj.optString("displayWord", obj.getString("word")),
                    category = obj.getString("category"),
                    secondaryCategory = if (obj.has("secondaryCategory")) obj.getString("secondaryCategory") else null,
                    difficulty = diffLevel,
                    imageId = obj.getString("imageId"),
                    description = if (obj.has("description")) obj.getString("description") else null,
                    source = if (obj.has("source")) obj.getString("source") else null
                )
                list.add(item)
            }
        }
        cachedWords = list
        list
    }

    suspend fun getCategories(): List<String> {
        val words = getAllWords()
        return words.map { it.category }.distinct()
    }

    suspend fun getWordsByCategory(category: String): List<WordItem> {
        val words = getAllWords()
        return words.filter { it.category == category }
    }

    suspend fun getWordsByDifficulty(difficulty: DifficultyLevel): List<WordItem> {
        val words = getAllWords()
        return words.filter { it.difficulty == difficulty }
    }
}
