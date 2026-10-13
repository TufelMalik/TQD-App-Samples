@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.techquantum.feature.pos.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.techquantum.core.designsystem.CompactModuleTitle
import com.techquantum.core.designsystem.PaletteAction
import com.techquantum.feature.pos.presentation.PosIntent
import com.techquantum.feature.pos.presentation.PosViewModel
import com.techquantum.feature.pos.ui.PosChargeScreen
import com.techquantum.feature.pos.ui.PosReceiptScreen
import com.techquantum.feature.pos.ui.PosWorkspaceScreen

@Composable
fun PosGraph(
    navController: NavHostController,
    onToggleDark: () -> Unit,
    modifier: Modifier = Modifier,
    onShowThemes: () -> Unit = {},
    viewModel: PosViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "workspace"
    val motion = MaterialTheme.motionScheme
    LaunchedEffect(state.receipt?.id, state.charging, route) {
        if (route == "charge" && state.receipt != null && !state.charging) {
            navController.navigate("receipt") { popUpTo("workspace"); launchSingleTop = true }
        }
    }
    Scaffold(modifier, contentWindowInsets = WindowInsets(0, 0, 0, 0), topBar = {
        CenterAlignedTopAppBar(title = { CompactModuleTitle(when (route) {
            "charge" -> "Complete payment"
            "receipt" -> "Payment complete"
            else -> "Counter service"
        }) }, navigationIcon = {
            if (route != "workspace") IconButton({ navController.popBackStack() }, Modifier.size(56.dp)) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back to counter")
            }
        }, actions = {
            PaletteAction(onShowThemes, 56.dp)
        })
    }) { padding ->
        NavHost(
            navController = navController,
            startDestination = "workspace",
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            enterTransition = { androidx.compose.animation.slideInHorizontally(motion.defaultSpatialSpec()) { it / 3 } + fadeIn(motion.defaultEffectsSpec()) },
            exitTransition = { androidx.compose.animation.slideOutHorizontally(motion.defaultSpatialSpec()) { -it / 3 } + fadeOut(motion.defaultEffectsSpec()) },
            popEnterTransition = { androidx.compose.animation.slideInHorizontally(motion.defaultSpatialSpec()) { -it / 3 } + fadeIn(motion.defaultEffectsSpec()) },
            popExitTransition = { androidx.compose.animation.slideOutHorizontally(motion.defaultSpatialSpec()) { it / 3 } + fadeOut(motion.defaultEffectsSpec()) },
        ) {
            composable("workspace") {
                PosWorkspaceScreen(state, viewModel::accept, onCharge = {
                    viewModel.accept(PosIntent.StartNewBill)
                    navController.navigate("charge") { launchSingleTop = true }
                })
            }
            composable("charge") {
                PosChargeScreen(state, viewModel::accept, onBack = { navController.popBackStack() })
            }
            composable("receipt") {
                PosReceiptScreen(state.receipt, onNewBill = {
                    viewModel.accept(PosIntent.StartNewBill)
                    navController.popBackStack("workspace", false)
                })
            }
        }
    }
}
