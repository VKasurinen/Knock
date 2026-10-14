package com.vkasurinen.knock.chat.presentation.chat_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vkasurinen.knock.core.designsystem.theme.extended
import com.vkasurinen.knock.core.designsystem.theme.labelXSmall
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.not_sent_try_again
import knock.feature.chat.presentation.generated.resources.reload_icon
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

/**
 * "Not sent · Try again" pill shown under a failed outgoing message.
 */
@Composable
fun MessageFailedHint(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.extended.textDestructive
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.extended.surfaceContainer)
            .clickable(onClick = onRetryClick)
            .padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = vectorResource(Res.drawable.reload_icon),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = stringResource(Res.string.not_sent_try_again),
            style = MaterialTheme.typography.labelXSmall,
            color = color
        )
    }
}
