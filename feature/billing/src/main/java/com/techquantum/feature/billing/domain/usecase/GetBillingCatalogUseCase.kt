package com.techquantum.feature.billing.domain.usecase

import com.techquantum.core.billingengine.MenuItem
import javax.inject.Inject

/**
 * Single-responsibility use case to filter items in the billing catalog.
 */
class GetBillingCatalogUseCase @Inject constructor() {

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
