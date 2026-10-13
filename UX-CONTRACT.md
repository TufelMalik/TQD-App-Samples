# UX Contract

Product evidence: the user request and the complete attached TechQuantum Showcase App UI/UX Research & Build Plan, reviewed 12 October 2026. Current scope is all four modules: Shop, Food, POS and Billing. The13 October redesign implementation was explicitly authorized by the user. This is an offline local showcase with no backend, payment collection or fulfilment.

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

Add to bag stays on detail, updates the badge and acknowledges the item. Buy now opens the bag only after the selected variant is accepted. Variants merge only when product, color and size all match. Quantity is clamped 1–99. Swipe or remove deletes that local row; adding the piece again recovers it. Wishlist is local and persistent through saved state.

Local catalogue filtering is immediate, bounded to fifteen products and deterministic; no network debounce or URL state is needed in this native offline app. Search has a clear button. Grid/list modes use the same dataset. Loading uses skeletons; empty results offer Clear filters. Repository failure offers retry.

Prices are mock integers in INR and include GST. Delivery is ₹99 below ₹1,999, complimentary from ₹1,999, and zero for an empty bag. Totals are calculated once by domain policy and shown in cart, checkout and receipt.

Checkout remains one page: address, payment choice, review. Place demo order validates all fields, displays inline errors and focuses the first invalid field. Valid submission locks the snapshot and disables repeat submission while a local receipt is recorded. All payment choices are simulated; no card/UPI credentials are requested. Success clears the cart and opens confirmation with the receipt. Continue shopping returns to Discover. Order/error state supports recovery if navigation or a local write is interrupted.

## Persistence and feedback

SavedStateHandle owns cart, wishlist, filters, address, payment and last order through Activity/process restoration. Per-module palette IDs and global System/Light/Dark and Reduce motion use app-local preferences; previous theme/dark keys migrate. Address is demo data stored in saved state, not a network or persistent database. Empty bags invite browsing. Errors retain input and offer retry. No external side effects or permission prompts exist in this flow.

Material dialogs/sheets own focus and back dismissal. Icon actions have content descriptions and 48dp targets. Cart removal has a tap alternative. Theme selection rerenders the same screen with semantic token mappings; it does not navigate or recreate cart state. All brand text outside color containers uses a readable surface foreground.

## Other module contracts

Food Home → Menu → Review → Checkout → Preview Tracking remains local. Menu collapse/category stickiness are functional; cart shows tax, delivery, packaging and tip before the action. Customization drafts survive palette changes. Clearing filters recovers unmatched search without nested unbounded scrolling. Correcting a field retains unresolved validation.

POS and Billing share one repository. Compact POS mounts one menu/bill pane; split panes require840dp width,600dp height and fontScale<=1.5. Operator controls are56dp minimum. Idle Billing preview follows latest shared receipt; output preserves its captured receipt through completion/dismissal. Discounts/GST use existing whole-rupee rounding; payment/print/share remain simulated.

Root owns IME and bottom tabs; app bars own top status clearance. Applied inner Scaffold padding is consumed. Shared sheets own modal IME insets. Palette selection changes no business state. Reduced mode stops loops and supplies immediate theme motion; monetary updates do not flood announcements.

## Verification

Run `:app:assembleDebug`, `:core:designsystem:testDebugUnitTest`, `:feature:ecommerce:testDebugUnitTest`, `:app:lintDebug` and connected Compose tests when an Android target is available. Actual implementation results/limits are recorded in docs/ui-ux-implementation/VALIDATION.md. Contrast checks cover the five light/dark schemes. Domain checks cover variants, quantity bounds, filters, delivery threshold, validation and duplicate submission. Device flow checks cover all six destinations, palette/mode changes and empty/invalid states. Static premium audit output is `premium-audit.json`; it cannot substitute for a device run.
