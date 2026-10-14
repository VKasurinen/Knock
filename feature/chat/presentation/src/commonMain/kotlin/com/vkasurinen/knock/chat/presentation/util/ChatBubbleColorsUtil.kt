package com.vkasurinen.knock.chat.presentation.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.vkasurinen.knock.core.designsystem.theme.extended

/**
 * Stable sender color (sender/1-5 tokens) for names in group chats.
 */
@Composable
fun getSenderColorForUser(userId: String): Color {
    val colorPool = MaterialTheme.colorScheme.extended.senderColors
    val index = userId.hashCode().toUInt() % colorPool.size.toUInt()

    return colorPool[index.toInt()]
}
