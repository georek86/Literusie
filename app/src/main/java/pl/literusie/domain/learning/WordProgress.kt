package pl.literusie.domain.learning

enum class WordStatus { NEW, LEARNING, MASTERED }

/**
 * Postęp w jednym słowie. [box] to numer „pudełka” powtórek (0–4): rośnie po bezbłędnym wpisaniu,
 * a słowo uznajemy za opanowane dopiero od pudełka 3 – jednorazowe poprawne wpisanie nie wystarcza.
 */
data class WordProgress(
    val wordId: String,
    val attempts: Int = 0,
    val completions: Int = 0,
    val errors: Int = 0,
    val box: Int = 0,
    val status: WordStatus = WordStatus.NEW,
    val lastPracticed: Long = 0L,
    val nextDue: Long = 0L,
)

fun interface Clock {
    fun now(): Long

    companion object {
        val System = Clock { java.lang.System.currentTimeMillis() }
    }
}

/** Reguły zapamiętywania (jedno miejsce, łatwe do przetestowania). */
object Mastery {
    const val MASTERED_BOX = 3
    const val MAX_BOX = 4
    private const val MINUTE = 60_000L
    private const val DAY = 24 * 60 * MINUTE
    private val INTERVALS = longArrayOf(10 * MINUTE, DAY, 3 * DAY, 7 * DAY, 14 * DAY)

    fun afterCompletion(old: WordProgress?, wordId: String, errors: Int, now: Long): WordProgress {
        val p = old ?: WordProgress(wordId)
        val box = when {
            errors == 0 -> minOf(p.box + 1, MAX_BOX)
            errors >= 3 -> maxOf(p.box - 1, 0)
            else -> p.box
        }
        return p.copy(
            attempts = p.attempts + 1,
            completions = p.completions + 1,
            errors = p.errors + errors,
            box = box,
            status = if (box >= MASTERED_BOX) WordStatus.MASTERED else WordStatus.LEARNING,
            lastPracticed = now,
            nextDue = now + INTERVALS[box],
        )
    }
}

/** Lokalny magazyn postępów (Room na urządzeniu, pamięć w testach). Nic nie opuszcza urządzenia. */
interface ProgressStore {
    suspend fun loadAll(): Map<String, WordProgress>
    suspend fun save(progress: WordProgress)
    suspend fun clear()
}

class InMemoryProgressStore(initial: Map<String, WordProgress> = emptyMap()) : ProgressStore {
    private val map = initial.toMutableMap()
    override suspend fun loadAll(): Map<String, WordProgress> = map.toMap()
    override suspend fun save(progress: WordProgress) { map[progress.wordId] = progress }
    override suspend fun clear() = map.clear()
}
