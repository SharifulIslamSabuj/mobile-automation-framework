# Locator Repository — My Demo App (Android)

## Document Control

| Field | Value |
|---|---|
| Document ID | MA-LOC-001 |
| Title | Centralized Locator Repository |
| Version | v1.0 |
| Status | Draft |
| Source of Truth | [saucelabs/my-demo-app-android](https://github.com/saucelabs/my-demo-app-android) |
| Source Commit | `8cf5fac23ca6cedafe7be3c63fad8fe4ee6f5612` (2024-12-12) |
| Analysis Method | Full local clone of the repository; every locator below was read directly from the actual XML layout / Java source file cited in its **Source File** and **Source Line** columns — none were guessed or inferred from device inspection alone |
| Companion Evidence | Cross-checked against the Phase 2 manual device-evidence findings (Appium Inspector captures) recorded earlier in this project; discrepancies, if any, are called out explicitly per element |
| Scope | All UI elements required to automate the 32 verified test cases (MA-TC-001), excluding **Crash app (debug)** per explicit exclusion below |

---

## 1. Purpose

This document is the **single source of truth** for every automation locator in the project. From this point forward:

- No locator is to be rediscovered ad hoc inside a Page Object or Test Class.
- Every Page Object must reference this repository.
- Any locator change discovered during automation must be corrected here first, then propagated.

## 2. Locator Selection Strategy

Locators are chosen in strict priority order. A lower-priority strategy is used **only** when every higher-priority option is unavailable or unstable, and the reason is documented per element.

| Priority | Strategy | Appium/UiAutomator2 Mapping |
|---|---|---|
| 1 | Accessibility ID | `accessibility id` → matches Android `contentDescription` |
| 2 | Resource ID | `id` → matches Android `resource-id` (`android:id/...`) |
| 3 | Unique View ID (scoped) | Resource ID that is **only unique within a scoped/relative search** (e.g., repeats across RecyclerView items or is reused across unrelated screens) — requires a compound strategy (id + index, id + nearest unique ancestor, or id + sibling text) |
| 4 | Reliable stable text | `-android uiautomator: new UiSelector().text("...")` — only for static, non-localized, non-dynamic text |
| 5 | Class + Attribute combination | `-android uiautomator: new UiSelector().className("...").instance(n)` or attribute match |
| 6 | XPath | Last resort only; reason is mandatory whenever used |

**Governing constraint discovered in source:** this app reuses several resource-ids across unrelated screens and repeats others identically across every item in a RecyclerView (documented in detail in Section 12 — Cross-Validation). Any element flagged **"Reused"** or **"Repeats per item"** in its Automation Notes column requires a Priority-3 compound locator, not a bare resource-id, even though resource-id is nominally Priority 2.

## 3. Naming Convention

Enterprise camelCase, `<screen><Element><Type>` pattern, e.g. `loginUsernameField`, `catalogProductCard`, `productQuantityIncreaseButton`, `cartCheckoutButton`, `shippingContinueButton`, `paymentReviewButton`, `reviewPlaceOrderButton`. The mapping from raw app `resource-id` to this convention is given per element below and consolidated in Section 13.

## 4. Package Root

All resource-ids below are fully qualified as `com.saucelabs.mydemoapp.android:id/<id>` unless noted as an Android platform id (`android:id/...`).

---

## Table of Contents

1. [Login Screen](#5-login-screen)
2. [Product Catalog Screen](#6-product-catalog-screen)
3. [Sort Dialog](#7-sort-dialog)
4. [Product Details Screen](#8-product-details-screen)
5. [Cart Screen](#9-cart-screen)
6. [Checkout — Shipping Address Screen](#10-checkout--shipping-address-screen)
7. [Checkout — Payment Screen](#11-checkout--payment-screen)
8. [Review Order Screen](#12-review-order-screen)
9. [Checkout Complete Screen](#13-checkout-complete-screen)
10. [Navigation Drawer](#14-navigation-drawer)
11. [Drawer Destination Screens](#15-drawer-destination-screens)
12. [Star Rating Review Dialog](#16-star-rating-review-dialog)
13. [Reset App State / Logout Dialogs](#17-reset-app-state--logout-dialogs)
14. [Validation & System Messages](#18-validation--system-messages)
15. [Dynamic Element Locator Patterns](#19-dynamic-element-locator-patterns)
16. [Cross-Validation Summary](#20-cross-validation-summary)
17. [Naming Convention Mapping](#21-naming-convention-mapping)

---

## 5. Login Screen

Source: `app/src/main/res/layout/fragment_login.xml`, `app/src/main/java/.../view/fragments/LoginFragment.java`

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason for Selection | Alternative | Automation Notes |
|---|---|---|---|---|---|---|---|---|
| Login Title | Resource ID | `loginTV` | 2 | fragment_login.xml | 17 | Unique on screen, static | Text "Login" | Static heading |
| Instructions Text | Resource ID | `selectTextTV` | 2 | fragment_login.xml | 31 | Unique on screen | — | Static |
| Username Label | Resource ID | `usernameTV` | 2 | fragment_login.xml | 44 | Unique | Text "Username" | Static |
| Username Input | Resource ID | `nameET` | 2 | fragment_login.xml | 68 | Unique on screen | — | EditText, `inputType="text"` |
| Username Error Icon | Resource ID | `usernameErrorIV` | 2 | fragment_login.xml | 79 | Unique | Accessibility: "Indicates error" | Hidden by default (`visibility="gone"`) |
| Username Error Text | Resource ID | `nameErrorTV` | 2 | fragment_login.xml | 92 | Unique | Text "Username is required" | `visibility="invisible"` until shown |
| Password Label | Resource ID | `passwordTV` | 2 | fragment_login.xml | 105 | Unique | Text "Password" | Static |
| Password Input | Resource ID | `passwordET` | 2 | fragment_login.xml | 129 | Unique on screen | — | EditText, `inputType="textPassword"` |
| Password Error Icon | Resource ID | `passwordErrorIV` | 2 | fragment_login.xml | 140 | Unique | — | Hidden by default |
| Password Error Text | Resource ID | `passwordErrorTV` | 2 | fragment_login.xml | 153 | Unique | — | Text set dynamically (see §18) |
| Biometric Login Button | Accessibility ID | `Tap to login using biometric verification` | 1 | fragment_login.xml | 176/183 | Has stable content-desc | Resource ID `bioMetricIB` | `visibility="gone"` unless `Constants.is_biometric` is true — do not assume present |
| Login Button | Accessibility ID | `Tap to login with given credentials` | 1 | fragment_login.xml | 190/196 | Stable, unique content-desc | Resource ID `loginBtn` | Primary submit action |
| Usernames Column Header | Resource ID | `savedNamesTV` | 2 | fragment_login.xml | 206 | Unique | Text "Usernames" | Static |
| Password Column Header | Resource ID | `savedPasswordTV` | 2 | fragment_login.xml | 223 | Unique | Text "Password" | Static |
| Credential Row 1 — Username | Accessibility ID | `Tap to use this username for login` | 3 | fragment_login.xml | 238/246 | Content-desc **repeats** on rows 2 and 3 | Resource ID `username1TV` | Content-desc is **not unique** across the 3 rows — use Resource ID `username1TV` instead, or Accessibility ID + text filter |
| Credential Row 1 — Password | Resource ID | `password1TV` | 2 | fragment_login.xml | 254 | Unique id | Text "10203040" | Only row with a visible password value in default data |
| Credential Row 2 — Username | Accessibility ID + text | `Tap to use this username for login` filtered by text `alice@example.com (locked out)` | 3 | fragment_login.xml | 266/272,275 | Content-desc reused | Resource ID `username2TV` | Prefer Resource ID |
| Credential Row 2 — Password | Resource ID | `password2TV` | 2 | fragment_login.xml | 282 | Unique id | — | Empty text by default |
| Credential Row 3 — Username | Accessibility ID | `Visual User Login` | 1 | fragment_login.xml | 293/299 | Unique content-desc (only row 3 has a distinct one) | Resource ID `username3TV` | Only credential row with a unique accessibility id |
| Credential Row 3 — Password | Resource ID | `password3TV` | 2 | fragment_login.xml | 309 | Unique id | — | Empty text by default |

**Login → screen-name to enterprise-name mapping:** `nameET` → `loginUsernameField`; `passwordET` → `loginPasswordField`; `loginBtn` → `loginButton`; `nameErrorTV` → `loginUsernameErrorLabel`; `passwordErrorTV` → `loginPasswordErrorLabel`.

---

## 6. Product Catalog Screen

Source: `app/src/main/res/layout/fragment_product_catalog.xml`, `item_products.xml`, `ratting_layout.xml`, `menu_header_layout.xml`, `view/adapters/ProductsAdapter.java`

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason for Selection | Alternative | Automation Notes |
|---|---|---|---|---|---|---|---|---|
| Screen Title | Accessibility ID | `title` | 1 | fragment_product_catalog.xml | 17 | Explicit static content-desc `"title"` | Resource ID `productTV` | Note: same id `productTV` is reused on the Cart screen ("My Cart") with a different string — screen context required |
| Product List (RecyclerView) | Accessibility ID | `Displays all products of catalog` | 1 | fragment_product_catalog.xml | 42 | Unique, stable | Resource ID `productRV` | Container only; items must be located via child index |
| Product Card Root | Class + Attribute | `androidx.constraintlayout.widget.ConstraintLayout` at `productRV` child index `n` | 5 | item_products.xml | 4 | Root of each recycled item has no id | — | Use `productRV` child index `n` as the scoping element for all fields below |
| Product Image | Accessibility ID (set at runtime) | `Product Image` | 1 | ProductsAdapter.java | 51 | Set programmatically via `setContentDescription("Product Image")`; identical string on every card | Resource ID `productIV` | **Repeats per item** — must combine with card index |
| Product Name | Accessibility ID (set at runtime) | `Product Title` | 1 | ProductsAdapter.java | 47 | Set programmatically; identical string on every card | Resource ID `titleTV` | **Repeats per item** — qualify with card index or match by visible text |
| Product Price | Accessibility ID (set at runtime) | `Product Price` | 1 | ProductsAdapter.java | 49 | Set programmatically; identical string on every card | Resource ID `priceTV` | **Repeats per item** |
| Product Rating (stars container) | Resource ID | `rattingV` (bound `ratting_layout.xml` include) | 3 | item_products.xml | 45 | Repeats per item, no content-desc on the star icons themselves | — | The 5 star ImageViews (`start1IV`…`start5IV`) intentionally have **no** content-description in `ratting_layout.xml` (a deliberate accessibility bug — see comment at line 14 of that file). Locate by `drawable` state (`ic_selected_star` vs `ic_unselected_start`) via `resource-id` + index, not accessibility id |
| Sort Button | Accessibility ID | `Shows current sorting order and displays available sorting options` | 1 | menu_header_layout.xml | 33 | Unique, stable | Resource ID `sortIV` | Only visible on the Catalog screen (`View.GONE` elsewhere per `MainActivity.setFragment`) |
| Navigation Drawer (Menu) Button | Accessibility ID | `View menu` | 1 | menu_header_layout.xml | 23 | Unique, stable, present on every screen | Resource ID `menuIV` | Global header element |
| Cart Icon/Button | Accessibility ID | `View cart` | 1 | menu_header_layout.xml | 47 | Unique, stable | Resource ID `cartRL` | Tappable container; wraps `cartIV` |
| Cart Badge Count | Resource ID | `cartTV` | 2 | menu_header_layout.xml | 77 | Unique | Accessibility: "Displays number of items in your cart" (shared with `cartIV`/`cartCircleRL` — not unique, use Resource ID) | **Conditionally rendered**: the whole `cartCircleRL` subtree (including `cartTV`) is absent from the view hierarchy when cart is empty — confirmed both in source (`MainActivity.setData()`, visibility toggle) and in Phase 2 device evidence. Never assert on `cartTV` without first asserting presence/absence of `cartCircleRL` |
| App Logo | Accessibility ID | `App logo and name` | 1 | menu_header_layout.xml | 97 | Unique | Resource ID `mTvTitle` | Not interactive (`clickable="false"`) |

---

## 7. Sort Dialog

Source: `app/src/main/res/layout/sort_dialog.xml`. Triggered from `MainActivity` via `sortIV` click → `showSortDialog()`.

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason for Selection | Alternative | Automation Notes |
|---|---|---|---|---|---|---|---|---|
| Dialog Title | Resource ID | `sortTV` | 2 | sort_dialog.xml | 18 | Unique within dialog | Text "Sort by:" | — |
| Name — Ascending Option | Accessibility ID | `Ascending order by name` | 1 | sort_dialog.xml | 31/40 | Unique, stable, on the clickable container itself | Resource ID `nameAscCL` | Tap target is the whole `ConstraintLayout`, not the text |
| Name — Ascending Checkmark | Resource ID | `tickNameAscIV` | 2 | sort_dialog.xml | 44 | Unique | Accessibility "Shows which sorting order is selected" (shared across all 4 checkmarks — not unique) | Visibility toggles to indicate selection state |
| Name — Descending Option | Accessibility ID | `Descending order by name` | 1 | sort_dialog.xml | 77/85 | Unique, stable | Resource ID `nameDesCL` | — |
| Name — Descending Checkmark | Resource ID | `tickNameDesIV` | 2 | sort_dialog.xml | 89 | Unique | — | `visibility="invisible"` by default |
| Price — Ascending Option | Accessibility ID | `Ascending order by price` | 1 | sort_dialog.xml | 123/131 | Unique, stable | Resource ID `priceAscCL` | — |
| Price — Ascending Checkmark | Resource ID | `tickPriceAscIV` | 2 | sort_dialog.xml | 135 | Unique | — | `visibility="invisible"` by default |
| Price — Descending Option | Accessibility ID | `Descending order by price` | 1 | sort_dialog.xml | 169/177 | Unique, stable | Resource ID `priceDesCL` | — |
| Price — Descending Checkmark | Resource ID | `tickPriceDscIV` | 2 | sort_dialog.xml | 181 | Unique | — | `visibility="invisible"` by default |

**Automation note (confirmed this session, both by source and by artifact-backed device evidence):** selecting any option dismisses the dialog automatically and re-orders the catalog; the resulting order was verified end-to-end for Price Ascending in Phase 2 evidence collection.

---

## 8. Product Details Screen

Source: `app/src/main/res/layout/fragment_product_detail.xml`, `item_color.xml`, `view/fragments/ProductDetailFragment.java`, `view/adapters/ColorsAdapter.java`

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason for Selection | Alternative | Automation Notes |
|---|---|---|---|---|---|---|---|---|
| Product Image | Accessibility ID | `Displays selected product` | 1 | fragment_product_detail.xml | 32 | Unique on this screen | Resource ID `productIV` | Note: same accessibility string used for the Catalog card image and Cart item image — screen-scope required if querying globally |
| Product Name | Resource ID | `productTV` (set via `binding.productTV.setText(...)` in code) | 2 | ProductDetailFragment.java | 123 | Unique on screen | — | **Important:** the field bound at runtime is `productTV`, distinct from the `titleTV` id that exists elsewhere in this same layout file's include structure — verify against the compiled binding, not just the raw XML id list |
| Product Price | Resource ID | `priceTV` | 2 | fragment_product_detail.xml | 48 | Unique on screen | — | — |
| Product Rating | Resource ID | `rattingV` include | 3 | fragment_product_detail.xml | 60 | Star icons have no content-desc (same accessibility gap as Catalog) | — | Same 5-star pattern as §6 |
| Color Selector List | Accessibility ID | `Displays available colors of selected product` | 1 | fragment_product_detail.xml | 72 | Unique | Resource ID `colorRV` | Container; items below |
| Color Swatch (per item) | Accessibility ID (runtime, value-dependent) | e.g. `Black color`, `Green color`, `Gray color`, `Blue color`, or `Unknown color` | 1 | ColorsAdapter.java | 82–98 | Dynamically constructed from the product's actual color value | Resource ID `colorIV` (repeats per item) | **Confirms Phase 2 finding**: products with a color-suffixed catalog name that don't match one of the four known `Constants` color values (`BLACK`/`GREEN`/`GRAY`/`BLUE`) fall through to the `default` case and are labeled `"Unknown color"` — this is a genuine source-level default, not an inference |
| Color Selected Indicator | Resource ID | `aroundIV` | 2 | item_color.xml | 10 | Unique per item scope | — | Only visible (`VISIBLE`) on the currently-selected swatch; `INVISIBLE` on others |
| Decrease Quantity Button | Accessibility ID | `Decrease item quantity` | 1 | fragment_product_detail.xml | 93 | Unique, stable | Resource ID `minusIV` | At `cartNo == 0` the Add-to-Cart button is disabled (see below) — confirmed in `ProductDetailFragment.onClick` |
| Quantity Value | Resource ID | `noTV` | 2 | fragment_product_detail.xml | 97 | Unique on screen | — | Default text `"1"` |
| Increase Quantity Button | Accessibility ID | `Increase item quantity` | 1 | fragment_product_detail.xml | 111 | Unique, stable | Resource ID `plusIV` | — |
| Add To Cart Button | Accessibility ID | `Tap to add product to cart` | 1 | fragment_product_detail.xml | 121 | Unique, stable | Resource ID `cartBt` | **Resource ID `cartBt` is reused** on the Cart screen for "Proceed To Checkout" (different text/content-desc there) — do not locate by id alone across screens |
| Product Highlights Label | Resource ID | `productHeightLightsTV` | 2 | fragment_product_detail.xml | 132 | Unique (note: id has a typo in source, "Height" not "Highlights" — record exactly as-is) | Text "Product Highlights" | — |
| Product Description | Resource ID | `descTV` | 2 | fragment_product_detail.xml | 145 | Unique on screen | — | — |

---

## 9. Cart Screen

Source: `app/src/main/res/layout/fragment_cart.xml`, `item_my_cart.xml`, `view/fragments/CartFragment.java`, `view/adapters/CartItemAdapter.java`

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason for Selection | Alternative | Automation Notes |
|---|---|---|---|---|---|---|---|---|
| Empty-Cart Title | Resource ID | `noItemTitleTV` | 2 | fragment_cart.xml | 16 | Unique | Text "No Items" | Only rendered when `noItemCL` is visible |
| Empty-Cart Illustration Container | Resource ID | `noItemCL` | 2 | fragment_cart.xml | 10 | Unique | — | `visibility="gone"` when cart has items — use as the state-detection element for "cart empty vs. populated" |
| Go Shopping Button (empty state) | Resource ID | `shoppingBt` | 2 | fragment_cart.xml | 59 | Unique | Text "Go Shopping" | Only present/relevant in empty state; **note:** this id also drives navigation in the populated-cart footer, per `CartFragment.onClick` line 108 — same `shoppingBt` id is bound once but the fragment always inflates both states, verify actual visibility before interacting |
| Cart Content Container | Resource ID | `cartCL` | 2 | fragment_cart.xml | 78 | Unique | — | `visibility="visible"` when cart has items — inverse of `noItemCL` |
| Cart Title | Resource ID | `productTV` | 3 | fragment_cart.xml | 98 | **Id reused** — same id as Catalog screen title, different text ("My Cart" vs "Products") | Text "My Cart" | Screen-scoped locator required; do not query globally by id |
| Cart Item List | Accessibility ID | `Displays list of selected products` | 1 | fragment_cart.xml | 114 | Unique | Resource ID `productRV` | Container; items below. Note: **same accessibility string** is reused on the Review Order screen's `placeOrderRV` — scope to screen |
| Cart Item — Product Name | Resource ID | `titleTV` | 3 | item_my_cart.xml | (bound via `ItemMyCartBinding`) | **Repeats per item**, and id also reused across Catalog/Cart/Review-Order item templates | — | Qualify with RecyclerView child index |
| Cart Item — Product Price | Resource ID | `priceTV` | 3 | item_my_cart.xml | (bound) | Repeats per item | — | Qualify with index |
| Cart Item — Quantity Value | Resource ID | `noTV` | 3 | item_my_cart.xml | (bound) | Repeats per item | — | Qualify with index |
| Cart Item — Decrease Quantity | Accessibility ID | `Decrease item quantity` | 1 (id 3 for uniqueness) | item_my_cart.xml | (bound, same string as `@string/decrease_item_quantity`) | Content-desc identical across all cart-item rows | Resource ID `minusIV` + index | **At quantity 1, tapping minus removes the item** — confirmed in `CartItemAdapter.onClick` (`minusIV`) line 96-109: `if (model.getNumberOfProduct() < 1) removeItem(...)`. This resolves the Phase 2 "minus-to-zero removal" item that was left Pending Verification — it is now source-confirmed as a real removal path, in addition to the explicit Remove Item button |
| Cart Item — Increase Quantity | Accessibility ID | `Increase item quantity` | 1 (id 3 for uniqueness) | item_my_cart.xml | (bound) | Repeats per row | Resource ID `plusIV` + index | — |
| Cart Item — Remove Button | Accessibility ID | `Removes product from cart` | 1 (id 3 for uniqueness) | item_my_cart.xml | 139 | Repeats per row | Resource ID `removeBt` | **Simulated 5-second UI-thread block on tap** — confirmed in `CartItemAdapter.java` lines 78-84 ("intentionally introduced" latency for demo/testing purposes). Automated waits after tapping Remove must account for this, or the test will appear to hang/time out prematurely if using a short implicit wait |
| Total Items Count | Resource ID | `itemsTV` | 2 | fragment_cart.xml | 206 | Unique | — | Text format `"<n> Items"` |
| Total Price | Resource ID | `totalPriceTV` | 2 | fragment_cart.xml | 220 | Unique | — | — |
| Proceed To Checkout Button | Accessibility ID | `Confirms products for checkout` | 1 | fragment_cart.xml | 238 | Unique, stable content-desc | Resource ID `cartBt` | **Resource ID `cartBt` reused** from Product Details ("Add to Cart") — accessibility id is the reliable differentiator between these two screens |

---

## 10. Checkout — Shipping Address Screen

Source: `app/src/main/res/layout/fragment_checkout_info.xml`, `view/fragments/CheckoutInfoFragment.java`

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason for Selection | Alternative | Automation Notes |
|---|---|---|---|---|---|---|---|---|
| Checkout Title | Resource ID | `checkoutTitleTV` | 2 | fragment_checkout_info.xml | 20 | Unique on screen | Text "Checkout" | Same id reused on Review Order screen (§12) — different text there |
| "Enter a shipping address" Subtitle | Resource ID | `enterShippingAddressTV` | 2 | fragment_checkout_info.xml | 33 | Unique on screen | Text "Enter a shipping address" | **Id reused on Review Order screen** with text "Review your order" — confirmed matching the resource-id-reuse pattern already documented from device evidence |
| Full Name Field | Resource ID | `fullNameET` | 2 | fragment_checkout_info.xml | 71 | Unique on screen | Hint "Rebecca Winter" | Required field |
| Full Name Error Text | Resource ID | `fullNameErrorTV` | 2 | fragment_checkout_info.xml | 95 | Unique | Text "Please provide your full name." | Shown via `TextWatcher`, live as-you-type (see §18) |
| Address Line 1 Field | Resource ID | `address1ET` | 2 | fragment_checkout_info.xml | 131 | Unique on screen | Hint "Mandorley 112" | Required field |
| Address Line 1 Error Text | Resource ID | `address1ErrorTV` | 2 | fragment_checkout_info.xml | 155 | Unique | Text "Please provide your address." | Live validation |
| Address Line 2 Field | Resource ID | `address2ET` | 2 | fragment_checkout_info.xml | 191 | Unique on screen | Hint "Entrance 1" | **Optional field — confirmed by source**: no `TextWatcher`/validation is attached to `address2ET` anywhere in `CheckoutInfoFragment.java`; it is read only if non-empty (`validate()` line 289). This resolves the MA-TDD-001 "required vs optional" pending item definitively — Address Line 2 is optional |
| Address Line 2 Error Text | Resource ID | `address2ErrorTV` | 2 | fragment_checkout_info.xml | 215 | Unique | Empty text by default, never populated in code | Present in layout but not driven by any validation logic found |
| City Field | Resource ID | `cityET` | 2 | fragment_checkout_info.xml | 258 | Unique on screen | Hint "Truro" | Required field |
| City Error Text | Resource ID | `cityErrorTV` | 2 | fragment_checkout_info.xml | 282 | Unique | Text "Please provide your city." | Live validation |
| State/Region Field | Resource ID | `stateET` | 2 | fragment_checkout_info.xml | 370 | Unique on screen | Hint "Cornwall" | **Optional field — confirmed by source**: no `TextWatcher` attached; read only if non-empty (`validate()` line 293) |
| State/Region Error Text | Resource ID | `stateErrorTV` | 2 | fragment_checkout_info.xml | 395 | Unique | Empty text, `visibility="visible"` by default but empty | Not driven by validation logic |
| Zip Code Field | Resource ID | `zipET` | 2 | fragment_checkout_info.xml | 310 | Unique on screen | Hint "89750", `inputType="number"` | Required field |
| Zip Code Error Text | Resource ID | `zipErrorTV` | 2 | fragment_checkout_info.xml | 335 | Unique | Text "Please provide your zip" | Live validation |
| Country Field | Resource ID | `countryET` | 2 | fragment_checkout_info.xml | 422 | Unique on screen | Hint "United Kingdom" | Required field |
| Country Error Text | Resource ID | `countryErrorTV` | 2 | fragment_checkout_info.xml | 447 | Unique | Text "Please provide your" (**string is truncated in source, not a typo in our extraction** — verify against live app; may render incomplete) | Live validation |
| To Payment / Continue Button | Accessibility ID | `Saves user info for checkout` | 1 | fragment_checkout_info.xml | 476 | Unique, stable | Resource ID `paymentBtn` | **Resource ID `paymentBtn` is reused 3 times** across this screen, the Payment screen (§11), and implicitly referenced in Review Order flow — always use accessibility id or screen-scope, never bare resource-id |

---

## 11. Checkout — Payment Screen

Source: `app/src/main/res/layout/fragment_checkout.xml`, `view/fragments/CheckoutFragment.java`

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason for Selection | Alternative | Automation Notes |
|---|---|---|---|---|---|---|---|---|
| Checkout Title | Resource ID | `enterPaymentTitleTV` | 2 | fragment_checkout.xml | 26 | Unique — **different id than the Shipping Address screen's title**, despite identical "Checkout" text | Text "Checkout" | Confirms the Phase 2 device-evidence finding that each checkout step uses its own title id |
| "Enter a payment method" Subtitle | Resource ID | `enterPaymentMethodTV` | 2 | fragment_checkout.xml | 39 | Unique on screen | Text "Enter a payment method" | — |
| Payment Details Note | Resource ID | `paymentDetailsTV` | 2 | fragment_checkout.xml | 53 | Unique | Text "You will not be charged until you review your purchase on the next screen." | — |
| Card Label | Resource ID | `cardTV` | 2 | fragment_checkout.xml | 65 | Unique | Text "Card" | — |
| Visa Icon | Accessibility ID | `Visa card` | 1 | fragment_checkout.xml | 92 | Unique, stable | Resource ID `visaIV` | Static icon; not confirmed clickable/selectable in source (no `OnClickListener` found on either card icon) |
| Mastercard Icon | Accessibility ID | `Mastercard` | 1 | fragment_checkout.xml | 82 | Unique, stable | Resource ID `mastercardIV` | Same — no click handler found; treat as decorative unless proven otherwise |
| Cardholder Name Field | Resource ID | `nameET` | 2 | fragment_checkout.xml | 133 | Unique on this screen | Hint "Rebecca Winter" | **Id `nameET` also exists on the Login screen** — different screen, no conflict if scoped, but do not reuse a single global locator across both |
| Cardholder Name Error | Resource ID | `nameErrorTV` | 2 | fragment_checkout.xml | 156 | Unique on screen | Text "Value looks invalid." | **Also reused on Login screen with different text** ("Username is required") — screen-scope mandatory |
| Card Number Field | Resource ID | `cardNumberET` | 2 | fragment_checkout.xml | 206 | Unique | Hint "3258 1256 7568 7891" | Custom widget `com.uphyca.creditcardedittext.CreditCardNumberEditText`, **not a plain EditText** — confirm the automation driver can interact with this custom view type; a plain-EditText fallback exists commented out in source (lines 193-203) as evidence the team is aware of this risk |
| Card Number Error | Resource ID | `cardNumberErrorTV` | 2 | fragment_checkout.xml | 226 | Unique | Text "Value looks invalid." | — |
| Expiration Date Field | Resource ID | `expirationDateET` | 2 | fragment_checkout.xml | 275 | Unique | Hint "03/25" | Custom widget `com.uphyca.creditcardedittext.CreditCardDateEditText` — same automation-compatibility caveat as Card Number |
| Expiration Date Error | Resource ID | `expirationDateErrorTV` | 2 | fragment_checkout.xml | 295 | Unique | Text "Value looks invalid." | — |
| Security Code Field | Resource ID | `securityCodeET` | 2 | fragment_checkout.xml | 342 | Unique | Hint "123", `maxLength="3"`, `inputType="number"` | Plain EditText (not custom) |
| Security Code Info Icon | Resource ID | `questionIV` | 2 | fragment_checkout.xml | 321 | Unique | `tooltipText` = "CVV is the last three digits on the back of your credit card." | No content-desc; tooltip text is a candidate alternative for verification-only assertions |
| Security Code Error | Resource ID | `securityCodeErrorTV` | 2 | fragment_checkout.xml | 366 | Unique | Text "Value looks invalid." | — |
| Billing-Same-As-Shipping Checkbox | Accessibility ID | `Select if User billing address and shipping address are same` | 1 | fragment_checkout.xml | 384 | Unique, stable | Resource ID `billingAddressCB` | `checked="true"` by default in layout. **Confirmed behavior:** unchecking it reveals a second, hidden address-entry block (`checkoutInfoCL`, initially `visibility="gone"`) — see below |
| Separate Billing Address Block | Resource ID | `checkoutInfoCL` | 2 | fragment_checkout.xml | 391 | Unique container | — | Hidden unless `billingAddressCB` is unchecked (`CheckoutFragment.onClick` lines 326-332). Contains a **second, fully duplicated set** of `fullNameET`/`address1ET`/`address2ET`/`cityET`/`stateET`/`zipET`/`countryET`/etc. ids (lines 400-818) that are **id-duplicates of the Shipping Address screen's own fields** — this is a same-screen id collision, not just a cross-screen one. If this block is ever visible, all field ids on this screen become ambiguous without XPath/scope disambiguation by parent id |
| Review Order / Continue Button | Accessibility ID | `Saves payment info and launches screen to review checkout data` | 1 | fragment_checkout.xml | 847 | Unique, stable | Resource ID `paymentBtn` | Third reuse of `paymentBtn` id (see §10) |

---

## 12. Review Order Screen

Source: `app/src/main/res/layout/fragment_place_order.xml`

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason for Selection | Alternative | Automation Notes |
|---|---|---|---|---|---|---|---|---|
| Checkout Title | Resource ID | `checkoutTitleTV` | 3 | fragment_place_order.xml | 26 | **Id identical to Shipping Address screen's title** — same text "Checkout" this time, coincidentally | — | Screen-scope by `window`/fragment context, not by id alone |
| "Review your order" Subtitle | Resource ID | `enterShippingAddressTV` | 3 | fragment_place_order.xml | 38 | **Id reused from Shipping Address screen's subtitle id**, different text here ("Review your order" vs "Enter a shipping address") | Text "Review your order" | Confirms the exact resource-id-reuse pattern already flagged from Phase 2 device evidence |
| Order Item List | Accessibility ID | `Displays list of selected products` | 3 | fragment_place_order.xml | 55 | **Accessibility string reused** from the Cart screen's `productRV` | Resource ID `placeOrderRV` | Resource ID is unique to this screen even though the accessibility string is not — prefer Resource ID here |
| Order Item — Product Name | Resource ID | `titleTV` | 3 | fragment_place_order.xml | (bound in RecyclerView item, shares `item_my_cart`-style structure inline at lines 90-102 range via generated binding) | Repeats per item / reused id family | — | Qualify with RecyclerView index |
| Order Item — Product Price | Resource ID | `priceTV` | 3 | fragment_place_order.xml | (bound) | Repeats per item | — | Qualify with index |
| Deliver Address Section Label | Text only (static, no id) | `Deliver Address` | 4 | fragment_place_order.xml | 81 | No `android:id` assigned to this TextView | — | Static label, no id — text match is the only option unless a new id is added |
| Full Name (shipping summary) | Resource ID | `fullNameTV` | 2 | fragment_place_order.xml | 90 | Unique on screen | — | — |
| Street Address (shipping summary) | Resource ID | `addressTV` | 2 | fragment_place_order.xml | 97 | Unique on screen | — | — |
| City (shipping summary) | Resource ID | `cityTV` | 3 | fragment_place_order.xml | 104 | **Id reused from Shipping Address screen's `cityTV` label** | — | Different role here (data display, not a label) — screen-scope required |
| Country/Zip (shipping summary) | Resource ID | `countryTV` | 3 | fragment_place_order.xml | 111 | Id reused from Shipping Address screen's `countryTV` label | — | Screen-scope required |
| Payment Method Section Label | Text only (static, no id) | `Payment Method` | 4 | fragment_place_order.xml | 131 | No id assigned | — | — |
| Cardholder Name (payment summary) | Resource ID | `cardHolderTV` | 2 | fragment_place_order.xml | 140 | Unique on screen | — | — |
| Card Number (payment summary) | Resource ID | `cardNumberTV` | 3 | fragment_place_order.xml | 147 | **Id reused from Payment screen's `cardNumberTV` label** | — | Different role (display vs. label) |
| Expiration Date (payment summary) | Resource ID | `expirationDateTV` | 3 | fragment_place_order.xml | 154 | Id reused from Payment screen's label | — | Screen-scope required |
| Billing-Address-Same Note | Resource ID | `billingAddressTV` | 2 | fragment_place_order.xml | 161 | Unique on screen | Text "Billing address is the same as shipping address" | Only meaningful text when billing == shipping; separate billing block (`billingAddressLL`, `visibility="gone"` by default) exists for the alternate case — id `billFullnameTV`/`billaddressTV`/`billingCityAndStateTV`/`billingZipAndCountryTV` |
| DHL Shipping Method Label | Resource ID | `dhlTV` | 2 | fragment_place_order.xml | 232 | Unique | Text "DHL Standard Delivery" | Confirmed as the only shipping-method row in source — no selector/alternative options exist in this layout |
| Shipping Cost | Resource ID | `amountTV` | 2 | fragment_place_order.xml | 244 | Unique | Text bound to `@string/delivery_amount` = "$5.99" (hardcoded string resource, not computed) | Not dynamically calculated — confirmed as a static string resource |
| Arrival Estimate | Resource ID | `arrivalTV` | 2 | fragment_place_order.xml | 256 | Unique | Text "Estimated to arrive within 3 weeks." | Static |
| Total Items Count | Resource ID | `itemNumberTV` | 2 | fragment_place_order.xml | 347 | Unique | — | Empty by default, set at runtime |
| Total Amount | Resource ID | `totalAmountTV` | 2 | fragment_place_order.xml | 356 | Unique | — | Empty by default, set at runtime |
| Place Order Button | Accessibility ID | `Completes the process of checkout` | 1 | fragment_place_order.xml | 380 | Unique, stable | Resource ID `paymentBtn` | Fourth/final reuse of `paymentBtn` id across the checkout flow |

---

## 13. Checkout Complete Screen

Source: `app/src/main/res/layout/fragment_checkout_complete.xml`

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason for Selection | Alternative | Automation Notes |
|---|---|---|---|---|---|---|---|---|
| "Checkout Complete" Title | Resource ID | `completeTV` | 2 | fragment_checkout_complete.xml | 18 | Unique | Text "Checkout Complete" | — |
| "Thank you for your order" | Resource ID | `thankYouTV` | 2 | fragment_checkout_complete.xml | 32 | Unique | — | — |
| "Your new swag is on its way" | Resource ID | `swagTV` | 2 | fragment_checkout_complete.xml | 43 | Unique | — | — |
| Order Dispatched Message | Resource ID | `orderTV` | 2 | fragment_checkout_complete.xml | 57 | Unique | Text "Your order has been dispatched and will arrive as fast as the pony gallops!" | — |
| Continue Shopping Button | Accessibility ID | `Tap to open catalog` | 1 | fragment_checkout_complete.xml | 76 | Unique, stable | Resource ID `shoopingBt` | **Note the id is misspelled in source** (`shoopingBt`, not `shoppingBt`) — record verbatim; confirmed identical in device evidence from Phase 2 |

**Cart badge note:** confirmed by both source review and Phase 2 device evidence that the cart badge (`cartTV`/`cartCircleRL`) does **not** reset to empty on this screen — it persists showing the pre-checkout count. No source code was found that clears `ST.cartItemList` on order completion within the files reviewed; treat as a structural observation, not an assumed defect.

---

## 14. Navigation Drawer

Source: `navigation_layout.xml`, `menu_header_layout.xml`, `menu_item.xml`, `view/adapters/MenuAdapter.java`, `model/MenuItem.java`, `view/activities/MainActivity.java` (menu construction: lines 244-271; click routing: lines 273-335)

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason for Selection | Alternative | Automation Notes |
|---|---|---|---|---|---|---|---|---|
| Drawer Menu List (RecyclerView) | Accessibility ID | `Recycler view for menu` | 1 | navigation_layout.xml | 12 | Unique | Resource ID `menuRV` | — |
| Drawer Item Row | Resource ID | `itemTV` | 3 | menu_item.xml | 20 | **Repeats identically for all 11 rows** | — | Must combine with row index or exact item text. Row order below is fixed by construction order in `MainActivity.setMenu()` |
| Row 0 — Catalog | Resource ID + text | `itemTV` filtered by text "Catalog" | 3 | MainActivity.java | 246 | No unique content-desc for this row | Index 0 | Destination: `FRAGMENT_PRODUCT_CATAlOG` (same activity, position 0 in `handleMenuClick`) |
| Row 1 — WebView | Resource ID + text | `itemTV` filtered by text "WebView" | 3 | MainActivity.java | 247 | No unique content-desc | Index 1 | Destination: `FRAGMENT_WEB_ADDRESS` (the URL-entry screen, §15) |
| Row 2 — QR Code Scanner | Resource ID + text | `itemTV` filtered by text "QR Code Scanner" | 3 | MainActivity.java | 248 | No unique content-desc | Index 2 | Destination: `FRAGMENT_QR`; triggers a runtime camera permission dialog |
| Row 3 — Geo Location | Resource ID + text | `itemTV` filtered by text "Geo Location" | 3 | MainActivity.java | 249 | No unique content-desc | Index 3 | Destination: `FRAGMENT_GEO_LOCATION`; triggers a runtime location permission dialog |
| Row 4 — Drawing | Resource ID + text | `itemTV` filtered by text "Drawing" | 3 | MainActivity.java | 250 | No unique content-desc | Index 4 | Destination: `FRAGMENT_DRAWING`; triggers a runtime storage permission dialog |
| Row 5 — About | Resource ID + text | `itemTV` filtered by text "About" | 3 | MainActivity.java | 251 | No unique content-desc | Index 5 | Destination: `FRAGMENT_ABOUT` |
| Row 6 — Reset App State | Resource ID + text | `itemTV` filtered by text "Reset App State" | 3 | MainActivity.java | 252 | No unique content-desc | Index 6 | **Not a navigation** — calls `showResetDialog()` directly (§17); confirms Phase 2 finding that this item is dialog-only, no dedicated screen |
| Row 7 — FingerPrint | Resource ID + text | `itemTV` filtered by text "FingerPrint" | 3 | MainActivity.java | 253 | No unique content-desc | Index 7 | Destination: `FRAGMENT_BIOMETRICS` |
| Row 8 — Virtual USB | Resource ID + text | `itemTV` filtered by text "Virtual USB" | 3 | MainActivity.java | 254 | No unique content-desc | Index 8 | Destination: separate **Activity** (`VirtualUsbActivity`), not a fragment — confirmed structurally different navigation mechanism from all other rows |
| Row 9 — Crash app (debug) | — | — | — | MainActivity.java | 255, 320 | **Excluded from this repository per explicit scope instruction** | — | Launches `DebugCrashActivity`, which intentionally terminates the app. Not documented further; do not automate |
| Row 10 — Log Out / Log In (state-dependent) | Resource ID + text/content-desc | `itemTV`; content-desc `"Logout Menu Item"` when logged in, `"Login Menu Item"` when logged out | 1 (when logged in) / 3 (when logged out, text-only) | MainActivity.java | 256-260 | Logout state has a unique content-desc; Login state does not | Index 10 | **This row's label and destination change based on `ST.isLogin`** — logged-in shows "Log Out" (opens logout confirmation dialog, §17); logged-out shows "Log In" (navigates to `FRAGMENT_LOGIN`). Automation must check state before asserting label/behavior |

---

## 15. Drawer Destination Screens

### 15.1 WebView (URL Entry)
Source: `fragment_web_address.xml`, `view/fragments/WebAddressFragment.java`

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason | Alternative | Notes |
|---|---|---|---|---|---|---|---|---|
| Screen Title | Resource ID | `webViewTV` | 2 | fragment_web_address.xml | 12 | Unique | Text "Webview" | — |
| URL Label | Resource ID | `urlTV` | 2 | fragment_web_address.xml | 25 | Unique | Text "URL" | — |
| URL Input Field | Resource ID | `urlET` | 2 | fragment_web_address.xml | 47 | Unique | Hint "https://www.website.com", `inputType="textUri"` | — |
| URL Error Text | Resource ID | `urlErrorTV` | 2 | fragment_web_address.xml | 72 | Unique | Text "Please provide a correct https url." | — |
| Instruction Text | Resource ID | `enterTV` | 2 | fragment_web_address.xml | 84 | Unique | Text "Enter an HTTPS url" | — |
| Go To Site Button | Accessibility ID | `Tap to view content of given url` | 1 | fragment_web_address.xml | 104 | Unique, stable | Resource ID `goBtn` | **Content-desc is identical to the "About" screen's website link** — screen-scope if querying globally |

### 15.2 WebView (Content Rendering)
Source: `fragment_web_view.xml`

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason | Alternative | Notes |
|---|---|---|---|---|---|---|---|---|
| Loading Spinner | Resource ID | `loadingIV` | 2 | fragment_web_view.xml | 11 | Unique | Accessibility "App logo" (generic, shared) | — |
| Loading Text | Resource ID | `loadingTV` | 2 | fragment_web_view.xml | 22 | Unique | Text "Loading ..." | — |
| WebView Content | Resource ID | `webView` | 2 | fragment_web_view.xml | 36 | Unique | — | `visibility="gone"` until content loads |
| Error State Container | Resource ID | `errorCL` | 2 | fragment_web_view.xml | 46 | Unique | — | `visibility="gone"` by default |
| Error Title | Resource ID | `errorTV` | 2 | fragment_web_view.xml | 55 | Unique | Default text "Error Loading Page" | — |
| Error Domain | Resource ID | `domainTV` | 2 | fragment_web_view.xml | 64 | Unique | Default text "Domain: Undefined" | — |
| Error Code | Resource ID | `errorCodeTV` | 2 | fragment_web_view.xml | 75 | Unique | — | Populated at runtime |
| Error Name | Resource ID | `errorNameTV` | 2 | fragment_web_view.xml | 84 | Unique | — | Populated at runtime |

### 15.3 QR Code Scanner
Source: `fragment_qr.xml`

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason | Alternative | Notes |
|---|---|---|---|---|---|---|---|---|
| Screen Title | Resource ID | `qrCodeTV` | 2 | fragment_qr.xml | 8 | Unique | Text "QR Code Scanner" | — |
| Camera Preview | Resource ID | `previewView` | 2 | fragment_qr.xml | 20 | Unique | Class `androidx.camera.view.PreviewView` | No content-desc |
| Camera Permission Dialog | Platform (system) | `com.android.permissioncontroller:id/permission_allow_foreground_only_button`, `..._one_time_button`, `..._deny_button` | 2 | N/A (Android OS) | N/A | Standard platform permission dialog, not app-owned | Text "While using the app" / "Only this time" / "Don't allow" | Confirmed matching Phase 2 device evidence exactly |

### 15.4 Geo Location
Source: `fragment_location.xml`

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason | Alternative | Notes |
|---|---|---|---|---|---|---|---|---|
| Screen Title | Resource ID | `locationTV` | 2 | fragment_location.xml | 11 | Unique | Text "Geo Location" | — |
| Details Text | Resource ID | `locationDetailsTV` | 2 | fragment_location.xml | 26 | Unique | — | — |
| "This link" Hyperlink | Accessibility ID | `Tap to visit given web address` | 1 | fragment_location.xml | 56 | Unique, stable | Resource ID `linkedTV` | — |
| Determining-Position Note | Resource ID | `extraTextTV` | 2 | fragment_location.xml | 65 | Unique | Text "Determining the position on Android can take a while." | — |
| Latitude Value | Resource ID | `latitudeTV` | 2 | fragment_location.xml | 124 | Unique | — | Populated at runtime, e.g. `23.7976321` |
| Longitude Value | Resource ID | `longitudeTV` | 2 | fragment_location.xml | 156 | Unique | — | Populated at runtime |
| Start Observing Button | Accessibility ID | `Start observation of user location` | 1 | fragment_location.xml | 204 | Unique, stable | Resource ID `startBtn` | — |
| Stop Observing Button | Accessibility ID | `Stop observation of user location` | 1 | fragment_location.xml | 217 | Unique, stable | Resource ID `stopBtn` | — |

### 15.5 Drawing
Source: `fragment_drawing.xml`

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason | Alternative | Notes |
|---|---|---|---|---|---|---|---|---|
| Screen Title | Resource ID | `drawingTV` | 2 | fragment_drawing.xml | 12 | Unique | Text "Drawing" | — |
| Drawing Pad Background | Accessibility ID | `Background for drawing pad` | 1 | fragment_drawing.xml | 79 | Unique | Resource ID `padBackgroundIV` | — |
| Drawing/Signature Pad | Accessibility ID | `Pad to draw on` | 1 | fragment_drawing.xml | 94 | Unique | Resource ID `signature_pad` | Custom widget `com.williamww.silkysignature.views.SignaturePad` |
| Clear Button | Accessibility ID | `Removes anything drawn on pad` | 1 | fragment_drawing.xml | 119 | Unique, stable | Resource ID `clearBtn` | — |
| Save Button | Accessibility ID | `Save anything drawn on pad` | 1 | fragment_drawing.xml | 130 | Unique, stable | Resource ID `saveBtn` | — |

### 15.6 About
Source: `fragment_about.xml`

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason | Alternative | Notes |
|---|---|---|---|---|---|---|---|---|
| Screen Title | Resource ID | `aboutTV` | 2 | fragment_about.xml | 11 | Unique | Text "About" | — |
| Version Text | Resource ID | `versionTV` | 2 | fragment_about.xml | 40 | Unique | Hardcoded placeholder "V.1.0.0-build 1 by" in source (device evidence showed "V.2.2.0-build 25" — value is overwritten at runtime/build time; do not hardcode either in assertions) | — | `clickable="true"` — no click handler was found in `AboutFragment.java` within the scope reviewed; treat as informational unless confirmed otherwise |
| "Go to the Sauce Labs website." Link | Accessibility ID | `Tap to view content of given url` | 1 (id 3 for screen-scope) | fragment_about.xml | 73 | Content-desc identical to the WebAddress screen's "Go To Site" button | Resource ID `webTV` | Prefer Resource ID here since the accessibility string is reused |

### 15.7 FingerPrint (Biometrics)
Source: `fragment_biometric.xml`

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason | Alternative | Notes |
|---|---|---|---|---|---|---|---|---|
| Screen Title | Resource ID | `bioMetricTV` | 2 | fragment_biometric.xml | 11 | Unique | Text "FingerPrint" | — |
| Enable Biometric Switch | Accessibility ID | `Enable or disable biometric login` | 1 | fragment_biometric.xml | 31 | Unique, stable | Resource ID `bioMetricSw` | `checked` state toggle |
| Info Text | Resource ID | `bioMetricInfoTV` | 2 | fragment_biometric.xml | 40 | Unique | — | — |
| Demo Disclaimer Text | Resource ID | `bioMetricDemoInfoTV` | 2 | fragment_biometric.xml | 54 | Unique | Explicit disclaimer that this screen would normally sit behind secure login | — | — |

### 15.8 Virtual USB (Activity, not Fragment)
Source: `activity_virtual_usb.xml`

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason | Alternative | Notes |
|---|---|---|---|---|---|---|---|---|
| Dynamic Status Message | Resource ID | `virtual_usb_message` | 2 | activity_virtual_usb.xml | 57 | Only id-bearing element on the screen | Default text "DUCK DUCK" | All other text on this screen is static and id-less; use text-match (Priority 4) only if needed for those |

---

## 16. Star Rating Review Dialog

Source: `app/src/main/res/layout/dialog_review.xml`. Triggered from `ProductsAdapter`/`ProductDetailFragment` star-tap handlers via `ST.showReviewDialog(mAct)`.

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason | Alternative | Notes |
|---|---|---|---|---|---|---|---|---|
| Confirmation Message | Resource ID | `sortTV` | 2 | dialog_review.xml | 18 | Unique within this dialog | Text "Thank you for submitting your review!" | **Id reused from Sort Dialog's title id** (`sortTV`) — this is a different dialog layout file, so no runtime collision, but flag for anyone maintaining both dialogs |
| Continue/Close Button | Accessibility ID | `Closes review dialog` | 1 | dialog_review.xml | 38 | Unique, stable | Resource ID `closeBt` | Text "Continue" |

---

## 17. Reset App State / Logout Dialogs

These are **standard Android platform `AlertDialog`** instances (title/message/positive/negative button pattern), built via `AlertDialog.Builder` in `MainActivity.java` (`showResetDialog()`, `showLogoutAlertDialog()` — called from drawer rows 6 and 10 respectively). No custom layout XML exists for these; they use the platform's default alert dialog template.

| Element Name | Locator Type | Locator Value | Priority | Source File | Line | Reason | Alternative | Notes |
|---|---|---|---|---|---|---|---|---|
| Reset — Dialog Title | Platform Resource ID | `android:id/alertTitle` | 2 | N/A (Android platform) | N/A | Standard framework id | Text "Reset App State" | Confirmed matching Phase 2 device evidence |
| Reset — Message | Platform Resource ID | `android:id/message` | 2 | N/A | N/A | Standard framework id | Text "Are you sure you want to Reset the App" | — |
| Reset — Cancel Button | Platform Resource ID | `android:id/button2` | 2 | N/A | N/A | Standard framework id | Text "CANCEL" | — |
| Reset — Confirm Button | Platform Resource ID | `android:id/button1` | 2 | N/A | N/A | Standard framework id | Text "RESET APP" | — |
| Logout — Dialog Title | Platform Resource ID | `android:id/alertTitle` | 2 | N/A | N/A | Standard framework id | Text "Log Out" | **Same platform id as Reset dialog's title** — the two dialogs are never shown simultaneously, so no runtime collision, but any locator relying on `android:id/alertTitle` alone must be scoped to "whichever dialog is currently foregrounded," not assumed to be one or the other |
| Logout — Message | Platform Resource ID | `android:id/message` | 2 | N/A | N/A | Standard framework id | Text "Are you sure you want to logout" | Same collision caveat as above |
| Logout — Cancel Button | Platform Resource ID | `android:id/button2` | 2 | N/A | N/A | Standard framework id | Text "CANCEL" | — |
| Logout — Confirm Button | Platform Resource ID | `android:id/button1` | 2 | N/A | N/A | Standard framework id | Text "LOGOUT" | — |

---

## 18. Validation & System Messages

Consolidated from `strings.xml` and the `TextWatcher`/`onClick` validation logic reviewed in `LoginFragment.java`, `CheckoutInfoFragment.java`, and `CheckoutFragment.java`.

| Context | Message Text | String Resource | Trigger | Locator (of the message element) |
|---|---|---|---|---|
| Login — empty username | "Username is required" | `username_is_required` | Tap Login with blank username field | `nameErrorTV` (fragment_login.xml) |
| Login — empty password | "Enter Password" | `enter_password` | Tap Login with username filled, password blank | `passwordErrorTV` (fragment_login.xml) — **text is set dynamically in code**, not the static layout default |
| Login — locked-out user | "Sorry this user has been locked out." | `soory_this_user_has_been_locked_out` | Submitting `alice@example.com` as username | `passwordErrorTV` (fragment_login.xml) — same element, different dynamically-set text; confirms `alice@example.com` is source-hardcoded as the locked-out account, matching Phase 2 device evidence exactly |
| Logout confirmation (informational, currently unused) | "You are Successfully logged out." | `you_have_successfully_loggedout` | `showAlert()` in `LoginFragment.java` — method exists but its only call site (line 82) is commented out | N/A — **not currently reachable in the shipped app flow**; flagging as dead code, not a test target |
| Shipping — Full Name empty | "Please provide your full name." | (hardcoded string, not a string resource) | Live as-you-type / on Continue tap | `fullNameErrorTV` (fragment_checkout_info.xml) |
| Shipping — Address Line 1 empty | "Please provide your address." | (hardcoded) | Live / on Continue tap | `address1ErrorTV` |
| Shipping — City empty | "Please provide your city." | (hardcoded) | Live / on Continue tap | `cityErrorTV` |
| Shipping — Zip empty | "Please provide your zip" | (hardcoded) | Live / on Continue tap | `zipErrorTV` |
| Shipping — Country empty | "Please provide your" (string appears truncated in source — verify live rendering) | (hardcoded) | Live / on Continue tap | `countryErrorTV` |
| Payment — any of Name/Card Number/Expiry/Security Code empty | "Value looks invalid." | `value_looks_invalid` | Live as-you-type / on Review Order tap | `nameErrorTV`, `cardNumberErrorTV`, `expirationDateErrorTV`, `securityCodeErrorTV` (fragment_checkout.xml) — **same message text reused across all four fields**, differentiate by which error TextView is visible |
| WebView — invalid URL | "Please provide a correct https url." | `please_provide_a_correct_https_url` | Submitting a non-HTTPS or malformed URL | `urlErrorTV` (fragment_web_address.xml) |
| WebView — page load failure (Toast) | "Oh no!!!" + description | `oh_no` (prefix) | WebView content fails to load | Toast — **not a persistent element**, cannot be located by resource-id; must be caught via `-android uiautomator` `new UiSelector().textContains("Oh no")` within its short visibility window, or via platform Toast-detection APIs |
| About — no browser available (Toast) | "No application can handle this request. Please install a web browser or check your URL." | (hardcoded, not a string resource) | Tapping the website link with no browser app installed | Toast — same transient-element caveat as above |
| Debug Crash — Backtrace not initialized (Toast) | "Backtrace client not initialized, cannot crash app" | (hardcoded) | Only relevant to the excluded Crash app (debug) item | Not documented further — out of scope |

---

## 19. Dynamic Element Locator Patterns

Reusable strategies for elements that cannot be located by a single static locator, to be implemented once in the Page Object base layer and reused everywhere:

1. **RecyclerView item by index** — `productRV` (Catalog), `productRV` (Cart), `placeOrderRV` (Review Order), `colorRV` (Product Details), `menuRV` (Drawer). Pattern: locate the RecyclerView by its container-level Accessibility ID or Resource ID, then resolve children by `instance(n)`/child index. Never assume a fixed total count — catalog size, cart size, and color count are all data-driven.

2. **RecyclerView item by matching child text** — needed whenever a specific product/cart-row/menu-row must be targeted (e.g., "the row whose `titleTV` reads 'Sauce Labs Backpack (red)'"). Required because `titleTV`/`priceTV`/`itemTV` ids repeat identically across every row (confirmed at the adapter level in `ProductsAdapter.java`, `CartItemAdapter.java`, `MenuAdapter.java`).

3. **Screen-scoped resource-id disambiguation** — required for every id flagged "reused" in this document (`productTV`, `nameET`, `nameErrorTV`, `paymentBtn`, `checkoutTitleTV`, `enterShippingAddressTV`, `cityTV`, `countryTV`, `cardNumberTV`, `expirationDateTV`, `cartBt`, `fullNameTV`/`fullNameET`/`fullNameRL`/etc. inside the hidden billing block). Page Objects must always resolve these relative to a screen-root element, never as a bare global `By.id()`.

4. **Dynamic-content-desc matching** — Color swatches (`Black color`/`Green color`/`Gray color`/`Blue color`/`Unknown color`) are constructed at runtime from the product's actual `ColorModel` value (`ColorsAdapter.java` lines 81-98). Automation must treat these as a **known enum of possible values**, not a single fixed string, and must handle `"Unknown color"` as a valid, source-confirmed outcome for non-standard color values — not an error state.

5. **Visibility-based state assertions, not text-based** — Cart badge presence (`cartCircleRL`), empty-cart vs. populated-cart containers (`noItemCL` vs. `cartCL`), and validation error rows (`*ErrorTV`/`*ErrorIV` pairs) all use `visibility` toggling (`VISIBLE`/`GONE`/`INVISIBLE`) rather than being added/removed from the tree in most cases — except the cart badge subtree, which is confirmed fully absent from the tree (not merely hidden) when the cart is empty, per both source (`MainActivity.setData()`) and Phase 2 device evidence.

6. **Custom widget classes** — `CreditCardNumberEditText` and `CreditCardDateEditText` (Payment screen) and `SignaturePad` (Drawing screen) are third-party custom Views, not stock Android widgets. Confirm the automation driver's element-interaction methods (text entry, gesture drawing) work against these before relying on them in the pilot.

---

## 20. Cross-Validation Summary

### 20.1 Confirmed Duplicate Resource-IDs (cross-screen)

| Resource ID | Appears In | Risk | Required Mitigation |
|---|---|---|---|
| `productTV` | Product Catalog (title), Cart (title) | High — different text, different screen | Screen-scope |
| `titleTV` | Catalog item card, Cart item card, Review Order item (implicitly) | High — repeats per RecyclerView item too | Screen-scope + index |
| `priceTV` | Same as above | High | Screen-scope + index |
| `cartBt` | Product Details ("Add to Cart"), Cart ("Proceed To Checkout") | High — opposite semantic actions | Accessibility ID, not resource-id |
| `paymentBtn` | Shipping ("To Payment"), Payment ("Review Order"), Review Order ("Place Order") | High — three different actions, one id | Accessibility ID, not resource-id |
| `checkoutTitleTV` | Shipping, Review Order | Medium — same text both times, coincidentally | Screen-scope for future-proofing |
| `enterShippingAddressTV` | Shipping ("Enter a shipping address"), Review Order ("Review your order") | High — different text | Screen-scope |
| `nameET` | Login, Payment (cardholder name) | Medium — different screens, unlikely to collide at runtime | Screen-scope |
| `nameErrorTV` | Login, Payment | Medium — different messages | Screen-scope |
| `fullNameET`/`fullNameRL`/`fullNameTV`/etc. | Shipping Address screen **and** the hidden billing-address block within the Payment screen (same file) | High — same-file, same-screen collision if billing block is ever shown | Requires parent-id qualification (`checkoutInfoCL` ancestor) if the billing block becomes reachable |
| `cardNumberTV`, `expirationDateTV` | Payment screen (field label), Review Order screen (data display) | Medium | Screen-scope |
| `cityTV`, `countryTV` | Shipping Address (label), Review Order (data display) | Medium | Screen-scope |
| `android:id/alertTitle`, `android:id/message`, `android:id/button1`, `android:id/button2` | Reset dialog, Logout dialog | Low (never simultaneous) | Scope to "currently foregrounded dialog" |

### 20.2 Weak / Not-Recommended Locators Identified

| Element | Why Weak | Recommended Alternative |
|---|---|---|
| Rating stars (`start1IV`–`start5IV`), all screens | No `contentDescription` at all — confirmed intentional in source comment ("We intentionally removed all content descriptions on images to showcase how accessibility bugs look", `ratting_layout.xml` line 14) | Resource ID + index + drawable-state assertion; do not attempt an accessibility-id strategy here, it will never work |
| "Deliver Address" / "Payment Method" section labels (Review Order) | No `android:id` at all | Text match (Priority 4) only |
| Card type icons (Visa/Mastercard) | No confirmed click handler in source; treat as non-interactive | Do not build a "select card type" automation step unless further evidence emerges |
| Toast messages (WebView load failure, About no-browser, Debug-crash) | Transient, no resource-id, short-lived | `-android uiautomator` text-contains within a short explicit wait window; do not rely on presence checks with default implicit wait |
| `versionTV` (About screen) | Hardcoded placeholder text in source differs from actual runtime-rendered value seen in device evidence | Do not assert exact string; assert pattern/prefix only if needed |

### 20.3 No Duplicate Locators Within a Single Screen's Static (non-repeating) Elements

Aside from the RecyclerView-repeated ids (expected and documented above) and the hidden-billing-block collision noted in §20.1, no other same-screen duplicate resource-ids were found across the reviewed layout files.

---

## 21. Naming Convention Mapping

| Raw App Resource-ID | Enterprise Page-Object Name |
|---|---|
| `nameET` (Login) | `loginUsernameField` |
| `passwordET` (Login) | `loginPasswordField` |
| `loginBtn` | `loginButton` |
| `productRV` (Catalog) | `catalogProductList` |
| `titleTV` (Catalog item) | `catalogProductName` |
| `priceTV` (Catalog item) | `catalogProductPrice` |
| `cartTV` | `catalogCartBadge` |
| `sortIV` | `catalogSortButton` |
| `minusIV` (Product Details) | `productQuantityDecreaseButton` |
| `plusIV` (Product Details) | `productQuantityIncreaseButton` |
| `cartBt` (Product Details) | `productAddToCartButton` |
| `cartBt` (Cart) | `cartCheckoutButton` |
| `removeBt` (Cart item) | `cartItemRemoveButton` |
| `paymentBtn` (Shipping) | `shippingContinueButton` |
| `paymentBtn` (Payment) | `paymentReviewButton` |
| `paymentBtn` (Review Order) | `reviewPlaceOrderButton` |
| `shoopingBt` (Checkout Complete) | `checkoutCompleteContinueShoppingButton` |
| `menuIV` | `headerMenuButton` |
| `cartRL` | `headerCartButton` |

*(Full mapping for every element is derivable directly from the "Element Name" column throughout Sections 5–17 using the `<screen><Element><Type>` convention in Section 3; the table above covers the highest-traffic elements explicitly called out in the task instructions.)*

---

## 22. Change Log

| Version | Date | Change |
|---|---|---|
| v1.0 | 2026-07-31 | Initial locator repository built from full source-code analysis of commit `8cf5fac`. Supersedes all locator values previously inferred from device inspection alone; device evidence retained only as corroboration, not as primary source. |
