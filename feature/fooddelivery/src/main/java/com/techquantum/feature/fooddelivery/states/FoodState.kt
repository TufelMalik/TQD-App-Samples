package com.techquantum.feature.fooddelivery.states

import com.techquantum.feature.fooddelivery.presentation.FoodEffect as PresentationFoodEffect
import com.techquantum.feature.fooddelivery.presentation.FoodIntent as PresentationFoodIntent
import com.techquantum.feature.fooddelivery.presentation.FoodState as PresentationFoodState

/**
 * Re-export Food delivery MVI states adhering to feature structure blueprint.
 */
typealias FoodState = PresentationFoodState
typealias FoodIntent = PresentationFoodIntent
typealias FoodEffect = PresentationFoodEffect
