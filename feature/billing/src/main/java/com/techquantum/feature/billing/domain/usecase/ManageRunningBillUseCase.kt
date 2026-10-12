package com.techquantum.feature.billing.domain.usecase

import com.techquantum.feature.billing.domain.BillingRepository
import javax.inject.Inject

/**
 * Single-responsibility use case to manage ongoing billing actions.
 */
class ManageRunningBillUseCase @Inject constructor(
    private val repository: BillingRepository
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
}
