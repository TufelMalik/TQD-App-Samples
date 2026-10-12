package com.techquantum.feature.ecommerce.data

import com.techquantum.feature.ecommerce.domain.CartItem
import com.techquantum.feature.ecommerce.domain.PaymentMethod
import com.techquantum.feature.ecommerce.domain.ShippingAddress
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryEcommerceRepositoryTest {
    @Test
    fun `local receipts have deterministic unique IDs and snapshot the complete bill`() = runBlocking {
        val repository = InMemoryEcommerceRepository()
        val product = FashionCatalog.products.first { it.id == "block-tee" }
        val items = listOf(CartItem(product, "White", "M", 1))
        val address = ShippingAddress("  Meera Patel  ", "9876543210", "12, River View", "Surat", "395007")

        val first = repository.placeOrder(items, address, PaymentMethod.UPI)
        val second = repository.placeOrder(items, address, PaymentMethod.CASH_ON_DELIVERY)

        assertEquals("TQ-1042", first.id)
        assertEquals("TQ-1043", second.id)
        assertEquals(899, first.subtotal)
        assertEquals(99, first.delivery)
        assertEquals(998, first.total)
        assertEquals("Meera Patel", first.address.name)
        assertEquals(PaymentMethod.UPI, first.payment)
        assertEquals(items, first.items)
    }

    @Test
    fun `the offline catalog covers each fashion collection with selectable variants`() {
        val products = FashionCatalog.products
        assertEquals(products.size, products.map { it.id }.toSet().size)
        assertEquals(5, products.map { it.category }.toSet().size)
        assertTrue(products.all { it.price > 0 && it.originalPrice >= it.price })
        assertTrue(products.all { it.colors.isNotEmpty() && it.sizes.isNotEmpty() })
    }
}
