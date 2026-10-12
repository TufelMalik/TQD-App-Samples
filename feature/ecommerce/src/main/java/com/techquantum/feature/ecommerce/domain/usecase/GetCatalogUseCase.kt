package com.techquantum.feature.ecommerce.domain.usecase

import com.techquantum.feature.ecommerce.domain.EcommerceRepository
import com.techquantum.feature.ecommerce.domain.Product
import javax.inject.Inject

/**
 * Single-responsibility use case to fetch the luxury product catalog.
 * Follows Single Responsibility Principle (SRP) and Dependency Inversion (DIP).
 */
class GetCatalogUseCase @Inject constructor(
    private val repository: EcommerceRepository
) {
    suspend operator fun invoke(): List<Product> = repository.products()
}
