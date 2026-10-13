package com.techquantum.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

/** Canonical palette owner. Fashion screens consume semantic Material colors, never these anchors. */
enum class FashionTheme(
    val brandName: String,
    val displayName: String,
    val description: String,
    internal val palette: FashionPalette,
) {
    LUXURY_ETHNIC(
        brandName = "Aaranya",
        displayName = "Luxury Ethnic Couture",
        description = "Traditional, premium, festive",
        palette = FashionPalette(
            background = Color(0xFFFFFAF2), darkBackground = Color(0xFF1C1012),
            primary = Color(0xFF6B0D1B), secondary = Color(0xFFD81B60),
            tertiary = Color(0xFFC49A2E), outline = Color(0xFF6A6F3A),
            ink = Color(0xFF241B18),
        ),
    ),
    URBAN_THREADS(
        brandName = "Urban Threads",
        displayName = "Men's Wear — Urban Threads",
        description = "Minimal, masculine, modern",
        palette = FashionPalette(
            background = Color(0xFFC7CBD4), darkBackground = Color(0xFF0A0A0A),
            primary = Color(0xFF0F1D3A), secondary = Color(0xFF1E3A5F),
            tertiary = Color(0xFF475569), outline = Color(0xFF475569),
            ink = Color(0xFF0A0A0A),
        ),
    ),
    STREETWEAR(
        brandName = "Afterhours",
        displayName = "Streetwear Fashion",
        description = "Bold, urban, high contrast",
        palette = FashionPalette(
            background = Color(0xFFF5F5F5), darkBackground = Color(0xFF0D0D0D),
            primary = Color(0xFFD72638), secondary = Color(0xFF2563EB),
            tertiary = Color(0xFFF97316), outline = Color(0xFF2C2C2C),
            ink = Color(0xFF0D0D0D), darkSurface = Color(0xFF2C2C2C),
        ),
    ),
    ATHLEISURE(
        brandName = "Form & Pace",
        displayName = "Athleisure Wear",
        description = "Sporty, energetic",
        palette = FashionPalette(
            background = Color(0xFFFFFFFF), darkBackground = Color(0xFF0D0F12),
            primary = Color(0xFFC6FF00), secondary = Color(0xFF0066FF),
            tertiary = Color(0xFFA7A9AC), outline = Color(0xFFA7A9AC),
            ink = Color(0xFF0D0F12),
        ),
    ),
    STUDIO(
        brandName = "Studio",
        displayName = "Studio Tiles",
        description = "A fresh perspective on your everyday wardrobe",
        palette = FashionPalette(
            background = Color(0xFFF1ECFF), darkBackground = Color(0xFF171527),
            primary = Color(0xFF354AE8), secondary = Color(0xFF7863B1),
            tertiary = Color(0xFF4A716B), outline = Color(0xFF79738C), ink = Color(0xFF202033),
        ),
    ),
    LUXEVA(
        brandName = "Luxeva",
        displayName = "Contemporary Women's Wear — Luxeva",
        description = "Feminine, boutique luxury",
        palette = FashionPalette(
            background = Color(0xFFF6EFE6), darkBackground = Color(0xFF1D1D1F),
            primary = Color(0xFFB5566B), secondary = Color(0xFF7A1F2D),
            tertiary = Color(0xFF8A6D5A), outline = Color(0xFF8A6D5A),
            ink = Color(0xFF1D1D1F),
        ),
    );

    val lightColorScheme: ColorScheme by lazy { palette.toColorScheme(dark = false) }
    val darkColorScheme: ColorScheme by lazy { palette.toColorScheme(dark = true) }

    fun colorScheme(dark: Boolean): ColorScheme = if (dark) darkColorScheme else lightColorScheme
}

val LocalFashionTheme = staticCompositionLocalOf { FashionTheme.LUXEVA }

/** Exact client palette anchors plus supporting neutrals, kept out of composable implementation. */
internal data class FashionPalette(
    val background: Color,
    val darkBackground: Color,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val outline: Color,
    val ink: Color,
    val darkSurface: Color = darkBackground,
)

private val Paper = Color(0xFFFFFFFF)
private val Black = Color(0xFF000000)
private val ErrorRed = Color(0xFFB3261E)

/** Stable dietary indicator anchors. Legibility also relies on the indicator's label and shape. */
val FoodVegGreen = Color(0xFF0F8A3C)
val FoodNonVegBrown = Color(0xFFB53D35)

/** WCAG relative-luminance contrast. Choosing the stronger neutral guarantees >= 4.5:1. */
internal fun contrastRatio(first: Color, second: Color): Float {
    val firstLuminance = first.luminance()
    val secondLuminance = second.luminance()
    return (max(firstLuminance, secondLuminance) + 0.05f) /
        (min(firstLuminance, secondLuminance) + 0.05f)
}

private fun onColor(color: Color, preferred: Color? = null): Color = when {
    preferred != null && contrastRatio(color, preferred) >= 4.5f -> preferred
    contrastRatio(color, Black) >= contrastRatio(color, Paper) -> Black
    else -> Paper
}

/** Raise a dark accent's tone while retaining its hue; bright lime/silver anchors stay intact. */
private fun Color.forDarkSurface(): Color = if (luminance() < 0.35f) lerp(this, Paper, 0.58f) else this

/** Input boundaries need 3:1 against their surface; safely bounded to prevent runaway execution. */
private fun Color.asOutlineAgainst(surface: Color): Color {
    if (contrastRatio(this, surface) >= 3f) return this
    val contrastTarget = onColor(surface)
    return (1..20).asSequence()
        .map { fraction -> lerp(this, contrastTarget, (fraction * 0.05f).coerceAtMost(1f)) }
        .firstOrNull { contrastRatio(it, surface) >= 3f } ?: contrastTarget
}

internal fun FashionPalette.toColorScheme(dark: Boolean): ColorScheme {
    val base = if (dark) darkBackground else background
    val surface = if (dark) darkSurface else background
    val rawP = if (dark) primary.forDarkSurface() else primary
    val p = if (!dark && contrastRatio(rawP, surface) < 3.2f) lerp(rawP, Black, 0.65f) else rawP
    val s = if (dark) secondary.forDarkSurface() else secondary
    val t = if (dark) tertiary.forDarkSurface() else tertiary
    val containerMix = if (dark) 0.17f else 0.12f
    val primaryContainer = lerp(surface, p, containerMix)
    val secondaryContainer = lerp(surface, s, containerMix)
    val tertiaryContainer = lerp(surface, t, containerMix)
    val surfaceContainerLowest = if (dark) base else lerp(base, Paper, 0.45f)
    val surfaceContainerLow = lerp(surface, if (dark) Paper else primary, 0.025f)
    val surfaceContainer = lerp(surface, if (dark) Paper else primary, 0.045f)
    val surfaceContainerHigh = lerp(surface, if (dark) Paper else primary, 0.07f)
    val surfaceContainerHighest = lerp(surface, if (dark) Paper else primary, 0.10f)
    val contentColor = onColor(surfaceContainerHighest, if (dark) Paper else ink)
    val surfaceVariant = lerp(surface, if (dark) Paper else tertiary, 0.12f)
    val inverseSurface = if (dark) background else darkBackground
    val error = if (dark) ErrorRed.forDarkSurface() else ErrorRed
    val errorContainer = lerp(surface, error, if (dark) 0.17f else 0.10f)
    val fixedPrimary = lerp(primary, Paper, 0.75f)
    val fixedSecondary = lerp(secondary, Paper, 0.75f)
    val fixedTertiary = lerp(tertiary, Paper, 0.75f)
    val fixedPrimaryDim = lerp(primary, Paper, 0.58f)
    val fixedSecondaryDim = lerp(secondary, Paper, 0.58f)
    val fixedTertiaryDim = lerp(tertiary, Paper, 0.58f)
    val scheme = if (dark) darkColorScheme() else lightColorScheme()
    return scheme.copy(
        primary = p, onPrimary = onColor(p),
        primaryContainer = primaryContainer, onPrimaryContainer = onColor(primaryContainer),
        inversePrimary = if (dark) primary else primary.forDarkSurface(),
        secondary = s, onSecondary = onColor(s),
        secondaryContainer = secondaryContainer, onSecondaryContainer = onColor(secondaryContainer),
        tertiary = t, onTertiary = onColor(t),
        tertiaryContainer = tertiaryContainer, onTertiaryContainer = onColor(tertiaryContainer),
        background = base, onBackground = onColor(base, if (dark) Paper else ink),
        surface = surface, onSurface = contentColor,
        surfaceVariant = surfaceVariant, onSurfaceVariant = onColor(surfaceVariant, contentColor),
        surfaceTint = p,
        inverseSurface = inverseSurface, inverseOnSurface = onColor(inverseSurface),
        error = error, onError = onColor(error),
        errorContainer = errorContainer, onErrorContainer = onColor(errorContainer),
        outline = (if (dark) outline.forDarkSurface() else outline).asOutlineAgainst(surface),
        outlineVariant = lerp(surface, contentColor, if (dark) 0.25f else 0.20f),
        scrim = Black,
        surfaceBright = lerp(surface, Paper, if (dark) 0.18f else 0.28f),
        surfaceDim = lerp(surface, Black, if (dark) 0.18f else 0.055f),
        surfaceContainerLowest = surfaceContainerLowest,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHighest,
        primaryFixed = fixedPrimary, primaryFixedDim = fixedPrimaryDim,
        onPrimaryFixed = onColor(fixedPrimaryDim), onPrimaryFixedVariant = onColor(fixedPrimaryDim),
        secondaryFixed = fixedSecondary, secondaryFixedDim = fixedSecondaryDim,
        onSecondaryFixed = onColor(fixedSecondaryDim), onSecondaryFixedVariant = onColor(fixedSecondaryDim),
        tertiaryFixed = fixedTertiary, tertiaryFixedDim = fixedTertiaryDim,
        onTertiaryFixed = onColor(fixedTertiaryDim), onTertiaryFixedVariant = onColor(fixedTertiaryDim),
    )
}
