@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.techquantum.feature.pos.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.techquantum.core.billingengine.BillLine
import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.core.billingengine.BillTotals
import com.techquantum.core.billingengine.MenuItem
import com.techquantum.core.designsystem.ActionFooter
import com.techquantum.core.designsystem.AnimatedMoney
import com.techquantum.core.designsystem.DietaryIndicator
import com.techquantum.core.designsystem.LocalTQDMotionPolicy
import com.techquantum.core.designsystem.LuxuryBadge
import com.techquantum.core.designsystem.QuantityStepper
import com.techquantum.core.designsystem.SkeletonShimmer
import com.techquantum.core.designsystem.formatRupees
import com.techquantum.core.designsystem.theme.TQDDimensions
import com.techquantum.feature.pos.presentation.PosIntent
import com.techquantum.feature.pos.presentation.PosState

@Composable
fun PosWorkspaceScreen(state: PosState, onIntent: (PosIntent) -> Unit, onCharge: () -> Unit) {
    var showBill by rememberSaveable { mutableStateOf(false) }
    val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val motion = MaterialTheme.motionScheme
    BoxWithConstraints(Modifier.fillMaxSize().testTag("pos_workspace")) {
        val wide = maxWidth >= 840.dp && maxHeight >= 600.dp && LocalDensity.current.fontScale <= 1.5f
        Column(Modifier.fillMaxSize().padding(if (wide) 20.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (wide) {
                CounterHeader(state, onIntent, true)
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    MenuPanel(state, onIntent, Modifier.weight(.62f).fillMaxHeight())
                    Surface(Modifier.weight(.38f).fillMaxHeight(), shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainerLow) { BillPanel(state, onIntent, onCharge) }
                }
            } else {
                Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(!showBill, { showBill = false; keyboard?.hide() }, label = { Text("Menu") },
                        modifier = Modifier.weight(1f).heightIn(min = TQDDimensions.operatorTouchTargetMin))
                    FilterChip(showBill, { showBill = true; keyboard?.hide() }, label = { Text("Running bill · ${state.bill.count}") },
                        modifier = Modifier.weight(1f).heightIn(min = TQDDimensions.operatorTouchTargetMin))
                }
                AnimatedContent(showBill, modifier = Modifier.weight(1f), label = "Counter pane",
                    transitionSpec = { fadeIn(motion.fastEffectsSpec()).togetherWith(fadeOut(motion.fastEffectsSpec())) }) { billVisible ->
                    if (billVisible) BillPanel(state, onIntent, onCharge, Modifier.fillMaxSize()) {
                        CounterHeader(state, onIntent, false)
                    } else Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MenuPanel(state, onIntent, Modifier.weight(1f)) { CounterHeader(state, onIntent, false) }
                        Button({ showBill = true; keyboard?.hide() }, Modifier.fillMaxWidth().heightIn(min = TQDDimensions.operatorTouchTargetMin)) {
                            Text("Review ${state.bill.count} items · ${formatRupees(state.bill.totals.total)}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CounterHeader(state: PosState, onIntent: (PosIntent) -> Unit, wide: Boolean) {
    var tablesOpen by remember { mutableStateOf(false) }
    val identity: @Composable (Modifier) -> Unit = { modifier ->
        Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.PointOfSale, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                }
            }
            Column(Modifier.weight(1f)) {
                FlowRow(verticalArrangement = Arrangement.spacedBy(6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("TechQuantum Kitchen", style = MaterialTheme.typography.titleLarge)
                    LuxuryBadge("Live POS")
                }
                Text("Operator ${state.operator} · Demo counter", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    val tableSelector: @Composable () -> Unit = {
        Box {
            OutlinedButton(
                onClick = { tablesOpen = true },
                modifier = Modifier.heightIn(min = TQDDimensions.operatorTouchTargetMin),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Icon(Icons.Outlined.TableRestaurant, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(state.bill.table)
                Icon(Icons.Outlined.ExpandMore, null, Modifier.size(18.dp))
            }
            DropdownMenu(tablesOpen, { tablesOpen = false }) {
                listOf("Counter 01", "Table 01", "Table 02", "Table 03", "Takeaway").forEach { table ->
                    DropdownMenuItem(text = { Text(table) }, onClick = { onIntent(PosIntent.SelectTable(table)); tablesOpen = false })
                }
            }
        }
    }
    if (wide) Row(Modifier.fillMaxWidth().testTag("pos_header"), horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically) { identity(Modifier.weight(1f)); tableSelector() }
    else Column(Modifier.fillMaxWidth().testTag("pos_header"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        identity(Modifier.fillMaxWidth()); tableSelector()
    }
}

@Composable
private fun MenuPanel(state: PosState, onIntent: (PosIntent) -> Unit, modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {}) {
    val cardWidth = if (LocalDensity.current.fontScale > 1.25f) 240.dp else 156.dp
    LazyVerticalGrid(GridCells.Adaptive(cardWidth), modifier.fillMaxSize().testTag("pos_item_grid"),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 12.dp)) {
        item(key = "counter-header", span = { GridItemSpan(maxLineSpan) }, contentType = "header") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                header()
                OutlinedTextField(state.query, { onIntent(PosIntent.Search(it)) },
                    Modifier.fillMaxWidth().testTag("pos_search"), label = { Text("Search the menu") }, singleLine = true,
                    trailingIcon = if (state.query.isNotEmpty()) ({ IconButton({ onIntent(PosIntent.Search("")) }, Modifier.size(TQDDimensions.operatorTouchTargetMin)) {
                        Icon(Icons.Outlined.Close, "Clear menu search")
                    } }) else null)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.categories, key = { it }, contentType = { "category" }) { category ->
                        FilterChip(state.category == category, { onIntent(PosIntent.SelectCategory(category)) },
                            label = { Text(category) }, modifier = Modifier.heightIn(min = TQDDimensions.operatorTouchTargetMin))
                    }
                }
            }
        }
        when {
            state.loading -> items(6, key = { "loading-$it" }, contentType = { "skeleton" }) {
                SkeletonShimmer(Modifier.fillMaxWidth().height(176.dp), "Loading menu")
            }
            state.filteredCatalog.isEmpty() -> item(key = "empty", span = { GridItemSpan(maxLineSpan) }, contentType = "empty") {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("No matching dishes", style = MaterialTheme.typography.titleLarge)
                    TextButton({ onIntent(PosIntent.Search("")); onIntent(PosIntent.SelectCategory("All")) },
                        Modifier.heightIn(min = TQDDimensions.operatorTouchTargetMin)) { Text("Clear filters") }
                }
            }
            else -> items(state.filteredCatalog, key = { it.id }, contentType = { "dish" }) { item ->
                Box(Modifier.animateItem(placementSpec = MaterialTheme.motionScheme.fastSpatialSpec())) {
                    PosItemCard(item, state.lastAddedId == item.id, state.addPulse) { onIntent(PosIntent.AddItem(item.id)) }
                }
            }
        }
    }
}

@Composable
private fun PosItemCard(item: MenuItem, justAdded: Boolean, pulse: Int, onAdd: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val scale = remember { Animatable(1f) }
    val motion = MaterialTheme.motionScheme
    val reduced = LocalTQDMotionPolicy.current.reduced
    LaunchedEffect(pulse) {
        if (justAdded && !reduced) {
            scale.animateTo(.97f, motion.fastSpatialSpec())
            scale.animateTo(1f, motion.fastSpatialSpec())
        } else scale.snapTo(1f)
    }
    Surface(
        onClick = onAdd,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 176.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .semantics { contentDescription = "Add ${item.name}" },
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, if (justAdded) colors.primary else colors.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                PosDishArtwork(item, Modifier.size(50.dp))
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = colors.primaryContainer,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Add, null, Modifier.size(18.dp), tint = colors.onPrimaryContainer)
                    }
                }
            }
            Text(item.name, style = MaterialTheme.typography.titleMedium, maxLines = Int.MAX_VALUE)
            DietaryIndicator(item.isVeg)
            Text(formatRupees(item.price), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun PosDishArtwork(item: MenuItem, modifier: Modifier = Modifier) {
    val plate = MaterialTheme.colorScheme.surface
    val rim = MaterialTheme.colorScheme.outlineVariant
    val food = MaterialTheme.colorScheme.tertiary
    val garnish = MaterialTheme.colorScheme.secondary
    Canvas(modifier) {
        val unit = size.minDimension
        drawCircle(plate, radius = unit * .49f)
        drawCircle(rim, radius = unit * .43f, style = Stroke(unit * .025f))
        when (item.category) {
            "Drinks" -> {
                drawRoundRect(food, topLeft = Offset(unit * .31f, unit * .22f),
                    size = Size(unit * .38f, unit * .58f), cornerRadius = CornerRadius(unit * .08f))
                drawLine(garnish, Offset(unit * .57f, unit * .12f), Offset(unit * .48f, unit * .48f), unit * .05f)
            }
            "Breakfast", "Breads" -> {
                drawOval(food, topLeft = Offset(unit * .19f, unit * .33f), size = Size(unit * .62f, unit * .34f))
                drawCircle(garnish, radius = unit * .08f, center = Offset(unit * .25f, unit * .72f))
                drawCircle(food.copy(alpha = .6f), radius = unit * .08f, center = Offset(unit * .47f, unit * .74f))
            }
            "Desserts" -> repeat(3) { index ->
                drawCircle(food, radius = unit * .14f, center = Offset(unit * (.3f + index * .2f), unit * (if (index == 1) .35f else .61f)))
            }
            else -> {
                drawCircle(food, radius = unit * .31f)
                repeat(5) { index ->
                    val angle = index * Math.PI * 2 / 5
                    drawCircle(garnish, radius = unit * .055f,
                        center = center + Offset((kotlin.math.cos(angle) * unit * .19f).toFloat(), (kotlin.math.sin(angle) * unit * .19f).toFloat()))
                }
            }
        }
    }
}

@Composable
private fun BillPanel(state: PosState, onIntent: (PosIntent) -> Unit, onCharge: () -> Unit,
    modifier: Modifier = Modifier, header: @Composable () -> Unit = {}) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LazyColumn(Modifier.weight(1f).testTag("pos_bill_lines"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item(key = "heading", contentType = "header") {
                header()
                Text("Running bill", style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                Text("${state.bill.count} items", style = MaterialTheme.typography.bodyMedium)
            }
            if (state.bill.lines.isEmpty()) item(key = "empty", contentType = "empty") {
                Text("Tap a dish to start this bill. POS and Billing share this counter.",
                    Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else items(state.bill.lines, key = { it.key }, contentType = { "line" }) { line ->
                PosBillRow(line, onIntent, Modifier.animateItem(placementSpec = MaterialTheme.motionScheme.fastSpatialSpec()))
            }
            item(key = "discount", contentType = "controls") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(0, 10, 15), key = { it }, contentType = { "discount" }) { percent ->
                        FilterChip(state.bill.discountPercent == percent, { onIntent(PosIntent.ApplyDiscount(percent)) },
                            label = { Text(if (percent == 0) "No discount" else "$percent% off") },
                            modifier = Modifier.heightIn(min = TQDDimensions.operatorTouchTargetMin))
                    }
                }
            }
            item(key = "totals", contentType = "totals") { PosTotals(state.bill.totals) }
        }
        Button(onCharge, Modifier.fillMaxWidth().heightIn(min = 60.dp),
            enabled = state.bill.lines.isNotEmpty() && !state.charging) {
            Text("Charge ${formatRupees(state.bill.totals.total)}")
        }
    }
}

@Composable
private fun PosBillRow(line: BillLine, onIntent: (PosIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(line.item.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            IconButton({ onIntent(PosIntent.RemoveItem(line.key)) }, Modifier.size(TQDDimensions.operatorTouchTargetMin)) {
                Icon(Icons.Outlined.DeleteOutline, "Remove ${line.item.name}")
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            QuantityStepper(line.quantity, { onIntent(PosIntent.ChangeQuantity(line.key, -1)) },
                { onIntent(PosIntent.ChangeQuantity(line.key, 1)) }, touchTargetSize = TQDDimensions.operatorTouchTargetMin, itemName = line.item.name)
            AnimatedMoney(line.total)
        }
        if (line.notes.isNotEmpty()) Text(line.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider()
    }
}

@Composable
private fun PosTotals(totals: BillTotals, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PosAmountLine("Subtotal", totals.subtotal)
            PosAmountLine("Discount", -totals.discount)
            PosAmountLine("Demo GST · 5%", totals.tax)
            HorizontalDivider(Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total", style = MaterialTheme.typography.titleMedium)
                AnimatedMoney(totals.total)
            }
        }
    }
}

@Composable
private fun PosAmountLine(label: String, amount: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(formatRupees(amount), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun PosChargeScreen(state: PosState, onIntent: (PosIntent) -> Unit, onBack: () -> Unit) {
    val totals = if (state.charging) state.receipt?.totals ?: state.bill.totals else state.bill.totals
    if (state.bill.lines.isEmpty() && state.receipt == null) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("This bill is empty", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Button(onBack, Modifier.heightIn(min = TQDDimensions.operatorTouchTargetMin), shape = RoundedCornerShape(18.dp)) { Text("Back to counter") }
        }
        return
    }
    Scaffold(bottomBar = {
        ActionFooter {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Total · ${formatRupees(totals.total)}", style = MaterialTheme.typography.titleLarge)
                Button(
                    onClick = { onIntent(PosIntent.Charge) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = !state.charging
                ) {
                    if (state.charging) {
                        Text("Recording demo payment…")
                    } else {
                        Text("Confirm ${state.payment} · ${formatRupees(totals.total)}")
                    }
                }
            }
        }
    }, contentWindowInsets = WindowInsets(0, 0, 0, 0)) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).verticalScroll(rememberScrollState()).testTag("pos_charge"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(Modifier.widthIn(max = TQDDimensions.formMaxWidth).fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LuxuryBadge("Point of Sale Terminal")
                Text("Ready at the counter.", style = MaterialTheme.typography.headlineLarge)
                Text("Select a payment method. This records a local receipt; no money is collected.",
                    style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(Modifier.widthIn(max = TQDDimensions.formMaxWidth).fillMaxWidth().padding(horizontal = 24.dp).selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("UPI", "Card", "Cash").forEach { payment ->
                    val selected = state.payment == payment
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 60.dp).selectable(
                                selected = selected,
                                enabled = !state.charging,
                                role = Role.RadioButton,
                                onClick = { onIntent(PosIntent.SelectPayment(payment)) }
                            ).padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            RadioButton(selected, null, enabled = !state.charging)
                            Text(payment, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            val icon = when (payment) {
                                "UPI" -> Icons.Outlined.QrCodeScanner
                                "Card" -> Icons.Outlined.CreditCard
                                else -> Icons.Outlined.Payments
                            }
                            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.widthIn(max = TQDDimensions.formMaxWidth).fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${state.bill.table} · ${state.operator}", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    (if (state.charging) state.receipt?.lines.orEmpty() else state.bill.lines).forEach { line ->
                        PosAmountLine("${line.item.name} × ${line.quantity}", line.total)
                    }
                }
            }
            PosTotals(totals, Modifier.widthIn(max = TQDDimensions.formMaxWidth).fillMaxWidth())
            Text("Demo GST is 5% after discounts. Each amount is rounded to the nearest rupee.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive }) }
        }
    }
}

@Composable
fun PosReceiptScreen(receipt: BillReceipt?, onNewBill: () -> Unit) {
    if (receipt == null) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("Start a new counter bill", style = MaterialTheme.typography.headlineMedium)
            Button(onNewBill, Modifier.heightIn(min = TQDDimensions.operatorTouchTargetMin), shape = RoundedCornerShape(18.dp)) { Text("Back to counter") }
        }
        return
    }
    val progress = remember(receipt.id) { Animatable(0f) }
    val motion = MaterialTheme.motionScheme
    LaunchedEffect(receipt.id) { progress.animateTo(1f, motion.slowSpatialSpec()) }
    val circle = MaterialTheme.colorScheme.primaryContainer
    val check = MaterialTheme.colorScheme.onPrimaryContainer
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp).testTag("pos_receipt"),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Canvas(Modifier.size(96.dp).semantics { contentDescription = "Payment recorded successfully" }
            .graphicsLayer { scaleX = .8f + .2f * progress.value; scaleY = scaleX }) {
            drawCircle(circle)
            val p = progress.value.coerceIn(0f, 1f)
            val path = Path().apply {
                moveTo(size.width * .26f, size.height * .51f)
                if (p <= .4f) lineTo(size.width * (.26f + .17f * p / .4f), size.height * (.51f + .16f * p / .4f))
                else { lineTo(size.width * .43f, size.height * .67f)
                    lineTo(size.width * (.43f + .31f * (p - .4f) / .6f), size.height * (.67f - .34f * (p - .4f) / .6f)) }
            }
            drawPath(path, check, style = Stroke(6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }
        Text("Payment recorded", style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        Surface(Modifier.widthIn(max = TQDDimensions.receiptMaxWidth).fillMaxWidth(), shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("TECHQUANTUM KITCHEN", style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace)
                    LuxuryBadge("Paid")
                }
                Text(receipt.id, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${receipt.table} · ${receipt.payment} · ${receipt.lines.sumOf { it.quantity }} items",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                receipt.lines.forEach { line -> PosAmountLine("${line.item.name} × ${line.quantity}", line.total) }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                PosTotals(receipt.totals)
            }
        }
        Text("Local showcase receipt. No real payment was taken.", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onNewBill, Modifier.widthIn(max = TQDDimensions.receiptMaxWidth).fillMaxWidth().heightIn(min = 60.dp), shape = RoundedCornerShape(18.dp)) { Text("Start new bill") }
    }
}
