package com.techquantum.core.billingengine

import java.io.Serializable

data class MenuItem(
    val id: String,
    val name: String,
    val category: String,
    val price: Int,
    val isVeg: Boolean = true,
    val spiceLevel: Int = 0,
) : Serializable

data class BillLine(
    val item: MenuItem,
    val quantity: Int,
    val notes: String = "",
) : Serializable {
    val key: String get() = item.id
    val total: Int get() = item.price * quantity
}

data class BillTotals(val subtotal: Int, val tax: Int, val discount: Int, val total: Int) : Serializable

data class BillReceipt(
    val id: String,
    val lines: List<BillLine>,
    val totals: BillTotals,
    val payment: String,
    val table: String,
) : Serializable

data class BillState(
    val catalog: List<MenuItem>,
    val lines: List<BillLine> = emptyList(),
    val discountPercent: Int = 0,
    val table: String = "Counter 01",
    val lastReceipt: BillReceipt? = null,
) {
    val count: Int get() = lines.sumOf(BillLine::quantity)
    val totals: BillTotals
        get() {
            val subtotal = lines.sumOf(BillLine::total)
            val discount = roundPercent(subtotal, discountPercent.coerceIn(0, 100))
            val taxable = subtotal - discount
            val tax = roundPercent(taxable, 5)
            return BillTotals(subtotal, tax, discount, taxable + tax)
        }
}

/** Demo policy: whole-INR HALF_UP discount, then 5% demo GST on the discounted amount. */
private fun roundPercent(amount: Int, percent: Int): Int = ((amount.toLong() * percent + 50) / 100).toInt()
