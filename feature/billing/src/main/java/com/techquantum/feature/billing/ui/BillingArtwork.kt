@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.techquantum.feature.billing.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.techquantum.core.billingengine.MenuItem
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Offline counter-menu illustrations, drawn entirely from the active module's semantic tokens. */
@Composable
fun BillingFoodArtwork(item: MenuItem, modifier: Modifier = Modifier) {
    com.techquantum.core.designsystem.EditorialArtwork(false, when (item.category) {
        "Breakfast", "Breads" -> 1
        "Starters" -> 3
        "Desserts" -> 4
        "Drinks" -> 5
        else -> 0
    }, "${item.name}, editorial food imagery", modifier)
}

@Composable
private fun IllustratedBillingFood(item: MenuItem, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Canvas(modifier.semantics { contentDescription = "${item.name} illustration" }) {
        val centre = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * .38f
        drawCircle(colors.surfaceContainerHighest, radius * 1.18f, centre)
        drawCircle(colors.surface, radius, centre)
        drawCircle(colors.outlineVariant, radius, centre, style = Stroke(radius * .035f))
        if (item.category == "Drinks") {
            drawRoundRect(colors.secondaryContainer, Offset(centre.x - radius * .36f, centre.y - radius * .65f),
                Size(radius * .72f, radius * 1.30f), CornerRadius(radius * .15f))
            drawLine(colors.onSecondaryContainer, Offset(centre.x + radius * .10f, centre.y - radius * .95f),
                Offset(centre.x - radius * .06f, centre.y + radius * .15f), radius * .055f)
        } else {
            val pieces = if (item.category == "Breads") 3 else if (item.category == "Desserts") 4 else 6
            repeat(pieces) { index ->
                val angle = index * PI * 2 / pieces
                val point = centre + Offset((cos(angle) * radius * .48f).toFloat(), (sin(angle) * radius * .40f).toFloat())
                drawOval(if (index % 2 == 0) colors.primaryContainer else colors.tertiaryContainer,
                    point - Offset(radius * .22f, radius * .19f), Size(radius * .44f, radius * .38f))
                drawLine(colors.onTertiaryContainer.copy(alpha = .35f), point - Offset(radius * .10f, 0f),
                    point + Offset(radius * .10f, 0f), radius * .025f)
            }
            drawCircle(colors.secondary, radius * .10f, centre)
            repeat(4) { index ->
                drawCircle(colors.primary, radius * .035f,
                    centre + Offset((index - 1.5f) * radius * .19f, radius * .63f))
            }
        }
    }
}

/** Clearly labelled decorative QR pattern; it contains no payment address or scannable payload. */
@Composable
fun DemoQrPlaceholder(modifier: Modifier = Modifier) {
    val ink = MaterialTheme.colorScheme.onSurface
    val paper = MaterialTheme.colorScheme.surface
    Canvas(modifier.semantics { contentDescription = "Demo UPI QR placeholder, not for payments" }) {
        drawRect(paper)
        val unit = size.minDimension / 15f
        repeat(13) { row -> repeat(13) { column ->
            if ((row * 3 + column * 5) % 7 < 3) drawRect(ink,
                Offset((column + 1) * unit, (row + 1) * unit), Size(unit * .8f, unit * .8f))
        } }
        // Crossing the pattern makes the preview visibly unsuitable as a payment QR.
        drawLine(paper, Offset(unit * 2, unit * 2), Offset(unit * 13, unit * 13), unit * 2)
    }
}

@Composable
fun PaperFeedAnimation(running: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val progress = remember { Animatable(0f) }
    val motion = MaterialTheme.motionScheme
    LaunchedEffect(running) {
        if (running) {
            progress.snapTo(0f)
            progress.animateTo(1f, motion.slowSpatialSpec())
        } else progress.snapTo(0f)
    }
    Box(modifier.fillMaxWidth().height(112.dp).padding(8.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxWidth().height(96.dp).semantics {
            contentDescription = if (running) "Preparing demo receipt" else "Demo receipt printer"
        }) {
            val width = size.height * .92f
            val left = (size.width - width) / 2f
            val top = size.height * .25f
            drawRoundRect(colors.primaryContainer, Offset(left, top), Size(width, size.height * .55f), CornerRadius(12.dp.toPx()))
            drawRoundRect(colors.onPrimaryContainer, Offset(left + width * .12f, top + size.height * .12f),
                Size(width * .76f, size.height * .07f), CornerRadius(3.dp.toPx()))
            val paperTop = top + size.height * .18f + progress.value * size.height * .18f
            drawRect(colors.surface, Offset(left + width * .18f, paperTop), Size(width * .64f, size.height * .44f))
            repeat(4) { line ->
                val y = paperTop + size.height * (.07f + line * .065f)
                drawLine(colors.outline, Offset(left + width * .27f, y), Offset(left + width * .73f, y), 1.dp.toPx())
            }
            drawCircle(colors.primary, 3.dp.toPx(), Offset(left + width * .84f, top + size.height * .40f))
        }
    }
}
