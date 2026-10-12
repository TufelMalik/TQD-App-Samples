package com.techquantum.feature.billing.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class BillingOutputTest {
    @Test fun rapidPrintThenShareKeepsOnePendingOperation() {
        val pending = beginBillingOutput(BillingOutputState(), BillingOutput.PRINT, hasBillOrReceipt = true)
        assertEquals(BillingOutputStatus.RUNNING, pending.status)
        assertSame(pending, beginBillingOutput(pending, BillingOutput.PRINT, true))
        assertSame(pending, beginBillingOutput(pending, BillingOutput.SHARE, true))
        assertEquals(BillingOutput.PRINT, pending.output)
    }

    @Test fun emptyBillWithoutReceiptCannotPrint() {
        val idle = BillingOutputState()
        assertSame(idle, beginBillingOutput(idle, BillingOutput.PRINT, hasBillOrReceipt = false))
    }

    @Test fun finishedReceiptCanBeSharedWithoutStartingANewBill() {
        val printed = BillingOutputState(BillingOutput.PRINT, BillingOutputStatus.SUCCEEDED)
        val sharing = beginBillingOutput(printed, BillingOutput.SHARE, hasBillOrReceipt = true)
        assertEquals(BillingOutput.SHARE, sharing.output)
        assertEquals(BillingOutputStatus.RUNNING, sharing.status)
    }
}
