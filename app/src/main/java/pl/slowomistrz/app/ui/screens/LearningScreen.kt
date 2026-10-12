package pl.slowomistrz.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import pl.slowomistrz.app.domain.engine.LearningSessionManager
import pl.slowomistrz.app.media.PolishTtsManager
import pl.slowomistrz.app.ui.components.TvInteractiveButton
import pl.slowomistrz.app.ui.components.WordIllustration
import pl.slowomistrz.app.ui.components.WordLetterSlotsRow
import pl.slowomistrz.app.ui.theme.AccentYellow
import pl.slowomistrz.app.ui.theme.BackgroundDark
import pl.slowomistrz.app.ui.theme.PrimaryBlue
import pl.slowomistrz.app.ui.theme.SoftError
import pl.slowomistrz.app.ui.theme.SuccessGreen
import pl.slowomistrz.app.ui.theme.TextPrimary
import pl.slowomistrz.app.ui.theme.TextSecondary

@Composable
fun LearningScreen(
    sessionManager: LearningSessionManager,
    ttsManager: PolishTtsManager,
    onBackToMenu: () -> Unit
) {
    val inputState by sessionManager.inputState.collectAsState()
    val currentWord = sessionManager.currentWord ?: return

    val focusRequester = remember { FocusRequester() }

    // Automatyczne odtworzenie wymowy przy nowym słowie
    LaunchedEffect(currentWord.id) {
        ttsManager.speakWord(currentWord.word)
        focusRequester.requestFocus()
    }

    // Automatyczne przejście do następnego słowa po krótkiej pauzie sukcesu
    LaunchedEffect(inputState.isWordCompleted) {
        if (inputState.isWordCompleted) {
            delay(1200) // Spokojna pauza edukacyjna
            sessionManager.completeCurrentWordAndAdvance()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                // Obsługa klawiatury fizycznej USB/Bluetooth
                if (keyEvent.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) {
                    when (keyEvent.key) {
                        Key.Backspace -> {
                            sessionManager.onBackspace()
                            true
                        }
                        Key.Escape, Key.Back -> {
                            onBackToMenu()
                            true
                        }
                        else -> {
                            val codePoint = keyEvent.nativeKeyEvent.unicodeChar
                            if (codePoint != 0) {
                                val ch = codePoint.toChar()
                                if (ch.isLetter()) {
                                    sessionManager.onCharTyped(ch)
                                    true
                                } else false
                            } else false
                        }
                    }
                } else false
            }
            .padding(horizontal = 48.dp, vertical = 32.dp)
    ) {
        // Górny pasek informacyjny (kategoria, postęp, przycisk powrotu)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TvInteractiveButton(
                text = "Powrót",
                icon = Icons.Default.ArrowBack,
                onClick = onBackToMenu
            )

            // Pasek postępu
            Text(
                text = "Słowo ${sessionManager.currentWordIndex + 1} z ${sessionManager.totalWordsCount} • ${currentWord.category.uppercase()}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )

            // Przycisk powtórzenia wymowy
            TvInteractiveButton(
                text = "Posłuchaj",
                icon = Icons.Default.VolumeUp,
                onClick = { ttsManager.speakWord(currentWord.word) }
            )
        }

        // Główna sekcja centralna (Ilustracja po lewej, Wpisywanie po prawej)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lewa kolumna: Duża ilustracja
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                WordIllustration(
                    imageId = currentWord.imageId,
                    wordLabel = currentWord.word,
                    size = 400.dp
                )
            }

            Spacer(modifier = Modifier.width(64.dp))

            // Prawa kolumna: Podpis, kafelki liter i informacja zwrotna
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Czytelny podpis w języku polskim
                Text(
                    text = currentWord.word,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryBlue,
                    letterSpacing = 6.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Pola kolejnych wpisywanych liter
                WordLetterSlotsRow(
                    inputState = inputState,
                    slotSize = 84.dp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Informacja zwrotna (sukces, błąd lub podpowiedź)
                Box(
                    modifier = Modifier.height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        inputState.isWordCompleted -> {
                            Text(
                                text = "Świetnie! Brawo!",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                        inputState.lastInputWasError -> {
                            Text(
                                text = inputState.errorMessage ?: "Spróbuj jeszcze raz!",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SoftError
                            )
                        }
                        else -> {
                            Text(
                                text = "Wpisz literkę na klawiaturze",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
