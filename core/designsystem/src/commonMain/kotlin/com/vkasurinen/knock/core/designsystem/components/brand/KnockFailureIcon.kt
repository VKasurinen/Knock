package com.vkasurinen.knock.core.designsystem.components.brand

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import knock.core.designsystem.generated.resources.Res
import knock.core.designsystem.generated.resources.close_icon
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun KnockFailureIcon(
    modifier: Modifier = Modifier
) {
    KnockResultBadge(
        icon = vectorResource(Res.drawable.close_icon),
        outlineColor = MaterialTheme.colorScheme.extended.destructiveSecondaryOutline,
        iconTint = MaterialTheme.colorScheme.extended.textDestructive,
        modifier = modifier
    )
}

@Composable
@Preview
fun KnockFailureIconPreview() {
    KnockTheme {
        KnockFailureIcon()
    }
}
