package com.techquantum.feature.ecommerce.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techquantum.core.designsystem.QuantityStepper
import com.techquantum.core.designsystem.components.TQDElevatedCard
import com.techquantum.core.designsystem.formatRupees
import com.techquantum.core.designsystem.theme.TQDDimensions
import com.techquantum.feature.ecommerce.domain.CartItem
import com.techquantum.feature.ecommerce.ui.ProductArtwork

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TQDElevatedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TQDDimensions.spaceLarge),
            horizontalArrangement = Arrangement.spacedBy(TQDDimensions.spaceLarge),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProductArtwork(
                product = item.product,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(TQDDimensions.radiusMedium)),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(TQDDimensions.spaceTiny),
            ) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${item.color} · ${item.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatRupees(item.total),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(TQDDimensions.spaceSmall),
            ) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Remove ${item.product.name}",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
                QuantityStepper(
                    quantity = item.quantity,
                    onDecrease = onDecrease,
                    onIncrease = onIncrease,
                )
            }
        }
    }
}
