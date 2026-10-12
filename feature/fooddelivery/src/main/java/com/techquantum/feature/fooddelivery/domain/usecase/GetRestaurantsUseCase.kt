package com.techquantum.feature.fooddelivery.domain.usecase

import com.techquantum.feature.fooddelivery.domain.FoodRepository
import com.techquantum.feature.fooddelivery.domain.Restaurant
import javax.inject.Inject

/**
 * Single-responsibility use case to fetch restaurant partners.
 */
class GetRestaurantsUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(): List<Restaurant> = repository.restaurants()
}
