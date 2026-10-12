package com.techquantum.feature.ecommerce.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.techquantum.core.designsystem.components.TQDElevatedCard
import com.techquantum.core.designsystem.formatRupees
import com.techquantum.core.designsystem.theme.TQDDimensions

@Composable
fun OrderSummaryCard(
    subtotal: Int,
    deliveryFee: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    TQDElevatedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TQDDimensions.spaceLarge),
            verticalArrangement = Arrangement.spacedBy(TQDDimensions.spaceMedium),
        ) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                Text(formatRupees(subtotal), style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Delivery", style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (deliveryFee == 0) "Free" else formatRupees(deliveryFee),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (deliveryFee == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    formatRupees(total),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
