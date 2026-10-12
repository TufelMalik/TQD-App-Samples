# UX Contract

Product evidence: the user request and the complete attached TechQuantum Showcase App UI/UX Research & Build Plan, reviewed 12 October 2026. Scope is Module 1 only; other modules are shell placeholders. This is an offline local showcase with no backend, payment collection or fulfilment.

Visual contract: [DESIGN.md](DESIGN.md). Runtime tokens belong to `:core:designsystem`; five fashion identities each have light/dark schemes. English UI, INR formatting (`en-IN`), Android platform text and input semantics, WCAG AA content contrast target.

## Canonical UI Map

| Capability | Canonical owner | Source of truth | Allowed variants | Verification |
|---|---|---|---|---|
| Select/Listbox | Material FilterChip / RadioButton | MVI enum state | category, sort, payment, theme | Compose interaction tests |
| Form | PurchaseScreens AddressField + EcommerceReducer.validateAddress | ShippingAddress | local demo checkout | validation unit / flow tests |
| Toast | EcommerceGraph SnackbarHost | EcommerceEffect | added-to-bag status | flow verification |
| CRUD | EcommerceViewModel + EcommerceReducer | EcommerceState | local cart mutations | JVM unit tests |

## Flow and state

Discover → Listing → Product Detail → Cart → Checkout → Confirmation, inside Shop's nested NavHost. Home tiles/carousel select a category; a product opens detail. Back preserves catalogue query/filter state and per-product selected variant. Root tab controllers are independent. Android predictive back is enabled in the manifest and handled by Navigation Compose.

Add to bag stays on detail, animates the bag feedback and acknowledges the item. Buy now adds the current variant and opens the bag. Variants merge only when product, color and size all match. Quantity is clamped 1–99. Swipe or remove deletes that local row; adding the piece again recovers it. Wishlist is local and persistent through saved state.

Local catalogue filtering is immediate, bounded to fifteen products and deterministic; no network debounce or URL state is needed in this native offline app. Search has a clear button. Grid/list modes use the same dataset. Loading uses skeletons; empty results offer Clear filters. Repository failure offers retry.

Prices are mock integers in INR and include GST. Delivery is ₹99 below ₹1,999, complimentary from ₹1,999, and zero for an empty bag. Totals are calculated once by domain policy and shown in cart, checkout and receipt.

Checkout remains one page: address, payment choice, review. Place demo order validates all fields, displays inline errors and focuses the first invalid field. Valid submission locks the snapshot and disables repeat submission while a local receipt is recorded. All payment choices are simulated; no card/UPI credentials are requested. Success clears the cart and opens confirmation with the receipt. Continue shopping returns to Discover. Order/error state supports recovery if navigation or a local write is interrupted.

## Persistence and feedback

SavedStateHandle owns cart, wishlist, filters, address, payment and last order through Activity/process restoration. The chosen theme and light/dark setting use app-local preferences. Address is demo data stored in saved state, not a network or persistent database. Empty bags invite browsing. Errors retain input and offer retry. No external side effects or permission prompts exist in this flow.

Material dialogs/sheets own focus and back dismissal. Icon actions have content descriptions and 48dp targets. Cart removal has a tap alternative. Theme selection rerenders the same screen with semantic token mappings; it does not navigate or recreate cart state. All brand text outside color containers uses a readable surface foreground.

## Verification

Run `:app:assembleDebug`, `:core:designsystem:testDebugUnitTest`, `:feature:ecommerce:testDebugUnitTest`, `:app:lintDebug` and connected Compose tests when an Android target is available. Contrast checks cover the five light/dark schemes. Domain checks cover variants, quantity bounds, filters, delivery threshold, validation and duplicate submission. Device flow checks cover all six destinations, palette/mode changes and empty/invalid states. Static premium audit output is `premium-audit.json`; it cannot substitute for a device run.
