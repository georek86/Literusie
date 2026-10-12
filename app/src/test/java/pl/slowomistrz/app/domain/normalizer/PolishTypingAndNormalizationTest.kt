package pl.slowomistrz.app.domain.normalizer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.slowomistrz.app.domain.engine.TypingEngine
import pl.slowomistrz.app.domain.engine.TypingResult
import pl.slowomistrz.app.domain.model.DifficultyLevel
import pl.slowomistrz.app.domain.model.WordItem

class PolishTypingAndNormalizationTest {

    @Test
    fun testPolishDiacriticsNormalization() {
        assertEquals('A', PolishCharNormalizer.normalizeChar('Ą'))
        assertEquals('C', PolishCharNormalizer.normalizeChar('Ć'))
        assertEquals('E', PolishCharNormalizer.normalizeChar('Ę'))
        assertEquals('L', PolishCharNormalizer.normalizeChar('Ł'))
        assertEquals('N', PolishCharNormalizer.normalizeChar('Ń'))
        assertEquals('O', PolishCharNormalizer.normalizeChar('Ó'))
        assertEquals('S', PolishCharNormalizer.normalizeChar('Ś'))
        assertEquals('Z', PolishCharNormalizer.normalizeChar('Ź'))
        assertEquals('Z', PolishCharNormalizer.normalizeChar('Ż'))

        // Case insensitivity
        assertEquals('A', PolishCharNormalizer.normalizeChar('ą'))
        assertEquals('C', PolishCharNormalizer.normalizeChar('ć'))
        assertEquals('Z', PolishCharNormalizer.normalizeChar('ż'))
        assertEquals('Z', PolishCharNormalizer.normalizeChar('ź'))
        assertEquals('K', PolishCharNormalizer.normalizeChar('k'))
    }

    @Test
    fun testMandatoryWordsFromSpec() {
        // Obowiązkowo przetestuj: „ĆMA”, „ŻABA”, „ŁÓDŹ”, „GĘŚ” i „ŹREBAK”

        // 1. ĆMA
        assertTrue(PolishCharNormalizer.isCharMatch('c', 'Ć'))
        assertTrue(PolishCharNormalizer.isCharMatch('C', 'Ć'))
        assertTrue(PolishCharNormalizer.isCharMatch('Ć', 'Ć'))
        assertEquals("CMA", PolishCharNormalizer.normalizeWord("ĆMA"))

        // 2. ŻABA
        assertTrue(PolishCharNormalizer.isCharMatch('z', 'Ż'))
        assertTrue(PolishCharNormalizer.isCharMatch('Z', 'Ż'))
        assertTrue(PolishCharNormalizer.isCharMatch('Ż', 'Ż'))
        assertEquals("ZABA", PolishCharNormalizer.normalizeWord("ŻABA"))

        // 3. ŁÓDŹ
        assertTrue(PolishCharNormalizer.isCharMatch('l', 'Ł'))
        assertTrue(PolishCharNormalizer.isCharMatch('o', 'Ó'))
        assertTrue(PolishCharNormalizer.isCharMatch('d', 'D'))
        assertTrue(PolishCharNormalizer.isCharMatch('z', 'Ź'))
        assertEquals("LODZ", PolishCharNormalizer.normalizeWord("ŁÓDŹ"))

        // 4. GĘŚ
        assertTrue(PolishCharNormalizer.isCharMatch('g', 'G'))
        assertTrue(PolishCharNormalizer.isCharMatch('e', 'Ę'))
        assertTrue(PolishCharNormalizer.isCharMatch('s', 'Ś'))
        assertEquals("GES", PolishCharNormalizer.normalizeWord("GĘŚ"))

        // 5. ŹREBAK
        assertTrue(PolishCharNormalizer.isCharMatch('z', 'Ź'))
        assertTrue(PolishCharNormalizer.isCharMatch('Ź', 'Ź'))
        assertEquals("ZREBAK", PolishCharNormalizer.normalizeWord("ŹREBAK"))
    }

    @Test
    fun testTypingEngineSequentialProgression() {
        val zaba = WordItem(
            id = "w_zaba",
            word = "ŻABA",
            category = "zwierzęta",
            difficulty = DifficultyLevel.LEVEL_1,
            imageId = "img_zaba"
        )
        val engine = TypingEngine(zaba)

        // Wpisanie małej litery 'z' (zamiennik dla Ż)
        val res1 = engine.onCharTyped('z')
        assertTrue(res1 is TypingResult.CorrectChar)
        assertEquals(1, (res1 as TypingResult.CorrectChar).newTypedCount)
        assertFalse(res1.isFinished)

        // Wpisanie błędnej litery 'x'
        val resError = engine.onCharTyped('x')
        assertTrue(resError is TypingResult.IncorrectChar)
        assertEquals(1, engine.getState().typedCount)
        assertTrue(engine.getState().lastInputWasError)

        // Poprawne wpisanie 'a' (case insensitive)
        val res2 = engine.onCharTyped('A')
        assertTrue(res2 is TypingResult.CorrectChar)
        assertEquals(2, (res2 as TypingResult.CorrectChar).newTypedCount)

        // Backspace
        val resBack = engine.onBackspace()
        assertTrue(resBack is TypingResult.Backspaced)
        assertEquals(1, engine.getState().typedCount)

        // Ponowne wpisanie 'A', potem 'B', potem 'A'
        engine.onCharTyped('a')
        engine.onCharTyped('b')
        val resFinal = engine.onCharTyped('a')

        assertTrue(resFinal is TypingResult.CorrectChar)
        assertTrue((resFinal as TypingResult.CorrectChar).isFinished)
        assertTrue(engine.getState().isWordCompleted)

        // Dalsze znaki są ignorowane
        val resExtra = engine.onCharTyped('c')
        assertEquals(TypingResult.Ignored, resExtra)
    }

    @Test
    fun testTypingEngineLodzWord() {
        val lodz = WordItem(
            id = "w_lodz",
            word = "ŁÓDŹ",
            category = "pojazdy",
            difficulty = DifficultyLevel.LEVEL_2,
            imageId = "img_lodz"
        )
        val engine = TypingEngine(lodz)

        // L -> O -> D -> Z
        assertTrue(engine.onCharTyped('L') is TypingResult.CorrectChar)
        assertTrue(engine.onCharTyped('O') is TypingResult.CorrectChar)
        assertTrue(engine.onCharTyped('D') is TypingResult.CorrectChar)
        val finalRes = engine.onCharTyped('Z')
        assertTrue(finalRes is TypingResult.CorrectChar)
        assertTrue((finalRes as TypingResult.CorrectChar).isFinished)
        assertTrue(engine.getState().isWordCompleted)
    }

    @Test
    fun testTypingEngineIgnoresNonLetters() {
        val kot = WordItem(
            id = "w_kot",
            word = "KOT",
            category = "zwierzęta",
            difficulty = DifficultyLevel.LEVEL_1,
            imageId = "img_kot"
        )
        val engine = TypingEngine(kot)

        // Klawisze funkcyjne / spacje / cyfry
        assertEquals(TypingResult.Ignored, engine.onCharTyped(' '))
        assertEquals(TypingResult.Ignored, engine.onCharTyped('1'))
        assertEquals(0, engine.getState().typedCount)
    }
}
