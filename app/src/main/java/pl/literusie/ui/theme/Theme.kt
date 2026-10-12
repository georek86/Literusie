package pl.literusie.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

@Composable
fun LiterusieTheme(font: FontFamily = FontFamily.SansSerif, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Palette.Sky, onPrimary = Palette.Paper,
            background = Palette.Cream, onBackground = Palette.Ink,
            surface = Palette.Paper, onSurface = Palette.Ink,
        ),
        typography = literusieTypography(font),
        content = content,
    )
}
