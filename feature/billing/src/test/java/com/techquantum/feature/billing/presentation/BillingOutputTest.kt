package com.techquantum.feature.billing.presentation

import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.core.billingengine.BillState
import com.techquantum.core.billingengine.BillTotals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class BillingOutputTest {
    private fun receipt(id: String) = BillReceipt(id, emptyList(), BillTotals(120, 6, 0, 126), "UPI", "Counter 01")

    @Test fun idlePreviewFollowsLatestSharedReceiptAfterPosCompletesAnotherBill() {
        val old = receipt("BILL-1042")
        val latest = receipt("BILL-1043")
        val state = BillingState(bill = BillState(emptyList(), lastReceipt = latest), receipt = old)
        assertSame(latest, state.previewReceipt)
    }

    @Test fun restoredReceiptIsAvailableWhenTheLocalRepositoryHasNoReceipt() {
        val restored = receipt("BILL-1042")
        assertSame(restored, BillingState(receipt = restored).previewReceipt)
    }

    @Test fun outputResultKeepsItsCapturedReceiptWhenPosCompletesAnotherBill() {
        val captured = receipt("BILL-1042")
        val newer = receipt("BILL-1043")
        val state = BillingState(bill = BillState(emptyList(), lastReceipt = newer), receipt = captured,
            output = BillingOutputState(BillingOutput.PRINT, BillingOutputStatus.SUCCEEDED))
        assertSame(captured, state.previewReceipt)
        assertSame(newer, state.copy(output = BillingOutputState()).previewReceipt)
    }

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
