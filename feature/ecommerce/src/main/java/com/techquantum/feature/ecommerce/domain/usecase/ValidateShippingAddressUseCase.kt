package com.techquantum.feature.ecommerce.domain.usecase

import com.techquantum.feature.ecommerce.domain.ShippingAddress
import javax.inject.Inject

/**
 * Single-responsibility use case to validate shipping addresses.
 */
class ValidateShippingAddressUseCase @Inject constructor() {

    operator fun invoke(address: ShippingAddress): Map<String, String> = buildMap {
        if (address.name.trim().length < 2) put("name", "Enter your full name.")
        if (!address.phone.trim().matches(Regex("[6-9][0-9]{9}"))) {
            put("phone", "Enter a valid 10-digit Indian mobile number.")
        }
        if (address.line.trim().length < 6) put("line", "Enter your building and street.")
        if (address.city.trim().length < 2) put("city", "Enter your city.")
        if (!address.pinCode.trim().matches(Regex("[1-9][0-9]{5}"))) {
            put("pinCode", "Enter a valid 6-digit PIN code.")
        }
    }
}
