---
document_id: MA-AA-001
title: AUT Analysis
version: v1.0
status: Draft
author: Project Owner / Repository Maintainer
created_date: 2026-07-29
last_updated: 2026-07-29
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
aut_name: Sauce Labs Android Demo App
aut_version: 2.2.0
related_documents: [MA-PV-001]
classification: Internal
---

# MA-AA-001 — AUT Analysis

**Mobile Automation Framework**

| Field | Value |
|---|---|
| Document ID | MA-AA-001 |
| Document Name | AUT Analysis |
| Version | v1.0 |
| Status | Draft |
| Project | Mobile Automation Framework |
| Project Code | MA |
| AUT | Sauce Labs Android Demo App |
| AUT Version | 2.2.0 |
| Platform | Android |
| Classification | Internal |

---

## Version History

| Version | Date | Author | Change Description |
|---|---|---|---|
| v0.1 | 2026-07-29 | Project Owner | Initial draft from screenshot-based evidence review |
| v1.0 | 2026-07-29 | Project Owner | First baseline version submitted for approval |

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Document Author | Project Owner / Repository Maintainer | Submitted | 2026-07-29 |
| Reviewer | Pending | Pending | — |
| Approver | Pending | Pending | — |

**Evidence basis:** This document is derived exclusively from screenshot evidence of the AUT provided by the document author. No source code, official documentation, internet search, or prior model knowledge of the Sauce Labs Android Demo App was used. Any application behavior not visibly confirmed in the supplied screenshots is marked **Not Observed**.

---

## 1. Purpose

This document records a factual, evidence-based understanding of the Sauce Labs Android Demo App (v2.2.0) prior to requirements definition, test strategy, and framework design. Per the documentation-first methodology established in [MA-PV-001], no analysis or design decision downstream of this document may assume application behavior that is not either confirmed here as Observed or explicitly logged as Not Observed. This document is descriptive only — it does not define requirements, test scope, test scenarios, or automation approach.

## 2. Application Overview

The AUT is a native Android application (v2.2.0) presenting a product shopping workflow: users browse a product catalog, view product details, manage a cart, authenticate, and complete a multi-step checkout culminating in an order confirmation. In addition to the shopping workflow, the application exposes a set of utility/demo screens (WebView, QR Code Scanner, Geo Location, Drawing, FingerPrint, Virtual USB, Crash App, Reset App State) reachable from the navigation drawer, alongside the shopping flow. Not Observed: whether these utility screens relate functionally to the shopping workflow or are independent demonstration modules.

## 3. Application Type

| Classification | Basis |
|---|---|
| Native Mobile Android Application | Observed: navigation drawer, native UI controls throughout all screens |
| E-commerce / Shopping Workflow Demo | Observed: catalog → product details → cart → checkout (shipping, payment, review) → order completion |
| Test/Demo Utility Application (secondary characteristic) | Observed: drawer items unrelated to commerce — WebView, QR Code Scanner, Geo Location, Drawing, FingerPrint, Virtual USB, Crash App (Debug), Reset App State |

## 4. Primary Business Objective

The visible objective is to simulate a complete mobile commerce transaction: product discovery, selection, cart management, authentication, and a multi-step checkout ending in order confirmation. Not Observed: any real payment processing, order fulfillment, or backend business logic — only the UI-level workflow is confirmed from the screenshots provided.

## 5. Major Functional Modules

| Module | Evidence Basis |
|---|---|
| Product Catalog | Product listing, grid, images, names, prices, ratings, sort control |
| Product Details | Product image, title, price, rating, color selection, quantity selector, Add to Cart, highlights |
| Shopping Cart | Cart badge, cart items, summary, quantity controls, remove item, total, Proceed to Checkout |
| Authentication (Login) | Username field, password field, login button, sample credentials |
| Checkout — Shipping | Full Name, Address Line 1/2, City, State/Region, Zip Code, Country, To Payment action |
| Checkout — Payment | Credit/Debit card entry, Continue navigation |
| Order Review | Order summary, Continue flow |
| Order Completion | Place Order action, Checkout Complete / Order Completed confirmation |
| Navigation Drawer | Catalog, WebView, QR Code Scanner, Geo Location, Drawing, About, Reset App State, FingerPrint, Virtual USB, Crash App (Debug), Login |
| Utility Features | WebView, QR Code Scanner, Geo Location, Drawing, FingerPrint, Virtual USB, Crash App (Debug), Reset App State — content of each Not Observed beyond drawer label |

## 6. Screen Inventory

| Screen Name | Primary Purpose | Key Components | Navigation |
|---|---|---|---|
| Product Catalog | Browse available products | Product grid, images, names, prices, ratings, sort button, cart icon, hamburger menu, footer, social media icons | Tap product → Product Details; tap cart icon → Cart; tap hamburger icon → Navigation Drawer |
| Product Details | View a single product and add it to cart | Product image, title, price, rating, color selection, quantity selector, Add to Cart button, product highlights, scrollable content | Back → Product Catalog; Add to Cart destination screen Not Observed (may remain on same screen or update Cart state) |
| Navigation Drawer | Access primary and utility sections | Catalog, WebView, QR Code Scanner, Geo Location, Drawing, About, Reset App State, FingerPrint, Virtual USB, Crash App (Debug), Login | Each item navigates to its respective screen; individual destination screens beyond Catalog and Login Not Observed |
| Shopping Cart | Review selected items before checkout | Cart badge, cart item(s), product summary, quantity controls, remove item, total, Proceed to Checkout button | Proceed to Checkout → Login and/or Shipping Address (exact sequence Not Observed) |
| Login | Authenticate the user | Username field, password field, login button, sample credentials | Destination after successful login Not Observed; login is also reachable directly from the Navigation Drawer |
| Shipping Address | Capture delivery address for checkout | Full Name, Address Line 1, Address Line 2, City, State/Region, Zip Code, Country, "To Payment" button | To Payment → Payment Method screen |
| Payment Method | Capture payment details for checkout | Credit/Debit card information, card entry fields, Continue navigation | Continue → Review Order screen |
| Review Order | Review complete order before submission | Order summary, Continue flow | Leads to Order Completion; exact screen hosting the "Place Order" trigger Not Observed |
| Order Completion | Confirm order was placed successfully | Place Order action, "Checkout Complete" / "Order Completed" message, success confirmation | Terminal screen; further navigation Not Observed |

## 7. User Journey

```
Launch
  ↓
Product Catalog (Browse Products)
  ↓
Product Details
  ↓
Add to Cart
  ↓
Shopping Cart
  ↓
Login (sequence relative to "Proceed to Checkout" Not Observed)
  ↓
Shipping Address
  ↓
Payment Method
  ↓
Review Order
  ↓
Place Order
  ↓
Order Completion
```

The Navigation Drawer provides a parallel access path to Login and to utility screens (WebView, QR Code Scanner, Geo Location, Drawing, FingerPrint, Virtual USB, Crash App, Reset App State, About) outside this primary linear journey. Not Observed: whether utility screens return to Catalog or maintain their own back-navigation.

## 8. Key UI Components

- Buttons (Add to Cart, Proceed to Checkout, Login, To Payment, Continue, Place Order)
- Input fields (text entry — login, shipping address, payment)
- Icons (cart icon, hamburger/menu icon, social media icons)
- Product Cards (image, name, price, rating)
- Rating indicator
- Cart Badge (item count)
- Navigation Drawer
- Quantity Selector / Controls
- Color Selector
- Sort control
- Footer

## 9. User Inputs

| Screen | Input Fields |
|---|---|
| Login | Username, Password |
| Product Details | Color selection, Quantity selector |
| Product Catalog | Sort selection |
| Shipping Address | Full Name, Address Line 1, Address Line 2, City, State/Region, Zip Code, Country |
| Payment Method | Credit/Debit card information (individual field breakdown Not Observed — evidence shows a general "card entry screen") |

## 10. Dynamic UI Elements

- Cart badge (item count)
- Quantity (Product Details and Cart)
- Product color selection state
- Rating (per product)
- Cart / order total amount

## 11. Scrollable Areas

- Product Details screen: confirmed scrollable content.
- Product Catalog: grid layout is visible; explicit scroll behavior Not Observed in the provided evidence.
- All other screens: scroll behavior Not Observed.

## 12. Navigation Summary

- Product Catalog is the entry hub, branching to Product Details, Cart, and the Navigation Drawer.
- Product Details returns to Product Catalog and feeds the Cart via Add to Cart.
- Cart initiates the checkout sequence via Proceed to Checkout.
- Checkout is a linear three-step wizard: Shipping Address → Payment Method → Review Order → Order Completion.
- Login is reachable both from the Navigation Drawer directly and somewhere in the Cart-to-Checkout path; the exact trigger point is Not Observed.
- The Navigation Drawer additionally exposes eight utility/demo screens and an About screen, all outside the shopping journey.

## 13. Observations

- The application follows a consistent shopping-workflow structure: catalog, detail, cart, multi-step checkout, confirmation.
- Checkout is implemented as a distinct step-by-step wizard (Shipping → Payment → Review → Completion) rather than a single-page checkout.
- The Navigation Drawer mixes primary commerce navigation (Catalog, Login) with device-capability and test-utility features (WebView, QR Code Scanner, Geo Location, Drawing, FingerPrint, Virtual USB, Crash App, Reset App State), suggesting the application is purpose-built as a demo/reference app rather than a production commerce app.
- The presence of "Reset App State" and "Crash App (Debug)" indicates the application includes deliberate QA/test-support tooling.
- A consistent header pattern (cart icon, menu icon) is visible across at least the Product Catalog screen.

## 14. Known Limitations

This analysis is based **only on screenshots** supplied by the document author. The following were **not analyzed** and must not be assumed by any downstream document:

- XML hierarchy
- Resource IDs
- Accessibility IDs
- Backend APIs
- Network communication
- Permissions
- Hidden screens
- Runtime behaviors
- Error handling
- Business rules not visible in the UI
- Database interactions
- Exact sequencing between Shopping Cart, Login, and Shipping Address (order of occurrence Not Observed)
- Exact screen hosting the "Place Order" trigger (Review Order vs. a separate step) — Not Observed

Locator strategy, automation feasibility, and test scenario definition are explicitly out of scope for this document and are deferred to later phases (MA-RS, MA-TS, MA-FD).

---

**End of Document — MA-AA-001, v1.0**
