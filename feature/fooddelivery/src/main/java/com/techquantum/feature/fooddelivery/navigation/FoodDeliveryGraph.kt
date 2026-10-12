@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.techquantum.feature.fooddelivery.navigation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.techquantum.core.designsystem.CartBadge
import com.techquantum.core.designsystem.SkeletonShimmer
import com.techquantum.feature.fooddelivery.presentation.*
import com.techquantum.feature.fooddelivery.ui.*

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
        if (route != "food/menu/{restaurantId}") TopAppBar(
            title = { Text(when (route) { "food/cart" -> "Your food order"; "food/checkout" -> "Guest checkout"; "food/tracking" -> "Follow your meal"; else -> "Neighbourhood kitchens" }, style = MaterialTheme.typography.titleLarge) },
            navigationIcon = { if (route != "food/home") IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Default.ArrowBack, "Go back") } },
            actions = {
                IconButton(onClick = onShowThemes) { Icon(Icons.Outlined.Palette, "Color palette") }
                IconButton(onClick = onToggleDark) { Icon(Icons.Default.Contrast, "Toggle dark mode") }
                if (state.order != null && route != "food/tracking") IconButton(onClick = ::goTracking) { Icon(Icons.Default.LocalShipping, "Track food order") }
                if (route != "food/tracking") IconButton(onClick = ::goCart) { BadgedBox({ CartBadge(state.cartCount) }) { Icon(Icons.Default.ShoppingBag, "Food cart, ${state.cartCount} items") } }
            },
        )
    }) { padding ->
        val heroSpring = motion.slowSpatialSpec<Rect>()
        NavHost(
            navController = navController,
            startDestination = "food/home",
            modifier = Modifier.fillMaxSize().padding(padding),
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
                    else FoodEmptyPanel("This kitchen is unavailable", "Explore another local kitchen.", "Explore kitchens", ::goHome)
                } else {
                    val hero = with(sharedScope) { Modifier.sharedElement(rememberSharedContentState("food-restaurant-${restaurant.id}"),
                        this@composable, boundsTransform = { _, _ -> heroSpring }) }
                    RestaurantMenuScreen(restaurant, state, viewModel::accept, { navController.popBackStack() }, ::goCart, onToggleDark, hero)
                }
            }
            composable("food/cart") { FoodCartScreen(state, viewModel::accept, { navController.navigate("food/checkout") { launchSingleTop = true } }, ::goHome) }
            composable("food/checkout") { FoodCheckoutScreen(state, viewModel::accept, ::goHome) }
            composable("food/tracking") { FoodTrackingScreen(state, viewModel::accept, ::goHome) }
        }
    }
    state.pendingDish?.let { pending ->
        val nextRestaurant = state.restaurants.firstOrNull { restaurant -> restaurant.dishes.any { it.id == pending.dishId } }
        AlertDialog(
            onDismissRequest = { viewModel.accept(FoodIntent.CancelRestaurantSwitch) },
            title = { Text("Start a new restaurant order?") },
            text = { Text("Your items from ${state.cartRestaurant?.name} will be replaced with a new order from ${nextRestaurant?.name}.") },
            confirmButton = { TextButton(onClick = { viewModel.accept(FoodIntent.ConfirmRestaurantSwitch) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) { Text("Start new order") } },
            dismissButton = { TextButton(onClick = { viewModel.accept(FoodIntent.CancelRestaurantSwitch) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) { Text("Keep current order") } },
        )
    }
}
