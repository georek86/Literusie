package pl.literusie.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import pl.literusie.domain.learning.LearningSession
import pl.literusie.domain.learning.LessonPlanner
import pl.literusie.domain.text.PolishNormalizer
import pl.literusie.domain.typing.TypingResult
import pl.literusie.domain.typing.TypingState
import pl.literusie.media.SpeechStatus
import pl.literusie.ui.app.AppDeps
import pl.literusie.ui.app.LearnMode
import pl.literusie.ui.app.Screen
import pl.literusie.ui.input.HeldKeys
import pl.literusie.ui.components.BackIcon
import pl.literusie.ui.components.LetterSlots
import pl.literusie.ui.components.PillButton
import pl.literusie.ui.components.SpeakerIcon
import pl.literusie.ui.platform.WordImage
import pl.literusie.ui.input.requestWhenReady
import pl.literusie.ui.theme.Palette

/** Pauza po poprawnym słowie: wystarczająco długa, by zobaczyć sukces, wystarczająco krótka, by nie nudzić. */
const val SUCCESS_PAUSE_MS = 1500L

private sealed interface Load {
    data object Loading : Load
    data object Empty : Load
    class Ready(val session: LearningSession) : Load
}

/** Buduje rundę (planer + zapisane postępy) i pokazuje ekran nauki albo przyjazną informację, gdy nie ma słów. */
@Composable
fun LearningHost(spec: Screen.Learning, deps: AppDeps, onExit: () -> Unit, onAnotherRound: () -> Unit) {
    val load by produceState<Load>(Load.Loading, spec) {
        // Brak dostępu do bazy postępów nie może blokować nauki – wtedy uczymy się bez zapisu.
        val progress = try { deps.store.loadAll() } catch (e: CancellationException) { throw e } catch (e: Exception) { emptyMap() }
        val planner = LessonPlanner(deps.catalog.words)
        val now = deps.clock.now()
        val words = when (spec.mode) {
            LearnMode.CONTINUE -> planner.next(progress, now)
            LearnMode.CATEGORY -> planner.category(spec.category.orEmpty(), progress)
            LearnMode.REVIEW -> planner.review(progress, now)
        }
        value = if (words.isEmpty()) Load.Empty else Load.Ready(LearningSession(words, deps.store, deps.clock, progress))
    }
    when (val l = load) {
        Load.Loading -> Box(Modifier.fillMaxSize())
        Load.Empty -> EmptyReview(onExit)
        is Load.Ready -> LearningScreen(
            session = l.session,
            deps = deps,
            title = spec.category?.replaceFirstChar { it.uppercase() }
                ?: if (spec.mode == LearnMode.REVIEW) "Powtarzamy" else "Zaczynamy",
            onExit = onExit,
            onAnotherRound = onAnotherRound,
        )
    }
}

@Composable
private fun EmptyReview(onExit: () -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestWhenReady() }
    Column(Modifier.fillMaxSize().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        WordImage("e_1f9e0", Modifier.size(120.dp), null)
        Spacer(Modifier.height(20.dp))
        Text("Na razie nie ma czego powtarzać", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text("Najpierw poznaj kilka słów w trybie „Zaczynamy”.", style = MaterialTheme.typography.bodyLarge, color = Palette.InkSoft)
        Spacer(Modifier.height(28.dp))
        PillButton("Wróć do menu", onExit, primary = true, focusRequester = focus, modifier = Modifier.testTag("empty_back"))
    }
}

@Composable
fun LearningScreen(
    session: LearningSession,
    deps: AppDeps,
    title: String,
    onExit: () -> Unit,
    onAnotherRound: () -> Unit,
) {
    val state by session.state.collectAsState()
    val speech by deps.speaker.status.collectAsState()
    val typing = state.typing
    val root = remember { FocusRequester() }
    val again = remember { FocusRequester() }
    // Klawisze wciśnięte i jeszcze niezwolnione – ignorujemy autopowtarzanie, by przytrzymanie nie wpisywało rzędu liter.
    val held = remember { HeldKeys<Key>() }

    LaunchedEffect(Unit) { root.requestWhenReady() }
    LaunchedEffect(state.roundFinished) { if (state.roundFinished) again.requestWhenReady() }

    // Nowe słowo → wymowa (poprzednia wypowiedź jest przerywana przez Speaker).
    LaunchedEffect(typing?.word?.id) { typing?.let { deps.speaker.speak(it.word.spoken) } }

    // Słowo ukończone → krótka pauza → zapis postępu i następne słowo.
    LaunchedEffect(typing?.finished, typing?.word?.id) {
        if (typing?.finished == true) {
            deps.sounds.success()
            delay(SUCCESS_PAUSE_MS)
            session.completeWordAndAdvance()
        }
    }

    fun onKey(e: KeyEvent): Boolean {
        if (e.type == KeyEventType.KeyUp) { held.up(e.key); return false }
        if (e.type != KeyEventType.KeyDown) return false
        val fresh = held.down(e.key)
        if (e.key == Key.Escape) { onExit(); return true }
        if (e.key == Key.Backspace) { if (fresh) session.onBackspace(); return true }
        if (e.isMetaPressed || (e.isCtrlPressed && !e.isAltPressed)) return false // skróty systemowe
        val cp = e.utf16CodePoint
        if (cp != 0 && Character.isLetter(cp)) {
            if (fresh && session.onLetter(cp.toChar()) is TypingResult.Wrong) deps.sounds.mistake()
            return true
        }
        return false // strzałki, OK, Tab, Fn… – zwykła nawigacja pilotem
    }

    BoxWithConstraints(
        Modifier.fillMaxSize()
            .focusRequester(root)
            .focusable()
            .onKeyEvent(::onKey)
            .padding(horizontal = 48.dp, vertical = 27.dp),
    ) {
        val screenHeight = maxHeight
        Column(Modifier.fillMaxSize()) {
            TopBar(title, state.index, state.total, onExit)
            Spacer(Modifier.height(12.dp))
            if (typing == null) {
                RoundDone(state.total, again, onAnotherRound, onExit)
            } else {
                Stage(typing, deps, screenHeight, Modifier.weight(1f))
            }
            if (speech != SpeechStatus.READY && speech != SpeechStatus.STARTING) VoiceWarning()
        }
    }
}

@Composable
private fun TopBar(title: String, index: Int, total: Int, onExit: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        PillButton("Wróć", onExit, icon = { BackIcon(Palette.Ink, 24.dp) }, height = 48.dp, modifier = Modifier.testTag("back"))
        Spacer(Modifier.width(20.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = Palette.InkSoft, maxLines = 1)
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            for (i in 0 until total) {
                Box(
                    Modifier.size(if (i == index) 18.dp else 14.dp).clip(CircleShape)
                        .background(
                            when {
                                i < index -> Palette.Mint
                                i == index -> Palette.Sky
                                else -> Palette.Line
                            }
                        ),
                )
            }
        }
    }
}

/** Główna kompozycja: ilustracja + podpis po lewej/prawej, pod spodem pola liter. */
@Composable
private fun Stage(typing: TypingState, deps: AppDeps, availableHeight: Dp, modifier: Modifier) {
    val picture = (availableHeight * 0.50f).coerceAtMost(300.dp)
    val done = typing.finished
    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        // Podpis dopasowuje wielkość do długości słowa, żeby nigdy nie wychodził poza ekran.
        val textWidth = maxWidth - picture - 48.dp
        val captionSize = (textWidth.value / (typing.length * 0.80f)).coerceIn(40f, 84f).sp
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Box(
                    Modifier.size(picture).clip(RoundedCornerShape(40.dp)).background(Palette.Paper)
                        .border(3.dp, if (done) Palette.Mint else Palette.Line, RoundedCornerShape(40.dp))
                        .testTag("illustration"),
                    contentAlignment = Alignment.Center,
                ) {
                    Crossfade(typing.word.imageId, animationSpec = tween(220), label = "picture") { id ->
                        WordImage(id, Modifier.size(picture * 0.78f), typing.word.text.lowercase())
                    }
                }
                Spacer(Modifier.width(48.dp))
                Column(Modifier.width(textWidth), verticalArrangement = Arrangement.Center) {
                    Text(
                        typing.word.text,
                        fontSize = captionSize, fontWeight = FontWeight.Black, letterSpacing = 2.sp, maxLines = 1, softWrap = false,
                        color = if (done) Palette.Mint else Palette.Ink,
                        modifier = Modifier.testTag("caption"),
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PillButton(
                            "Posłuchaj", { deps.speaker.speak(typing.word.spoken) },
                            icon = { SpeakerIcon(Palette.Sky, 26.dp) }, height = 48.dp,
                            modifier = Modifier.testTag("replay"),
                        )
                        Spacer(Modifier.width(18.dp))
                        Text("${typing.typed} z ${typing.length} liter", style = MaterialTheme.typography.bodyLarge, color = Palette.InkSoft,
                            modifier = Modifier.testTag("progress_text"))
                    }
                    Spacer(Modifier.height(12.dp))
                    val (msg, color) = message(typing, deps.settings.hints)
                    Text(msg, style = MaterialTheme.typography.titleMedium, color = color, modifier = Modifier.testTag("message"))
                }
            }
            Spacer(Modifier.height(24.dp))
            LetterSlots(typing, maxSlot = (availableHeight * 0.15f).coerceAtMost(80.dp), showHint = deps.settings.hints)
        }
    }
}

private fun message(t: TypingState, hints: Boolean): Pair<String, androidx.compose.ui.graphics.Color> = when {
    t.finished -> "Brawo!" to Palette.Mint
    t.streakErrors > 0 -> {
        val expected = t.expected?.let { PolishNormalizer.normalizeChar(it) }
        (if (hints && expected != null) "Prawie! Poszukaj literki $expected" else "Spróbuj jeszcze raz") to Palette.Peach
    }
    else -> "Pisz literki na klawiaturze" to Palette.InkSoft
}

/** Spokojna informacja dla dorosłego (nie dla dziecka) – nauka działa dalej bez głosu. */
@Composable
private fun VoiceWarning() {
    Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
        Text(
            "Brak polskiego głosu – poproś dorosłego o ustawienie (Postępy → Dla rodziców)",
            fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Palette.Ink,
            modifier = Modifier.testTag("voice_warning").clip(RoundedCornerShape(20.dp))
                .background(Palette.PeachSoft).padding(horizontal = 18.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun RoundDone(total: Int, again: FocusRequester, onAnotherRound: () -> Unit, onExit: () -> Unit) {
    Column(Modifier.fillMaxSize().testTag("round_done"), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        WordImage("e_1f3c6", Modifier.size(150.dp), null)
        Spacer(Modifier.height(12.dp))
        Text("Brawo!", style = MaterialTheme.typography.displayMedium, color = Palette.Mint)
        Text("Napisane słowa: $total", style = MaterialTheme.typography.titleMedium, color = Palette.InkSoft)
        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            PillButton("Jeszcze raz", onAnotherRound, primary = true, focusRequester = again, modifier = Modifier.testTag("again"))
            PillButton("Menu", onExit, modifier = Modifier.testTag("menu"))
        }
    }
}
