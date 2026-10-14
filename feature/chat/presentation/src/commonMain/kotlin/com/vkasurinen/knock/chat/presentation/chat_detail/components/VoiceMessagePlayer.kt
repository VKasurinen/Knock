package com.vkasurinen.knock.chat.presentation.chat_detail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.pause
import knock.feature.chat.presentation.generated.resources.pause_icon
import knock.feature.chat.presentation.generated.resources.play
import knock.feature.chat.presentation.generated.resources.play_icon
import com.vkasurinen.knock.chat.presentation.util.formatDuration
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private val PLAYER_WIDTH = 220.dp

/**
 * Play/pause, a progress bar that can be tapped or dragged to seek, and the remaining time.
 *
 * @param positionMs current position, only meaningful while this message is the loaded one
 */
@Composable
fun VoiceMessagePlayer(
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onPlayPauseClick: () -> Unit,
    onSeek: (positionMs: Long) -> Unit,
    modifier: Modifier = Modifier,
    uploadProgress: Float? = null
) {
    // Follows the finger while dragging, the player is only told where to seek on release
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val playedFraction = dragFraction
        ?: if(durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val remainingMs = if(positionMs > 0 || isPlaying) durationMs - positionMs else durationMs
    // Inside a chat bubble LocalContentColor is the bubble's text color
    val extended = MaterialTheme.colorScheme.extended
    val onBubble = LocalContentColor.current
    val playIconColor = if(onBubble == extended.onBubbleOut) extended.bubbleOutEnd else extended.bubbleIn

    Column(
        modifier = modifier.width(PLAYER_WIDTH),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledIconButton(
                onClick = onPlayPauseClick,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = onBubble,
                    contentColor = playIconColor
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = vectorResource(if(isPlaying) Res.drawable.pause_icon else Res.drawable.play_icon),
                    contentDescription = stringResource(if(isPlaying) Res.string.pause else Res.string.play),
                    modifier = Modifier.size(16.dp)
                )
            }

            SeekBar(
                fraction = playedFraction,
                onFractionChange = { dragFraction = it },
                onSeekFinished = { fraction ->
                    dragFraction = null
                    onSeek((fraction * durationMs).toLong())
                },
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
            )

            Text(
                text = formatDuration(remainingMs),
                style = MaterialTheme.typography.labelSmall,
                color = onBubble.copy(alpha = 0.8f)
            )
        }

        if(uploadProgress != null) {
            LinearProgressIndicator(
                progress = { uploadProgress },
                modifier = Modifier.fillMaxWidth(),
                color = onBubble,
                trackColor = onBubble.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun SeekBar(
    fraction: Float,
    onFractionChange: (Float) -> Unit,
    onSeekFinished: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val progressColor = LocalContentColor.current
    val trackColor = progressColor.copy(alpha = 0.3f)
    var latestFraction by remember { mutableStateOf(fraction) }
    // The gesture detectors outlive recompositions, so they need the latest callbacks
    val currentOnFractionChange by rememberUpdatedState(onFractionChange)
    val currentOnSeekFinished by rememberUpdatedState(onSeekFinished)

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    currentOnSeekFinished((offset.x / size.width).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        latestFraction = (offset.x / size.width).coerceIn(0f, 1f)
                        currentOnFractionChange(latestFraction)
                    },
                    onDragEnd = { currentOnSeekFinished(latestFraction) },
                    onDragCancel = { currentOnSeekFinished(latestFraction) },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        latestFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        currentOnFractionChange(latestFraction)
                    }
                )
            }
    ) {
        val trackHeight = 4.dp.toPx()
        val top = (size.height - trackHeight) / 2f
        val radius = CornerRadius(trackHeight / 2f)

        drawRoundRect(
            color = trackColor,
            topLeft = Offset(0f, top),
            size = Size(size.width, trackHeight),
            cornerRadius = radius
        )
        drawRoundRect(
            color = progressColor,
            topLeft = Offset(0f, top),
            size = Size(size.width * fraction, trackHeight),
            cornerRadius = radius
        )
        drawCircle(
            color = progressColor,
            radius = 6.dp.toPx(),
            center = Offset(size.width * fraction, size.height / 2f)
        )
    }
}

@Composable
@Preview
fun VoiceMessagePlayerPreview() {
    KnockTheme {
        VoiceMessagePlayer(
            isPlaying = true,
            positionMs = 4_000,
            durationMs = 12_000,
            onPlayPauseClick = {},
            onSeek = {}
        )
    }
}
