package com.techquantum.feature.ecommerce.domain.usecase

import com.techquantum.feature.ecommerce.domain.FashionCategory
import com.techquantum.feature.ecommerce.domain.Product
import com.techquantum.feature.ecommerce.domain.ProductSort
import javax.inject.Inject

/**
 * Single-responsibility use case to filter and sort products.
 */
class FilterProductsUseCase @Inject constructor() {

    operator fun invoke(
        products: List<Product>,
        query: String,
        category: FashionCategory,
        maxPrice: Int,
        sort: ProductSort,
    ): List<Product> {
        val search = query.trim()
        val matches = products.filter { product ->
            (category == FashionCategory.ALL || product.category == category) &&
                product.price <= maxPrice &&
                (search.isBlank() || listOf(
                    product.name,
                    product.subtitle,
                    product.category.label,
                ).any { it.contains(search, ignoreCase = true) })
        }
        return when (sort) {
            ProductSort.FEATURED -> matches
            ProductSort.PRICE_LOW -> matches.sortedBy(Product::price)
            ProductSort.PRICE_HIGH -> matches.sortedByDescending(Product::price)
        }
    }
}
