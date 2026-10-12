package com.techquantum.feature.fooddelivery.domain.usecase

import com.techquantum.feature.fooddelivery.domain.FoodAddress
import com.techquantum.feature.fooddelivery.domain.FoodLine
import com.techquantum.feature.fooddelivery.domain.FoodOrder
import com.techquantum.feature.fooddelivery.domain.FoodPayment
import com.techquantum.feature.fooddelivery.domain.FoodRepository
import com.techquantum.feature.fooddelivery.domain.Restaurant
import javax.inject.Inject

/**
 * Single-responsibility use case to place a food order.
 */
class PlaceFoodOrderUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(
        restaurant: Restaurant,
        lines: List<FoodLine>,
        address: FoodAddress,
        payment: FoodPayment,
        instructions: String,
        tip: Int,
    ): FoodOrder {
        require(lines.isNotEmpty()) { "Cart cannot be empty" }
        require(lines.all { it.dish.restaurantId == restaurant.id }) { "All items must be from the same restaurant" }
        return repository.placeOrder(restaurant, lines, address, payment, instructions, tip)
    }
}
