package com.techquantum.tqdappsample

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

/** Uses the actual Activity and Hilt graph to verify cross-feature state and local side effects. */
class ShowcaseModulesFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun foodCustomizationGuestCheckoutAndLiveTracking() {
        compose.onNodeWithText("Food").performClick()
        compose.onNodeWithTag("food_home").assertIsDisplayed()
        waitForText("Surati Rasoi")
        compose.onNodeWithText("Surati Rasoi").performScrollTo().performClick()
        compose.onNodeWithTag("food_menu").assertIsDisplayed()
        compose.onNodeWithText("Surati Special Thali").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("Customise")[0].performClick()
        compose.onNodeWithText("Extra paneer").performScrollTo().performClick()
        compose.onNodeWithText("Add to order · ₹289").performScrollTo().performClick()
        compose.onNodeWithText("View order").performClick()
        compose.onNodeWithTag("food_cart").assertIsDisplayed()
        compose.onNodeWithText("Extra paneer", substring = true).assertExists()
        compose.onNodeWithText("Continue").performClick()
        compose.onNodeWithTag("food_checkout").assertIsDisplayed()
        compose.onNodeWithText("Use sample address").performScrollTo().performClick()
        compose.onNodeWithText("Place order").performClick()
        compose.waitUntil(8_000) { compose.onAllNodesWithTag("food_tracking").fetchSemanticsNodes().isNotEmpty() }
        repeat(3) {
            val preview = compose.onAllNodesWithText("Preview next update")
            if (preview.fetchSemanticsNodes().isNotEmpty()) preview[0].performScrollTo().performClick()
        }
        compose.onNodeWithText("Order something else").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun posAndBillingShareTheBillThenRecordPaymentAndPrint() {
        compose.onNodeWithText("POS").performClick()
        compose.onNodeWithTag("pos_workspace").assertIsDisplayed()
        waitForDescription("Add Masala Dosa")
        compose.onNodeWithContentDescription("Add Masala Dosa").performScrollTo().performClick()
        compose.onNodeWithText("Billing").performClick()
        compose.onNodeWithTag("billing_selection").assertIsDisplayed()
        compose.onNodeWithContentDescription("Open running bill").performClick()
        compose.onNodeWithTag("billing_running_bill").assertIsDisplayed()
        compose.onNodeWithText("Masala Dosa").assertExists()
        compose.onNodeWithText("POS").performClick()
        compose.onNodeWithText("Review 1 items · ₹126").performClick()
        compose.onNodeWithText("Charge ₹126").performClick()
        compose.onNodeWithTag("pos_charge").assertIsDisplayed()
        compose.onNodeWithText("Confirm UPI · ₹126").performClick()
        compose.waitUntil(8_000) { compose.onAllNodesWithTag("pos_receipt").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Payment recorded").assertIsDisplayed()
        compose.onNodeWithText("Start new bill").performScrollTo().performClick()
        compose.onNodeWithText("Billing").performClick()
        compose.onNodeWithText("Select food").performClick()
        compose.onNodeWithTag("bill-add-masala-dosa").performScrollTo().performClick()
        compose.onNodeWithText("Review bill").performClick()
        compose.onNodeWithText("Preview receipt").performClick()
        compose.onNodeWithTag("billing_preview").assertIsDisplayed()
        compose.onNodeWithText("Print demo bill").performClick()
        compose.waitUntil(8_000) { compose.onAllNodesWithTag("billing_print_success").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("printed", substring = true).assertExists()
    }

    private fun waitForText(text: String) {
        compose.waitUntil(8_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun waitForDescription(description: String) {
        compose.waitUntil(8_000) { compose.onAllNodesWithContentDescription(description).fetchSemanticsNodes().isNotEmpty() }
    }
}
