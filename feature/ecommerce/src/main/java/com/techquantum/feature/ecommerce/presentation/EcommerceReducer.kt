package com.techquantum.feature.ecommerce.presentation

import com.techquantum.feature.ecommerce.domain.CartItem
import com.techquantum.feature.ecommerce.domain.FashionCategory
import com.techquantum.feature.ecommerce.domain.ShippingAddress

/** Pure state transitions keep checkout, variants and totals independent of Compose. */
object EcommerceReducer {
    const val MAX_QUANTITY = 99

    fun reduce(state: EcommerceState, intent: EcommerceIntent): EcommerceState {
        // The checkout snapshot is immutable while its local receipt is being created.
        if (state.placingOrder && intent !is EcommerceIntent.ClearError) return state
        return when (intent) {
            is EcommerceIntent.Search -> state.copy(query = intent.query)
            is EcommerceIntent.SelectCategory -> state.copy(category = intent.category)
            is EcommerceIntent.SetSort -> state.copy(sort = intent.sort)
            is EcommerceIntent.SetMaxPrice -> state.copy(maxPrice = intent.price.coerceIn(0, 10_000))
            EcommerceIntent.ToggleGrid -> state.copy(gridView = !state.gridView)
            is EcommerceIntent.ToggleWishlist -> {
                if (state.products.none { it.id == intent.productId }) state
                else state.copy(wishlist = if (intent.productId in state.wishlist) {
                    state.wishlist - intent.productId
                } else {
                    state.wishlist + intent.productId
                })
            }
            is EcommerceIntent.AddToCart -> addToCart(state, intent)
            is EcommerceIntent.ChangeQuantity -> state.copy(
                cart = state.cart.map { item ->
                    if (item.key == intent.key) item.copy(
                        quantity = (item.quantity.toLong() + intent.delta)
                            .coerceIn(1, MAX_QUANTITY.toLong()).toInt(),
                    ) else item
                },
                error = null,
            )
            is EcommerceIntent.RemoveFromCart -> state.copy(
                cart = state.cart.filterNot { it.key == intent.key },
                error = null,
            )
            is EcommerceIntent.UpdateAddress -> state.copy(
                address = intent.address,
                addressErrors = state.addressErrors.keys.associateWith { field ->
                    validateAddress(intent.address)[field].orEmpty()
                }.filterValues(String::isNotEmpty),
                error = null,
            )
            is EcommerceIntent.SelectPayment -> state.copy(payment = intent.payment, error = null)
            EcommerceIntent.PlaceOrder -> {
                val errors = validateAddress(state.address)
                when {
                    state.cart.isEmpty() -> state.copy(error = "Add an item before placing your order.")
                    errors.isNotEmpty() -> state.copy(addressErrors = errors, error = "Check your delivery details.")
                    else -> state.copy(placingOrder = true, addressErrors = emptyMap(), error = null)
                }
            }
            EcommerceIntent.ClearError -> state.copy(error = null)
            EcommerceIntent.Retry -> state.copy(loading = true, error = null)
            EcommerceIntent.StartNewOrder -> state.copy(
                order = null,
                category = FashionCategory.ALL,
                query = "",
                error = null,
                addressErrors = emptyMap(),
            )
        }
    }

    fun validateAddress(address: ShippingAddress): Map<String, String> = buildMap {
        if (address.name.trim().length < 2) put("name", "Enter your full name.")
        if (!address.phone.trim().matches(Regex("[6-9][0-9]{9}"))) {
            put("phone", "Enter a valid 10-digit Indian mobile number.")
        }
        if (address.line.trim().length < 6) put("line", "Enter your building and street.")
        if (address.city.trim().length < 2) put("city", "Enter your city.")
        if (!address.pinCode.trim().matches(Regex("[1-9][0-9]{5}"))) {
            put("pinCode", "Enter a valid 6-digit PIN code.")
        }
    }

    private fun addToCart(state: EcommerceState, intent: EcommerceIntent.AddToCart): EcommerceState {
        val product = state.products.firstOrNull { it.id == intent.productId }
            ?: return state.copy(error = "This item is unavailable. Try another style.")
        if (intent.color !in product.colors || intent.size !in product.sizes) {
            return state.copy(error = "Choose an available colour and size.")
        }
        val newItem = CartItem(product, intent.color, intent.size, 1)
        val existingItem = state.cart.firstOrNull { it.key == newItem.key }
        if (existingItem != null && existingItem.quantity >= MAX_QUANTITY) {
            return state.copy(error = "You can add up to $MAX_QUANTITY of each variant.")
        }
        return state.copy(
            cart = if (existingItem == null) state.cart + newItem else state.cart.map { item ->
                if (item.key == newItem.key) item.copy(quantity = item.quantity + 1) else item
            },
            order = null,
            error = null,
        )
    }
}
