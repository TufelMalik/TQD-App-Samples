package com.techquantum.core.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized design system dimension tokens for the application.
 * Adheres to standard 4dp/8dp grid with luxury squircle radii.
 */
object TQDDimensions {
    // Spacing & Padding
    val spaceNone: Dp = 0.dp
    val spaceMicro: Dp = 2.dp
    val spaceTiny: Dp = 4.dp
    val spaceExtraSmall: Dp = 6.dp
    val spaceSmall: Dp = 8.dp
    val spaceMediumSmall: Dp = 10.dp
    val spaceMedium: Dp = 12.dp
    val spaceRegular: Dp = 14.dp
    val spaceLarge: Dp = 16.dp
    val spaceExtraLarge: Dp = 20.dp
    val space2XLarge: Dp = 24.dp
    val space3XLarge: Dp = 28.dp
    val space4XLarge: Dp = 32.dp

    // Corner Radii
    val radiusPill: Dp = 999.dp
    val radiusMicro: Dp = 4.dp
    val radiusSmall: Dp = 8.dp
    val radiusMedium: Dp = 14.dp
    val radiusRegular: Dp = 18.dp
    val radiusCard: Dp = 22.dp
    val radiusCardLarge: Dp = 24.dp
    val radiusHero: Dp = 28.dp
    val radiusEditorial: Dp = 32.dp

    // Elevation & Borders
    val borderHairline: Dp = 0.5.dp
    val borderThin: Dp = 1.dp
    val borderMedium: Dp = 1.5.dp
    val borderThick: Dp = 2.dp
    val elevationSubtle: Dp = 2.dp
    val elevationFloat: Dp = 6.dp
    val elevationModal: Dp = 12.dp

    // Min touch targets & Interactive Heights
    val touchTargetMin: Dp = 48.dp
    val operatorTouchTargetMin: Dp = 56.dp
    val receiptMaxWidth: Dp = 560.dp
    val formMaxWidth: Dp = 720.dp
    val buttonHeightStandard: Dp = 52.dp
    val buttonHeightLarge: Dp = 56.dp
    val buttonHeightCharge: Dp = 60.dp
    val inputHeightStandard: Dp = 56.dp

    // Responsive Breakpoints
    val breakpointCompactMax: Dp = 599.dp
    val breakpointMediumMax: Dp = 839.dp
    val breakpointExpandedMin: Dp = 840.dp
}
