package com.techquantum.feature.ecommerce.data

import com.techquantum.feature.ecommerce.domain.CartItem
import com.techquantum.feature.ecommerce.domain.DeliveryPolicy
import com.techquantum.feature.ecommerce.domain.EcommerceRepository
import com.techquantum.feature.ecommerce.domain.OrderReceipt
import com.techquantum.feature.ecommerce.domain.PaymentMethod
import com.techquantum.feature.ecommerce.domain.Product
import com.techquantum.feature.ecommerce.domain.ShippingAddress
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class InMemoryEcommerceRepository @Inject constructor() : EcommerceRepository {
    private val orderSequence = AtomicInteger(1041)

    override suspend fun products(): List<Product> {
        delay(550)
        return FashionCatalog.products
    }

    override suspend fun placeOrder(
        items: List<CartItem>,
        address: ShippingAddress,
        payment: PaymentMethod,
    ): OrderReceipt {
        require(items.isNotEmpty()) { "Add an item before placing your order." }
        require(items.all { it.quantity in 1..99 }) { "Invalid item quantity." }
        delay(750)
        val snapshot = items.toList()
        val subtotal = snapshot.sumOf(CartItem::total)
        val delivery = DeliveryPolicy.fee(subtotal)
        return OrderReceipt(
            id = "TQ-${orderSequence.incrementAndGet()}",
            items = snapshot,
            address = address.copy(
                name = address.name.trim(),
                phone = address.phone.trim(),
                line = address.line.trim(),
                city = address.city.trim(),
                pinCode = address.pinCode.trim(),
            ),
            payment = payment,
            subtotal = subtotal,
            delivery = delivery,
            total = subtotal + delivery,
        )
    }
}
