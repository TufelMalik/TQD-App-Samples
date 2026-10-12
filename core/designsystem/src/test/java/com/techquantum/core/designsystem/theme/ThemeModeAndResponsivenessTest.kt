package com.techquantum.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.techquantum.core.designsystem.FashionTheme
import com.techquantum.core.designsystem.ShowcaseModule
import com.techquantum.core.designsystem.colorScheme
import com.techquantum.core.designsystem.utils.responsive.TQDWindowWidthSizeClass
import com.techquantum.core.designsystem.utils.responsive.calculateResponsiveConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeAndResponsivenessTest {

    @Test
    fun responsiveBreakpointsClassifyCorrectly() {
        // Compact (< 600dp)
        assertEquals(TQDWindowWidthSizeClass.COMPACT, TQDWindowWidthSizeClass.fromWidth(320.dp))
        assertEquals(TQDWindowWidthSizeClass.COMPACT, TQDWindowWidthSizeClass.fromWidth(411.dp))
        assertEquals(TQDWindowWidthSizeClass.COMPACT, TQDWindowWidthSizeClass.fromWidth(599.dp))

        // Medium (600dp .. 839dp)
        assertEquals(TQDWindowWidthSizeClass.MEDIUM, TQDWindowWidthSizeClass.fromWidth(600.dp))
        assertEquals(TQDWindowWidthSizeClass.MEDIUM, TQDWindowWidthSizeClass.fromWidth(720.dp))
        assertEquals(TQDWindowWidthSizeClass.MEDIUM, TQDWindowWidthSizeClass.fromWidth(839.dp))

        // Expanded (>= 840dp)
        assertEquals(TQDWindowWidthSizeClass.EXPANDED, TQDWindowWidthSizeClass.fromWidth(840.dp))
        assertEquals(TQDWindowWidthSizeClass.EXPANDED, TQDWindowWidthSizeClass.fromWidth(1024.dp))
        assertEquals(TQDWindowWidthSizeClass.EXPANDED, TQDWindowWidthSizeClass.fromWidth(1280.dp))
    }

    @Test
    fun responsiveConfigCalculatesAdaptiveColumnsAndSpacing() {
        val compactConfig = calculateResponsiveConfig(360.dp)
        assertEquals(TQDWindowWidthSizeClass.COMPACT, compactConfig.widthClass)
        assertEquals(2, compactConfig.gridColumns)
        assertEquals(TQDDimensions.spaceLarge, compactConfig.horizontalPadding)
        assertFalse(compactConfig.isTwoPane)

        val mediumConfig = calculateResponsiveConfig(700.dp)
        assertEquals(TQDWindowWidthSizeClass.MEDIUM, mediumConfig.widthClass)
        assertEquals(3, mediumConfig.gridColumns)
        assertEquals(TQDDimensions.space2XLarge, mediumConfig.horizontalPadding)
        assertTrue(mediumConfig.isTwoPane)

        val expandedConfig = calculateResponsiveConfig(1000.dp)
        assertEquals(TQDWindowWidthSizeClass.EXPANDED, expandedConfig.widthClass)
        assertEquals(4, expandedConfig.gridColumns)
        assertEquals(TQDDimensions.space4XLarge, expandedConfig.horizontalPadding)
        assertTrue(expandedConfig.isTwoPane)
    }

    @Test
    fun dimensionsSatisfyAccessibilityAndLuxuryStandards() {
        // Touch target min must be at least 48dp according to Material 3 / WCAG accessibility guidelines
        assertTrue("Min touch target must be at least 48dp", TQDDimensions.touchTargetMin >= 48.dp)
        assertTrue("Button standard height must be >= 48dp", TQDDimensions.buttonHeightStandard >= 48.dp)
        assertTrue("Input standard height must be >= 48dp", TQDDimensions.inputHeightStandard >= 48.dp)

        // Corner radii must be positive and follow visual hierarchy
        assertTrue(TQDDimensions.radiusCard > TQDDimensions.radiusSmall)
        assertTrue(TQDDimensions.radiusHero > TQDDimensions.radiusCard)
    }

    @Test
    fun bothThemeModesProvideDistinctSurfacesForAllThemes() {
        FashionTheme.entries.forEach { theme ->
            val lightScheme = theme.colorScheme(dark = false)
            val darkScheme = theme.colorScheme(dark = true)

            // Backgrounds must be completely distinct between light and dark
            assertNotEquals(
                "Light and Dark backgrounds must differ for ${theme.name}",
                lightScheme.background,
                darkScheme.background
            )

            // OnBackground must be legible against background
            assertNotEquals(
                "OnBackground must differ from background for ${theme.name}",
                lightScheme.background,
                lightScheme.onBackground
            )
            assertNotEquals(
                "OnBackground must differ from background in dark mode for ${theme.name}",
                darkScheme.background,
                darkScheme.onBackground
            )

            // Primary colors must not be transparent
            assertTrue(lightScheme.primary.alpha > 0.9f)
            assertTrue(darkScheme.primary.alpha > 0.9f)
        }
    }

    @Test
    fun showcaseModulesProvideRobustSchemesInBothModes() {
        listOf(
            ShowcaseModule.SHOP,
            ShowcaseModule.FOOD,
            ShowcaseModule.POS,
            ShowcaseModule.BILLING
        ).forEach { module ->
            val lightScheme = module.colorScheme(darkTheme = false)
            val darkScheme = module.colorScheme(darkTheme = true)

            assertNotEquals(
                "${module.name} should have distinct background between light and dark",
                lightScheme.background,
                darkScheme.background
            )
            assertNotEquals(
                "${module.name} should have distinct surface between light and dark",
                lightScheme.surface,
                darkScheme.surface
            )
        }
    }
}
