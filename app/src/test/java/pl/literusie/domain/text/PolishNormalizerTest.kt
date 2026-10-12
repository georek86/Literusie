package pl.literusie.domain.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.literusie.CMA
import pl.literusie.GES
import pl.literusie.LODZ
import pl.literusie.ZABA
import pl.literusie.ZREBAK
import pl.literusie.domain.model.Word
import pl.literusie.domain.typing.TypingEngine
import pl.literusie.domain.typing.TypingResult

class PolishNormalizerTest {

    @Test fun `wszystkie polskie znaki sprowadzane do liter lacinskich`() {
        val pairs = mapOf('Ą' to 'A', 'Ć' to 'C', 'Ę' to 'E', 'Ł' to 'L', 'Ń' to 'N', 'Ó' to 'O', 'Ś' to 'S', 'Ź' to 'Z', 'Ż' to 'Z')
        for ((pl, ascii) in pairs) {
            assertEquals(ascii, PolishNormalizer.normalizeChar(pl))
            assertEquals(ascii, PolishNormalizer.normalizeChar(pl.lowercaseChar()))
        }
    }

    @Test fun `wielkosc liter nie ma znaczenia`() {
        assertTrue(PolishNormalizer.matches('k', 'K'))
        assertTrue(PolishNormalizer.matches('K', 'k'))
        assertEquals("KOT", PolishNormalizer.normalizeWord("kot"))
    }

    @Test fun `bledne litery nie sa uznawane`() {
        assertFalse(PolishNormalizer.matches('S', 'Ć'))
        assertFalse(PolishNormalizer.matches('X', 'Ż'))
        assertFalse(PolishNormalizer.matches('O', 'Ą'))
        assertFalse(PolishNormalizer.matches('K', 'T'))
    }

    @Test fun `ze Z i Z z kropka sa rownowazne w obie strony`() {
        assertTrue(PolishNormalizer.matches('Z', 'Ź'))
        assertTrue(PolishNormalizer.matches('Z', 'Ż'))
        assertTrue(PolishNormalizer.matches('ż', 'Ż'))
        assertTrue(PolishNormalizer.matches('ź', 'Z'))
    }

    @Test fun `oryginalna pisownia pozostaje nietknieta`() {
        assertEquals("ŻABA", ZABA.text)
        assertEquals("zaba", Word("x", "ŻABA", "", null, 1, "").spoken.let { PolishNormalizer.normalizeWord(it).lowercase() })
        assertEquals("żaba", ZABA.spoken)
    }

    private fun typeAll(word: Word, input: String): TypingResult {
        val e = TypingEngine(word)
        var last: TypingResult = TypingResult.Ignored
        for (c in input) last = e.type(c)
        return last
    }

    private fun assertAccepts(word: Word, vararg inputs: String) {
        for (input in inputs) {
            val r = typeAll(word, input)
            assertTrue("${word.text} <- $input", r is TypingResult.Correct && r.finished)
        }
    }

    @Test fun `CMA`() = assertAccepts(CMA, "CMA", "ĆMA", "cma", "ćma")
    @Test fun `ZABA`() = assertAccepts(ZABA, "ZABA", "ŻABA", "zaba", "żaba")
    @Test fun `LODZ`() = assertAccepts(LODZ, "LODZ", "ŁÓDŹ", "lodz", "łódź", "ŁODŻ".replace('Ż', 'Z'))
    @Test fun `GES`() = assertAccepts(GES, "GES", "GĘŚ", "ges")
    @Test fun `ZREBAK`() = assertAccepts(ZREBAK, "ZREBAK", "ŹREBAK", "zrebak", "źrebak")

    @Test fun `bledne slowo nie konczy zadania`() {
        for ((word, bad) in listOf(CMA to "SMA", ZABA to "XABA", LODZ to "LADZ", GES to "GAS", ZREBAK to "TREBAK")) {
            val r = typeAll(word, bad)
            assertFalse("${word.text} <- $bad", r is TypingResult.Correct && r.finished)
        }
    }
}
