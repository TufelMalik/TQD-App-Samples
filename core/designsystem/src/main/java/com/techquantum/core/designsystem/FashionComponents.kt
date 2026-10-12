@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.techquantum.core.designsystem

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

/** Stable-geometry first paint with pearlescent luxury shimmer. Its colors track the active Material surface hierarchy. */
@Composable
fun SkeletonShimmer(modifier: Modifier = Modifier) {
    val base = MaterialTheme.colorScheme.surfaceContainerHigh
    val highlight = MaterialTheme.colorScheme.surfaceContainerLowest
    val mid = MaterialTheme.colorScheme.surfaceContainer
    val transition = rememberInfiniteTransition(label = "Product skeleton")
    val phase by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "Shimmer sweep",
    )
    Box(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .semantics {
                contentDescription = "Loading products"
                progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
            }
            .drawWithCache {
                val width = size.width.coerceAtLeast(1f)
                val startX = phase * width
                val brush = Brush.linearGradient(
                    listOf(base, mid, highlight, mid, base),
                    start = Offset(startX, 0f),
                    end = Offset(startX + width, size.height),
                )
                onDrawBehind { drawRect(brush) }
            },
    )
}

/** Luxury badge pill with editorial uppercase tracking for haute tags and culinary highlights. */
@Composable
fun LuxuryBadge(
    text: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(0.5.dp, contentColor.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
        ) {
            if (icon != null) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = contentColor,
                )
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            )
        }
    }
}

/** Luxury rating chip with micro star glyph and jewel-container styling. */
@Composable
fun RatingPill(
    rating: Double,
    reviewCount: Int? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Text(
                text = String.format(java.util.Locale.US, "%.1f", rating),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )
            if (reviewCount != null) {
                Text(
                    text = "($reviewCount)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
fun RatingPill(
    rating: Float,
    reviewCount: Int? = null,
    modifier: Modifier = Modifier,
) {
    RatingPill(rating.toDouble(), reviewCount, modifier)
}

/** Luxury discount savings pill with emphasized primary tint. */
@Composable
fun DiscountPill(
    percent: Int,
    modifier: Modifier = Modifier,
) {
    if (percent <= 0) return
    Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Text(
            text = "$percent% OFF",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        )
    }
}

@Composable
fun QuantityStepper(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    touchTargetSize: Dp = 48.dp,
) {
    val scale = remember { Animatable(1f) }
    val motion = MaterialTheme.motionScheme
    LaunchedEffect(quantity) {
        scale.snapTo(1.10f)
        scale.animateTo(1f, animationSpec = motion.fastSpatialSpec())
    }
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onDecrease,
                enabled = quantity > 1,
                modifier = Modifier.size(touchTargetSize).semantics { contentDescription = "Decrease quantity" },
            ) { Text("−", style = MaterialTheme.typography.titleLarge) }
            AnimatedContent(
                targetState = quantity,
                modifier = Modifier.width(28.dp).graphicsLayer { scaleX = scale.value; scaleY = scale.value },
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInVertically(motion.fastSpatialSpec()) { it * direction } +
                        fadeIn(motion.fastEffectsSpec())) togetherWith
                        (slideOutVertically(motion.fastSpatialSpec()) { -it * direction } +
                            fadeOut(motion.fastEffectsSpec()))
                },
                label = "Quantity",
            ) { value ->
                Text(
                    value.toString(),
                    modifier = Modifier.width(28.dp).semantics { contentDescription = "Quantity $value" },
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                )
            }
            IconButton(
                onClick = onIncrease,
                modifier = Modifier.size(touchTargetSize).semantics { contentDescription = "Increase quantity" },
            ) { Text("+", style = MaterialTheme.typography.titleLarge) }
        }
    }
}

fun formatRupees(amount: Int): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).apply {
    maximumFractionDigits = 0
    minimumFractionDigits = 0
}.format(amount)

/** Roll-up uses the expressive spring rather than a fixed easing curve. */
@Composable
fun AnimatedMoney(amount: Int, modifier: Modifier = Modifier) {
    val motion = MaterialTheme.motionScheme
    AnimatedContent(
        targetState = amount,
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        transitionSpec = {
            val direction = if (targetState > initialState) 1 else -1
            (slideInVertically(motion.fastSpatialSpec()) { it / 2 * direction } +
                fadeIn(motion.fastEffectsSpec())) togetherWith
                (slideOutVertically(motion.fastSpatialSpec()) { -it / 2 * direction } +
                    fadeOut(motion.fastEffectsSpec()))
        },
        label = "Price total",
    ) { value ->
        Text(formatRupees(value), style = MaterialTheme.typography.titleLargeEmphasized, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun CartBadge(count: Int, modifier: Modifier = Modifier) {
    if (count <= 0) return
    val motion = MaterialTheme.motionScheme
    val scale = remember { Animatable(1f) }
    LaunchedEffect(count) {
        scale.snapTo(1.22f)
        scale.animateTo(1f, animationSpec = motion.fastSpatialSpec())
    }
    Badge(
        modifier = modifier.defaultMinSize(20.dp, 20.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .semantics { contentDescription = "$count items in bag"; liveRegion = LiveRegionMode.Polite },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) { Text(if (count > 99) "99+" else count.toString()) }
}
