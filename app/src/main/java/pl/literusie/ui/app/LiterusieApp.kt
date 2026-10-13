package pl.literusie.ui.app

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.launch
import pl.literusie.domain.learning.Clock
import pl.literusie.domain.learning.ProgressStore
import pl.literusie.domain.learning.WordProgress
import pl.literusie.domain.model.WordCatalog
import pl.literusie.media.SettingsStore
import pl.literusie.media.SoundEffects
import pl.literusie.media.Speaker
import pl.literusie.ui.screens.CategoryScreen
import pl.literusie.ui.screens.LearningHost
import pl.literusie.ui.screens.MainMenuScreen
import pl.literusie.ui.screens.ParentGateScreen
import pl.literusie.ui.screens.ProgressScreen
import pl.literusie.ui.screens.SettingsScreen
import pl.literusie.ui.theme.LiterusieTheme
import pl.literusie.ui.theme.Palette

/** Wszystko, czego potrzebuje interfejs – na Androidzie dostarcza to MainActivity, w testach atrapy. */
class AppDeps(
    val catalog: WordCatalog,
    val store: ProgressStore,
    val speaker: Speaker,
    val sounds: SoundEffects,
    val settings: SettingsStore,
    val clock: Clock = Clock.System,
    val openVoiceSettings: () -> Unit = {},
    /** Pobieranie dodatkowych pakietów słówek (null = funkcja wyłączona). */
    val packs: pl.literusie.data.packs.PackService? = null,
    /** Wołane po zainstalowaniu pakietów – host przeładowuje bazę słów. */
    val onPacksChanged: () -> Unit = {},
)

enum class LearnMode { CONTINUE, CATEGORY, REVIEW }

sealed interface Screen {
    data object Menu : Screen
    data object Categories : Screen
    data object Progress : Screen
    data object ParentGate : Screen
    data object Settings : Screen
    /** [round] zmienia się przy „Jeszcze raz”, żeby zbudować nową rundę. */
    data class Learning(val mode: LearnMode, val category: String? = null, val round: Int = 0) : Screen
}

/** Ekran, do którego prowadzi „wstecz” z danego ekranu. */
fun Screen.parent(): Screen? = when (this) {
    Screen.Menu -> null
    Screen.Settings, Screen.ParentGate -> Screen.Progress
    else -> Screen.Menu
}

@Composable
fun LiterusieApp(
    deps: AppDeps,
    font: FontFamily = FontFamily.SansSerif,
    initial: Screen = Screen.Menu,
    /** Na Androidzie: BackHandler przekazany z Activity (przycisk Wstecz na pilocie). */
    backHook: @Composable (enabled: Boolean, onBack: () -> Unit) -> Unit = { _, _ -> },
) {
    LiterusieTheme(font) {
        var screen by remember { mutableStateOf(initial) }
        var progress by remember { mutableStateOf<Map<String, WordProgress>>(emptyMap()) }
        val scope = rememberCoroutineScope()

        // Postępy odświeżamy po każdym powrocie z nauki; awaria bazy nie może zatrzymać aplikacji.
        LaunchedEffect(screen is Screen.Learning) {
            if (screen !is Screen.Learning) {
                progress = try { deps.store.loadAll() } catch (e: Exception) { emptyMap() }
            }
        }

        backHook(screen.parent() != null) { screen.parent()?.let { screen = it } }

        Box(Modifier.fillMaxSize().background(Palette.Cream)) {
            Crossfade(targetState = screen, animationSpec = tween(160), label = "screen") { s ->
                when (s) {
                    Screen.Menu -> MainMenuScreen(
                        catalog = deps.catalog,
                        progress = progress,
                        onStart = { screen = Screen.Learning(LearnMode.CONTINUE) },
                        onCategories = { screen = Screen.Categories },
                        onReview = { screen = Screen.Learning(LearnMode.REVIEW) },
                        onProgress = { screen = Screen.Progress },
                    )
                    Screen.Categories -> CategoryScreen(
                        catalog = deps.catalog,
                        progress = progress,
                        onSelect = { screen = Screen.Learning(LearnMode.CATEGORY, it) },
                        onBack = { screen = Screen.Menu },
                    )
                    Screen.Progress -> ProgressScreen(
                        catalog = deps.catalog,
                        progress = progress,
                        onBack = { screen = Screen.Menu },
                        onParents = { screen = Screen.ParentGate },
                    )
                    Screen.ParentGate -> ParentGateScreen(
                        onPassed = { screen = Screen.Settings },
                        onBack = { screen = Screen.Progress },
                    )
                    Screen.Settings -> SettingsScreen(
                        deps = deps,
                        onClearProgress = { scope.launch { try { deps.store.clear() } catch (_: Exception) {}; progress = emptyMap() } },
                        onBack = { screen = Screen.Progress },
                    )
                    is Screen.Learning -> LearningHost(
                        spec = s,
                        deps = deps,
                        onExit = { screen = Screen.Menu },
                        onAnotherRound = { screen = s.copy(round = s.round + 1) },
                    )
                }
            }
        }
    }
}
