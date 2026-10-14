package com.vkasurinen.knock.chat.presentation.chat_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.removed_from_chat_banner
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RemovedFromChatBanner(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(Res.string.removed_from_chat_banner),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.extended.textSecondary,
        textAlign = TextAlign.Center,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.extended.surfaceLower,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    )
}

@Composable
@Preview
fun RemovedFromChatBannerPreview() {
    KnockTheme {
        RemovedFromChatBanner(
            modifier = Modifier.fillMaxWidth()
        )
    }
}
