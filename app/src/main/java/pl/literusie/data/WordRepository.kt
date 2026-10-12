package pl.literusie.data

import org.json.JSONArray
import org.json.JSONException
import pl.literusie.domain.model.Word
import pl.literusie.domain.model.WordCatalog

/** Wynik wczytania bazy: poprawne słowa oraz opisy odrzuconych rekordów (błędne dane nie wywracają aplikacji). */
data class ParseResult(val words: List<Word>, val rejected: List<String>)

object WordParser {
    fun parse(json: String): ParseResult {
        val words = mutableListOf<Word>()
        val rejected = mutableListOf<String>()
        val seenIds = HashSet<String>()
        val seenTexts = HashSet<String>()
        val array = try { JSONArray(json) } catch (e: JSONException) {
            return ParseResult(emptyList(), listOf("baza uszkodzona: ${e.message}"))
        }
        for (i in 0 until array.length()) {
            val o = array.optJSONObject(i)
            if (o == null) { rejected += "rekord $i: nie jest obiektem"; continue }
            val id = o.optString("id", "")
            val text = o.optString("word", "").trim()
            val category = o.optString("category", "")
            val image = o.optString("imageId", "")
            val level = o.optInt("difficulty", 0)
            val problem = when {
                id.isEmpty() -> "brak id"
                text.isEmpty() -> "puste słowo"
                !text.all { it.isLetter() } -> "słowo zawiera znaki inne niż litery"
                category.isEmpty() -> "brak kategorii"
                image.isEmpty() -> "brak ilustracji"
                level !in 1..4 -> "zły poziom trudności"
                !seenIds.add(id) -> "zdublowany identyfikator"
                !seenTexts.add(text.uppercase()) -> "zdublowane słowo"
                else -> null
            }
            if (problem != null) { rejected += "rekord $i ($text): $problem"; continue }
            words += Word(
                id = id, text = text.uppercase(), category = category,
                secondaryCategory = o.optString("secondaryCategory", "").ifEmpty { null },
                difficulty = level, imageId = image,
                description = o.optString("description", "").ifEmpty { null },
                source = o.optString("source", "").ifEmpty { null },
                imageLicense = o.optString("imageLicense", "").ifEmpty { null },
            )
        }
        return ParseResult(words, rejected)
    }
}

/** Czyta bazę z podanego źródła (na Androidzie: asset). Cache w pamięci. */
class WordRepository(private val read: () -> String) {
    private var cached: WordCatalog? = null
    var rejected: List<String> = emptyList()
        private set

    fun catalog(): WordCatalog = cached ?: run {
        val r = WordParser.parse(read())
        rejected = r.rejected
        WordCatalog(r.words).also { cached = it }
    }
}
