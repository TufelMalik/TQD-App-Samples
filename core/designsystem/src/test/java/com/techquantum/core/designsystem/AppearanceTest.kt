package com.techquantum.core.designsystem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AppearanceTest {
    @Test
    fun modeMigrationAndUnknownIdsRecoverWithoutOverridingSystemChoice() {
        assertEquals(AppearanceMode.SYSTEM, AppearanceMode.restore(null, null))
        assertEquals(AppearanceMode.DARK, AppearanceMode.restore(null, true))
        assertEquals(AppearanceMode.LIGHT, AppearanceMode.restore(null, false))
        assertEquals(AppearanceMode.SYSTEM, AppearanceMode.restore("SYSTEM", true))
        assertEquals(AppearanceMode.SYSTEM, AppearanceMode.restore("unknown", null))
        assertTrue(AppearanceMode.SYSTEM.isDark(true))
        assertFalse(AppearanceMode.SYSTEM.isDark(false))
        assertFalse(AppearanceMode.LIGHT.isDark(true))
        assertTrue(AppearanceMode.DARK.isDark(false))
        assertNull(restorePalette("unknown"))
    }

    @Test
    fun paletteOverrideIsExplicitAndDefaultsStayIndependent() {
        listOf(false, true).forEach { dark ->
            ShowcaseModule.entries.forEach { module ->
                FashionTheme.entries.forEach { theme ->
                    val default = resolveShowcaseColorScheme(module, theme, dark)
                    assertSame(
                        if (module == ShowcaseModule.SHOP) theme.colorScheme(dark) else module.colorScheme(
                            dark
                        ), default
                    )
                    assertSame(
                        theme.colorScheme(dark),
                        resolveShowcaseColorScheme(module, FashionTheme.LUXEVA, dark, theme)
                    )
                }
            }
        }
    }

    @Test
    fun actualNavigationAndDietaryRolesRemainReadableForEveryPalette() {
        val schemes = listOf(false, true).flatMap { dark ->
            FashionTheme.entries.map { it.colorScheme(dark) } + ShowcaseModule.entries.map {
                it.colorScheme(
                    dark
                )
            }
        }
        schemes.forEach { scheme ->
            assertTrue(contrastRatio(scheme.onSurface, scheme.surfaceContainer) >= 4.5f)
            assertTrue(contrastRatio(scheme.onSurfaceVariant, scheme.surfaceContainer) >= 4.5f)
            assertTrue(contrastRatio(scheme.onPrimaryContainer, scheme.primaryContainer) >= 4.5f)
            listOf(false, true).forEach { veg ->
                val dietary = dietaryColorScheme(scheme, veg)
                assertTrue(contrastRatio(dietary.primary, dietary.surface) >= 3f)
                assertTrue(contrastRatio(dietary.onSurfaceVariant, dietary.surface) >= 4.5f)
            }
        }
    }
}
