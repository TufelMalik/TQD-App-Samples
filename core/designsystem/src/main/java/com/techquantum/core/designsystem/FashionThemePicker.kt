@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.techquantum.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
            Modifier.fillMaxWidth().heightIn(max = 680.dp)
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Make it yours", style = MaterialTheme.typography.headlineLarge)
            Text("Five collections. Your kind of style.", style = MaterialTheme.typography.bodyMedium,
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FashionTheme.entries.forEach { theme ->
                    val selected = theme == selectedTheme
                    Surface(
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        shape = MaterialTheme.shapes.medium,
                        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    ) {
                        Row(
                            Modifier.fillMaxWidth().selectable(
                                selected = selected, role = Role.RadioButton, onClick = { onThemeSelected(theme) },
                            ).padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(theme.displayName, style = MaterialTheme.typography.titleSmall)
                                Text(theme.description, style = MaterialTheme.typography.bodySmall)
                                FashionStoreTheme(theme, darkTheme) { ThemePaletteSwatches() }
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Done") }
        }
    }
}

/** Each preview receives its own provider so every swatch still consumes semantic Material tokens. */
@Composable
private fun ThemePaletteSwatches() {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        listOf(colors.primary, colors.secondary, colors.tertiary, colors.outline, colors.background).forEach { color ->
            Surface(
                modifier = Modifier.size(17.dp), shape = CircleShape,
                color = color, border = BorderStroke(1.dp, colors.outlineVariant),
            ) { Box(Modifier.background(color)) }
        }
    }
}
