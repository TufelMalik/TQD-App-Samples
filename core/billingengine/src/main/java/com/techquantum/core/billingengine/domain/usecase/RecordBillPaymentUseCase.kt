package com.techquantum.core.billingengine.domain.usecase

import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.core.billingengine.domain.repository.IBillRepository
import javax.inject.Inject

/**
 * Single-responsibility use case to record payment and issue a validated receipt.
 */
class RecordBillPaymentUseCase @Inject constructor(
    private val repository: IBillRepository
) {
    operator fun invoke(paymentMethod: String): BillReceipt {
        val currentState = repository.state.value
        check(currentState.lines.isNotEmpty()) { "Cannot complete bill with empty lines" }
        return repository.complete(paymentMethod)
    }
}
