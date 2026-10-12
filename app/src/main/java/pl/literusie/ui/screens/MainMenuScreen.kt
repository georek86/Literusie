package pl.literusie.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.literusie.domain.learning.WordProgress
import pl.literusie.domain.model.WordCatalog
import pl.literusie.ui.components.TvCard
import pl.literusie.ui.platform.WordImage
import pl.literusie.ui.input.requestWhenReady
import pl.literusie.ui.theme.Palette

@Composable
fun MainMenuScreen(
    catalog: WordCatalog,
    progress: Map<String, WordProgress>,
    onStart: () -> Unit,
    onCategories: () -> Unit,
    onReview: () -> Unit,
    onProgress: () -> Unit,
) {
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { first.requestWhenReady() }
    val learned = progress.values.count { it.completions > 0 }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        Wordmark()
        Spacer(Modifier.height(8.dp))
        Text("Poznajemy słowa: patrzę, słucham, piszę", style = MaterialTheme.typography.bodyLarge, color = Palette.InkSoft)
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            MenuTile("Zaczynamy", "e_1f680", Palette.SkySoft, onStart, "menu_start", first)
            MenuTile("Wybierz kategorię", "e_1f9e9", Palette.SunSoft, onCategories, "menu_categories")
            MenuTile("Powtarzamy", "e_1f9e0", Palette.MintSoft, onReview, "menu_review")
            MenuTile("Postępy", "e_1f3c6", Palette.LilacSoft, onProgress, "menu_progress")
        }
        Spacer(Modifier.weight(1f))
        Text(
            "Poznane słowa: $learned z ${catalog.words.size}",
            style = MaterialTheme.typography.bodyMedium, color = Palette.InkSoft,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Ilustracje: Twemoji, © Twitter, Inc. i współtwórcy, CC-BY 4.0",
            fontSize = 14.sp, color = Palette.InkSoft.copy(alpha = .8f),
        )
    }
}

/** Logo z kolorowych kafelków z literami – spójne z polami liter na ekranie nauki. */
@Composable
private fun Wordmark() {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        "LITERUSIE".forEachIndexed { i, c ->
            Box(
                Modifier.size(width = 54.dp, height = 66.dp).clip(RoundedCornerShape(16.dp))
                    .background(Palette.Accents[i % Palette.Accents.size]),
                contentAlignment = Alignment.Center,
            ) {
                Text(c.toString(), fontSize = 42.sp, fontWeight = FontWeight.Black, color = Palette.Paper)
            }
        }
    }
}

@Composable
private fun MenuTile(
    label: String, imageId: String, tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit, tag: String, focusRequester: FocusRequester? = null,
) {
    TvCard(
        onClick = onClick,
        modifier = Modifier.size(width = 200.dp, height = 232.dp).testTag(tag),
        focusRequester = focusRequester,
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(Modifier.height(6.dp))
            Box(Modifier.size(112.dp).clip(CircleShape).background(tint), contentAlignment = Alignment.Center) {
                WordImage(imageId, Modifier.size(76.dp), null)
            }
            Spacer(Modifier.height(10.dp))
            // Stała wysokość etykiety (2 linie), żeby ikony we wszystkich kafelkach były na tej samej wysokości.
            Box(Modifier.height(64.dp), contentAlignment = Alignment.Center) {
                Text(label, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            }
        }
    }
}
