package pl.literusie.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.Density
import pl.literusie.data.WordParser
import pl.literusie.domain.learning.Clock
import pl.literusie.domain.learning.InMemoryProgressStore
import pl.literusie.domain.model.Word
import pl.literusie.domain.model.WordCatalog
import pl.literusie.media.FakeSpeaker
import pl.literusie.media.InMemorySettings
import pl.literusie.media.NoSoundEffects
import pl.literusie.ui.app.AppDeps
import java.io.File

val ASSETS = File(System.getProperty("literusie.assets") ?: "../../app/src/main/assets")

fun realCatalog(): WordCatalog = WordCatalog(WordParser.parse(File(ASSETS, "words_database.json").readText()).words)

class Fixture(
    val catalog: WordCatalog = realCatalog(),
    val store: InMemoryProgressStore = InMemoryProgressStore(),
    val speaker: FakeSpeaker = FakeSpeaker(),
    val settings: InMemorySettings = InMemorySettings(),
    val clock: Clock = Clock { 1_700_000_000_000L },
) {
    val deps get() = AppDeps(catalog, store, speaker, NoSoundEffects, settings, clock)
}

fun catalogOf(vararg words: Word) = WordCatalog(words.toList())

private val LETTER_KEYS = mapOf(
    'A' to Key.A, 'B' to Key.B, 'C' to Key.C, 'D' to Key.D, 'E' to Key.E, 'F' to Key.F, 'G' to Key.G, 'H' to Key.H,
    'I' to Key.I, 'J' to Key.J, 'K' to Key.K, 'L' to Key.L, 'M' to Key.M, 'N' to Key.N, 'O' to Key.O, 'P' to Key.P,
    'Q' to Key.Q, 'R' to Key.R, 'S' to Key.S, 'T' to Key.T, 'U' to Key.U, 'V' to Key.V, 'W' to Key.W, 'X' to Key.X,
    'Y' to Key.Y, 'Z' to Key.Z,
)

/** Wciska kolejne klawisze ASCII tak, jak zrobiłaby to fizyczna klawiatura (down + up dla każdego). */
@OptIn(ExperimentalTestApi::class)
fun SemanticsNodeInteractionsProvider.typeKeys(text: String) {
    for (c in text.uppercase()) onRoot().performKeyInput { pressKey(LETTER_KEYS.getValue(c)) }
}

@OptIn(ExperimentalTestApi::class)
fun SemanticsNodeInteractionsProvider.press(key: Key) {
    onRoot().performKeyInput { pressKey(key) }
}

/** Ekran telewizora: 1920×1080 px przy gęstości 2.0 → 960×540 dp. */
val TV_DENSITY = Density(2f)

@OptIn(ExperimentalTestApi::class)
fun DesktopComposeUiTest.setTv(content: @androidx.compose.runtime.Composable () -> Unit) = setContent {
    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides TV_DENSITY) {
        testFocusManager = androidx.compose.ui.platform.LocalFocusManager.current
        content()
    }
}

/** Klawisz + chwila na przetworzenie (zegar testowy jest ręczny). */
@OptIn(ExperimentalTestApi::class)
fun DesktopComposeUiTest.press(key: Key) {
    onRoot().performKeyInput { pressKey(key) }
    mainClock.advanceTimeBy(50); waitForIdle()
}

@OptIn(ExperimentalTestApi::class)
fun DesktopComposeUiTest.typeKeys(text: String) {
    for (c in text.uppercase()) press(LETTER_KEYS.getValue(c))
}

/** Ekran gotowy: efekty startowe (np. ustawienie fokusu) zdążyły się wykonać. */
@OptIn(ExperimentalTestApi::class)
fun DesktopComposeUiTest.settle(ms: Long = 600) {
    mainClock.autoAdvance = false
    waitForIdle(); mainClock.advanceTimeBy(ms); waitForIdle()
}

/**
 * Menedżer fokusu aktualnej kompozycji. Na prawdziwym Androidzie strzałki pilota przesuwają fokus systemowo;
 * w środowisku desktopowym robimy to jawnie, sprawdzając w ten sposób, czy GRAF fokusu (sąsiedzi) jest poprawny.
 */
lateinit var testFocusManager: androidx.compose.ui.focus.FocusManager

@OptIn(ExperimentalTestApi::class)
fun DesktopComposeUiTest.dpad(direction: androidx.compose.ui.focus.FocusDirection) {
    runOnIdle { testFocusManager.moveFocus(direction) }
    mainClock.advanceTimeBy(100); waitForIdle()
}

/** Ta sama czcionka co w aplikacji (Nunito) – zrzuty i testy układu mają realne szerokości tekstu. */
val NUNITO: androidx.compose.ui.text.font.FontFamily = run {
    fun f(name: String, w: androidx.compose.ui.text.font.FontWeight) =
        androidx.compose.ui.text.platform.Font(File(ASSETS, "../res/font/$name.ttf"), w)
    androidx.compose.ui.text.font.FontFamily(
        f("nunito_semibold", androidx.compose.ui.text.font.FontWeight.Normal),
        f("nunito_semibold", androidx.compose.ui.text.font.FontWeight.SemiBold),
        f("nunito_extrabold", androidx.compose.ui.text.font.FontWeight.Bold),
        f("nunito_extrabold", androidx.compose.ui.text.font.FontWeight.ExtraBold),
        f("nunito_black", androidx.compose.ui.text.font.FontWeight.Black),
    )
}
