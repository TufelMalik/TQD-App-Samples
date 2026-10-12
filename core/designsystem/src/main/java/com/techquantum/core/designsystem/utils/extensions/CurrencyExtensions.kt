package com.techquantum.core.designsystem.utils.extensions

import java.text.NumberFormat
import java.util.Locale

/**
 * Currency and price formatting utilities following Indian Rupee (INR) numbering conventions.
 */
fun formatRupees(amount: Int): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).apply {
    maximumFractionDigits = 0
    minimumFractionDigits = 0
}.format(amount)

fun Int.toRupeesString(): String = formatRupees(this)
