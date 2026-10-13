package com.techquantum.feature.fooddelivery.ui

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.techquantum.feature.fooddelivery.domain.Dish
import com.techquantum.feature.fooddelivery.domain.FoodArt
import com.techquantum.feature.fooddelivery.domain.Restaurant
import kotlin.math.cos
import kotlin.math.sin

@Composable fun RestaurantArtwork(restaurant: Restaurant, modifier: Modifier = Modifier) =
    com.techquantum.core.designsystem.EditorialArtwork(false, foodPhotoCell(restaurant.artwork), "${restaurant.name}, editorial food imagery", modifier)
@Composable fun DishArtwork(dish: Dish, modifier: Modifier = Modifier) =
    com.techquantum.core.designsystem.EditorialArtwork(false, foodPhotoCell(dish.artwork), "${dish.name}, editorial food imagery", modifier)

private fun foodPhotoCell(art: FoodArt): Int = when (art) {
    FoodArt.THALI -> 0
    FoodArt.DOSA -> 1
    FoodArt.PIZZA -> 2
    FoodArt.BOWL, FoodArt.WRAP -> 3
    FoodArt.DESSERT -> 4
    FoodArt.DRINK -> 5
}

/** Plates and ingredients are rendered locally; no image request is needed during the pitch. */
@Composable private fun FoodArtwork(art: FoodArt, description: String, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    Canvas(modifier.semantics { contentDescription = description; role = Role.Image }) {
        val w = size.width; val h = size.height; val r = minOf(w, h) * .40f
        val center = Offset(w * .5f, h * .50f)
        fun at(x: Float, y: Float) = Offset(center.x + r * x, center.y + r * y)
        drawRect(c.surfaceContainerLow)
        drawRect(c.secondaryContainer.copy(alpha = .75f), Offset(w * .04f, 0f), Size(w * .19f, h))
        repeat(14) { index ->
            val x = w * (index / 12f)
            drawLine(c.outlineVariant.copy(alpha = .24f), Offset(x, 0f), Offset(x - h * .4f, h), 2f)
        }
        drawOval(c.onSurface.copy(alpha = .12f), Offset(center.x - r, center.y + r * .1f), Size(r * 2.1f, r * 1.04f))
        drawCircle(c.surfaceContainerLowest, r * 1.10f, center)
        drawCircle(c.outlineVariant.copy(alpha = .45f), r * 1.02f, center, style = Stroke(r * .02f))
        fun bowl(x: Float, y: Float, radius: Float, fill: androidx.compose.ui.graphics.Color) {
            drawCircle(c.onSurface.copy(alpha = .12f), r * radius * 1.12f, at(x + .03f, y + .04f))
            drawCircle(c.surfaceContainerHighest, r * radius * 1.12f, at(x, y))
            drawCircle(fill, r * radius, at(x, y))
            drawCircle(c.onSurface.copy(alpha = .12f), r * radius, at(x, y), style = Stroke(r * .012f))
        }
        when (art) {
            FoodArt.THALI -> {
                bowl(-.49f, -.38f, .29f, c.primary)
                bowl(.21f, -.48f, .27f, c.secondary)
                bowl(.65f, -.08f, .24f, c.tertiary)
                drawOval(c.secondaryContainer, at(-.55f, .14f), Size(r * 1.1f, r * .63f))
                drawOval(c.secondary, at(-.62f, .05f), Size(r * 1.04f, r * .66f))
                repeat(15) { index ->
                    drawCircle(c.onSecondary.copy(alpha = .16f), r * .035f, at(-.45f + (index % 5) * .18f, .20f + (index / 5) * .15f))
                }
                drawCircle(c.surfaceBright, r * .30f, at(.52f, .55f))
                repeat(25) { index ->
                    val angle = index * 2.4f; val grainR = (index % 6) * .043f
                    drawLine(c.outlineVariant, at(.52f + cos(angle) * grainR, .55f + sin(angle) * grainR), at(.54f + cos(angle) * grainR, .57f + sin(angle) * grainR), r * .012f)
                }
            }
            FoodArt.DOSA -> {
                rotate(-24f, center) {
                    val roll = Path().apply { moveTo(center.x - r * .86f, center.y - r * .29f); lineTo(center.x + r * .72f, center.y - r * .15f); lineTo(center.x + r * .81f, center.y + r * .39f); lineTo(center.x - r * .72f, center.y + r * .20f); close() }
                    drawPath(roll, c.secondary)
                    drawPath(roll, c.onSecondary.copy(alpha = .12f), style = Stroke(r * .018f))
                    repeat(9) { i -> drawLine(c.onSecondary.copy(alpha = .15f), at(-.67f + i * .15f, -.20f), at(-.63f + i * .15f, .23f), r * .022f) }
                }
                bowl(-.49f, .62f, .21f, c.primary)
                bowl(.01f, .72f, .18f, c.tertiaryContainer)
                bowl(.50f, .58f, .20f, c.tertiary)
            }
            FoodArt.PIZZA -> {
                drawCircle(c.secondary, r * .88f, center)
                drawCircle(c.primary, r * .76f, center)
                drawCircle(c.secondaryContainer, r * .70f, center)
                repeat(6) { slice ->
                    val angle = slice * Math.PI.toFloat() / 3
                    drawLine(c.onSecondaryContainer.copy(alpha = .20f), center, at(cos(angle) * .85f, sin(angle) * .85f), r * .022f)
                }
                repeat(15) { index ->
                    val angle = index * 2.40f; val distance = .26f + index % 3 * .16f
                    val position = at(cos(angle) * distance, sin(angle) * distance)
                    drawCircle(if (index % 3 == 0) c.tertiary else c.primary, r * .072f, position)
                    if (index % 3 == 0) drawCircle(c.onTertiary.copy(alpha = .35f), r * .030f, position)
                }
            }
            FoodArt.BOWL -> {
                drawCircle(c.secondaryContainer, r * .87f, center)
                repeat(28) { index ->
                    val angle = index * 2.399f; val distance = (index % 7) * .105f
                    drawOval(if (index % 3 == 0) c.tertiary else c.tertiaryContainer,
                        at(cos(angle) * distance - .1f, sin(angle) * distance - .06f), Size(r * .28f, r * .15f))
                }
                repeat(8) { index ->
                    drawRoundRect(if (index % 2 == 0) c.secondary else c.primary, at(-.48f + index % 4 * .28f, -.24f + index / 4 * .39f), Size(r * .20f, r * .18f), CornerRadius(r * .035f))
                }
                drawCircle(c.surfaceBright, r * .24f, at(.42f, .49f))
                drawCircle(c.tertiary, r * .08f, at(.39f, .47f))
            }
            FoodArt.WRAP -> rotate(-25f, center) {
                drawRoundRect(c.secondary, at(-.70f, -.26f), Size(r * 1.45f, r * .62f), CornerRadius(r * .23f))
                drawRoundRect(c.surfaceBright, at(-.58f, -.29f), Size(r * .76f, r * .68f), CornerRadius(r * .04f))
                repeat(5) { index -> drawLine(c.outlineVariant, at(-.49f + index * .13f, -.28f), at(-.49f + index * .13f, .37f), r * .013f) }
                drawOval(c.tertiary, at(.61f, -.16f), Size(r * .21f, r * .34f))
                drawCircle(c.primary, r * .065f, at(.70f, .02f))
            }
            FoodArt.DESSERT -> {
                repeat(9) { index ->
                    drawRoundRect(c.secondary, at(-.68f + index % 3 * .43f, -.58f + index / 3 * .41f), Size(r * .39f, r * .35f), CornerRadius(r * .045f))
                    drawCircle(c.tertiary, r * .055f, at(-.49f + index % 3 * .43f, -.42f + index / 3 * .41f))
                }
            }
            FoodArt.DRINK -> {
                drawRoundRect(c.secondaryContainer, at(-.43f, -.69f), Size(r * .86f, r * 1.40f), CornerRadius(r * .16f))
                drawRoundRect(c.secondary, at(-.36f, -.43f), Size(r * .72f, r * 1.05f), CornerRadius(r * .12f))
                drawOval(c.surfaceBright, at(-.42f, -.73f), Size(r * .84f, r * .23f))
                drawLine(c.primary, at(.12f, -.58f), at(.36f, -1.01f), r * .065f)
                drawLine(c.onSecondary.copy(alpha = .35f), at(-.20f, -.25f), at(-.20f, .36f), r * .035f)
            }
        }
    }
}
