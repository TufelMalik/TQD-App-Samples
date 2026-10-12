package com.techquantum.feature.billing.domain

import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.core.billingengine.BillState
import kotlinx.coroutines.flow.StateFlow

/** Billing and POS deliberately observe the same local running order. */
interface BillingRepository {
    val state: StateFlow<BillState>
    fun add(itemId: String, quantity: Int = 1, notes: String = "")
    fun changeQuantity(itemId: String, delta: Int)
    fun remove(itemId: String)
    fun updateNotes(itemId: String, notes: String)
    fun applyDiscount(percent: Int)
    fun selectTable(table: String)
    fun complete(payment: String): BillReceipt
}
