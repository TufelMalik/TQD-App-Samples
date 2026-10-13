# TechQuantum Showcase — UI/UX redesign audit and implementation plan

Implementation authorized13 October2026 by the user's instruction: **“start implementation in application code.”** Historical audit findings/line citations below remain frozen to the audit baseline. Current changes and validation are tracked in [implementation records](docs/ui-ux-implementation/README.md); any draft/approval language below describes the earlier audit delivery.

**Status: DRAFT — awaiting approval. No application code modified.**

Audit evidence collected 12 October and regenerated 13 October 2026. Source baseline: branch `features/new_ui`, commit `9f17f86b7d3dcd0349cd8fe7c31c7b332018b627`. This plan covers all four native modules,17 destinations, and their shared overlays. The attached showcase plan supplies product context; the user's latest request authorizes an audit and plan only, not implementation. Earlier requests define the desired architecture and preserve distinct module accents unless the theme-scope question below is resolved differently.

## 1. Most important findings

1. **Food no-results search crashes the app.** A vertically scrolling empty panel is measured inside a LazyColumn with unbounded height. Reproduced with `zzzzz`; correct scroll ownership before adding motion.
2. **POS is unusable at compact widths and duplicated at expanded widths.** Both workspace implementations are inside `if(wide)` without a mobile `else`. Phone shows only the header; tablet composes both layouts.
3. **Billing can show/output an older receipt after POS completes a newer one.** Billing-local receipt takes precedence over the shared latest receipt. Reproduced with Billing#1042 followed by POS#1043.
4. **Two selected-tab text colors miss normal-text AA.** Settled calculated contrast is3.87:1 for Luxeva light and4.31:1 for Streetwear light. Existing onX scheme tests do not cover this component pair.
5. **Large-text Billing chrome can eliminate the receipt viewport.** At320dp/200% text, unrestricted title/subtitle beside four actions grow into tall chrome. Print/Share remain while receipt content disappears.
6. **Scroll/inset fixes are uneven.** Shop Listing and Billing Selection already put headers in their grids; Food Home also scrolls its intro. Preserve those fixes. Footer gaps are visible above bottom tabs; unconsumed root padding and repeated child navigation-bar padding are likely causes requiring measured ownership correction.
7. **Palette scope/access is inconsistent.** Food Menu lacks a palette action. The current global FashionTheme overrides existing Food/POS/Billing schemes, conflicting with earlier module-color requirements. Clarify scope and make launch/state/persistence consistent.
8. **Responsive feedback exists, but needs refinement.** Shared heroes, springs, steppers, animated totals, success draws and paper feed are already present. Reduce competing effects, improve press/state/error feedback, and define reduced-motion/interruption behavior rather than layering animation everywhere.

P1 means a core-flow blocker/crash. P2 means a material usability or correctness issue. P3 means localized polish or optional enhancement. Runtime-confirmed findings were exercised in the local emulator. Source-confirmed findings are established by code; risks remain explicitly untested. No measured performance or user-research claim is made.

## 2. Markdown audit bundle

The linked documents are part of this implementation plan. They contain the complete finding evidence, causes, fixes, per-screen specifications, and acceptance criteria. Shared concepts have separate files as requested.

| File | Contents |
|---|---|
| [SHOP_AUDIT.md](<D:/Projects/TQD/TQD App Sample/docs/ui-ux-audit/SHOP_AUDIT.md>) | Six-screen audit; purchase/validation/state issues; scroll, responsive and accessibility contracts; exact source evidence and tasks. |
| [FOOD_DELIVERY_AUDIT.md](<D:/Projects/TQD/TQD App Sample/docs/ui-ux-audit/FOOD_DELIVERY_AUDIT.md>) | Five-screen audit; reproduced crash, menu palette/empty category, validation/restoration, billing/route semantics and tasks. |
| [POS_AUDIT.md](<D:/Projects/TQD/TQD App Sample/docs/ui-ux-audit/POS_AUDIT.md>) | Three-screen/pane audit; phone/tablet branching, header ownership, operator targets, payment grouping, shared engine preservation. |
| [BILLING_AUDIT.md](<D:/Projects/TQD/TQD App Sample/docs/ui-ux-audit/BILLING_AUDIT.md>) | Selection/running/preview plus editor/output; receipt mismatch, large-text chrome, targets, insets, sheet/receipt specifications. |
| [SCROLLING_INSETS_LAYOUT.md](<D:/Projects/TQD/TQD App Sample/docs/ui-ux-audit/SCROLLING_INSETS_LAYOUT.md>) | One-owner inset contract; all17 routes' scrolling/pinned/collapsing behavior; width/height/font/IME rules. |
| [THEMES_DESIGN_SYSTEM.md](<D:/Projects/TQD/TQD App Sample/docs/ui-ux-audit/THEMES_DESIGN_SYSTEM.md>) | Palette scope, placement/persistence, semantic color/contrast, typography/spacing/surfaces, reusable components. |
| [MOTION_INTERACTIONS.md](<D:/Projects/TQD/TQD App Sample/docs/ui-ux-audit/MOTION_INTERACTIONS.md>) | Existing-motion inventory, source findings,interaction specifications: purpose/API/timing/interruption/reduced mode/performance. |
| [ACCESSIBILITY_VALIDATION.md](<D:/Projects/TQD/TQD App Sample/docs/ui-ux-audit/ACCESSIBILITY_VALIDATION.md>) | Source findings, native semantics, every-screen acceptance, compact/large text/theme/keyboard/system-nav/TalkBack/state/performance matrix. |
| [RESEARCH_REFERENCES.md](<D:/Projects/TQD/TQD App Sample/docs/ui-ux-audit/RESEARCH_REFERENCES.md>) |15 official technical guide topics, six specific Figma/Dribbble/Behance references, direct images, verified limits, palette contrast calculation. |
| [AUDIT_EVIDENCE.md](<D:/Projects/TQD/TQD App Sample/docs/ui-ux-audit/AUDIT_EVIDENCE.md>) | Source/APK/configuration identity, build results, runtime coverage, durable screenshots/logs, limits and reproduction steps. |
| [RUNTIME_OBSERVATIONS.md](<D:/Projects/TQD/TQD App Sample/docs/ui-ux-audit/RUNTIME_OBSERVATIONS.md>) | Markdown export of captured accessibility-tree labels/bounds and full crash log. Supports screenshots; does not replace TalkBack testing. |
| [AUDIT_TASK_CHECKPOINT.md](<D:/Projects/TQD/TQD App Sample/AUDIT_TASK_CHECKPOINT.md>) | Done/remaining/resume instructions, updated to documentation completion at delivery. |

## 3. Scope reviewed and behavior to preserve

| Module | Screens reviewed | Active source symbols / exact starting lines |
|---|---|---|
| Shop | Discover, Listing, Detail, Cart, Checkout, Confirmation | `DiscoverScreen` EC121; `ProductListingScreen` EC534; `ProductDetailScreen` EC766; `CartScreen` EP57; `CheckoutScreen` EP191; `ConfirmationScreen` EP279. |
| Food | Home, Restaurant Menu, Cart/Review, Checkout, Tracking | `FoodHomeScreen` FS48; `RestaurantMenuScreen` FS215; `FoodCartScreen` FS396; `FoodCheckoutScreen` FS556; `FoodTrackingScreen` FS652. |
| POS | Workspace compact/expanded and Menu/Bill panes, Payment, Receipt | `PosWorkspaceScreen` PS54, `MenuPanel` PS175, `BillPanel` PS313; `PosChargeScreen` PS425; `PosReceiptScreen` PS526. |
| Billing | Food Selection, Running Bill, Preview; item editor and print/share result overlays | `FoodSelectionScreen` BS47; `RunningBillScreen` BS193; `BillPreviewScreen` BS360; `BillingItemEditor` BS481; `BillingOutputSheet` BS513. |

Source aliases in section8 link the exact files. Additional overlays reviewed: root FashionThemePicker, Shop filters/sizing, Food area/customization/restaurant-switch confirmation, Billing notes/output. Overlay behavior beyond saved captures is source review, with pending runtime cases in the validation matrix.

Preserve the single Activity, four-tab bottom NavigationBar, four independent nested NavHosts, root SharedTransitionLayout, predictive-back manifest flag, Hilt, MVI feature boundaries, and local/offline data. Do not introduce real payment/printer/location services or migrate to Koin/Navigation3. The installed stack is Kotlin2.2.10, BOM2026.09.00, Material3 1.5.0-alpha29, Navigation Compose2.9.8 and Hilt2.59.2; version upgrades are not prerequisites for these fixes.

Preserve quantity bounds/variant merging, notes/table/discount state, INR integer-domain totals, Food itemized fees/tips, Shop delivery policy, shared POS/Billing repository, HALF_UP rounding then demo5%GST, captured payment/print snapshots, duplicate-action guards and restoration/effect recovery. Animation never owns domain mutation or receipt identity. Typealias bridges and inactive alternative card files are not duplicate live ViewModels; edit active owners.

The earlier UI_UX_IMPLEMENTATION_REPORT.md refers to commit4526542 and overstates complete verification. DESIGN.md/UX-CONTRACT.md remain useful but contain stale Shop-only scope and inconsistent header/color claims. Reconcile those documents after design approval. Current code, reproducible runtime observations and explicit user scope take precedence over historical claims.

## 4. Design contract proposed for approval

- **Scrolling:** one primary vertical owner per active phone body or tablet pane. Discovery/search/intro/filters scroll together. Pin compact route chrome and essential amount/action only. Food Menu retains its existing nested-scroll collapse and sticky category row. Horizontal rails are allowed; do not confuse them with an unbounded same-axis scroller.
- **Insets:** root tabs own system-bottom safety; apply/consume root padding once. Module app bars own status/cutout safety. Footers above tabs do not add a second system bottom inset. Form viewport owns IME avoidance before scrolling; keep invalid/focused controls visible. Palette/sheets own their Material modal insets independently.
- **Top section:** retain safe status padding. No current baseline evidence establishes a universal remaining duplicated top status inset. Tighten excessive body/editorial spacing where it delays primary content; bound app-bar title/action allocation at large text.
- **Themes:** recommended global System/Light/Dark with per-module palette slots. Keep the five Shop identities and existing Food/POS/Billing defaults. Optional explicit global showcase override only if approved. Picker launches near the top consistently; mode/scope/selected state are labeled and persisted without resetting graph/cart/bill/focus/scroll.
- **Typography/surface:** canonical Material roles, serif only for Shop campaigns; readable sans operational headings, emphasized prices/totals, restrained monospace receipts. Reuse existing TQDDimensions/shape roles,16–20dp compact gutters,24dp expanded/section spacing,48dp native/56dp operator minima. Surfaces use matching onX foregrounds; semitransparent pairs are tested after compositing.
- **Motion:** preserve existing theme springs/shared heroes, local value feedback and success effects. Primary feedback settles around≤300ms and heroes400–600ms; these are targets for springs. Reduced mode removes spatial/looping/decorative effects while preserving useful final state and direct gestures. No per-keystroke whole-screen animation, row duplication or repeated success replay.
- **Accessibility/state:** named controls, heading/selection/error/status semantics, contextual quantities, visible gesture alternatives, error-specific focus/recovery, honest simulated copy, readable200% text, and no per-second tracking announcements.

## 5. Phased implementation roadmap

This is a future work schedule. Every task includes its exact affected owners, prerequisites, material risk and acceptance. File aliases expand to exact linked paths in section8. Named **new** components are proposed, not claimed to exist. Module documents provide component-level details; shared specifications apply to all tasks.

### Phase A — Restore correctness before redesign

| Task / priority | Work and affected files | Dependencies / risks | Acceptance criteria |
|---|---|---|---|
| A1 / P1 | Split `FoodEmptyPanel` content from standalone scroll host; update LazyColumn callers. **FS**; add meaningful no-match/failure UI regression in **FlowTest**. | None. Risk: fixing Home while leaving a missing-route screen unscrollable, or creating another unbounded owner. | `zzzzz`, category no-match and load failure render recovery without crash; standalone empty/missing body remains bounded and scroll-reachable. |
| A2 / P1 | Correct mutually exclusive wide/mobile branches in `PosWorkspaceScreen`. **PS**, **FlowTest**. | None; layout tests depend on deterministically selected window width. Risk: selected pane lost on resize or duplicate bill owners. | Compact search/add/review/charge available; exactly one split above breakpoint;839/840/841dp tested with populated state and palette/resize. |
| A3 / P2 | Define latest receipt versus captured in-progress/output receipt precedence. **BC**, **BV**, **BillingOutputTest**; shared **BR** read contract preserved. | Reproduce with A2 for compact flow or current expanded flow. Risk: replacing an active output snapshot with a newer shared receipt. | Print1042→POS completes1043→idle Billing previews/outputs1043; active output remains its own captured receipt; duplicate print/share still completes once. |
| A4 / P2 | Fix selected nav text/background semantic pair. **M**, **FT**, **FashionContrastTest**. | None. Risk: weakening brand anchors or only testing raw colors rather than actual bar alpha surface. | All five×two selected/unselected tab label pairs pass normal-text4.5:1; selection also conveyed by indicator/semantics; requested palette anchors preserved. |
| A5 / P2 | Bound Billing top chrome and prioritize actions/overflow at narrow/large text. **BG**; shared proposed PaletteAction if approved in C2. | Can start before C2. Risk: hiding essential Back/Palette/Bill or truncating body data. |320dp/200% selection/running/preview have reachable content and CTA; subtitle suppresses/wraps responsibly, full title remains accessible. |

### Phase B — Scrolling, insets, keyboard and adaptive structure

| Task / priority | Work and affected files | Dependencies / risks | Acceptance criteria |
|---|---|---|---|
| B1 / P2 | Instrument root/module/footer boundaries, implement apply/consume once. **M**, **EG**, **FG**, **PG**, **BG**. | A1/A2; measured gesture/three-button baseline. Risk: double consumption or accidental status/IME obstruction. | Root reserves tabs once; app bars own top once; route bodies fill intended safe viewport; no unexplained top/bottom strip and system gestures work. |
| B2 / P2 | Remove repeated footer navigation insets only after ownership is proven; unify measured CTA/snackbar clearance. **EC**, **EP**, **FS**, **PS**, **BS**, **EG**, **BG**. | B1. Risk: applying a footer rule to a screen actually drawing to device bottom; tall large-text bars. | Last rows/fees scroll entirely above CTA; snackbar never covers action; footer lies directly above root tabs with one system contribution in both nav modes. |
| B3 / P2 | Rebuild compact POS scroll owners and short-height tablet panes; constrain payment grouping. **PS**, optional reuse **RESP/DIM**. | A2/B1/B2. Risk: weighted lazy measurement and split-pane starvation at840dp. | Menu identity/search/categories scroll; Bill detail/discount/totals scroll except compact Charge summary; height/pane minima fall back safely; payment max≈720dp. |
| B4 / P2 | Explicit form/editor IME ownership; first-invalid focus + bring-into-view; useful Next/Done. **EP**, **FS**, **BS**, **M** for agreed root-nav IME presentation. | B1/B2. Risk: hiding navigation semantics incorrectly, competing focus requests, or losing draft. | Shop/Food/Billing notes readable with IME; action reachable; Back first hides keyboard; draft and selected payment preserved; invalid field visible. |
| B5 / P2/P3 | Adapt card/row widths to content and font; wrap header/badge/filter/money groups; bounded Billing receipt. **EC**, **EP**, **FS**, **PS**, **BS**, **DIM/RESP**. | B3/B4 where impacted. Risk: arbitrary fontScale math, essential price truncation, changing item identity across list/grid modes. |320/360/411dp and200% text preserve names/prices/controls; expanded receipts≈560dp and forms≈720dp; short landscape still scrolls. |
| B6 / P2 risk-driven | Bound editor/result/palette sheet viewport and provide one scroll plus reachable actions. **BS**, **PK**, **FS** customization/area sheets. | B4/B5. First reproduce each short-height risk; keep working palette list/footer. Risk: stacked or unbounded scrollers. | Normal/320dp/200%/short-landscape overlays have all content/actions reachable; Back/focus restore correctly, no lost notes/customizations/output receipt. |

### Phase C — Coherent palette and shared components

| Task / priority | Work and affected files | Dependencies / risks | Acceptance criteria |
|---|---|---|---|
| C1 / P2 | Implement approved palette scope, appearance enum, stable IDs and migration. **M**, **ST**, **FT**, **FST**, **PK**; proposed preference model under existing core design-system package. Investigate combined large-text/dark system-bar appearance (A13) before changing flag ownership. | Theme-scope approval. Risk: overwriting existing preference or recalculating graph/business state on recolor. | Existing Shop preference survives; unknown ID falls back; System/Light/Dark persists; module/default/global override follows approved copy; cart/bill/form/scroll unchanged; system chrome legible after modal/resize. |
| C2 / P2 | Consistent top PaletteAction and route-state coverage including Food Menu. **EG/FG/PG/BG**, **FS**, **PK**, proposed shared core component. | C1, B5 chrome constraints; overlay scope decision. Risk: new action crowds small app bar or modal focus stacks. | All17 route states have a near-top reachable named palette entry; Menu expanded/collapsed/loading/failure/missing covered;48/56dp targets; editor policy explicit. |
| C3 / P2 | Consolidate role-based dietary indicator into core, preserve textual meaning and safe contrast. **FC/FT/ST**, **FS**341, **PS**229/264, **BS**175. | Approved color contract. Risk: arbitrary client palette changing diet meaning or misusing error for non-veg. | Features read Material roles only; stable labeled diet/spice semantics across approved palettes/modes; no unsupported regulatory-compliance claim. |
| C4 / P3 | Canonical typography/spacing/surfaces and measured ActionFooter component; avoid duplicate tokens. **FST**, **ST**, **DIM**, **TYP**, **FC**, touched active screens. | B1–B6/C1. Risk: broad cosmetic churn or fixed text heights; extract only repeated proven contracts. | Shop keeps editorial identity; operational headings readable sans; operator targets uniform; all fonts scale; feature hex absent; components reused without business changes. |

### Phase D — State recovery and native accessibility

| Task / priority | Work and affected files | Dependencies / risks | Acceptance criteria |
|---|---|---|---|
| D1 / P2 | Preserve Food unresolved errors; distinguish load-failure/not-found; correct pending-switch/location/filter restoration. **FR**, **FV**, **FG**, **EG**, **EV**; **FoodReducerTest** and targeted restoration/UI tests. | A1/B4. Risk: replaying pending add on recreation, wiping existing cart, or retry state loss. | Invalid fields corrected independently; restored valid detail retries correctly; missing content honest; restore never silently replaces cart. |
| D2 / P2 | Surface Shop cap/user-action errors, gate Buy now navigation on an accepted add, disable locked form/payment, correct quantity boundaries/context labels. **ER**, **EG**, **EC**, **EP**, **FC**, active **FS/PS/BS** call sites. | B4/C4. Risk: duplicate announcements, disabling controls beyond busy state, changing1–99 policy. | No silent cap/discarded edit or navigation after rejected add; valid Buy now keeps current cart destination; plus disables99/minus1; all steppers/context targets readable; reducer/snapshot policy unchanged. |
| D3 / P2/P3 | Native headings/groups/action names/error/progress/status/pane semantics; one live-feedback owner; single payment-row stop. **FC**, **PK**, **EC/EP/FS/PS/BS**, graphs where appropriate. | C2/D1/D2. Risk: over-merging child actions or hiding essential nodes. | Manual TalkBack and keyboard/switch tasks complete across modules; one named action per logical control, no countdown/amount announcement flood. |
| D4 / P3 | Honest mock copy, meaningful actionable labels, usable empty-category recovery, appropriate loader labels. **FS**, **FC**, **FoodCatalog** only if data availability metadata requires it. | A1/D1. Risk: unnecessary changes to mock menu or promotional layout. | No false GPS/Michelin/integration claim; Explore all works or isn't styled as an action; unavailable category gives recovery; loaders name correct module. |

### Phase E — Purposeful motion, after usable static layouts

| Task / priority | Work and affected files | Dependencies / risks | Acceptance criteria |
|---|---|---|---|
| E1 / P2 | Proposed local reduced-motion preference policy; gate loops/spatial effects and verify durationScale0. **FC**, **M**, **ST**, **FS**, **PK**; proposed core policy provider. | C1/D3. Risk: inventing platform API or tying domain completion to animation. |0× and user reduced mode show final useful states without pulses/shimmer/flights; ordinary gestures/navigation remain functional; policy local and lifecycle-safe. |
| E2 / P3 | Refine interruption/value feedback; remove whole-row quantity animation; preserve native press state. **FC**, **PS**, **FS**, **EC**, **EG** optional measured arc. | D2/E1. Risk: rapid-tap queues, velocity resets, changing click timing. | Latest target wins; no duplicate row focus or stale amounts; feedback local≤300ms, gestures immediate; optional arc measured or omitted. |
| E3 / P3 | Verify existing hero scopes/geometry/predictive back and add only justified choreography. **EG**, **FG**, **EC**, **FS**, **M**. | B5/E1; stable keys retained. Risk: duplicate visible shared keys, overlay clipping, source scroll lost. | Push/pop/cancel restore correct hero;400–600ms settling target; shape/ContentScale behavior explicitly tested; reduced mode immediate. |
| E4 / P3 | Tracking status/pulse and success/printing lifecycle polish. **FS**, **FV** only if profiling justifies leaf deadline; **EP**, **PS**, **BA**, **BS**. | E1/D3/A3. Risk: clock poll causes whole-screen work; success replay; delay affects snapshot ownership. | Stage announced once; pulse stops Delivered/offscreen; receipt check/feed once perID; no ETA-driven full-screen animation; mock completion/duplicate guards unchanged. |
| E5 / P3 optional | Measure release frames; add only justified benchmark/profile tooling. **gradle/libs.versions.toml**, app Gradle only if needed; proposed `:benchmark` is not installed. | E1–E4 with static regressions resolved. Risk: debug/emulator FPS claims or premature dependency upgrades. | Physical60/120Hz frame distributions recorded; no material regression; loops/allocations/recompositions addressed based on trace. |

### Phase F — Verification and documentation closure

| Task / priority | Work and affected files | Dependencies / risks | Acceptance criteria |
|---|---|---|---|
| F1 / P1 release gate | Extend existing flow/UI suites, targeted reducer/receipt/contrast tests; run baseline compile/JVM/lint then selected-device connected tests. **FlowTest**, **EcommerceFlowTest**, **CatalogScreensTest**, **BillingOutputTest**, **FoodReducerTest**, **FashionContrastTest**. | Relevant approved tasks complete. Risk: tests mirror implementation or only assert route tags. | All validation matrix mandatory rows pass or have explicit material blocker; negative crash/phone/tablet/stale-receipt/large-text cases covered; existing domain tests intact. |
| F2 / P2 gate | Manual keyboard/TalkBack/system-nav/restoration and physical motion/performance review; archive durable before/after evidence. **Audit documents/evidence**, no domain changes. | F1 and real targets. Risk: claiming accessibility from tree/screenshot alone. | Configuration/APK/source identity recorded; unsupported/untested cases disclosed; gesture/3-button and200% text complete; no animation overload or focus trap. |
| F3 / P3 | Reconcile **DESIGN.md**, **UX-CONTRACT.md**, **UI_UX_IMPLEMENTATION_REPORT.md** and checkpoint to approved scope/actual checks. | Final behavior/validation results known. Risk: old report claims copied as passed tests. | One coherent design contract, historical report labeled, precise command/results and remaining limits, no claim of real services/compliance beyond evidence. |

No implementation task is marked done by this audit. Layout/race/contrast fixes can be reviewed in small coherent changes after approval. Do not split a public callback change from its caller updates, or merge animation polish before correctness gates.

## 6. Validation status and plan

Current baseline compilation,58 JVM tests and lint pass; lint retains15 warnings and zero errors. All17 routes were source-reviewed and exercised through mock flows; POS payment/receipt required expanded layout. Static captures validate visible layout/labels for their recorded configurations. They do not validate TalkBack, animation smoothness, performance or every system-navigation mode. Evidence was regenerated into the documentation folder after build-output cleanup; see AUDIT_EVIDENCE.md for exact reruns and artifacts.

Pending after implementation: full320/360/411dp matrix,599/600/839/840/841dp boundaries, short landscape, font130/200%, all approved theme/mode pairs and intermediate colors, Food/editor keyboard cases, gesture/three-button back including cancellation, TalkBack/keyboard/switch traversal, lifecycle/restoration, race guards and release physical60/120Hz frame traces. ACCESSIBILITY_VALIDATION.md specifies scenarios and pass conditions rather than assuming tools are installed or results exist.

## 7. Material open questions and approval record

1. **Palette scope:** recommended global appearance mode plus per-module palette selection, retaining current Food/POS/Billing defaults. Should a fashion palette instead recolor all modules globally? Current implementation and earlier requirements conflict; this changes preference migration and picker copy.
2. **Modal palette access:** does “each screen” include customization/editor/output sheets? Recommended route-level access everywhere, with a single coordinator preserving modal drafts if sheet-level access is required. This changes overlay state/focus policy; do not stack palette and editor sheets.

Other routine choices use the defaults in this plan: native48dp targets, operator56dp, bounded receipts/forms, global System/Light/Dark, keyboard-aware form footer, no decorative animation on monetary data, and preservation of current architecture/business policies. New palette catalogs or benchmark modules remain explicitly proposed.

Approval status: **not approved for implementation**. The user requested an audit, per-feature/concept Markdown files, and a continuation checkpoint. This document delivers that reviewable draft and stops before application changes.

## 8. Exact source-file index

Aliases in the roadmap refer only to these current files. Links point to one-based lines; module findings cite additional exact symbols/lines.

| Alias | File / principal symbol |
|---|---|
| M | [MainActivity.kt:65 — ShowcaseShell](<D:/Projects/TQD/TQD App Sample/app/src/main/java/com/techquantum/tqdappsample/MainActivity.kt:65>) |
| ST | [ShowcaseTheme.kt:43 — ShowcaseAppTheme](<D:/Projects/TQD/TQD App Sample/core/designsystem/src/main/java/com/techquantum/core/designsystem/ShowcaseTheme.kt:43>) |
| FT | [FashionTheme.kt:14 — FashionTheme](<D:/Projects/TQD/TQD App Sample/core/designsystem/src/main/java/com/techquantum/core/designsystem/FashionTheme.kt:14>) |
| FST | [FashionStoreTheme.kt:18 — FashionTypography](<D:/Projects/TQD/TQD App Sample/core/designsystem/src/main/java/com/techquantum/core/designsystem/FashionStoreTheme.kt:18>) |
| PK | [FashionThemePicker.kt:40](<D:/Projects/TQD/TQD App Sample/core/designsystem/src/main/java/com/techquantum/core/designsystem/FashionThemePicker.kt:40>) |
| FC | [FashionComponents.kt:202 — QuantityStepper](<D:/Projects/TQD/TQD App Sample/core/designsystem/src/main/java/com/techquantum/core/designsystem/FashionComponents.kt:202>) |
| DIM | [TQDDimensions.kt:10](<D:/Projects/TQD/TQD App Sample/core/designsystem/src/main/java/com/techquantum/core/designsystem/theme/TQDDimensions.kt:10>) |
| TYP | [TQDTypography.kt:12](<D:/Projects/TQD/TQD App Sample/core/designsystem/src/main/java/com/techquantum/core/designsystem/theme/TQDTypography.kt:12>) |
| RESP | [ResponsiveLayout.kt:46 — calculateConfig](<D:/Projects/TQD/TQD App Sample/core/designsystem/src/main/java/com/techquantum/core/designsystem/utils/responsive/ResponsiveLayout.kt:46>) |
| EC | [CatalogScreens.kt:121 — DiscoverScreen](<D:/Projects/TQD/TQD App Sample/feature/ecommerce/src/main/java/com/techquantum/feature/ecommerce/ui/CatalogScreens.kt:121>) |
| EP | [PurchaseScreens.kt:57 — CartScreen](<D:/Projects/TQD/TQD App Sample/feature/ecommerce/src/main/java/com/techquantum/feature/ecommerce/ui/PurchaseScreens.kt:57>) |
| EG | [EcommerceGraph.kt:36](<D:/Projects/TQD/TQD App Sample/feature/ecommerce/src/main/java/com/techquantum/feature/ecommerce/navigation/EcommerceGraph.kt:36>) |
| ER | [EcommerceReducer.kt](<D:/Projects/TQD/TQD App Sample/feature/ecommerce/src/main/java/com/techquantum/feature/ecommerce/presentation/EcommerceReducer.kt>) |
| EV | [EcommerceViewModel.kt](<D:/Projects/TQD/TQD App Sample/feature/ecommerce/src/main/java/com/techquantum/feature/ecommerce/presentation/EcommerceViewModel.kt>) |
| FS | [FoodScreens.kt:48 — FoodHomeScreen](<D:/Projects/TQD/TQD App Sample/feature/fooddelivery/src/main/java/com/techquantum/feature/fooddelivery/ui/FoodScreens.kt:48>) |
| FG | [FoodDeliveryGraph.kt:33](<D:/Projects/TQD/TQD App Sample/feature/fooddelivery/src/main/java/com/techquantum/feature/fooddelivery/navigation/FoodDeliveryGraph.kt:33>) |
| FR | [FoodReducer.kt](<D:/Projects/TQD/TQD App Sample/feature/fooddelivery/src/main/java/com/techquantum/feature/fooddelivery/presentation/FoodReducer.kt>) |
| FV | [FoodViewModel.kt](<D:/Projects/TQD/TQD App Sample/feature/fooddelivery/src/main/java/com/techquantum/feature/fooddelivery/presentation/FoodViewModel.kt>) |
| FoodCatalog | [FoodCatalog.kt:19](<D:/Projects/TQD/TQD App Sample/feature/fooddelivery/src/main/java/com/techquantum/feature/fooddelivery/data/FoodCatalog.kt:19>) |
| PS | [PosScreens.kt:54 — PosWorkspaceScreen](<D:/Projects/TQD/TQD App Sample/feature/pos/src/main/java/com/techquantum/feature/pos/ui/PosScreens.kt:54>) |
| PG | [PosGraph.kt:35](<D:/Projects/TQD/TQD App Sample/feature/pos/src/main/java/com/techquantum/feature/pos/navigation/PosGraph.kt:35>) |
| BS | [BillingScreens.kt:47 — FoodSelectionScreen](<D:/Projects/TQD/TQD App Sample/feature/billing/src/main/java/com/techquantum/feature/billing/ui/BillingScreens.kt:47>) |
| BG | [BillingGraph.kt](<D:/Projects/TQD/TQD App Sample/feature/billing/src/main/java/com/techquantum/feature/billing/navigation/BillingGraph.kt>) |
| BC | [BillingContract.kt:42 — BillingState.previewReceipt](<D:/Projects/TQD/TQD App Sample/feature/billing/src/main/java/com/techquantum/feature/billing/presentation/BillingContract.kt:42>) |
| BV | [BillingViewModel.kt:40](<D:/Projects/TQD/TQD App Sample/feature/billing/src/main/java/com/techquantum/feature/billing/presentation/BillingViewModel.kt:40>) |
| BA | [BillingArtwork.kt:81 — PaperFeedAnimation](<D:/Projects/TQD/TQD App Sample/feature/billing/src/main/java/com/techquantum/feature/billing/ui/BillingArtwork.kt:81>) |
| BR | [BillRepository.kt:65 — complete](<D:/Projects/TQD/TQD App Sample/core/billingengine/src/main/java/com/techquantum/core/billingengine/BillRepository.kt:65>) |
| FlowTest | [ShowcaseModulesFlowTest.kt:38](<D:/Projects/TQD/TQD App Sample/app/src/androidTest/java/com/techquantum/tqdappsample/ShowcaseModulesFlowTest.kt:38>) |
| EcommerceFlowTest | [EcommerceFlowTest.kt](<D:/Projects/TQD/TQD App Sample/app/src/androidTest/java/com/techquantum/tqdappsample/EcommerceFlowTest.kt>) |
| CatalogScreensTest | [CatalogScreensTest.kt](<D:/Projects/TQD/TQD App Sample/feature/ecommerce/src/androidTest/java/com/techquantum/feature/ecommerce/ui/CatalogScreensTest.kt>) |
| BillingOutputTest | [BillingOutputTest.kt](<D:/Projects/TQD/TQD App Sample/feature/billing/src/test/java/com/techquantum/feature/billing/presentation/BillingOutputTest.kt>) |
| FoodReducerTest | [FoodReducerTest.kt](<D:/Projects/TQD/TQD App Sample/feature/fooddelivery/src/test/java/com/techquantum/feature/fooddelivery/FoodReducerTest.kt>) |
| FashionContrastTest | [FashionThemeContrastTest.kt:11](<D:/Projects/TQD/TQD App Sample/core/designsystem/src/test/java/com/techquantum/core/designsystem/FashionThemeContrastTest.kt:11>) |

Verified external guides and specific visual references are cited at the relevant concept decisions and centralized in RESEARCH_REFERENCES.md. They support patterns/API constraints, not invented test observations or a redesign conversion claim.
