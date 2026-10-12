package com.techquantum.feature.pos.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.feature.pos.domain.PosRepository
import com.techquantum.feature.pos.domain.usecase.ManagePosBillUseCase
import com.techquantum.feature.pos.domain.usecase.ProcessPosChargeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PosViewModel @Inject constructor(
    private val repository: PosRepository,
    private val managePosBillUseCase: ManagePosBillUseCase,
    private val processPosChargeUseCase: ProcessPosChargeUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val mutableState = MutableStateFlow(PosState(
        bill = repository.state.value,
        query = savedStateHandle.get<String>("query").orEmpty(),
        category = savedStateHandle["category"] ?: "All",
        payment = savedStateHandle["payment"] ?: "UPI",
        receipt = savedStateHandle.get<BillReceipt>("receipt"),
    ))
    val state: StateFlow<PosState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch { repository.state.collect { bill -> mutableState.update { it.copy(bill = bill) } } }
        viewModelScope.launch { delay(400); mutableState.update { it.copy(loading = false) } }
    }

    fun accept(intent: PosIntent) {
        if (mutableState.value.charging && intent != PosIntent.ClearError) return
        when (intent) {
            is PosIntent.Search -> mutableState.update { it.copy(query = intent.query) }
            is PosIntent.SelectCategory -> mutableState.update { it.copy(category = intent.category) }
            is PosIntent.AddItem -> {
                managePosBillUseCase.addItem(intent.id)
                mutableState.update { it.copy(receipt = null, lastAddedId = intent.id, addPulse = it.addPulse + 1, error = null) }
            }
            is PosIntent.ChangeQuantity -> managePosBillUseCase.changeQuantity(intent.id, intent.delta)
            is PosIntent.RemoveItem -> managePosBillUseCase.removeItem(intent.id)
            is PosIntent.ApplyDiscount -> managePosBillUseCase.applyDiscount(intent.percent)
            is PosIntent.SelectTable -> managePosBillUseCase.selectTable(intent.table)
            is PosIntent.SelectPayment -> mutableState.update { it.copy(payment = intent.payment) }
            PosIntent.ClearError -> mutableState.update { it.copy(error = null) }
            PosIntent.StartNewBill -> mutableState.update { it.copy(receipt = null, error = null) }
            PosIntent.Charge -> {
                if (repository.state.value.lines.isEmpty()) {
                    mutableState.update { it.copy(error = "Add an item before charging this bill.") }
                } else {
                    // Snapshot synchronously before the animation, so Billing cannot alter this payment.
                    mutableState.update { it.copy(charging = true, error = null) }
                    val receipt = processPosChargeUseCase(mutableState.value.payment)
                    savedStateHandle["receipt"] = receipt
                    mutableState.update { it.copy(receipt = receipt, bill = repository.state.value) }
                    viewModelScope.launch { delay(650); mutableState.update { it.copy(charging = false) } }
                }
            }
        }
        val next = mutableState.value
        savedStateHandle["query"] = next.query
        savedStateHandle["category"] = next.category
        savedStateHandle["payment"] = next.payment
        savedStateHandle["receipt"] = next.receipt
    }
}
