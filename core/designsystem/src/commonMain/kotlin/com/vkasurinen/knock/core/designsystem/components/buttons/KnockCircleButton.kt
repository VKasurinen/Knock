package com.vkasurinen.knock.core.designsystem.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vkasurinen.knock.core.designsystem.theme.extended

/**
 * Floating round button used on top of the chat wallpaper (top bar back/more, composer attach).
 * surface/container fill with a soft white edge and shadow.
 */
@Composable
fun KnockCircleButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    iconTint: Color = MaterialTheme.colorScheme.extended.textPrimary,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .size(size)
            .floatingSurface(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Round primary action (send / record) filled with the outgoing bubble gradient.
 */
@Composable
fun KnockGradientCircleButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 20.dp,
    enabled: Boolean = true,
) {
    val extended = MaterialTheme.colorScheme.extended
    Box(
        modifier = modifier
            .size(size)
            .then(
                if(enabled) {
                    Modifier
                        .shadow(4.dp, CircleShape, ambientColor = GradientShadowColor, spotColor = GradientShadowColor)
                        .background(
                            Brush.linearGradient(
                                0f to extended.bubbleOutStart,
                                0.714f to extended.bubbleOutEnd,
                                start = Offset.Zero,
                                end = Offset.Infinite
                            ),
                            CircleShape
                        )
                } else {
                    Modifier
                        .background(extended.disabledFill, CircleShape)
                        .border(1.dp, extended.disabledOutline, CircleShape)
                }
            )
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if(enabled) extended.onBubbleOut else extended.textDisabled,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Background of the floating "glass" elements on the chat wallpaper: surface/container,
 * 1 dp white 40 % edge and a soft shadow.
 */
@Composable
fun Modifier.floatingSurface(
    shape: androidx.compose.ui.graphics.Shape,
    borderColor: Color = Color.White.copy(alpha = 0.4f)
): Modifier {
    val extended = MaterialTheme.colorScheme.extended
    return this
        .shadow(2.dp, shape, ambientColor = FloatingShadowColor, spotColor = FloatingShadowColor)
        .background(extended.surfaceContainer, shape)
        .border(1.dp, borderColor, shape)
        .clip(shape)
}

private val FloatingShadowColor = Color(0x141A120D)
private val GradientShadowColor = Color(0x331A120D)
