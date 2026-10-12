package com.techquantum.core.billingengine.domain.repository

import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.core.billingengine.BillState
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for managing billing state across POS and Billing modules.
 * Follows Dependency Inversion Principle (DIP).
 */
interface IBillRepository {
    val state: StateFlow<BillState>

    fun add(itemId: String, quantity: Int = 1, notes: String = "")
    fun changeQuantity(itemId: String, delta: Int)
    fun remove(itemId: String)
    fun updateNotes(itemId: String, notes: String)
    fun applyDiscount(percent: Int)
    fun selectTable(table: String)
    fun complete(payment: String): BillReceipt
    fun clear()
}
