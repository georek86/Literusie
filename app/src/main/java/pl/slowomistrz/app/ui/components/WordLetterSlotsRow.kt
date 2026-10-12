package pl.slowomistrz.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.slowomistrz.app.domain.model.WordInputState
import pl.slowomistrz.app.ui.theme.BorderSubtle
import pl.slowomistrz.app.ui.theme.FocusHighlight
import pl.slowomistrz.app.ui.theme.LetterSlotActive
import pl.slowomistrz.app.ui.theme.LetterSlotEmpty
import pl.slowomistrz.app.ui.theme.LetterSlotFilled
import pl.slowomistrz.app.ui.theme.SoftError
import pl.slowomistrz.app.ui.theme.SuccessGreen
import pl.slowomistrz.app.ui.theme.TextPrimary
import pl.slowomistrz.app.ui.theme.TextSubtle

@Composable
fun WordLetterSlotsRow(
    inputState: WordInputState,
    modifier: Modifier = Modifier,
    slotSize: Dp = 80.dp
) {
    val totalLength = inputState.totalLength

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalLength) {
            val isFilled = inputState.isLetterFilled(i)
            val isCurrentActive = (i == inputState.typedCount && !inputState.isWordCompleted)
            val isError = (isCurrentActive && inputState.lastInputWasError)

            val targetBgColor = when {
                inputState.isWordCompleted -> SuccessGreen
                isFilled -> LetterSlotFilled
                isError -> SoftError
                isCurrentActive -> LetterSlotActive
                else -> LetterSlotEmpty
            }

            val targetBorderColor = when {
                inputState.isWordCompleted -> SuccessGreen
                isCurrentActive -> FocusHighlight
                isFilled -> LetterSlotFilled
                else -> BorderSubtle
            }

            val animatedBg by animateColorAsState(
                targetValue = targetBgColor,
                animationSpec = tween(durationMillis = 200),
                label = "slotBg"
            )

            val animatedBorder by animateColorAsState(
                targetValue = targetBorderColor,
                animationSpec = tween(durationMillis = 200),
                label = "slotBorder"
            )

            Box(
                modifier = Modifier
                    .size(slotSize)
                    .clip(RoundedCornerShape(18.dp))
                    .background(animatedBg)
                    .border(3.dp, animatedBorder, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isFilled || inputState.isWordCompleted) {
                    Text(
                        text = inputState.getDisplayCharForSlot(i).toString(),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                } else if (isCurrentActive) {
                    // Kropka lub delikatna kreska zachęcająca dziecko
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(FocusHighlight)
                    )
                }
            }
        }
    }
}
