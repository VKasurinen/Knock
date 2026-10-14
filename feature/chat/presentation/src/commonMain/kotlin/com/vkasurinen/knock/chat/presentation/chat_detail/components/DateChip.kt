package com.vkasurinen.knock.chat.presentation.chat_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vkasurinen.knock.core.designsystem.theme.extended
import com.vkasurinen.knock.core.designsystem.theme.labelXSmall

@Composable
fun DateChip(
    date: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = date,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.extended.surfaceContainer,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(
                vertical = 4.dp,
                horizontal = 12.dp
            ),
        style = MaterialTheme.typography.labelXSmall,
        color = MaterialTheme.colorScheme.extended.textTertiary
    )
}
