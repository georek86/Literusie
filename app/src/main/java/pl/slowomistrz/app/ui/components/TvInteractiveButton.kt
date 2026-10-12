package pl.slowomistrz.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.slowomistrz.app.ui.theme.BorderSubtle
import pl.slowomistrz.app.ui.theme.FocusHighlight
import pl.slowomistrz.app.ui.theme.PrimaryBlue
import pl.slowomistrz.app.ui.theme.SurfaceCard
import pl.slowomistrz.app.ui.theme.TextPrimary

@Composable
fun TvInteractiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> if (isPrimary) PrimaryBlue else Color(0xFF333C55)
            isPrimary -> Color(0xFF1E88E5)
            else -> SurfaceCard
        },
        label = "btnBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) FocusHighlight else BorderSubtle,
        label = "btnBorder"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(3.dp, borderColor, RoundedCornerShape(20.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter) {
                    onClick()
                    true
                } else false
            }
            .padding(horizontal = 28.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isFocused && isPrimary) Color(0xFF12151C) else TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFocused && isPrimary) Color(0xFF12151C) else TextPrimary
            )
        }
    }
}
