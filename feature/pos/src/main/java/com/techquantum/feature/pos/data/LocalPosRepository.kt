package com.techquantum.feature.pos.data

import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.core.billingengine.BillRepository
import com.techquantum.core.billingengine.BillState
import com.techquantum.feature.pos.domain.PosRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class LocalPosRepository @Inject constructor(private val billRepository: BillRepository) : PosRepository {
    override val state: StateFlow<BillState> = billRepository.state
    override fun add(itemId: String) = billRepository.add(itemId)
    override fun changeQuantity(itemId: String, delta: Int) = billRepository.changeQuantity(itemId, delta)
    override fun remove(itemId: String) = billRepository.remove(itemId)
    override fun applyDiscount(percent: Int) = billRepository.applyDiscount(percent)
    override fun selectTable(table: String) = billRepository.selectTable(table)
    override fun complete(payment: String): BillReceipt = billRepository.complete(payment)
}
