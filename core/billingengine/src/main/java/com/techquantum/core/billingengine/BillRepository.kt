package com.techquantum.core.billingengine

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** One local bill shared by POS and Billing. Synchronized mutations keep receipt IDs and totals atomic. */
@Singleton
class BillRepository @Inject constructor() {
    private val mutableState = MutableStateFlow(BillState(catalog = BillCatalogue.items))
    val state: StateFlow<BillState> = mutableState.asStateFlow()
    private var receiptSequence = 1041

    @Synchronized
    fun add(itemId: String, quantity: Int = 1, notes: String = "") {
        val current = mutableState.value
        val item = current.catalog.firstOrNull { it.id == itemId } ?: return
        if (quantity <= 0) return
        val existing = current.lines.firstOrNull { it.key == itemId }
        val cleanNotes = notes.trim().take(160)
        val nextLine = if (existing == null) BillLine(item, quantity.coerceAtMost(99), cleanNotes)
        else existing.copy(
            quantity = (existing.quantity.toLong() + quantity).coerceAtMost(99).toInt(),
            notes = cleanNotes.ifEmpty { existing.notes },
        )
        mutableState.value = current.copy(
            lines = if (existing == null) current.lines + nextLine else current.lines.map { if (it.key == itemId) nextLine else it },
            lastReceipt = null,
        )
    }

    @Synchronized
    fun changeQuantity(itemId: String, delta: Int) {
        mutableState.value = mutableState.value.let { current -> current.copy(lines = current.lines.map {
            if (it.key == itemId) it.copy(quantity = (it.quantity.toLong() + delta).coerceIn(1, 99).toInt()) else it
        }) }
    }

    @Synchronized
    fun remove(itemId: String) {
        mutableState.value = mutableState.value.let { current -> current.copy(lines = current.lines.filterNot { it.key == itemId }) }
    }

    @Synchronized
    fun updateNotes(itemId: String, notes: String) {
        mutableState.value = mutableState.value.let { current -> current.copy(lines = current.lines.map {
            if (it.key == itemId) it.copy(notes = notes.trim().take(160)) else it
        }) }
    }

    @Synchronized
    fun applyDiscount(percent: Int) {
        mutableState.value = mutableState.value.copy(discountPercent = percent.coerceIn(0, 100))
    }

    @Synchronized
    fun selectTable(table: String) {
        if (table.isNotBlank()) mutableState.value = mutableState.value.copy(table = table.trim().take(40))
    }

    @Synchronized
    fun complete(payment: String): BillReceipt {
        val current = mutableState.value
        check(current.lines.isNotEmpty()) { "Add an item before completing a bill." }
        val receipt = BillReceipt(
            id = "BILL-${++receiptSequence}",
            lines = current.lines.toList(),
            totals = current.totals,
            payment = payment.trim().ifEmpty { "Cash" }.take(40),
            table = current.table,
        )
        mutableState.value = current.copy(lines = emptyList(), discountPercent = 0, lastReceipt = receipt)
        return receipt
    }

    @Synchronized
    fun clear() {
        mutableState.value = mutableState.value.copy(lines = emptyList(), discountPercent = 0, lastReceipt = null)
    }
}
