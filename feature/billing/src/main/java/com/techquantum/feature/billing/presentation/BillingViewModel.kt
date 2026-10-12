package com.techquantum.feature.billing.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.feature.billing.domain.BillingRepository
import com.techquantum.feature.billing.domain.usecase.ManageRunningBillUseCase
import com.techquantum.feature.billing.domain.usecase.ProcessBillingOutputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val repository: BillingRepository,
    private val manageRunningBillUseCase: ManageRunningBillUseCase,
    private val processBillingOutputUseCase: ProcessBillingOutputUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val mutableState = MutableStateFlow(BillingState(
        bill = repository.state.value,
        query = savedStateHandle["billingQuery"] ?: "",
        category = savedStateHandle["billingCategory"] ?: "All",
        receipt = savedStateHandle.get<BillReceipt>("billingReceipt"),
        payment = savedStateHandle["billingPayment"] ?: "UPI (demo)",
    ))
    val state = mutableState.asStateFlow()
    private val effectsChannel = Channel<BillingEffect>(Channel.BUFFERED)
    val effects = effectsChannel.receiveAsFlow()

    init {
        viewModelScope.launch { repository.state.collect { bill -> mutableState.update { it.copy(bill = bill) } } }
        viewModelScope.launch { delay(280); mutableState.update { it.copy(loading = false) } }
    }

    fun onIntent(intent: BillingIntent) {
        val current = mutableState.value
        if (current.busy && intent !is BillingIntent.DismissError) return
        when (intent) {
            is BillingIntent.Search -> {
                savedStateHandle["billingQuery"] = intent.query
                mutableState.update { it.copy(query = intent.query) }
            }
            is BillingIntent.SelectCategory -> {
                savedStateHandle["billingCategory"] = intent.category
                mutableState.update { it.copy(category = intent.category) }
            }
            is BillingIntent.Add -> {
                manageRunningBillUseCase.addItem(intent.itemId)
                effectsChannel.trySend(BillingEffect("Added to the running bill"))
            }
            is BillingIntent.ChangeQuantity -> manageRunningBillUseCase.changeQuantity(intent.itemId, intent.delta)
            is BillingIntent.Remove -> {
                manageRunningBillUseCase.removeItem(intent.itemId)
                effectsChannel.trySend(BillingEffect("Item removed. Add it again from the menu."))
            }
            is BillingIntent.Edit -> {
                val item = current.bill.catalog.firstOrNull { it.id == intent.itemId } ?: return
                val line = current.bill.lines.firstOrNull { it.item.id == intent.itemId }
                mutableState.update { it.copy(editingItem = item, editingExisting = intent.existing,
                    editQuantity = if (intent.existing) line?.quantity ?: 1 else 1,
                    editNotes = if (intent.existing) line?.notes.orEmpty() else "") }
            }
            is BillingIntent.EditQuantity -> mutableState.update { it.copy(editQuantity = (it.editQuantity + intent.delta).coerceIn(1, 99)) }
            is BillingIntent.EditNotes -> mutableState.update { it.copy(editNotes = intent.notes.take(120)) }
            BillingIntent.DismissEditor -> mutableState.update { it.copy(editingItem = null) }
            BillingIntent.SaveEditor -> {
                val item = current.editingItem ?: return
                if (current.editingExisting) {
                    val existing = repository.state.value.lines.firstOrNull { it.item.id == item.id }
                    if (existing != null) {
                        manageRunningBillUseCase.changeQuantity(item.id, current.editQuantity - existing.quantity)
                        manageRunningBillUseCase.updateNotes(item.id, current.editNotes)
                    } else manageRunningBillUseCase.addItem(item.id, current.editQuantity, current.editNotes)
                } else manageRunningBillUseCase.addItem(item.id, current.editQuantity, current.editNotes)
                mutableState.update { it.copy(editingItem = null) }
                effectsChannel.trySend(BillingEffect("${item.name} updated in the bill"))
            }
            is BillingIntent.ApplyDiscount -> manageRunningBillUseCase.applyDiscount(intent.percent)
            is BillingIntent.SelectTable -> manageRunningBillUseCase.selectTable(intent.table)
            is BillingIntent.SelectPayment -> {
                savedStateHandle["billingPayment"] = intent.payment
                mutableState.update { it.copy(payment = intent.payment) }
            }
            is BillingIntent.Output -> output(intent.output)
            BillingIntent.DismissOutput -> mutableState.update { it.copy(output = BillingOutputState()) }
            BillingIntent.DismissError -> mutableState.update { it.copy(error = null) }
        }
    }

    private fun output(output: BillingOutput) {
        val current = mutableState.value
        val transition = beginBillingOutput(current.output, output,
            repository.state.value.lines.isNotEmpty() || current.previewReceipt != null)
        if (transition === current.output) return
        mutableState.update { it.copy(output = transition, error = null) }
        viewModelScope.launch {
            try {
                val receipt = if (repository.state.value.lines.isNotEmpty()) processBillingOutputUseCase.completeBill(current.payment)
                    else current.previewReceipt ?: error("No receipt to output")
                savedStateHandle["billingReceipt"] = receipt
                mutableState.update { it.copy(receipt = receipt) }
                delay(560)
                mutableState.update { it.copy(output = transition.copy(status = BillingOutputStatus.SUCCEEDED)) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(output = BillingOutputState(),
                    error = "The demo receipt couldn't be prepared. Your running bill is still available.") }
            }
        }
    }
}
