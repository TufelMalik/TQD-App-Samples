@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.techquantum.feature.fooddelivery.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.techquantum.core.designsystem.CartBadge
import com.techquantum.core.designsystem.CompactModuleTitle
import com.techquantum.core.designsystem.DietaryIndicator
import com.techquantum.core.designsystem.EditorialArtwork
import com.techquantum.core.designsystem.PaletteAction
import com.techquantum.core.designsystem.RatingPill
import com.techquantum.core.designsystem.ShowcaseSheet
import com.techquantum.core.designsystem.SkeletonShimmer
import com.techquantum.core.designsystem.formatRupees
import com.techquantum.feature.fooddelivery.domain.Cuisine
import com.techquantum.feature.fooddelivery.domain.Dish
import com.techquantum.feature.fooddelivery.domain.Restaurant
import com.techquantum.feature.fooddelivery.presentation.FoodIntent
import com.techquantum.feature.fooddelivery.presentation.FoodState
import com.techquantum.core.designsystem.R as DesignR

@Composable
internal fun FreshFoodHome(state: FoodState, onIntent: (FoodIntent) -> Unit, onRestaurant: (String) -> Unit,
    heroModifier: @Composable (String) -> Modifier, modifier: Modifier) {
    var location by rememberSaveable { mutableStateOf(false) }
    val large = LocalDensity.current.fontScale > 1.25f
    val keyboard = LocalSoftwareKeyboardController.current
    LazyColumn(modifier.fillMaxSize().testTag("food_home"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item(key = "location", contentType = "header") {
            TextButton({ location = true }, Modifier.heightIn(min = 48.dp)) {
                Icon(Icons.Default.LocationOn, null); Text(state.deliveryArea, Modifier.padding(horizontal = 8.dp)); Icon(Icons.Default.ExpandMore, null)
            }
        }
        item(key = "search", contentType = "search") {
            OutlinedTextField(state.query, { onIntent(FoodIntent.Search(it)) }, Modifier.fillMaxWidth().testTag("food_search"),
                label = { Text(stringResource(DesignR.string.selected_copy_1)) }, singleLine = true, shape = RoundedCornerShape(999.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                leadingIcon = { Icon(Icons.Default.Search, null) }, trailingIcon = {
                    if (state.query.isNotBlank()) IconButton({ onIntent(FoodIntent.Search("")) }) { Icon(Icons.Default.Close, "Clear food search") }
                })
        }
        item(key = "campaign", contentType = "campaign") {
            Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                Row(Modifier.heightIn(min = 190.dp)) {
                    Column(Modifier.weight(1f).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(DesignR.string.fresh_campaign), style = MaterialTheme.typography.headlineSmall)
                        Text(stringResource(DesignR.string.fresh_campaign_copy), style = MaterialTheme.typography.bodySmall)
                    }
                    EditorialArtwork(false, 0, null, Modifier.width(if (large) 100.dp else 140.dp).height(210.dp))
                }
            }
        }
        item(key = "cuisines", contentType = "categories") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(DesignR.string.fresh_cuisines), style = MaterialTheme.typography.titleLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(Cuisine.entries, key = { it.name }, contentType = { "cuisine" }) { cuisine ->
                        Surface(onClick = { onIntent(FoodIntent.SelectCuisine(cuisine)) }, shape = RoundedCornerShape(18.dp),
                            color = if (state.cuisine == cuisine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.width(if (large) 120.dp else 88.dp).semantics { selected = state.cuisine == cuisine }) {
                            Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                EditorialArtwork(false, when(cuisine) { Cuisine.SOUTH_INDIAN -> 1; Cuisine.PIZZA -> 2; Cuisine.HEALTHY -> 3; else -> 0 }, null,
                                    Modifier.size(56.dp).clip(CircleShape))
                                Text(cuisine.label, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
        item(key = "restaurants-heading", contentType = "heading") { Text(stringResource(DesignR.string.fresh_restaurants), style = MaterialTheme.typography.titleLarge) }
        when {
            state.loading && state.restaurants.isEmpty() -> items(2, key = { "skeleton-$it" }, contentType = { "skeleton" }) { SkeletonShimmer(Modifier.fillMaxWidth().height(190.dp)) }
            state.error != null && state.restaurants.isEmpty() -> item(key = "error", contentType = "error") { FoodEmptyPanel("The kitchens couldn't load", state.error, "Try again", { onIntent(FoodIntent.Retry) }) }
            state.filteredRestaurants.isEmpty() -> item(key = "empty", contentType = "empty") { FoodEmptyPanel("No kitchens match yet", "Try another cuisine or a shorter search.", "Clear filters", { onIntent(FoodIntent.Search("")); onIntent(FoodIntent.SelectCuisine(Cuisine.ALL)) }) }
            else -> items(state.filteredRestaurants.chunked(if (large) 1 else 2), key = { it.first().id }, contentType = { "restaurant-row" }) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { restaurant -> FreshRestaurantCard(restaurant, { onRestaurant(restaurant.id) }, heroModifier(restaurant.id), Modifier.weight(1f)) }
                    if (!large && row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        item(key = "demo", contentType = "note") { Text(stringResource(DesignR.string.selected_copy_2), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
    if (location) ShowcaseSheet({ location = false }) {
        Text(stringResource(DesignR.string.selected_copy_3), style = MaterialTheme.typography.headlineSmall)
        listOf("Adajan, Surat", "Vesu, Surat", "City Light, Surat", "Pal, Surat").forEach { area ->
            TextButton({ onIntent(FoodIntent.SelectLocation(area)); location = false }, Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(area) }
        }
    }
}

@Composable
private fun FreshRestaurantCard(restaurant: Restaurant, onClick: () -> Unit, hero: Modifier, modifier: Modifier) {
    Card(onClick, modifier, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        RestaurantArtwork(restaurant, hero.fillMaxWidth().height(124.dp))
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(restaurant.name, style = MaterialTheme.typography.titleMedium)
            RatingPill(restaurant.rating)
            Text("${restaurant.etaMinutes}–${restaurant.etaMinutes + 5} min · ${restaurant.cuisine.label}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun FreshRestaurantMenu(restaurant: Restaurant, state: FoodState, onIntent: (FoodIntent) -> Unit,
    onBack: () -> Unit, onCart: () -> Unit, heroModifier: Modifier, modifier: Modifier,
    onShowThemes: () -> Unit, onCustomize: (Dish) -> Unit) {
    val dishes = restaurant.dishes.filter { state.menuCategory == null || it.category == state.menuCategory }
    val large = LocalDensity.current.fontScale > 1.25f || LocalConfiguration.current.screenWidthDp < 360
    Scaffold(modifier.testTag("food_menu"), contentWindowInsets = WindowInsets(0,0,0,0), topBar = {
        TopAppBar(title = { CompactModuleTitle(restaurant.name) }, navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back") } },
            actions = { PaletteAction(onShowThemes); IconButton(onCart) { BadgedBox({ CartBadge(state.cartCount) }) { Icon(Icons.Default.ShoppingBag, "Food cart, ${state.cartCount} items") } } })
    }, bottomBar = { if (state.cartCount > 0) FoodOrderBar(state, "View order", onCart) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).testTag("food_menu_list"), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item(key = "restaurant-story", contentType = "hero") {
                Column {
                    RestaurantArtwork(restaurant, heroModifier.fillMaxWidth().height(230.dp))
                    Surface(Modifier.fillMaxWidth().offset(y = (-18).dp), shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), color = MaterialTheme.colorScheme.surface) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(restaurant.name, style = MaterialTheme.typography.headlineSmall)
                            RatingPill(restaurant.rating, restaurant.reviewCount)
                            Text(restaurant.description, style = MaterialTheme.typography.bodyMedium)
                            Text("${restaurant.etaMinutes}–${restaurant.etaMinutes + 5} min · ${restaurant.distanceKm} km", style = MaterialTheme.typography.labelLarge)
                            Text(stringResource(DesignR.string.selected_copy_4), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            stickyHeader(key = "menu-categories", contentType = "categories") {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item(key = "all", contentType = "category") { FilterChip(state.menuCategory == null, { onIntent(FoodIntent.SelectMenuCategory(null)) }, label = { Text(stringResource(DesignR.string.selected_copy_5)) }, modifier = Modifier.heightIn(min = 48.dp)) }
                        items(restaurant.dishes.map { it.category }.distinct(), key = { it.name }, contentType = { "category" }) { category ->
                            FilterChip(state.menuCategory == category, { onIntent(FoodIntent.SelectMenuCategory(category)) }, label = { Text(category.label) }, modifier = Modifier.heightIn(min = 48.dp))
                        }
                    }
                }
            }
            if (dishes.isEmpty()) item(key = "empty-menu", contentType = "empty") { FoodEmptyPanel("No dishes in this category", "Choose another category to keep exploring.", "Show all dishes", { onIntent(FoodIntent.SelectMenuCategory(null)) }) }
            else {
                item(key = "featured-heading", contentType = "heading") { Text(stringResource(DesignR.string.fresh_featured), Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.titleLarge) }
                item(key = "featured-${dishes.first().id}", contentType = "featured") {
                    val dish = dishes.first()
                    val plain = state.cart.firstOrNull { it.dish.id == dish.id && it.addOns.isEmpty() && it.note.isEmpty() }
                    RecommendedDish(dish, plain?.quantity ?: 0, { onIntent(FoodIntent.AddDish(dish.id)) },
                        { plain?.let { onIntent(FoodIntent.ChangeQuantity(it.key, -1)) } }, { onCustomize(dish) },
                        Modifier.padding(horizontal = 20.dp).fillMaxWidth(), featured = true)
                }
                if (dishes.size > 1) {
                    item(key = "recommendations-heading", contentType = "heading") {
                        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(DesignR.string.fresh_recommended), style = MaterialTheme.typography.titleLarge)
                            Text(stringResource(DesignR.string.fresh_recommended_copy), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    items(dishes.drop(1).chunked(if (large) 1 else 2), key = { it.first().id }, contentType = { "recommendation-row" }) { row ->
                        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { dish ->
                                val plain = state.cart.firstOrNull { it.dish.id == dish.id && it.addOns.isEmpty() && it.note.isEmpty() }
                                RecommendedDish(dish, plain?.quantity ?: 0, { onIntent(FoodIntent.AddDish(dish.id)) },
                                    { plain?.let { onIntent(FoodIntent.ChangeQuantity(it.key, -1)) } }, { onCustomize(dish) }, Modifier.weight(1f))
                            }
                            if (!large && row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendedDish(dish: Dish, quantity: Int, onAdd: () -> Unit, onDecrease: () -> Unit, onCustomize: () -> Unit,
    modifier: Modifier, featured: Boolean = false) {
    Surface(modifier, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .3f))) {
        Column {
            DishArtwork(dish, Modifier.fillMaxWidth().aspectRatio(if (featured) 1.9f else 1.25f))
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DietaryIndicator(dish.vegetarian)
                Text(dish.name, style = MaterialTheme.typography.titleMedium)
                if (featured) Text(dish.description, style = MaterialTheme.typography.bodyMedium)
                Text(formatRupees(dish.price), style = MaterialTheme.typography.titleMedium)
                QuickFoodAdd(quantity, onAdd, onDecrease, dish.name)
                if (dish.addOns.isNotEmpty()) TextButton(onCustomize, Modifier.heightIn(min = 48.dp)) { Text(stringResource(DesignR.string.selected_copy_6)) }
            }
        }
    }
}
