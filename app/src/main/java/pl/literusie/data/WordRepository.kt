package pl.literusie.data

import org.json.JSONArray
import org.json.JSONException
import java.io.File
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

/**
 * Czyta bazę podstawową (assets) i dołącza słowa z pobranych pakietów ([packsDir]/<id>/words.json).
 * Słowo z pakietu trafia do bazy tylko, gdy jest poprawne, nie duplikuje słowa już istniejącego i ma plik obrazka
 * (nigdy nie podstawiamy cudzej grafiki). Obrazki pakietów mają imageId w postaci „pack:<id>/<nazwa>”.
 */
class WordRepository(
    private val read: () -> String,
    private val packsDir: File? = null,
    /** Drugi plik słów wbudowany w aplikację (ARASAAC); null/wyjątek = brak. Obrazki: assets/illustrations_arasaac. */
    private val readExtra: (() -> String)? = null,
) {
    private var cached: WordCatalog? = null
    var rejected: List<String> = emptyList()
        private set

    fun catalog(): WordCatalog = cached ?: run {
        val base = WordParser.parse(read())
        val rejects = base.rejected.toMutableList()
        val words = base.words.toMutableList()
        val texts = words.map { it.text }.toHashSet()
        val ids = words.map { it.id }.toHashSet()
        readExtra?.let { r ->
            val extra = runCatching { r() }.getOrNull()
            if (extra != null) {
                val parsed = WordParser.parse(extra)
                rejects += parsed.rejected.map { "words_arasaac: $it" }
                for (w in parsed.words) if (texts.add(w.text) && ids.add(w.id)) words += w
            }
        }
        packsDir?.listFiles()?.filter { it.isDirectory && !it.name.startsWith(".") }?.sortedBy { it.name }?.forEach { pack ->
            val json = runCatching { File(pack, "words.json").readText(Charsets.UTF_8) }.getOrNull() ?: return@forEach
            val parsed = WordParser.parse(json)
            rejects += parsed.rejected.map { "pakiet ${pack.name}: $it" }
            for (w in parsed.words) {
                val image = File(pack, "img/${w.imageId}.png")
                when {
                    !image.isFile -> rejects += "pakiet ${pack.name}: brak obrazka dla ${w.text}"
                    w.text in texts || w.id in ids -> Unit // duplikat słowa z bazy podstawowej lub innego pakietu
                    else -> {
                        texts += w.text; ids += w.id
                        words += w.copy(imageId = "pack:${pack.name}/${w.imageId}")
                    }
                }
            }
        }
        rejected = rejects
        WordCatalog(words).also { cached = it }
    }
}
