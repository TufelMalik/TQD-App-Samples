package com.techquantum.feature.fooddelivery.data

import com.techquantum.feature.fooddelivery.domain.FoodAddress
import com.techquantum.feature.fooddelivery.domain.FoodFees
import com.techquantum.feature.fooddelivery.domain.FoodLine
import com.techquantum.feature.fooddelivery.domain.FoodOrder
import com.techquantum.feature.fooddelivery.domain.FoodPayment
import com.techquantum.feature.fooddelivery.domain.FoodRepository
import com.techquantum.feature.fooddelivery.domain.Restaurant
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalFoodRepository @Inject constructor() : FoodRepository {
    private val sequence = AtomicInteger(2400)
    private val localOrders = mutableListOf<FoodOrder>()
    override suspend fun restaurants(): List<Restaurant> {
        delay(450)
        return FoodCatalog.restaurants
    }
    override suspend fun placeOrder(restaurant: Restaurant, lines: List<FoodLine>, address: FoodAddress, payment: FoodPayment, instructions: String, tip: Int): FoodOrder {
        require(lines.isNotEmpty())
        require(lines.all { it.dish.restaurantId == restaurant.id && it.quantity in 1..99 })
        val snapshot = lines.map { it.copy(dish = it.dish.copy(addOns = it.dish.addOns.toList()), addOns = it.addOns.toList()) }
        delay(650)
        return FoodOrder("TQ-${sequence.incrementAndGet()}", restaurant, snapshot, address.copy(), payment,
            instructions.trim(), FoodFees.bill(snapshot, tip)).also { localOrders += it }
    }
}
