package com.techquantum.feature.pos.domain.usecase

import com.techquantum.core.billingengine.MenuItem
import javax.inject.Inject

/**
 * Single-responsibility use case to filter POS catalog items.
 */
class GetPosCatalogUseCase @Inject constructor() {

    operator fun invoke(
        items: List<MenuItem>,
        category: String,
        query: String
    ): List<MenuItem> {
        val search = query.trim()
        return items.filter { item ->
            (category == "All" || item.category == category) &&
                (search.isBlank() || item.name.contains(search, ignoreCase = true))
        }
    }
}
