package com.techquantum.feature.billing.states

import com.techquantum.feature.billing.presentation.BillingEffect as PresentationBillingEffect
import com.techquantum.feature.billing.presentation.BillingIntent as PresentationBillingIntent
import com.techquantum.feature.billing.presentation.BillingOutput as PresentationBillingOutput
import com.techquantum.feature.billing.presentation.BillingOutputState as PresentationBillingOutputState
import com.techquantum.feature.billing.presentation.BillingOutputStatus as PresentationBillingOutputStatus
import com.techquantum.feature.billing.presentation.BillingState as PresentationBillingState

/**
 * Re-export Billing MVI states adhering to feature structure blueprint.
 */
typealias BillingState = PresentationBillingState
typealias BillingIntent = PresentationBillingIntent
typealias BillingEffect = PresentationBillingEffect
typealias BillingOutput = PresentationBillingOutput
typealias BillingOutputState = PresentationBillingOutputState
typealias BillingOutputStatus = PresentationBillingOutputStatus
