package com.vkasurinen.knock.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import knock.core.designsystem.generated.resources.check_check_icon
import knock.feature.chat.presentation.generated.resources.delivered
import knock.feature.chat.presentation.generated.resources.read
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vkasurinen.knock.chat.domain.models.ChatMessageDeliveryStatus
import knock.core.designsystem.generated.resources.alert_icon
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.check_icon
import knock.feature.chat.presentation.generated.resources.failed
import knock.feature.chat.presentation.generated.resources.loading_icon
import knock.feature.chat.presentation.generated.resources.sending
import knock.feature.chat.presentation.generated.resources.sent
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import knock.core.designsystem.generated.resources.Res as DesignSystemRes

/**
 * Delivery status icon shown after the time inside an outgoing bubble.
 * Tinted with the bubble's meta color (LocalContentColor).
 */
@Composable
fun MessageStatus(
    status: ChatMessageDeliveryStatus,
    modifier: Modifier = Modifier
) {
    val (description, icon) = when(status) {
        ChatMessageDeliveryStatus.SENDING -> stringResource(Res.string.sending) to
                vectorResource(Res.drawable.loading_icon)
        ChatMessageDeliveryStatus.SENT -> stringResource(Res.string.sent) to
                vectorResource(Res.drawable.check_icon)
        ChatMessageDeliveryStatus.DELIVERED -> stringResource(Res.string.delivered) to
                vectorResource(DesignSystemRes.drawable.check_check_icon)
        ChatMessageDeliveryStatus.READ -> stringResource(Res.string.read) to
                vectorResource(DesignSystemRes.drawable.check_check_icon)
        ChatMessageDeliveryStatus.FAILED -> stringResource(Res.string.failed) to
                vectorResource(DesignSystemRes.drawable.alert_icon)
    }
    Icon(
        imageVector = icon,
        contentDescription = description,
        // Read messages stand out from delivered ones by color ("chat/read-receipt" in Figma)
        tint = if(status == ChatMessageDeliveryStatus.READ) {
            MaterialTheme.colorScheme.extended.readReceipt
        } else LocalContentColor.current,
        modifier = modifier.size(14.dp)
    )
}
