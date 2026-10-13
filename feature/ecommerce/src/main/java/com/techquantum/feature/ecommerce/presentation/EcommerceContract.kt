package com.techquantum.feature.ecommerce.presentation

import com.techquantum.feature.ecommerce.domain.CartItem
import com.techquantum.feature.ecommerce.domain.DeliveryPolicy
import com.techquantum.feature.ecommerce.domain.FashionCategory
import com.techquantum.feature.ecommerce.domain.OrderReceipt
import com.techquantum.feature.ecommerce.domain.PaymentMethod
import com.techquantum.feature.ecommerce.domain.Product
import com.techquantum.feature.ecommerce.domain.ProductSort
import com.techquantum.feature.ecommerce.domain.ShippingAddress

data class EcommerceState(
    val products: List<Product> = emptyList(),
    val loading: Boolean = true,
    val query: String = "",
    val category: FashionCategory = FashionCategory.ALL,
    val sort: ProductSort = ProductSort.FEATURED,
    val gridView: Boolean = true,
    val maxPrice: Int = 10_000,
    val wishlist: Set<String> = emptySet(),
    val cart: List<CartItem> = emptyList(),
    val address: ShippingAddress = ShippingAddress(),
    val payment: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    val addressErrors: Map<String, String> = emptyMap(),
    val placingOrder: Boolean = false,
    val order: OrderReceipt? = null,
    val error: String? = null,
) {
    val filteredProducts: List<Product>
        get() {
            val search = query.trim()
            val matches = products.filter { product ->
                (category == FashionCategory.ALL || product.category == category) &&
                    product.price <= maxPrice &&
                    (search.isBlank() || listOf(
                        product.name,
                        product.subtitle,
                        product.category.label,
                    ).any { it.contains(search, ignoreCase = true) })
            }
            return when (sort) {
                ProductSort.FEATURED -> matches
                ProductSort.PRICE_LOW -> matches.sortedBy(Product::price)
                ProductSort.PRICE_HIGH -> matches.sortedByDescending(Product::price)
            }
        }

    val cartCount: Int get() = cart.sumOf(CartItem::quantity)
    val subtotal: Int get() = cart.sumOf(CartItem::total)
    val delivery: Int get() = DeliveryPolicy.fee(subtotal)
    val total: Int get() = subtotal + delivery
}

sealed interface EcommerceIntent {
    data class Search(val query: String) : EcommerceIntent
    data class SelectCategory(val category: FashionCategory) : EcommerceIntent
    data class SetSort(val sort: ProductSort) : EcommerceIntent
    data class SetMaxPrice(val price: Int) : EcommerceIntent
    data object ToggleGrid : EcommerceIntent
    data class ToggleWishlist(val productId: String) : EcommerceIntent
    data class AddToCart(val productId: String, val color: String, val size: String, val openCart: Boolean = false) : EcommerceIntent
    data class ChangeQuantity(val key: String, val delta: Int) : EcommerceIntent
    data class RemoveFromCart(val key: String) : EcommerceIntent
    data class UpdateAddress(val address: ShippingAddress) : EcommerceIntent
    data class SelectPayment(val payment: PaymentMethod) : EcommerceIntent
    data object PlaceOrder : EcommerceIntent
    data object ClearError : EcommerceIntent
    data object Retry : EcommerceIntent
    data object StartNewOrder : EcommerceIntent
}

sealed interface EcommerceEffect {
    data class AddedToCart(val productName: String, val openCart: Boolean = false) : EcommerceEffect
    data class OrderPlaced(val orderId: String) : EcommerceEffect
}
