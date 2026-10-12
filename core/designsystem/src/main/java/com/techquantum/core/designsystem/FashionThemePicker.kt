@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.techquantum.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.getValue
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun FashionThemePicker(
    selectedTheme: FashionTheme,
    darkTheme: Boolean,
    onThemeSelected: (FashionTheme) -> Unit,
    onDarkThemeChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(0.88f)
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Make it yours", style = MaterialTheme.typography.headlineLarge)
            Text("Select a color palette. Adapts across all modules.", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !darkTheme, onClick = { onDarkThemeChanged(false) }, label = { Text("Light") })
                FilterChip(selected = darkTheme, onClick = { onDarkThemeChanged(true) }, label = { Text("Dark") })
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            // Measure the header and footer first; only the palette list consumes remaining height.
            // ModalBottomSheet applies the safe window insets around this bounded content.
            Column(
                Modifier.fillMaxWidth().weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()).selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                FashionTheme.entries.forEach { theme ->
                    val selected = theme == selectedTheme
                    val surfaceColor by androidx.compose.animation.animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        label = "theme_surface_color"
                    )
                    val borderColor by androidx.compose.animation.animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                        label = "theme_border_color"
                    )
                    Surface(
                        color = surfaceColor,
                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor),
                        shadowElevation = if (selected) 2.dp else 0.dp,
                    ) {
                        Row(
                            Modifier.fillMaxWidth().selectable(
                                selected = selected, role = Role.RadioButton, onClick = { onThemeSelected(theme) },
                            ).padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(theme.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                Text(theme.description, style = MaterialTheme.typography.bodySmall, color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant)
                                FashionStoreTheme(theme, darkTheme) { ThemePaletteSwatches() }
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)) { Text("Done", style = MaterialTheme.typography.titleSmall) }
        }
    }
}

/** Each preview receives its own provider so every swatch still consumes semantic Material tokens. */
@Composable
private fun ThemePaletteSwatches() {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(colors.primary, colors.secondary, colors.tertiary, colors.outline, colors.background).forEach { color ->
            Surface(
                modifier = Modifier.size(20.dp), shape = CircleShape,
                color = color, border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.6f)),
                shadowElevation = 1.dp,
            ) {}
        }
    }
}
