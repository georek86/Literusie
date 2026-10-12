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
        onNodeWithTag("caption").assertTextEquals("KOT")
        onNodeWithTag("progress_text").assertTextEquals("0 z 3 liter")
        assertEquals(listOf("kot"), fx.speaker.spoken)           // wymowa małymi literami, raz

        typeKeys("k")
        onNodeWithTag("progress_text").assertTextEquals("1 z 3 liter")

        typeKeys("t")                                              // błąd – postęp zostaje
        onNodeWithTag("progress_text").assertTextEquals("1 z 3 liter")
        onNodeWithTag("message").assertTextEquals("Prawie! Poszukaj literki O")

        press(Key.Backspace)
        onNodeWithTag("progress_text").assertTextEquals("0 z 3 liter")

        typeKeys("kot")
        onNodeWithTag("message").assertTextEquals("Brawo!")

        // w czasie pauzy sukcesu przypadkowe klawisze nic nie wpisują do kolejnego słowa
        typeKeys("lis")
        mainClock.advanceTimeBy(1_600)
        settle()
        onNodeWithTag("caption").assertTextEquals("LIS")
        onNodeWithTag("progress_text").assertTextEquals("0 z 3 liter")
        assertEquals(listOf("kot", "lis"), fx.speaker.spoken)

        val saved = runBlocking { fx.store.loadAll() }.getValue("kot")
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
        typeKeys("ko")
        onNodeWithTag("replay").performClick()
        settle()
        assertEquals(listOf("kot", "kot"), fx.speaker.spoken)
        onNodeWithTag("progress_text").assertTextEquals("2 z 3 liter")
        typeKeys("t")
        onNodeWithTag("message").assertTextEquals("Brawo!")
    }

    @Test fun `brak polskiego glosu pokazuje informacje dla opiekuna, nauka dziala dalej`() = runDesktopComposeUiTest(1920, 1080) {
        val fx = twoWordFixture()
        fx.speaker.status.value = pl.literusie.media.SpeechStatus.NO_POLISH_VOICE
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Learning(LearnMode.CONTINUE)) }
        settle()
        onNodeWithTag("voice_warning").assertIsDisplayed()
        typeKeys("kot")
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
