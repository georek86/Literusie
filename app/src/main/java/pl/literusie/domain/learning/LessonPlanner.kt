package pl.literusie.domain.learning

import pl.literusie.domain.model.Word

/** Wybór słów do jednej rundy (domyślnie 10): najpierw znane, potem nowe – od najłatwiejszych. */
class LessonPlanner(private val words: List<Word>) {

    enum class Mode { CONTINUE, REVIEW }

    /** „Zaczynamy”: kilka powtórek zaległych słów, reszta to nowe słowa wg rosnącej trudności. */
    fun next(progress: Map<String, WordProgress>, now: Long, size: Int = ROUND_SIZE): List<Word> {
        val dueReviews = words.filter { learned(progress[it.id]) && progress.getValue(it.id).nextDue <= now }
            .sortedBy { progress.getValue(it.id).nextDue }
            .take(size / 3)
        val fresh = interleavedNew(words, progress).take(size - dueReviews.size)
        val fill = if (dueReviews.size + fresh.size < size) {
            words.filter { learned(progress[it.id]) && it !in dueReviews }
                .sortedBy { progress.getValue(it.id).lastPracticed }
                .take(size - dueReviews.size - fresh.size)
        } else emptyList()
        return dueReviews + fresh + fill
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
