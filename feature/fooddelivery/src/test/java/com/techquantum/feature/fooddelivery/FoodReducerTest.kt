package com.techquantum.feature.fooddelivery

import com.techquantum.feature.fooddelivery.data.FoodCatalog
import com.techquantum.feature.fooddelivery.data.LocalFoodRepository
import com.techquantum.feature.fooddelivery.domain.*
import com.techquantum.feature.fooddelivery.presentation.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FoodReducerTest {
    private val restaurant = FoodCatalog.restaurants.first()
    private val dish = restaurant.dishes.first()
    private val ready = FoodState(restaurants = FoodCatalog.restaurants, loading = false)
    private val address = FoodAddress("Aditi Shah", "9876543210", "24 River View, Adajan", "Surat", "395009")

    @Test fun customizationChangesPriceAndKeepsDifferentNotesSeparate() {
        val first = FoodReducer.reduce(ready, FoodIntent.AddDish(dish.id, setOf("paneer", "butter", "unknown"), " Less spice "))
        assertEquals(304, first.cart.single().unitPrice)
        assertEquals(listOf("butter", "paneer"), first.cart.single().addOns.map(AddOn::id))
        assertEquals("Less spice", first.cart.single().note)
        val merged = FoodReducer.reduce(first, FoodIntent.AddDish(dish.id, setOf("butter", "paneer"), "Less spice"))
        assertEquals(2, merged.cart.single().quantity)
        val separate = FoodReducer.reduce(merged, FoodIntent.AddDish(dish.id, setOf("butter", "paneer"), "No onion"))
        assertEquals(2, separate.cart.size)
        assertEquals(3, separate.cartCount)
    }

    @Test fun billsIncludeAddOnsGstDeliveryPlatformAndOptionalTip() {
        val simple = FoodFees.bill(listOf(FoodLine(dish = FoodCatalog.restaurants[1].dishes.first())), tip = 20)
        assertEquals(FoodBill(159, 8, 29, 5, 20), simple)
        assertEquals(221, simple.total)
        val threshold = FoodFees.bill(listOf(FoodLine(dish.copy(price = 499))))
        assertEquals(0, threshold.delivery)
        assertEquals(25, threshold.tax)
        assertEquals(529, threshold.total)
        assertEquals(FoodBill(0, 0, 0, 0, 0), FoodFees.bill(emptyList(), tip = 30))
    }

    @Test fun editingCustomizationMergesMatchingRowsWithoutLosingQuantity() {
        var state = FoodReducer.reduce(ready, FoodIntent.AddDish(dish.id))
        state = FoodReducer.reduce(state, FoodIntent.AddDish(dish.id, setOf("paneer")))
        val customized = state.cart.last()
        state = FoodReducer.reduce(state, FoodIntent.ChangeQuantity(customized.key, 1))
        state = FoodReducer.reduce(state, FoodIntent.CustomizeLine(customized.key, emptySet(), ""))
        assertEquals(1, state.cart.size)
        assertEquals(3, state.cart.single().quantity)
        assertEquals(747, state.bill.subtotal)
    }

    @Test fun anotherRestaurantRequiresExplicitReplacementAndCancelKeepsTheOrder() {
        val original = FoodReducer.reduce(ready, FoodIntent.AddDish(dish.id)).copy(tip = 20)
        val otherDish = FoodCatalog.restaurants[1].dishes.first()
        val pending = FoodReducer.reduce(original, FoodIntent.AddDish(otherDish.id, setOf("cheese"), "Crispy"))
        assertEquals(original.cart, pending.cart)
        assertNotNull(pending.pendingDish)
        assertEquals(original.cart, FoodReducer.reduce(pending, FoodIntent.CancelRestaurantSwitch).cart)
        val switched = FoodReducer.reduce(pending, FoodIntent.ConfirmRestaurantSwitch)
        assertEquals(otherDish.id, switched.cart.single().dish.id)
        assertEquals(189, switched.cart.single().unitPrice)
        assertEquals("Crispy", switched.cart.single().note)
        assertEquals(0, switched.tip)
        assertNull(switched.pendingDish)
    }

    @Test fun removeFinalQuickAddedItemClearsAllCharges() {
        val added = FoodReducer.reduce(ready, FoodIntent.AddDish(dish.id))
        val removed = FoodReducer.reduce(added, FoodIntent.ChangeQuantity(added.cart.single().key, -1))
        assertTrue(removed.cart.isEmpty())
        assertEquals(0, removed.bill.total)
    }

    @Test fun checkoutValidatesTheGuestAndPreventsDuplicateSubmissionsAndEdits() {
        val invalid = FoodReducer.reduce(FoodReducer.reduce(ready, FoodIntent.AddDish(dish.id)), FoodIntent.PlaceOrder)
        assertFalse(invalid.placingOrder)
        assertEquals(setOf("name", "phone", "line", "pinCode"), invalid.addressErrors.keys)
        val submitting = FoodReducer.reduce(invalid.copy(address = address), FoodIntent.PlaceOrder)
        assertTrue(submitting.placingOrder)
        assertEquals(submitting, FoodReducer.reduce(submitting, FoodIntent.PlaceOrder))
        assertEquals(submitting, FoodReducer.reduce(submitting, FoodIntent.RemoveLine(submitting.cart.single().key)))
        assertEquals(submitting, FoodReducer.reduce(submitting, FoodIntent.AddDish(dish.id)))
    }

    @Test fun repositoryReceiptIsAnIndependentItemizedSnapshot() = runBlocking {
        val lines = mutableListOf(FoodLine(dish, dish.addOns.filter { it.id == "paneer" }, "Less spice", 2))
        val receipt = LocalFoodRepository().placeOrder(restaurant, lines, address, FoodPayment.UPI, "Ring the bell", 20)
        lines.clear()
        assertEquals(2, receipt.lines.single().quantity)
        assertEquals(listOf("paneer"), receipt.lines.single().addOns.map(AddOn::id))
        assertEquals("Less spice", receipt.lines.single().note)
        assertEquals(578, receipt.bill.subtotal)
        assertEquals(29, receipt.bill.tax)
        assertEquals(0, receipt.bill.delivery)
        assertEquals(632, receipt.bill.total)
        assertEquals("Ring the bell", receipt.instructions)
        assertEquals(FoodPayment.UPI, receipt.payment)
    }

    @Test fun previewUpdatesAndTickerNeverRegressACompletedDelivery() {
        val order = FoodOrder("TQ-TEST", restaurant, listOf(FoodLine(dish)), address, FoodPayment.CASH, "", FoodFees.bill(listOf(FoodLine(dish))))
        var state = ready.copy(order = order)
        repeat(3) { state = FoodReducer.reduce(state, FoodIntent.AdvanceTracking) }
        assertEquals(TrackingStage.DELIVERED, state.trackingStage)
        state = FoodReducer.reduce(state, FoodIntent.TrackingTick)
        assertEquals(TrackingStage.DELIVERED, state.trackingStage)
        assertEquals(0, state.etaSeconds)
        state = FoodReducer.reduce(state, FoodIntent.NewOrder)
        assertNull(state.order)
        assertEquals(TrackingStage.PLACED, state.trackingStage)
    }
}
