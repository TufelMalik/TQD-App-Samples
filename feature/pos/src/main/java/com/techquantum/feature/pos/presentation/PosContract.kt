package com.techquantum.feature.pos.presentation

import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.core.billingengine.BillState
import com.techquantum.core.billingengine.MenuItem

data class PosState(
    val bill: BillState,
    val loading: Boolean = true,
    val query: String = "",
    val category: String = "All",
    val operator: String = "Riya Shah",
    val payment: String = "UPI",
    val charging: Boolean = false,
    val receipt: BillReceipt? = null,
    val error: String? = null,
    val lastAddedId: String? = null,
    val addPulse: Int = 0,
) {
    val filteredCatalog: List<MenuItem> get() = bill.catalog.filter {
        (category == "All" || category == it.category) && it.name.contains(query.trim(), ignoreCase = true)
    }
    val categories: List<String> get() = listOf("All") + bill.catalog.map { it.category }.distinct()
}

sealed interface PosIntent {
    data class Search(val query: String) : PosIntent
    data class SelectCategory(val category: String) : PosIntent
    data class AddItem(val id: String) : PosIntent
    data class ChangeQuantity(val id: String, val delta: Int) : PosIntent
    data class RemoveItem(val id: String) : PosIntent
    data class ApplyDiscount(val percent: Int) : PosIntent
    data class SelectTable(val table: String) : PosIntent
    data class SelectPayment(val payment: String) : PosIntent
    data object Charge : PosIntent
    data object ClearError : PosIntent
    data object StartNewBill : PosIntent
}
