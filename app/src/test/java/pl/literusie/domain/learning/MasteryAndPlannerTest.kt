package pl.literusie.domain.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.literusie.w

class MasteryTest {
    @Test fun `jednorazowe poprawne wpisanie nie oznacza opanowania`() {
        val p = Mastery.afterCompletion(null, "a", 0, 0)
        assertEquals(WordStatus.LEARNING, p.status)
    }

    @Test fun `trzy bezbledne wpisania daja opanowanie, blad cofa`() {
        var p = Mastery.afterCompletion(null, "a", 0, 0)
        p = Mastery.afterCompletion(p, "a", 0, 1)
        assertEquals(WordStatus.LEARNING, p.status)
        p = Mastery.afterCompletion(p, "a", 0, 2)
        assertEquals(WordStatus.MASTERED, p.status)
        p = Mastery.afterCompletion(p, "a", 4, 3)
        assertEquals(WordStatus.LEARNING, p.status)
        assertEquals(4, p.attempts)
        assertEquals(4, p.errors)
    }

    @Test fun `odstep powtorek rosnie z pudelkiem`() {
        val a = Mastery.afterCompletion(null, "a", 0, 0)
        val b = Mastery.afterCompletion(a, "a", 0, 0)
        assertTrue(b.nextDue > a.nextDue)
    }
}

class LessonPlannerTest {
    private val words = listOf(
        w("1", "KOT", "zwierzęta", 1), w("2", "LIS", "zwierzęta", 1), w("3", "DOM", "miejsca", 1),
        w("4", "SŁOŃ", "zwierzęta", 2), w("5", "ZEGAR", "czas", 2), w("6", "MOTYL", "zwierzęta", 3),
        w("7", "BIEGAĆ", "czynności", 3), w("8", "AUTOBUS", "pojazdy", 4),
    )
    private val planner = LessonPlanner(words)

    @Test fun `na poczatku najpierw latwe slowa z roznych kategorii`() {
        val r = planner.next(emptyMap(), 0, size = 3)
        assertTrue(r.all { it.difficulty == 1 })
        assertEquals(listOf("miejsca", "zwierzęta"), r.map { it.category }.distinct().sorted())
    }

    @Test fun `slowa w rundzie sie nie powtarzaja`() {
        val r = planner.next(emptyMap(), 0, size = 8)
        assertEquals(r.size, r.map { it.id }.distinct().size)
    }

    @Test fun `poznane slowa nie wracaja jako nowe, a kolejnosc jest stala`() {
        val p = mapOf("1" to Mastery.afterCompletion(null, "1", 0, 0))
        val a = planner.next(p, now = 0, size = 5)
        assertFalse(a.any { it.id == "1" })
        assertEquals(a, planner.next(p, now = Long.MAX_VALUE / 2, size = 5))
        assertEquals(planner.next(emptyMap(), 0, 8), planner.next(emptyMap(), 123, 8))
    }

    @Test fun `powtarzanie to tylko slowa juz napisane`() {
        assertTrue(planner.review(emptyMap(), 0).isEmpty())
        val p = mapOf("3" to Mastery.afterCompletion(null, "3", 0, 5))
        assertEquals(listOf("3"), planner.review(p, 10).map { it.id })
    }

    @Test fun `kategoria obejmuje tez druga kategorie slowa i zaczyna od nowych latwych`() {
        val ws = words + w("9", "ŻÓŁW", "zwierzęta", 2) + w("10", "MAMA", "ludzie", 1, second = "zwierzęta")
        val r = LessonPlanner(ws).category("zwierzęta", emptyMap())
        assertTrue(r.any { it.id == "10" })
        assertEquals(r.sortedBy { it.difficulty }.map { it.id }, r.map { it.id })
    }
}
