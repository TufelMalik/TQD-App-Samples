@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.techquantum.feature.fooddelivery.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.techquantum.core.designsystem.ActionFooter
import com.techquantum.core.designsystem.AnimatedMoney
import com.techquantum.core.designsystem.DietaryIndicator
import com.techquantum.core.designsystem.LocalTQDMotionPolicy
import com.techquantum.core.designsystem.LuxuryBadge
import com.techquantum.core.designsystem.QuantityStepper
import com.techquantum.core.designsystem.RatingPill
import com.techquantum.core.designsystem.ShowcaseSheet
import com.techquantum.core.designsystem.formatRupees
import com.techquantum.feature.fooddelivery.domain.AddOn
import com.techquantum.feature.fooddelivery.domain.Dish
import com.techquantum.feature.fooddelivery.domain.FoodAddress
import com.techquantum.feature.fooddelivery.domain.FoodBill
import com.techquantum.feature.fooddelivery.domain.FoodLine
import com.techquantum.feature.fooddelivery.domain.FoodPayment
import com.techquantum.feature.fooddelivery.domain.Restaurant
import com.techquantum.feature.fooddelivery.domain.TrackingStage
import com.techquantum.feature.fooddelivery.presentation.FoodIntent
import com.techquantum.feature.fooddelivery.presentation.FoodState

@Composable
fun FoodHomeScreen(state: FoodState, onIntent: (FoodIntent) -> Unit, onRestaurant: (String) -> Unit,
    heroModifier: @Composable (String) -> Modifier, modifier: Modifier = Modifier) {
    FreshFoodHome(state, onIntent, onRestaurant, heroModifier, modifier)
}

@Composable private fun RestaurantCard(restaurant: Restaurant, onClick: () -> Unit, heroModifier: Modifier) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Box {
            RestaurantArtwork(restaurant, heroModifier.fillMaxWidth().height(180.dp))
            FlowRow(
                Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LuxuryBadge(text = restaurant.cuisine.label)
                RatingPill(rating = restaurant.rating)
            }
        }
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(restaurant.name, style = MaterialTheme.typography.titleLarge)
            Text(restaurant.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Schedule, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${restaurant.etaMinutes}–${restaurant.etaMinutes + 5} min", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Place, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${restaurant.distanceKm} km", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(restaurant.cuisine.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun RestaurantMenuScreen(restaurant: Restaurant, state: FoodState, onIntent: (FoodIntent) -> Unit,
    onBack: () -> Unit, onCart: () -> Unit, onToggleDark: () -> Unit, heroModifier: Modifier, modifier: Modifier = Modifier,
    onShowThemes: () -> Unit = {}) {
    var customDish by remember { mutableStateOf<Dish?>(null) }
    FreshRestaurantMenu(restaurant, state, onIntent, onBack, onCart, heroModifier, modifier, onShowThemes, { customDish = it })
    customDish?.let { dish -> CustomizeDishSheet(dish, emptySet(), "", "Add to order", { customDish = null }) { addOns, note ->
        onIntent(FoodIntent.AddDish(dish.id, addOns, note)); customDish = null
    } }
}

@Composable private fun DishRow(dish: Dish, quantity: Int, onAdd: () -> Unit, onDecrease: () -> Unit,
    onCustomize: () -> Unit, modifier: Modifier = Modifier) {
    val stack = LocalDensity.current.fontScale > 1.25f || androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp < 360
    val copy: @Composable (Modifier) -> Unit = { contentModifier ->
        Column(contentModifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DietaryIndicator(dish.vegetarian)
            Text(dish.name, style = MaterialTheme.typography.titleMedium)
            Text(dish.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatRupees(dish.price), style = MaterialTheme.typography.titleMedium)
            if (dish.spice > 0) Text(if (dish.spice == 1) "Mild spice" else "Spicy", style = MaterialTheme.typography.labelMedium)
            if (dish.addOns.isNotEmpty()) TextButton(onCustomize, colors = accessibleTextButtonColors()) { Text("Customise") }
        }
    }
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        if (stack) Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            copy(Modifier.fillMaxWidth())
            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DishArtwork(dish, Modifier.size(80.dp).clip(MaterialTheme.shapes.medium))
                QuickFoodAdd(quantity, onAdd, onDecrease, dish.name)
            }
        } else Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            copy(Modifier.weight(1f))
            Column(Modifier.width(132.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DishArtwork(dish, Modifier.size(112.dp).clip(MaterialTheme.shapes.medium))
                QuickFoodAdd(quantity, onAdd, onDecrease, dish.name)
            }
        }
    }
}

@Composable internal fun QuickFoodAdd(quantity: Int, onAdd: () -> Unit, onDecrease: () -> Unit, itemName: String) {
    val motion = MaterialTheme.motionScheme
    AnimatedContent(quantity > 0, transitionSpec = {
        fadeIn(motion.fastEffectsSpec()).togetherWith(fadeOut(motion.fastEffectsSpec()))
            .using(SizeTransform { _, _ -> motion.fastSpatialSpec() })
    }, label = "Quick add") { added ->
        if (!added) FilledTonalButton(onAdd, Modifier.heightIn(min = 48.dp).semantics { contentDescription = "Add $itemName" }) {
            Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Text(" Add")
        } else QuantityStepper(quantity, onDecrease, onAdd, itemName = itemName, minimum = 0)
    }
}

@Composable private fun VegLabel(vegetarian: Boolean) { DietaryIndicator(vegetarian) }

@Composable fun FoodEmptyPanel(title: String, description: String, action: String, onAction: () -> Unit) {
    // Content only: a LazyColumn item must not create another unbounded vertical scroller.
    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(Icons.Default.Restaurant, null, Modifier.size(44.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = onAction) { Text(action) }
    }
}

@Composable fun FoodEmptyScreen(title: String, description: String, action: String, onAction: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        FoodEmptyPanel(title, description, action, onAction)
    }
}

@Composable private fun FoodSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ShowcaseSheet(onDismiss, content = content)
}

@Composable private fun CustomizeDishSheet(dish: Dish, initialIds: Set<String>, initialNote: String, action: String, onDismiss: () -> Unit, onSave: (Set<String>, String) -> Unit) {
    var selected by rememberSaveable(dish.id) { mutableStateOf(initialIds.toList()) }
    var note by rememberSaveable(dish.id) { mutableStateOf(initialNote) }
    FoodSheet(onDismiss) {
        Text(dish.name, style = MaterialTheme.typography.headlineSmall)
        Text("Make it your kind of delicious", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        dish.addOns.forEach { option ->
            Row(Modifier.fillMaxWidth().toggleable(option.id in selected, role = Role.Checkbox, onValueChange = {
                selected = if (it) selected + option.id else selected - option.id
            }), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(option.id in selected, null)
                Text(option.name, Modifier.weight(1f).padding(start = 8.dp))
                Text("+${formatRupees(option.price)}", style = MaterialTheme.typography.labelLarge)
            }
        }
        OutlinedTextField(note, { note = it.take(120) }, Modifier.fillMaxWidth(), label = { Text("Note for the kitchen") },
            placeholder = { Text("Less spice, no onions…") }, minLines = 2, maxLines = 3, colors = accessibleFieldColors())
        Text("Requests are subject to the kitchen's availability.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = { onSave(selected.toSet(), note) }, Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            Text("$action · ${formatRupees(dish.price + dish.addOns.filter { it.id in selected }.sumOf(AddOn::price))}")
        }
    }
}

@Composable private fun accessibleFieldColors() = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.onSurface,
    focusedLabelColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.onSurface)
@Composable private fun accessibleTextButtonColors() = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
@Composable private fun accessibleRadioColors() = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.onSurface, unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
fun FoodCartScreen(state: FoodState, onIntent: (FoodIntent) -> Unit, onCheckout: () -> Unit, onBrowse: () -> Unit, modifier: Modifier = Modifier) {
    var customKey by rememberSaveable { mutableStateOf<String?>(null) }
    Column(modifier.fillMaxSize().testTag("food_cart")) {
        if (state.cart.isEmpty()) {
            FoodEmptyScreen("Something delicious is missing", "Explore nearby kitchens and add your favourites.", "Explore kitchens", onBrowse)
        } else {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LuxuryBadge("Artisanal Basket · ${state.cartCount} items")
                        Text(state.cartRestaurant?.name ?: "Your kitchen", style = MaterialTheme.typography.headlineSmall)
                        Text("Freshly prepared for your doorstep", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                item {
                    val animatedProgress by animateFloatAsState(
                        targetValue = (state.bill.subtotal / 499f).coerceIn(0f, 1f),
                        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                        label = "free_delivery_progress"
                    )
                    if (state.bill.subtotal < 499) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        ) {
                            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.LocalShipping, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                    Text("Add ${formatRupees(499 - state.bill.subtotal)} more for free delivery!", style = MaterialTheme.typography.labelMedium)
                                }
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f))
                        ) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.tertiary)
                                Text("You unlocked Free Delivery on this order!", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
                items(state.cart, key = FoodLine::key) { line ->
                    Surface(
                        modifier = Modifier.animateItem(),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                                DishArtwork(line.dish, Modifier.size(68.dp).clip(RoundedCornerShape(18.dp)))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(line.dish.name, style = MaterialTheme.typography.titleMedium)
                                    if (line.addOns.isNotEmpty()) Text(line.addOns.joinToString { "${it.name} (+${formatRupees(it.price)})" },
                                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (line.note.isNotBlank()) Text("Kitchen note: ${line.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${formatRupees(line.unitPrice)} each", style = MaterialTheme.typography.labelMedium)
                                }
                                IconButton(onClick = { onIntent(FoodIntent.RemoveLine(line.key)) }) { Icon(Icons.Default.DeleteOutline, "Remove ${line.dish.name}") }
                            }
                            FlowRow(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                AnimatedMoney(line.total)
                                QuantityStepper(line.quantity, { onIntent(FoodIntent.ChangeQuantity(line.key, -1)) }, { onIntent(FoodIntent.ChangeQuantity(line.key, 1)) }, itemName = line.dish.name)
                            }
                            TextButton(onClick = { customKey = line.key }, colors = accessibleTextButtonColors(), contentPadding = PaddingValues(0.dp)) { Text("Edit customisation") }
                        }
                    }
                }
                item {
                    OutlinedTextField(state.instructions, { onIntent(FoodIntent.SetInstructions(it)) }, Modifier.fillMaxWidth(), label = { Text("Delivery instructions") },
                        placeholder = { Text("Leave at the door, ring the bell…") }, minLines = 2, maxLines = 3, shape = RoundedCornerShape(18.dp), colors = accessibleFieldColors())
                }
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Thank your rider", style = MaterialTheme.typography.titleMedium)
                            Text("Optional. Your full tip goes to the rider.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(listOf(0, 10, 20, 30)) { amount -> FilterChip(state.tip == amount, { onIntent(FoodIntent.SetTip(amount)) },
                                    label = { Text(if (amount == 0) "No tip" else formatRupees(amount)) }, shape = RoundedCornerShape(999.dp)) }
                            }
                        }
                    }
                }
                item { FoodBillCard(state.bill) }
            }
            FoodOrderBar(state, "Continue", onCheckout)
        }
    }
    state.cart.firstOrNull { it.key == customKey }?.let { line ->
        CustomizeDishSheet(line.dish, line.addOns.map(AddOn::id).toSet(), line.note, "Save changes", { customKey = null }) { addOns, note ->
            onIntent(FoodIntent.CustomizeLine(line.key, addOns, note)); customKey = null
        }
    }
}

@Composable fun FoodBillCard(bill: FoodBill) {
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Every charge, up front", style = MaterialTheme.typography.titleMedium)
                LuxuryBadge("Itemized Receipt")
            }
            BillRow("Items & add-ons", bill.subtotal)
            BillRow("GST (5%)", bill.tax)
            BillRow("Delivery", bill.delivery, bill.delivery == 0)
            BillRow("Platform fee", bill.platform)
            BillRow("Rider tip", bill.tip)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Total", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                AnimatedMoney(bill.total)
            }
        }
    }
}
@Composable private fun BillRow(label: String, amount: Int, free: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(if (free) "Free" else formatRupees(amount), style = MaterialTheme.typography.bodyMedium)
    }
}
@Composable internal fun FoodOrderBar(state: FoodState, action: String, onClick: () -> Unit, enabled: Boolean = true) {
    val largeType = LocalDensity.current.fontScale > 1.25f
    ActionFooter {
        if (largeType) Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Total", Modifier.weight(1f)); AnimatedMoney(state.bill.total) }
            Button(onClick, Modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = enabled, shape = RoundedCornerShape(18.dp)) { Text(action) }
        } else Row(Modifier.padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f)) { AnimatedMoney(state.bill.total); Text("Includes tax & fees", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Button(onClick, Modifier.heightIn(min = 52.dp), enabled = enabled, shape = RoundedCornerShape(18.dp)) { Text(action) }
        }
    }
}

@Composable
fun FoodCheckoutScreen(state: FoodState, onIntent: (FoodIntent) -> Unit, onBrowse: () -> Unit, modifier: Modifier = Modifier) {
    val keyboard = LocalSoftwareKeyboardController.current
    val addressFocus = remember { listOf("name", "phone", "line", "city", "pinCode").associateWith { FocusRequester() } }
    val listState = rememberLazyListState()
    var validationAttempt by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(validationAttempt, state.addressErrors) {
        if (validationAttempt > 0) addressFocus.keys.firstOrNull { it in state.addressErrors }?.let { field ->
            listState.scrollToItem(1)
            withFrameNanos { }
            addressFocus.getValue(field).requestFocus()
        }
    }
    Column(modifier.fillMaxSize().testTag("food_checkout")) {
        if (state.cart.isEmpty()) FoodEmptyScreen("Your order is empty", "Add a meal before choosing a delivery address.", "Explore kitchens", onBrowse)
        else {
            LazyColumn(Modifier.weight(1f), state = listState, contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LuxuryBadge("Fast Guest Checkout")
                        Text("Good food, no sign-up.", style = MaterialTheme.typography.headlineMedium)
                        Text("Enter delivery details to place your local preview order.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = { onIntent(FoodIntent.UpdateAddress(FoodAddress("Aditi Shah", "9876543210", "24, River View Apartments, Adajan", "Surat", "395009"))) },
                            enabled = !state.placingOrder, colors = accessibleTextButtonColors()) { Text("Use sample address") }
                    }
                }
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Delivery address", style = MaterialTheme.typography.titleLarge)
                            AddressField("Full name", state.address.name, state.addressErrors["name"], !state.placingOrder, focus = addressFocus.getValue("name")) { onIntent(FoodIntent.UpdateAddress(state.address.copy(name = it))) }
                            AddressField("Mobile number", state.address.phone, state.addressErrors["phone"], !state.placingOrder, KeyboardType.Phone, addressFocus.getValue("phone")) {
                                onIntent(FoodIntent.UpdateAddress(state.address.copy(phone = it.filter(Char::isDigit).take(10)))) }
                            AddressField("Building, street & landmark", state.address.line, state.addressErrors["line"], !state.placingOrder, focus = addressFocus.getValue("line")) { onIntent(FoodIntent.UpdateAddress(state.address.copy(line = it.take(180)))) }
                            AddressField("City", state.address.city, state.addressErrors["city"], !state.placingOrder, focus = addressFocus.getValue("city")) { onIntent(FoodIntent.UpdateAddress(state.address.copy(city = it))) }
                            AddressField("PIN code", state.address.pinCode, state.addressErrors["pinCode"], !state.placingOrder, KeyboardType.Number, addressFocus.getValue("pinCode"), last = true) {
                                onIntent(FoodIntent.UpdateAddress(state.address.copy(pinCode = it.filter(Char::isDigit).take(6)))) }
                        }
                    }
                }
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.selectableGroup().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Payment", style = MaterialTheme.typography.titleLarge)
                            FoodPayment.entries.forEach { payment ->
                                val selected = state.payment == payment
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 56.dp)
                                            .selectable(selected, enabled = !state.placingOrder, role = Role.RadioButton, onClick = { onIntent(FoodIntent.SelectPayment(payment)) })
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(selected, null, enabled = !state.placingOrder, colors = accessibleRadioColors())
                                        Text(payment.label, Modifier.padding(start = 10.dp), style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                            Text("All payment choices are previews. No money is collected.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("${state.cartCount} items from ${state.cartRestaurant?.name}", style = MaterialTheme.typography.titleMedium)
                            if (state.instructions.isNotBlank()) Text("Delivery note: ${state.instructions}", style = MaterialTheme.typography.bodyMedium)
                            FoodBillCard(state.bill)
                        }
                    }
                }
                state.error?.let { error -> item { Text(error, Modifier.semantics { liveRegion = LiveRegionMode.Polite }, color = MaterialTheme.colorScheme.error) } }
            }
            FoodOrderBar(state, if (state.placingOrder) "Placing order…" else "Place order", { validationAttempt++; keyboard?.hide(); onIntent(FoodIntent.PlaceOrder) }, !state.placingOrder)
        }
    }
}

@Composable private fun AddressField(label: String, value: String, error: String?, enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text, focus: FocusRequester? = null, last: Boolean = false, onChange: (String) -> Unit) {
    val manager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    OutlinedTextField(value, onChange, Modifier.fillMaxWidth().then(if (focus != null) Modifier.focusRequester(focus) else Modifier),
        enabled = enabled, label = { Text(label) }, singleLine = true, isError = error != null,
        supportingText = if (error != null) ({ Text(error) }) else null, colors = accessibleFieldColors(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = if (last) ImeAction.Done else ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { manager.moveFocus(FocusDirection.Next) }, onDone = { keyboard?.hide() }))
}

@Composable
fun FoodTrackingScreen(state: FoodState, onIntent: (FoodIntent) -> Unit, onHome: () -> Unit, modifier: Modifier = Modifier) {
    val order = state.order
    if (order == null) {
        Box(modifier.fillMaxSize().testTag("food_tracking")) { FoodEmptyScreen("A meal is waiting for you", "Order from a nearby kitchen to follow its journey.", "Explore kitchens", onHome) }
        return
    }
    val motion = MaterialTheme.motionScheme
    LazyColumn(modifier.fillMaxSize().testTag("food_tracking"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            LuxuryBadge("Preview tracking · Order ${order.id}")
            AnimatedContent(state.trackingStage, transitionSpec = { (fadeIn(motion.defaultEffectsSpec()) + slideInVertically(motion.defaultSpatialSpec()) { it / 3 })
                .togetherWith(fadeOut(motion.fastEffectsSpec())) }, label = "Delivery status") { stage ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (stage == TrackingStage.DELIVERED) "Your meal has arrived." else stage.detail, modifier = Modifier.semantics { heading(); liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.headlineLarge)
                    Text(if (stage == TrackingStage.DELIVERED) "Delivered to ${order.address.name}" else "Arriving in ${state.etaSeconds / 60} min ${state.etaSeconds % 60}s",
                        style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { DeliveryRouteMap(state.trackingStage, order.restaurant.name, Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(28.dp))) }
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    TrackingStage.entries.forEach { stage ->
                        val completed = state.trackingStage.ordinal >= stage.ordinal
                        val status = androidx.compose.ui.res.stringResource(when {
                            state.trackingStage == stage -> com.techquantum.core.designsystem.R.string.tracking_current
                            completed -> com.techquantum.core.designsystem.R.string.tracking_completed
                            else -> com.techquantum.core.designsystem.R.string.tracking_upcoming
                        })
                        Row(Modifier.semantics(mergeDescendants = true) {
                            stateDescription = status
                        }, verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Icon(if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null,
                                tint = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                            Column {
                                Text(stage.label, style = MaterialTheme.typography.titleMedium)
                                if (state.trackingStage == stage) Text(stage.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(order.restaurant.name, style = MaterialTheme.typography.titleLarge)
                    Text(order.lines.joinToString { "${it.quantity} × ${it.dish.name}" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${order.address.line}, ${order.address.city} ${order.address.pinCode}", style = MaterialTheme.typography.bodyMedium)
                    Text("${formatRupees(order.bill.total)} · ${order.payment.label}", style = MaterialTheme.typography.titleMedium)
                    if (state.trackingStage != TrackingStage.DELIVERED) OutlinedButton(onClick = { onIntent(FoodIntent.AdvanceTracking) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) { Text("Preview next update") }
                    Button(onClick = { if (state.trackingStage == TrackingStage.DELIVERED) onIntent(FoodIntent.NewOrder); onHome() },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(18.dp)) {
                        Text(if (state.trackingStage == TrackingStage.DELIVERED) "Order something else" else "Back to kitchens")
                    }
                    Text("Live showcase preview · route and timing are simulated locally.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable private fun DeliveryRouteMap(stage: TrackingStage, restaurant: String, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val reduced = LocalTQDMotionPolicy.current.reduced
    val progress by animateFloatAsState(stage.ordinal / 3f, MaterialTheme.motionScheme.slowSpatialSpec(), label = "Delivery journey")
    val pulse = if (!reduced && stage != TrackingStage.DELIVERED) {
        val animated by rememberInfiniteTransition(label = "Preview location").animateFloat(.55f, 1f,
            infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "Rider pulse")
        animated
    } else 1f
    Box(modifier.background(c.surfaceContainerHigh)) {
        Canvas(Modifier.fillMaxSize().semantics { contentDescription = "Preview delivery route from $restaurant to your address, ${stage.label}" }) {
            val w = size.width; val h = size.height
            repeat(8) { i -> drawLine(c.outlineVariant.copy(alpha = .45f), Offset(i * w / 7, 0f), Offset(i * w / 7 + w * .08f, h), 8.dp.toPx()) }
            repeat(5) { i -> drawLine(c.outlineVariant.copy(alpha = .4f), Offset(0f, i * h / 4), Offset(w, i * h / 4 + h * .05f), 8.dp.toPx()) }
            val points = listOf(Offset(w * .15f, h * .75f), Offset(w * .35f, h * .68f), Offset(w * .42f, h * .32f), Offset(w * .79f, h * .26f))
            val path = Path().apply { moveTo(points[0].x, points[0].y); points.drop(1).forEach { lineTo(it.x, it.y) } }
            drawPath(path, c.primary, style = Stroke(6.dp.toPx()))
            drawCircle(c.primaryContainer, 16.dp.toPx(), points.first()); drawCircle(c.onPrimaryContainer, 6.dp.toPx(), points.first())
            drawCircle(c.secondaryContainer, 17.dp.toPx(), points.last()); drawCircle(c.onSecondaryContainer, 6.dp.toPx(), points.last())
            val scaled = progress.coerceIn(0f, 1f) * 3; val segment = scaled.toInt().coerceAtMost(2); val local = (scaled - segment).coerceIn(0f, 1f)
            val dot = points[segment] + (points[segment + 1] - points[segment]) * local
            if (!reduced && stage != TrackingStage.DELIVERED) drawCircle(c.primary.copy(alpha = .18f * pulse), 24.dp.toPx() * pulse, dot)
            drawCircle(c.primary, 11.dp.toPx(), dot); drawCircle(c.onPrimary, 4.dp.toPx(), dot)
        }
        Surface(Modifier.align(Alignment.BottomStart).padding(14.dp), shape = CircleShape, color = c.surface) {
            Text("${if (stage == TrackingStage.DELIVERED) "At your doorstep" else "Following your meal"}", Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium, color = c.onSurface)
        }
    }
}
