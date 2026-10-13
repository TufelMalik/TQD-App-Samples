package com.techquantum.tqdappsample.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.techquantum.core.billingengine.BillLine
import com.techquantum.core.billingengine.BillReceipt
import com.techquantum.core.billingengine.BillState
import com.techquantum.core.billingengine.MenuItem
import com.techquantum.core.designsystem.FashionTheme
import com.techquantum.core.designsystem.ShowcaseAppTheme
import com.techquantum.core.designsystem.ShowcaseModule
import com.techquantum.feature.billing.presentation.BillingOutputState
import com.techquantum.feature.billing.presentation.BillingState
import com.techquantum.feature.billing.ui.BillPreviewScreen
import com.techquantum.feature.billing.ui.FoodSelectionScreen
import com.techquantum.feature.billing.ui.RunningBillScreen
import com.techquantum.feature.ecommerce.domain.CartItem
import com.techquantum.feature.ecommerce.domain.DeliveryPolicy
import com.techquantum.feature.ecommerce.domain.FashionCategory
import com.techquantum.feature.ecommerce.domain.GarmentStyle
import com.techquantum.feature.ecommerce.domain.OrderReceipt
import com.techquantum.feature.ecommerce.domain.PaymentMethod
import com.techquantum.feature.ecommerce.domain.Product
import com.techquantum.feature.ecommerce.domain.ProductSort
import com.techquantum.feature.ecommerce.domain.ShippingAddress
import com.techquantum.feature.ecommerce.presentation.EcommerceState
import com.techquantum.feature.ecommerce.ui.CartScreen
import com.techquantum.feature.ecommerce.ui.CheckoutScreen
import com.techquantum.feature.ecommerce.ui.ConfirmationScreen
import com.techquantum.feature.ecommerce.ui.DiscoverScreen
import com.techquantum.feature.ecommerce.ui.ProductDetailScreen
import com.techquantum.feature.ecommerce.ui.ProductListingScreen
import com.techquantum.feature.fooddelivery.domain.AddOn
import com.techquantum.feature.fooddelivery.domain.Cuisine
import com.techquantum.feature.fooddelivery.domain.Dish
import com.techquantum.feature.fooddelivery.domain.FoodAddress
import com.techquantum.feature.fooddelivery.domain.FoodArt
import com.techquantum.feature.fooddelivery.domain.FoodFees
import com.techquantum.feature.fooddelivery.domain.FoodLine
import com.techquantum.feature.fooddelivery.domain.FoodOrder
import com.techquantum.feature.fooddelivery.domain.FoodPayment
import com.techquantum.feature.fooddelivery.domain.MenuCategory
import com.techquantum.feature.fooddelivery.domain.Restaurant
import com.techquantum.feature.fooddelivery.domain.TrackingStage
import com.techquantum.feature.fooddelivery.presentation.FoodState
import com.techquantum.feature.fooddelivery.ui.FoodCartScreen
import com.techquantum.feature.fooddelivery.ui.FoodCheckoutScreen
import com.techquantum.feature.fooddelivery.ui.FoodHomeScreen
import com.techquantum.feature.fooddelivery.ui.FoodTrackingScreen
import com.techquantum.feature.fooddelivery.ui.RestaurantMenuScreen
import com.techquantum.feature.pos.presentation.PosState
import com.techquantum.feature.pos.ui.PosChargeScreen
import com.techquantum.feature.pos.ui.PosReceiptScreen
import com.techquantum.feature.pos.ui.PosWorkspaceScreen

@Composable
private fun PreviewTheme(module: ShowcaseModule, content: @Composable () -> Unit) {
    ShowcaseAppTheme(module = module, fashionTheme = FashionTheme.LUXEVA, darkTheme = false, content = content)
}

private val ecommerceProducts = listOf(
    Product(
        id = "ethnic-kurta",
        name = "Ivory Banarasi Kurta",
        subtitle = "Festive couture",
        category = FashionCategory.ETHNIC,
        price = 5499,
        originalPrice = 7999,
        rating = 4.8,
        reviewCount = 184,
        description = "Rich brocade weave with a tailored silhouette for celebrations, ceremonies, and evening occasions.",
        colors = listOf("Ivory", "Gold", "Rose"),
        sizes = listOf("S", "M", "L", "XL"),
        artwork = GarmentStyle.KURTA,
    ),
    Product(
        id = "street-overshirt",
        name = "Metro Overshirt",
        subtitle = "Layered streetwear",
        category = FashionCategory.STREETWEAR,
        price = 3299,
        originalPrice = 4299,
        rating = 4.6,
        reviewCount = 212,
        description = "An oversized overshirt with a crisp finish, built for easy layering and a clean city silhouette.",
        colors = listOf("Coal", "Olive", "Cobalt"),
        sizes = listOf("S", "M", "L", "XL"),
        artwork = GarmentStyle.SHIRT,
    ),
    Product(
        id = "women-dress",
        name = "Rumi Silk Dress",
        subtitle = "Boutique luxury",
        category = FashionCategory.WOMEN,
        price = 6999,
        originalPrice = 8999,
        rating = 4.7,
        reviewCount = 96,
        description = "A softly draped silk dress with a defined waist and a refined boutique finish.",
        colors = listOf("Blush", "Ink"),
        sizes = listOf("XS", "S", "M", "L"),
        artwork = GarmentStyle.DRESS,
    ),
    Product(
        id = "active-joggers",
        name = "Aero Joggers",
        subtitle = "Move with ease",
        category = FashionCategory.ACTIVE,
        price = 2499,
        originalPrice = 3499,
        rating = 4.5,
        reviewCount = 143,
        description = "Lightweight joggers with a tapered leg, made for commuting, warmups, and all-day comfort.",
        colors = listOf("Storm", "Sage"),
        sizes = listOf("S", "M", "L", "XL"),
        artwork = GarmentStyle.TROUSERS,
    ),
    Product(
        id = "men-linen",
        name = "Linen Relaxed Shirt",
        subtitle = "Daily essentials",
        category = FashionCategory.MEN,
        price = 2799,
        originalPrice = 3799,
        rating = 4.4,
        reviewCount = 88,
        description = "An airy linen shirt with a relaxed fit that works for workdays and easy weekend plans.",
        colors = listOf("Sand", "Sky"),
        sizes = listOf("S", "M", "L", "XL"),
        artwork = GarmentStyle.SHIRT,
    ),
)

private val ecommerceAddress = ShippingAddress(
    name = "Aditi Shah",
    phone = "9876543210",
    line = "24, River View Apartments, Adajan",
    city = "Surat",
    pinCode = "395009",
)

private val ecommerceCart = listOf(
    CartItem(ecommerceProducts[0], "Ivory", "M", 1),
    CartItem(ecommerceProducts[1], "Olive", "L", 2),
)

private fun ecommerceReceipt(items: List<CartItem> = ecommerceCart): OrderReceipt {
    val subtotal = items.sumOf(CartItem::total)
    val delivery = DeliveryPolicy.fee(subtotal)
    return OrderReceipt(
        id = "TQD-ORD-2609",
        items = items,
        address = ecommerceAddress,
        payment = PaymentMethod.UPI,
        subtotal = subtotal,
        delivery = delivery,
        total = subtotal + delivery,
    )
}

private fun ecommerceState(
    loading: Boolean = false,
    query: String = "",
    category: FashionCategory = FashionCategory.ALL,
    gridView: Boolean = true,
    cart: List<CartItem> = ecommerceCart,
    order: OrderReceipt? = null,
    payment: PaymentMethod = PaymentMethod.UPI,
    placingOrder: Boolean = false,
    error: String? = null,
): EcommerceState = EcommerceState(
    products = ecommerceProducts,
    loading = loading,
    query = query,
    category = category,
    sort = ProductSort.FEATURED,
    gridView = gridView,
    maxPrice = 10_000,
    wishlist = setOf(ecommerceProducts[1].id, ecommerceProducts[2].id),
    cart = cart,
    address = ecommerceAddress,
    payment = payment,
    placingOrder = placingOrder,
    order = order,
    error = error,
)

private val foodAddOnCheese = AddOn("cheese", "Extra cheese", 39)
private val foodAddOnButter = AddOn("butter", "Butter", 20)
private val foodAddOnSalad = AddOn("salad", "Side salad", 25)

private val foodThali = Dish(
    id = "thali",
    restaurantId = "rangoli",
    name = "Surat Thali",
    description = "A generous Gujarati thali with seasonal sabzi, dal, rotli, rice, farsan, and sweet.",
    price = 219,
    category = MenuCategory.POPULAR,
    vegetarian = true,
    spice = 1,
    artwork = FoodArt.THALI,
    addOns = listOf(foodAddOnButter),
)
private val foodDosa = Dish(
    id = "dosa",
    restaurantId = "rangoli",
    name = "Crispy Masala Dosa",
    description = "Golden dosa with spiced potato filling, chutney, and sambar.",
    price = 179,
    category = MenuCategory.MAINS,
    vegetarian = true,
    spice = 1,
    artwork = FoodArt.DOSA,
    addOns = listOf(foodAddOnCheese),
)
private val foodPizza = Dish(
    id = "pizza",
    restaurantId = "verde",
    name = "Garden Fire Pizza",
    description = "Wood-fired pizza with a bright tomato base, charred crust, and fresh vegetables.",
    price = 329,
    category = MenuCategory.POPULAR,
    vegetarian = true,
    spice = 0,
    artwork = FoodArt.PIZZA,
    addOns = listOf(foodAddOnCheese, foodAddOnSalad),
)
private val foodBowl = Dish(
    id = "bowl",
    restaurantId = "verde",
    name = "Protein Bowl",
    description = "A nourishing bowl with grains, grilled vegetables, and a lemon herb dressing.",
    price = 259,
    category = MenuCategory.MAINS,
    vegetarian = true,
    spice = 0,
    artwork = FoodArt.BOWL,
)
private val foodDrink = Dish(
    id = "drink",
    restaurantId = "verde",
    name = "Cold Lime Soda",
    description = "A refreshing citrus soda with a touch of mint.",
    price = 89,
    category = MenuCategory.DRINKS,
    vegetarian = true,
    spice = 0,
    artwork = FoodArt.DRINK,
)

private val foodRestaurant = Restaurant(
    id = "rangoli",
    name = "Rangoli Thali House",
    description = "Home-style Gujarati meals with a modern plated finish.",
    cuisine = Cuisine.GUJARATI,
    rating = 4.8,
    reviewCount = 1240,
    etaMinutes = 24,
    distanceKm = 2.3,
    artwork = FoodArt.THALI,
    dishes = listOf(foodThali, foodDosa),
)
private val foodRestaurantTwo = Restaurant(
    id = "verde",
    name = "Verde Slice Studio",
    description = "Wood-fired pies and bright salads for an easy lunch or dinner.",
    cuisine = Cuisine.PIZZA,
    rating = 4.7,
    reviewCount = 801,
    etaMinutes = 28,
    distanceKm = 3.1,
    artwork = FoodArt.PIZZA,
    dishes = listOf(foodPizza, foodBowl, foodDrink),
)
private val foodRestaurants = listOf(foodRestaurant, foodRestaurantTwo)

private val foodCart = listOf(
    FoodLine(foodThali, addOns = listOf(foodAddOnButter), note = "Less salt", quantity = 1),
    FoodLine(foodDosa, quantity = 2),
)
private val foodAddress = FoodAddress(
    name = "Aditi Shah",
    phone = "9876543210",
    line = "24, River View Apartments, Adajan",
    city = "Surat",
    pinCode = "395009",
)

private fun foodOrder(stage: TrackingStage = TrackingStage.OUT_FOR_DELIVERY): FoodOrder {
    val bill = FoodFees.bill(foodCart, tip = 20)
    return FoodOrder(
        id = "FOOD-2841",
        restaurant = foodRestaurant,
        lines = foodCart,
        address = foodAddress,
        payment = FoodPayment.UPI,
        instructions = "Leave at the door and ring once.",
        bill = bill,
    )
}

private fun foodState(
    cart: List<FoodLine> = foodCart,
    order: FoodOrder? = null,
    trackingStage: TrackingStage = TrackingStage.PLACED,
    trackingSeconds: Int = 0,
    cuisine: Cuisine = Cuisine.ALL,
    menuCategory: MenuCategory? = null,
    query: String = "",
    loading: Boolean = false,
    error: String? = null,
    placingOrder: Boolean = false,
    tip: Int = 20,
    payment: FoodPayment = FoodPayment.UPI,
    instructions: String = "",
    address: FoodAddress = foodAddress,
): FoodState = FoodState(
    restaurants = foodRestaurants,
    loading = loading,
    deliveryArea = "Adajan, Surat",
    query = query,
    cuisine = cuisine,
    menuCategory = menuCategory,
    cart = cart,
    instructions = instructions,
    tip = tip,
    address = address,
    payment = payment,
    placingOrder = placingOrder,
    order = order,
    trackingStage = trackingStage,
    trackingSeconds = trackingSeconds,
    error = error,
)

private val posCatalog = listOf(
    MenuItem("pos-butter-dosa", "Butter Dosa", "Breakfast", 179, true, 1),
    MenuItem("pos-paneer-wrap", "Paneer Wrap", "Starters", 149, true, 0),
    MenuItem("pos-cold-coffee", "Cold Coffee", "Drinks", 119, true, 0),
    MenuItem("pos-gulab-jamun", "Gulab Jamun", "Desserts", 99, true, 0),
    MenuItem("pos-chicken-bowl", "Chicken Bowl", "Lunch", 259, false, 2),
)
private val posBill = BillState(
    catalog = posCatalog,
    lines = listOf(
        BillLine(posCatalog[0], 2),
        BillLine(posCatalog[2], 1),
        BillLine(posCatalog[4], 1),
    ),
    discountPercent = 5,
    table = "Table 02",
)
private val posReceipt = BillReceipt(
    id = "BILL-2048",
    lines = posBill.lines,
    totals = posBill.totals,
    payment = "UPI",
    table = posBill.table,
)

private fun posState(
    query: String = "",
    category: String = "All",
    payment: String = "UPI",
    charging: Boolean = false,
    receipt: BillReceipt? = posReceipt,
    lastAddedId: String? = posCatalog[0].id,
): PosState = PosState(
    bill = posBill,
    loading = false,
    query = query,
    category = category,
    operator = "Riya Shah",
    payment = payment,
    charging = charging,
    receipt = receipt,
    error = null,
    lastAddedId = lastAddedId,
    addPulse = 1,
)

private val billingCatalog = listOf(
    MenuItem("bill-masala-dosa", "Masala Dosa", "Mains", 149, true, 1),
    MenuItem("bill-paneer-tikka", "Paneer Tikka", "Starters", 229, true, 2),
    MenuItem("bill-lassi", "Sweet Lassi", "Drinks", 79, true, 0),
    MenuItem("bill-brownie", "Chocolate Brownie", "Desserts", 129, true, 0),
    MenuItem("bill-biryani", "Veg Biryani", "Mains", 219, true, 1),
)
private val billingBill = BillState(
    catalog = billingCatalog,
    lines = listOf(
        BillLine(billingCatalog[0], 2, "Less oil"),
        BillLine(billingCatalog[2], 3),
        BillLine(billingCatalog[3], 1),
    ),
    discountPercent = 10,
    table = "Counter 01",
)
private val billingReceipt = BillReceipt(
    id = "BILL-3102",
    lines = billingBill.lines,
    totals = billingBill.totals,
    payment = "UPI (demo)",
    table = billingBill.table,
)

private fun billingState(
    query: String = "",
    category: String = "All",
    loading: Boolean = false,
    editingItem: MenuItem? = null,
    editingExisting: Boolean = false,
    output: BillingOutputState = BillingOutputState(),
    receipt: BillReceipt? = billingReceipt,
    payment: String = "UPI (demo)",
    error: String? = null,
): BillingState = BillingState(
    bill = billingBill,
    loading = loading,
    query = query,
    category = category,
    editingItem = editingItem,
    editingExisting = editingExisting,
    editQuantity = 2,
    editNotes = "Less chili, please",
    receipt = receipt,
    payment = payment,
    output = output,
    error = error,
)

@Preview(name = "Shop · Discover", group = "Shop", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun ShopDiscoverPreview() {
    PreviewTheme(ShowcaseModule.SHOP) {
        DiscoverScreen(ecommerceState(), onIntent = {}, onBrowse = {}, onProduct = {}, heroModifier = { Modifier })
    }
}

@Preview(name = "Shop · Collection", group = "Shop", showBackground = true, widthDp = 1280, heightDp = 960)
@Composable
private fun ShopCollectionPreview() {
    PreviewTheme(ShowcaseModule.SHOP) {
        ProductListingScreen(
            state = ecommerceState(category = FashionCategory.STREETWEAR, gridView = true),
            onIntent = {},
            onProduct = {},
            heroModifier = { Modifier },
        )
    }
}

@Preview(name = "Shop · Product detail", group = "Shop", showBackground = true, widthDp = 412, heightDp = 1200)
@Composable
private fun ShopProductDetailPreview() {
    PreviewTheme(ShowcaseModule.SHOP) {
        ProductDetailScreen(
            product = ecommerceProducts[0],
            state = ecommerceState(),
            onIntent = {},
            onCart = {},
            heroModifier = Modifier,
        )
    }
}

@Preview(name = "Shop · Bag", group = "Shop", showBackground = true, widthDp = 412, heightDp = 1100)
@Composable
private fun ShopBagPreview() {
    PreviewTheme(ShowcaseModule.SHOP) {
        CartScreen(state = ecommerceState(), onIntent = {}, onCheckout = {}, onBrowse = {})
    }
}

@Preview(name = "Shop · Checkout", group = "Shop", showBackground = true, widthDp = 412, heightDp = 1240)
@Composable
private fun ShopCheckoutPreview() {
    PreviewTheme(ShowcaseModule.SHOP) {
        CheckoutScreen(state = ecommerceState(placingOrder = false), onIntent = {}, onBrowse = {})
    }
}

@Preview(name = "Shop · Confirmation", group = "Shop", showBackground = true, widthDp = 412, heightDp = 1020)
@Composable
private fun ShopConfirmationPreview() {
    PreviewTheme(ShowcaseModule.SHOP) {
        ConfirmationScreen(order = ecommerceReceipt(), onContinue = {})
    }
}

@Preview(name = "Food · Home", group = "Food", showBackground = true, widthDp = 412, heightDp = 940)
@Composable
private fun FoodHomePreview() {
    PreviewTheme(ShowcaseModule.FOOD) {
        FoodHomeScreen(state = foodState(), onIntent = {}, onRestaurant = {}, heroModifier = { Modifier })
    }
}

@Preview(name = "Food · Menu", group = "Food", showBackground = true, widthDp = 412, heightDp = 1240)
@Composable
private fun FoodMenuPreview() {
    PreviewTheme(ShowcaseModule.FOOD) {
        RestaurantMenuScreen(
            restaurant = foodRestaurant,
            state = foodState(cart = listOf(FoodLine(foodThali, addOns = listOf(foodAddOnButter), note = "Less salt", quantity = 1))),
            onIntent = {},
            onBack = {},
            onCart = {},
            onToggleDark = {},
            heroModifier = Modifier,
        )
    }
}

@Preview(name = "Food · Cart", group = "Food", showBackground = true, widthDp = 412, heightDp = 1240)
@Composable
private fun FoodCartPreview() {
    PreviewTheme(ShowcaseModule.FOOD) {
        FoodCartScreen(state = foodState(), onIntent = {}, onCheckout = {}, onBrowse = {})
    }
}

@Preview(name = "Food · Checkout", group = "Food", showBackground = true, widthDp = 412, heightDp = 1300)
@Composable
private fun FoodCheckoutPreview() {
    PreviewTheme(ShowcaseModule.FOOD) {
        FoodCheckoutScreen(state = foodState(order = foodOrder(), instructions = "Leave at the door and ring once."), onIntent = {}, onBrowse = {})
    }
}

@Preview(name = "Food · Tracking", group = "Food", showBackground = true, widthDp = 412, heightDp = 1100)
@Composable
private fun FoodTrackingPreview() {
    PreviewTheme(ShowcaseModule.FOOD) {
        FoodTrackingScreen(
            state = foodState(order = foodOrder(), trackingStage = TrackingStage.OUT_FOR_DELIVERY, trackingSeconds = 240),
            onIntent = {},
            onHome = {},
        )
    }
}

@Preview(name = "POS · Workspace", group = "POS", showBackground = true, widthDp = 1280, heightDp = 820)
@Composable
private fun PosWorkspacePreview() {
    PreviewTheme(ShowcaseModule.POS) {
        PosWorkspaceScreen(state = posState(), onIntent = {}, onCharge = {})
    }
}

@Preview(name = "POS · Charge", group = "POS", showBackground = true, widthDp = 412, heightDp = 1120)
@Composable
private fun PosChargePreview() {
    PreviewTheme(ShowcaseModule.POS) {
        PosChargeScreen(state = posState(), onIntent = {}, onBack = {})
    }
}

@Preview(name = "POS · Receipt", group = "POS", showBackground = true, widthDp = 412, heightDp = 1100)
@Composable
private fun PosReceiptPreview() {
    PreviewTheme(ShowcaseModule.POS) {
        PosReceiptScreen(receipt = posReceipt, onNewBill = {})
    }
}

@Preview(name = "Billing · Select food", group = "Billing", showBackground = true, widthDp = 1280, heightDp = 900)
@Composable
private fun BillingSelectFoodPreview() {
    PreviewTheme(ShowcaseModule.BILLING) {
        FoodSelectionScreen(state = billingState(), onIntent = {}, onReview = {})
    }
}

@Preview(name = "Billing · Running bill", group = "Billing", showBackground = true, widthDp = 412, heightDp = 1120)
@Composable
private fun BillingRunningBillPreview() {
    PreviewTheme(ShowcaseModule.BILLING) {
        RunningBillScreen(state = billingState(), onIntent = {}, onPreview = {}, onBrowse = {})
    }
}

@Preview(name = "Billing · Receipt preview", group = "Billing", showBackground = true, widthDp = 412, heightDp = 1260)
@Composable
private fun BillingReceiptPreview() {
    PreviewTheme(ShowcaseModule.BILLING) {
        BillPreviewScreen(state = billingState(), onIntent = {}, onBrowse = {})
    }
}

