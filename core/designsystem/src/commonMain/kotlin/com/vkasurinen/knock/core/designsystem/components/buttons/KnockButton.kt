package com.vkasurinen.knock.core.designsystem.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended


enum class KnockButtonStyle {
    PRIMARY,
    DESTRUCTIVE_PRIMARY,
    SECONDARY,
    DESTRUCTIVE_SECONDARY,
    TEXT
}

@Composable
fun KnockButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: KnockButtonStyle = KnockButtonStyle.PRIMARY,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val extended = MaterialTheme.colorScheme.extended

    val containerColor = when(style) {
        KnockButtonStyle.PRIMARY -> if(isHovered) extended.primaryHover else MaterialTheme.colorScheme.primary
        KnockButtonStyle.DESTRUCTIVE_PRIMARY -> if(isHovered) extended.destructiveHover else MaterialTheme.colorScheme.error
        KnockButtonStyle.SECONDARY,
        KnockButtonStyle.DESTRUCTIVE_SECONDARY -> if(isHovered) extended.surfaceHigher else extended.secondaryFill
        KnockButtonStyle.TEXT -> if(isHovered) extended.surfaceHigher else Color.Transparent
    }
    val contentColor = when(style) {
        KnockButtonStyle.PRIMARY -> MaterialTheme.colorScheme.onPrimary
        KnockButtonStyle.DESTRUCTIVE_PRIMARY -> MaterialTheme.colorScheme.onError
        KnockButtonStyle.SECONDARY -> extended.textPrimary
        KnockButtonStyle.DESTRUCTIVE_SECONDARY -> extended.textDestructive
        KnockButtonStyle.TEXT -> extended.tertiary
    }
    val colors = ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = if(style == KnockButtonStyle.TEXT) {
            Color.Transparent
        } else extended.disabledFill,
        disabledContentColor = extended.textDisabled
    )

    val border = when {
        style == KnockButtonStyle.TEXT -> null
        !enabled -> BorderStroke(1.5.dp, extended.disabledOutline)
        style == KnockButtonStyle.SECONDARY -> BorderStroke(1.5.dp, extended.secondaryOutline)
        style == KnockButtonStyle.DESTRUCTIVE_SECONDARY -> BorderStroke(1.5.dp, extended.destructiveSecondaryOutline)
        else -> null
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 44.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = colors,
        border = border,
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(15.dp)
                    .alpha(
                        alpha = if(isLoading) 1f else 0f
                    ),
                strokeWidth = 1.5.dp,
                color = LocalContentColor.current
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    8.dp,
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(
                    if(isLoading) 0f else 1f
                )
            ) {
                leadingIcon?.invoke()
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
@Preview
fun KnockPrimaryButtonPreview() {
    KnockTheme(
        darkTheme = true
    ) {
        KnockButton(
            text = "Hello world!",
            onClick = {},
            style = KnockButtonStyle.PRIMARY
        )
    }
}

@Composable
@Preview
fun KnockSecondaryButtonPreview() { 
    KnockTheme(
        darkTheme = true
    ) {
        KnockButton(
            text = "Hello world!",
            onClick = {},
            style = KnockButtonStyle.SECONDARY,
            isLoading = true
        )
    }
}

@Composable
@Preview
fun KnockDestructivePrimaryButtonPreview() {
    KnockTheme(
        darkTheme = true
    ) {
        KnockButton(
            text = "Hello world!",
            onClick = {},
            style = KnockButtonStyle.DESTRUCTIVE_PRIMARY
        )
    }
}

@Composable
@Preview
fun KnockDestructiveSecondaryButtonPreview() {
    KnockTheme(
        darkTheme = true
    ) {
        KnockButton(
            text = "Hello world!",
            onClick = {},
            style = KnockButtonStyle.DESTRUCTIVE_SECONDARY
        )
    }
}

@Composable
@Preview
fun KnockTextButtonPreview() {
    KnockTheme(
        darkTheme = true
    ) {
        KnockButton(
            text = "Hello world!",
            onClick = {},
            style = KnockButtonStyle.TEXT
        )
    }
}