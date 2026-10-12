package com.techquantum.feature.fooddelivery.presentation

import com.techquantum.feature.fooddelivery.domain.*

object FoodReducer {
    fun reduce(state: FoodState, intent: FoodIntent): FoodState {
        if (state.placingOrder && intent !is FoodIntent.TrackingTick && intent !is FoodIntent.AdvanceTracking) return state
        return when (intent) {
            is FoodIntent.SelectLocation -> state.copy(deliveryArea = intent.area.take(60))
            is FoodIntent.Search -> state.copy(query = intent.query.take(100))
            is FoodIntent.SelectCuisine -> state.copy(cuisine = intent.cuisine)
            is FoodIntent.SelectMenuCategory -> state.copy(menuCategory = intent.category)
            is FoodIntent.AddDish -> add(state, PendingDish(intent.dishId, intent.addOnIds, intent.note))
            is FoodIntent.ChangeQuantity -> state.copy(cart = state.cart.mapNotNull { line ->
                if (line.key != intent.key) line else (line.quantity + intent.delta.coerceIn(-1, 1)).let { quantity ->
                    if (quantity <= 0) null else line.copy(quantity = quantity.coerceAtMost(99))
                }
            }, error = null)
            is FoodIntent.RemoveLine -> state.copy(cart = state.cart.filterNot { it.key == intent.key }, error = null)
            is FoodIntent.CustomizeLine -> customize(state, intent)
            is FoodIntent.SetInstructions -> state.copy(instructions = intent.instructions.take(180))
            is FoodIntent.SetTip -> state.copy(tip = intent.amount.coerceIn(0, 100))
            is FoodIntent.UpdateAddress -> state.copy(address = intent.address, addressErrors = emptyMap(), error = null)
            is FoodIntent.SelectPayment -> state.copy(payment = intent.payment)
            FoodIntent.CancelRestaurantSwitch -> state.copy(pendingDish = null)
            FoodIntent.ConfirmRestaurantSwitch -> state.pendingDish?.let { add(state.copy(cart = emptyList(), tip = 0, pendingDish = null), it) } ?: state
            FoodIntent.PlaceOrder -> {
                val errors = validateAddress(state.address)
                when {
                    state.cart.isEmpty() -> state.copy(error = "Add something delicious before checkout.")
                    errors.isNotEmpty() -> state.copy(addressErrors = errors, error = "Check your delivery details.")
                    else -> state.copy(placingOrder = true, addressErrors = emptyMap(), error = null)
                }
            }
            FoodIntent.TrackingTick -> if (state.order == null || state.trackingStage == TrackingStage.DELIVERED) state else {
                val seconds = state.trackingSeconds + 1
                val automatic = when { seconds >= 42 -> TrackingStage.DELIVERED; seconds >= 24 -> TrackingStage.OUT_FOR_DELIVERY; seconds >= 8 -> TrackingStage.PREPARING; else -> TrackingStage.PLACED }
                state.copy(trackingSeconds = seconds, trackingStage = maxOf(state.trackingStage, automatic))
            }
            FoodIntent.AdvanceTracking -> if (state.order == null) state else state.copy(trackingStage = TrackingStage.entries[(state.trackingStage.ordinal + 1).coerceAtMost(3)])
            FoodIntent.NewOrder -> state.copy(order = null, trackingStage = TrackingStage.PLACED, trackingSeconds = 0, error = null, query = "", cuisine = Cuisine.ALL)
            FoodIntent.Retry -> state.copy(loading = true, error = null)
        }
    }

    private fun add(state: FoodState, addition: PendingDish): FoodState {
        val dish = state.restaurants.flatMap(Restaurant::dishes).firstOrNull { it.id == addition.dishId } ?: return state
        if (state.cart.isNotEmpty() && state.cart.first().dish.restaurantId != dish.restaurantId) return state.copy(pendingDish = addition)
        val line = FoodLine(dish, dish.addOns.filter { it.id in addition.addOnIds }.sortedBy(AddOn::id), addition.note.trim().take(120))
        val existing = state.cart.firstOrNull { it.key == line.key }
        return state.copy(cart = if (existing == null) state.cart + line else state.cart.map { if (it.key == line.key) it.copy(quantity = (it.quantity + 1).coerceAtMost(99)) else it }, pendingDish = null, error = null)
    }

    private fun customize(state: FoodState, intent: FoodIntent.CustomizeLine): FoodState {
        val line = state.cart.firstOrNull { it.key == intent.key } ?: return state
        val changed = line.copy(addOns = line.dish.addOns.filter { it.id in intent.addOnIds }.sortedBy(AddOn::id), note = intent.note.trim().take(120))
        val others = state.cart.filterNot { it.key == line.key }
        val existing = others.firstOrNull { it.key == changed.key }
        val updated = if (existing == null) others + changed else others.map { if (it.key == changed.key) it.copy(quantity = (it.quantity + changed.quantity).coerceAtMost(99)) else it }
        return state.copy(cart = updated, error = null)
    }

    fun validateAddress(address: FoodAddress): Map<String, String> = buildMap {
        if (address.name.trim().length < 2) put("name", "Enter your name")
        if (!address.phone.matches(Regex("[6-9][0-9]{9}"))) put("phone", "Enter a 10-digit mobile number")
        if (address.line.trim().length < 8) put("line", "Add your building and street")
        if (address.city.trim().length < 2) put("city", "Enter your city")
        if (!address.pinCode.matches(Regex("[1-9][0-9]{5}"))) put("pinCode", "Enter a 6-digit PIN code")
    }
}
