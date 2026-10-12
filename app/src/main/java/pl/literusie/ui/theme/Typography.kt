package pl.literusie.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Zaokrąglony, czytelny z kilku metrów krój (Nunito) – tekst nigdy nie schodzi poniżej 20 sp. */
fun literusieTypography(font: FontFamily): Typography {
    fun s(size: Int, weight: FontWeight) =
        TextStyle(fontFamily = font, fontSize = size.sp, fontWeight = weight, color = Palette.Ink)
    return Typography(
        displayLarge = s(96, FontWeight.Black),
        displayMedium = s(72, FontWeight.Black),
        headlineLarge = s(44, FontWeight.ExtraBold),
        headlineMedium = s(34, FontWeight.ExtraBold),
        titleLarge = s(28, FontWeight.ExtraBold),
        titleMedium = s(24, FontWeight.ExtraBold),
        bodyLarge = s(24, FontWeight.SemiBold),
        bodyMedium = s(20, FontWeight.SemiBold),
        labelLarge = s(22, FontWeight.ExtraBold),
    )
}
