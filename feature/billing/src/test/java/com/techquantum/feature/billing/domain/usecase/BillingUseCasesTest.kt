package com.techquantum.feature.billing.domain.usecase

import com.techquantum.core.billingengine.BillCatalogue
import com.techquantum.core.billingengine.BillLine
import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.core.billingengine.BillState
import com.techquantum.feature.billing.domain.BillingRepository
import com.techquantum.feature.billing.presentation.BillingOutput
import com.techquantum.feature.billing.presentation.BillingOutputStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BillingUseCasesTest {

    private val catalog = BillCatalogue.items
    private val mutableState = MutableStateFlow(BillState(catalog = catalog))

    private val fakeRepository = object : BillingRepository {
        override val state: StateFlow<BillState> = mutableState.asStateFlow()
        override fun add(itemId: String, quantity: Int, notes: String) {
            val item = catalog.first { it.id == itemId }
            val current = mutableState.value
            mutableState.value = current.copy(lines = current.lines + BillLine(item, quantity, notes))
        }
        override fun changeQuantity(itemId: String, delta: Int) {}
        override fun remove(itemId: String) {
            val current = mutableState.value
            mutableState.value = current.copy(lines = current.lines.filterNot { it.item.id == itemId })
        }
        override fun updateNotes(itemId: String, notes: String) {}
        override fun applyDiscount(percent: Int) {
            mutableState.value = mutableState.value.copy(discountPercent = percent)
        }
        override fun selectTable(table: String) {
            mutableState.value = mutableState.value.copy(table = table)
        }
        override fun complete(payment: String): BillReceipt {
            val current = mutableState.value
            val receipt = BillReceipt(
                id = "BILL-999",
                lines = current.lines,
                totals = current.totals,
                payment = payment,
                table = current.table
            )
            mutableState.value = current.copy(lines = emptyList(), lastReceipt = receipt)
            return receipt
        }
    }

    private val getBillingCatalogUseCase = GetBillingCatalogUseCase()
    private val manageRunningBillUseCase = ManageRunningBillUseCase(fakeRepository)
    private val processBillingOutputUseCase = ProcessBillingOutputUseCase(fakeRepository)

    @Test
    fun getBillingCatalogFiltersByCategoryAndQuery() {
        val filtered = getBillingCatalogUseCase(catalog, category = "All", query = "Dosa")
        assertTrue(filtered.isNotEmpty())
        assertTrue(filtered.all { it.name.contains("Dosa", ignoreCase = true) })
    }

    @Test
    fun manageRunningBillAppliesMutationsCorrectly() {
        val item = catalog.first()
        manageRunningBillUseCase.addItem(item.id, quantity = 2, notes = "No onion")
        assertEquals(1, fakeRepository.state.value.lines.size)
        assertEquals("No onion", fakeRepository.state.value.lines.first().notes)

        manageRunningBillUseCase.applyDiscount(10)
        assertEquals(10, fakeRepository.state.value.discountPercent)

        manageRunningBillUseCase.selectTable("Table 12")
        assertEquals("Table 12", fakeRepository.state.value.table)
    }

    @Test
    fun processBillingOutputGeneratesReceiptAndManagesOutputState() {
        val item = catalog.first()
        manageRunningBillUseCase.addItem(item.id, quantity = 1)

        val receipt = processBillingOutputUseCase.completeBill("Cash")
        assertEquals("BILL-999", receipt.id)
        assertEquals("Cash", receipt.payment)

        val runningState = processBillingOutputUseCase.startOutput(BillingOutput.PRINT, hasBillOrReceipt = true)
        assertEquals(BillingOutputStatus.RUNNING, runningState.status)

        val idleState = processBillingOutputUseCase.startOutput(BillingOutput.PRINT, hasBillOrReceipt = false)
        assertEquals(BillingOutputStatus.IDLE, idleState.status)
    }
}
