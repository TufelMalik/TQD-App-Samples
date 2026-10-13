package com.techquantum.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.techquantum.core.designsystem.theme.TQDDimensions

/**
 * Luxury Elevated Card with squircle curvature and subtle tonal depth.
 */
@Composable
fun TQDElevatedCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(TQDDimensions.radiusCard),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: CardElevation = CardDefaults.elevatedCardElevation(
        defaultElevation = TQDDimensions.elevationSubtle,
        pressedElevation = TQDDimensions.elevationFloat,
    ),
    border: BorderStroke? = BorderStroke(
        TQDDimensions.borderHairline,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
    ),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
            elevation = elevation,
            border = border,
        ) {
            Box { content() }
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
            elevation = elevation,
            border = border,
        ) {
            Box { content() }
        }
    }
}

/**
 * Luxury Outlined Card with precision hairline border and light tonal tint.
 */
@Composable
fun TQDOutlinedCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(TQDDimensions.radiusCard),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    if (onClick != null) {
        OutlinedCard(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.outlinedCardColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
            border = BorderStroke(TQDDimensions.borderThin, borderColor),
        ) {
            Box { content() }
        }
    } else {
        OutlinedCard(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.outlinedCardColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
            border = BorderStroke(TQDDimensions.borderThin, borderColor),
        ) {
            Box { content() }
        }
    }
}
