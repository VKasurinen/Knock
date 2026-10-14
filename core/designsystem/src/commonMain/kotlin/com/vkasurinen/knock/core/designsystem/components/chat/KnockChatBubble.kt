package com.vkasurinen.knock.core.designsystem.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import com.vkasurinen.knock.core.designsystem.theme.labelXSmall
import com.vkasurinen.knock.core.designsystem.theme.titleXSmall
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

enum class BubbleDirection {
    /** Received message: cream bubble, tail bottom-left */
    IN,
    /** Own message: wood gradient bubble, tail bottom-right */
    OUT
}

/**
 * Knock message bubble ("Message Bubble" in Figma).
 *
 * @param senderName shown above the text in group chats (incoming only).
 * @param quote slot for a reply quote. Not used until the backend supports replies.
 * @param messageStatus delivery status icon shown after the time (outgoing only).
 */
@Composable
fun KnockChatBubble(
    messageContent: String,
    formattedDateTime: String,
    direction: BubbleDirection,
    modifier: Modifier = Modifier,
    senderName: String? = null,
    senderColor: Color = Color.Unspecified,
    maxWidth: Dp = 288.dp,
    messageStatus: @Composable (RowScope.() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    quote: @Composable (() -> Unit)? = null,
    attachmentContent: @Composable (() -> Unit)? = null
) {
    val extended = MaterialTheme.colorScheme.extended
    val shape = when(direction) {
        BubbleDirection.IN -> RoundedCornerShape(
            topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 6.dp
        )
        BubbleDirection.OUT -> RoundedCornerShape(
            topStart = 20.dp, topEnd = 20.dp, bottomEnd = 6.dp, bottomStart = 20.dp
        )
    }
    val contentColor = when(direction) {
        BubbleDirection.IN -> extended.onBubbleIn
        BubbleDirection.OUT -> extended.onBubbleOut
    }
    val metaColor = when(direction) {
        BubbleDirection.IN -> extended.textTertiary
        BubbleDirection.OUT -> extended.onBubbleOut
    }
    val outBrush = remember(extended.bubbleOutStart, extended.bubbleOutEnd) {
        outgoingBubbleBrush(extended.bubbleOutStart, extended.bubbleOutEnd)
    }

    Column(
        modifier = modifier
            .widthIn(max = maxWidth)
            .width(IntrinsicSize.Max)
            .shadow(
                elevation = if(direction == BubbleDirection.OUT) 3.dp else 2.dp,
                shape = shape,
                ambientColor = BubbleShadowColor,
                spotColor = BubbleShadowColor
            )
            .clip(shape)
            .then(
                when(direction) {
                    BubbleDirection.IN -> Modifier
                        .background(extended.bubbleIn)
                        .border(1.dp, Color.White.copy(alpha = 0.5f), shape)
                    BubbleDirection.OUT -> Modifier.background(outBrush)
                }
            )
            .then(
                if(onLongClick != null) {
                    Modifier.combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = extended.surfaceOutline),
                        onLongClick = onLongClick,
                        onClick = {}
                    )
                } else Modifier
            )
            .padding(start = 14.dp, end = 14.dp, top = 9.dp, bottom = 7.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Attachments (voice player, images) inside the bubble read their color from here
        CompositionLocalProvider(LocalContentColor provides contentColor) {
        if(senderName != null && direction == BubbleDirection.IN) {
            Text(
                text = senderName,
                style = MaterialTheme.typography.titleXSmall,
                color = if(senderColor.isSpecified) senderColor else extended.textPrimary,
                maxLines = 1
            )
        }
        quote?.invoke()
        attachmentContent?.invoke()
        if(messageContent.isNotBlank() || attachmentContent == null) {
            Text(
                text = messageContent,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formattedDateTime,
                style = MaterialTheme.typography.labelXSmall,
                color = metaColor
            )
            if(messageStatus != null) {
                CompositionLocalProvider(LocalContentColor provides metaColor) {
                    messageStatus()
                }
            }
        }
        }
    }
}

private val BubbleShadowColor = Color(0x241A120D)

/**
 * CSS-like linear gradient at 164° with the end color reached at 71.4 %, as in the Figma bubble.
 */
private fun outgoingBubbleBrush(start: Color, end: Color): ShaderBrush {
    return object : ShaderBrush() {
        override fun createShader(size: Size): Shader {
            val angle = 164.29 * PI / 180.0
            val dirX = sin(angle).toFloat()
            val dirY = -cos(angle).toFloat()
            val length = abs(size.width * dirX) + abs(size.height * dirY)
            val center = Offset(size.width / 2f, size.height / 2f)
            val half = Offset(dirX * length / 2f, dirY * length / 2f)
            return LinearGradientShader(
                from = center - half,
                to = center + half,
                colors = listOf(start, end),
                colorStops = listOf(0f, 0.714f)
            )
        }
    }
}

@Composable
@Preview
fun KnockChatBubbleInPreview() {
    KnockTheme {
        KnockChatBubble(
            messageContent = "Moikka! Löysin vihdoin sen puukahvilan, josta puhuttiin ☕",
            senderName = "Eero Laine",
            senderColor = MaterialTheme.colorScheme.extended.senderColors[1],
            formattedDateTime = "10.15",
            direction = BubbleDirection.IN,
        )
    }
}

@Composable
@Preview
fun KnockChatBubbleOutPreview() {
    KnockTheme(darkTheme = true) {
        KnockChatBubble(
            messageContent = "Sopii mulle! Varaan pöydän neljälle.",
            formattedDateTime = "10.15",
            direction = BubbleDirection.OUT,
        )
    }
}
