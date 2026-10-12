package com.techquantum.core.billingengine.domain.usecase

import com.techquantum.core.billingengine.BillLine
import com.techquantum.core.billingengine.BillTotals
import javax.inject.Inject

/**
 * Single-responsibility use case to calculate billing totals with discount and 5% demo GST.
 * Follows Single Responsibility Principle (SRP).
 */
class CalculateBillTotalsUseCase @Inject constructor() {

    operator fun invoke(lines: List<BillLine>, discountPercent: Int): BillTotals {
        val subtotal = lines.sumOf(BillLine::total)
        val validDiscount = discountPercent.coerceIn(0, 100)
        val discount = roundPercent(subtotal, validDiscount)
        val taxable = subtotal - discount
        val tax = roundPercent(taxable, 5) // 5% GST
        val total = taxable + tax
        return BillTotals(
            subtotal = subtotal,
            tax = tax,
            discount = discount,
            total = total
        )
    }

    private fun roundPercent(amount: Int, percent: Int): Int =
        ((amount.toLong() * percent + 50) / 100).toInt()
}
