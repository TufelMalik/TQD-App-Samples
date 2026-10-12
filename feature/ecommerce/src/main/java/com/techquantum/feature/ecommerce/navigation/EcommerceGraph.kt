@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.techquantum.feature.ecommerce.navigation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.techquantum.core.designsystem.CartBadge
import com.techquantum.core.designsystem.LocalFashionTheme
import com.techquantum.core.designsystem.SkeletonShimmer
import com.techquantum.feature.ecommerce.domain.FashionCategory
import com.techquantum.feature.ecommerce.presentation.*
import com.techquantum.feature.ecommerce.ui.*
import kotlinx.coroutines.launch

/** All six destinations share one ViewModel and the shell's root shared transition scope. */
@Composable
fun EcommerceGraph(
    navController: NavHostController,
    sharedScope: SharedTransitionScope,
    onShowThemes: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EcommerceViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "discover"
    val snackbar = remember { SnackbarHostState() }
    val flight = remember { Animatable(1f) }
    val motion = MaterialTheme.motionScheme
    val brand = LocalFashionTheme.current.brandName
    fun goCart() = navController.navigate("cart") { launchSingleTop = true }
    fun browse(category: FashionCategory) {
        viewModel.accept(EcommerceIntent.SelectCategory(category))
        navController.navigate("listing") { launchSingleTop = true }
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is EcommerceEffect.AddedToCart -> {
                    launch { flight.snapTo(0f); flight.animateTo(1f, motion.slowSpatialSpec()) }
                    launch { snackbar.showSnackbar("${effect.productName} added to your bag") }
                }
                is EcommerceEffect.OrderPlaced -> navController.navigate("confirmation") {
                    popUpTo("discover")
                    launchSingleTop = true
                }
            }
        }
    }
    // Recover a completed local checkout if its one-shot effect arrived while another tab was open.
    LaunchedEffect(state.order?.id, state.cart.isEmpty(), route) {
        if (state.order != null && state.cart.isEmpty() && route == "checkout") navController.navigate("confirmation") {
            popUpTo("discover")
            launchSingleTop = true
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text(when (route) {
                            "discover" -> brand
                            "listing" -> "The collection"
                            "cart" -> "Your bag"
                            "checkout" -> "Checkout"
                            "confirmation" -> "Order confirmed"
                            else -> "A closer look"
                        }, style = MaterialTheme.typography.titleLarge)
                        if (route == "discover") Text("Curated by TechQuantum", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    if (route != "discover") IconButton(onClick = {
                        if (route == "confirmation") navController.popBackStack("discover", false)
                        else navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Go back")
                    }
                },
                actions = {
                    IconButton(onClick = onShowThemes) { Icon(Icons.Outlined.Palette, "Change fashion theme") }
                    if (route != "confirmation") IconButton(onClick = { goCart() }) {
                        BadgedBox(badge = { CartBadge(state.cartCount) }) {
                            Icon(Icons.Outlined.ShoppingBag, "Your bag, ${state.cartCount} items")
                        }
                    }
                },
            )
        },
        snackbarHost = {
            // Purchase actions remain tappable while an add-to-bag acknowledgement is visible.
            SnackbarHost(snackbar, Modifier.padding(bottom = when (route) {
                "cart", "checkout" -> 144.dp
                "product/{productId}" -> 100.dp
                else -> 0.dp
            }))
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val heroSpring = motion.slowSpatialSpec<Rect>()
            NavHost(
                navController = navController,
                startDestination = "discover",
                enterTransition = { androidx.compose.animation.slideInHorizontally(motion.defaultSpatialSpec()) { it / 3 } + fadeIn(motion.defaultEffectsSpec()) },
                exitTransition = { androidx.compose.animation.slideOutHorizontally(motion.defaultSpatialSpec()) { -it / 3 } + fadeOut(motion.defaultEffectsSpec()) },
                popEnterTransition = { androidx.compose.animation.slideInHorizontally(motion.defaultSpatialSpec()) { -it / 3 } + fadeIn(motion.defaultEffectsSpec()) },
                popExitTransition = { androidx.compose.animation.slideOutHorizontally(motion.defaultSpatialSpec()) { it / 3 } + fadeOut(motion.defaultEffectsSpec()) },
                modifier = Modifier.fillMaxSize(),
            ) {
                composable("discover") {
                    val visibilityScope = this
                    DiscoverScreen(state, viewModel::accept, ::browse,
                        onProduct = { navController.navigate("product/$it") },
                        heroModifier = { id -> with(sharedScope) {
                            Modifier.sharedElement(rememberSharedContentState("product-$id"), visibilityScope,
                                boundsTransform = { _, _ -> heroSpring })
                        } })
                }
                composable("listing") {
                    val visibilityScope = this
                    ProductListingScreen(state, viewModel::accept,
                        onProduct = { navController.navigate("product/$it") },
                        heroModifier = { id -> with(sharedScope) {
                            Modifier.sharedElement(rememberSharedContentState("product-$id"), visibilityScope,
                                boundsTransform = { _, _ -> heroSpring })
                        } })
                }
                composable("product/{productId}") { backStack ->
                    val product = state.products.firstOrNull { it.id == backStack.arguments?.getString("productId") }
                    if (product != null) {
                        val imageModifier = with(sharedScope) {
                            Modifier.sharedElement(rememberSharedContentState("product-${product.id}"),
                                this@composable, boundsTransform = { _, _ -> heroSpring })
                        }
                        ProductDetailScreen(product, state, viewModel::accept, { goCart() }, imageModifier)
                    } else if (state.loading) {
                        SkeletonShimmer(Modifier.padding(20.dp).fillMaxWidth().height(360.dp))
                    } else {
                        EmptyFashionState("This piece is unavailable", "Browse the collection to find something you love.",
                            "Browse collection", { browse(FashionCategory.ALL) })
                    }
                }
                composable("cart") {
                    CartScreen(state, viewModel::accept, onCheckout = { navController.navigate("checkout") },
                        onBrowse = { browse(FashionCategory.ALL) })
                }
                composable("checkout") { CheckoutScreen(state, viewModel::accept, onBrowse = { browse(FashionCategory.ALL) }) }
                composable("confirmation") {
                    ConfirmationScreen(state.order, onContinue = {
                        viewModel.accept(EcommerceIntent.StartNewOrder)
                        navController.popBackStack("discover", false)
                    })
                }
            }
            if (flight.value < .99f) {
                val color = MaterialTheme.colorScheme.primary
                Canvas(Modifier.fillMaxSize()) {
                    val t = flight.value.coerceIn(0f, 1f)
                    val x = size.width * (.5f + .4f * t)
                    val y = size.height * (.83f * (1 - t) - .24f * kotlin.math.sin(t * Math.PI).toFloat())
                    drawCircle(color, (12.dp.toPx() * (1 - .5f * t)), Offset(x, y))
                }
            }
        }
    }
}
