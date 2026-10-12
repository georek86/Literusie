package pl.slowomistrz.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.slowomistrz.app.ui.components.TvInteractiveButton
import pl.slowomistrz.app.ui.theme.BackgroundDark
import pl.slowomistrz.app.ui.theme.PastelGreen
import pl.slowomistrz.app.ui.theme.PrimaryBlue
import pl.slowomistrz.app.ui.theme.TextPrimary
import pl.slowomistrz.app.ui.theme.TextSecondary

@Composable
fun MainMenuScreen(
    onStartLearning: () -> Unit,
    onChooseCategory: () -> Unit,
    onRepeatLearned: () -> Unit,
    onViewProgress: () -> Unit
) {
    val startFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        startFocusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 64.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tytuł i podtytuł aplikacji
            Text(
                text = "LITERUSIE",
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                color = PrimaryBlue,
                letterSpacing = 6.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Edukacyjna przygoda z polskimi słowami i klawiaturą",
                fontSize = 26.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Przyciski menu głównego dostosowane do pilota TV
            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvInteractiveButton(
                    text = "Zaczynamy",
                    icon = Icons.Default.PlayArrow,
                    isPrimary = true,
                    onClick = onStartLearning,
                    modifier = Modifier.focusRequester(startFocusRequester)
                )

                TvInteractiveButton(
                    text = "Wybierz kategorię",
                    icon = Icons.Default.Category,
                    onClick = onChooseCategory
                )

                TvInteractiveButton(
                    text = "Powtarzamy",
                    icon = Icons.Default.Refresh,
                    onClick = onRepeatLearned
                )

                TvInteractiveButton(
                    text = "Postępy",
                    icon = Icons.Default.BarChart,
                    onClick = onViewProgress
                )
            }
        }
    }
}
