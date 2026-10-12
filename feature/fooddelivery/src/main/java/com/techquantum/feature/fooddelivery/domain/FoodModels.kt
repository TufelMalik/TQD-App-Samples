package com.techquantum.feature.fooddelivery.domain

import java.io.Serializable

enum class Cuisine(val label: String) {
    ALL("All"), GUJARATI("Gujarati"), SOUTH_INDIAN("South Indian"), PIZZA("Pizza"), HEALTHY("Healthy")
}
enum class MenuCategory(val label: String) { POPULAR("Popular"), MAINS("Mains"), SIDES("Sides"), DRINKS("Drinks") }
enum class FoodArt { THALI, DOSA, PIZZA, BOWL, WRAP, DESSERT, DRINK }
data class AddOn(val id: String, val name: String, val price: Int) : Serializable
data class Dish(
    val id: String, val restaurantId: String, val name: String, val description: String,
    val price: Int, val category: MenuCategory, val vegetarian: Boolean = true,
    val spice: Int = 1, val artwork: FoodArt, val addOns: List<AddOn> = emptyList(),
) : Serializable
data class Restaurant(
    val id: String, val name: String, val description: String, val cuisine: Cuisine,
    val rating: Double, val reviewCount: Int, val etaMinutes: Int, val distanceKm: Double,
    val artwork: FoodArt, val dishes: List<Dish>,
) : Serializable
data class FoodLine(val dish: Dish, val addOns: List<AddOn> = emptyList(), val note: String = "", val quantity: Int = 1) : Serializable {
    val key: String get() = "${dish.id}|${addOns.map { it.id }.sorted().joinToString(",")}|$note"
    val unitPrice: Int get() = dish.price + addOns.sumOf(AddOn::price)
    val total: Int get() = unitPrice * quantity
}
data class FoodAddress(val name: String = "", val phone: String = "", val line: String = "", val city: String = "Surat", val pinCode: String = "") : Serializable
enum class FoodPayment(val label: String) { UPI("UPI"), CARD("Debit / credit card"), CASH("Cash on delivery") }
enum class TrackingStage(val label: String, val detail: String) {
    PLACED("Placed", "Your order is with the kitchen."),
    PREPARING("Preparing", "Freshly made, just for you."),
    OUT_FOR_DELIVERY("Out for delivery", "Your rider is on the way."),
    DELIVERED("Delivered", "Enjoy every bite."),
}
data class FoodBill(val subtotal: Int, val tax: Int, val delivery: Int, val platform: Int, val tip: Int) : Serializable {
    val total: Int get() = subtotal + tax + delivery + platform + tip
}
object FoodFees {
    const val FREE_DELIVERY_FROM = 499
    fun bill(lines: List<FoodLine>, tip: Int = 0): FoodBill {
        val subtotal = lines.sumOf(FoodLine::total)
        if (subtotal == 0) return FoodBill(0, 0, 0, 0, 0)
        return FoodBill(subtotal, (subtotal * 5 + 50) / 100, if (subtotal >= FREE_DELIVERY_FROM) 0 else 29, 5, tip.coerceIn(0, 100))
    }
}
data class FoodOrder(
    val id: String, val restaurant: Restaurant, val lines: List<FoodLine>, val address: FoodAddress,
    val payment: FoodPayment, val instructions: String, val bill: FoodBill,
) : Serializable
data class PendingDish(val dishId: String, val addOnIds: Set<String>, val note: String) : Serializable

interface FoodRepository {
    suspend fun restaurants(): List<Restaurant>
    suspend fun placeOrder(restaurant: Restaurant, lines: List<FoodLine>, address: FoodAddress, payment: FoodPayment, instructions: String, tip: Int): FoodOrder
}
