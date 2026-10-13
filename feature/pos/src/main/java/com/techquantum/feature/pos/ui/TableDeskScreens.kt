@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
package com.techquantum.feature.pos.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.techquantum.core.designsystem.*
import com.techquantum.core.designsystem.theme.TQDDimensions
import com.techquantum.feature.pos.presentation.*
import com.techquantum.core.designsystem.R as DesignR

@Composable
internal fun TableDeskWorkspace(state: PosState, onIntent: (PosIntent) -> Unit, onCharge: () -> Unit) {
    var billVisible by rememberSaveable { mutableStateOf(false) }
    BoxWithConstraints(Modifier.fillMaxSize().testTag("pos_workspace")) {
        val wide = maxWidth >= 840.dp && maxHeight >= 600.dp && LocalDensity.current.fontScale <= 1.5f
        if (wide) Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CounterHeader(state, onIntent, true)
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                DeskCatalogue(state, onIntent, Modifier.weight(.62f).fillMaxHeight(), showTables = false)
                Surface(Modifier.weight(.38f).fillMaxHeight(), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                    BillPanel(state, onIntent, onCharge)
                }
            }
        } else Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (billVisible) {
                TextButton({ billVisible = false }, Modifier.heightIn(min = 56.dp)) { Text(stringResource(DesignR.string.selected_copy_7)) }
                BillPanel(state, onIntent, onCharge, Modifier.weight(1f))
            } else {
                DeskCatalogue(state, onIntent, Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton({ billVisible = true }, Modifier.weight(1f).heightIn(min = 56.dp)) {
                            Text("Review ${state.bill.count} items · ${formatRupees(state.bill.totals.total)}")
                        }
                        Button(onCharge, enabled = state.bill.lines.isNotEmpty() && !state.charging, modifier = Modifier.heightIn(min = 56.dp)) {
                            Text("Charge ${formatRupees(state.bill.totals.total)}")
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun DeskCatalogue(state: PosState, onIntent: (PosIntent) -> Unit, modifier: Modifier, showTables: Boolean = true) {
    val large = LocalDensity.current.fontScale > 1.25f
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (!large && showTables) DeskTables(state.bill.table, { onIntent(PosIntent.SelectTable(it)) })
        // Table selection scrolls with the menu when large text needs more space.
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!large) LazyColumn(Modifier.width(102.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(state.categories, key = { it }, contentType = { "category" }) { category ->
                    Surface(onClick = { onIntent(PosIntent.SelectCategory(category)) }, shape = RoundedCornerShape(14.dp),
                        color = if (state.category == category) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).semantics { selected = state.category == category }) {
                        Text(category, Modifier.padding(10.dp), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            LazyColumn(Modifier.weight(1f).fillMaxHeight().testTag("pos_item_grid"), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                if (large && showTables) item(key = "tables", contentType = "tables") {
                    DeskTables(state.bill.table, { onIntent(PosIntent.SelectTable(it)) })
                }
                item(key = "search", contentType = "search") {
                    OutlinedTextField(state.query, { onIntent(PosIntent.Search(it)) }, Modifier.fillMaxWidth().testTag("pos_search"),
                        label = { Text(stringResource(DesignR.string.selected_copy_8)) }, singleLine = true, shape = RoundedCornerShape(18.dp))
                }
                if (large) item(key = "categories", contentType = "categories") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.categories, key = { it }, contentType = { "category" }) { category ->
                            FilterChip(state.category == category, { onIntent(PosIntent.SelectCategory(category)) }, label = { Text(category) }, modifier = Modifier.heightIn(min = 56.dp))
                        }
                    }
                }
                when {
                    state.loading -> items(3, key = { "skeleton-$it" }, contentType = { "skeleton" }) { SkeletonShimmer(Modifier.fillMaxWidth().height(96.dp)) }
                    state.filteredCatalog.isEmpty() -> item(key = "empty", contentType = "empty") {
                        Text(stringResource(DesignR.string.selected_copy_9), style = MaterialTheme.typography.titleMedium)
                        TextButton({ onIntent(PosIntent.Search("")); onIntent(PosIntent.SelectCategory("All")) }, Modifier.heightIn(min = 56.dp)) { Text(stringResource(DesignR.string.selected_copy_10)) }
                    }
                    else -> items(state.filteredCatalog, key = { it.id }, contentType = { "item" }) { item ->
                        Surface(onClick = { onIntent(PosIntent.AddItem(item.id)) }, shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp).semantics { contentDescription = "Add ${item.name}" }) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    EditorialArtwork(false, counterPhotoCell(item.category), null, Modifier.size(42.dp))
                                    Text(item.name, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                                    Icon(Icons.Outlined.Add, null, Modifier.size(20.dp))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(formatRupees(item.price), style = MaterialTheme.typography.titleMedium)
                                    DietaryIndicator(item.isVeg)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeskTables(selectedTable: String, onSelect: (String) -> Unit) {
    val tables = listOf("Table 01", "Table 02", "Table 03", "Table 04")
    var serviceOpen by remember { mutableStateOf(false) }
    Column(Modifier.testTag("desk_tables"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(DesignR.string.desk_tables), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Box {
                TextButton({ serviceOpen = true }, Modifier.heightIn(min = 56.dp)) {
                    Text(if (selectedTable in tables) "Dine in" else selectedTable)
                }
                DropdownMenu(serviceOpen, { serviceOpen = false }) {
                    listOf("Counter 01", "Takeaway").forEach { service ->
                        DropdownMenuItem(text = { Text(service) }, onClick = { onSelect(service); serviceOpen = false })
                    }
                }
            }
        }
        tables.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { table ->
                    Surface(onClick = { onSelect(table) }, shape = RoundedCornerShape(14.dp),
                        color = if (selectedTable == table) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, if (selectedTable == table) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp).semantics { selected = selectedTable == table }) {
                        Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.TableRestaurant, null, Modifier.size(20.dp))
                            Text(table, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

internal fun counterPhotoCell(category: String): Int = when (category) {
    "Breakfast", "Breads" -> 1
    "Drinks" -> 5
    "Desserts" -> 4
    "Starters" -> 3
    else -> 0
}

@Composable
internal fun DeskPaymentProgress() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf(DesignR.string.desk_review, DesignR.string.desk_method, DesignR.string.desk_finish).forEachIndexed { index, label ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(shape = RoundedCornerShape(999.dp), color = if (index == 1) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh) {
                    Text("${index + 1}", Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                }
                Text(stringResource(label), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
internal fun ChargeDiscount(percent: Int, enabled: Boolean, onDiscount: (Int) -> Unit) {
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(DesignR.string.desk_discount), style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (listOf(0, 5, 10, 15, 20) + percent).distinct().sorted().forEach { value ->
                    FilterChip(percent == value, { onDiscount(value) }, enabled = enabled,
                        label = { Text(if (value == 0) stringResource(DesignR.string.desk_discount_none) else stringResource(DesignR.string.desk_discount_percent, value)) },
                        modifier = Modifier.heightIn(min = TQDDimensions.operatorTouchTargetMin).testTag("charge_discount_$value"))
                }
            }
            Text(stringResource(DesignR.string.desk_discount_note), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun DeskCharge(state: PosState, onIntent: (PosIntent) -> Unit, onBack: () -> Unit) {
    val totals = if (state.charging) state.receipt?.totals ?: state.bill.totals else state.bill.totals
    var expanded by rememberSaveable { mutableStateOf(false) }
    if (state.bill.lines.isEmpty() && state.receipt == null) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text(stringResource(DesignR.string.selected_copy_11), style = MaterialTheme.typography.headlineMedium)
            Button(onBack, Modifier.heightIn(min = 56.dp)) { Text(stringResource(DesignR.string.selected_copy_12)) }
        }
        return
    }
    Scaffold(contentWindowInsets = WindowInsets(0,0,0,0), bottomBar = {
        ActionFooter {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(DesignR.string.desk_no_payment), style = MaterialTheme.typography.bodySmall)
                Button({ onIntent(PosIntent.Charge) }, Modifier.fillMaxWidth().heightIn(min = 60.dp), enabled = !state.charging) {
                    Text(if (state.charging) "Recording demo payment…" else "Confirm ${state.payment} · ${formatRupees(totals.total)}")
                }
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).verticalScroll(rememberScrollState()).testTag("pos_charge"),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = TQDDimensions.formMaxWidth).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(state.bill.table, style = MaterialTheme.typography.titleLarge)
                DeskPaymentProgress()
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Column(Modifier.padding(16.dp)) {
                        TextButton({ expanded = !expanded }, Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                            Text(stringResource(DesignR.string.desk_order), Modifier.weight(1f)); Text(if (expanded) "−" else "+")
                        }
                        if (expanded) (if (state.charging) state.receipt?.lines.orEmpty() else state.bill.lines).forEach { line ->
                            PosAmountLine("${line.item.name} × ${line.quantity}", line.total)
                        }
                    }
                }
                Text(formatRupees(totals.total), Modifier.align(Alignment.CenterHorizontally).testTag("charge_total"), style = MaterialTheme.typography.displayMedium)
                ChargeDiscount(state.bill.discountPercent, !state.charging) { onIntent(PosIntent.ApplyDiscount(it)) }
                Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Cash", "Card", "UPI").forEach { payment ->
                        Surface(shape = RoundedCornerShape(18.dp),
                            color = if (state.payment == payment) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, if (state.payment == payment) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
                            Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).selectable(state.payment == payment, enabled = !state.charging,
                                role = Role.RadioButton, onClick = { onIntent(PosIntent.SelectPayment(payment)) }).padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(when(payment) { "Cash" -> Icons.Outlined.Payments; "Card" -> Icons.Outlined.CreditCard; else -> Icons.Outlined.QrCodeScanner }, null)
                                Text(payment, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                                RadioButton(state.payment == payment, null, enabled = !state.charging)
                            }
                        }
                    }
                }
                PosTotals(totals)
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive }) }
            }
        }
    }
}
