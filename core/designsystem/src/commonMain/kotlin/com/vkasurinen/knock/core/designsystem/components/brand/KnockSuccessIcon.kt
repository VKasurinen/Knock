package com.vkasurinen.knock.core.designsystem.components.brand

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import knock.core.designsystem.generated.resources.Res
import knock.core.designsystem.generated.resources.check_icon
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 96 dp result badge: surface/surface-higher circle with a 6 dp outline and a 36 dp icon.
 */
@Composable
fun KnockResultBadge(
    icon: ImageVector,
    outlineColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(vertical = 8.dp)
            .size(96.dp)
            .background(MaterialTheme.colorScheme.extended.surfaceHigher, CircleShape)
            .border(6.dp, outlineColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun KnockSuccessIcon(
    modifier: Modifier = Modifier
) {
    KnockResultBadge(
        icon = vectorResource(Res.drawable.check_icon),
        outlineColor = MaterialTheme.colorScheme.extended.successOutline,
        iconTint = MaterialTheme.colorScheme.extended.success,
        modifier = modifier
    )
}

@Composable
@Preview
fun KnockSuccessIconPreview() {
    KnockTheme {
        KnockSuccessIcon()
    }
}
