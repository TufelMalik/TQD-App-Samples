package com.techquantum.feature.fooddelivery.domain.usecase

import com.techquantum.feature.fooddelivery.domain.TrackingStage
import javax.inject.Inject

/**
 * Single-responsibility use case to calculate tracking transitions.
 */
class TrackFoodOrderUseCase @Inject constructor() {

    fun nextStage(current: TrackingStage): TrackingStage = when (current) {
        TrackingStage.PLACED -> TrackingStage.PREPARING
        TrackingStage.PREPARING -> TrackingStage.OUT_FOR_DELIVERY
        TrackingStage.OUT_FOR_DELIVERY -> TrackingStage.DELIVERED
        TrackingStage.DELIVERED -> TrackingStage.DELIVERED
    }
}
