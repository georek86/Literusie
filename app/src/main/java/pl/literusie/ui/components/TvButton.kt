package pl.literusie.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.literusie.ui.theme.Palette

/**
 * Podstawowy element nawigowany pilotem: karta z wyraźnym, ale spokojnym wyróżnieniem fokusu
 * (niebieska ramka + minimalne powiększenie, bez pulsowania). OK/Enter wywołuje [onClick].
 */
@Composable
fun TvCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = Palette.Paper,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(28.dp),
    focusRequester: FocusRequester? = null,
    focusBorder: Color = Palette.Sky,
    content: @Composable BoxScope.() -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.04f else 1f, tween(120), label = "scale")
    val border by animateColorAsState(if (focused) focusBorder else Palette.Line, tween(120), label = "border")
    val elevation by animateFloatAsState(if (focused) 14f else 3f, tween(120), label = "elevation")
    Box(
        modifier = modifier
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(elevation.dp, shape, ambientColor = Palette.Ink.copy(alpha = .25f), spotColor = Palette.Ink.copy(alpha = .25f))
            .clip(shape)
            .background(background)
            .border(BorderStroke(if (focused) 5.dp else 2.dp, border), shape)
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        content = content,
    )
}

/** Przycisk-pastylka z tekstem i opcjonalną ikoną. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    focusRequester: FocusRequester? = null,
    icon: (@Composable () -> Unit)? = null,
    height: Dp = 56.dp,
) {
    TvCard(
        onClick = onClick,
        modifier = modifier,
        background = if (primary) Palette.Sky else Palette.Paper,
        shape = RoundedCornerShape(height / 2),
        focusRequester = focusRequester,
        // Na niebieskim przycisku niebieska ramka byłaby niewidoczna – używamy żółtej.
        focusBorder = if (primary) Palette.Sun else Palette.Sky,
    ) {
        Row(
            Modifier.heightIn(min = height).padding(horizontal = 26.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) { icon(); Spacer(Modifier.width(10.dp)) }
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                color = if (primary) Palette.Paper else Palette.Ink,
            )
        }
    }
}
