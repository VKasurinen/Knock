package com.vkasurinen.knock.core.designsystem.components.chat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import com.vkasurinen.knock.core.designsystem.theme.extended

/**
 * Chat background: wallpaper gradient (chat/wallpaper-top → mid → bottom) with the Knock
 * doodle pattern (door, sun, coffee, bird, heart, leaf) drawn at 22 % opacity.
 */
@Composable
fun KnockChatWallpaper(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val extended = MaterialTheme.colorScheme.extended
    Box(
        modifier = modifier
            .clipToBounds()
            .background(
                Brush.verticalGradient(
                    0f to extended.wallpaperTop,
                    0.5f to extended.wallpaperMid,
                    1f to extended.wallpaperBottom
                )
            )
    ) {
        val doodlePaths = remember { DOODLES.map { shape -> shape.map { PathParser().parsePathString(it).toPath() } } }
        val patternColor = extended.wallpaperPattern.copy(alpha = 0.22f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val unit = density
            val tileWidth = TILE_WIDTH * unit
            val tileHeight = TILE_HEIGHT * unit
            val stroke = Stroke(
                width = 1.6f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
            var tileY = 0f
            while (tileY < size.height) {
                var tileX = 0f
                while (tileX < size.width) {
                    TILE.forEach { doodle ->
                        val center = Offset(
                            x = tileX + doodle.centerX * unit,
                            y = tileY + doodle.centerY * unit
                        )
                        translate(center.x - 12f * unit, center.y - 12f * unit) {
                            rotate(degrees = doodle.rotation, pivot = Offset(12f * unit, 12f * unit)) {
                                scale(scale = unit, pivot = Offset.Zero) {
                                    doodlePaths[doodle.shape].forEach { path ->
                                        drawPath(path = path, color = patternColor, style = stroke)
                                    }
                                }
                            }
                        }
                    }
                    tileX += tileWidth
                }
                tileY += tileHeight
            }
        }

        content()
    }
}

private class Doodle(
    left: Float,
    top: Float,
    containerSize: Float,
    val rotation: Float,
    val shape: Int,
) {
    val centerX = left + containerSize / 2f
    val centerY = top + containerSize / 2f
}

private const val TILE_WIDTH = 412f
private const val TILE_HEIGHT = 782f

// Positions (dp) of the "Pattern/Wallpaper Tile" component in Figma. The tile repeats every 782 dp.
private val TILE = listOf(
    Doodle(-6.14f, 6f, 31.894f, 25f, 0),
    Doodle(95f, 8.01f, 28.465f, -12f, 1),
    Doodle(185.58f, 8f, 24.415f, 1f, 2),
    Doodle(271.19f, 15f, 29.093f, 14f, 0),
    Doodle(358f, 0.62f, 31.47f, -23f, 3),
    Doodle(49f, 90.83f, 27.803f, -10f, 4),
    Doodle(138.74f, 90f, 25.223f, 3f, 0),
    Doodle(214.38f, 85f, 29.686f, 16f, 1),
    Doodle(312f, 83.4f, 31.007f, -21f, 2),
    Doodle(403f, 83.66f, 27.107f, -8f, 0),
    Doodle(1.91f, 172f, 26f, 5f, 5),
    Doodle(87.58f, 167f, 30.242f, 18f, 4),
    Doodle(186f, 154.19f, 30.506f, -19f, 0),
    Doodle(277f, 166.49f, 26.377f, -6f, 1),
    Doodle(355.08f, 164f, 26.746f, 7f, 2),
    Doodle(40.79f, 249f, 30.761f, 20f, 5),
    Doodle(140f, 236.98f, 29.968f, -17f, 5),
    Doodle(221f, 249.33f, 25.616f, -4f, 4),
    Doodle(308.25f, 246f, 27.459f, 9f, 0),
    Doodle(394.01f, 241f, 31.243f, 22f, 1),
    Doodle(4f, 319.79f, 29.394f, -15f, 3),
    Doodle(95f, 320.16f, 24.823f, -2f, 5),
    Doodle(181.42f, 328f, 28.138f, 11f, 5),
    Doodle(267.24f, 323f, 31.687f, 24f, 4),
    Doodle(358f, 312.6f, 28.784f, -13f, 0),
    Doodle(49f, 403f, 24f, 0f, 0),
    Doodle(134.6f, 398f, 28.784f, 13f, 3),
    Doodle(221f, 395.24f, 31.687f, -24f, 5),
    Doodle(312f, 395.42f, 28.138f, -11f, 5),
    Doodle(402.16f, 407f, 24.823f, 2f, 4),
    Doodle(-2.21f, 480f, 29.394f, 15f, 2),
    Doodle(95f, 466.01f, 31.243f, -22f, 0),
    Doodle(186f, 478.25f, 27.459f, -9f, 3),
    Doodle(275.33f, 477f, 25.616f, 4f, 5),
    Doodle(350.98f, 484f, 29.968f, 17f, 5),
    Doodle(49f, 548.79f, 30.761f, -20f, 1),
    Doodle(140f, 549.08f, 26.746f, -7f, 2),
    Doodle(218.49f, 559f, 26.377f, 6f, 0),
    Doodle(304.19f, 554f, 30.506f, 19f, 3),
    Doodle(403f, 553.58f, 30.242f, -18f, 5),
    Doodle(4f, 631.91f, 26f, -5f, 0),
    Doodle(91.66f, 641f, 27.107f, 8f, 1),
    Doodle(177.4f, 636f, 31.007f, 21f, 2),
    Doodle(277f, 624.38f, 29.686f, -16f, 0),
    Doodle(358f, 636.74f, 25.223f, -3f, 3),
    Doodle(44.83f, 711f, 27.803f, 10f, 4),
    Doodle(130.62f, 718f, 31.47f, 23f, 0),
    Doodle(221f, 707.19f, 29.093f, -14f, 1),
    Doodle(312f, 707.58f, 24.415f, -1f, 2),
    Doodle(398.01f, 715f, 28.465f, 12f, 0),
)

// 24×24 Lucide-style doodles, 1.6 px stroke
private val DOODLES = listOf(
    // Door
    listOf(
        "M6 22V9C6 7.4087 6.63214 5.88258 7.75736 4.75736C8.88258 3.63214 10.4087 3 12 3C13.5913 3 15.1174 3.63214 16.2426 4.75736C17.3679 5.88258 18 7.4087 18 9V22H6Z",
        "M14.5 16C15.0523 16 15.5 15.5523 15.5 15C15.5 14.4477 15.0523 14 14.5 14C13.9477 14 13.5 14.4477 13.5 15C13.5 15.5523 13.9477 16 14.5 16Z",
    ),
    // Sun
    listOf(
        "M12 16C14.2091 16 16 14.2091 16 12C16 9.79086 14.2091 8 12 8C9.79086 8 8 9.79086 8 12C8 14.2091 9.79086 16 12 16Z",
        "M12 2V4", "M12 20V22", "M4.93 4.93L6.34 6.34", "M17.66 17.66L19.07 19.07",
        "M2 12H4", "M20 12H22", "M6.34 17.66L4.93 19.07", "M19.07 4.93L17.66 6.34",
    ),
    // Coffee
    listOf(
        "M10 2V4", "M14 2V4", "M6 2V4",
        "M16 8C16.2652 8 16.5196 8.10536 16.7071 8.29289C16.8946 8.48043 17 8.73478 17 9V17C17 18.0609 16.5786 19.0783 15.8284 19.8284C15.0783 20.5786 14.0609 21 13 21H7C5.93913 21 4.92172 20.5786 4.17157 19.8284C3.42143 19.0783 3 18.0609 3 17V9C3 8.73478 3.10536 8.48043 3.29289 8.29289C3.48043 8.10536 3.73478 8 4 8H18C19.0609 8 20.0783 8.42143 20.8284 9.17157C21.5786 9.92172 22 10.9391 22 12C22 13.0609 21.5786 14.0783 20.8284 14.8284C20.0783 15.5786 19.0609 16 18 16H17",
    ),
    // Bird
    listOf(
        "M16 7H16.01",
        "M3.4 18H12C14.1217 18 16.1566 17.1571 17.6569 15.6569C19.1571 14.1566 20 12.1217 20 10V7C20.0023 6.14791 19.7323 5.31737 19.2296 4.62939C18.7269 3.94142 18.0175 3.43196 17.205 3.17526C16.3925 2.91856 15.5193 2.92804 14.7125 3.2023C13.9058 3.47656 13.2077 4.00128 12.72 4.7L2 20",
        "M20 7L22 7.5L20 8", "M10 18V21", "M14 17.75V21",
        "M7 18C8.23312 17.9999 9.43627 17.6199 10.4457 16.9117C11.4552 16.2035 12.2219 15.2014 12.6416 14.0419C13.0612 12.8824 13.1134 11.6217 12.7911 10.4315C12.4687 9.24127 11.7874 8.17926 10.84 7.39",
    ),
    // Heart
    listOf(
        "M19 14C20.49 12.54 22 10.79 22 8.5C22 7.04131 21.4205 5.64236 20.3891 4.61091C19.3576 3.57946 17.9587 3 16.5 3C14.74 3 13.5 3.5 12 5C10.5 3.5 9.26 3 7.5 3C6.04131 3 4.64236 3.57946 3.61091 4.61091C2.57946 5.64236 2 7.04131 2 8.5C2 10.8 3.5 12.55 5 14L12 21L19 14Z",
    ),
    // Leaf
    listOf(
        "M11 20C9.24406 20.0053 7.55025 19.3505 6.25452 18.1654C4.95878 16.9803 4.15577 15.3515 4.00474 13.6021C3.8537 11.8527 4.36569 10.1104 5.43915 8.72074C6.51261 7.33112 8.06913 6.3957 9.8 6.1C15.5 5 17 4.48 19 2C20 4 21 6.18 21 10C21 15.5 16.22 20 11 20Z",
        "M2 21C2 18 3.85 15.64 7.08 15C9.5 14.52 12 13 13 12",
    ),
)
