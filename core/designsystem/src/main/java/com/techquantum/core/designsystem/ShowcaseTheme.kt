@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.techquantum.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class ShowcaseModule { SHOP, FOOD, POS, BILLING }

// Palette anchors are non-composable tokens. Screens and shell controls consume semantic roles.
private val FoodPalette = FashionPalette(
    background = Color(0xFFFFF8F0), darkBackground = Color(0xFF1F1A17),
    primary = Color(0xFFE6472E), secondary = Color(0xFFF6A823),
    tertiary = Color(0xFF3C8A3C), outline = Color(0xFF8C7062), ink = Color(0xFF1F1A17),
)
private val PosPalette = FashionPalette(
    background = Color(0xFFF8FAFC), darkBackground = Color(0xFF12191D),
    primary = Color(0xFF006A67), secondary = Color(0xFF2563EB),
    tertiary = Color(0xFF8B681E), outline = Color(0xFF475569), ink = Color(0xFF182126),
)
private val BillingPalette = FashionPalette(
    background = Color(0xFFF6FBF2), darkBackground = Color(0xFF111A12),
    primary = Color(0xFF2E6932), secondary = Color(0xFF285F59),
    tertiary = Color(0xFF88690A), outline = Color(0xFF536653), ink = Color(0xFF18231A),
)
private val ModuleSchemes: Map<Pair<ShowcaseModule, Boolean>, ColorScheme> =
    listOf(ShowcaseModule.FOOD to FoodPalette, ShowcaseModule.POS to PosPalette, ShowcaseModule.BILLING to BillingPalette)
        .flatMap { (module, palette) -> listOf(false, true).map { dark -> (module to dark) to palette.toColorScheme(dark) } }
        .toMap()

fun ShowcaseModule.colorScheme(darkTheme: Boolean): ColorScheme =
    ModuleSchemes[this to darkTheme] ?: FashionTheme.LUXEVA.colorScheme(darkTheme)

/** Exactly one root expressive provider is shared by all four module graphs. */
@Composable
fun ShowcaseAppTheme(
    module: ShowcaseModule,
    fashionTheme: FashionTheme,
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalFashionTheme provides fashionTheme) {
        MaterialExpressiveTheme(
            colorScheme = if (module == ShowcaseModule.SHOP) fashionTheme.colorScheme(darkTheme) else module.colorScheme(darkTheme),
            motionScheme = MotionScheme.expressive(),
            typography = FashionTypography,
            shapes = Shapes(extraSmall = RoundedCornerShape(6.dp), small = RoundedCornerShape(12.dp),
                medium = RoundedCornerShape(18.dp), large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(32.dp)),
            content = content,
        )
    }
}

@Composable
fun ShowcaseTheme(module: ShowcaseModule, darkTheme: Boolean, content: @Composable () -> Unit) {
    ShowcaseAppTheme(module, FashionTheme.LUXEVA, darkTheme, content)
}
