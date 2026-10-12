package com.techquantum.tqdappsample

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

/** Exercises the actual Activity, Hilt graph, nested navigation and local order side effect. */
class EcommerceFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun discoverToOrderConfirmation_validatesThenRecordsTheDemoOrder() {
        compose.onNodeWithTag("discover_screen").assertIsDisplayed()
        awaitCatalog()
        compose.onAllNodesWithText("Explore")[0].performClick()
        compose.onNodeWithTag("product_listing_screen").assertIsDisplayed()
        compose.onNodeWithText("Gulmohar Silk Kurta").performScrollTo().performClick()
        compose.onNodeWithTag("product_detail_screen").assertIsDisplayed()
        compose.onNodeWithText("Buy now").performClick()
        compose.onNodeWithText("Your bag").assertIsDisplayed()
        compose.onNodeWithContentDescription("Increase quantity").performClick()
        compose.onNodeWithContentDescription("Quantity 2").assertIsDisplayed()
        compose.onNodeWithText("Continue to checkout").performClick()
        compose.onNodeWithText("Place demo order").performClick()
        compose.onNodeWithText("Enter your full name.").assertIsDisplayed()
        fill("Full name", "Demo Shopper")
        fill("Mobile number", "9876543210")
        fill("House, street and locality", "12 Sample Road")
        fill("City", "Ahmedabad")
        fill("PIN code", "380001")
        compose.onNodeWithText("Place demo order").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Order confirmed").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Order placed successfully").assertIsDisplayed()
        compose.onNodeWithText("2 pieces · Cash on delivery").assertIsDisplayed()
        compose.onNodeWithText("Continue shopping").performScrollTo().performClick()
        compose.onNodeWithTag("discover_screen").assertIsDisplayed()
        compose.onNodeWithContentDescription("Your bag, 0 items").assertExists()
    }

    @Test
    fun changingThemeKeepsTheCurrentRouteAndOtherModuleAccentsSeparate() {
        awaitCatalog()
        compose.onAllNodesWithText("Explore")[0].performClick()
        compose.onNodeWithContentDescription("Change fashion theme").performClick()
        compose.onNodeWithText("Athleisure Wear").performScrollTo().performClick()
        compose.onNodeWithText("Light", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Done").assertIsDisplayed().performClick()
        compose.onNodeWithTag("product_listing_screen").assertIsDisplayed()
        compose.onNodeWithText("Food").performClick()
        compose.onNodeWithTag("food_home").assertIsDisplayed()
        compose.onNodeWithText("Shop").performClick()
        compose.onNodeWithTag("product_listing_screen").assertIsDisplayed()
    }

    private fun fill(label: String, value: String) {
        compose.onNodeWithText(label).performScrollTo().performTextInput(value)
    }

    private fun awaitCatalog() {
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Explore").fetchSemanticsNodes().isNotEmpty() }
    }
}
