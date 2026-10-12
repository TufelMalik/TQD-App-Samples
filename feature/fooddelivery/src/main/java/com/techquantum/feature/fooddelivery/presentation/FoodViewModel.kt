package com.techquantum.feature.fooddelivery.presentation

import androidx.annotation.MainThread
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techquantum.feature.fooddelivery.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.techquantum.feature.fooddelivery.domain.usecase.GetRestaurantsUseCase
import com.techquantum.feature.fooddelivery.domain.usecase.PlaceFoodOrderUseCase

@HiltViewModel
class FoodViewModel @Inject constructor(
    private val getRestaurantsUseCase: GetRestaurantsUseCase,
    private val placeFoodOrderUseCase: PlaceFoodOrderUseCase,
    private val saved: SavedStateHandle,
) : ViewModel() {
    private val mutableState = MutableStateFlow(FoodState(
        cart = saved.get<ArrayList<FoodLine>>("food.cart")?.toList().orEmpty(),
        address = saved["food.address"] ?: FoodAddress(), payment = FoodPayment.entries.firstOrNull { it.name == saved.get<String>("food.payment") } ?: FoodPayment.CASH,
        instructions = saved.get<String>("food.instructions").orEmpty(), tip = saved["food.tip"] ?: 0,
        order = saved["food.order"], trackingSeconds = saved["food.seconds"] ?: 0,
        trackingStage = TrackingStage.entries.firstOrNull { it.name == saved.get<String>("food.stage") } ?: TrackingStage.PLACED,
    ))
    val state: StateFlow<FoodState> = mutableState.asStateFlow()
    private val mutableEffects = MutableSharedFlow<FoodEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<FoodEffect> = mutableEffects.asSharedFlow()
    private var catalogJob: Job? = null
    private var trackingJob: Job? = null
    init { load(); if (mutableState.value.order != null) track() }

    @MainThread fun accept(intent: FoodIntent) {
        val before = mutableState.value
        val next = FoodReducer.reduce(before, intent)
        mutableState.value = next
        persist(next)
        when (intent) {
            FoodIntent.Retry -> if (!next.placingOrder) load()
            FoodIntent.PlaceOrder -> if (!before.placingOrder && next.placingOrder) place(next)
            FoodIntent.NewOrder -> trackingJob?.cancel()
            else -> Unit
        }
    }
    private fun load() {
        catalogJob?.cancel()
        catalogJob = viewModelScope.launch {
            mutableState.value = mutableState.value.copy(loading = true, error = null)
            try { mutableState.value = mutableState.value.copy(restaurants = getRestaurantsUseCase(), loading = false) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { mutableState.value = mutableState.value.copy(loading = false, error = "The kitchens couldn't load. Try again.") }
        }
    }
    private fun place(snapshot: FoodState) = viewModelScope.launch {
        try {
            val restaurant = snapshot.cartRestaurant ?: error("Restaurant unavailable")
            val order = placeFoodOrderUseCase(restaurant, snapshot.cart, snapshot.address, snapshot.payment, snapshot.instructions, snapshot.tip)
            val next = mutableState.value.copy(cart = emptyList(), placingOrder = false, order = order, trackingStage = TrackingStage.PLACED, trackingSeconds = 0, tip = 0, error = null)
            mutableState.value = next; persist(next); track()
            mutableEffects.emit(FoodEffect.OrderPlaced(order.id))
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { mutableState.value = mutableState.value.copy(placingOrder = false, error = "Your preview order couldn't be placed. Please try again.") }
    }
    private fun track() {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            while (mutableState.value.order != null && mutableState.value.trackingStage != TrackingStage.DELIVERED) {
                delay(1000); accept(FoodIntent.TrackingTick)
            }
        }
    }
    private fun persist(state: FoodState) {
        saved["food.cart"] = ArrayList(state.cart); saved["food.address"] = state.address
        saved["food.payment"] = state.payment.name; saved["food.instructions"] = state.instructions
        saved["food.tip"] = state.tip; saved["food.order"] = state.order
        saved["food.seconds"] = state.trackingSeconds; saved["food.stage"] = state.trackingStage.name
    }
}
