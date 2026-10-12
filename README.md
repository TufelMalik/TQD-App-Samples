# TechQuantum Fashion Showcase

Offline Kotlin / Jetpack Compose fashion demo. Module 1 is implemented under `:feature:ecommerce`; shared palettes, typography and motion components live in `:core:designsystem`. Hilt provides the in-memory catalogue. The Shop flow has Discover, Listing, Detail, Cart, Checkout and Confirmation; Food, POS and Billing retain independent shell accents as placeholders.

Tap the palette icon on any shopping screen to change among Luxury Ethnic Couture, Urban Threads, Streetwear, Athleisure and Luxeva, then choose Light or Dark. Settings persist locally. Search, category, filter/sort, grid/list, wishlist, color/size, quantities and demo checkout are interactive. Artwork is bundled Compose illustration, so the complete showcase works offline.

The catalogue has fifteen Indian fashion pieces priced in INR. Prices include GST; delivery is ₹99 below ₹1,999 and complimentary from ₹1,999. Checkout requests demo address fields and never collects payment details. Receipts are local showcase state.

## Build

Use Android Studio with SDK 37 and Java 21, or run:

```powershell
.\gradlew.bat :app:assembleDebug :core:designsystem:testDebugUnitTest :feature:ecommerce:testDebugUnitTest :app:lintDebug
.\gradlew.bat :feature:ecommerce:connectedDebugAndroidTest :app:connectedDebugAndroidTest
```

Material 3 is pinned to `1.5.0-alpha29`: the plan's public `MaterialExpressiveTheme` / `MaterialTheme.motionScheme` APIs are internal in stable `1.4.0`. This is the only deliberate dependency departure from the plan's stability claim. Other Compose libraries follow the BOM, and hidden APIs are not accessed.

See [DESIGN.md](DESIGN.md) and [UX-CONTRACT.md](UX-CONTRACT.md) for token ownership and flow behavior.
