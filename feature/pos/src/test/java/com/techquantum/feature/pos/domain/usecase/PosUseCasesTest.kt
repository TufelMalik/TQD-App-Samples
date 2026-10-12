package com.techquantum.feature.pos.domain.usecase

import com.techquantum.core.billingengine.BillCatalogue
import com.techquantum.core.billingengine.BillLine
import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.core.billingengine.BillState
import com.techquantum.core.billingengine.BillTotals
import com.techquantum.feature.pos.domain.PosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PosUseCasesTest {

    private val catalog = BillCatalogue.items
    private val mutableState = MutableStateFlow(BillState(catalog = catalog))

    private val fakeRepository = object : PosRepository {
        override val state: StateFlow<BillState> = mutableState.asStateFlow()
        override fun add(itemId: String) {
            val item = catalog.first { it.id == itemId }
            val current = mutableState.value
            mutableState.value = current.copy(lines = current.lines + BillLine(item, 1))
        }
        override fun changeQuantity(itemId: String, delta: Int) {}
        override fun remove(itemId: String) {
            val current = mutableState.value
            mutableState.value = current.copy(lines = current.lines.filterNot { it.item.id == itemId })
        }
        override fun applyDiscount(percent: Int) {
            mutableState.value = mutableState.value.copy(discountPercent = percent)
        }
        override fun selectTable(table: String) {
            mutableState.value = mutableState.value.copy(table = table)
        }
        override fun complete(payment: String): BillReceipt {
            val current = mutableState.value
            val receipt = BillReceipt(
                id = "BILL-POS-01",
                lines = current.lines,
                totals = current.totals,
                payment = payment,
                table = current.table
            )
            mutableState.value = current.copy(lines = emptyList(), lastReceipt = receipt)
            return receipt
        }
    }

    private val getPosCatalogUseCase = GetPosCatalogUseCase()
    private val managePosBillUseCase = ManagePosBillUseCase(fakeRepository)
    private val processPosChargeUseCase = ProcessPosChargeUseCase(fakeRepository)

    @Test
    fun getPosCatalogFiltersByCategoryAndSearch() {
        val allFiltered = getPosCatalogUseCase(catalog, category = "All", query = "Dosa")
        assertTrue(allFiltered.isNotEmpty())
        assertTrue(allFiltered.all { it.name.contains("Dosa", ignoreCase = true) })

        val breakfastOnly = getPosCatalogUseCase(catalog, category = "Breakfast", query = "")
        assertTrue(breakfastOnly.isNotEmpty())
        assertTrue(breakfastOnly.all { it.category == "Breakfast" })
    }

    @Test
    fun managePosBillPerformsStateMutations() {
        val dosa = catalog.first { it.id == "masala-dosa" }
        managePosBillUseCase.addItem(dosa.id)
        assertEquals(1, fakeRepository.state.value.lines.size)

        managePosBillUseCase.applyDiscount(15)
        assertEquals(15, fakeRepository.state.value.discountPercent)

        managePosBillUseCase.selectTable("Table 05")
        assertEquals("Table 05", fakeRepository.state.value.table)

        managePosBillUseCase.removeItem(dosa.id)
        assertEquals(0, fakeRepository.state.value.lines.size)
    }

    @Test
    fun processPosChargeCompletesAndReturnsReceipt() {
        val dosa = catalog.first { it.id == "masala-dosa" }
        managePosBillUseCase.addItem(dosa.id)

        val receipt = processPosChargeUseCase("UPI")
        assertEquals("BILL-POS-01", receipt.id)
        assertEquals("UPI", receipt.payment)
        assertEquals(0, fakeRepository.state.value.lines.size)
    }
}
