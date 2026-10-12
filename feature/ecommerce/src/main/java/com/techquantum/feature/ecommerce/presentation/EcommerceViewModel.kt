package com.techquantum.feature.ecommerce.presentation

import androidx.annotation.MainThread
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techquantum.feature.ecommerce.domain.CartItem
import com.techquantum.feature.ecommerce.domain.FashionCategory
import com.techquantum.feature.ecommerce.domain.OrderReceipt
import com.techquantum.feature.ecommerce.domain.PaymentMethod
import com.techquantum.feature.ecommerce.domain.ProductSort
import com.techquantum.feature.ecommerce.domain.ShippingAddress
import com.techquantum.feature.ecommerce.domain.usecase.GetCatalogUseCase
import com.techquantum.feature.ecommerce.domain.usecase.PlaceOrderUseCase
import com.techquantum.feature.ecommerce.domain.usecase.ValidateShippingAddressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class EcommerceViewModel @Inject constructor(
    private val getCatalogUseCase: GetCatalogUseCase,
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val validateAddressUseCase: ValidateShippingAddressUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val mutableState = MutableStateFlow(restoreState())
    val state: StateFlow<EcommerceState> = mutableState.asStateFlow()

    private val mutableEffects = MutableSharedFlow<EcommerceEffect>(extraBufferCapacity = 8)
    val effects: SharedFlow<EcommerceEffect> = mutableEffects.asSharedFlow()
    private var catalogJob: Job? = null

    init {
        loadCatalog()
    }

    @MainThread
    fun accept(intent: EcommerceIntent) {
        val previous = mutableState.value
        val next = EcommerceReducer.reduce(previous, intent)
        mutableState.value = next
        persistState(next)
        when (intent) {
            is EcommerceIntent.AddToCart -> if (next.cartCount > previous.cartCount) {
                next.products.firstOrNull { it.id == intent.productId }?.let { product ->
                    viewModelScope.launch { mutableEffects.emit(EcommerceEffect.AddedToCart(product.name)) }
                }
            }
            EcommerceIntent.PlaceOrder -> if (!previous.placingOrder && next.placingOrder) {
                placeOrder(next)
            }
            EcommerceIntent.Retry -> if (!next.placingOrder) loadCatalog()
            else -> Unit
        }
    }

    private fun loadCatalog() {
        catalogJob?.cancel()
        catalogJob = viewModelScope.launch {
            mutableState.value = mutableState.value.copy(loading = true, error = null)
            try {
                val products = getCatalogUseCase()
                mutableState.value = mutableState.value.copy(products = products, loading = false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.value = mutableState.value.copy(
                    loading = false,
                    error = "Couldn't load this edit. Please try again.",
                )
            }
        }
    }

    private fun placeOrder(snapshot: EcommerceState) {
        viewModelScope.launch {
            try {
                val receipt = placeOrderUseCase(snapshot.cart, snapshot.address, snapshot.payment)
                val next = mutableState.value.copy(
                    placingOrder = false,
                    order = receipt,
                    cart = emptyList(),
                    error = null,
                )
                mutableState.value = next
                persistState(next)
                mutableEffects.emit(EcommerceEffect.OrderPlaced(receipt.id))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.value = mutableState.value.copy(
                    placingOrder = false,
                    error = "Your demo order couldn't be placed. Please try again.",
                )
            }
        }
    }

    private fun restoreState(): EcommerceState = EcommerceState(
        cart = savedStateHandle.get<ArrayList<CartItem>>("cart")?.toList().orEmpty(),
        address = savedStateHandle.get<ShippingAddress>("address") ?: ShippingAddress(),
        wishlist = savedStateHandle.get<ArrayList<String>>("wishlist")?.toSet().orEmpty(),
        query = savedStateHandle.get<String>("query").orEmpty(),
        category = enumValueOrDefault(savedStateHandle["category"], FashionCategory.ALL),
        sort = enumValueOrDefault(savedStateHandle["sort"], ProductSort.FEATURED),
        gridView = savedStateHandle["gridView"] ?: true,
        maxPrice = (savedStateHandle.get<Int>("maxPrice") ?: 10_000).coerceIn(0, 10_000),
        payment = enumValueOrDefault(savedStateHandle["payment"], PaymentMethod.CASH_ON_DELIVERY),
        order = savedStateHandle.get<OrderReceipt>("order"),
    )

    private fun persistState(state: EcommerceState) {
        savedStateHandle["cart"] = ArrayList(state.cart)
        savedStateHandle["address"] = state.address
        savedStateHandle["wishlist"] = ArrayList(state.wishlist)
        savedStateHandle["query"] = state.query
        savedStateHandle["category"] = state.category.name
        savedStateHandle["sort"] = state.sort.name
        savedStateHandle["gridView"] = state.gridView
        savedStateHandle["maxPrice"] = state.maxPrice
        savedStateHandle["payment"] = state.payment.name
        savedStateHandle["order"] = state.order
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == value } ?: default
}
