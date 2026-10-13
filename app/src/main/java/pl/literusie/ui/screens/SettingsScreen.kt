package pl.literusie.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.literusie.domain.model.Word
import pl.literusie.domain.typing.TypingEngine
import pl.literusie.domain.typing.TypingState
import pl.literusie.media.SpeechStatus
import pl.literusie.ui.app.AppDeps
import pl.literusie.ui.components.BackIcon
import pl.literusie.ui.components.LetterSlots
import pl.literusie.ui.components.PillButton
import pl.literusie.ui.input.requestWhenReady
import pl.literusie.ui.theme.Palette

/**
 * Bramka przed ustawieniami: trzeba wpisać słowo z klawiatury. Dziecko, które przypadkiem
 * naciśnie kilka klawiszy, nie wejdzie do ustawień ani nie skasuje postępów.
 */
@Composable
fun ParentGateScreen(onPassed: () -> Unit, onBack: () -> Unit) {
    val engine = remember { TypingEngine(Word("gate", "OPIEKUN", "", null, 1, "")) }
    var state by remember { mutableStateOf(engine.state) }
    val root = remember { FocusRequester() }
    LaunchedEffect(Unit) { root.requestWhenReady() }
    LaunchedEffect(state.finished) { if (state.finished) onPassed() }

    Column(
        Modifier.fillMaxSize().padding(48.dp)
            .focusRequester(root).focusable()
            .onKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown) return@onKeyEvent false
                when {
                    e.key == Key.Escape -> { onBack(); true }
                    e.key == Key.Backspace -> { engine.backspace(); state = engine.state; true }
                    e.utf16CodePoint != 0 && Character.isLetter(e.utf16CodePoint) -> { engine.type(e.utf16CodePoint.toChar()); state = engine.state; true }
                    else -> false
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Text("Tylko dla dorosłych", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Aby wejść do ustawień, wpisz na klawiaturze słowo:", style = MaterialTheme.typography.bodyLarge, color = Palette.InkSoft)
        Spacer(Modifier.height(8.dp))
        Text("OPIEKUN", fontSize = 40.sp, color = Palette.Ink, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))
        LetterSlots(state, maxSlot = 64.dp, showHint = false, modifier = Modifier.testTag("gate_slots"))
        Spacer(Modifier.height(32.dp))
        PillButton("Wróć", onBack, icon = { BackIcon(Palette.Ink) }, modifier = Modifier.testTag("back"))
    }
}

@Composable
fun SettingsScreen(deps: AppDeps, onClearProgress: () -> Unit, onBack: () -> Unit) {
    var effects by remember { mutableStateOf(deps.settings.soundEffects) }
    var hints by remember { mutableStateOf(deps.settings.hints) }
    var confirmClear by remember { mutableStateOf(false) }
    val speech by deps.speaker.status.collectAsState()
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { first.requestWhenReady() }

    Column(Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 28.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PillButton("Wróć", onBack, icon = { BackIcon(Palette.Ink) }, focusRequester = first, modifier = Modifier.testTag("back"))
            Spacer(Modifier.width(24.dp))
            Text("Dla rodziców", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(24.dp))
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SettingRow("Cichy dźwięk po poprawnym słowie", effects, "setting_effects") {
                effects = !effects; deps.settings.soundEffects = effects
            }
            SettingRow("Podpowiedzi przy błędnej literze", hints, "setting_hints") {
                hints = !hints; deps.settings.hints = hints
            }
            val voiceText = when (speech) {
                SpeechStatus.READY -> "Polski głos: działa"
                SpeechStatus.STARTING -> "Polski głos: uruchamianie…"
                SpeechStatus.NO_POLISH_VOICE -> "Polski głos: brak – zainstaluj go w ustawieniach telewizora"
                SpeechStatus.UNAVAILABLE -> "Odczytywanie słów: silnik mowy niedostępny"
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(voiceText, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).testTag("voice_status"))
                if (speech == SpeechStatus.NO_POLISH_VOICE || speech == SpeechStatus.UNAVAILABLE) {
                    PillButton("Ustaw głos", deps.openVoiceSettings, modifier = Modifier.testTag("voice_settings"))
                }
            }
            if (deps.packs != null) PacksRow(deps)
            PillButton(
                if (confirmClear) "Na pewno? Naciśnij jeszcze raz" else "Wyczyść postępy dziecka",
                { if (confirmClear) { onClearProgress(); confirmClear = false } else confirmClear = true },
                modifier = Modifier.testTag("clear_progress"),
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            "Literusie działa bez internetu, bez reklam i bez kont. Postępy zapisują się tylko na tym urządzeniu.\n" +
                "Ilustracje: Twemoji (CC-BY 4.0); piktogramy ARASAAC (Gobierno de Aragón, autor: Sergio Palao, CC BY-NC-SA 4.0 – tylko użytek niekomercyjny). Czcionka: Nunito (SIL OFL 1.1).",
            fontSize = 16.sp, color = Palette.InkSoft, textAlign = TextAlign.Start,
        )
    }
}

@Composable
private fun SettingRow(label: String, on: Boolean, tag: String, onToggle: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        PillButton(if (on) "Włączone" else "Wyłączone", onToggle, primary = on, modifier = Modifier.testTag(tag))
    }
}

/** Pobieranie dodatkowych słówek – wyłącznie tutaj (za bramką dla dorosłych), nigdy podczas nauki. */
@Composable
private fun PacksRow(deps: AppDeps) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var status by remember { mutableStateOf("Dodatkowe słówka: pobierane z internetu (ARASAAC), tylko na Twoje żądanie") }
    var busy by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(status, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).testTag("packs_status"))
        PillButton(
            if (busy) "Pobieranie…" else "Pobierz słówka",
            {
                if (!busy) {
                    busy = true; status = "Pobieranie…"
                    scope.launch {
                        when (val r = deps.packs!!.update()) {
                            is pl.literusie.data.packs.PackResult.Installed -> { status = "Zainstalowano pakiety: ${r.packs}"; deps.onPacksChanged() }
                            pl.literusie.data.packs.PackResult.UpToDate -> status = "Masz już najnowsze słówka"
                            is pl.literusie.data.packs.PackResult.Failed -> status = "Nie udało się pobrać (${r.reason}). Nauka działa dalej."
                        }
                        busy = false
                    }
                }
            },
            modifier = Modifier.testTag("packs_download"),
        )
    }
}
