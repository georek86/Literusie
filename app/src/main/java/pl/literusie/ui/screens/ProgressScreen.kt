package pl.literusie.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.literusie.domain.learning.WordProgress
import pl.literusie.domain.learning.WordStatus
import pl.literusie.domain.model.WordCatalog
import pl.literusie.ui.components.BackIcon
import pl.literusie.ui.components.PillButton
import pl.literusie.ui.input.requestWhenReady
import pl.literusie.ui.theme.Palette
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ProgressScreen(
    catalog: WordCatalog,
    progress: Map<String, WordProgress>,
    onBack: () -> Unit,
    onParents: () -> Unit,
) {
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { first.requestWhenReady() }

    val total = catalog.words.size
    val known = catalog.words.filter { (progress[it.id]?.completions ?: 0) > 0 }
    val mastered = catalog.words.count { progress[it.id]?.status == WordStatus.MASTERED }
    val practicing = known.size - mastered
    val last = progress.values.maxOfOrNull { it.lastPracticed } ?: 0L

    Column(Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 28.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PillButton("Wróć", onBack, icon = { BackIcon(Palette.Ink) }, focusRequester = first, modifier = Modifier.testTag("back"))
            Spacer(Modifier.width(24.dp))
            Text("Postępy", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.weight(1f))
            PillButton("Dla rodziców", onParents, modifier = Modifier.testTag("parents"))
        }
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Ring(known.size, mastered, total)
            Spacer(Modifier.width(56.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Legend(Palette.Mint, "Opanowane", mastered, "legend_mastered")
                Legend(Palette.Sun, "Ćwiczone", practicing, "legend_practicing")
                Legend(Palette.Line, "Jeszcze nowe", total - known.size, "legend_new")
                Spacer(Modifier.height(6.dp))
                Text(
                    if (last > 0) "Ostatnia nauka: " + DateTimeFormatter.ofPattern("d.MM.yyyy").format(Instant.ofEpochMilli(last).atZone(ZoneId.systemDefault()))
                    else "Jeszcze nie zaczęliśmy – zapraszamy!",
                    style = MaterialTheme.typography.bodyLarge, color = Palette.InkSoft,
                )
            }
        }
    }
}

@Composable
private fun Ring(known: Int, mastered: Int, total: Int) {
    Box(Modifier.size(300.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 36.dp.toPx()
            val inset = stroke / 2
            val sz = Size(size.width - stroke, size.height - stroke)
            val tl = Offset(inset, inset)
            fun arc(color: Color, sweep: Float) =
                drawArc(color, -90f, sweep, false, tl, sz, style = Stroke(stroke, cap = StrokeCap.Butt))
            arc(Palette.Line, 360f)
            if (total > 0) {
                if (known > 0) arc(Palette.Sun, 360f * known / total)
                if (mastered > 0) arc(Palette.Mint, 360f * mastered / total)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$known", fontSize = 72.sp, fontWeight = FontWeight.Black, color = Palette.Ink, modifier = Modifier.testTag("known_count"))
            Text("z $total słów", style = MaterialTheme.typography.bodyLarge, color = Palette.InkSoft)
        }
    }
}

@Composable
private fun Legend(color: Color, label: String, value: Int, tag: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(28.dp)) { drawRoundRect(color, cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())) }
        Spacer(Modifier.width(16.dp))
        Text("$label:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(10.dp))
        Text("$value", fontSize = 32.sp, fontWeight = FontWeight.Black, modifier = Modifier.testTag(tag))
    }
}
