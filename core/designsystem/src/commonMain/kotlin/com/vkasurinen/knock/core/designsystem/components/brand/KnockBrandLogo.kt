package com.vkasurinen.knock.core.designsystem.components.brand

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.KnockWood50
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Knock logo "D — K in a speech bubble": a speech bubble with the K cut out of it.
 *
 * With [onBrand] the logo sits on the brand background (surface/bg) and the bubble is
 * always Cream. Otherwise it is Olive (btn/primary) on light surfaces and Cream on dark ones.
 */
@Composable
fun KnockBrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    onBrand: Boolean = true,
) {
    val isDarkSurface = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val color = if(onBrand || isDarkSurface) KnockWood50 else MaterialTheme.colorScheme.primary
    val vector = remember(color) { bubbleLogoVector(color) }
    Image(
        imageVector = vector,
        contentDescription = null,
        modifier = modifier.size(size)
    )
}

// Figma "Logo/Bubble K": the bubble and tail with the K strokes subtracted, in a 64 x 64 box
private const val BUBBLE_K_PATH = "M43 10C50.1797 10 56 15.8203 56 23V35C56 42.1797 50.1797 48 43 48H25.7275L11 57L13.9521 45.9258C10.3705 43.6106 8 39.5821 8 35V23C8 15.8203 13.8203 10 21 10H43Z" +
    "M25 16.75C23.4812 16.75 22.25 17.9812 22.25 19.5V38.5C22.25 40.0188 23.4812 41.25 25 41.25C26.5188 41.25 27.75 40.0188 27.75 38.5V33.1162C27.7871 33.0855 27.8265 33.0564 27.8623 33.0234L29.9824 31.0723L36.2422 40.0703C37.1095 41.317 38.8236 41.6249 40.0703 40.7578C41.317 39.8905 41.6249 38.1764 40.7578 36.9297L34.0684 27.3135L40.3623 21.5234C41.4796 20.4951 41.5516 18.7553 40.5234 17.6377C39.4951 16.5204 37.7553 16.4484 36.6377 17.4766L27.75 25.6523V19.5C27.75 17.9812 26.5188 16.75 25 16.75Z"

private fun bubbleLogoVector(color: Color): ImageVector {
    return ImageVector.Builder(
        name = "KnockBubbleLogo",
        defaultWidth = 64.dp,
        defaultHeight = 64.dp,
        viewportWidth = 64f,
        viewportHeight = 64f
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(BUBBLE_K_PATH).toNodes(),
            pathFillType = PathFillType.EvenOdd,
            fill = SolidColor(color)
        )
    }.build()
}

@Composable
@Preview
fun KnockBrandLogoPreview() {
    KnockTheme {
        KnockBrandLogo(onBrand = false)
    }
}
