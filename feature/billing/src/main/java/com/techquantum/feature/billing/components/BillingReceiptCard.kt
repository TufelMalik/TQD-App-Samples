package com.techquantum.feature.billing.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.core.designsystem.formatRupees
import com.techquantum.core.designsystem.theme.TQDDimensions

@Composable
fun BillingReceiptCard(
    receipt: BillReceipt,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TQDDimensions.radiusCardLarge),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TQDDimensions.space2XLarge),
            verticalArrangement = Arrangement.spacedBy(TQDDimensions.spaceMedium),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text("TechQuantum Kitchen", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Monospace)
                Text("Ahmedabad · Counter showcase", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(8.dp))
                Text("Bill #${receipt.id.removePrefix("BILL-")}", fontFamily = FontFamily.Monospace)
                Text(receipt.table, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            receipt.lines.forEach { line ->
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(line.item.name, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        Text(formatRupees(line.total), style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                    }
                    Text(
                        "${line.quantity} × ${formatRupees(line.item.price)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontFamily = FontFamily.Monospace)
                Text(formatRupees(receipt.totals.total), fontFamily = FontFamily.Monospace)
            }
            Text("Payment: ${receipt.payment}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            Text(
                "Thank you. Come hungry, leave happy.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}
