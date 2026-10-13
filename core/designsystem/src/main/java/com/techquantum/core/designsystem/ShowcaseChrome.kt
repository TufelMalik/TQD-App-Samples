@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.techquantum.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.techquantum.core.designsystem.theme.TQDDimensions

val LocalActionFooterHeightChanged = staticCompositionLocalOf<(Int) -> Unit> { {} }

@Composable
fun ActionFooter(modifier: Modifier = Modifier, onHeightChanged: (Int) -> Unit = LocalActionFooterHeightChanged.current,
    content: @Composable () -> Unit) {
    Surface(modifier.fillMaxWidth().onSizeChanged { onHeightChanged(it.height) },
        color = MaterialTheme.colorScheme.surfaceContainer, contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), content = content)
}

@Composable
fun PaletteAction(onClick: () -> Unit = LocalShowPalette.current, targetSize: Dp = 48.dp,
    description: String = stringResource(R.string.palette_action)) {
    IconButton(onClick, Modifier.size(targetSize)) { Icon(Icons.Outlined.Palette, description) }
}

/** Chrome may shorten visually, but Text semantics retain the complete route title. */
@Composable
fun CompactModuleTitle(title: String, subtitle: String? = null) {
    Column {
        Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (subtitle != null && LocalConfiguration.current.screenWidthDp >= 400 && LocalDensity.current.fontScale <= 1.15f) {
            Text(subtitle, style = MaterialTheme.typography.labelSmall, maxLines = 1,
                overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** A single modal owner. The draft remains composed while the palette temporarily replaces its window. */
@Composable
fun ShowcaseSheet(onDismiss: () -> Unit, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val scroll = rememberScrollState()
    val showPalette = LocalShowPalette.current
    if (LocalPaletteVisible.current) return
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))) {
        Column(modifier.fillMaxWidth().heightIn(max = (LocalConfiguration.current.screenHeightDp * .85f).dp)
            .imePadding().verticalScroll(scroll).padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { PaletteAction(showPalette, TQDDimensions.operatorTouchTargetMin) }
            content()
        }
    }
}

/** Stable, labeled dietary meaning uses a scoped semantic scheme, never the client accent. */
@Composable
fun DietaryIndicator(vegetarian: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val scoped = remember(scheme, vegetarian) { dietaryColorScheme(scheme, vegetarian) }
    MaterialTheme(colorScheme = scoped) {
        val colors = MaterialTheme.colorScheme
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(Modifier.size(16.dp), color = colors.surface, shape = RoundedCornerShape(3.dp),
                border = BorderStroke(1.dp, colors.primary)) {
                Canvas(Modifier.padding(3.dp)) {
                    if (vegetarian) drawCircle(colors.primary)
                    else drawPath(Path().apply {
                        moveTo(size.width / 2, 0f); lineTo(size.width, size.height); lineTo(0f, size.height); close()
                    }, colors.primary)
                }
            }
            Text(stringResource(if (vegetarian) R.string.diet_vegetarian else R.string.diet_non_vegetarian),
                style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        }
    }
}

internal fun dietaryColorScheme(parent: ColorScheme, vegetarian: Boolean): ColorScheme {
    val anchor = if (vegetarian) FoodVegGreen else FoodNonVegBrown
    val target = if (contrastRatio(Color.Black, parent.surface) >= contrastRatio(Color.White, parent.surface)) Color.Black else Color.White
    var indicator = anchor
    for (step in 1..10) {
        if (contrastRatio(indicator, parent.surface) >= 3f) break
        indicator = lerp(anchor, target, step / 10f)
    }
    return parent.copy(primary = indicator)
}
