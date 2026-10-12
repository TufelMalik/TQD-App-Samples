package com.techquantum.core.billingengine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BillRepositoryTest {
    @Test
    fun `the shared catalog merges item quantities while keeping notes and different items`() {
        val repository = BillRepository()
        repository.add("idli-sambar", 2, "  Sambar on the side  ")
        repository.add("idli-sambar")
        repository.add("masala-dosa")
        val bill = repository.state.value
        assertEquals(2, bill.lines.size)
        assertEquals(4, bill.count)
        assertEquals(3, bill.lines.first().quantity)
        assertEquals("Sambar on the side", bill.lines.first().notes)
        assertEquals(375, bill.totals.subtotal)
    }

    @Test
    fun `discount and GST round half up on the discounted total`() {
        val repository = BillRepository()
        repository.add("idli-sambar") // 85; 10% discount rounds 8.5 to 9.
        repository.applyDiscount(10)
        assertEquals(BillTotals(subtotal = 85, tax = 4, discount = 9, total = 80), repository.state.value.totals)
        repository.changeQuantity("idli-sambar", 1)
        assertEquals(BillTotals(subtotal = 170, tax = 8, discount = 17, total = 161), repository.state.value.totals)
        repository.applyDiscount(100)
        assertEquals(BillTotals(subtotal = 170, tax = 0, discount = 170, total = 0), repository.state.value.totals)
    }

    @Test
    fun `quantity boundaries cannot create negative or overflowing bills`() {
        val repository = BillRepository()
        repository.add("unknown")
        repository.add("masala-dosa", -1)
        assertTrue(repository.state.value.lines.isEmpty())
        repository.add("masala-dosa", Int.MAX_VALUE)
        assertEquals(99, repository.state.value.count)
        repository.changeQuantity("masala-dosa", Int.MIN_VALUE)
        assertEquals(1, repository.state.value.count)
        repository.changeQuantity("masala-dosa", Int.MAX_VALUE)
        assertEquals(99, repository.state.value.count)
        repository.remove("masala-dosa")
        assertEquals(BillTotals(0, 0, 0, 0), repository.state.value.totals)
    }

    @Test
    fun `completion freezes the receipt and atomically clears the shared running bill`() {
        val repository = BillRepository()
        repository.selectTable("Table 03")
        repository.add("paneer-tikka", 2)
        repository.applyDiscount(10)
        val before = repository.state.value
        val receipt = repository.complete("UPI")
        assertEquals("BILL-1042", receipt.id)
        assertEquals(before.lines, receipt.lines)
        assertEquals(before.totals, receipt.totals)
        assertEquals("Table 03", receipt.table)
        assertEquals("UPI", receipt.payment)
        assertTrue(repository.state.value.lines.isEmpty())
        assertEquals(0, repository.state.value.discountPercent)
        assertEquals(receipt, repository.state.value.lastReceipt)
        assertThrows(IllegalStateException::class.java) { repository.complete("UPI") }

        repository.add("butter-naan")
        assertNull(repository.state.value.lastReceipt)
        val next = repository.complete("Cash")
        assertEquals("BILL-1043", next.id)
        assertEquals(before.lines, receipt.lines)
        assertEquals(before.totals, receipt.totals)
    }

    @Test
    fun `the repository serializes concurrent counter taps`() {
        val repository = BillRepository()
        val workers = List(5) { Thread { repeat(5) { repository.add("masala-chaas") } } }
        workers.forEach(Thread::start)
        workers.forEach(Thread::join)
        assertEquals(25, repository.state.value.count)
        assertEquals(1, repository.state.value.lines.size)
        assertEquals(BillTotals(1_500, 75, 0, 1_575), repository.state.value.totals)
    }

    @Test
    fun `clearing a draft resets its discount and notes without removing the catalog or counter`() {
        val repository = BillRepository()
        repository.add("paneer-tikka", notes = "No onion")
        repository.updateNotes("paneer-tikka", "  Extra mint chutney  ")
        repository.selectTable("Takeaway")
        repository.applyDiscount(15)
        assertEquals("Extra mint chutney", repository.state.value.lines.single().notes)
        repository.clear()
        assertEquals("Takeaway", repository.state.value.table)
        assertEquals(15, repository.state.value.catalog.size)
        assertEquals(0, repository.state.value.discountPercent)
        assertFalse(repository.state.value.catalog.all { it.isVeg })
        assertTrue(repository.state.value.lines.isEmpty())
    }
}
