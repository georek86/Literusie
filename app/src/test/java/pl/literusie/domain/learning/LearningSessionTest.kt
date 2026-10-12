package pl.literusie.domain.learning

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.literusie.ZABA
import pl.literusie.domain.typing.TypingResult
import pl.literusie.w

class LearningSessionTest {
    private val kot = w("kot", "KOT")
    private val pies = w("pies", "PIES")
    private var now = 1_000_000L
    private val clock = Clock { now }

    private fun type(s: LearningSession, text: String) = text.forEach { s.onLetter(it) }

    @Test fun `ukonczenie slowa zapisuje postep i przechodzi do nastepnego`() = runBlocking {
        val store = InMemoryProgressStore()
        val s = LearningSession(listOf(kot, pies), store, clock)
        type(s, "kot")
        assertTrue(s.state.value.inputLocked)
        s.completeWordAndAdvance()
        assertEquals("PIES", s.currentWord?.text)
        val saved = store.loadAll().getValue("kot")
        assertEquals(1, saved.completions)
        assertEquals(WordStatus.LEARNING, saved.status)
        assertEquals(now, saved.lastPracticed)
        assertEquals(1, s.state.value.index)
    }

    @Test fun `po ukonczeniu slowa przypadkowe klawisze nie trafiaja do kolejnego`() = runBlocking {
        val s = LearningSession(listOf(kot, pies), InMemoryProgressStore(), clock)
        type(s, "kot")
        assertEquals(TypingResult.Ignored, s.onLetter('P'))
        assertEquals(TypingResult.Ignored, s.onBackspace())
        assertEquals(3, s.state.value.typing?.typed)
    }

    @Test fun `nowe slowo zaczyna sie od zera po pauzie`() = runBlocking {
        val s = LearningSession(listOf(kot, pies), InMemoryProgressStore(), clock)
        type(s, "kot"); s.onLetter('P')
        s.completeWordAndAdvance()
        assertEquals(0, s.state.value.typing?.typed)
        assertFalse(s.state.value.inputLocked)
        assertTrue(s.onLetter('p') is TypingResult.Correct)
    }

    @Test fun `ostatnie slowo konczy runde`() = runBlocking {
        val s = LearningSession(listOf(kot), InMemoryProgressStore(), clock)
        type(s, "kot")
        s.completeWordAndAdvance()
        assertTrue(s.state.value.roundFinished)
        assertNull(s.state.value.typing)
        assertEquals(TypingResult.Ignored, s.onLetter('K'))
    }

    @Test fun `bledy trafiaja do statystyk i obnizaja pewnosc`() = runBlocking {
        val store = InMemoryProgressStore()
        val s = LearningSession(listOf(kot), store, clock)
        type(s, "xyzkot")
        s.completeWordAndAdvance()
        val p = store.loadAll().getValue("kot")
        assertEquals(3, p.errors)
        assertEquals(0, p.box)
    }

    @Test fun `wznowienie po restarcie - postep z magazynu jest widoczny dla nowej sesji`() = runBlocking {
        val store = InMemoryProgressStore()
        val first = LearningSession(listOf(kot, pies), store, clock)
        type(first, "kot"); first.completeWordAndAdvance()
        // „restart aplikacji”: nowy planer i nowa sesja na tym samym magazynie
        val progress = store.loadAll()
        val next = LessonPlanner(listOf(kot, pies)).next(progress, now, size = 1)
        assertEquals(listOf(pies), next)
    }

    @Test fun `awaria zapisu nie przerywa nauki`() = runBlocking {
        val broken = object : ProgressStore {
            override suspend fun loadAll() = emptyMap<String, WordProgress>()
            override suspend fun save(progress: WordProgress) = error("dysk pelny")
            override suspend fun clear() = Unit
        }
        val s = LearningSession(listOf(kot, pies), broken, clock)
        type(s, "kot")
        s.completeWordAndAdvance()
        assertEquals("PIES", s.currentWord?.text)
    }

    @Test fun `slowo z polskim znakiem przechodzi bez ogonka`() = runBlocking {
        val s = LearningSession(listOf(ZABA), InMemoryProgressStore(), clock)
        type(s, "zaba")
        s.completeWordAndAdvance()
        assertTrue(s.state.value.roundFinished)
    }

    @Test fun `pusta lista slow nie wywraca sesji`() {
        val s = LearningSession(emptyList(), InMemoryProgressStore(), clock)
        assertTrue(s.state.value.roundFinished)
        assertNull(s.currentWord)
        assertNotNull(s.state.value)
    }
}
