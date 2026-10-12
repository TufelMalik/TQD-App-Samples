package com.techquantum.feature.pos.domain

import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.core.billingengine.BillState
import kotlinx.coroutines.flow.StateFlow

/** The operator feature uses the shared bill engine through its domain boundary. */
interface PosRepository {
    val state: StateFlow<BillState>
    fun add(itemId: String)
    fun changeQuantity(itemId: String, delta: Int)
    fun remove(itemId: String)
    fun applyDiscount(percent: Int)
    fun selectTable(table: String)
    fun complete(payment: String): BillReceipt
}
