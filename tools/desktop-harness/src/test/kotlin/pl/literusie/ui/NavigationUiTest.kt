package pl.literusie.ui

import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.literusie.domain.learning.Mastery
import pl.literusie.ui.app.LearnMode
import pl.literusie.ui.app.LiterusieApp
import pl.literusie.ui.app.Screen
import pl.literusie.ui.screens.CATEGORY_COLUMNS

@OptIn(ExperimentalTestApi::class)
class NavigationUiTest {

    /** Zgłoszony błąd: po wejściu w „Wybierz kategorię” kategorie znikały i nie dało się ich wybrać. */
    @Test fun `kategorie nie znikaja z czasem i mozna je wybrac pilotem`() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture()
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Categories) }
        settle()
        val first = fx.catalog.categories.first().name
        onNodeWithTag("category_$first").assertIsDisplayed().assertIsFocused()
        for (c in fx.catalog.categories.take(8)) onNodeWithTag("category_${c.name}").assertIsDisplayed()

        // mija 30 sekund (sam ekran, bez żadnego wejścia) – nic nie może zniknąć
        repeat(300) { mainClock.advanceTimeBy(100) }
        waitForIdle()
        for (c in fx.catalog.categories.take(8)) onNodeWithTag("category_${c.name}").assertIsDisplayed()
        onNodeWithTag("category_$first").assertIsFocused()

        // strzałki: w prawo przenosi fokus na drugą kategorię
        dpad(FocusDirection.Right)
        onNodeWithTag("category_${fx.catalog.categories[1].name}").assertIsFocused()

        // OK/Enter otwiera naukę w tej kategorii
        press(Key.Enter)
        mainClock.advanceTimeBy(500); waitForIdle()
        onNodeWithTag("caption").assertIsDisplayed()
        // kolejność jest losowa: na ekranie ma być któreś słowo z wybranej kategorii
        assertTrue(caption() in fx.catalog.words.filter { it.inCategory(fx.catalog.categories[1].name) }.map { it.text })
    }

    @Test fun `do ostatniej kategorii da sie dojechac strzalkami i wybrac`() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture()
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Categories) }
        settle()
        val last = fx.catalog.categories.last().name
        repeat((fx.catalog.categories.size - 1) / CATEGORY_COLUMNS) { dpad(FocusDirection.Down) }
        repeat((fx.catalog.categories.size - 1) % CATEGORY_COLUMNS) { dpad(FocusDirection.Right) }
        waitForIdle()
        onNodeWithTag("category_$last").assertIsDisplayed().assertIsFocused()
    }

    @Test fun `menu glowne - fokus startuje na Zaczynamy, strzalki i OK dzialaja`() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture()
        setTv { LiterusieApp(fx.deps, font = NUNITO) }
        settle()
        onNodeWithTag("menu_start").assertIsFocused()
        dpad(FocusDirection.Right); onNodeWithTag("menu_categories").assertIsFocused()
        dpad(FocusDirection.Right); onNodeWithTag("menu_review").assertIsFocused()
        dpad(FocusDirection.Right); onNodeWithTag("menu_progress").assertIsFocused()
        press(Key.Enter)   // na Androidzie OK/DPAD_CENTER także wywołuje klik (clickable)
        mainClock.advanceTimeBy(500); waitForIdle()
        onNodeWithTag("known_count").assertTextEquals("0")
    }

    @Test fun `Esc w nauce wraca do menu`() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture()
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Learning(LearnMode.CONTINUE)) }
        settle()
        onNodeWithTag("caption").assertIsDisplayed()
        press(Key.Escape)
        mainClock.advanceTimeBy(500); waitForIdle()
        onNodeWithTag("menu_start").assertIsDisplayed()
    }

    @Test fun `przycisk Wroc dziala pilotem`() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture()
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Progress) }
        settle()
        onNodeWithTag("back").assertIsFocused()
        press(Key.Enter)
        mainClock.advanceTimeBy(500); waitForIdle()
        onNodeWithTag("menu_start").assertIsDisplayed()
    }

    @Test fun `postepy pokazuja zapisane slowa`() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture()
        runBlocking {
            val words = fx.catalog.words.take(10)
            words.take(6).forEach { fx.store.save(Mastery.afterCompletion(null, it.id, 0, 1_700_000_000_000)) }
            words.take(2).forEach {
                var p = Mastery.afterCompletion(null, it.id, 0, 1_700_000_000_000)
                repeat(2) { _ -> p = Mastery.afterCompletion(p, it.id, 0, 1_700_000_000_000) }
                fx.store.save(p)
            }
        }
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Progress) }
        settle()
        onNodeWithTag("known_count").assertTextEquals("6")
        onNodeWithTag("legend_mastered").assertTextEquals("2")
        onNodeWithTag("legend_practicing").assertTextEquals("4")
        onNodeWithTag("legend_new").assertTextEquals("${fx.catalog.words.size - 6}")
    }

    @Test fun `ustawienia sa za bramka - bledne slowo nie wpuszcza, poprawne tak`() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture()
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Progress) }
        settle()
        onNodeWithTag("parents").performClick()
        mainClock.advanceTimeBy(500); waitForIdle()
        onNodeWithTag("gate_slots").assertIsDisplayed()
        typeKeys("xyzdorosly")                       // przypadkowe klawisze
        mainClock.advanceTimeBy(500); waitForIdle()
        onNodeWithTag("gate_slots").assertIsDisplayed()
        typeKeys("opiekun")
        mainClock.advanceTimeBy(500); waitForIdle()
        onNodeWithTag("setting_effects").assertIsDisplayed()
    }

    @Test fun `ustawienia - przelaczniki zapisuja sie, czyszczenie wymaga potwierdzenia`() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture()
        runBlocking { fx.store.save(Mastery.afterCompletion(null, fx.catalog.words[0].id, 0, 1)) }
        setTv { LiterusieApp(fx.deps, font = NUNITO, initial = Screen.Settings) }
        settle()
        assertFalse("dzwieki domyslnie wylaczone", fx.settings.soundEffects)
        assertTrue(fx.settings.hints)
        onNodeWithTag("setting_effects").performClick()
        onNodeWithTag("setting_hints").performClick()
        assertTrue(fx.settings.soundEffects)
        assertFalse(fx.settings.hints)

        onNodeWithTag("clear_progress").performClick()          // pierwsze kliknięcie tylko prosi o potwierdzenie
        waitForIdle()
        assertEquals(1, runBlocking { fx.store.loadAll() }.size)
        onNodeWithTag("clear_progress").performClick()
        waitForIdle()
        assertEquals(0, runBlocking { fx.store.loadAll() }.size)
    }

    @Test fun `status glosu w ustawieniach i skrot do konfiguracji`() = runDesktopComposeUiTest(1920, 1080) {
        val fx = Fixture()
        fx.speaker.status.value = pl.literusie.media.SpeechStatus.NO_POLISH_VOICE
        var opened = 0
        setTv { LiterusieApp(AppDepsWithVoiceHook(fx) { opened++ }, font = NUNITO, initial = Screen.Settings) }
        settle()
        onNodeWithTag("voice_settings").performClick()
        assertEquals(1, opened)
    }

    private fun AppDepsWithVoiceHook(fx: Fixture, hook: () -> Unit) =
        pl.literusie.ui.app.AppDeps(fx.catalog, fx.store, fx.speaker, pl.literusie.media.NoSoundEffects, fx.settings, fx.clock, hook)
}
