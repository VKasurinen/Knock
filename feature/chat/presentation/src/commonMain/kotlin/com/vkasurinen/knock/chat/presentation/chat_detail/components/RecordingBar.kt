package com.vkasurinen.knock.chat.presentation.chat_detail.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.cancel
import knock.feature.chat.presentation.generated.resources.send
import com.vkasurinen.knock.chat.presentation.model.RecordingUi
import com.vkasurinen.knock.chat.presentation.util.formatDuration
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockCircleButton
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockGradientCircleButton
import com.vkasurinen.knock.core.designsystem.components.buttons.floatingSurface
import knock.core.designsystem.generated.resources.close_icon
import knock.core.designsystem.generated.resources.send_icon
import org.jetbrains.compose.resources.vectorResource
import knock.core.designsystem.generated.resources.Res as DesignSystemRes
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val WAVEFORM_BARS = 40

/**
 * Replaces the message text field while a voice message is being recorded.
 */
@Composable
fun RecordingBar(
    recording: RecordingUi,
    onCancelClick: () -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = MaterialTheme.colorScheme.extended
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KnockCircleButton(
            icon = vectorResource(DesignSystemRes.drawable.close_icon),
            contentDescription = stringResource(Res.string.cancel),
            onClick = onCancelClick,
            size = 48.dp,
            iconSize = 20.dp,
            iconTint = extended.textDestructive
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .floatingSurface(
                    shape = RoundedCornerShape(24.dp),
                    borderColor = extended.surfaceOutline
                )
                .padding(start = 18.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RecordingDot()
            Text(
                text = formatDuration(recording.elapsedMs),
                style = MaterialTheme.typography.labelSmall,
                color = extended.textPrimary
            )
            Waveform(
                amplitudes = recording.amplitudes,
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
            )
        }
        KnockGradientCircleButton(
            icon = vectorResource(DesignSystemRes.drawable.send_icon),
            contentDescription = stringResource(Res.string.send),
            onClick = onSendClick
        )
    }
}

@Composable
private fun RecordingDot() {
    val transition = rememberInfiniteTransition(label = "recording_dot")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recording_dot_alpha"
    )
    Box(
        modifier = Modifier
            .size(10.dp)
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.extended.textDestructive, CircleShape)
    )
}

@Composable
private fun Waveform(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.extended.tertiary
    val idleColor = MaterialTheme.colorScheme.extended.surfaceOutline

    Canvas(modifier = modifier) {
        val slotWidth = size.width / WAVEFORM_BARS
        val barWidth = slotWidth * 0.6f
        val minBarHeight = 3.dp.toPx()
        // Newest level on the right, empty slots on the left until the waveform fills up
        val padded = List(WAVEFORM_BARS - amplitudes.size) { null } + amplitudes

        padded.forEachIndexed { index, amplitude ->
            // Speech rarely reaches full scale, so boost quiet levels to keep the bars lively
            val level = amplitude?.let { kotlin.math.sqrt(it) } ?: 0f
            val barHeight = (size.height * level).coerceAtLeast(minBarHeight)
            drawRoundRect(
                color = if(amplitude != null) barColor else idleColor,
                topLeft = Offset(
                    x = index * slotWidth + (slotWidth - barWidth) / 2f,
                    y = (size.height - barHeight) / 2f
                ),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f)
            )
        }
    }
}

@Composable
@Preview
fun RecordingBarPreview() {
    KnockTheme {
        RecordingBar(
            recording = RecordingUi(
                elapsedMs = 7_000,
                amplitudes = List(25) { (it % 7) / 7f }
            ),
            onCancelClick = {},
            onSendClick = {}
        )
    }
}
