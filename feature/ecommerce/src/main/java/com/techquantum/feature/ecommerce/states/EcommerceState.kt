package com.techquantum.feature.ecommerce.states

import com.techquantum.feature.ecommerce.presentation.EcommerceEffect as ContractEcommerceEffect
import com.techquantum.feature.ecommerce.presentation.EcommerceIntent as ContractEcommerceIntent
import com.techquantum.feature.ecommerce.presentation.EcommerceState as ContractEcommerceState

/**
 * Re-export MVI contracts from states package adhering to feature structure blueprint.
 */
typealias EcommerceState = ContractEcommerceState
typealias EcommerceIntent = ContractEcommerceIntent
typealias EcommerceEffect = ContractEcommerceEffect
