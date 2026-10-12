package com.techquantum.feature.ecommerce.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.techquantum.core.designsystem.FashionStoreTheme
import com.techquantum.core.designsystem.FashionTheme
import com.techquantum.core.designsystem.FashionThemePicker
import com.techquantum.feature.ecommerce.data.FashionCatalog
import com.techquantum.feature.ecommerce.domain.FashionCategory
import com.techquantum.feature.ecommerce.domain.ProductSort
import com.techquantum.feature.ecommerce.presentation.EcommerceIntent
import com.techquantum.feature.ecommerce.presentation.EcommerceState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CatalogScreensTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun failedCollectionOffersRetryOnDiscoverAndListing() {
        var discover by mutableStateOf(true)
        var retried = 0
        val failed = EcommerceState(loading = false, error = "Couldn't load this edit. Please try again.")
        compose.setContent {
            FashionStoreTheme(darkTheme = false) {
                Surface {
                    val onIntent: (EcommerceIntent) -> Unit = { if (it == EcommerceIntent.Retry) retried += 1 }
                    if (discover) DiscoverScreen(failed, onIntent, {}, {}, { Modifier })
                    else ProductListingScreen(failed, onIntent, {}, { Modifier })
                }
            }
        }
        compose.onNodeWithText("Try again").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, retried); discover = false }
        compose.onNodeWithText("Try again").performClick()
        compose.runOnIdle { assertEquals(2, retried) }
    }

    @Test
    fun noResultsCanRecoverToTheFullCollection() {
        var state by mutableStateOf(EcommerceState(products = FashionCatalog.products, loading = false))
        compose.setContent {
            FashionStoreTheme(darkTheme = false) {
                Surface {
                    ProductListingScreen(state, { state = state.reduceCatalog(it) }, {}, { Modifier })
                }
            }
        }

        compose.onNodeWithTag("catalog_search").performTextInput("no matching garment")
        compose.onNodeWithText("Let's find a better fit").assertIsDisplayed()
        compose.onNodeWithText("Clear filters").performClick()
        compose.onNodeWithText("15 pieces").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals("", state.query)
            assertEquals(FashionCategory.ALL, state.category)
            assertEquals(15, state.filteredProducts.size)
        }
    }

    @Test
    fun filterSheetChangesPriceAndSortAndResetRecoversResults() {
        var state by mutableStateOf(EcommerceState(products = FashionCatalog.products, loading = false))
        compose.setContent {
            FashionStoreTheme(darkTheme = true) {
                Surface {
                    ProductListingScreen(state, { state = state.reduceCatalog(it) }, {}, { Modifier })
                }
            }
        }

        compose.onNodeWithText("Filter & sort").performClick()
        compose.onNodeWithText("Price: low to high").performClick()
        compose.onNodeWithText("Up to ₹1,500").performClick()
        compose.runOnIdle {
            assertEquals(ProductSort.PRICE_LOW, state.sort)
            assertEquals(listOf(899, 1499), state.filteredProducts.map { it.price })
        }
        compose.onNodeWithText("Show 2 pieces").performClick()
        compose.onNodeWithText("2 pieces").assertIsDisplayed()
        compose.onNodeWithText("Filter & sort").performClick()
        compose.onNodeWithText("Reset").performScrollTo().performClick()
        compose.onNodeWithText("Show 15 pieces").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(ProductSort.FEATURED, state.sort)
            assertEquals(10_000, state.maxPrice)
        }
    }

    @Test
    fun selectedVariantSurvivesAllTenLooksAndAddsTheChosenItem() {
        val product = FashionCatalog.products.first()
        var theme by mutableStateOf(FashionTheme.LUXURY_ETHNIC)
        var dark by mutableStateOf(false)
        var added: EcommerceIntent.AddToCart? = null
        compose.setContent {
            FashionStoreTheme(theme, dark) {
                Surface {
                    ProductDetailScreen(
                        product = product,
                        state = EcommerceState(products = FashionCatalog.products, loading = false),
                        onIntent = { if (it is EcommerceIntent.AddToCart) added = it },
                        onCart = {}, heroModifier = Modifier,
                    )
                }
            }
        }

        compose.onNodeWithText("Colour: Maroon").performScrollTo()
        compose.onNodeWithText("Ivory").performClick()
        compose.onNodeWithContentDescription("Size L").performClick()
        compose.onNodeWithContentDescription("Size XS, unavailable").assertIsNotEnabled()
        FashionTheme.entries.forEach { nextTheme ->
            listOf(false, true).forEach { nextDark ->
                compose.runOnIdle { theme = nextTheme; dark = nextDark }
                compose.onNodeWithText("Ivory / L · Ready to add").assertIsDisplayed()
                compose.onNodeWithText("Add to bag").assertIsDisplayed()
            }
        }
        compose.onNodeWithText("Add to bag").performClick()
        compose.runOnIdle { assertEquals(EcommerceIntent.AddToCart(product.id, "Ivory", "L"), added) }
    }

    @Test
    fun themePickerSelectsEveryThemeAndBothBrightnessModes() {
        var selected by mutableStateOf(FashionTheme.LUXEVA)
        var dark by mutableStateOf(false)
        compose.setContent {
            FashionStoreTheme(selected, dark) {
                FashionThemePicker(selected, dark, { selected = it }, { dark = it }, {})
            }
        }

        FashionTheme.entries.forEach { theme ->
            compose.onNodeWithText(theme.displayName).performScrollTo().performClick()
            compose.onNodeWithText(theme.displayName).assertIsSelected()
            compose.runOnIdle { assertEquals(theme, selected) }
        }
        compose.onNodeWithText("Dark").assertIsDisplayed().performClick().assertIsSelected()
        compose.onNodeWithText("Done").assertIsDisplayed()
        compose.runOnIdle { assertEquals(true, dark) }
        compose.onNodeWithText("Light").performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(false, dark) }
    }
}

/** Test host updates controlled screen state in response to the same public intents as the ViewModel. */
private fun EcommerceState.reduceCatalog(intent: EcommerceIntent): EcommerceState = when (intent) {
    is EcommerceIntent.Search -> copy(query = intent.query)
    is EcommerceIntent.SelectCategory -> copy(category = intent.category)
    is EcommerceIntent.SetSort -> copy(sort = intent.sort)
    is EcommerceIntent.SetMaxPrice -> copy(maxPrice = intent.price)
    EcommerceIntent.ToggleGrid -> copy(gridView = !gridView)
    else -> this
}
