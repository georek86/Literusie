package pl.literusie.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.literusie.ZABA
import pl.literusie.domain.learning.WordStatus
import pl.literusie.ui.app.LearnMode
import pl.literusie.ui.app.LiterusieApp
import pl.literusie.ui.app.Screen
import pl.literusie.w

@OptIn(ExperimentalTestApi::class)
class LearningFlowTest {
    private val kot = w("kot", "KOT", level = 1)
    private val lis = w("lis", "LIS", level = 1)

    private fun twoWordFixture() = Fixture(catalog = catalogOf(kot, lis))

    @Test fun `wpisywanie litera po literze, blad, backspace, sukces i nastepne slowo`() = runDesktopComposeUiTest(1920, 1080) {
        val fx = twoWordFixture()
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Learning(LearnMode.CONTINUE)) }
        settle()
        val first = caption()                                         // KOT albo LIS (kolejność jest losowa)
        val second = listOf("KOT", "LIS").single { it != first }
        onNodeWithTag("progress_text").assertTextEquals("0 z 3 liter")
        assertEquals(listOf(first.lowercase()), fx.speaker.spoken)   // wymowa małymi literami, raz

        typeKeys(first.take(1))
        onNodeWithTag("progress_text").assertTextEquals("1 z 3 liter")

        typeKeys("q")                                                 // błąd – postęp zostaje
        onNodeWithTag("progress_text").assertTextEquals("1 z 3 liter")
        onNodeWithTag("message").assertTextEquals("Prawie! Poszukaj literki ${first[1]}")

        press(Key.Backspace)
        onNodeWithTag("progress_text").assertTextEquals("0 z 3 liter")

        typeKeys(first)
        onNodeWithTag("message").assertTextEquals("Brawo!")

        // w czasie pauzy sukcesu przypadkowe klawisze nic nie wpisują do kolejnego słowa
        typeKeys(second)
        mainClock.advanceTimeBy(1_600)
        waitForIdle()
        onNodeWithTag("caption").assertTextEquals(second)
        onNodeWithTag("progress_text").assertTextEquals("0 z 3 liter")
        assertEquals(listOf(first.lowercase(), second.lowercase()), fx.speaker.spoken)

        val saved = runBlocking { fx.store.loadAll() }.getValue(first.lowercase())
        assertEquals(1, saved.completions)
        assertEquals(1, saved.errors)
        assertEquals(WordStatus.LEARNING, saved.status)
    }

    @Test fun `slowo z polskimi znakami mozna wpisac bez ogonkow`() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture(catalog = catalogOf(ZABA))
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Learning(LearnMode.CONTINUE)) }
        settle()
        onNodeWithTag("caption").assertTextEquals("ŻABA")          // podpis z ogonkiem
        typeKeys("zaba")
        onNodeWithTag("message").assertTextEquals("Brawo!")
        mainClock.advanceTimeBy(1_600); waitForIdle()
        onNodeWithTag("round_done").assertIsDisplayed()
    }

    @Test fun `ponowne odsluchanie nie traci wpisanych liter`() = runDesktopComposeUiTest(1920, 1080) {
        val fx = twoWordFixture()
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Learning(LearnMode.CONTINUE)) }
        settle()
        val word = caption()
        typeKeys(word.take(2))
        onNodeWithTag("replay").performClick()
        waitForIdle()
        assertEquals(listOf(word.lowercase(), word.lowercase()), fx.speaker.spoken)
        onNodeWithTag("progress_text").assertTextEquals("2 z 3 liter")
        typeKeys(word.drop(2))
        onNodeWithTag("message").assertTextEquals("Brawo!")
    }

    @Test fun `brak polskiego glosu pokazuje informacje dla opiekuna, nauka dziala dalej`() = runDesktopComposeUiTest(1920, 1080) {
        val fx = twoWordFixture()
        fx.speaker.status.value = pl.literusie.media.SpeechStatus.NO_POLISH_VOICE
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Learning(LearnMode.CONTINUE)) }
        settle()
        onNodeWithTag("voice_warning").assertIsDisplayed()
        typeKeys(caption())
        onNodeWithTag("message").assertTextEquals("Brawo!")
    }

    @Test fun `powtarzanie bez poznanych slow pokazuje przyjazna informacje`() = runDesktopComposeUiTest(1920, 1080) {
        val fx = twoWordFixture()
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Learning(LearnMode.REVIEW)) }
        settle()
        onNodeWithTag("empty_back").assertIsDisplayed()
    }

    @Test fun `wznowienie po ponownym uruchomieniu - kolejne uruchomienie zaczyna od nowych slow`() = runDesktopComposeUiTest(1920, 1080) {
        val fx = twoWordFixture()
        runBlocking {
            fx.store.save(pl.literusie.domain.learning.Mastery.afterCompletion(null, "kot", 0, fx.clock.now()))
        }
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Learning(LearnMode.CONTINUE)) }
        settle()
        onNodeWithTag("caption").assertTextEquals("LIS")
    }
}
