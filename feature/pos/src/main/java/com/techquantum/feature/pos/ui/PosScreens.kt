@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.techquantum.feature.pos.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.techquantum.core.billingengine.BillLine
import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.core.billingengine.BillTotals
import com.techquantum.core.billingengine.MenuItem
import com.techquantum.core.designsystem.*
import com.techquantum.feature.pos.presentation.PosIntent
import com.techquantum.feature.pos.presentation.PosState

@Composable
fun PosWorkspaceScreen(state: PosState, onIntent: (PosIntent) -> Unit, onCharge: () -> Unit) {
    var showBill by rememberSaveable { mutableStateOf(false) }
    val entryPulse = remember { Animatable(1f) }
    val motion = MaterialTheme.motionScheme
    LaunchedEffect(state.addPulse) {
        if (state.addPulse > 0) {
            entryPulse.snapTo(.96f)
            entryPulse.animateTo(1f, motion.fastSpatialSpec())
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().testTag("pos_workspace")) {
        val wide = maxWidth >= 840.dp
        Column(Modifier.fillMaxSize().padding(if (wide) 20.dp else 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CounterHeader(state, onIntent, wide)
            if (wide) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    MenuPanel(state, onIntent, Modifier.weight(.62f).fillMaxHeight())
                    Surface(
                        Modifier.weight(.38f).fillMaxHeight().graphicsLayer { scaleX = entryPulse.value; scaleY = entryPulse.value },
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    ) { BillPanel(state, onIntent, onCharge) }
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = !showBill,
                        onClick = { showBill = false },
                        label = { Text("Menu") },
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                    )
                    FilterChip(
                        selected = showBill,
                        onClick = { showBill = true },
                        label = { Text("Running bill · ${state.bill.count}") },
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                    )
                }
                if (showBill) {
                    BillPanel(state, onIntent, onCharge, Modifier.weight(1f))
                } else {
                    MenuPanel(state, onIntent, Modifier.weight(1f))
                    Button(
                        onClick = { showBill = true },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("Review ${state.bill.count} items · ${formatRupees(state.bill.totals.total)}")
                    }
                }
            }
        }
    }
}

@Composable
private fun CounterHeader(state: PosState, onIntent: (PosIntent) -> Unit, wide: Boolean) {
    var tablesOpen by remember { mutableStateOf(false) }
    val header: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.PointOfSale, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                }
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("TechQuantum Kitchen", style = MaterialTheme.typography.titleLarge)
                    LuxuryBadge("Live POS")
                }
                Text("Operator ${state.operator} · Demo counter", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Box {
            OutlinedButton(
                onClick = { tablesOpen = true },
                modifier = Modifier.heightIn(min = 52.dp),
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
    if (wide) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) { header() }
    else Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) { header() }
}

@Composable
private fun MenuPanel(state: PosState, onIntent: (PosIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            state.query, { onIntent(PosIntent.Search(it)) }, Modifier.fillMaxWidth().testTag("pos_search"),
            placeholder = { Text("Search the menu") }, singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = if (state.query.isNotEmpty()) ({ IconButton({ onIntent(PosIntent.Search("")) }) {
                Icon(Icons.Outlined.Close, "Clear menu search")
            } }) else null,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.categories) { category ->
                FilterChip(
                    selected = state.category == category,
                    onClick = { onIntent(PosIntent.SelectCategory(category)) },
                    label = { Text(category) },
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.heightIn(min = 48.dp)
                )
            }
        }
        if (state.loading) {
            LazyVerticalGrid(GridCells.Adaptive(156.dp), Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(6) { SkeletonShimmer(Modifier.fillMaxWidth().height(170.dp)) }
            }
        } else if (state.filteredCatalog.isEmpty()) {
            Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Text("No matching dishes", style = MaterialTheme.typography.titleLarge)
                TextButton({ onIntent(PosIntent.Search("")); onIntent(PosIntent.SelectCategory("All")) }) { Text("Clear filters") }
            }
        } else {
            LazyVerticalGrid(GridCells.Adaptive(156.dp), Modifier.weight(1f).testTag("pos_item_grid"),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp)) {
                items(state.filteredCatalog, key = { it.id }) { item ->
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
    LaunchedEffect(pulse) {
        if (justAdded) { scale.snapTo(.94f); scale.animateTo(1f, motion.fastSpatialSpec()) }
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
            Text(item.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    Modifier.size(12.dp),
                    shape = RoundedCornerShape(3.dp),
                    border = BorderStroke(1.dp, if (item.isVeg) colors.tertiary else colors.primary),
                    color = colors.surface
                ) {
                    Box(Modifier.padding(2.5.dp).background(if (item.isVeg) colors.tertiary else colors.primary, androidx.compose.foundation.shape.CircleShape))
                }
                Text(if (item.isVeg) "Vegetarian" else "Non-vegetarian", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
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
private fun BillPanel(state: PosState, onIntent: (PosIntent) -> Unit, onCharge: () -> Unit, modifier: Modifier = Modifier) {
    val discountPulse = remember { Animatable(1f) }
    val motion = MaterialTheme.motionScheme
    LaunchedEffect(state.bill.discountPercent) {
        discountPulse.snapTo(1.06f)
        discountPulse.animateTo(1f, motion.fastSpatialSpec())
    }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Running bill", style = MaterialTheme.typography.titleLarge)
            LuxuryBadge("${state.bill.count} items")
        }
        if (state.bill.lines.isEmpty()) {
            Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Icon(Icons.Outlined.ReceiptLong, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Text("Tap a dish to start this bill", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("POS and Billing share this counter.", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(Modifier.weight(1f).testTag("pos_bill_lines"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.bill.lines, key = { it.key }) { line -> PosBillRow(line, onIntent, Modifier.animateItem()) }
            }
        }
        LazyRow(Modifier.graphicsLayer { scaleX = discountPulse.value; scaleY = discountPulse.value },
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf(0, 10, 15)) { percent ->
                FilterChip(
                    selected = state.bill.discountPercent == percent,
                    onClick = { onIntent(PosIntent.ApplyDiscount(percent)) },
                    label = { Text(if (percent == 0) "No discount" else "$percent% off") },
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.heightIn(min = 48.dp)
                )
            }
        }
        PosTotals(state.bill.totals)
        Button(
            onClick = onCharge,
            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
            shape = RoundedCornerShape(18.dp),
            enabled = state.bill.lines.isNotEmpty() && !state.charging
        ) {
            Text("Charge ${formatRupees(state.bill.totals.total)}")
        }
    }
}

@Composable
private fun PosBillRow(line: BillLine, onIntent: (PosIntent) -> Unit, modifier: Modifier = Modifier) {
    val motion = MaterialTheme.motionScheme
    val arrival = remember(line.key) { Animatable(0f) }
    LaunchedEffect(line.key) { arrival.animateTo(1f, motion.fastSpatialSpec()) }
    AnimatedContent(line.quantity, modifier.fillMaxWidth().graphicsLayer {
        translationX = (1f - arrival.value) * 36.dp.toPx()
        alpha = arrival.value.coerceIn(0f, 1f)
    }, transitionSpec = {
        (slideInHorizontally(motion.fastSpatialSpec()) { it / 8 } + fadeIn(motion.fastEffectsSpec())) togetherWith
            (slideOutHorizontally(motion.fastSpatialSpec()) { -it / 8 } + fadeOut(motion.fastEffectsSpec()))
    }, label = "Counter line") { quantity ->
        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(line.item.name, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                IconButton({ onIntent(PosIntent.RemoveItem(line.key)) }, Modifier.size(48.dp)) {
                    Icon(Icons.Outlined.DeleteOutline, "Remove ${line.item.name}")
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                QuantityStepper(quantity, { onIntent(PosIntent.ChangeQuantity(line.key, -1)) },
                    { onIntent(PosIntent.ChangeQuantity(line.key, 1)) }, touchTargetSize = 48.dp)
                AnimatedMoney(line.item.price * quantity)
            }
            if (line.notes.isNotEmpty()) Text(line.notes, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun PosTotals(totals: BillTotals) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
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
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(formatRupees(amount), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun PosChargeScreen(state: PosState, onIntent: (PosIntent) -> Unit, onBack: () -> Unit) {
    val totals = if (state.charging) state.receipt?.totals ?: state.bill.totals else state.bill.totals
    if (state.bill.lines.isEmpty() && state.receipt == null) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("This bill is empty", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Button(onBack, Modifier.heightIn(min = 52.dp), shape = RoundedCornerShape(18.dp)) { Text("Back to counter") }
        }
        return
    }
    Scaffold(bottomBar = {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PosTotals(totals)
                Button(
                    onClick = { onIntent(PosIntent.Charge) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = !state.charging
                ) {
                    Text(if (state.charging) "Recording demo payment…" else "Confirm ${state.payment} · ${formatRupees(totals.total)}")
                }
            }
        }
    }, contentWindowInsets = WindowInsets(0, 0, 0, 0)) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp).testTag("pos_charge"),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LuxuryBadge("Point of Sale Terminal")
                Text("Ready at the counter.", style = MaterialTheme.typography.headlineLarge)
                Text("Select a payment method. This records a local receipt; no money is collected.",
                    style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${state.bill.table} · ${state.operator}", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    (if (state.charging) state.receipt?.lines.orEmpty() else state.bill.lines).forEach { line ->
                        PosAmountLine("${line.item.name} × ${line.quantity}", line.total)
                    }
                }
            }
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
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("Start a new counter bill", style = MaterialTheme.typography.headlineMedium)
            Button(onNewBill, Modifier.heightIn(min = 52.dp), shape = RoundedCornerShape(18.dp)) { Text("Back to counter") }
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
        Surface(Modifier.widthIn(max = 560.dp).fillMaxWidth(), shape = RoundedCornerShape(24.dp),
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
        Button(onNewBill, Modifier.widthIn(max = 560.dp).fillMaxWidth().heightIn(min = 60.dp), shape = RoundedCornerShape(18.dp)) { Text("Start new bill") }
    }
}
