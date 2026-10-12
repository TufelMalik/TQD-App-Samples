@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.techquantum.feature.billing.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.techquantum.core.billingengine.BillLine
import com.techquantum.core.billingengine.BillTotals
import com.techquantum.core.billingengine.MenuItem
import com.techquantum.core.designsystem.*
import com.techquantum.feature.billing.presentation.*

@Composable
fun FoodSelectionScreen(
    state: BillingState,
    onIntent: (BillingIntent) -> Unit,
    onReview: () -> Unit,
    onActionBarHeightChanged: (Int) -> Unit = {},
) {
    Scaffold(
        modifier = Modifier.testTag("billing_selection"),
        bottomBar = { BillingActionBar("${state.bill.count} items in the running bill", state.bill.totals.total,
            "Review bill", state.bill.lines.isNotEmpty(), onReview, onActionBarHeightChanged) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LuxuryBadge("Kitchen Counter & Quick Billing")
                Text("Fresh choices. Fast billing.", style = MaterialTheme.typography.headlineMedium)
                Text("Tap to add. Hold an item to choose quantity and preparation notes.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { onIntent(BillingIntent.Search(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Find food") },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    leadingIcon = { Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = { if (state.query.isNotEmpty()) IconButton({ onIntent(BillingIntent.Search("")) }) {
                        Icon(Icons.Outlined.Close, "Clear food search")
                    } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                )
            }
            CategoryRail(state, onIntent)
            if (state.loading) {
                LazyVerticalGrid(GridCells.Adaptive(156.dp), Modifier.weight(1f), contentPadding = PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(4) { SkeletonShimmer(Modifier.fillMaxWidth().height(228.dp)) }
                }
            } else if (state.visibleItems.isEmpty()) {
                Column(Modifier.weight(1f).fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Outlined.SearchOff, null, Modifier.size(48.dp))
                    Text("No food matches", style = MaterialTheme.typography.titleLarge)
                    TextButton({ onIntent(BillingIntent.Search("")); onIntent(BillingIntent.SelectCategory("All")) }) { Text("Clear filters") }
                }
            } else {
                LazyVerticalGrid(GridCells.Adaptive(156.dp), Modifier.weight(1f).testTag("billing-menu"),
                    contentPadding = PaddingValues(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.visibleItems, key = MenuItem::id) { item ->
                        FoodSelectionCard(item, state.bill.lines.firstOrNull { it.key == item.id }?.quantity ?: 0, onIntent)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRail(state: BillingState, onIntent: (BillingIntent) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(listOf("All") + state.bill.catalog.map { it.category }.distinct()) { category ->
            FilterChip(
                selected = state.category == category,
                onClick = { onIntent(BillingIntent.SelectCategory(category)) },
                label = { Text(category) },
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.heightIn(min = 48.dp)
            )
        }
    }
}

@Composable
private fun FoodSelectionCard(item: MenuItem, count: Int, onIntent: (BillingIntent) -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, if (count > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(Modifier.fillMaxWidth().combinedClickable(
            role = Role.Button,
            onClickLabel = "Add ${item.name}",
            onLongClickLabel = "Customize ${item.name}",
            onClick = { onIntent(BillingIntent.Add(item.id)) },
            onLongClick = { onIntent(BillingIntent.Edit(item.id, false)) },
        ).padding(14.dp)) {
            BillingFoodArtwork(item, Modifier.fillMaxWidth().height(92.dp).clip(RoundedCornerShape(14.dp)))
            Spacer(Modifier.height(8.dp))
            FoodIndicators(item)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.name, Modifier.weight(1f).heightIn(min = 42.dp), style = MaterialTheme.typography.titleSmall,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                IconButton({ onIntent(BillingIntent.Edit(item.id, false)) }, Modifier.size(48.dp)) {
                    Icon(Icons.Outlined.EditNote, "Customize ${item.name}")
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatRupees(item.price), style = MaterialTheme.typography.titleMedium)
                FilledTonalButton(
                    onClick = { onIntent(BillingIntent.Add(item.id)) },
                    modifier = Modifier.heightIn(min = 44.dp).testTag("bill-add-${item.id}"),
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) { Text("Add") }
            }
            if (count > 0) {
                Spacer(Modifier.height(4.dp))
                LuxuryBadge("In bill: $count", containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun FoodIndicators(item: MenuItem) {
    val colors = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            Modifier.size(12.dp),
            shape = RoundedCornerShape(3.dp),
            border = BorderStroke(1.dp, if (item.isVeg) colors.tertiary else colors.primary),
            color = colors.surface
        ) {
            Box(Modifier.padding(2.5.dp).background(if (item.isVeg) colors.tertiary else colors.primary, CircleShape))
        }
        Text(if (item.isVeg) "Veg" else "Non-veg", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        Text("·", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        Text(when (item.spiceLevel) { 0 -> "No spice"; 1 -> "🌶️ Mild"; 2 -> "🌶️🌶️ Medium"; else -> "🌶️🌶️🌶️ Hot" },
            style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
    }
}

@Composable
fun RunningBillScreen(
    state: BillingState,
    onIntent: (BillingIntent) -> Unit,
    onPreview: () -> Unit,
    onBrowse: () -> Unit,
    onActionBarHeightChanged: (Int) -> Unit = {},
) {
    if (state.bill.lines.isEmpty()) {
        LaunchedEffect(Unit) { onActionBarHeightChanged(0) }
        Column(Modifier.fillMaxSize().testTag("billing_running_bill").padding(24.dp), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.ReceiptLong, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Text("Your next bill starts here", style = MaterialTheme.typography.headlineSmall)
            Text("Select food here or on the POS tab. Both use the same running bill.", textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
            Button(onBrowse, Modifier.heightIn(min = 52.dp), shape = RoundedCornerShape(18.dp)) { Text("Select food") }
            if (state.previewReceipt != null) TextButton(onPreview, Modifier.heightIn(min = 52.dp)) { Text("View last receipt") }
        }
        return
    }
    Scaffold(modifier = Modifier.testTag("billing_running_bill"),
        bottomBar = { BillingActionBar("Total including demo GST", state.bill.totals.total, "Preview receipt", true,
            onPreview, onActionBarHeightChanged) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Running bill", style = MaterialTheme.typography.headlineMedium)
                    LuxuryBadge("${state.bill.count} items · Shared with POS")
                }
            }
            item {
                Text("Counter or table", style = MaterialTheme.typography.titleSmall)
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items((listOf(state.bill.table, "Takeaway", "Table 01", "Table 02")).distinct()) { table ->
                        FilterChip(
                            selected = state.bill.table == table,
                            onClick = { onIntent(BillingIntent.SelectTable(table)) },
                            label = { Text(table) },
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }
            }
            items(state.bill.lines, key = BillLine::key) { line ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            BillingFoodArtwork(line.item, Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)))
                            Column(Modifier.weight(1f)) {
                                Text(line.item.name, style = MaterialTheme.typography.titleMedium)
                                Text("${formatRupees(line.item.price)} each", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall)
                                if (line.notes.isNotBlank()) Text(line.notes, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton({ onIntent(BillingIntent.Remove(line.key)) }, Modifier.size(48.dp)) {
                                Icon(Icons.Outlined.DeleteOutline, "Remove ${line.item.name}")
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            QuantityStepper(line.quantity, { onIntent(BillingIntent.ChangeQuantity(line.key, -1)) },
                                { onIntent(BillingIntent.ChangeQuantity(line.key, 1)) }, touchTargetSize = 48.dp)
                            AnimatedMoney(line.total)
                        }
                        TextButton({ onIntent(BillingIntent.Edit(line.key, true)) }, Modifier.heightIn(min = 40.dp)) { Text("Edit quantity & notes") }
                    }
                }
            }
            item {
                Text("Demo discount", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0, 5, 10).forEach { percent ->
                        FilterChip(
                            selected = state.bill.discountPercent == percent,
                            onClick = { onIntent(BillingIntent.ApplyDiscount(percent)) },
                            label = { Text(if (percent == 0) "None" else "$percent%") },
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(Modifier.padding(18.dp)) {
                        BillingTotals(state.bill.totals, state.bill.discountPercent)
                    }
                }
            }
            item { Text("Demo GST is 5% after discount, rounded to whole rupees. All charges are shown before printing.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
            item {
                OutlinedButton(
                    onClick = onBrowse,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) { Text("Add more food") }
            }
        }
    }
}

@Composable
private fun BillingActionBar(
    label: String,
    total: Int,
    action: String,
    enabled: Boolean,
    onClick: () -> Unit,
    onHeightChanged: (Int) -> Unit,
) {
    Surface(
        modifier = Modifier.onSizeChanged { onHeightChanged(it.height) },
        tonalElevation = 3.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AnimatedMoney(total)
            }
            Button(onClick, Modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = enabled, shape = RoundedCornerShape(18.dp)) { Text(action) }
        }
    }
}

@Composable
fun BillingTotals(totals: BillTotals, discountPercent: Int? = null, receiptStyle: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ReceiptMoneyRow("Subtotal", totals.subtotal, receiptStyle = receiptStyle)
        ReceiptMoneyRow(if (discountPercent != null) "Discount ($discountPercent%)" else "Discount", totals.discount,
            negative = true, receiptStyle = receiptStyle)
        ReceiptMoneyRow("Demo GST (5%)", totals.tax, receiptStyle = receiptStyle)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Total", style = MaterialTheme.typography.titleMedium, fontFamily = if (receiptStyle) FontFamily.Monospace else null)
            AnimatedMoney(totals.total)
        }
    }
}

@Composable
private fun ReceiptMoneyRow(label: String, amount: Int, negative: Boolean = false, receiptStyle: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontFamily = if (receiptStyle) FontFamily.Monospace else null)
        Text((if (negative && amount > 0) "−" else "") + formatRupees(amount), style = MaterialTheme.typography.bodyMedium,
            fontFamily = if (receiptStyle) FontFamily.Monospace else null)
    }
}

@Composable
fun BillPreviewScreen(
    state: BillingState,
    onIntent: (BillingIntent) -> Unit,
    onBrowse: () -> Unit,
    onActionBarHeightChanged: (Int) -> Unit = {},
) {
    val receipt = state.previewReceipt
    val lines = receipt?.lines ?: state.bill.lines
    val totals = receipt?.totals ?: state.bill.totals
    if (lines.isEmpty()) {
        RunningBillScreen(state, onIntent, {}, onBrowse, onActionBarHeightChanged)
        return
    }
    Scaffold(modifier = Modifier.testTag("billing_preview"), bottomBar = {
        Surface(
            modifier = Modifier.onSizeChanged { onActionBarHeightChanged(it.height) },
            tonalElevation = 3.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onIntent(BillingIntent.Output(BillingOutput.PRINT)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = !state.busy
                ) {
                    Icon(Icons.Outlined.Print, null)
                    Spacer(Modifier.width(10.dp))
                    Text(if (state.busy) "Preparing receipt…" else "Print demo bill")
                }
                OutlinedButton(
                    onClick = { onIntent(BillingIntent.Output(BillingOutput.SHARE)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = !state.busy,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Outlined.Share, null)
                    Spacer(Modifier.width(10.dp))
                    Text("Share demo receipt")
                }
            }
        }
    }, contentWindowInsets = WindowInsets(0, 0, 0, 0)) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Receipt preview", style = MaterialTheme.typography.headlineMedium)
                    LuxuryBadge("Thermal Print Ready")
                }
                Text("Demo output · No printer or payment service connected", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
                PaperFeedAnimation(state.busy)
            }
            if (receipt == null) item {
                Text("Payment label", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("UPI (demo)", "Cash (demo)").forEach { payment ->
                        FilterChip(
                            selected = state.payment == payment,
                            onClick = { onIntent(BillingIntent.SelectPayment(payment)) },
                            label = { Text(payment) },
                            enabled = !state.busy,
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Restaurant, null, Modifier.size(30.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("TechQuantum Kitchen", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Monospace)
                            Text("Ahmedabad · Counter showcase", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Spacer(Modifier.height(8.dp))
                            Text(receipt?.let { "Bill #${it.id.removePrefix("BILL-")}" } ?: "Draft bill", fontFamily = FontFamily.Monospace)
                            Text(receipt?.table ?: state.bill.table, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        lines.forEach { line ->
                            Column {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(line.item.name, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                                    Text(formatRupees(line.total), style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                                }
                                Text("${line.quantity} × ${formatRupees(line.item.price)}", style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (line.notes.isNotBlank()) Text(line.notes, style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        BillingTotals(totals, if (receipt == null) state.bill.discountPercent else null, receiptStyle = true)
                        Text("Payment: ${receipt?.payment ?: state.payment}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            DemoQrPlaceholder(Modifier.size(84.dp))
                            Text("UPI QR placeholder", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                            Text("Not for payments", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                        Text("Thank you. Come hungry, leave happy.", style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontFamily = FontFamily.Monospace)
                    }
                }
            }
            if (state.error != null) item {
                Text(state.error, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
            item { Text("Print and share are animated demonstrations. Completing a bill keeps this receipt available and starts a fresh running order.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun BillingItemEditor(state: BillingState, onIntent: (BillingIntent) -> Unit) {
    val item = state.editingItem ?: return
    ModalBottomSheet(onDismissRequest = { onIntent(BillingIntent.DismissEditor) }) {
        Column(Modifier.fillMaxWidth().heightIn(max = 540.dp).verticalScroll(rememberScrollState()).imePadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(item.name, style = MaterialTheme.typography.headlineSmall)
            FoodIndicators(item)
            Text("Quantity", style = MaterialTheme.typography.titleMedium)
            QuantityStepper(state.editQuantity, { onIntent(BillingIntent.EditQuantity(-1)) },
                { onIntent(BillingIntent.EditQuantity(1)) }, touchTargetSize = 48.dp)
            OutlinedTextField(
                value = state.editNotes,
                onValueChange = { onIntent(BillingIntent.EditNotes(it)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                label = { Text("Preparation notes") },
                placeholder = { Text("Less spice, no onion…") },
                maxLines = 3,
                supportingText = { Text("${state.editNotes.length}/120 characters") }
            )
            Button(
                onClick = { onIntent(BillingIntent.SaveEditor) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(if (state.editingExisting) "Update item" else "Add ${state.editQuantity} to bill")
            }
        }
    }
}

@Composable
fun BillingOutputSheet(state: BillingState, onDismiss: () -> Unit, onNewBill: () -> Unit) {
    if (state.output.status != BillingOutputStatus.SUCCEEDED) return
    val receipt = state.receipt ?: return
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().testTag(if (state.output.output == BillingOutput.PRINT) "billing_print_success" else "billing_share_success")
            .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Outlined.CheckCircle, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            AnimatedContent(state.output.output, label = "Receipt output result") { output ->
                Text("Bill #${receipt.id.removePrefix("BILL-")} ${if (output == BillingOutput.PRINT) "printed" else "ready to share"}",
                    style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
            Text(if (state.output.output == BillingOutput.PRINT) "Demo printer finished. Your receipt is available for this session."
                else "Demo share completed. Your receipt is available for this session.", textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onDismiss, Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(18.dp)) { Text("Done") }
            TextButton(onNewBill, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Start another bill") }
        }
    }
}
