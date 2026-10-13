---
version: alpha
name: "TechQuantum Fashion Showcase"
description: "A fashion lookbook that turns into a familiar, thumb-friendly shopping flow."
colors:
  primary: "#B5566B"
  secondary: "#7A1F2D"
  tertiary: "#8A6D5A"
  background: "#F6EFE6"
  foreground: "#1D1D1F"
typography:
  display:
    fontFamily: "serif"
  body:
    fontFamily: "sans-serif"
rounded:
  sm: "12px"
  md: "18px"
  lg: "24px"
  xl: "32px"
spacing:
  page-inset: "20px"
  section-gap: "24px"
components:
  button:
    height: "52px"
  icon-button:
    height: "48px"
---

# TechQuantum Fashion Showcase

## Approved layout selection — 13 October 2026

The user selected Shop C (Studio Tiles) with a B+C product detail, Food A (Fresh Local) with Menu C and redesigned recommendations, POS C (Table Desk) with Charge discount, and Billing A (Paper Ledger).

Shop discovery now leads with asymmetric collection tiles and a product rail. Detail combines an immersive editorial photo with an anchored information card, side variant/view controls, expandable information and a pill purchase action. Colour/size selections retain their existing cart behavior; the illustration view accurately reflects the selected variant. The bundled editorial photographs are illustrative category images.

Food uses cream/forest green, a photographic campaign, circular cuisine choices and two-column restaurant cards. The menu uses an immersive image and rounded information panel. Recommendations use readable photo cards with separate add/quantity and customization actions; large text uses a single column.

POS is table-first, with a category sidebar and compact item list. At enlarged text, categories become a horizontal strip and table selection scrolls with menu content. Wide workspaces keep the existing single catalogue plus bill pane. Charge shows payment stages and discount choices before the payment method, with totals calculated by the shared billing engine. Billing keeps individual item cards and a centered paper receipt with demo output actions.

Studio is a sixth, optional fashion palette, and the selected design is applied once through a preference revision. Existing five palettes, appearance, reduced motion and business state remain available. New anchors and generated image rendering live in `core/designsystem`; feature code uses semantic colors. Both photographic atlases are bundled for offline rendering, and are cropped at display time without stretching.

## Overview

The reference is a boutique's seasonal lookbook: expressive garment illustrations, quiet typography and a collection that is easy to explore. This is a product showcase for Indian retail clients on a phone, with local prices in INR and English copy. Home carries the brand expression; checkout uses familiar labelled fields and an itemized summary.

The signature is one wardrobe that changes its visual identity live. The palette picker remains available across all six shopping destinations and keeps the cart, selections and address intact. Other tabs retain their own default accents and explicit per-module palette overrides. Appearance and reduced motion are global. The attached plan is product evidence; the user's requested scope and five themes take precedence over its original shopping accent.

Runtime ownership: `core/designsystem/.../FashionTheme.kt` is the canonical palette source. `ShowcaseAppTheme` is the app root and maps the active module/override to Material color, typography, shape and motion tokens; `LocalFashionTheme` supplies identity copy. This file documents those decisions and the default Luxeva anchors rather than generating a second token system. Feature composables use `MaterialTheme.colorScheme` exclusively.

## Colors

Luxury Ethnic uses ivory, maroon, pink, gold and olive. Urban Threads uses steel grey, navy, denim, slate and charcoal. Streetwear uses black, asphalt, red, blue, orange and off-white. Athleisure uses white/graphite, lime, blue and silver. Luxeva uses cream, dusty rose, wine, mocha and graphite. All anchors and derived light/dark semantic roles are centralized in `FashionTheme.kt`.

Each solid color owns a contrast-derived `onX` color. Text on surfaces uses `onSurface` or `onSurfaceVariant`; text on tinted containers uses that container's corresponding foreground. Lime is a fill accent, with dark text. Error roles remain separate from the brand. The theme contrast suite tests all ten schemes including surface hierarchy and containers.

## Typography

Platform serif at 24–36sp gives campaign copy its lookbook character. Sans serif at 12–20sp carries product information, totals and controls. Fonts are local system families, so the showcase requires no network. Sentence case, visible action labels and localized rupee formatting maintain clarity. Prices use the emphasized title scale. Food/POS/Billing use sans operational headings to preserve hierarchy at large text.

## Layout

Use a 20dp phone gutter, 12–24dp gaps, adaptive listing columns from 150dp, and a bento category composition. Home has one carousel and one product rail. Listing search/category/filter controls scroll in full-span grid items. Food menu categories remain sticky while its app bar collapses. POS compact headers/search/categories scroll with the active pane; only action summaries remain pinned. Enlarged text uses240dp minimum grid cells. Forms constrain to720dp and receipts to560dp. Detail, cart and checkout place purchase actions at the bottom above the root navigation bar. Safe-area and keyboard insets keep actions and fields accessible. Cart and checkout own vertical scroll independently.

## Elevation & Depth

Use tonal surface layers for cards, with elevation reserved for sticky purchase bars and modal sheets. Navigation and ActionFooter use opaque semantic surfaces. Product illustrations reserve their aspect ratio during loading and content changes; skeletons match those slots. Avoid blanket gradients or decorative shadows.

## Shapes

Controls and chips are pills. Shared containers use Material shapes (12, 18, 24 and 32dp), campaign artwork rounds at 28dp and product art at 22dp. Rounding communicates hierarchy rather than giving every surface the same silhouette.

## Components

Shared owners are `ShowcasePalettePicker`, `PaletteAction`, `CompactModuleTitle`, `ShowcaseSheet`, `ActionFooter`, `DietaryIndicator`, `SkeletonShimmer`, `QuantityStepper`, `AnimatedMoney`, `CartBadge`, Material buttons/chips/fields and the root snackbar. ProductArtwork is a local fashion illustration shared across catalogue/detail/cart. Every icon action has an accessible name; swiping a cart row has a remove-button alternative. Disabled variants remain visible with an unavailable label.

Motion uses public `MaterialExpressiveTheme` and `MaterialTheme.motionScheme` spring specs, a single root `SharedTransitionLayout`, shared product artwork, `AnimatedContent` variant/layout/price changes, badge/snackbar acknowledgement and a drawn confirmation check. Material 3 is pinned to **1.5.0-alpha29** because stable 1.4.0 keeps the requested Expressive APIs internal. Android animator scale and an app Reduce motion preference both disable spatial effects and loops; skeletons are static in reduced mode. Existing Material version is retained without a dependency upgrade. See docs/ui-ux-implementation/VALIDATION.md for actual test evidence.

## Do's and Don'ts

- Keep cart, theme, variants and address state independent of palette selection.
- Use semantic foregrounds and visible text errors instead of color alone.
- Show inclusive prices and the delivery charge before submitting.
- Keep all data and artwork local; payment and delivery remain explicitly simulated.
- Avoid screen-local hex values, default dynamic color overrides and hidden Expressive API access.
