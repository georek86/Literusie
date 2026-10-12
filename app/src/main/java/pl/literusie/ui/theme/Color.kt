package pl.literusie.ui.theme

import androidx.compose.ui.graphics.Color

/** Ciepła, pastelowa paleta z kilkoma wyraźnymi akcentami. Tekst: granat (ink) na kremie – wysoki kontrast. */
object Palette {
    val Cream = Color(0xFFFFF7E8)
    val Paper = Color(0xFFFFFFFF)
    val Line = Color(0xFFEADFC8)

    val Ink = Color(0xFF2B2858)
    val InkSoft = Color(0xFF65618F)

    val Sky = Color(0xFF3E9BF2)
    val SkySoft = Color(0xFFDDEEFF)
    val Mint = Color(0xFF2FB886)
    val MintSoft = Color(0xFFD5F4E6)
    val Sun = Color(0xFFFFC145)
    val SunSoft = Color(0xFFFFEFC4)
    val Coral = Color(0xFFFF7A62)
    val CoralSoft = Color(0xFFFFE0D9)
    val Lilac = Color(0xFF8F7BE0)
    val LilacSoft = Color(0xFFE8E2FB)

    /** Spokojny „błąd”: ciepła brzoskwinia zamiast alarmowej czerwieni. */
    val Peach = Color(0xFFF2994A)
    val PeachSoft = Color(0xFFFFE7D1)

    /** Kolejne kafelki dostają kolejne pastele. */
    val Tints = listOf(SkySoft, MintSoft, SunSoft, CoralSoft, LilacSoft)
    val Accents = listOf(Sky, Mint, Sun, Coral, Lilac)
}
