package com.techquantum.feature.ecommerce.domain

interface EcommerceRepository {
    suspend fun products(): List<Product>

    /** Records a local demo receipt; never contacts a payment or delivery service. */
    suspend fun placeOrder(
        items: List<CartItem>,
        address: ShippingAddress,
        payment: PaymentMethod,
    ): OrderReceipt
}
