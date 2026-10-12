package com.techquantum.core.billingengine

import com.techquantum.core.billingengine.domain.usecase.CalculateBillTotalsUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateBillTotalsUseCaseTest {

    private val useCase = CalculateBillTotalsUseCase()

    @Test
    fun computesSubtotalTaxAndTotalWithoutDiscount() {
        val dosa = MenuItem(id = "dosa", name = "Masala Dosa", category = "Food", price = 120)
        val lines = listOf(BillLine(item = dosa, quantity = 2)) // 240
        val totals = useCase(lines, discountPercent = 0)

        assertEquals(240, totals.subtotal)
        assertEquals(0, totals.discount)
        // 5% GST on 240 = 12
        assertEquals(12, totals.tax)
        assertEquals(252, totals.total)
    }

    @Test
    fun appliesDiscountThenCalculatesGstOnDiscountedAmount() {
        val dosa = MenuItem(id = "dosa", name = "Masala Dosa", category = "Food", price = 200)
        val lines = listOf(BillLine(item = dosa, quantity = 1)) // 200
        val totals = useCase(lines, discountPercent = 10) // 10% of 200 = 20

        assertEquals(200, totals.subtotal)
        assertEquals(20, totals.discount)
        // Taxable = 180. 5% GST on 180 = 9
        assertEquals(9, totals.tax)
        assertEquals(189, totals.total)
    }

    @Test
    fun handlesEmptyLinesGracefully() {
        val totals = useCase(emptyList(), discountPercent = 20)
        assertEquals(0, totals.subtotal)
        assertEquals(0, totals.discount)
        assertEquals(0, totals.tax)
        assertEquals(0, totals.total)
    }
}
