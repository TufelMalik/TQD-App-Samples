@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package com.techquantum.feature.ecommerce.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techquantum.core.designsystem.ActionFooter
import com.techquantum.core.designsystem.AnimatedMoney
import com.techquantum.core.designsystem.DiscountPill
import com.techquantum.core.designsystem.LocalFashionTheme
import com.techquantum.core.designsystem.LuxuryBadge
import com.techquantum.core.designsystem.RatingPill
import com.techquantum.core.designsystem.ShowcaseSheet
import com.techquantum.core.designsystem.SkeletonShimmer
import com.techquantum.feature.ecommerce.domain.FashionCategory
import com.techquantum.feature.ecommerce.domain.Product
import com.techquantum.feature.ecommerce.domain.ProductSort
import com.techquantum.feature.ecommerce.presentation.EcommerceIntent
import com.techquantum.feature.ecommerce.presentation.EcommerceState
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DiscoverScreen(
    state: EcommerceState,
    onIntent: (EcommerceIntent) -> Unit,
    onBrowse: (FashionCategory) -> Unit,
    onProduct: (String) -> Unit,
    heroModifier: @Composable (String) -> Modifier,
    modifier: Modifier = Modifier,
) {
    val theme = LocalFashionTheme.current
    val enlargedType = LocalDensity.current.fontScale > 1.15f
    val catalogError = state.error?.takeIf { state.products.isEmpty() && !state.loading }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("discover_screen"),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Column(
                Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LuxuryBadge(
                    "Haute Couture · Autumn / Winter '26",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text("A wardrobe,\nuniquely yours.", style = MaterialTheme.typography.headlineLarge)
                Text(
                    theme.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CatalogSearch(
                    state.query,
                    { onIntent(EcommerceIntent.Search(it)) },
                    { onBrowse(FashionCategory.ALL) })
            }
        }
        if (catalogError != null) {
            item {
                CatalogFailurePanel(
                    catalogError,
                    { onIntent(EcommerceIntent.Retry) },
                    Modifier.padding(horizontal = 20.dp)
                )
            }
            return@LazyColumn
        }
        item {
            if (state.loading) {
                SkeletonShimmer(
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .height(290.dp)
                        .clip(RoundedCornerShape(32.dp))
                )
            } else {
                CampaignCarousel(state.products, onBrowse)
            }
        }
        item {
            Column(
                Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SectionHeading("Shop your style")
                if (enlargedType) {
                    listOf(
                        FashionCategory.ETHNIC to "For every celebration",
                        FashionCategory.MEN to "Everyday essentials",
                        FashionCategory.WOMEN to "A little elegance",
                        FashionCategory.STREETWEAR to "Make it yours",
                        FashionCategory.ACTIVE to "Move with ease",
                    ).forEach { (category, subtitle) ->
                        CategoryTile(
                            category,
                            null,
                            onBrowse,
                            Modifier.heightIn(min = 108.dp),
                            subtitle
                        )
                    }
                } else {
                    Row(
                        Modifier.height(214.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CategoryTile(
                            FashionCategory.ETHNIC,
                            state.products.firstOrNull { it.category == FashionCategory.ETHNIC },
                            onBrowse,
                            Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            "For every celebration"
                        )
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CategoryTile(
                                FashionCategory.MEN,
                                null,
                                onBrowse,
                                Modifier.weight(1f),
                                "Everyday essentials"
                            )
                            CategoryTile(
                                FashionCategory.WOMEN,
                                null,
                                onBrowse,
                                Modifier.weight(1f),
                                "A little elegance"
                            )
                        }
                    }
                    Row(
                        Modifier.height(108.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CategoryTile(
                            FashionCategory.STREETWEAR,
                            null,
                            onBrowse,
                            Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            "Make it yours"
                        )
                        CategoryTile(
                            FashionCategory.ACTIVE,
                            null,
                            onBrowse,
                            Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            "Move with ease"
                        )
                    }
                }
            }
        }
        item {
            SectionHeading(
                "For you",
                "View all",
                { onBrowse(FashionCategory.ALL) },
                Modifier.padding(horizontal = 20.dp)
            )
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (state.loading) {
                    items(4) { ProductSkeleton(Modifier.width(178.dp)) }
                } else {
                    items(state.products.take(8), key = { it.id }) { product ->
                        ProductCard(
                            product,
                            product.id in state.wishlist,
                            { onProduct(product.id) },
                            { onIntent(EcommerceIntent.ToggleWishlist(product.id)) },
                            heroModifier(product.id),
                            Modifier.width(178.dp)
                        )
                    }
                }
            }
        }
        item {
            Surface(
                Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.18f)
                ),
            ) {
                Row(
                    Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.LocalShipping,
                                null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    Column {
                        Text(
                            "A good fit, delivered.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                        Text(
                            "Free delivery on orders from ₹1,999",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CampaignCarousel(products: List<Product>, onBrowse: (FashionCategory) -> Unit) {
    val enlargedType = LocalDensity.current.fontScale > 1.15f
    val campaigns = remember(products) {
        listOf(
            Campaign(
                "Made for\nthe moments.",
                "The occasion edit",
                FashionCategory.ETHNIC,
                products.firstOrNull { it.category == FashionCategory.ETHNIC }),
            Campaign(
                "Everyday.\nExtraordinary.",
                "The city essentials",
                FashionCategory.MEN,
                products.firstOrNull { it.category == FashionCategory.MEN }),
            Campaign(
                "An effortless\nkind of lovely.",
                "The boutique edit",
                FashionCategory.WOMEN,
                products.firstOrNull { it.category == FashionCategory.WOMEN }),
        )
    }
    val pager = rememberPagerState { campaigns.size }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalPager(
            pager,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 14.dp
        ) { index ->
            val campaign = campaigns[index]
            Surface(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                shadowElevation = 2.dp,
            ) {
                Box {
                    campaign.product?.let { product ->
                        Box(Modifier.matchParentSize(), contentAlignment = Alignment.CenterEnd) {
                            ProductArtwork(
                                product,
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(if (enlargedType) .30f else .46f)
                            )
                        }
                    }
                    Column(
                        Modifier
                            .heightIn(min = 286.dp)
                            .fillMaxWidth(if (enlargedType) .78f else .60f)
                            .padding(22.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            LuxuryBadge(
                                campaign.subtitle,
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                campaign.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontSize = 29.sp,
                                lineHeight = 33.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { onBrowse(campaign.category) },
                            shape = RoundedCornerShape(999.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        ) {
                            Text("Explore", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.AutoMirrored.Default.ArrowForward,
                                null,
                                Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            repeat(campaigns.size) { index ->
                Box(
                    Modifier
                        .width(if (pager.currentPage == index) 26.dp else 8.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (pager.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.5f
                            )
                        )
                )
            }
        }
    }
}

private data class Campaign(
    val title: String,
    val subtitle: String,
    val category: FashionCategory,
    val product: Product?
)

@Composable
private fun CategoryTile(
    category: FashionCategory,
    product: Product?,
    onBrowse: (FashionCategory) -> Unit,
    modifier: Modifier,
    subtitle: String,
) {
    val colors = MaterialTheme.colorScheme
    val container = when (category) {
        FashionCategory.ETHNIC, FashionCategory.WOMEN -> colors.secondaryContainer
        FashionCategory.ACTIVE -> colors.tertiaryContainer
        else -> colors.surfaceContainerHigh
    }
    val textColor = when (category) {
        FashionCategory.ETHNIC, FashionCategory.WOMEN -> colors.onSecondaryContainer
        FashionCategory.ACTIVE -> colors.onTertiaryContainer
        else -> colors.onSurface
    }
    Surface(
        onClick = { onBrowse(category) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = container,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f)),
        shadowElevation = 1.dp,
    ) {
        Box {
            product?.let {
                ProductArtwork(
                    it,
                    Modifier
                        .align(Alignment.BottomEnd)
                        .height(145.dp)
                        .width(105.dp)
                        .offset(x = 12.dp, y = 22.dp)
                )
            }
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    category.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    subtitle, style = MaterialTheme.typography.bodySmall, color = textColor,
                    modifier = Modifier.fillMaxWidth(if (product == null) 1f else .72f)
                )
            }
            Surface(
                shape = CircleShape,
                color = colors.surface.copy(alpha = 0.45f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(32.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowForward,
                        null,
                        Modifier.size(16.dp),
                        tint = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun ProductListingScreen(
    state: EcommerceState,
    onIntent: (EcommerceIntent) -> Unit,
    onProduct: (String) -> Unit,
    heroModifier: @Composable (String) -> Modifier,
    modifier: Modifier = Modifier,
) {
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val motion = MaterialTheme.motionScheme
    val catalogError = state.error?.takeIf { state.products.isEmpty() && !state.loading }
    LazyVerticalGrid(
        columns = if (state.gridView) GridCells.Adaptive(if (LocalDensity.current.fontScale > 1.25f) 240.dp else 150.dp) else GridCells.Fixed(1),
        modifier = modifier
            .fillMaxSize()
            .testTag("product_listing_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    if (state.category == FashionCategory.ALL) "The whole collection" else "The ${state.category.label.lowercase()} edit",
                    style = MaterialTheme.typography.headlineMedium
                )
                CatalogSearch(state.query, { onIntent(EcommerceIntent.Search(it)) })
                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "${state.filteredProducts.size} pieces",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AssistChip(
                        onClick = { showFilters = true }, label = { Text("Filter & sort") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.FilterList,
                                null,
                                Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        })
                    FilledTonalIconButton(
                        onClick = { onIntent(EcommerceIntent.ToggleGrid) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            if (state.gridView) Icons.Default.ViewAgenda else Icons.Default.GridView,
                            if (state.gridView) "Show list view" else "Show grid view"
                        )
                    }
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(FashionCategory.entries) { category ->
                        FilterChip(
                            selected = state.category == category,
                            onClick = { onIntent(EcommerceIntent.SelectCategory(category)) },
                            label = { Text(category.label) })
                    }
                }
                HorizontalDivider(
                    Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f)
                )
            }
        }
            if (state.loading) {
                items(6) { ProductSkeleton(Modifier.fillMaxWidth()) }
            } else if (catalogError != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    CatalogFailurePanel(catalogError, { onIntent(EcommerceIntent.Retry) })
                }
            } else if (state.filteredProducts.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            null,
                            Modifier.size(44.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("Let's find a better fit", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Try a different search or clear your filters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FilledTonalButton(onClick = {
                            onIntent(EcommerceIntent.Search(""))
                            onIntent(EcommerceIntent.SelectCategory(FashionCategory.ALL))
                            onIntent(EcommerceIntent.SetMaxPrice(10_000))
                        }) { Text("Clear filters") }
                    }
                }
            } else {
                items(state.filteredProducts, key = { it.id }) { product ->
                    val itemModifier = Modifier.animateItem(
                        fadeInSpec = motion.fastEffectsSpec(),
                        placementSpec = motion.defaultSpatialSpec(),
                        fadeOutSpec = motion.fastEffectsSpec(),
                    )
                    if (state.gridView) ProductCard(
                        product,
                        product.id in state.wishlist,
                        { onProduct(product.id) },
                        { onIntent(EcommerceIntent.ToggleWishlist(product.id)) },
                        heroModifier(product.id),
                        itemModifier.fillMaxWidth()
                    )
                    else ProductListRow(
                        product,
                        product.id in state.wishlist,
                        { onProduct(product.id) },
                        { onIntent(EcommerceIntent.ToggleWishlist(product.id)) },
                        heroModifier(product.id),
                        itemModifier
                    )
                }
            }
        }
    if (showFilters) {
        CatalogFilterSheet(state, onIntent, { showFilters = false })
    }
}

@Composable
private fun CatalogFilterSheet(
    state: EcommerceState,
    onIntent: (EcommerceIntent) -> Unit,
    onDismiss: () -> Unit
) {
    ShowcaseSheet(onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Find your next favourite", style = MaterialTheme.typography.headlineSmall)
            Text("Sort by", style = MaterialTheme.typography.titleMedium)
            Column(Modifier.selectableGroup()) {
                ProductSort.entries.forEach { sort ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(
                                selected = state.sort == sort, role = Role.RadioButton,
                                onClick = { onIntent(EcommerceIntent.SetSort(sort)) }),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = state.sort == sort, onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.onSurface,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            sort.label,
                            Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            HorizontalDivider()
            Text("Price", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    1_500 to "Up to ₹1,500",
                    3_000 to "Up to ₹3,000",
                    6_000 to "Up to ₹6,000",
                    10_000 to "Any price"
                )
                    .chunked(2).forEach { prices ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            prices.forEach { (price, label) ->
                                FilterChip(
                                    selected = state.maxPrice == price,
                                    onClick = { onIntent(EcommerceIntent.SetMaxPrice(price)) },
                                    label = { Text(label) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        onIntent(EcommerceIntent.SetSort(ProductSort.FEATURED))
                        onIntent(EcommerceIntent.SetMaxPrice(10_000))
                        onIntent(EcommerceIntent.Search(""))
                        onIntent(EcommerceIntent.SelectCategory(FashionCategory.ALL))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) { Text("Reset") }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1.4f)
                ) { Text("Show ${state.filteredProducts.size} pieces") }
            }
        }
    }
}

@Composable
fun ProductDetailScreen(
    product: Product,
    state: EcommerceState,
    onIntent: (EcommerceIntent) -> Unit,
    onCart: () -> Unit,
    heroModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    var selectedColor by rememberSaveable(product.id) { mutableStateOf(product.colors.first()) }
    var selectedSize by rememberSaveable(product.id) {
        mutableStateOf(product.sizes.firstOrNull { it == "M" } ?: product.sizes.first())
    }
    var detailsExpanded by rememberSaveable(product.id) { mutableStateOf(false) }
    val motion = MaterialTheme.motionScheme
    val enlargedType = LocalDensity.current.fontScale > 1.25f
    Column(modifier
        .fillMaxSize()
        .testTag("product_detail_screen")) {
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item {
                Box(Modifier
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(32.dp))) {
                    ProductArtwork(
                        product,
                        heroModifier
                            .fillMaxWidth()
                            .aspectRatio(.94f),
                        variantIndex = product.colors.indexOf(selectedColor)
                    )
                    WishlistButton(
                        product,
                        product.id in state.wishlist,
                        { onIntent(EcommerceIntent.ToggleWishlist(product.id)) },
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    )
                    if (product.originalPrice > product.price) {
                        DiscountPill(
                            ((product.originalPrice - product.price) * 100) / product.originalPrice,
                            Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        )
                    }
                }
            }
            item {
                Column(
                    Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LuxuryBadge(
                        product.subtitle,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        product.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RatingPill(product.rating, product.reviewCount)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedMoney(product.price)
                        Text(
                            money(product.originalPrice),
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${((product.originalPrice - product.price) * 100) / product.originalPrice}% off",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                    Text(
                        "Inclusive of all taxes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                Column(
                    Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedContent(selectedColor, transitionSpec = {
                        fadeIn(motion.fastEffectsSpec<Float>()).togetherWith(fadeOut(motion.fastEffectsSpec<Float>()))
                    }, label = "Colour choice") { color ->
                        Text(
                            "Colour: $color",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(product.colors) { color ->
                            FilterChip(
                                selected = selectedColor == color,
                                onClick = { selectedColor = color },
                                label = { Text(color) },
                                leadingIcon = if (selectedColor == color) ({
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        null,
                                        Modifier.size(18.dp)
                                    )
                                }) else null,
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }
                    AnimatedContent(selectedSize, transitionSpec = {
                        fadeIn(motion.fastEffectsSpec<Float>()).togetherWith(fadeOut(motion.fastEffectsSpec<Float>()))
                    }, label = "Size choice") { selected ->
                        Text(
                            "Size: $selected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    }
                    val sizes =
                        if (product.sizes.all { it.toIntOrNull() != null }) product.sizes else listOf(
                            "XS",
                            "S",
                            "M",
                            "L",
                            "XL",
                            "XXL"
                        )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sizes) { size ->
                            val available = size in product.sizes
                            FilterChip(
                                selected = selectedSize == size,
                                onClick = { selectedSize = size },
                                enabled = available,
                                label = {
                                    Text(
                                        size,
                                        textDecoration = if (available) TextDecoration.None else TextDecoration.LineThrough
                                    )
                                },
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .semantics {
                                        contentDescription =
                                            "Size $size${if (available) "" else ", unavailable"}"
                                    })
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        AnimatedContent(
                            selectedColor to selectedSize,
                            Modifier.fillMaxWidth(),
                            transitionSpec = {
                                fadeIn(motion.fastEffectsSpec<Float>()).togetherWith(fadeOut(motion.fastEffectsSpec<Float>()))
                            },
                            label = "Selected variant"
                        ) { (color, size) ->
                            Row(
                                Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "$color / $size · Ready to add",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            item {
                Column(
                    Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "The details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { detailsExpanded = !detailsExpanded },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                        ) { Text(if (detailsExpanded) "Show less" else "Read more") }
                    }
                    Text(
                        product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (detailsExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    AnimatedVisibility(
                        detailsExpanded,
                        enter = fadeIn(motion.fastEffectsSpec()),
                        exit = fadeOut(motion.fastEffectsSpec())
                    ) {
                        Text(
                            "Care: gentle wash with similar colours. Dry in shade.\nFit: choose your usual size for the intended silhouette.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column {
                            Text(
                                "Delivered in 3–5 days",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "Free from ₹1,999. Otherwise ₹99.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        "Collection illustrations; actual fabric and fit may vary.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        ActionFooter {
            val addToBag =
                { onIntent(EcommerceIntent.AddToCart(product.id, selectedColor, selectedSize)) }
            val buyNow = { onIntent(EcommerceIntent.AddToCart(product.id, selectedColor, selectedSize, openCart = true)) }
            if (enlargedType) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AddToBagButton(addToBag, Modifier.fillMaxWidth())
                    Button(
                        onClick = buyNow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 54.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Buy now", style = MaterialTheme.typography.titleSmall)
                    }
                }
            } else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AddToBagButton(addToBag, Modifier.weight(1f))
                    Button(
                        onClick = buyNow,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 54.dp),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text("Buy now", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogSearch(query: String, onQuery: (String) -> Unit, onSearch: () -> Unit = {}) {
    val keyboard = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = query,
        onValueChange = onQuery,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("catalog_search")
            .semantics { contentDescription = "Search the collection" },
        placeholder = {
            Text(
                "Search your next favourite",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = if (query.isNotEmpty()) ({
            IconButton(onClick = { onQuery("") }) {
                Icon(
                    Icons.Default.Close,
                    "Clear search"
                )
            }
        }) else null,
        shape = RoundedCornerShape(22.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.onSurface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            cursorColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide(); onSearch() }),
    )
}

@Composable
private fun AddToBagButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Icon(Icons.Default.AddShoppingCart, null, Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Add to bag", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SectionHeading(
    title: String,
    action: String? = null,
    onAction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (action != null) TextButton(
            onClick = onAction,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Text(action, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun CatalogFailurePanel(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                Icons.Default.Refresh,
                null,
                Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Your collection is waiting",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.heightIn(min = 50.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Try again") }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    saved: Boolean,
    onClick: () -> Unit,
    onWishlist: () -> Unit,
    heroModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Card(
            onClick = onClick,
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            ),
        ) {
            Box {
                ProductArtwork(product, heroModifier
                    .fillMaxWidth()
                    .aspectRatio(.77f))
                WishlistButton(
                    product,
                    saved,
                    onWishlist,
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
                if (product.originalPrice > product.price) {
                    val discount =
                        ((product.originalPrice - product.price) * 100) / product.originalPrice
                    DiscountPill(discount, Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp))
                }
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                product.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            Text(
                product.subtitle.substringAfter("·", product.subtitle).trim(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    money(product.price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                RatingPill(product.rating, product.reviewCount)
            }
        }
    }
}

@Composable
private fun ProductListRow(
    product: Product,
    saved: Boolean,
    onClick: () -> Unit,
    onWishlist: () -> Unit,
    heroModifier: Modifier,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        shadowElevation = 1.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                ProductArtwork(product, heroModifier
                    .width(118.dp)
                    .height(156.dp))
                if (product.originalPrice > product.price) {
                    DiscountPill(
                        ((product.originalPrice - product.price) * 100) / product.originalPrice,
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                    )
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Text(
                    product.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    money(product.price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RatingPill(product.rating)
                    Text(
                        "${product.rating} stars · ${product.reviewCount} reviews",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            WishlistButton(product, saved, onWishlist, Modifier.padding(end = 10.dp))
        }
    }
}

@Composable
private fun WishlistButton(
    product: Product,
    saved: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        if (saved) 1.08f else 1f,
        MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "Saved piece"
    )
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = .92f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        shadowElevation = 2.dp,
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
            Icon(
                if (saved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                if (saved) "Remove ${product.name} from saved pieces" else "Save ${product.name}",
                Modifier
                    .scale(scale)
                    .size(22.dp), tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ProductSkeleton(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SkeletonShimmer(Modifier
            .fillMaxWidth()
            .aspectRatio(.77f)
            .clip(RoundedCornerShape(26.dp)))
        SkeletonShimmer(Modifier
            .fillMaxWidth(.82f)
            .height(16.dp)
            .clip(RoundedCornerShape(8.dp)))
        SkeletonShimmer(Modifier
            .fillMaxWidth(.5f)
            .height(14.dp)
            .clip(RoundedCornerShape(8.dp)))
    }
}

private fun money(amount: Int): String =
    "₹${NumberFormat.getIntegerInstance(Locale.forLanguageTag("en-IN")).format(amount)}"
