@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.techquantum.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Compatibility entry for standalone Shop previews/tests. */
@Composable
fun FashionThemePicker(selectedTheme: FashionTheme, darkTheme: Boolean, onThemeSelected: (FashionTheme) -> Unit,
    onDarkThemeChanged: (Boolean) -> Unit, onDismiss: () -> Unit) {
    ShowcasePalettePicker(ShowcaseModule.SHOP, selectedTheme,
        if (darkTheme) AppearanceMode.DARK else AppearanceMode.LIGHT, darkTheme, false,
        { onThemeSelected(it ?: FashionTheme.LUXEVA) }, { onDarkThemeChanged(it.isDark(darkTheme)) }, {}, onDismiss)
}

@Composable
fun ShowcasePalettePicker(module: ShowcaseModule, selected: FashionTheme?, appearance: AppearanceMode,
    darkTheme: Boolean, reducedMotion: Boolean, onPalette: (FashionTheme?) -> Unit,
    onAppearance: (AppearanceMode) -> Unit, onReducedMotion: (Boolean) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)),
        containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.9f).padding(horizontal = 20.dp).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val moduleName = stringResource(module.labelRes())
            Text(stringResource(R.string.theme_title, moduleName), style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() })
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.theme_scope, moduleName), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.selectableGroup()) {
                    AppearanceMode.entries.forEach { mode ->
                        FilterChip(appearance == mode, { onAppearance(mode) }, label = { Text(stringResource(mode.labelRes)) })
                    }
                }
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .toggleable(reducedMotion, role = Role.Switch, onValueChange = onReducedMotion),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.reduce_motion), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Switch(reducedMotion, null)
                }
                HorizontalDivider()
                Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (module != ShowcaseModule.SHOP) PaletteRow(stringResource(R.string.theme_default), null,
                        module.colorScheme(darkTheme), selected == null) { onPalette(null) }
                    FashionTheme.entries.forEach { theme ->
                        PaletteRow(theme.displayName, theme.description, theme.colorScheme(darkTheme), selected == theme) { onPalette(theme) }
                    }
                }
            }
            HorizontalDivider()
            Button(onDismiss, Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(stringResource(R.string.theme_done)) }
        }
    }
}

@Composable
private fun PaletteRow(title: String, description: String?, preview: ColorScheme, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    // Atomic role changes keep foreground/background contrast safe during live selection.
    Surface(color = if (selected) colors.primaryContainer else colors.surfaceContainerLow,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant)) {
        Row(Modifier.fillMaxWidth().selectable(selected, role = Role.RadioButton, onClick = onClick).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RadioButton(selected, null, colors = RadioButtonDefaults.colors(
                selectedColor = colors.onPrimaryContainer, unselectedColor = colors.onSurfaceVariant))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (description != null) Text(description, style = MaterialTheme.typography.bodySmall)
                MaterialTheme(colorScheme = preview) {
                    val swatches = MaterialTheme.colorScheme
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(swatches.primary, swatches.secondary, swatches.tertiary, swatches.outline, swatches.background).forEach {
                            Surface(Modifier.size(20.dp), shape = CircleShape, color = it,
                                border = BorderStroke(1.dp, swatches.outline)) {}
                        }
                    }
                }
            }
        }
    }
}
