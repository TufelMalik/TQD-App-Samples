package com.techquantum.feature.ecommerce.presentation

import com.techquantum.feature.ecommerce.data.FashionCatalog
import com.techquantum.feature.ecommerce.domain.DeliveryPolicy
import com.techquantum.feature.ecommerce.domain.FashionCategory
import com.techquantum.feature.ecommerce.domain.OrderReceipt
import com.techquantum.feature.ecommerce.domain.PaymentMethod
import com.techquantum.feature.ecommerce.domain.ProductSort
import com.techquantum.feature.ecommerce.domain.ShippingAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EcommerceReducerTest {
    private val initial = EcommerceState(products = FashionCatalog.products, loading = false)
    private val shirt = FashionCatalog.products.first { it.id == "urban-oxford" }
    private val validAddress = ShippingAddress(
        name = "Aarav Shah",
        phone = "9876543210",
        line = "12, Lotus Apartments, C G Road",
        city = "Ahmedabad",
        pinCode = "380009",
    )

    @Test
    fun `the same variant merges while a different size remains a separate cart row`() {
        val first = EcommerceReducer.reduce(initial, EcommerceIntent.AddToCart(shirt.id, "Navy", "M"))
        val second = EcommerceReducer.reduce(first, EcommerceIntent.AddToCart(shirt.id, "Navy", "M"))
        val third = EcommerceReducer.reduce(second, EcommerceIntent.AddToCart(shirt.id, "Navy", "L"))

        assertEquals(2, third.cart.size)
        assertEquals(3, third.cartCount)
        assertEquals(2, third.cart.first().quantity)
        assertEquals(1, third.cart.last().quantity)
        assertEquals(4_497, third.subtotal)
        assertEquals(0, third.delivery)
        assertEquals(4_497, third.total)
    }

    @Test
    fun `invalid variants and unknown items do not modify the cart`() {
        val invalidSize = EcommerceReducer.reduce(initial, EcommerceIntent.AddToCart(shirt.id, "Navy", "99"))
        val invalidColor = EcommerceReducer.reduce(initial, EcommerceIntent.AddToCart(shirt.id, "Orange", "M"))
        val unknown = EcommerceReducer.reduce(initial, EcommerceIntent.AddToCart("missing", "Navy", "M"))

        listOf(invalidSize, invalidColor, unknown).forEach { state ->
            assertTrue(state.cart.isEmpty())
            assertNotNull(state.error)
        }
    }

    @Test
    fun `quantity stays within bounds and explicit removal clears totals`() {
        val added = EcommerceReducer.reduce(initial, EcommerceIntent.AddToCart(shirt.id, "Navy", "M"))
        val key = added.cart.single().key
        val decreased = EcommerceReducer.reduce(added, EcommerceIntent.ChangeQuantity(key, -1))
        val maximum = EcommerceReducer.reduce(decreased, EcommerceIntent.ChangeQuantity(key, Int.MAX_VALUE))
        val blockedAdd = EcommerceReducer.reduce(maximum, EcommerceIntent.AddToCart(shirt.id, "Navy", "M"))
        val removed = EcommerceReducer.reduce(blockedAdd, EcommerceIntent.RemoveFromCart(key))

        assertEquals(1, decreased.cart.single().quantity)
        assertEquals(99, maximum.cart.single().quantity)
        assertEquals(99, blockedAdd.cart.single().quantity)
        assertNotNull(blockedAdd.error)
        assertEquals(0, removed.cartCount)
        assertEquals(0, removed.subtotal)
        assertEquals(0, removed.delivery)
        assertEquals(0, removed.total)
    }

    @Test
    fun `delivery is visible below the free shipping threshold and absent on an empty cart`() {
        assertEquals(0, DeliveryPolicy.fee(0))
        assertEquals(99, DeliveryPolicy.fee(1_998))
        assertEquals(0, DeliveryPolicy.fee(1_999))
        assertEquals(0, DeliveryPolicy.fee(2_000))
        val added = EcommerceReducer.reduce(initial, EcommerceIntent.AddToCart(shirt.id, "Navy", "M"))
        assertEquals(1_499, added.subtotal)
        assertEquals(99, added.delivery)
        assertEquals(1_598, added.total)
    }

    @Test
    fun `category search budget and sorting combine without changing the source catalog`() {
        val category = EcommerceReducer.reduce(initial, EcommerceIntent.SelectCategory(FashionCategory.MEN))
        val budget = EcommerceReducer.reduce(category, EcommerceIntent.SetMaxPrice(2_000))
        val sorted = EcommerceReducer.reduce(budget, EcommerceIntent.SetSort(ProductSort.PRICE_HIGH))
        val searched = EcommerceReducer.reduce(sorted, EcommerceIntent.Search("  OXFORD  "))

        assertEquals(listOf("city-chinos", "urban-oxford"), sorted.filteredProducts.map { it.id })
        assertEquals(listOf("urban-oxford"), searched.filteredProducts.map { it.id })
        assertEquals(15, searched.products.size)
        assertEquals(10_000, EcommerceReducer.reduce(initial, EcommerceIntent.SetMaxPrice(Int.MAX_VALUE)).maxPrice)
        assertTrue(EcommerceReducer.reduce(initial, EcommerceIntent.SetMaxPrice(-1)).filteredProducts.isEmpty())
    }

    @Test
    fun `checkout requires valid delivery details and a nonempty cart`() {
        val emptyCheckout = EcommerceReducer.reduce(initial.copy(address = validAddress), EcommerceIntent.PlaceOrder)
        val added = EcommerceReducer.reduce(initial, EcommerceIntent.AddToCart(shirt.id, "Navy", "M"))
        val invalidCheckout = EcommerceReducer.reduce(added, EcommerceIntent.PlaceOrder)
        val edited = EcommerceReducer.reduce(invalidCheckout, EcommerceIntent.UpdateAddress(validAddress))
        val validCheckout = EcommerceReducer.reduce(edited, EcommerceIntent.PlaceOrder)

        assertFalse(emptyCheckout.placingOrder)
        assertNotNull(emptyCheckout.error)
        assertFalse(invalidCheckout.placingOrder)
        assertEquals(setOf("name", "phone", "line", "city", "pinCode"), invalidCheckout.addressErrors.keys)
        assertTrue(edited.addressErrors.isEmpty())
        assertTrue(validCheckout.placingOrder)
        assertEquals(null, validCheckout.error)
    }

    @Test
    fun `phone and PIN validation reject malformed Indian delivery fields`() {
        val invalid = validAddress.copy(phone = "1234567890", pinCode = "012345")
        assertEquals(setOf("phone", "pinCode"), EcommerceReducer.validateAddress(invalid).keys)
        assertTrue(EcommerceReducer.validateAddress(validAddress.copy(
            name = "  Aarav Shah  ",
            phone = " 9876543210 ",
            pinCode = " 380009 ",
        )).isEmpty())
    }

    @Test
    fun `repeated checkout taps and edits cannot change the receipt snapshot`() {
        val added = EcommerceReducer.reduce(initial.copy(address = validAddress), EcommerceIntent.AddToCart(shirt.id, "Navy", "M"))
        val checkout = EcommerceReducer.reduce(added, EcommerceIntent.PlaceOrder)
        val duplicate = EcommerceReducer.reduce(checkout, EcommerceIntent.PlaceOrder)
        val attemptedRemoval = EcommerceReducer.reduce(duplicate, EcommerceIntent.RemoveFromCart(checkout.cart.single().key))
        val attemptedAddressChange = EcommerceReducer.reduce(attemptedRemoval, EcommerceIntent.UpdateAddress(ShippingAddress()))

        assertTrue(checkout.placingOrder)
        assertEquals(checkout, duplicate)
        assertEquals(checkout, attemptedRemoval)
        assertEquals(checkout, attemptedAddressChange)
    }

    @Test
    fun `starting a new bag invalidates the previous receipt so checkout cannot replay it`() {
        val previousBag = EcommerceReducer.reduce(initial, EcommerceIntent.AddToCart(shirt.id, "Navy", "M"))
        val receipt = OrderReceipt(
            id = "TQ-1042",
            items = previousBag.cart,
            address = validAddress,
            payment = PaymentMethod.CASH_ON_DELIVERY,
            subtotal = previousBag.subtotal,
            delivery = previousBag.delivery,
            total = previousBag.total,
        )
        val completed = initial.copy(order = receipt, address = validAddress)
        val invalidAdd = EcommerceReducer.reduce(completed, EcommerceIntent.AddToCart(shirt.id, "Navy", "invalid"))
        val newBag = EcommerceReducer.reduce(completed, EcommerceIntent.AddToCart(shirt.id, "Steel", "L"))
        val checkout = EcommerceReducer.reduce(newBag, EcommerceIntent.PlaceOrder)

        assertEquals(receipt, invalidAdd.order)
        assertNull(newBag.order)
        assertEquals(1, newBag.cartCount)
        assertTrue(checkout.placingOrder)
        assertNull(checkout.order)
    }
}
