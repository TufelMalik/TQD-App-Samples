package com.techquantum.feature.fooddelivery.presentation

import com.techquantum.feature.fooddelivery.domain.Cuisine
import com.techquantum.feature.fooddelivery.domain.FoodAddress
import com.techquantum.feature.fooddelivery.domain.FoodFees
import com.techquantum.feature.fooddelivery.domain.FoodLine
import com.techquantum.feature.fooddelivery.domain.FoodOrder
import com.techquantum.feature.fooddelivery.domain.FoodPayment
import com.techquantum.feature.fooddelivery.domain.MenuCategory
import com.techquantum.feature.fooddelivery.domain.PendingDish
import com.techquantum.feature.fooddelivery.domain.Restaurant
import com.techquantum.feature.fooddelivery.domain.TrackingStage

data class FoodState(
    val restaurants: List<Restaurant> = emptyList(), val loading: Boolean = true,
    val deliveryArea: String = "Adajan, Surat",
    val query: String = "", val cuisine: Cuisine = Cuisine.ALL, val menuCategory: MenuCategory? = null,
    val cart: List<FoodLine> = emptyList(), val instructions: String = "", val tip: Int = 0,
    val address: FoodAddress = FoodAddress(), val payment: FoodPayment = FoodPayment.CASH,
    val addressErrors: Map<String, String> = emptyMap(), val placingOrder: Boolean = false,
    val order: FoodOrder? = null, val trackingStage: TrackingStage = TrackingStage.PLACED,
    val trackingSeconds: Int = 0, val pendingDish: PendingDish? = null, val error: String? = null,
) {
    val filteredRestaurants get() = restaurants.filter {
        (cuisine == Cuisine.ALL || it.cuisine == cuisine) &&
            (query.isBlank() || listOf(it.name, it.cuisine.label, it.description).any { text -> text.contains(query.trim(), true) })
    }
    val cartCount get() = cart.sumOf(FoodLine::quantity)
    val cartRestaurant get() = restaurants.firstOrNull { it.id == cart.firstOrNull()?.dish?.restaurantId }
    val bill get() = FoodFees.bill(cart, tip)
    val etaSeconds get() = if (trackingStage == TrackingStage.DELIVERED) 0 else ((order?.restaurant?.etaMinutes ?: 25) * 60 - trackingSeconds * 35).coerceAtLeast(30)
}
sealed interface FoodIntent {
    data class SelectLocation(val area: String) : FoodIntent
    data class Search(val query: String) : FoodIntent
    data class SelectCuisine(val cuisine: Cuisine) : FoodIntent
    data class SelectMenuCategory(val category: MenuCategory?) : FoodIntent
    data class AddDish(val dishId: String, val addOnIds: Set<String> = emptySet(), val note: String = "") : FoodIntent
    data class ChangeQuantity(val key: String, val delta: Int) : FoodIntent
    data class RemoveLine(val key: String) : FoodIntent
    data class CustomizeLine(val key: String, val addOnIds: Set<String>, val note: String) : FoodIntent
    data class SetInstructions(val instructions: String) : FoodIntent
    data class SetTip(val amount: Int) : FoodIntent
    data class UpdateAddress(val address: FoodAddress) : FoodIntent
    data class SelectPayment(val payment: FoodPayment) : FoodIntent
    data object ConfirmRestaurantSwitch : FoodIntent
    data object CancelRestaurantSwitch : FoodIntent
    data object PlaceOrder : FoodIntent
    data object TrackingTick : FoodIntent
    data object AdvanceTracking : FoodIntent
    data object NewOrder : FoodIntent
    data object Retry : FoodIntent
}
sealed interface FoodEffect { data class OrderPlaced(val id: String) : FoodEffect }
