package com.techquantum.feature.fooddelivery.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.techquantum.core.designsystem.LuxuryBadge
import com.techquantum.core.designsystem.RatingPill
import com.techquantum.core.designsystem.theme.TQDDimensions
import com.techquantum.feature.fooddelivery.domain.Restaurant
import com.techquantum.feature.fooddelivery.ui.RestaurantArtwork

@Composable
fun PublicRestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit,
    heroModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TQDDimensions.radiusCardLarge),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Box {
            RestaurantArtwork(restaurant, heroModifier.fillMaxWidth().height(180.dp))
            Row(
                Modifier.fillMaxWidth().padding(TQDDimensions.spaceRegular),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LuxuryBadge(text = restaurant.cuisine.label)
                RatingPill(rating = restaurant.rating)
            }
        }
        Column(
            Modifier.padding(TQDDimensions.spaceRegular),
            verticalArrangement = Arrangement.spacedBy(TQDDimensions.spaceSmall),
        ) {
            Text(restaurant.name, style = MaterialTheme.typography.titleLarge)
            Text(
                restaurant.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TQDDimensions.spaceExtraSmall),
            ) {
                Icon(Icons.Default.Schedule, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${restaurant.etaMinutes}–${restaurant.etaMinutes + 5} min",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("·", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.Default.Place, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${restaurant.distanceKm} km",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("·", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(restaurant.cuisine.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
