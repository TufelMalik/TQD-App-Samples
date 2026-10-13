@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.techquantum.tqdappsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.core.content.edit
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.techquantum.core.designsystem.AppearanceMode
import com.techquantum.core.designsystem.FashionTheme
import com.techquantum.core.designsystem.LocalPaletteVisible
import com.techquantum.core.designsystem.LocalShowPalette
import com.techquantum.core.designsystem.ShowcaseAppTheme
import com.techquantum.core.designsystem.ShowcaseModule
import com.techquantum.core.designsystem.ShowcasePalettePicker
import com.techquantum.core.designsystem.rememberReducedMotion
import com.techquantum.core.designsystem.restorePalette
import com.techquantum.feature.billing.navigation.BillingGraph
import com.techquantum.feature.ecommerce.navigation.EcommerceGraph
import com.techquantum.feature.fooddelivery.navigation.FoodDeliveryGraph
import com.techquantum.feature.pos.navigation.PosGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferences = getSharedPreferences("fashion_showcase", MODE_PRIVATE)
        if (preferences.getInt("selected_design_revision", 0) < 2) {
            preferences.edit {
                putString("palette_SHOP", FashionTheme.STUDIO.name)
                listOf("FOOD", "POS", "BILLING").forEach { putString("palette_$it", "DEFAULT") }
                putInt("selected_design_revision", 2)
            }
        }
        setContent {
            val systemDark = isSystemInDarkTheme()
            var appearance by rememberSaveable { mutableStateOf(AppearanceMode.restore(preferences.getString("appearance", null),
                if (preferences.contains("dark")) preferences.getBoolean("dark", false) else null)) }
            var palettes by rememberSaveable { mutableStateOf<List<String>>(ShowcaseModule.entries.map { module ->
                preferences.getString("palette_${module.name}",
                    if (module == ShowcaseModule.SHOP) preferences.getString("theme", FashionTheme.LUXEVA.name) else "DEFAULT") ?: "DEFAULT"
            }) }
            var reduceMotion by rememberSaveable { mutableStateOf(preferences.getBoolean("reduce_motion", false)) }
            val dark = appearance.isDark(systemDark)
            val reduced = rememberReducedMotion(reduceMotion)
            ShowcaseShell(palettes, dark, appearance, reduceMotion, reduced,
                onPalette = { module, palette ->
                    palettes = palettes.mapIndexed { index, value -> if (index == module.ordinal) palette?.name ?: "DEFAULT" else value }
                    preferences.edit { putString("palette_${module.name}", palette?.name ?: "DEFAULT") }
                }, onAppearance = { appearance = it; preferences.edit { putString("appearance", it.name) } },
                onReduced = { reduceMotion = it; preferences.edit { putBoolean("reduce_motion", it) } },
                onSystemBars = {
                    WindowInsetsControllerCompat(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !dark
                        isAppearanceLightNavigationBars = !dark
                    }
                })
        }
    }
}

private enum class ModuleTab(val label: String, val icon: ImageVector) {
    SHOP("Shop", Icons.Outlined.ShoppingBag), FOOD("Food", Icons.Outlined.Restaurant),
    POS("POS", Icons.Outlined.PointOfSale), BILLING("Billing", Icons.AutoMirrored.Outlined.ReceiptLong);
    val module get() = ShowcaseModule.valueOf(name)
}

@Composable
private fun ShowcaseShell(palettes: List<String>, dark: Boolean, appearance: AppearanceMode,
    requestedReduced: Boolean, reduced: Boolean, onPalette: (ShowcaseModule, FashionTheme?) -> Unit,
    onAppearance: (AppearanceMode) -> Unit, onReduced: (Boolean) -> Unit, onSystemBars: () -> Unit) {
    var selectedTab by rememberSaveable { mutableStateOf(ModuleTab.SHOP) }
    var showThemes by rememberSaveable { mutableStateOf(false) }
    val shopNav = rememberNavController()
    val foodNav = rememberNavController()
    val posNav = rememberNavController()
    val billingNav = rememberNavController()
    val shopTheme = restorePalette(palettes[ShowcaseModule.SHOP.ordinal]) ?: FashionTheme.LUXEVA
    val selectedPalette = restorePalette(palettes[selectedTab.module.ordinal])
    SideEffect { onSystemBars() }
    // Reapply after Material disposes a modal window and restores its appearance flags.
    LaunchedEffect(showThemes, dark) { if (!showThemes) { withFrameNanos { }; onSystemBars() } }
    val openPalette = { showThemes = true }
    ShowcaseAppTheme(selectedTab.module, shopTheme, dark, selectedPalette, reduced) {
        CompositionLocalProvider(LocalShowPalette provides openPalette, LocalPaletteVisible provides showThemes) {
            SharedTransitionLayout {
                val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
                Scaffold(modifier = Modifier.imePadding(), contentWindowInsets = WindowInsets(0, 0, 0, 0), bottomBar = {
                    if (!keyboardVisible) NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                        ModuleTab.entries.forEach { tab ->
                            val scale by animateFloatAsState(if (!reduced && tab == selectedTab) 1.08f else 1f,
                                MaterialTheme.motionScheme.fastSpatialSpec(), label = "Tab selection")
                            NavigationBarItem(selectedTab == tab, { selectedTab = tab },
                                icon = { Icon(tab.icon, null, Modifier.graphicsLayer { scaleX = scale; scaleY = scale }) },
                                label = { Text(tab.label) }, colors = NavigationBarItemDefaults.colors(
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                    }
                }) { padding ->
                    val viewport = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    AnimatedContent(selectedTab, modifier = Modifier.fillMaxSize(), label = "Module change",
                        transitionSpec = { fadeIn(tween(if (reduced) 0 else 160)).togetherWith(fadeOut(tween(if (reduced) 0 else 100))) }) { tab ->
                        when (tab) {
                            ModuleTab.SHOP -> EcommerceGraph(shopNav, this@SharedTransitionLayout, openPalette, viewport)
                            ModuleTab.FOOD -> FoodDeliveryGraph(foodNav, this@SharedTransitionLayout, {}, viewport, onShowThemes = openPalette)
                            ModuleTab.POS -> PosGraph(posNav, {}, viewport, onShowThemes = openPalette)
                            ModuleTab.BILLING -> BillingGraph(billingNav, {}, viewport, onShowThemes = openPalette)
                        }
                    }
                }
            }
            if (showThemes) ShowcasePalettePicker(selectedTab.module,
                selectedPalette ?: shopTheme.takeIf { selectedTab == ModuleTab.SHOP }, appearance, dark, requestedReduced,
                { onPalette(selectedTab.module, it) }, onAppearance, onReduced, { showThemes = false })
        }
    }
}
