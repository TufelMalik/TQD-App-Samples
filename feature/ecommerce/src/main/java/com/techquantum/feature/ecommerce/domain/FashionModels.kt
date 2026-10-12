package com.techquantum.feature.ecommerce.domain

import java.io.Serializable

enum class FashionCategory(val label: String) {
    ALL("All"),
    ETHNIC("Ethnic"),
    MEN("Menswear"),
    STREETWEAR("Streetwear"),
    ACTIVE("Activewear"),
    WOMEN("Womenswear"),
}

enum class GarmentStyle { KURTA, SHIRT, HOODIE, JACKET, DRESS, TROUSERS }

enum class ProductSort(val label: String) {
    FEATURED("Featured"),
    PRICE_LOW("Price: low to high"),
    PRICE_HIGH("Price: high to low"),
}

/** A local, inclusive-of-GST demo price in whole Indian rupees. */
data class Product(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: FashionCategory,
    val price: Int,
    val originalPrice: Int,
    val rating: Double,
    val reviewCount: Int,
    val description: String,
    val colors: List<String>,
    val sizes: List<String>,
    val artwork: GarmentStyle,
) : Serializable

data class CartItem(
    val product: Product,
    val color: String,
    val size: String,
    val quantity: Int,
) : Serializable {
    val key: String get() = "${product.id}|$color|$size"
    val total: Int get() = product.price * quantity
}

data class ShippingAddress(
    val name: String = "",
    val phone: String = "",
    val line: String = "",
    val city: String = "",
    val pinCode: String = "",
) : Serializable

enum class PaymentMethod(val label: String) {
    UPI("UPI"),
    CARD("Debit / credit card"),
    CASH_ON_DELIVERY("Cash on delivery"),
}

data class OrderReceipt(
    val id: String,
    val items: List<CartItem>,
    val address: ShippingAddress,
    val payment: PaymentMethod,
    val subtotal: Int,
    val delivery: Int,
    val total: Int,
) : Serializable

object DeliveryPolicy {
    const val FREE_DELIVERY_MINIMUM = 1_999
    const val DELIVERY_FEE = 99

    fun fee(subtotal: Int): Int =
        if (subtotal == 0 || subtotal >= FREE_DELIVERY_MINIMUM) 0 else DELIVERY_FEE
}
