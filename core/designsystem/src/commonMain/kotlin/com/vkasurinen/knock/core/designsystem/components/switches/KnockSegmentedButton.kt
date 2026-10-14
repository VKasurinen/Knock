package com.vkasurinen.knock.core.designsystem.components.switches

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import knock.core.designsystem.generated.resources.Res
import knock.core.designsystem.generated.resources.check_icon
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Segmented control ("Language Switch" in Figma). The selected segment uses btn/primary
 * so it stands out in both themes.
 */
@Composable
fun <T> KnockSegmentedButton(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    val extended = MaterialTheme.colorScheme.extended
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(extended.surfaceHigher, RoundedCornerShape(14.dp))
            .border(1.dp, extended.surfaceOutline, RoundedCornerShape(14.dp))
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val containerColor by animateColorAsState(
                if(isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
            )
            val contentColor by animateColorAsState(
                if(isSelected) MaterialTheme.colorScheme.onPrimary else extended.textSecondary
            )
            val shape = RoundedCornerShape(10.dp)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .then(
                        if(isSelected) {
                            Modifier.shadow(2.dp, shape, ambientColor = SegmentShadowColor, spotColor = SegmentShadowColor)
                        } else Modifier
                    )
                    .clip(shape)
                    .background(containerColor)
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelect(option) }
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(isSelected) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.check_icon),
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor
                )
            }
        }
    }
}

private val SegmentShadowColor = Color(0x1F1A120D)

@Composable
@Preview
fun KnockSegmentedButtonPreview() {
    KnockTheme {
        KnockSegmentedButton(
            options = listOf("Suomi", "English"),
            selected = "Suomi",
            onSelect = {},
            label = { it }
        )
    }
}
