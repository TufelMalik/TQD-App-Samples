@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.techquantum.feature.ecommerce.navigation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.techquantum.core.designsystem.CartBadge
import com.techquantum.core.designsystem.CompactModuleTitle
import com.techquantum.core.designsystem.LocalActionFooterHeightChanged
import com.techquantum.core.designsystem.LocalFashionTheme
import com.techquantum.core.designsystem.PaletteAction
import com.techquantum.core.designsystem.SkeletonShimmer
import com.techquantum.feature.ecommerce.domain.FashionCategory
import com.techquantum.feature.ecommerce.presentation.EcommerceEffect
import com.techquantum.feature.ecommerce.presentation.EcommerceIntent
import com.techquantum.feature.ecommerce.presentation.EcommerceViewModel
import com.techquantum.feature.ecommerce.ui.CartScreen
import com.techquantum.feature.ecommerce.ui.CheckoutScreen
import com.techquantum.feature.ecommerce.ui.ConfirmationScreen
import com.techquantum.feature.ecommerce.ui.DiscoverScreen
import com.techquantum.feature.ecommerce.ui.EmptyFashionState
import com.techquantum.feature.ecommerce.ui.ProductDetailScreen
import com.techquantum.feature.ecommerce.ui.ProductListingScreen
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
    var footerHeight by remember(route) { mutableIntStateOf(0) }
    val footerDp = with(LocalDensity.current) { footerHeight.toDp() }
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
                    if (effect.openCart) goCart()
                    launch { snackbar.showSnackbar("${effect.productName} added to your bag") }
                }
                is EcommerceEffect.OrderPlaced -> navController.navigate("confirmation") {
                    popUpTo("discover")
                    launchSingleTop = true
                }
            }
        }
    }
    LaunchedEffect(state.error, route) {
        val error = state.error
        if (error != null && route != "checkout" && state.products.isNotEmpty()) {
            snackbar.showSnackbar(error)
            if (viewModel.state.value.error == error) viewModel.accept(EcommerceIntent.ClearError)
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
                        CompactModuleTitle(when (route) {
                            "discover" -> brand
                            "listing" -> "The collection"
                            "cart" -> "Your bag"
                            "checkout" -> "Checkout"
                            "confirmation" -> "Order confirmed"
                            else -> "A closer look"
                        }, if (route == "discover") "Curated by TechQuantum" else null)
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
                    PaletteAction(onShowThemes, description = "Change fashion theme")
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
            SnackbarHost(snackbar, Modifier.padding(bottom = footerDp + 8.dp))
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            val heroSpring = motion.slowSpatialSpec<Rect>()
            CompositionLocalProvider(LocalActionFooterHeightChanged provides { footerHeight = it }) {
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
                    } else if (state.error != null) {
                        EmptyFashionState("This collection couldn't load", state.error.orEmpty(), "Retry", { viewModel.accept(EcommerceIntent.Retry) })
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
            }
        }
    }
}
