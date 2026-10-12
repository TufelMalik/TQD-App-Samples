@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.techquantum.feature.fooddelivery.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.techquantum.core.designsystem.AnimatedMoney
import com.techquantum.core.designsystem.CartBadge
import com.techquantum.core.designsystem.QuantityStepper
import com.techquantum.core.designsystem.SkeletonShimmer
import com.techquantum.core.designsystem.formatRupees
import com.techquantum.feature.fooddelivery.domain.*
import com.techquantum.feature.fooddelivery.presentation.*

@Composable
fun FoodHomeScreen(state: FoodState, onIntent: (FoodIntent) -> Unit, onRestaurant: (String) -> Unit,
    heroModifier: @Composable (String) -> Modifier, modifier: Modifier = Modifier) {
    var locationPicker by rememberSaveable { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val largeType = LocalDensity.current.fontScale > 1.25f
    LazyColumn(modifier.fillMaxSize().testTag("food_home"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Row(Modifier.fillMaxWidth().clickable { locationPicker = true }.heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onSurface)
                Column(Modifier.weight(1f)) {
                    Text("Deliver to", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(state.deliveryArea, style = MaterialTheme.typography.titleMedium)
                }
                Icon(Icons.Default.ExpandMore, "Change delivery area")
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Good food.\nA good day.", style = MaterialTheme.typography.displaySmall)
                OutlinedTextField(state.query, { onIntent(FoodIntent.Search(it)) }, Modifier.fillMaxWidth().testTag("food_search"),
                    placeholder = { Text("Restaurant or cuisine") }, leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = if (state.query.isNotBlank()) ({ IconButton(onClick = { onIntent(FoodIntent.Search("")) }) { Icon(Icons.Default.Close, "Clear food search") } }) else null,
                    singleLine = true, shape = RoundedCornerShape(20.dp), colors = accessibleFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }))
            }
        }
        item {
            Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your lunch break,\nbut better.", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("Free delivery from ₹499. Local kitchens, freshly made.", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("What sounds good?", style = MaterialTheme.typography.titleLarge)
                Cuisine.entries.chunked(if (largeType) 2 else 3).forEach { cuisines ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        cuisines.forEach { cuisine ->
                            FilterChip(state.cuisine == cuisine, { onIntent(FoodIntent.SelectCuisine(cuisine)) },
                                label = { Text(cuisine.label) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp))
                        }
                    }
                }
            }
        }
        item { Text("Kitchens near you", style = MaterialTheme.typography.titleLarge) }
        when {
            state.loading -> items(3) { SkeletonShimmer(Modifier.fillMaxWidth().height(230.dp)) }
            state.error != null && state.restaurants.isEmpty() -> item { FoodEmptyPanel("The kitchens couldn't load", state.error,
                "Try again", { onIntent(FoodIntent.Retry) }) }
            state.filteredRestaurants.isEmpty() -> item { FoodEmptyPanel("No kitchens match yet", "Try another cuisine or a shorter search.",
                "Clear filters", { onIntent(FoodIntent.Search("")); onIntent(FoodIntent.SelectCuisine(Cuisine.ALL)) }) }
            else -> items(state.filteredRestaurants, key = Restaurant::id) { restaurant ->
                RestaurantCard(restaurant, { onRestaurant(restaurant.id) }, heroModifier(restaurant.id))
            }
        }
        item { Text("A local preview. No restaurant is contacted when you order.", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
    if (locationPicker) FoodSheet({ locationPicker = false }) {
        Text("Where shall we deliver?", style = MaterialTheme.typography.headlineSmall)
        listOf("Adajan, Surat", "Vesu, Surat", "City Light, Surat", "Pal, Surat").forEach { location ->
            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).selectable(state.deliveryArea == location, role = Role.RadioButton,
                onClick = { onIntent(FoodIntent.SelectLocation(location)); locationPicker = false }), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(state.deliveryArea == location, null, colors = accessibleRadioColors())
                Text(location, Modifier.padding(start = 12.dp))
            }
        }
    }
}

@Composable private fun RestaurantCard(restaurant: Restaurant, onClick: () -> Unit, heroModifier: Modifier) {
    Card(onClick = onClick, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        RestaurantArtwork(restaurant, heroModifier.fillMaxWidth().height(170.dp))
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(restaurant.name, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Text(" ${restaurant.rating}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }
            Text(restaurant.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${restaurant.cuisine.label} · ${restaurant.etaMinutes}–${restaurant.etaMinutes + 5} min · ${restaurant.distanceKm} km",
                style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun RestaurantMenuScreen(restaurant: Restaurant, state: FoodState, onIntent: (FoodIntent) -> Unit,
    onBack: () -> Unit, onCart: () -> Unit, onToggleDark: () -> Unit, heroModifier: Modifier, modifier: Modifier = Modifier) {
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var customDish by remember { mutableStateOf<Dish?>(null) }
    Scaffold(modifier.nestedScroll(scroll.nestedScrollConnection).testTag("food_menu"), contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeTopAppBar(title = { Text(restaurant.name) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Default.ArrowBack, "Go back") } },
                actions = {
                    IconButton(onClick = onToggleDark) { Icon(Icons.Default.Contrast, "Toggle dark mode") }
                    IconButton(onClick = onCart) { BadgedBox({ CartBadge(state.cartCount) }) { Icon(Icons.Default.ShoppingBag, "Food cart, ${state.cartCount} items") } }
                }, scrollBehavior = scroll)
        }, bottomBar = { if (state.cartCount > 0) FoodOrderBar(state, "View order", onCart) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    RestaurantArtwork(restaurant, heroModifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(26.dp)))
                    Text(restaurant.description, style = MaterialTheme.typography.bodyLarge)
                    Text("★ ${restaurant.rating} (${restaurant.reviewCount}) · ${restaurant.etaMinutes}–${restaurant.etaMinutes + 5} min · ${restaurant.distanceKm} km",
                        style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("5% GST, ₹5 platform fee. Delivery ₹29, free from ₹499.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            stickyHeader {
                Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(state.menuCategory == null, { onIntent(FoodIntent.SelectMenuCategory(null)) }, label = { Text("All dishes") }) }
                        items(MenuCategory.entries) { category -> FilterChip(state.menuCategory == category, { onIntent(FoodIntent.SelectMenuCategory(category)) }, label = { Text(category.label) }) }
                    }
                }
            }
            items(restaurant.dishes.filter { state.menuCategory == null || it.category == state.menuCategory }, key = Dish::id) { dish ->
                val plain = state.cart.firstOrNull { it.dish.id == dish.id && it.addOns.isEmpty() && it.note.isEmpty() }
                DishRow(dish, plain?.quantity ?: 0, { onIntent(FoodIntent.AddDish(dish.id)) },
                    { plain?.let { onIntent(FoodIntent.ChangeQuantity(it.key, -1)) } }, { customDish = dish }, Modifier.padding(horizontal = 20.dp))
            }
        }
    }
    customDish?.let { dish -> CustomizeDishSheet(dish, emptySet(), "", "Add to order", { customDish = null }) { addOns, note ->
        onIntent(FoodIntent.AddDish(dish.id, addOns, note)); customDish = null
    } }
}

@Composable private fun DishRow(dish: Dish, quantity: Int, onAdd: () -> Unit, onDecrease: () -> Unit, onCustomize: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VegLabel(dish.vegetarian)
                Text(dish.name, style = MaterialTheme.typography.titleMedium)
                Text(dish.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatRupees(dish.price), style = MaterialTheme.typography.titleMedium)
                if (dish.spice > 0) Text(if (dish.spice == 1) "Mild spice" else "Spicy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (dish.addOns.isNotEmpty()) TextButton(onClick = onCustomize, contentPadding = PaddingValues(0.dp), colors = accessibleTextButtonColors()) { Text("Customise") }
            }
            Column(Modifier.width(124.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DishArtwork(dish, Modifier.size(112.dp).clip(RoundedCornerShape(18.dp)))
                QuickFoodAdd(quantity, onAdd, onDecrease)
            }
        }
    }
}

@Composable private fun QuickFoodAdd(quantity: Int, onAdd: () -> Unit, onDecrease: () -> Unit) {
    val motion = MaterialTheme.motionScheme
    AnimatedContent(quantity > 0, transitionSpec = { (fadeIn(motion.fastEffectsSpec()) + scaleIn(motion.fastSpatialSpec(), .92f))
        .togetherWith(fadeOut(motion.fastEffectsSpec())).using(SizeTransform { _, _ -> motion.fastSpatialSpec() }) }, label = "Quick add") { added ->
        if (!added) FilledTonalButton(onClick = onAdd, modifier = Modifier.heightIn(min = 48.dp)) { Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Text(" Add") }
        else Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Remove, "Remove one item", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                AnimatedContent(quantity, label = "Dish count", transitionSpec = { fadeIn(motion.fastEffectsSpec()).togetherWith(fadeOut(motion.fastEffectsSpec())) }) {
                    Text(it.toString(), Modifier.width(28.dp).semantics { contentDescription = "Quantity $it" },
                        style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                IconButton(onClick = onAdd, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Add, "Add one item", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer) }
            }
        }
    }
}

@Composable private fun VegLabel(vegetarian: Boolean) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(Modifier.size(13.dp), shape = RoundedCornerShape(3.dp), border = BorderStroke(1.dp, if (vegetarian) colors.tertiary else colors.primary), color = colors.surface) {
            Box(Modifier.padding(3.dp).background(if (vegetarian) colors.tertiary else colors.primary, CircleShape))
        }
        Text(if (vegetarian) "Vegetarian" else "Non-vegetarian", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
    }
}

@Composable fun FoodEmptyPanel(title: String, description: String, action: String, onAction: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(Icons.Default.Restaurant, null, Modifier.size(44.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = onAction) { Text(action) }
    }
}

@Composable private fun FoodSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))) {
        Column(Modifier.fillMaxWidth().heightIn(max = 680.dp).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
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
            FoodEmptyPanel("Something delicious is missing", "Explore nearby kitchens and add your favourites.", "Explore kitchens", onBrowse)
        } else {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                item {
                    Text(state.cartRestaurant?.name ?: "Your kitchen", style = MaterialTheme.typography.headlineSmall)
                    Text("Freshly prepared for your doorstep", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(state.cart, key = FoodLine::key) { line ->
                    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                                DishArtwork(line.dish, Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(line.dish.name, style = MaterialTheme.typography.titleMedium)
                                    if (line.addOns.isNotEmpty()) Text(line.addOns.joinToString { "${it.name} (+${formatRupees(it.price)})" },
                                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (line.note.isNotBlank()) Text("Kitchen note: ${line.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${formatRupees(line.unitPrice)} each", style = MaterialTheme.typography.labelMedium)
                                }
                                IconButton(onClick = { onIntent(FoodIntent.RemoveLine(line.key)) }) { Icon(Icons.Default.DeleteOutline, "Remove ${line.dish.name}") }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                AnimatedMoney(line.total, Modifier.weight(1f))
                                QuantityStepper(line.quantity, { onIntent(FoodIntent.ChangeQuantity(line.key, -1)) }, { onIntent(FoodIntent.ChangeQuantity(line.key, 1)) })
                            }
                            TextButton(onClick = { customKey = line.key }, colors = accessibleTextButtonColors(), contentPadding = PaddingValues(0.dp)) { Text("Edit customisation") }
                        }
                    }
                }
                item {
                    OutlinedTextField(state.instructions, { onIntent(FoodIntent.SetInstructions(it)) }, Modifier.fillMaxWidth(), label = { Text("Delivery instructions") },
                        placeholder = { Text("Leave at the door, ring the bell…") }, minLines = 2, maxLines = 3, colors = accessibleFieldColors())
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Thank your rider", style = MaterialTheme.typography.titleMedium)
                        Text("Optional. Your full tip goes to the rider.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf(0, 10, 20, 30)) { amount -> FilterChip(state.tip == amount, { onIntent(FoodIntent.SetTip(amount)) },
                                label = { Text(if (amount == 0) "No tip" else formatRupees(amount)) }) }
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
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Every charge, up front", style = MaterialTheme.typography.titleMedium)
            BillRow("Items & add-ons", bill.subtotal)
            BillRow("GST (5%)", bill.tax)
            BillRow("Delivery", bill.delivery, bill.delivery == 0)
            BillRow("Platform fee", bill.platform)
            BillRow("Rider tip", bill.tip)
            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Total", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium); AnimatedMoney(bill.total) }
        }
    }
}
@Composable private fun BillRow(label: String, amount: Int, free: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(if (free) "Free" else formatRupees(amount), style = MaterialTheme.typography.bodyMedium)
    }
}
@Composable private fun FoodOrderBar(state: FoodState, action: String, onClick: () -> Unit, enabled: Boolean = true) {
    val largeType = LocalDensity.current.fontScale > 1.25f
    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 3.dp) {
        if (largeType) Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Total", Modifier.weight(1f)); AnimatedMoney(state.bill.total) }
            Button(onClick, Modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = enabled) { Text(action) }
        } else Row(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f)) { AnimatedMoney(state.bill.total); Text("Includes tax & fees", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Button(onClick, Modifier.heightIn(min = 52.dp), enabled = enabled) { Text(action) }
        }
    }
}

@Composable
fun FoodCheckoutScreen(state: FoodState, onIntent: (FoodIntent) -> Unit, onBrowse: () -> Unit, modifier: Modifier = Modifier) {
    val keyboard = LocalSoftwareKeyboardController.current
    Column(modifier.fillMaxSize().testTag("food_checkout")) {
        if (state.cart.isEmpty()) FoodEmptyPanel("Your order is empty", "Add a meal before choosing a delivery address.", "Explore kitchens", onBrowse)
        else {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                item {
                    Text("Good food, no sign-up.", style = MaterialTheme.typography.headlineMedium)
                    Text("Enter delivery details to place your local preview order.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { onIntent(FoodIntent.UpdateAddress(FoodAddress("Aditi Shah", "9876543210", "24, River View Apartments, Adajan", "Surat", "395009"))) },
                        enabled = !state.placingOrder, colors = accessibleTextButtonColors()) { Text("Use sample address") }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Delivery address", style = MaterialTheme.typography.titleLarge)
                        AddressField("Full name", state.address.name, state.addressErrors["name"], !state.placingOrder) { onIntent(FoodIntent.UpdateAddress(state.address.copy(name = it))) }
                        AddressField("Mobile number", state.address.phone, state.addressErrors["phone"], !state.placingOrder, KeyboardType.Phone) {
                            onIntent(FoodIntent.UpdateAddress(state.address.copy(phone = it.filter(Char::isDigit).take(10)))) }
                        AddressField("Building, street & landmark", state.address.line, state.addressErrors["line"], !state.placingOrder) { onIntent(FoodIntent.UpdateAddress(state.address.copy(line = it.take(180)))) }
                        AddressField("City", state.address.city, state.addressErrors["city"], !state.placingOrder) { onIntent(FoodIntent.UpdateAddress(state.address.copy(city = it))) }
                        AddressField("PIN code", state.address.pinCode, state.addressErrors["pinCode"], !state.placingOrder, KeyboardType.Number) {
                            onIntent(FoodIntent.UpdateAddress(state.address.copy(pinCode = it.filter(Char::isDigit).take(6)))) }
                    }
                }
                item {
                    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Payment", style = MaterialTheme.typography.titleLarge)
                        FoodPayment.entries.forEach { payment ->
                            Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).selectable(state.payment == payment, enabled = !state.placingOrder,
                                role = Role.RadioButton, onClick = { onIntent(FoodIntent.SelectPayment(payment)) }), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(state.payment == payment, null, enabled = !state.placingOrder, colors = accessibleRadioColors())
                                Text(payment.label, Modifier.padding(start = 10.dp))
                            }
                        }
                        Text("All payment choices are previews. No money is collected.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                item {
                    Text("${state.cartCount} items from ${state.cartRestaurant?.name}", style = MaterialTheme.typography.titleMedium)
                    if (state.instructions.isNotBlank()) Text("Delivery note: ${state.instructions}", style = MaterialTheme.typography.bodyMedium)
                    FoodBillCard(state.bill)
                }
                state.error?.let { error -> item { Text(error, Modifier.semantics { liveRegion = LiveRegionMode.Polite }, color = MaterialTheme.colorScheme.error) } }
            }
            FoodOrderBar(state, if (state.placingOrder) "Placing order…" else "Place order", { keyboard?.hide(); onIntent(FoodIntent.PlaceOrder) }, !state.placingOrder)
        }
    }
}

@Composable private fun AddressField(label: String, value: String, error: String?, enabled: Boolean, keyboardType: KeyboardType = KeyboardType.Text, onChange: (String) -> Unit) {
    OutlinedTextField(value, onChange, Modifier.fillMaxWidth(), enabled = enabled, label = { Text(label) }, singleLine = true,
        isError = error != null, supportingText = if (error != null) ({ Text(error) }) else null, colors = accessibleFieldColors(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next))
}

@Composable
fun FoodTrackingScreen(state: FoodState, onIntent: (FoodIntent) -> Unit, onHome: () -> Unit, modifier: Modifier = Modifier) {
    val order = state.order
    if (order == null) {
        Box(modifier.fillMaxSize().testTag("food_tracking")) { FoodEmptyPanel("A meal is waiting for you", "Order from a nearby kitchen to follow its journey.", "Explore kitchens", onHome) }
        return
    }
    val motion = MaterialTheme.motionScheme
    LazyColumn(modifier.fillMaxSize().testTag("food_tracking"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Text("Order ${order.id}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            AnimatedContent(state.trackingStage, transitionSpec = { (fadeIn(motion.defaultEffectsSpec()) + slideInVertically(motion.defaultSpatialSpec()) { it / 3 })
                .togetherWith(fadeOut(motion.fastEffectsSpec())) }, label = "Delivery status") { stage ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (stage == TrackingStage.DELIVERED) "Your meal has arrived." else stage.detail, style = MaterialTheme.typography.headlineLarge)
                    Text(if (stage == TrackingStage.DELIVERED) "Delivered to ${order.address.name}" else "Arriving in ${state.etaSeconds / 60} min ${state.etaSeconds % 60}s",
                        style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { DeliveryRouteMap(state.trackingStage, order.restaurant.name, Modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(28.dp))) }
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    TrackingStage.entries.forEach { stage ->
                        val completed = state.trackingStage.ordinal >= stage.ordinal
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Icon(if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
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
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(order.restaurant.name, style = MaterialTheme.typography.titleLarge)
                Text(order.lines.joinToString { "${it.quantity} × ${it.dish.name}" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${order.address.line}, ${order.address.city} ${order.address.pinCode}", style = MaterialTheme.typography.bodyMedium)
                Text("${formatRupees(order.bill.total)} · ${order.payment.label}", style = MaterialTheme.typography.titleMedium)
                if (state.trackingStage != TrackingStage.DELIVERED) OutlinedButton(onClick = { onIntent(FoodIntent.AdvanceTracking) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) { Text("Preview next update") }
                Button(onClick = { if (state.trackingStage == TrackingStage.DELIVERED) onIntent(FoodIntent.NewOrder); onHome() }, Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    Text(if (state.trackingStage == TrackingStage.DELIVERED) "Order something else" else "Back to kitchens")
                }
                Text("Live showcase preview · route and timing are simulated locally.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable private fun DeliveryRouteMap(stage: TrackingStage, restaurant: String, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val progress by animateFloatAsState(stage.ordinal / 3f, MaterialTheme.motionScheme.slowSpatialSpec(), label = "Delivery journey")
    val pulse by rememberInfiniteTransition(label = "Live location").animateFloat(.55f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "Rider pulse")
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
            drawCircle(c.primary.copy(alpha = .18f * pulse), 24.dp.toPx() * pulse, dot)
            drawCircle(c.primary, 11.dp.toPx(), dot); drawCircle(c.onPrimary, 4.dp.toPx(), dot)
        }
        Surface(Modifier.align(Alignment.BottomStart).padding(14.dp), shape = CircleShape, color = c.surface) {
            Text("${if (stage == TrackingStage.DELIVERED) "At your doorstep" else "Following your meal"}", Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium, color = c.onSurface)
        }
    }
}
