# TechQuantum Fashion Showcase

Offline Kotlin / Jetpack Compose showcase with four functional modules. Shared palettes, typography, editorial assets and motion components live in `:core:designsystem`. Hilt provides the local catalogues. Shop uses Studio Tiles with an immersive product detail; Food uses Fresh Local with a story-style menu; POS uses Table Desk with charge discounts; Billing uses Paper Ledger. All orders, payments, tracking and receipt outputs remain local demos.

Tap the palette icon to switch among Studio Tiles, Luxury Ethnic Couture, Urban Threads, Streetwear, Athleisure and Luxeva, then choose System, Light or Dark. Settings persist locally. Search, categories, filter/sort, grid/list, wishlist, color/size, quantities and demo checkout are interactive. Editorial photo atlases and variant illustrations are bundled, so the complete showcase works offline.

The catalogue has fifteen Indian fashion pieces priced in INR. Prices include GST; delivery is ₹99 below ₹1,999 and complimentary from ₹1,999. Checkout requests demo address fields and never collects payment details. Receipts are local showcase state.

## Build

Use Android Studio with SDK 37 and Java 21, or run:

```powershell
.\gradlew.bat :app:assembleDebug :core:designsystem:testDebugUnitTest :feature:ecommerce:testDebugUnitTest :app:lintDebug
.\gradlew.bat :feature:ecommerce:connectedDebugAndroidTest :app:connectedDebugAndroidTest
```

Material 3 is pinned to `1.5.0-alpha29`: the plan's public `MaterialExpressiveTheme` / `MaterialTheme.motionScheme` APIs are internal in stable `1.4.0`. This is the only deliberate dependency departure from the plan's stability claim. Other Compose libraries follow the BOM, and hidden APIs are not accessed.

See [DESIGN.md](DESIGN.md) and [UX-CONTRACT.md](UX-CONTRACT.md) for token ownership and flow behavior.
