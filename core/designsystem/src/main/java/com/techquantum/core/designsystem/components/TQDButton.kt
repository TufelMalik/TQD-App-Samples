package com.techquantum.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.techquantum.core.designsystem.theme.TQDDimensions

/**
 * Luxury Primary Action Button with squircle or pill shape and standard 48dp+ touch target.
 */
@Composable
fun TQDPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(TQDDimensions.radiusRegular),
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    minHeight: Dp = TQDDimensions.buttonHeightStandard,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = TQDDimensions.space2XLarge,
        vertical = TQDDimensions.spaceRegular,
    ),
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = minHeight),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.38f),
            disabledContentColor = contentColor.copy(alpha = 0.38f),
        ),
        contentPadding = contentPadding,
        content = content,
    )
}

/**
 * Luxury Outlined Secondary Button.
 */
@Composable
fun TQDOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(TQDDimensions.radiusRegular),
    borderColor: Color = MaterialTheme.colorScheme.outline,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    minHeight: Dp = TQDDimensions.buttonHeightStandard,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = TQDDimensions.space2XLarge,
        vertical = TQDDimensions.spaceRegular,
    ),
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = minHeight),
        enabled = enabled,
        shape = shape,
        border = BorderStroke(TQDDimensions.borderThin, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
        ),
        contentPadding = contentPadding,
        content = content,
    )
}
