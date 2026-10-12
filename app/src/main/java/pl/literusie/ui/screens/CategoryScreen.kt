package pl.literusie.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.literusie.domain.learning.WordProgress
import pl.literusie.domain.model.WordCatalog
import pl.literusie.ui.components.BackIcon
import pl.literusie.ui.components.PillButton
import pl.literusie.ui.components.TvCard
import pl.literusie.ui.platform.WordImage
import pl.literusie.ui.input.requestWhenReady
import pl.literusie.ui.theme.Palette

const val CATEGORY_COLUMNS = 3

@Composable
fun CategoryScreen(
    catalog: WordCatalog,
    progress: Map<String, WordProgress>,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
) {
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { first.requestWhenReady() }

    Column(Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 28.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PillButton("Wróć", onBack, icon = { BackIcon(Palette.Ink) }, modifier = Modifier.testTag("back"))
            Spacer(Modifier.width(24.dp))
            Text("Wybierz kategorię", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(CATEGORY_COLUMNS),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.fillMaxSize().testTag("category_grid"),
        ) {
            itemsIndexed(catalog.categories, key = { _, c -> c.name }) { i, cat ->
                val inCat = catalog.words.filter { it.inCategory(cat.name) }
                val learned = inCat.count { (progress[it.id]?.completions ?: 0) > 0 }
                TvCard(
                    onClick = { onSelect(cat.name) },
                    modifier = Modifier.fillMaxWidth().height(120.dp).testTag("category_${cat.name}"),
                    background = Palette.Tints[i % Palette.Tints.size],
                    shape = RoundedCornerShape(26.dp),
                    focusRequester = if (i == 0) first else null,
                ) {
                    Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(60.dp).clip(CircleShape).background(Palette.Paper), contentAlignment = Alignment.Center) {
                                WordImage(cat.coverImageId, Modifier.size(42.dp), null)
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                cat.name.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                                maxLines = 2,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$learned z ${cat.wordCount}", fontSize = 16.sp, color = Palette.InkSoft)
                            Spacer(Modifier.width(10.dp))
                            ProgressBar(if (cat.wordCount == 0) 0f else learned.toFloat() / cat.wordCount, Palette.Accents[i % Palette.Accents.size], Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressBar(fraction: Float, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier.fillMaxWidth()) {
    Box(modifier.height(10.dp).clip(RoundedCornerShape(5.dp)).background(Palette.Paper.copy(alpha = .9f))) {
        Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(10.dp).clip(RoundedCornerShape(5.dp)).background(color))
    }
}
