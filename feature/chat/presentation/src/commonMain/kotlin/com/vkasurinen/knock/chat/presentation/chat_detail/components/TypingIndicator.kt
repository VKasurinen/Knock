package com.vkasurinen.knock.chat.presentation.chat_detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.presentation.util.UiText
import com.vkasurinen.knock.core.designsystem.theme.extended
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Three-dot "typing" bubble. [text] (e.g. "Alice is typing…") is used for accessibility
 * and controls the visibility.
 */
@Composable
fun TypingIndicator(
    text: UiText?,
    modifier: Modifier = Modifier
) {
    // Keep the last description while the indicator animates out
    var lastText by remember { mutableStateOf(text) }
    if(text != null) {
        lastText = text
    }
    val description = lastText?.asString().orEmpty()

    AnimatedVisibility(
        visible = text != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            TypingDotsBubble(
                modifier = Modifier.semantics { contentDescription = description }
            )
        }
    }
}

@Composable
private fun TypingDotsBubble(
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(topStart = 20.dp, topEnd = 17.dp, bottomEnd = 17.dp, bottomStart = 6.dp)
    val transition = rememberInfiniteTransition()
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart
        )
    )
    Row(
        modifier = modifier
            .shadow(2.dp, shape, ambientColor = DotsShadowColor, spotColor = DotsShadowColor)
            .background(MaterialTheme.colorScheme.extended.bubbleIn, shape)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        repeat(3) { index ->
            val distance = (phase - index).let { if(it < 0) it + 3f else it }
            val alpha = if(distance < 1f) 1f else 0.45f
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(alpha)
                    .background(MaterialTheme.colorScheme.extended.wallpaperPattern, CircleShape)
            )
        }
    }
}

private val DotsShadowColor = Color(0x1A1A120D)

@Composable
@Preview
fun TypingIndicatorPreview() {
    KnockTheme {
        TypingIndicator(text = UiText.DynamicString("Alice is typing…"))
    }
}
