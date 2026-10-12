package com.techquantum.feature.ecommerce.domain.usecase

import com.techquantum.feature.ecommerce.data.FashionCatalog
import com.techquantum.feature.ecommerce.domain.CartItem
import com.techquantum.feature.ecommerce.domain.EcommerceRepository
import com.techquantum.feature.ecommerce.domain.FashionCategory
import com.techquantum.feature.ecommerce.domain.OrderReceipt
import com.techquantum.feature.ecommerce.domain.PaymentMethod
import com.techquantum.feature.ecommerce.domain.ProductSort
import com.techquantum.feature.ecommerce.domain.ShippingAddress
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EcommerceUseCasesTest {

    private val sampleProducts = FashionCatalog.products

    private val fakeRepository = object : EcommerceRepository {
        override suspend fun products() = sampleProducts
        override suspend fun placeOrder(
            cart: List<CartItem>,
            address: ShippingAddress,
            payment: PaymentMethod
        ): OrderReceipt {
            val subtotal = cart.sumOf { it.total }
            return OrderReceipt(
                id = "TEST-ORDER-1",
                items = cart,
                address = address,
                payment = payment,
                subtotal = subtotal,
                delivery = 0,
                total = subtotal
            )
        }
    }

    private val getCatalogUseCase = GetCatalogUseCase(fakeRepository)
    private val filterProductsUseCase = FilterProductsUseCase()
    private val validateAddressUseCase = ValidateShippingAddressUseCase()
    private val placeOrderUseCase = PlaceOrderUseCase(fakeRepository, validateAddressUseCase)

    @Test
    fun getCatalogReturnsAllProducts() = runBlocking {
        val products = getCatalogUseCase()
        assertTrue(products.isNotEmpty())
        assertEquals(sampleProducts.size, products.size)
    }

    @Test
    fun filterProductsFiltersByCategoryAndSorts() {
        val ethnicProducts = filterProductsUseCase(
            products = sampleProducts,
            query = "",
            category = FashionCategory.ETHNIC,
            maxPrice = 10_000,
            sort = ProductSort.PRICE_LOW
        )

        assertTrue(ethnicProducts.isNotEmpty())
        assertTrue(ethnicProducts.all { it.category == FashionCategory.ETHNIC })
        // Check sorted by price ascending
        for (i in 0 until ethnicProducts.size - 1) {
            assertTrue(ethnicProducts[i].price <= ethnicProducts[i + 1].price)
        }
    }

    @Test
    fun filterProductsFiltersBySearchQuery() {
        val results = filterProductsUseCase(
            products = sampleProducts,
            query = "Kurta",
            category = FashionCategory.ALL,
            maxPrice = 10_000,
            sort = ProductSort.FEATURED
        )

        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.name.contains("Kurta", ignoreCase = true) })
    }

    @Test
    fun validateAddressCatchesMissingAndInvalidFields() {
        val emptyAddress = ShippingAddress()
        val errors = validateAddressUseCase(emptyAddress)

        assertTrue(errors.containsKey("name"))
        assertTrue(errors.containsKey("phone"))
        assertTrue(errors.containsKey("line"))
        assertTrue(errors.containsKey("city"))
        assertTrue(errors.containsKey("pinCode"))

        val validAddress = ShippingAddress(
            name = "Demo Shopper",
            phone = "9876543210",
            line = "12 Sample Road",
            city = "Ahmedabad",
            pinCode = "380001"
        )
        val validErrors = validateAddressUseCase(validAddress)
        assertTrue(validErrors.isEmpty())
    }

    @Test
    fun placeOrderSucceedsWithValidData() = runBlocking {
        val validAddress = ShippingAddress(
            name = "Demo Shopper",
            phone = "9876543210",
            line = "12 Sample Road",
            city = "Ahmedabad",
            pinCode = "380001"
        )
        val product = sampleProducts.first()
        val cart = listOf(CartItem(product = product, color = product.colors.first(), size = product.sizes.first(), quantity = 2))

        val receipt = placeOrderUseCase(cart, validAddress, PaymentMethod.CASH_ON_DELIVERY)
        assertEquals("TEST-ORDER-1", receipt.id)
        assertEquals(cart.size, receipt.items.size)
        assertEquals(PaymentMethod.CASH_ON_DELIVERY, receipt.payment)
    }
}
