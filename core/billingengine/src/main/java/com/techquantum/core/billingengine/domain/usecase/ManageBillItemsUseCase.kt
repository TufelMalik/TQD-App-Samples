package com.techquantum.core.billingengine.domain.usecase

import com.techquantum.core.billingengine.domain.repository.IBillRepository
import javax.inject.Inject

/**
 * Single-responsibility use case to manage item additions, quantities, notes, and removals.
 */
class ManageBillItemsUseCase @Inject constructor(
    private val repository: IBillRepository
) {
    fun addItem(itemId: String, quantity: Int = 1, notes: String = "") {
        repository.add(itemId, quantity, notes)
    }

    fun changeQuantity(itemId: String, delta: Int) {
        repository.changeQuantity(itemId, delta)
    }

    fun removeItem(itemId: String) {
        repository.remove(itemId)
    }

    fun updateNotes(itemId: String, notes: String) {
        repository.updateNotes(itemId, notes)
    }

    fun applyDiscount(percent: Int) {
        repository.applyDiscount(percent)
    }

    fun selectTable(table: String) {
        repository.selectTable(table)
    }

    fun clear() {
        repository.clear()
    }
}
