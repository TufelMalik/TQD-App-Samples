@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.techquantum.tqdappsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.core.content.edit
import androidx.core.view.WindowInsetsControllerCompat
import com.techquantum.core.designsystem.*
import com.techquantum.feature.ecommerce.navigation.EcommerceGraph
import com.techquantum.feature.fooddelivery.navigation.FoodDeliveryGraph
import com.techquantum.feature.pos.navigation.PosGraph
import com.techquantum.feature.billing.navigation.BillingGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferences = getSharedPreferences("fashion_showcase", MODE_PRIVATE)
        setContent {
            val systemDark = isSystemInDarkTheme()
            var themeName by rememberSaveable { mutableStateOf(preferences.getString("theme", FashionTheme.LUXEVA.name)!!) }
            var darkTheme by rememberSaveable { mutableStateOf(preferences.getBoolean("dark", systemDark)) }
            SideEffect {
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            val theme = FashionTheme.entries.firstOrNull { it.name == themeName } ?: FashionTheme.LUXEVA
            ShowcaseShell(theme, darkTheme,
                onTheme = { themeName = it.name; preferences.edit { putString("theme", it.name) } },
                onDark = { darkTheme = it; preferences.edit { putBoolean("dark", it) } })
        }
    }
}

private enum class ModuleTab(val label: String, val icon: ImageVector) {
    SHOP("Shop", Icons.Outlined.ShoppingBag), FOOD("Food", Icons.Outlined.Restaurant),
    POS("POS", Icons.Outlined.PointOfSale), BILLING("Billing", Icons.AutoMirrored.Outlined.ReceiptLong),
}

@Composable
private fun ShowcaseShell(theme: FashionTheme, darkTheme: Boolean, onTheme: (FashionTheme) -> Unit, onDark: (Boolean) -> Unit) {
    var selectedTab by rememberSaveable { mutableStateOf(ModuleTab.SHOP) }
    var showThemes by rememberSaveable { mutableStateOf(false) }
    val shopNav = rememberNavController()
    val foodNav = rememberNavController()
    val posNav = rememberNavController()
    val billingNav = rememberNavController()
    val shell: @Composable () -> Unit = {
        SharedTransitionLayout {
            Scaffold(bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
                    tonalElevation = 6.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    NavigationBar(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        ModuleTab.entries.forEach { tab ->
                            val scale by animateFloatAsState(if (tab == selectedTab) 1.14f else 1f,
                                MaterialTheme.motionScheme.fastSpatialSpec(), label = "Tab selection")
                            NavigationBarItem(selected = tab == selectedTab, onClick = { selectedTab = tab },
                                icon = { Icon(tab.icon, tab.label, Modifier.graphicsLayer { scaleX = scale; scaleY = scale }) },
                                label = { Text(tab.label) }, colors = NavigationBarItemDefaults.colors(
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                    }
                }
            }) { padding ->
                val contentModifier = Modifier.fillMaxSize().padding(padding)
                when (selectedTab) {
                    ModuleTab.SHOP -> EcommerceGraph(shopNav, this@SharedTransitionLayout, { showThemes = true }, contentModifier)
                    ModuleTab.FOOD -> FoodDeliveryGraph(foodNav, this@SharedTransitionLayout, { onDark(!darkTheme) }, contentModifier)
                    ModuleTab.POS -> PosGraph(posNav, { onDark(!darkTheme) }, contentModifier)
                    ModuleTab.BILLING -> BillingGraph(billingNav, { onDark(!darkTheme) }, contentModifier)
                }
            }
        }
        if (showThemes && selectedTab == ModuleTab.SHOP) FashionThemePicker(theme, darkTheme, onTheme, onDark, { showThemes = false })
    }
    ShowcaseAppTheme(when (selectedTab) {
        ModuleTab.SHOP -> ShowcaseModule.SHOP
        ModuleTab.FOOD -> ShowcaseModule.FOOD
        ModuleTab.POS -> ShowcaseModule.POS
        ModuleTab.BILLING -> ShowcaseModule.BILLING
    }, theme, darkTheme, shell)
}
