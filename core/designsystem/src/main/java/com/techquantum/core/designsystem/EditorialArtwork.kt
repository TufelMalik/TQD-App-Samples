package com.techquantum.core.designsystem

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import android.graphics.Canvas as AndroidCanvas

// using Context.getDrawable to avoid an extra dependency import

/** Locally bundled editorial photography. Source regions are cropped at draw time, never stretched. */
@Composable
fun EditorialArtwork(fashion: Boolean, cell: Int, description: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val resId = if (fashion) R.drawable.fashion_editorial else R.drawable.food_editorial
    // Load drawable and convert to ImageBitmap safely (handles vector and bitmap drawables)
    val configuration = LocalConfiguration.current
    val image: ImageBitmap? = remember(resId, configuration) {
        val drawable = context.getDrawable(resId) ?: return@remember null
        val bitmap: Bitmap = when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            else -> {
                val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 1
                val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 1
                val bmp = createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = AndroidCanvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp
            }
        }
        bitmap.asImageBitmap()
    }
    if (image == null) return
    Canvas(modifier.then(if (description == null) Modifier else Modifier.semantics {
        contentDescription = description
        role = Role.Image
    })) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas
        val width = image.width / 3
        val height = image.height / 2
        val index = cell.coerceIn(0, 5)
        val aspect = size.width / size.height
        val sourceWidth = minOf(width.toFloat(), height * aspect).roundToInt().coerceAtLeast(1)
        val sourceHeight = minOf(height.toFloat(), width / aspect).roundToInt().coerceAtLeast(1)
        drawImage(image,
            srcOffset = IntOffset(index % 3 * width + (width - sourceWidth) / 2, index / 3 * height + (height - sourceHeight) / 2),
            srcSize = IntSize(sourceWidth, sourceHeight),
            dstSize = IntSize(size.width.roundToInt().coerceAtLeast(1), size.height.roundToInt().coerceAtLeast(1)))
    }
}
