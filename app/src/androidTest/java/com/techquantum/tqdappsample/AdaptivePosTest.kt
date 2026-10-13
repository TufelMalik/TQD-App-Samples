package com.techquantum.tqdappsample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.techquantum.core.billingengine.BillCatalogue
import com.techquantum.core.billingengine.BillState
import com.techquantum.core.designsystem.ShowcaseModule
import com.techquantum.core.designsystem.ShowcaseTheme
import com.techquantum.feature.pos.presentation.PosState
import com.techquantum.feature.pos.ui.PosWorkspaceScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AdaptivePosTest {
    @get:Rule val compose = createComposeRule()

    @Test fun everyWorkspaceBreakpointMountsExactlyOneMenu() {
        var width by mutableStateOf(360)
        var height by mutableStateOf(800)
        var pixelsPerDp = 1f
        compose.setContent {
            ShowcaseTheme(ShowcaseModule.POS, false) {
                pixelsPerDp = LocalDensity.current.density
                Box(Modifier.requiredSize(width.dp, height.dp)) {
                    PosWorkspaceScreen(PosState(BillState(BillCatalogue.items), loading = false), {}, {})
                }
            }
        }
        listOf(360 to 800, 839 to 800, 840 to 800, 841 to 800, 840 to 599).forEach { (w, h) ->
            compose.runOnIdle { width = w; height = h }
            compose.onAllNodesWithContentDescription("Add Masala Dosa").assertCountEquals(1)
            compose.onAllNodesWithText("Counter 01").assertCountEquals(1)
            if (w >= 840 && h >= 600) {
                val headerHeight = compose.onNodeWithTag("pos_header").fetchSemanticsNode().boundsInRoot.height
                assertTrue("Expanded header must leave room for the workspace", headerHeight <= 140f * pixelsPerDp)
            }
        }
    }
}
