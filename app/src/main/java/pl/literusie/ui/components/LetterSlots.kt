package pl.literusie.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.literusie.domain.typing.TypingState
import pl.literusie.ui.theme.Palette

/**
 * Rząd pól na litery: wpisane – miętowe, bieżące – z niebieską ramką (po błędzie ciepła brzoskwinia),
 * kolejne – puste. Wielkość pól dopasowuje się do długości słowa.
 */
@Composable
fun LetterSlots(
    state: TypingState,
    modifier: Modifier = Modifier,
    maxSlot: Dp = 76.dp,
    showHint: Boolean = true,
) {
    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val gap = 10.dp
        val n = state.length.coerceAtLeast(1)
        val slot = minOf(maxSlot, (maxWidth - gap * (n - 1)) / n)
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            for (i in 0 until state.length) {
                val filled = i < state.typed
                val active = i == state.typed && !state.finished
                val wrong = active && state.streakErrors > 0
                LetterSlot(
                    letter = state.word.text[i],
                    filled = filled,
                    active = active,
                    wrong = wrong,
                    done = state.finished,
                    hint = active && showHint && state.showHint,
                    size = slot,
                    modifier = Modifier.testTag("slot_$i"),
                )
            }
        }
    }
}

@Composable
private fun LetterSlot(
    letter: Char, filled: Boolean, active: Boolean, wrong: Boolean, done: Boolean, hint: Boolean,
    size: Dp, modifier: Modifier,
) {
    val bg by animateColorAsState(
        when { filled -> Palette.MintSoft; wrong -> Palette.PeachSoft; else -> Palette.Paper }, tween(160), label = "slotBg",
    )
    val border by animateColorAsState(
        when { filled -> Palette.Mint; wrong -> Palette.Peach; active -> Palette.Sky; else -> Palette.Line }, tween(160), label = "slotBorder",
    )
    val pop by animateFloatAsState(
        if (filled) 1f else .86f, spring(dampingRatio = .55f, stiffness = Spring.StiffnessMedium), label = "pop",
    )
    val shape = RoundedCornerShape(size * .24f)
    androidx.compose.foundation.layout.Box(
        modifier
            .size(size, size * 1.18f)
            .clip(shape)
            .background(bg)
            .border(if (active) 5.dp else 3.dp, border, shape),
        contentAlignment = Alignment.Center,
    ) {
        val show = filled || hint
        if (show) {
            Text(
                letter.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontSize = (size.value * .62f).sp,
                fontWeight = FontWeight.Black,
                color = if (filled) Palette.Ink else Palette.InkSoft.copy(alpha = .35f),
                modifier = Modifier.graphicsLayer { scaleX = pop; scaleY = pop },
            )
        }
    }
}
