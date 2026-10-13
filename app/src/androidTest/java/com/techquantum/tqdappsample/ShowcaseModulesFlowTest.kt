package com.techquantum.tqdappsample

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

/** Uses the actual Activity and Hilt graph to verify cross-feature state and local side effects. */
class ShowcaseModulesFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun unmatchedFoodSearchKeepsTheScreenAndOffersClearFilters() {
        compose.onNodeWithText("Food").performClick()
        compose.onNodeWithTag("food_search").performTextInput("zzzzz")
        compose.onNodeWithTag("food_search").performImeAction()
        compose.onNodeWithTag("food_home").performScrollToIndex(5)
        compose.waitUntil(8_000) { compose.onAllNodesWithText("Clear filters").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Clear filters").performScrollTo().performClick()
        waitForText("Surati Rasoi")
        compose.onNodeWithTag("food_home").assertIsDisplayed()
    }

    @Test
    fun foodCustomizationGuestCheckoutAndLiveTracking() {
        compose.onNodeWithText("Food").performClick()
        compose.onNodeWithTag("food_home").assertIsDisplayed()
        waitForText("Surati Rasoi")
        compose.onNodeWithText("Surati Rasoi").performScrollTo().performClick()
        compose.onNodeWithTag("food_menu_list").performScrollToIndex(3)
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
            compose.onNodeWithTag("food_tracking").performScrollToIndex(3)
            val preview = compose.onAllNodesWithText("Preview next update")
            if (preview.fetchSemanticsNodes().isNotEmpty()) preview[0].performScrollTo().performClick()
        }
        compose.onNodeWithText("Order something else").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun paletteCanTemporarilyReplaceTheFoodEditorWithoutLosingItsDraft() {
        compose.onNodeWithText("Food").performClick()
        waitForText("Surati Rasoi")
        compose.onNodeWithText("Surati Rasoi").performScrollTo().performClick()
        compose.onNodeWithTag("food_menu_list").performScrollToIndex(3)
        compose.onAllNodesWithText("Customise")[0].performClick()
        compose.onNodeWithText("Extra paneer").performScrollTo().performClick()
        compose.onNodeWithText("Note for the kitchen").performScrollTo().performTextInput("Less spice")
        val palettes = compose.onAllNodesWithContentDescription("Color palette")
        palettes[palettes.fetchSemanticsNodes().lastIndex].performScrollTo().performClick()
        compose.onNodeWithText("Module default").performScrollTo().performClick()
        compose.onNodeWithText("Done").performClick()
        compose.onNodeWithText("Less spice").performScrollTo().assertExists()
        compose.onNodeWithText("Add to order · ₹289").performScrollTo().assertExists()
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
        if (text == "Surati Rasoi") compose.onNodeWithTag("food_home").performScrollToIndex(6)
        compose.waitUntil(8_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test
    fun chargeDiscountUpdatesTheTotalAndSharedBillingState() {
        compose.onNodeWithText("POS").performClick()
        waitForDescription("Add Masala Dosa")
        compose.onNodeWithContentDescription("Add Masala Dosa").performScrollTo().performClick()
        compose.onNodeWithText("Charge ₹126").performClick()
        compose.onNodeWithTag("charge_discount_10").performScrollTo().performClick()
        compose.onNodeWithText("Confirm UPI · ₹113").assertIsDisplayed()
        compose.onNodeWithText("Billing").performClick()
        compose.onNodeWithContentDescription("Open running bill").performClick()
        compose.onNodeWithTag("billing_running_list").performScrollToIndex(4)
        compose.onNodeWithText("Discount (10%)").performScrollTo().assertExists()
        compose.onNodeWithText("Preview receipt").performClick()
        compose.onNodeWithText("₹113").assertExists()
        compose.onNodeWithText("POS").performClick()
        compose.onNodeWithText("Confirm UPI · ₹113").performClick()
        compose.waitUntil(8_000) { compose.onAllNodesWithTag("pos_receipt").fetchSemanticsNodes().isNotEmpty() }
    }

    private fun waitForDescription(description: String) {
        compose.waitUntil(8_000) { compose.onAllNodesWithContentDescription(description).fetchSemanticsNodes().isNotEmpty() }
    }
}
