package com.vkasurinen.knock.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign
import com.vkasurinen.knock.core.designsystem.components.brand.KnockBrandLogo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import knock.feature.chat.presentation.generated.resources.Res
import com.vkasurinen.knock.core.designsystem.theme.extended
import com.vkasurinen.knock.core.presentation.util.DeviceConfiguration
import com.vkasurinen.knock.core.presentation.util.currentDeviceConfiguration

@Composable
fun EmptyListSection(
    title: String,
    description: String?,
    modifier: Modifier = Modifier
) {
    val configuration = currentDeviceConfiguration()
    val illustrationSize = if(configuration == DeviceConfiguration.MOBILE_LANDSCAPE) 112.dp else 160.dp
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(illustrationSize)
                .background(MaterialTheme.colorScheme.extended.surfaceHigher, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            KnockBrandLogo(
                onBrand = false,
                size = illustrationSize * 0.52f
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.extended.textPrimary,
            textAlign = TextAlign.Center
        )
        if(description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.extended.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}
