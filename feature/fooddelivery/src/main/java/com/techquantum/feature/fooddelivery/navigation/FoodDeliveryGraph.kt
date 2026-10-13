@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.techquantum.feature.fooddelivery.navigation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.techquantum.core.designsystem.CartBadge
import com.techquantum.core.designsystem.CompactModuleTitle
import com.techquantum.core.designsystem.LocalPaletteVisible
import com.techquantum.core.designsystem.PaletteAction
import com.techquantum.core.designsystem.SkeletonShimmer
import com.techquantum.feature.fooddelivery.presentation.FoodEffect
import com.techquantum.feature.fooddelivery.presentation.FoodIntent
import com.techquantum.feature.fooddelivery.presentation.FoodViewModel
import com.techquantum.feature.fooddelivery.ui.FoodCartScreen
import com.techquantum.feature.fooddelivery.ui.FoodCheckoutScreen
import com.techquantum.feature.fooddelivery.ui.FoodEmptyScreen
import com.techquantum.feature.fooddelivery.ui.FoodHomeScreen
import com.techquantum.feature.fooddelivery.ui.FoodTrackingScreen
import com.techquantum.feature.fooddelivery.ui.RestaurantMenuScreen

/** Food owns a nested graph, while hero artwork participates in the app's shared transition root. */
@Composable
fun FoodDeliveryGraph(
    navController: NavHostController,
    sharedScope: SharedTransitionScope,
    onToggleDark: () -> Unit,
    modifier: Modifier = Modifier,
    onShowThemes: () -> Unit = {},
    viewModel: FoodViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "food/home"
    val motion = MaterialTheme.motionScheme
    fun goCart() = navController.navigate("food/cart") { launchSingleTop = true }
    fun goHome() {
        if (!navController.popBackStack("food/home", false)) navController.navigate("food/home") { launchSingleTop = true }
    }
    fun goTracking() = navController.navigate("food/tracking") { popUpTo("food/home"); launchSingleTop = true }
    LaunchedEffect(viewModel) { viewModel.effects.collect { if (it is FoodEffect.OrderPlaced) goTracking() } }
    LaunchedEffect(state.order?.id, state.cart.isEmpty(), route) {
        if (state.order != null && state.cart.isEmpty() && route == "food/checkout") goTracking()
    }
    Scaffold(modifier = modifier, contentWindowInsets = WindowInsets(0, 0, 0, 0), topBar = {
        if (route != "food/menu/{restaurantId}" || state.restaurants.none { it.id == entry?.arguments?.getString("restaurantId") }) TopAppBar(
            title = { CompactModuleTitle(when (route) { "food/cart" -> "Your food order"; "food/checkout" -> "Guest checkout"; "food/tracking" -> "Follow your meal"; else -> "Local kitchens" }) },
            navigationIcon = { if (route != "food/home") IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Default.ArrowBack, "Go back") } },
            actions = {
                PaletteAction(onShowThemes)
                if (route != "food/tracking") IconButton(onClick = ::goCart) { BadgedBox({ CartBadge(state.cartCount) }) { Icon(Icons.Default.ShoppingBag, "Food cart, ${state.cartCount} items") } }
            },
        )
    }) { padding ->
        val heroSpring = motion.slowSpatialSpec<Rect>()
        NavHost(
            navController = navController,
            startDestination = "food/home",
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
            enterTransition = { androidx.compose.animation.slideInHorizontally(motion.defaultSpatialSpec()) { it / 3 } + fadeIn(motion.defaultEffectsSpec()) },
            exitTransition = { androidx.compose.animation.slideOutHorizontally(motion.defaultSpatialSpec()) { -it / 3 } + fadeOut(motion.defaultEffectsSpec()) },
            popEnterTransition = { androidx.compose.animation.slideInHorizontally(motion.defaultSpatialSpec()) { -it / 3 } + fadeIn(motion.defaultEffectsSpec()) },
            popExitTransition = { androidx.compose.animation.slideOutHorizontally(motion.defaultSpatialSpec()) { it / 3 } + fadeOut(motion.defaultEffectsSpec()) },
        ) {
            composable("food/home") {
                val visibility = this
                FoodHomeScreen(state, viewModel::accept, { id ->
                    viewModel.accept(FoodIntent.SelectMenuCategory(null))
                    navController.navigate("food/menu/$id") { launchSingleTop = true }
                }, heroModifier = { id -> with(sharedScope) {
                    Modifier.sharedElement(rememberSharedContentState("food-restaurant-$id"), visibility, boundsTransform = { _, _ -> heroSpring })
                } })
            }
            composable("food/menu/{restaurantId}") { backStack ->
                val restaurant = state.restaurants.firstOrNull { it.id == backStack.arguments?.getString("restaurantId") }
                if (restaurant == null) {
                    if (state.loading) SkeletonShimmer(Modifier.padding(20.dp).fillMaxWidth().height(300.dp))
                    else if (state.error != null) FoodEmptyScreen("The kitchens couldn't load", state.error.orEmpty(), "Retry", { viewModel.accept(FoodIntent.Retry) })
                    else FoodEmptyScreen("This kitchen is unavailable", "Explore another local kitchen.", "Explore kitchens", ::goHome)
                } else {
                    val hero = with(sharedScope) { Modifier.sharedElement(rememberSharedContentState("food-restaurant-${restaurant.id}"),
                        this@composable, boundsTransform = { _, _ -> heroSpring }) }
                    RestaurantMenuScreen(restaurant, state, viewModel::accept, { navController.popBackStack() }, ::goCart, onToggleDark, hero, onShowThemes = onShowThemes)
                }
            }
            composable("food/cart") { FoodCartScreen(state, viewModel::accept, { navController.navigate("food/checkout") { launchSingleTop = true } }, ::goHome) }
            composable("food/checkout") { FoodCheckoutScreen(state, viewModel::accept, ::goHome) }
            composable("food/tracking") { FoodTrackingScreen(state, viewModel::accept, ::goHome) }
        }
    }
    if (!LocalPaletteVisible.current) state.pendingDish?.let { pending ->
        val nextRestaurant = state.restaurants.firstOrNull { restaurant -> restaurant.dishes.any { it.id == pending.dishId } }
        AlertDialog(
            onDismissRequest = { viewModel.accept(FoodIntent.CancelRestaurantSwitch) },
            title = { Column { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { PaletteAction(onShowThemes) }; Text("Start a new restaurant order?") } },
            text = { Text("Your items from ${state.cartRestaurant?.name} will be replaced with a new order from ${nextRestaurant?.name}.") },
            confirmButton = { TextButton(onClick = { viewModel.accept(FoodIntent.ConfirmRestaurantSwitch) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) { Text("Start new order") } },
            dismissButton = { TextButton(onClick = { viewModel.accept(FoodIntent.CancelRestaurantSwitch) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) { Text("Keep current order") } },
        )
    }
}
