package com.vkasurinen.knock.chat.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.size
import knock.core.designsystem.generated.resources.close_icon
import knock.core.designsystem.generated.resources.Res as DesignSystemRes
import org.jetbrains.compose.resources.vectorResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.cancel
import com.vkasurinen.knock.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource

@Composable
fun ManageChatHeaderRow(
    title: String,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(
                start = 24.dp,
                end = 12.dp,
                top = 16.dp,
                bottom = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.extended.textPrimary,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onCloseClick
        ) {
            Icon(
                imageVector = vectorResource(DesignSystemRes.drawable.close_icon),
                contentDescription = stringResource(Res.string.cancel),
                tint = MaterialTheme.colorScheme.extended.textPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}