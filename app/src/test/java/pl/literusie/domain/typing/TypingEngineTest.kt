package pl.literusie.domain.typing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.literusie.ZABA
import pl.literusie.w

class TypingEngineTest {
    private val kot = w("kot", "KOT")

    @Test fun `litery sprawdzane po kolei K O T`() {
        val e = TypingEngine(kot)
        assertEquals(TypingResult.Correct(1, false), e.type('k'))
        assertEquals(TypingResult.Correct(2, false), e.type('O'))
        assertEquals(TypingResult.Correct(3, true), e.type('t'))
        assertTrue(e.state.finished)
    }

    @Test fun `litera spoza kolejnosci jest bledem i nie psuje postepu`() {
        val e = TypingEngine(kot)
        e.type('K')
        assertTrue(e.type('T') is TypingResult.Wrong)
        assertEquals(1, e.state.typed)
        assertEquals(1, e.state.totalErrors)
        assertEquals('O', e.state.expected)
        assertTrue(e.type('O') is TypingResult.Correct)
        assertEquals(0, e.state.streakErrors)
        assertEquals(1, e.state.totalErrors)
    }

    @Test fun `backspace cofa o jedna litere i nie schodzi ponizej zera`() {
        val e = TypingEngine(kot)
        assertEquals(TypingResult.Ignored, e.backspace())
        e.type('K'); e.type('O')
        assertEquals(TypingResult.Backspaced, e.backspace())
        assertEquals(1, e.state.typed)
        e.backspace()
        assertEquals(0, e.state.typed)
        assertEquals(TypingResult.Ignored, e.backspace())
        e.type('K'); e.type('O'); e.type('T')
        assertTrue(e.state.finished)
    }

    @Test fun `nieistotne znaki sa ignorowane`() {
        val e = TypingEngine(kot)
        for (c in listOf(' ', '1', '.', '\n', '\t', '-', '!')) assertEquals(TypingResult.Ignored, e.type(c))
        assertEquals(0, e.state.typed)
        assertEquals(0, e.state.totalErrors)
    }

    @Test fun `po ukonczeniu slowa kolejne klawisze nic nie robia`() {
        val e = TypingEngine(kot)
        "KOT".forEach { e.type(it) }
        assertEquals(TypingResult.Ignored, e.type('A'))
        assertEquals(TypingResult.Ignored, e.backspace())
        assertEquals(3, e.state.typed)
        assertEquals(0, e.state.totalErrors)
    }

    @Test fun `podpowiedz po dwoch bledach pod rzad`() {
        val e = TypingEngine(kot)
        e.type('X')
        assertFalse(e.state.showHint)
        e.type('Y')
        assertTrue(e.state.showHint)
        e.type('K')
        assertFalse(e.state.showHint)
    }

    @Test fun `reset zaczyna nowe slowo od zera`() {
        val e = TypingEngine(kot)
        e.type('X'); e.type('K')
        e.reset(ZABA)
        assertEquals(0, e.state.typed)
        assertEquals(0, e.state.totalErrors)
        assertNull(e.state.lastWrong)
        assertEquals('Ż', e.state.expected)
    }
}
