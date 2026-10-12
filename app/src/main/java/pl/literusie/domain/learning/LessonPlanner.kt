package pl.literusie.domain.learning

import pl.literusie.domain.model.Word

/** Wybór słów do jednej rundy (domyślnie 10): najpierw znane, potem nowe – od najłatwiejszych. */
class LessonPlanner(private val words: List<Word>) {

    enum class Mode { CONTINUE, REVIEW }

    /**
     * „Zaczynamy”: stały program – nowe słowa w tej samej, powtarzalnej kolejności (poziomy trudności rosnąco,
     * w obrębie poziomu na przemian kategorie). Powtórki są osobno w trybie „Powtarzamy”, więc kolejność
     * nie zmienia się między uruchomieniami; zmienia się tylko to, że ukończone słowa zostają za nami.
     */
    fun next(progress: Map<String, WordProgress>, now: Long, size: Int = ROUND_SIZE): List<Word> {
        val fresh = interleavedNew(words, progress).take(size)
        if (fresh.size >= size) return fresh
        // Wszystko już poznane: dopełniamy najdawniej ćwiczonymi.
        val fill = words.filter { learned(progress[it.id]) }
            .sortedBy { progress.getValue(it.id).lastPracticed }
            .take(size - fresh.size)
        return fresh + fill
    }

    /** Wybrana kategoria: nowe słowa (łatwe najpierw), potem te ćwiczone najdawniej. */
    fun category(name: String, progress: Map<String, WordProgress>, size: Int = ROUND_SIZE): List<Word> {
        val inCat = words.filter { it.inCategory(name) }
        val fresh = inCat.filter { !learned(progress[it.id]) }.sortedBy { it.difficulty }
        val seen = inCat.filter { learned(progress[it.id]) }.sortedBy { progress.getValue(it.id).lastPracticed }
        return (fresh + seen).take(size)
    }

    /** „Powtarzamy”: tylko słowa, które dziecko już wpisało; zaległe pierwsze. */
    fun review(progress: Map<String, WordProgress>, now: Long, size: Int = ROUND_SIZE): List<Word> =
        words.filter { learned(progress[it.id]) }
            .sortedWith(compareBy({ progress.getValue(it.id).nextDue > now }, { progress.getValue(it.id).nextDue }))
            .take(size)

    private fun learned(p: WordProgress?) = p != null && p.completions > 0

    /** Nowe słowa: poziomami trudności, a w obrębie poziomu na przemian z różnych kategorii. */
    private fun interleavedNew(all: List<Word>, progress: Map<String, WordProgress>): List<Word> =
        all.filter { !learned(progress[it.id]) }
            .groupBy { it.difficulty }.toSortedMap().values
            .flatMap { level ->
                val queues = level.groupBy { it.category }.toSortedMap().values.map { it.iterator() }
                buildList {
                    while (queues.any { it.hasNext() }) queues.forEach { if (it.hasNext()) add(it.next()) }
                }
            }

    companion object {
        const val ROUND_SIZE = 10
    }
}
