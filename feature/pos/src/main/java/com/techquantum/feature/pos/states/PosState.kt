package com.techquantum.feature.pos.states

import com.techquantum.feature.pos.presentation.PosIntent as PresentationPosIntent
import com.techquantum.feature.pos.presentation.PosState as PresentationPosState

/**
 * Re-export POS MVI states adhering to feature structure blueprint.
 */
typealias PosState = PresentationPosState
typealias PosIntent = PresentationPosIntent
