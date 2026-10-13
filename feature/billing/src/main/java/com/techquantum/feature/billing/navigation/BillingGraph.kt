@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.techquantum.feature.billing.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.techquantum.core.designsystem.PaletteAction
import com.techquantum.feature.billing.presentation.BillingIntent
import com.techquantum.feature.billing.presentation.BillingViewModel
import com.techquantum.feature.billing.ui.BillPreviewScreen
import com.techquantum.feature.billing.ui.BillingItemEditor
import com.techquantum.feature.billing.ui.BillingOutputSheet
import com.techquantum.feature.billing.ui.FoodSelectionScreen
import com.techquantum.feature.billing.ui.RunningBillScreen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BillingGraph(
    navController: NavHostController,
    onToggleDark: () -> Unit,
    modifier: Modifier = Modifier,
    onShowThemes: () -> Unit = {},
) {
    val viewModel: BillingViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "select-food"
    val snackbar = remember { SnackbarHostState() }
    var actionBarHeightPx by remember { mutableIntStateOf(0) }
    val snackbarBottomPadding = with(LocalDensity.current) { actionBarHeightPx.toDp() } + 8.dp
    val motion = MaterialTheme.motionScheme
    fun browse() { navController.navigate("select-food") { popUpTo("select-food"); launchSingleTop = true } }
    LaunchedEffect(viewModel) { viewModel.effects.collectLatest { snackbar.showSnackbar(it.message) } }
    Scaffold(modifier = modifier, snackbarHost = {
        SnackbarHost(snackbar, modifier = Modifier.padding(bottom = snackbarBottomPadding))
    },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(title = { CompactModuleTitle("Kitchen bill", "TechQuantum · Counter demo") }, navigationIcon = {
                if (route != "select-food") IconButton({ navController.popBackStack() }, Modifier.size(56.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back")
                }
            }, actions = {
                PaletteAction(onShowThemes, 56.dp)
                IconButton({ navController.navigate("running-bill") { launchSingleTop = true } }, Modifier.size(56.dp)) {
                    BadgedBox(badge = { CartBadge(state.bill.count) }) { Icon(Icons.Outlined.ReceiptLong, "Open running bill") }
                }
            })
        }) { padding ->
        NavHost(
            navController = navController,
            startDestination = "select-food",
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
            enterTransition = { androidx.compose.animation.slideInHorizontally(motion.defaultSpatialSpec()) { it / 3 } + fadeIn(motion.defaultEffectsSpec()) },
            exitTransition = { androidx.compose.animation.slideOutHorizontally(motion.defaultSpatialSpec()) { -it / 3 } + fadeOut(motion.defaultEffectsSpec()) },
            popEnterTransition = { androidx.compose.animation.slideInHorizontally(motion.defaultSpatialSpec()) { -it / 3 } + fadeIn(motion.defaultEffectsSpec()) },
            popExitTransition = { androidx.compose.animation.slideOutHorizontally(motion.defaultSpatialSpec()) { it / 3 } + fadeOut(motion.defaultEffectsSpec()) },
        ) {
            composable("select-food") { FoodSelectionScreen(state, viewModel::onIntent,
                { navController.navigate("running-bill") { launchSingleTop = true } },
                onActionBarHeightChanged = { actionBarHeightPx = it }) }
            composable("running-bill") { RunningBillScreen(state, viewModel::onIntent,
                { navController.navigate("bill-preview") { launchSingleTop = true } }, ::browse,
                onActionBarHeightChanged = { actionBarHeightPx = it }) }
            composable("bill-preview") { BillPreviewScreen(state, viewModel::onIntent, ::browse,
                onActionBarHeightChanged = { actionBarHeightPx = it }) }
        }
    }
    BillingItemEditor(state, viewModel::onIntent)
    BillingOutputSheet(state, onDismiss = { viewModel.onIntent(BillingIntent.DismissOutput) }, onNewBill = {
        viewModel.onIntent(BillingIntent.DismissOutput)
        browse()
    })
}
