package pl.literusie.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Proste ikony rysowane w kodzie – bez dodatkowych bibliotek. */
@Composable
fun SpeakerIcon(color: Color, size: Dp = 28.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val body = Path().apply {
            moveTo(w * .08f, w * .38f); lineTo(w * .28f, w * .38f); lineTo(w * .50f, w * .18f)
            lineTo(w * .50f, w * .82f); lineTo(w * .28f, w * .62f); lineTo(w * .08f, w * .62f); close()
        }
        drawPath(body, color)
        val st = Stroke(width = w * .09f, cap = StrokeCap.Round)
        drawArc(color, -45f, 90f, false, Offset(w * .30f, w * .30f), Size(w * .40f, w * .40f), style = st)
        drawArc(color, -50f, 100f, false, Offset(w * .22f, w * .16f), Size(w * .68f, w * .68f), style = st)
    }
}

@Composable
fun BackIcon(color: Color, size: Dp = 28.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val p = Path().apply {
            moveTo(w * .62f, w * .18f); lineTo(w * .30f, w * .50f); lineTo(w * .62f, w * .82f)
        }
        drawPath(p, color, style = Stroke(width = w * .12f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
