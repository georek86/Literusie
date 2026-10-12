package pl.literusie.ui.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import pl.literusie.ui.theme.Palette

/**
 * Ilustracja słowa z zasobów aplikacji (assets/illustrations/<imageId>.svg).
 * Gdy pliku brakuje, pokazujemy spokojne, puste tło – nigdy cudzą, losową grafikę.
 */
@Composable
fun WordImage(imageId: String, modifier: Modifier, contentDescription: String?) {
    val context = LocalContext.current
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data("file:///android_asset/illustrations/$imageId.svg")
            .decoderFactory(SvgDecoder.Factory())
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier,
        error = { androidx.compose.foundation.layout.Box(Modifier.clip(CircleShape).background(Palette.SkySoft)) },
    )
}
