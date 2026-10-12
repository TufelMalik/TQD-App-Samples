package com.techquantum.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FashionThemeContrastTest {
    @Test
    fun everyThemeHasAaContrastForAllContentPairsInBothModes() {
        FashionTheme.entries.forEach { theme ->
            listOf(false, true).forEach { dark ->
                val mode = if (dark) "dark" else "light"
                theme.colorScheme(dark).contentPairs().forEach { (role, pair) ->
                    val ratio = contrastRatio(pair.first, pair.second)
                    assertTrue("${theme.name} $mode $role contrast is $ratio, must be at least 4.5:1", ratio >= 4.5f)
                }
            }
        }
    }

    @Test
    fun lightSchemesRetainAllRequestedBrandAnchors() {
        val expected = mapOf(
            FashionTheme.LUXURY_ETHNIC to listOf(0xFFFFFAF2, 0xFF6B0D1B, 0xFFD81B60, 0xFFC49A2E, 0xFF6A6F3A),
            FashionTheme.URBAN_THREADS to listOf(0xFFC7CBD4, 0xFF0F1D3A, 0xFF1E3A5F, 0xFF475569, 0xFF0A0A0A),
            FashionTheme.STREETWEAR to listOf(0xFFF5F5F5, 0xFFD72638, 0xFF2563EB, 0xFFF97316, 0xFF2C2C2C, 0xFF0D0D0D),
            FashionTheme.ATHLEISURE to listOf(0xFFFFFFFF, 0xFFC6FF00, 0xFF0066FF, 0xFFA7A9AC, 0xFF0D0F12),
            FashionTheme.LUXEVA to listOf(0xFFF6EFE6, 0xFFB5566B, 0xFF7A1F2D, 0xFF8A6D5A, 0xFF1D1D1F),
        )
        expected.forEach { (theme, anchors) ->
            val palette = theme.palette
            val actual = setOf(palette.background, palette.primary, palette.secondary, palette.tertiary,
                palette.outline, palette.ink, palette.darkBackground, palette.darkSurface)
            anchors.forEach { anchor -> assertTrue("${theme.name} missing ${anchor.toString(16)}", Color(anchor) in actual) }
            assertEquals(theme.palette.background, theme.lightColorScheme.background)
            assertEquals(theme.palette.darkBackground, theme.darkColorScheme.background)
        }
    }

    @Test
    fun schemeSwitchDoesNotChangeThemeIdentityAndReturnsCachedScheme() {
        FashionTheme.entries.forEach { theme ->
            assertTrue(theme.colorScheme(false) === theme.lightColorScheme)
            assertTrue(theme.colorScheme(true) === theme.darkColorScheme)
            assertTrue(theme.brandName.isNotBlank())
        }
        assertEquals(5, FashionTheme.entries.size)
    }

    @Test
    fun inputOutlinesRemainVisibleOnEveryThemeSurface() {
        FashionTheme.entries.forEach { theme ->
            listOf(false, true).forEach { dark ->
                val scheme = theme.colorScheme(dark)
                assertTrue("${theme.name} outline needs 3:1", contrastRatio(scheme.outline, scheme.surface) >= 3f)
            }
        }
    }

    @Test
    fun otherModulesHaveIndependentAaContentPairsInBothModes() {
        listOf(ShowcaseModule.FOOD, ShowcaseModule.POS, ShowcaseModule.BILLING).forEach { module ->
            listOf(false, true).forEach { dark ->
                val scheme = module.colorScheme(dark)
                scheme.contentPairs().forEach { (role, pair) ->
                    val ratio = contrastRatio(pair.first, pair.second)
                    assertTrue("${module.name} $dark $role contrast is $ratio", ratio >= 4.5f)
                }
                assertTrue("${module.name} outline needs 3:1", contrastRatio(scheme.outline, scheme.surface) >= 3f)
            }
        }
    }

    @Test
    fun foodKeepsItsRequestedPaletteAndFashionSwitchesCannotOverrideIt() {
        val food = ShowcaseModule.FOOD.colorScheme(false)
        assertEquals(Color(0xFFFFF8F0), food.background)
        assertEquals(Color(0xFFE6472E), food.primary)
        assertEquals(Color(0xFFF6A823), food.secondary)
        assertEquals(Color(0xFF3C8A3C), food.tertiary)
        assertEquals(Color(0xFF1F1A17), food.onSurface)
        FashionTheme.entries.forEach { theme ->
            theme.colorScheme(false)
            theme.colorScheme(true)
            assertTrue(food === ShowcaseModule.FOOD.colorScheme(false))
        }
    }

    private fun ColorScheme.contentPairs(): Map<String, Pair<Color, Color>> = mapOf(
        "primary/onPrimary" to (primary to onPrimary),
        "secondary/onSecondary" to (secondary to onSecondary),
        "tertiary/onTertiary" to (tertiary to onTertiary),
        "primaryContainer/onPrimaryContainer" to (primaryContainer to onPrimaryContainer),
        "secondaryContainer/onSecondaryContainer" to (secondaryContainer to onSecondaryContainer),
        "tertiaryContainer/onTertiaryContainer" to (tertiaryContainer to onTertiaryContainer),
        "background/onBackground" to (background to onBackground),
        "surface/onSurface" to (surface to onSurface),
        "surfaceVariant/onSurfaceVariant" to (surfaceVariant to onSurfaceVariant),
        "surfaceContainerLowest/onSurface" to (surfaceContainerLowest to onSurface),
        "surfaceContainerLow/onSurface" to (surfaceContainerLow to onSurface),
        "surfaceContainer/onSurface" to (surfaceContainer to onSurface),
        "surfaceContainerHigh/onSurface" to (surfaceContainerHigh to onSurface),
        "surfaceContainerHighest/onSurface" to (surfaceContainerHighest to onSurface),
        "surfaceBright/onSurface" to (surfaceBright to onSurface),
        "surfaceDim/onSurface" to (surfaceDim to onSurface),
        "inverseSurface/inverseOnSurface" to (inverseSurface to inverseOnSurface),
        "error/onError" to (error to onError),
        "errorContainer/onErrorContainer" to (errorContainer to onErrorContainer),
        "primaryFixed/onPrimaryFixed" to (primaryFixed to onPrimaryFixed),
        "primaryFixedDim/onPrimaryFixed" to (primaryFixedDim to onPrimaryFixed),
        "secondaryFixed/onSecondaryFixed" to (secondaryFixed to onSecondaryFixed),
        "secondaryFixedDim/onSecondaryFixed" to (secondaryFixedDim to onSecondaryFixed),
        "tertiaryFixed/onTertiaryFixed" to (tertiaryFixed to onTertiaryFixed),
        "tertiaryFixedDim/onTertiaryFixed" to (tertiaryFixedDim to onTertiaryFixed),
    )
}
