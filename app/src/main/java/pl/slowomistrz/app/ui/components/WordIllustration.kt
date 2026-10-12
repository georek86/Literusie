package pl.slowomistrz.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import pl.slowomistrz.app.ui.theme.BorderSubtle
import pl.slowomistrz.app.ui.theme.PrimaryBlue
import pl.slowomistrz.app.ui.theme.SurfaceCard
import pl.slowomistrz.app.ui.theme.TextPrimary

@Composable
fun WordIllustration(
    imageId: String,
    wordLabel: String,
    modifier: Modifier = Modifier,
    size: Dp = 380.dp
) {
    val context = LocalContext.current
    val jpgPath = "illustrations/$imageId.jpg"
    val svgPath = "illustrations/$imageId.svg"

    val imageModel = remember(imageId) {
        val hasJpg = try {
            context.assets.open(jpgPath).close()
            true
        } catch (e: Exception) {
            false
        }
        if (hasJpg) {
            "file:///android_asset/$jpgPath"
        } else {
            "file:///android_asset/$svgPath"
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(32.dp))
            .background(SurfaceCard)
            .border(2.dp, BorderSubtle, RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageModel)
                .decoderFactory(SvgDecoder.Factory())
                .crossfade(true)
                .build(),
            contentDescription = "Ilustracja dla słowa $wordLabel",
            modifier = Modifier.fillMaxSize()
        )
    }
}
