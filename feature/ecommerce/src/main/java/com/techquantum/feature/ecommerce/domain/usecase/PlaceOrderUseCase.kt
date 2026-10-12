package com.techquantum.feature.ecommerce.domain.usecase

import com.techquantum.feature.ecommerce.domain.CartItem
import com.techquantum.feature.ecommerce.domain.EcommerceRepository
import com.techquantum.feature.ecommerce.domain.OrderReceipt
import com.techquantum.feature.ecommerce.domain.PaymentMethod
import com.techquantum.feature.ecommerce.domain.ShippingAddress
import javax.inject.Inject

/**
 * Single-responsibility use case to place an e-commerce order.
 */
class PlaceOrderUseCase @Inject constructor(
    private val repository: EcommerceRepository,
    private val validateAddress: ValidateShippingAddressUseCase,
) {
    suspend operator fun invoke(
        cart: List<CartItem>,
        address: ShippingAddress,
        payment: PaymentMethod,
    ): OrderReceipt {
        check(cart.isNotEmpty()) { "Add an item before placing your order." }
        val errors = validateAddress(address)
        check(errors.isEmpty()) { "Check your delivery details." }
        return repository.placeOrder(cart, address, payment)
    }
}
