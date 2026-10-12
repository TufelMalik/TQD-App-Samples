package com.techquantum.feature.pos.domain.usecase

import com.techquantum.feature.pos.domain.PosRepository
import javax.inject.Inject

/**
 * Single-responsibility use case to manage ongoing POS bill operations.
 */
class ManagePosBillUseCase @Inject constructor(
    private val repository: PosRepository
) {
    fun addItem(id: String) {
        repository.add(id)
    }

    fun changeQuantity(id: String, delta: Int) {
        repository.changeQuantity(id, delta)
    }

    fun removeItem(id: String) {
        repository.remove(id)
    }

    fun applyDiscount(percent: Int) {
        repository.applyDiscount(percent)
    }

    fun selectTable(table: String) {
        repository.selectTable(table)
    }
}
