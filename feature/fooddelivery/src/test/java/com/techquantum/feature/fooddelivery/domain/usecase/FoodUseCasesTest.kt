package com.techquantum.feature.fooddelivery.domain.usecase

import com.techquantum.feature.fooddelivery.data.FoodCatalog
import com.techquantum.feature.fooddelivery.domain.Cuisine
import com.techquantum.feature.fooddelivery.domain.FoodAddress
import com.techquantum.feature.fooddelivery.domain.FoodLine
import com.techquantum.feature.fooddelivery.domain.FoodOrder
import com.techquantum.feature.fooddelivery.domain.FoodPayment
import com.techquantum.feature.fooddelivery.domain.FoodRepository
import com.techquantum.feature.fooddelivery.domain.Restaurant
import com.techquantum.feature.fooddelivery.domain.TrackingStage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodUseCasesTest {

    private val sampleRestaurants = FoodCatalog.restaurants

    private val fakeRepository = object : FoodRepository {
        override suspend fun restaurants(): List<Restaurant> = sampleRestaurants
        override suspend fun placeOrder(
            restaurant: Restaurant,
            lines: List<FoodLine>,
            address: FoodAddress,
            payment: FoodPayment,
            instructions: String,
            tip: Int
        ): FoodOrder {
            return FoodOrder(
                id = "TQ-TEST-99",
                restaurant = restaurant,
                lines = lines,
                address = address,
                payment = payment,
                instructions = instructions,
                bill = com.techquantum.feature.fooddelivery.domain.FoodFees.bill(lines, tip)
            )
        }
    }

    private val getRestaurantsUseCase = GetRestaurantsUseCase(fakeRepository)
    private val filterRestaurantsUseCase = FilterRestaurantsUseCase()
    private val placeFoodOrderUseCase = PlaceFoodOrderUseCase(fakeRepository)
    private val trackFoodOrderUseCase = TrackFoodOrderUseCase()

    @Test
    fun getRestaurantsReturnsAllKitchens() = runBlocking {
        val restaurants = getRestaurantsUseCase()
        assertTrue(restaurants.isNotEmpty())
        assertEquals(sampleRestaurants.size, restaurants.size)
    }

    @Test
    fun filterRestaurantsFiltersByCuisine() {
        val gujaratiOnly = filterRestaurantsUseCase(
            restaurants = sampleRestaurants,
            cuisine = Cuisine.GUJARATI,
            query = ""
        )
        assertTrue(gujaratiOnly.isNotEmpty())
        assertTrue(gujaratiOnly.all { it.cuisine == Cuisine.GUJARATI })
    }

    @Test
    fun filterRestaurantsFiltersBySearchQuery() {
        val results = filterRestaurantsUseCase(
            restaurants = sampleRestaurants,
            cuisine = Cuisine.ALL,
            query = "Surati Rasoi"
        )
        assertEquals(1, results.size)
        assertEquals("Surati Rasoi", results.first().name)
    }

    @Test
    fun trackOrderAdvancesThroughStagesProperly() {
        assertEquals(TrackingStage.PREPARING, trackFoodOrderUseCase.nextStage(TrackingStage.PLACED))
        assertEquals(TrackingStage.OUT_FOR_DELIVERY, trackFoodOrderUseCase.nextStage(TrackingStage.PREPARING))
        assertEquals(TrackingStage.DELIVERED, trackFoodOrderUseCase.nextStage(TrackingStage.OUT_FOR_DELIVERY))
        assertEquals(TrackingStage.DELIVERED, trackFoodOrderUseCase.nextStage(TrackingStage.DELIVERED))
    }

    @Test
    fun placeOrderCreatesValidFoodOrder() = runBlocking {
        val restaurant = sampleRestaurants.first()
        val dish = restaurant.dishes.first()
        val line = FoodLine(dish = dish, quantity = 2)
        val address = FoodAddress(name = "Customer", phone = "9999999999", line = "Ring Road")

        val order = placeFoodOrderUseCase(
            restaurant = restaurant,
            lines = listOf(line),
            address = address,
            payment = FoodPayment.UPI,
            instructions = "Extra napkins",
            tip = 20
        )

        assertEquals("TQ-TEST-99", order.id)
        assertEquals(restaurant.name, order.restaurant.name)
        assertEquals(1, order.lines.size)
        assertEquals(2, order.lines.first().quantity)
    }
}
