package com.vkasurinen.knock.core.designsystem.components.brand

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vkasurinen.knock.core.designsystem.theme.KnockBuff500
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.KnockWood200
import com.vkasurinen.knock.core.designsystem.theme.KnockWood300
import com.vkasurinen.knock.core.designsystem.theme.KnockWood50
import com.vkasurinen.knock.core.designsystem.theme.KnockWood600
import com.vkasurinen.knock.core.designsystem.theme.extended
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Knock logo "A — Door": a panelled door with two knock waves.
 *
 * With [onBrand] the logo sits on the brand background (surface/bg) and the door is
 * always light. Otherwise the colors follow the current theme tokens.
 */
@Composable
fun KnockBrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    onBrand: Boolean = true,
) {
    val colors = if(onBrand) {
        DoorColors(
            casing = KnockWood300,
            door = KnockWood50,
            panel = KnockWood200,
            knob = KnockWood600,
            waves = KnockBuff500,
        )
    } else {
        DoorColors(
            casing = MaterialTheme.colorScheme.extended.textPrimary,
            door = MaterialTheme.colorScheme.primary,
            panel = MaterialTheme.colorScheme.extended.primaryHover,
            knob = MaterialTheme.colorScheme.surface,
            waves = MaterialTheme.colorScheme.extended.accentBrand,
        )
    }
    val vector = remember(colors) { doorLogoVector(colors) }
    Image(
        imageVector = vector,
        contentDescription = null,
        modifier = modifier.size(size)
    )
}

private data class DoorColors(
    val casing: Color,
    val door: Color,
    val panel: Color,
    val knob: Color,
    val waves: Color,
)

private fun doorLogoVector(colors: DoorColors): ImageVector {
    fun ImageVector.Builder.fillPath(pathData: String, color: Color) = addPath(
        pathData = PathParser().parsePathString(pathData).toNodes(),
        fill = SolidColor(color)
    )

    fun ImageVector.Builder.strokePath(pathData: String, color: Color) = addPath(
        pathData = PathParser().parsePathString(pathData).toNodes(),
        stroke = SolidColor(color),
        strokeLineWidth = 3f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    )

    return ImageVector.Builder(
        name = "KnockDoorLogo",
        defaultWidth = 64.dp,
        defaultHeight = 64.dp,
        viewportWidth = 64f,
        viewportHeight = 64f
    ).apply {
        // Casing
        fillPath("M12 10C12 8.34315 13.3431 7 15 7H41C42.6569 7 44 8.34315 44 10V57H12V10Z", colors.casing)
        // Door
        fillPath("M16 12C16 11.4477 16.4477 11 17 11H39C39.5523 11 40 11.4477 40 12V57H16V12Z", colors.door)
        // Panels
        fillPath("M20 16.5C20 15.6716 20.6716 15 21.5 15H34.5C35.3284 15 36 15.6716 36 16.5V28.5C36 29.3284 35.3284 30 34.5 30H21.5C20.6716 30 20 29.3284 20 28.5V16.5Z", colors.panel)
        fillPath("M20 37.5C20 36.6716 20.6716 36 21.5 36H34.5C35.3284 36 36 36.6716 36 37.5V51.5C36 52.3284 35.3284 53 34.5 53H21.5C20.6716 53 20 52.3284 20 51.5V37.5Z", colors.panel)
        // Knob
        fillPath("M33.3 33A2.2 2.2 0 1 0 37.7 33A2.2 2.2 0 1 0 33.3 33Z", colors.knob)
        // Threshold
        fillPath("M8 58C8 56.8954 8.89543 56 10 56H46C47.1046 56 48 56.8954 48 58C48 59.1046 47.1046 60 46 60H10C8.89543 60 8 59.1046 8 58Z", colors.casing)
        // Knock waves
        strokePath("M49 26C49.8513 26.7507 50.533 27.674 51 28.7085C51.467 29.743 51.7085 30.865 51.7085 32C51.7085 33.135 51.467 34.257 51 35.2915C50.533 36.326 49.8513 37.2493 49 38", colors.waves)
        strokePath("M54.5 21C56.0146 22.4042 57.2229 24.1059 58.0492 25.9988C58.8755 27.8916 59.302 29.9347 59.302 32C59.302 34.0653 58.8755 36.1084 58.0492 38.0012C57.2229 39.8941 56.0146 41.5958 54.5 43", colors.waves)
    }.build()
}

@Composable
@Preview
fun KnockBrandLogoPreview() {
    KnockTheme {
        KnockBrandLogo(onBrand = false)
    }
}
