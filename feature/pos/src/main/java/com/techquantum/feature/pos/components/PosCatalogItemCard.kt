package com.techquantum.feature.pos.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.techquantum.core.billingengine.MenuItem
import com.techquantum.core.designsystem.DietaryIndicator
import com.techquantum.core.designsystem.formatRupees
import com.techquantum.core.designsystem.theme.TQDDimensions

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PosCatalogItemCard(
    item: MenuItem,
    justAdded: Boolean,
    pulse: Int,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    artwork: @Composable (MenuItem, Modifier) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val scale = remember { Animatable(1f) }
    val motion = MaterialTheme.motionScheme
    LaunchedEffect(pulse) {
        if (justAdded) {
            scale.snapTo(.94f)
            scale.animateTo(1f, motion.fastSpatialSpec())
        }
    }
    Surface(
        onClick = onAdd,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 176.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .semantics { contentDescription = "Add ${item.name}" },
        shape = RoundedCornerShape(TQDDimensions.radiusCard),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, if (justAdded) colors.primary else colors.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Column(Modifier.padding(TQDDimensions.spaceRegular), verticalArrangement = Arrangement.spacedBy(TQDDimensions.spaceMediumSmall)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                artwork(item, Modifier.size(50.dp))
                Surface(
                    shape = CircleShape,
                    color = colors.primaryContainer,
                    modifier = Modifier.size(34.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Add, null, Modifier.size(18.dp), tint = colors.onPrimaryContainer)
                    }
                }
            }
            DietaryIndicator(item.isVeg)
            Text(formatRupees(item.price), style = MaterialTheme.typography.titleLarge)
        }
    }
}
