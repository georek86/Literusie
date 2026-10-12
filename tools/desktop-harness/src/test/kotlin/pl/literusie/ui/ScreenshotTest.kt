package pl.literusie.ui

import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import kotlinx.coroutines.runBlocking
import org.junit.Test
import pl.literusie.domain.learning.Mastery
import pl.literusie.ui.app.LearnMode
import pl.literusie.ui.app.LiterusieApp
import pl.literusie.ui.app.Screen
import pl.literusie.w
import java.io.File
import javax.imageio.ImageIO

/** Zrzuty ekranów 1920×1080 do przeglądu wyglądu (build/shots). To także test, że każdy ekran się renderuje. */
@OptIn(ExperimentalTestApi::class)
class ScreenshotTest {
    private val dir = File(System.getProperty("literusie.shots") ?: "build/shots").also { it.mkdirs() }

    private fun DesktopComposeUiTest.shot(name: String) {
        mainClock.advanceTimeBy(700); waitForIdle()
        ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", File(dir, "$name.png"))
    }

    private fun withData(fx: Fixture) = runBlocking {
        fx.catalog.words.filter { it.category == "zwierzęta" }.take(30).forEachIndexed { i, w ->
            var p = Mastery.afterCompletion(null, w.id, 0, 1_700_000_000_000)
            if (i < 12) repeat(2) { p = Mastery.afterCompletion(p, w.id, 0, 1_700_000_000_000) }
            fx.store.save(p)
        }
        fx.catalog.words.filter { it.category == "jedzenie i napoje" }.take(9).forEach {
            fx.store.save(Mastery.afterCompletion(null, it.id, 1, 1_700_000_000_000))
        }
    }

    @Test fun menu() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture().also(::withData)
        setTv { LiterusieApp(fx.deps, font = NUNITO) }; settle(); shot("01_menu")
    }

    @Test fun categories() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture().also(::withData)
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Categories) }; settle(); shot("02_categories")
        repeat(3) { dpad(FocusDirection.Down) }; dpad(FocusDirection.Right); shot("02b_categories_scrolled")
    }

    @Test fun learning() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture(catalog = pl.literusie.ui.realCatalog())
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Learning(LearnMode.CATEGORY, "zwierzęta")) }; settle()
        shot("03_learning_start")
        typeKeys("b"); shot("03b_learning_1")           // pierwsze słowo w kategorii: BÓBR
        typeKeys("x"); shot("03c_learning_error")
        typeKeys("x"); shot("03d_learning_hint")
        typeKeys("obr"); shot("03e_learning_done")
    }

    @Test fun longWord() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture(catalog = catalogOf(w("sh", "SUPERBOHATER", "ludzie i rodzina", 4, "e_1f9b8")))
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Learning(LearnMode.CONTINUE)) }; settle()
        typeKeys("super"); shot("04_long_word")
    }

    @Test fun polishLetters() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture(catalog = catalogOf(w("zb", "ŻABA", "zwierzęta", 1, "e_1f438")))
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Learning(LearnMode.CONTINUE)) }; settle()
        typeKeys("za"); shot("05_zaba")
    }

    @Test fun roundDone() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture(catalog = catalogOf(w("k", "KOT", "zwierzęta", 1, "e_1f408")))
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Learning(LearnMode.CONTINUE)) }; settle()
        typeKeys("kot"); mainClock.advanceTimeBy(1600); waitForIdle(); shot("06_round_done")
    }

    @Test fun progress() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture().also(::withData)
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Progress) }; settle(); shot("07_progress")
    }

    @Test fun settingsAndGate() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture()
        fx.speaker.status.value = pl.literusie.media.SpeechStatus.NO_POLISH_VOICE
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.ParentGate) }; settle()
        typeKeys("opi"); shot("08_gate")
        typeKeys("ekun"); mainClock.advanceTimeBy(800); waitForIdle(); shot("09_settings")
    }

    @Test fun noVoice() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture()
        fx.speaker.status.value = pl.literusie.media.SpeechStatus.NO_POLISH_VOICE
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Learning(LearnMode.CATEGORY, "emocje")) }; settle(); shot("10_no_voice")
    }
}
