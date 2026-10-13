@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
package com.techquantum.feature.ecommerce.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.techquantum.core.designsystem.*
import com.techquantum.feature.ecommerce.domain.*
import com.techquantum.feature.ecommerce.presentation.*
import com.techquantum.core.designsystem.R as DesignR

@Composable
internal fun StudioDiscover(state: EcommerceState, onIntent: (EcommerceIntent) -> Unit,
    onBrowse: (FashionCategory) -> Unit, onProduct: (String) -> Unit,
    heroModifier: @Composable (String) -> Modifier, modifier: Modifier) {
    val large = LocalDensity.current.fontScale > 1.25f
    LazyColumn(modifier.fillMaxSize().testTag("discover_screen"), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item(key = "welcome", contentType = "header") {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(stringResource(DesignR.string.studio_greeting), style = MaterialTheme.typography.headlineMedium)
                CatalogSearch(state.query, { onIntent(EcommerceIntent.Search(it)) }, { onBrowse(FashionCategory.ALL) })
            }
        }
        when {
            state.error != null && state.products.isEmpty() && !state.loading -> item(key = "error", contentType = "error") {
                CatalogFailurePanel(state.error, { onIntent(EcommerceIntent.Retry) })
            }
            state.loading && state.products.isEmpty() -> item(key = "loading", contentType = "skeleton") {
                SkeletonShimmer(Modifier.fillMaxWidth().height(300.dp))
            }
            else -> {
                item(key = "collections", contentType = "bento") {
                    val categories = listOf(FashionCategory.ETHNIC, FashionCategory.MEN, FashionCategory.ACTIVE)
                    val captions = listOf(DesignR.string.studio_ethnic, DesignR.string.studio_casual, DesignR.string.studio_active)
                    if (large) Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        categories.forEachIndexed { index, category ->
                            StudioCollection(category, index, stringResource(captions[index]), { onBrowse(category) }, Modifier.fillMaxWidth().heightIn(min = 170.dp))
                        }
                    } else Row(Modifier.fillMaxWidth().height(302.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StudioCollection(categories[0], 0, stringResource(captions[0]), { onBrowse(categories[0]) }, Modifier.weight(1f).fillMaxHeight())
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            StudioCollection(categories[1], 1, stringResource(captions[1]), { onBrowse(categories[1]) }, Modifier.weight(1f).fillMaxWidth())
                            StudioCollection(categories[2], 4, stringResource(captions[2]), { onBrowse(categories[2]) }, Modifier.weight(1f).fillMaxWidth())
                        }
                    }
                }
                item(key = "other-collections", contentType = "categories") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(FashionCategory.ALL, FashionCategory.WOMEN, FashionCategory.STREETWEAR), key = { it.name }, contentType = { "category" }) { category ->
                            AssistChip(onClick = { onBrowse(category) }, label = { Text(category.label) }, modifier = Modifier.heightIn(min = 48.dp))
                        }
                    }
                }
                item(key = "trending-heading", contentType = "heading") {
                    SectionHeading(stringResource(DesignR.string.studio_trending), "View all", { onBrowse(FashionCategory.ALL) })
                }
                item(key = "trending", contentType = "product-rail") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.products.take(8), key = { it.id }, contentType = { "product" }) { product ->
                            ProductCard(product, product.id in state.wishlist, { onProduct(product.id) },
                                { onIntent(EcommerceIntent.ToggleWishlist(product.id)) }, heroModifier(product.id), Modifier.width(if (large) 240.dp else 168.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioCollection(category: FashionCategory, cell: Int, caption: String, onClick: () -> Unit, modifier: Modifier) {
    Surface(onClick, modifier, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Box {
            EditorialArtwork(true, cell, null, Modifier.fillMaxSize())
            Surface(Modifier.align(Alignment.BottomStart).fillMaxWidth(), color = MaterialTheme.colorScheme.surface.copy(alpha = .94f)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(category.label, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
                    }
                    Text(caption, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
internal fun StudioProductDetail(product: Product, state: EcommerceState, onIntent: (EcommerceIntent) -> Unit,
    onCart: () -> Unit, heroModifier: Modifier, modifier: Modifier) {
    var colour by rememberSaveable(product.id) { mutableStateOf(product.colors.first()) }
    var size by rememberSaveable(product.id) { mutableStateOf(product.sizes.firstOrNull { it == "M" } ?: product.sizes.first()) }
    var view by rememberSaveable(product.id) { mutableIntStateOf(0) }
    var details by rememberSaveable(product.id) { mutableStateOf(false) }
    var care by rememberSaveable(product.id) { mutableStateOf(false) }
    val large = LocalDensity.current.fontScale > 1.25f
    val add = { onIntent(EcommerceIntent.AddToCart(product.id, colour, size)) }
    Column(modifier.fillMaxSize().testTag("product_detail_screen")) {
        BoxWithConstraints(Modifier.weight(1f)) {
        val heroHeight = maxHeight.coerceAtMost(if (large) 350.dp else 440.dp)
        LazyColumn(Modifier.fillMaxSize().testTag("product_detail_list"), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item(key = "editorial", contentType = "hero") {
                Box(Modifier.fillMaxWidth().height(heroHeight).clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))) {
                    if (view == 2) ProductArtwork(product, heroModifier.fillMaxSize(), product.colors.indexOf(colour))
                    else EditorialArtwork(true, if (view == 1 && product.artwork == GarmentStyle.SHIRT) 5 else editorialCell(product),
                        product.name, heroModifier.fillMaxSize())
                    if (!large) {
                        Column(Modifier.align(Alignment.TopStart).padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            product.colors.forEach { value ->
                                val description = stringResource(DesignR.string.studio_variant, value)
                                FilledTonalIconButton(onClick = { colour = value; view = 2 }, Modifier.size(48.dp).semantics {
                                    contentDescription = description; selected = colour == value
                                }) { if (colour == value) Icon(Icons.Default.CheckCircle, null) else Text(value.take(1)) }
                            }
                        }
                        Column(Modifier.align(Alignment.TopEnd).padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(DesignR.string.studio_photo, DesignR.string.studio_garment, DesignR.string.studio_illustration).forEachIndexed { index, text ->
                                val description = stringResource(text)
                                FilledTonalButton({ view = index }, Modifier.width(96.dp).heightIn(min = 48.dp).semantics { selected = view == index },
                                    contentPadding = PaddingValues(8.dp)) { Text(description, style = MaterialTheme.typography.labelSmall) }
                            }
                        }
                    }
                    Surface(Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = .96f)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(product.name, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                                WishlistButton(product, product.id in state.wishlist, { onIntent(EcommerceIntent.ToggleWishlist(product.id)) })
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                AnimatedMoney(product.price)
                                RatingPill(product.rating, product.reviewCount)
                            }
                            Text(stringResource(DesignR.string.studio_taxes), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item(key = "options", contentType = "variant-controls") {
                Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(DesignR.string.studio_colour, colour), style = MaterialTheme.typography.titleMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(product.colors, key = { it }, contentType = { "colour" }) { value ->
                            FilterChip(colour == value, { colour = value; view = 2 }, label = { Text(value) }, modifier = Modifier.heightIn(min = 48.dp))
                        }
                    }
                    Text(stringResource(DesignR.string.studio_size, size), Modifier.testTag("product_size_heading"), style = MaterialTheme.typography.titleMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(if (product.sizes.all { it.toIntOrNull() != null }) product.sizes else listOf("XS", "S", "M", "L", "XL", "XXL"), key = { it }, contentType = { "size" }) { value ->
                            FilterChip(size == value, { size = value }, enabled = value in product.sizes,
                                label = { Text(value) }, modifier = Modifier.heightIn(min = 48.dp).semantics {
                                    contentDescription = "Size $value${if (value in product.sizes) "" else ", unavailable"}"
                                })
                        }
                    }
                    if (large) FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(DesignR.string.studio_photo, DesignR.string.studio_garment, DesignR.string.studio_illustration).forEachIndexed { index, label ->
                            FilterChip(view == index, { view = index }, label = { Text(stringResource(label)) }, modifier = Modifier.heightIn(min = 48.dp))
                        }
                    }
                    Text(stringResource(DesignR.string.studio_image_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item(key = "details", contentType = "information") {
                Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StudioAccordion(stringResource(DesignR.string.studio_details), product.description, details, { details = !details })
                    StudioAccordion(stringResource(DesignR.string.studio_fit), stringResource(DesignR.string.studio_fit_copy), care, { care = !care })
                }
            }
        }
        }
        ActionFooter {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text(stringResource(DesignR.string.studio_size, size), Modifier.padding(14.dp), style = MaterialTheme.typography.labelLarge)
                    }
                    Button(add, Modifier.weight(1f).heightIn(min = 54.dp), shape = RoundedCornerShape(999.dp)) {
                        Text(stringResource(DesignR.string.studio_bag))
                    }
                }
                TextButton({ onIntent(EcommerceIntent.AddToCart(product.id, colour, size, openCart = true)) }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(stringResource(DesignR.string.studio_buy))
                }
            }
        }
    }
}

@Composable
private fun StudioAccordion(title: String, copy: String, expanded: Boolean, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column {
            TextButton(onClick, Modifier.fillMaxWidth().heightIn(min = 52.dp).semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" }) {
                Text(title, Modifier.weight(1f)); Text(if (expanded) "−" else "+")
            }
            if (expanded) Text(copy, Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

internal fun editorialCell(product: Product): Int = when (product.category) {
    FashionCategory.ETHNIC -> 0
    FashionCategory.MEN -> 1
    FashionCategory.WOMEN -> 2
    FashionCategory.STREETWEAR -> 3
    FashionCategory.ACTIVE -> 4
    FashionCategory.ALL -> 5
}
