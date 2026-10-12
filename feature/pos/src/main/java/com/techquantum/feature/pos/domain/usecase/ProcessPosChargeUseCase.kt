package com.techquantum.feature.pos.domain.usecase

import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.feature.pos.domain.PosRepository
import javax.inject.Inject

/**
 * Single-responsibility use case to process and finalize POS charges.
 */
class ProcessPosChargeUseCase @Inject constructor(
    private val repository: PosRepository
) {
    operator fun invoke(paymentMethod: String): BillReceipt {
        check(repository.state.value.lines.isNotEmpty()) { "Add an item before charging this bill." }
        return repository.complete(paymentMethod)
    }
}
