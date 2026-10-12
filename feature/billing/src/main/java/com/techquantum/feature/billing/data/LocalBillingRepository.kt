package com.techquantum.feature.billing.data

import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.core.billingengine.BillRepository
import com.techquantum.feature.billing.domain.BillingRepository
import javax.inject.Inject

class LocalBillingRepository @Inject constructor(private val bills: BillRepository) : BillingRepository {
    override val state = bills.state
    override fun add(itemId: String, quantity: Int, notes: String) = bills.add(itemId, quantity, notes)
    override fun changeQuantity(itemId: String, delta: Int) = bills.changeQuantity(itemId, delta)
    override fun remove(itemId: String) = bills.remove(itemId)
    override fun updateNotes(itemId: String, notes: String) = bills.updateNotes(itemId, notes)
    override fun applyDiscount(percent: Int) = bills.applyDiscount(percent)
    override fun selectTable(table: String) = bills.selectTable(table)
    override fun complete(payment: String): BillReceipt = bills.complete(payment)
}
