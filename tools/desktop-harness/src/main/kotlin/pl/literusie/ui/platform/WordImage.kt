package pl.literusie.ui.platform

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import pl.literusie.ui.theme.Palette
import java.io.File

/** Wersja desktopowa (tylko testy/podgląd): czyta te same pliki SVG co aplikacja z assets. */
@Composable
fun WordImage(imageId: String, modifier: Modifier, contentDescription: String?) {
    val density = LocalDensity.current
    val painter = remember(imageId) {
        runCatching {
            File(System.getProperty("literusie.assets") ?: "../../app/src/main/assets", "illustrations/$imageId.svg")
                .inputStream().use { loadSvgPainter(it, density) }
        }.getOrNull()
    }
    if (painter != null) Image(painter, contentDescription, modifier) else Box(modifier.background(Palette.SkySoft))
}
