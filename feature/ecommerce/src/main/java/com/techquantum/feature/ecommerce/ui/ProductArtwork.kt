package com.techquantum.feature.ecommerce.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.techquantum.feature.ecommerce.domain.GarmentStyle
import com.techquantum.feature.ecommerce.domain.Product

/** Offline collection artwork. Its entire palette is supplied by the active fashion theme. */
@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun ProductArtwork(product: Product, modifier: Modifier = Modifier, variantIndex: Int = 0) {
    val scheme = MaterialTheme.colorScheme
    val defaultRole = when (product.artwork) {
        GarmentStyle.KURTA, GarmentStyle.DRESS -> 0
        GarmentStyle.SHIRT, GarmentStyle.JACKET -> 1
        GarmentStyle.HOODIE, GarmentStyle.TROUSERS -> 2
    }
    val roleIndex = (defaultRole + variantIndex) % 3
    val fabric by animateColorAsState(listOf(scheme.primary, scheme.secondary, scheme.tertiary)[roleIndex],
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(), label = "Fabric colour")
    val onFabric = listOf(scheme.onPrimary, scheme.onSecondary, scheme.onTertiary)[roleIndex]
    Canvas(modifier.semantics {
        contentDescription = "${product.name}, ${product.artwork.name.lowercase()} collection illustration"
        role = Role.Image
    }) {
        val w = size.width
        val h = size.height
        fun point(x: Float, y: Float) = Offset(w * x, h * y)
        fun garmentPath(points: List<Pair<Float, Float>>) = Path().apply {
            points.forEachIndexed { index, (x, y) ->
                if (index == 0) moveTo(w * x, h * y) else lineTo(w * x, h * y)
            }
            close()
        }

        drawRect(scheme.surfaceContainerLow)
        drawCircle(scheme.surfaceContainerHigh, w * .55f, point(.72f, .29f))
        drawRect(scheme.primaryContainer.copy(alpha = .3f), point(.04f, .07f), Size(w * .17f, h * .83f))
        for (line in 0..22) {
            val x = w * (.045f + line * .044f)
            drawLine(scheme.outlineVariant.copy(alpha = .12f), Offset(x, 0f), Offset(x, h), w * .002f)
        }
        drawOval(
            scheme.onSurface.copy(alpha = .08f),
            point(.20f, .89f),
            Size(w * .6f, h * .055f),
        )

        val silhouette = when (product.artwork) {
            GarmentStyle.KURTA -> garmentPath(listOf(
                .32f to .20f, .43f to .17f, .5f to .24f, .57f to .17f, .68f to .20f,
                .86f to .46f, .75f to .53f, .66f to .37f, .69f to .85f,
                .31f to .85f, .34f to .37f, .25f to .53f, .14f to .46f,
            ))
            GarmentStyle.SHIRT -> garmentPath(listOf(
                .32f to .24f, .43f to .20f, .5f to .27f, .57f to .20f, .68f to .24f,
                .85f to .49f, .75f to .57f, .66f to .41f, .67f to .78f,
                .50f to .82f, .33f to .78f, .34f to .41f, .25f to .57f, .15f to .49f,
            ))
            GarmentStyle.HOODIE -> garmentPath(listOf(
                .32f to .27f, .42f to .23f, .58f to .23f, .68f to .27f,
                .89f to .61f, .77f to .67f, .67f to .47f, .69f to .80f,
                .31f to .80f, .33f to .47f, .23f to .67f, .11f to .61f,
            ))
            GarmentStyle.JACKET -> garmentPath(listOf(
                .31f to .25f, .43f to .21f, .50f to .27f, .57f to .21f, .69f to .25f,
                .88f to .61f, .76f to .66f, .66f to .44f, .68f to .79f,
                .32f to .79f, .34f to .44f, .24f to .66f, .12f to .61f,
            ))
            GarmentStyle.DRESS -> garmentPath(listOf(
                .35f to .20f, .43f to .18f, .5f to .25f, .57f to .18f, .65f to .20f,
                .72f to .35f, .63f to .40f, .60f to .48f, .78f to .87f,
                .22f to .87f, .40f to .48f, .37f to .40f, .28f to .35f,
            ))
            GarmentStyle.TROUSERS -> garmentPath(listOf(
                .32f to .20f, .68f to .20f, .71f to .86f, .53f to .86f,
                .5f to .47f, .47f to .86f, .29f to .86f,
            ))
        }
        if (product.artwork == GarmentStyle.HOODIE) {
            drawOval(fabric.copy(alpha = .9f), point(.38f, .13f), Size(w * .24f, h * .23f))
            drawOval(onFabric.copy(alpha = .18f), point(.42f, .16f), Size(w * .16f, h * .15f))
        }
        drawPath(silhouette, Brush.linearGradient(
            listOf(fabric, fabric.copy(alpha = .76f), fabric),
            start = point(.2f, .2f), end = point(.8f, .86f),
        ))
        drawPath(silhouette, scheme.onSurface.copy(alpha = .13f), style = Stroke(w * .004f))

        // Soft folds, tailored edges and seams make each silhouette recognisable at card size.
        val seam = onFabric.copy(alpha = .30f)
        when (product.artwork) {
            GarmentStyle.KURTA, GarmentStyle.SHIRT, GarmentStyle.JACKET -> {
                drawLine(seam, point(.5f, .26f), point(.5f, if (product.artwork == GarmentStyle.KURTA) .8f else .76f), w * .005f)
                val collar = garmentPath(listOf(.43f to .18f, .5f to .26f, .45f to .32f, .39f to .22f))
                drawPath(collar, onFabric.copy(alpha = .16f))
                drawPath(garmentPath(listOf(.57f to .18f, .5f to .26f, .55f to .32f, .61f to .22f)), onFabric.copy(alpha = .16f))
                repeat(if (product.artwork == GarmentStyle.KURTA) 4 else 6) {
                    drawCircle(onFabric.copy(alpha = .68f), w * .007f, point(.515f, .34f + it * .062f))
                }
                drawPath(garmentPath(listOf(.54f to .37f, .62f to .37f, .615f to .46f, .55f to .46f)), onFabric.copy(alpha = .1f))
                drawLine(seam, point(.54f, .37f), point(.62f, .37f), w * .004f)
            }
            GarmentStyle.HOODIE -> {
                drawLine(seam, point(.47f, .28f), point(.45f, .46f), w * .006f)
                drawLine(seam, point(.53f, .28f), point(.55f, .46f), w * .006f)
                drawPath(garmentPath(listOf(.4f to .56f, .6f to .56f, .64f to .7f, .36f to .7f)), onFabric.copy(alpha = .1f))
                drawLine(seam, point(.36f, .7f), point(.64f, .7f), w * .004f)
                drawLine(seam, point(.32f, .77f), point(.68f, .77f), w * .009f)
            }
            GarmentStyle.DRESS -> {
                drawLine(seam, point(.40f, .48f), point(.60f, .48f), w * .012f)
                for (fold in 0..4) {
                    drawLine(onFabric.copy(alpha = .14f), point(.43f + fold * .034f, .49f), point(.28f + fold * .11f, .85f), w * .004f)
                }
            }
            GarmentStyle.TROUSERS -> {
                drawLine(seam, point(.32f, .24f), point(.68f, .24f), w * .008f)
                drawLine(seam, point(.5f, .24f), point(.5f, .44f), w * .004f)
                drawLine(seam, point(.36f, .31f), point(.31f, .38f), w * .004f)
                drawLine(seam, point(.64f, .31f), point(.69f, .38f), w * .004f)
            }
        }
        drawPath(garmentPath(listOf(.36f to .42f, .41f to .5f, .39f to .76f, .35f to .77f)), onFabric.copy(alpha = .08f))
        drawPath(garmentPath(listOf(.60f to .43f, .65f to .43f, .67f to .75f, .61f to .74f)), scheme.onSurface.copy(alpha = .08f))
    }
}
