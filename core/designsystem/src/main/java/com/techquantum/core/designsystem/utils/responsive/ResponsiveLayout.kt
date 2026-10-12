package com.techquantum.core.designsystem.utils.responsive

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.techquantum.core.designsystem.theme.TQDDimensions

/**
 * Window width size classes according to Material 3 responsive layout guidelines.
 */
enum class TQDWindowWidthSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED;

    companion object {
        fun fromWidth(widthDp: Dp): TQDWindowWidthSizeClass = when {
            widthDp < 600.dp -> COMPACT
            widthDp < 840.dp -> MEDIUM
            else -> EXPANDED
        }

        fun fromWidthInt(widthDp: Int): TQDWindowWidthSizeClass = when {
            widthDp < 600 -> COMPACT
            widthDp < 840 -> MEDIUM
            else -> EXPANDED
        }
    }
}

/**
 * Holds responsive design decisions based on screen width.
 */
data class ResponsiveWindowConfig(
    val widthClass: TQDWindowWidthSizeClass,
    val gridColumns: Int,
    val horizontalPadding: Dp,
    val cardSpacing: Dp,
    val isTwoPane: Boolean,
)

/**
 * Calculate responsive window config based on width in Dp.
 */
fun calculateResponsiveConfig(widthDp: Dp): ResponsiveWindowConfig {
    val sizeClass = TQDWindowWidthSizeClass.fromWidth(widthDp)
    return when (sizeClass) {
        TQDWindowWidthSizeClass.COMPACT -> ResponsiveWindowConfig(
            widthClass = TQDWindowWidthSizeClass.COMPACT,
            gridColumns = 2,
            horizontalPadding = TQDDimensions.spaceLarge, // 16.dp
            cardSpacing = TQDDimensions.spaceMediumSmall, // 10.dp
            isTwoPane = false,
        )
        TQDWindowWidthSizeClass.MEDIUM -> ResponsiveWindowConfig(
            widthClass = TQDWindowWidthSizeClass.MEDIUM,
            gridColumns = 3,
            horizontalPadding = TQDDimensions.space2XLarge, // 24.dp
            cardSpacing = TQDDimensions.spaceLarge, // 16.dp
            isTwoPane = true,
        )
        TQDWindowWidthSizeClass.EXPANDED -> ResponsiveWindowConfig(
            widthClass = TQDWindowWidthSizeClass.EXPANDED,
            gridColumns = 4,
            horizontalPadding = TQDDimensions.space4XLarge, // 32.dp
            cardSpacing = TQDDimensions.spaceExtraLarge, // 20.dp
            isTwoPane = true,
        )
    }
}

/**
 * Composable helper to remember current screen responsive config.
 */
@Composable
fun rememberResponsiveWindowConfig(): ResponsiveWindowConfig {
    val configuration = LocalConfiguration.current
    return calculateResponsiveConfig(configuration.screenWidthDp.dp)
}
