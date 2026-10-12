package pl.slowomistrz.app.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.slowomistrz.app.domain.model.DifficultyLevel
import pl.slowomistrz.app.domain.model.WordItem
import pl.slowomistrz.app.domain.model.WordLearningStatus
import kotlinx.coroutines.runBlocking

class LearningSessionManagerTest {

    @Test
    fun testSessionProgressionAndCompletion() = runBlocking {
        val wordList = listOf(
            WordItem("1", "KOT", category = "zwierzęta", difficulty = DifficultyLevel.LEVEL_1, imageId = "img_kot"),
            WordItem("2", "DOM", category = "dom", difficulty = DifficultyLevel.LEVEL_1, imageId = "img_dom")
        )

        val session = LearningSessionManager(wordList)
        assertEquals(0, session.currentWordIndex)
        assertEquals("KOT", session.currentWord?.word)

        // Wpisanie K -> O -> T
        session.onCharTyped('k')
        session.onCharTyped('o')
        val res = session.onCharTyped('t')
        assertTrue(res is TypingResult.CorrectChar)
        assertTrue(session.inputState.value.isWordCompleted)

        // Przejście do następnego słowa
        val advanced = session.completeCurrentWordAndAdvance()
        assertTrue(advanced)
        assertEquals(1, session.currentWordIndex)
        assertEquals("DOM", session.currentWord?.word)
        assertFalse(session.inputState.value.isWordCompleted)
        assertEquals(0, session.inputState.value.typedCount)

        // Wpisanie D -> O -> M
        session.onCharTyped('d')
        session.onCharTyped('o')
        session.onCharTyped('m')
        assertTrue(session.inputState.value.isWordCompleted)

        val endReached = session.completeCurrentWordAndAdvance()
        assertFalse(endReached) // Koniec listy
    }

    @Test
    fun testJumpToWord() {
        val wordList = listOf(
            WordItem("1", "KOT", category = "zwierzęta", difficulty = DifficultyLevel.LEVEL_1, imageId = "img_kot"),
            WordItem("2", "DOM", category = "dom", difficulty = DifficultyLevel.LEVEL_1, imageId = "img_dom")
        )
        val session = LearningSessionManager(wordList)
        session.jumpToWord(1)
        assertEquals(1, session.currentWordIndex)
        assertEquals("DOM", session.currentWord?.word)
    }
}
