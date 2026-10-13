package com.techquantum.feature.billing.presentation

import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.core.billingengine.BillState
import com.techquantum.core.billingengine.MenuItem

enum class BillingOutput { PRINT, SHARE }
enum class BillingOutputStatus { IDLE, RUNNING, SUCCEEDED }

data class BillingOutputState(
    val output: BillingOutput = BillingOutput.PRINT,
    val status: BillingOutputStatus = BillingOutputStatus.IDLE,
)

/** A single pending operation owns the receipt, preventing rapid Print / Share double completion. */
fun beginBillingOutput(
    current: BillingOutputState,
    output: BillingOutput,
    hasBillOrReceipt: Boolean,
): BillingOutputState = if (!hasBillOrReceipt || current.status == BillingOutputStatus.RUNNING) current
else BillingOutputState(output, BillingOutputStatus.RUNNING)

data class BillingState(
    val bill: BillState = BillState(emptyList(), emptyList(), 0, "Takeaway", null),
    val loading: Boolean = true,
    val query: String = "",
    val category: String = "All",
    val editingItem: MenuItem? = null,
    val editingExisting: Boolean = false,
    val editQuantity: Int = 1,
    val editNotes: String = "",
    val receipt: BillReceipt? = null,
    val payment: String = "UPI (demo)",
    val output: BillingOutputState = BillingOutputState(),
    val error: String? = null,
) {
    val visibleItems: List<MenuItem> get() = bill.catalog.filter {
        (category == "All" || it.category == category) &&
            (query.isBlank() || it.name.contains(query.trim(), ignoreCase = true))
    }
    val busy: Boolean get() = output.status == BillingOutputStatus.RUNNING
    // Output owns its captured snapshot; an idle preview follows the shared counter's latest receipt.
    val previewReceipt: BillReceipt? get() = when {
        output.status != BillingOutputStatus.IDLE -> receipt ?: bill.lastReceipt
        bill.lines.isEmpty() -> bill.lastReceipt ?: receipt
        else -> null
    }
}

sealed interface BillingIntent {
    data class Search(val query: String) : BillingIntent
    data class SelectCategory(val category: String) : BillingIntent
    data class Add(val itemId: String) : BillingIntent
    data class ChangeQuantity(val itemId: String, val delta: Int) : BillingIntent
    data class Remove(val itemId: String) : BillingIntent
    data class Edit(val itemId: String, val existing: Boolean) : BillingIntent
    data class EditQuantity(val delta: Int) : BillingIntent
    data class EditNotes(val notes: String) : BillingIntent
    data object DismissEditor : BillingIntent
    data object SaveEditor : BillingIntent
    data class ApplyDiscount(val percent: Int) : BillingIntent
    data class SelectTable(val table: String) : BillingIntent
    data class SelectPayment(val payment: String) : BillingIntent
    data class Output(val output: BillingOutput) : BillingIntent
    data object DismissOutput : BillingIntent
    data object DismissError : BillingIntent
}

data class BillingEffect(val message: String)
