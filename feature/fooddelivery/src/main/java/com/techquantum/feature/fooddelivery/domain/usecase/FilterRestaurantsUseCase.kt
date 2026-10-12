package com.techquantum.feature.fooddelivery.domain.usecase

import com.techquantum.feature.fooddelivery.domain.Cuisine
import com.techquantum.feature.fooddelivery.domain.Restaurant
import javax.inject.Inject

/**
 * Single-responsibility use case to filter restaurants by cuisine and query.
 */
class FilterRestaurantsUseCase @Inject constructor() {

    operator fun invoke(
        restaurants: List<Restaurant>,
        cuisine: Cuisine,
        query: String
    ): List<Restaurant> {
        val search = query.trim()
        return restaurants.filter { restaurant ->
            (cuisine == Cuisine.ALL || restaurant.cuisine == cuisine) &&
                (search.isBlank() || listOf(
                    restaurant.name,
                    restaurant.cuisine.label,
                    restaurant.description
                ).any { text -> text.contains(search, ignoreCase = true) })
        }
    }
}
