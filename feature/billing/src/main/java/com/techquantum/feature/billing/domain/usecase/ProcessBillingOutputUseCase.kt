package com.techquantum.feature.billing.domain.usecase

import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.feature.billing.domain.BillingRepository
import com.techquantum.feature.billing.presentation.BillingOutput
import com.techquantum.feature.billing.presentation.BillingOutputState
import com.techquantum.feature.billing.presentation.BillingOutputStatus
import javax.inject.Inject

/**
 * Single-responsibility use case to finalize billing output (print/share).
 */
class ProcessBillingOutputUseCase @Inject constructor(
    private val repository: BillingRepository
) {
    fun completeBill(payment: String): BillReceipt {
        val current = repository.state.value
        check(current.lines.isNotEmpty()) { "Cannot complete bill with empty lines" }
        return repository.complete(payment)
    }

    fun startOutput(output: BillingOutput, hasBillOrReceipt: Boolean): BillingOutputState {
        return if (hasBillOrReceipt) {
            BillingOutputState(output, BillingOutputStatus.RUNNING)
        } else {
            BillingOutputState(output, BillingOutputStatus.IDLE)
        }
    }
}
