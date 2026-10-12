@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.techquantum.feature.billing.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.techquantum.core.designsystem.CartBadge
import com.techquantum.feature.billing.presentation.BillingIntent
import com.techquantum.feature.billing.presentation.BillingViewModel
import com.techquantum.feature.billing.ui.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BillingGraph(
    navController: NavHostController,
    onToggleDark: () -> Unit,
    modifier: Modifier = Modifier,
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
            TopAppBar(title = { Column {
                Text("Kitchen bill", style = MaterialTheme.typography.titleLarge)
                Text("TechQuantum · Counter demo", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } }, navigationIcon = {
                if (route != "select-food") IconButton({ navController.popBackStack() }, Modifier.size(56.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back")
                }
            }, actions = {
                IconButton(onToggleDark, Modifier.size(56.dp)) { Icon(Icons.Outlined.DarkMode, "Toggle light and dark mode") }
                IconButton({ navController.navigate("running-bill") { launchSingleTop = true } }, Modifier.size(56.dp)) {
                    BadgedBox(badge = { CartBadge(state.bill.count) }) { Icon(Icons.Outlined.ReceiptLong, "Open running bill") }
                }
            })
        }) { padding ->
        NavHost(navController, startDestination = "select-food", modifier = Modifier.fillMaxSize().padding(padding),
            enterTransition = { fadeIn(motion.fastEffectsSpec()) }, exitTransition = { fadeOut(motion.fastEffectsSpec()) },
            popEnterTransition = { fadeIn(motion.fastEffectsSpec()) }, popExitTransition = { fadeOut(motion.fastEffectsSpec()) }) {
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
