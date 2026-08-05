---
document_id: MA-RS-001
title: Requirements Specification
version: v1.8
status: Draft
author: Project Owner / Repository Maintainer
created_date: 2026-07-29
last_updated: 2026-08-04
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
aut_name: Sauce Labs Android Demo App
aut_version: 2.2.0
related_documents: [MA-PV-001, MA-AA-001, MA-LOC-001]
classification: Internal
---

# MA-RS-001 — Requirements Specification

**Mobile Automation Framework**

| Field | Value |
|---|---|
| Document ID | MA-RS-001 |
| Document Name | Requirements Specification |
| Version | v1.8 |
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
| v0.1 | 2026-07-29 | Project Owner | Initial draft derived from MA-PV-001 and MA-AA-001 |
| v1.0 | 2026-07-29 | Project Owner | First baseline version submitted for approval |
| v1.1 | 2026-08-01 | Project Owner | TC-012 Isolated Resolution: FR-012's Acceptance Criteria resolved from "Out of Current Observation" using Task #16 reconciliation evidence (this session) and MA-LOC-001 §6 — Add to Cart does not navigate away from Product Details; the cart badge appearing is the verifiable state change. No other requirement modified. |
| v1.2 | 2026-08-02 | Project Owner | Phase 10.1A (Authentication Module Documentation Reconciliation): FR-004's Acceptance Criteria resolved from "Out of Current Observation" using Phase 9.5G runtime evidence (this session, Enterprise Framework v1.0.0) — post-login destination is the Product Catalog screen, corroborated by the Navigation Drawer's authenticated-state marker. Scoped to the standalone login path only. Requirement Traceability Summary (§12) Authentication row updated accordingly. No other requirement modified. |
| v1.3 | 2026-08-02 | Project Owner | Phase 10.4A (Product Details Module Documentation Reconciliation): FR-011's Acceptance Criteria partially resolved from "Out of Current Observation" using Phase 10.3 runtime evidence (this session, real-device execution, Enterprise Framework v1.0.0, unchanged) — Increase/Decrease each change quantity by exactly 1, and reducing to 0 disables Add to Cart. Scoped precisely: the selector's maximum bound and the Decrease control's own state at the zero floor were not exercised and remain Out of Current Observation — not inferred. Requirement Traceability Summary (§12) Product Details row updated accordingly. No other requirement modified. |
| v1.4 | 2026-08-04 | Project Owner | Phase 11.9A (Cart Module — TC-018 Documentation Reconciliation): FR-018's Acceptance Criteria fully resolved from "Out of Current Observation" using Phase 11.7 real-device evidence (this session) — Proceed to Checkout's destination is conditional on authentication state (Login, with auto-resume to Shipping Address after successful login, if anonymous; Shipping Address directly if already authenticated), with Cart contents preserved throughout. Requirement Traceability Summary (§12) Cart row updated from Partial to Yes accordingly. No other requirement modified. |
| v1.5 | 2026-08-04 | Project Owner | Phase 12.7A (Payment Module Documentation Reconciliation): FR-023's Acceptance Criteria fully resolved from "Out of Current Observation" using Phase 12.3/12.5 real-device evidence (this session) — the Payment Method screen's card entry fields are exactly four (Cardholder Name, Card Number, Expiration Date, Security Code); Card Number and Expiration Date auto-format entered input (space-grouped digits, slash-inserted date respectively); the Billing-Same-As-Shipping checkbox is checked by default. FR-022 reviewed against the same evidence and found to require no wording change. Requirement Traceability Summary (§12) Checkout — Payment row updated from Partial to Yes accordingly. No other requirement modified. |
| v1.6 | 2026-08-04 | Project Owner | Phase 13.3A (TC-026 Documentation Reconciliation): FR-026's Acceptance Criteria fully resolved from "Out of Current Observation" using Phase 13.2 real-device automated-execution evidence and Phase 13.3 Enterprise Acceptance Review (this session) — the Place Order action is hosted on the Review Order screen (the "Place Order" button, confirmed via `paymentBtn`'s fourth reuse, accessibility id "Completes the process of checkout"); tapping it navigates to the Checkout Complete screen. Requirement Traceability Summary (§12) Order Placement row updated from Partial to Yes accordingly. No other requirement modified. |
| v1.7 | 2026-08-04 | Project Owner | Phase 14.2A (Product Sort — TC-006 Documentation Reconciliation): FR-006's Acceptance Criteria fully resolved from "Out of Current Observation" using Phase 14.1 real-device automated-execution evidence and Phase 14.2 Enterprise Acceptance Review (this session) — the Sort control's four options each re-order the Product Catalog correctly (Name Ascending/Descending alphabetically, Price Ascending/Descending numerically, in the named direction); Name Ascending is the Product Catalog's default sort state. Requirement Traceability Summary (§12) Product Browsing row updated from Partial to Yes accordingly. No other requirement modified. |
| v1.8 | 2026-08-05 | Project Owner | Phase 15.2A (Navigation — TC-028 Documentation Reconciliation): FR-028's Acceptance Criteria fully resolved from "Out of Current Observation" using Phase 15.1 real-device automated-execution evidence and Phase 15.2 Enterprise Acceptance Review (this session) — all seven navigable drawer destinations (WebView, QR Code Scanner, Geo Location, Drawing, About, FingerPrint, Virtual USB) confirmed to navigate to their own screen; Reset App State confirmed to open a dismissible confirmation dialog, not a navigation. Crash App (Debug) remains permanently excluded from automation (not a gap — an explicit, evidence-independent scope boundary). Requirement Traceability Summary (§12) Navigation row updated from Partial to Yes accordingly. This is the final remaining item from the original "elevated-risk items" list (MA-TS-001 §8) — no unresolved items remain. No other requirement modified. |

---

## 1. Purpose

This document defines what the Mobile Automation Framework must support, derived exclusively from [MA-PV-001] (Project Vision & Scope) and [MA-AA-001] (AUT Analysis). It does not define how the framework will be implemented. Every requirement is traceable to one of the two source documents; where MA-AA-001 did not observe a behavior, the requirement is marked "Out of Current Observation" rather than assumed.

## 2. Scope

Scope is inherited from MA-PV-001 Section 11 and constrained further by what MA-AA-001 confirmed as observable in the AUT. This document covers functional and non-functional requirements for automating the flows identified in MA-AA-001 (Product Catalog, Product Details, Cart, Login, Checkout, Order Completion, Navigation Drawer) within the platform, language, and tooling boundaries frozen in MA-PV-001. It does not cover framework architecture, test case design, or automation implementation.

## 3. Business Objectives

Inherited from MA-PV-001 Section 7:

- Demonstrate enterprise-grade QA automation engineering capability.
- Establish a documentation trail mirroring real enterprise SDLC governance.
- Produce a framework structurally reusable against a different AUT with minimal rework.
- Reduce evaluation risk for reviewers assessing this project as a hiring signal.
- Create a durable reference project usable across future professional contexts.

## 4. Stakeholders

Inherited from MA-PV-001 Section 10:

| Stakeholder | Interest |
|---|---|
| QA Engineers | Coverage decisions and manual-effort reduction |
| SDET Engineers | Engineering rigor and traceability |
| Automation Engineers | Tool selection and maintainability |
| Software Test Engineers | Structural reference for planning before execution |
| Students | Learning reference for professional process |
| Recruiters | Fast, credible signal of project seriousness |
| Hiring Managers | Evidence of planning and documentation discipline |
| Technical Interviewers | Defensible, traceable engineering decisions |

## 5. Referenced Documents

| Document ID | Title | Role |
|---|---|---|
| MA-PV-001 | Project Vision & Scope | Source of business objectives, scope boundaries, principles, constraints, assumptions |
| MA-AA-001 | AUT Analysis | Source of observed screens, components, navigation, and functional modules |

## 6. Functional Requirements

### 6.1 Application Launch

| Field | Value |
|---|---|
| ID | FR-001 |
| Title | Application Launch to Product Catalog |
| Description | Framework must support verifying the application launches and displays the Product Catalog screen. |
| Priority | Must |
| Source | MA-AA-001 §6, §7 |
| Acceptance Criteria | On launch, Product Catalog elements (grid, cart icon, menu icon) are present and verifiable. |

### 6.2 Authentication

| Field | Value |
|---|---|
| ID | FR-002 |
| Title | Login Screen Access |
| Description | Framework must support verifying Login screen accessibility via the Navigation Drawer. |
| Priority | Must |
| Source | MA-AA-001 §6 |
| Acceptance Criteria | Selecting "Login" from the Navigation Drawer displays Username field, Password field, and Login button. |

| Field | Value |
|---|---|
| ID | FR-003 |
| Title | Login Credential Entry |
| Description | Framework must support entering values into Username and Password fields and submitting the Login form. |
| Priority | Must |
| Source | MA-AA-001 §9 |
| Acceptance Criteria | Text can be entered into both fields and the Login button is triggerable. |

| Field | Value |
|---|---|
| ID | FR-004 |
| Title | Login Outcome Verification |
| Description | Framework must support verifying the result of a Login submission. |
| Priority | Should |
| Source | MA-AA-001 §6 |
| Acceptance Criteria | Post-login destination screen is capturable and verifiable. Resolved via Phase 9.5G runtime evidence (real-device execution, this session's Enterprise Framework v1.0.0): submitting valid credentials via the Navigation Drawer's standalone Login entry point returns the user to the Product Catalog screen, independently corroborated by the Navigation Drawer showing the authenticated-state marker ("Log Out" displayed, "Log In" absent) immediately after. Scoped to the standalone login path only — the Checkout-interrupted login path (relevant to FR-018) is a separate, still-unresolved item. |

### 6.3 Product Browsing

| Field | Value |
|---|---|
| ID | FR-005 |
| Title | Product Catalog Display Verification |
| Description | Framework must support verifying product listing elements (image, name, price, rating) are displayed. |
| Priority | Must |
| Source | MA-AA-001 §5, §6 |
| Acceptance Criteria | Each product card exposes image, name, price, and rating for verification. |

| Field | Value |
|---|---|
| ID | FR-006 |
| Title | Product Sort Interaction |
| Description | Framework must support interacting with the Sort control on the Product Catalog screen. |
| Priority | Should |
| Source | MA-AA-001 §6, §9 |
| Acceptance Criteria | Sort control is selectable and offers four options: Name - Ascending, Name - Descending, Price - Ascending, Price - Descending. Selecting an option dismisses the Sort dialog automatically and re-orders the Product Catalog accordingly — Name/Price Ascending or Descending re-orders alphabetically/numerically in the named direction. Name - Ascending is the Product Catalog's default sort state on app launch. Resolved via Phase 14.1 real-device automated-execution evidence and Phase 14.2 Enterprise Acceptance Review. |

| Field | Value |
|---|---|
| ID | FR-007 |
| Title | Cart Icon and Badge Verification |
| Description | Framework must support verifying the cart icon and its item-count badge from the Catalog screen. |
| Priority | Must |
| Source | MA-AA-001 §6, §10 |
| Acceptance Criteria | Cart badge value is readable and reflects the current cart item count. |

| Field | Value |
|---|---|
| ID | FR-008 |
| Title | Navigation Drawer Access |
| Description | Framework must support opening the Navigation Drawer from the Catalog screen. |
| Priority | Must |
| Source | MA-AA-001 §6 |
| Acceptance Criteria | Tapping the menu icon displays the Navigation Drawer with its listed items. |

### 6.4 Product Details

| Field | Value |
|---|---|
| ID | FR-009 |
| Title | Product Detail Navigation |
| Description | Framework must support navigating from a Catalog product card to its Product Details screen. |
| Priority | Must |
| Source | MA-AA-001 §6, §7 |
| Acceptance Criteria | Tapping a product card opens Product Details showing image, title, price, and rating. |

| Field | Value |
|---|---|
| ID | FR-010 |
| Title | Product Color Selection |
| Description | Framework must support selecting a product color option on the Product Details screen. |
| Priority | Should |
| Source | MA-AA-001 §5, §9 |
| Acceptance Criteria | Color selection control is interactable and reflects the selected state. |

| Field | Value |
|---|---|
| ID | FR-011 |
| Title | Product Quantity Selection |
| Description | Framework must support setting product quantity on the Product Details screen. |
| Priority | Must |
| Source | MA-AA-001 §9, §10 |
| Acceptance Criteria | Quantity selector accepts a value change — **confirmed via Phase 10.3 runtime evidence** (real-device execution, this session, Enterprise Framework v1.0.0): each Increase/Decrease tap changes the displayed quantity by exactly 1, and reducing quantity to 0 disables the Add to Cart button. The selector's maximum bound and the Decrease control's own enabled/disabled state at the zero floor were not exercised and remain Out of Current Observation. |

| Field | Value |
|---|---|
| ID | FR-012 |
| Title | Add to Cart Action |
| Description | Framework must support triggering Add to Cart from Product Details. |
| Priority | Must |
| Source | MA-AA-001 §5, §6 |
| Acceptance Criteria | Add to Cart button is interactable; tapping it does not navigate away from Product Details — the resulting, verifiable state change is the header cart badge transitioning from absent (empty cart) to present with an item count. Resolved via Task #16 reconciliation (this session); corroborated by MA-LOC-001 §6. |

| Field | Value |
|---|---|
| ID | FR-013 |
| Title | Product Details Scroll Support |
| Description | Framework must support scrolling Product Details content to reveal Product Highlights. |
| Priority | Must |
| Source | MA-AA-001 §11 |
| Acceptance Criteria | Scrollable region on Product Details can be scrolled to expose Product Highlights. |

### 6.5 Cart

| Field | Value |
|---|---|
| ID | FR-014 |
| Title | Cart Screen Access |
| Description | Framework must support navigating to the Shopping Cart from the Catalog cart icon. |
| Priority | Must |
| Source | MA-AA-001 §6, §7 |
| Acceptance Criteria | Tapping the cart icon opens the Cart screen showing cart items and total. |

| Field | Value |
|---|---|
| ID | FR-015 |
| Title | Cart Item Quantity Update |
| Description | Framework must support updating item quantity within the Cart screen. |
| Priority | Must |
| Source | MA-AA-001 §5, §10 |
| Acceptance Criteria | Quantity control on the Cart updates item quantity and recalculates the total. |

| Field | Value |
|---|---|
| ID | FR-016 |
| Title | Cart Item Removal |
| Description | Framework must support removing an item from the Cart. |
| Priority | Must |
| Source | MA-AA-001 §5 |
| Acceptance Criteria | Remove Item action removes the selected item and updates the cart summary. |

| Field | Value |
|---|---|
| ID | FR-017 |
| Title | Cart Total Verification |
| Description | Framework must support verifying the Cart total amount. |
| Priority | Must |
| Source | MA-AA-001 §10 |
| Acceptance Criteria | Cart total reflects the sum of item price and quantity as displayed. |

| Field | Value |
|---|---|
| ID | FR-018 |
| Title | Proceed to Checkout Action |
| Description | Framework must support triggering Proceed to Checkout from the Cart screen. |
| Priority | Must |
| Source | MA-AA-001 §5, §6 |
| Acceptance Criteria | Proceed to Checkout button is interactable and triggers navigation whose destination is conditional on authentication state — **confirmed via Phase 11.7 real-device evidence** (this session, real device, Enterprise Framework v1.0.0): an anonymous user is redirected to the Login screen, and a successful login automatically resumes the checkout flow, landing directly on the Shipping Address screen; an already-authenticated user is taken directly to the Shipping Address screen, with no Login interstitial. Cart contents are preserved across every transition (verified via the header cart badge). |

### 6.6 Checkout — Shipping

| Field | Value |
|---|---|
| ID | FR-019 |
| Title | Shipping Address Screen Access |
| Description | Framework must support verifying the Shipping Address screen is reached during Checkout. |
| Priority | Must |
| Source | MA-AA-001 §6 |
| Acceptance Criteria | Shipping Address screen displays Full Name, Address Line 1, Address Line 2, City, State/Region, Zip Code, and Country fields. |

| Field | Value |
|---|---|
| ID | FR-020 |
| Title | Shipping Address Data Entry |
| Description | Framework must support entering values into all Shipping Address fields. |
| Priority | Must |
| Source | MA-AA-001 §9 |
| Acceptance Criteria | Each Shipping Address field accepts text input. |

| Field | Value |
|---|---|
| ID | FR-021 |
| Title | Proceed to Payment Action |
| Description | Framework must support triggering the "To Payment" navigation from Shipping Address. |
| Priority | Must |
| Source | MA-AA-001 §6 |
| Acceptance Criteria | "To Payment" button navigates to the Payment Method screen. |

### 6.7 Checkout — Payment

| Field | Value |
|---|---|
| ID | FR-022 |
| Title | Payment Method Screen Access |
| Description | Framework must support verifying the Payment Method screen displays after Shipping Address. |
| Priority | Must |
| Source | MA-AA-001 §6 |
| Acceptance Criteria | Payment Method screen displays card entry fields. |

| Field | Value |
|---|---|
| ID | FR-023 |
| Title | Payment Card Data Entry |
| Description | Framework must support entering credit/debit card information on the Payment Method screen. |
| Priority | Must |
| Source | MA-AA-001 §9 |
| Acceptance Criteria | All four card entry fields — Cardholder Name, Card Number, Expiration Date, Security Code — accept and retain input (real-device evidence, Phase 12.3/12.5). Card Number auto-formats entered digits into space-grouped groups of four (e.g. `4111 1111 1111 1111`); Expiration Date auto-inserts a slash after the second digit (e.g. `12/25`) — both confirmed via `getText()` returning the formatted value, not the raw input. The Billing-Same-As-Shipping checkbox is checked by default. |

| Field | Value |
|---|---|
| ID | FR-024 |
| Title | Continue to Review Action |
| Description | Framework must support triggering Continue navigation from Payment Method to Review Order. |
| Priority | Must |
| Source | MA-AA-001 §6 |
| Acceptance Criteria | Continue action navigates to the Review Order screen. |

### 6.8 Order Review

| Field | Value |
|---|---|
| ID | FR-025 |
| Title | Review Order Screen Verification |
| Description | Framework must support verifying the Review Order screen displays an order summary. |
| Priority | Must |
| Source | MA-AA-001 §6 |
| Acceptance Criteria | Review Order screen shows order summary content consistent with cart, shipping, and payment data entered. |

### 6.9 Order Placement

| Field | Value |
|---|---|
| ID | FR-026 |
| Title | Place Order Action |
| Description | Framework must support triggering the Place Order action. |
| Priority | Must |
| Source | MA-AA-001 §6 |
| Acceptance Criteria | The Place Order action is hosted on the Review Order screen and is triggerable (real-device evidence, Phase 13.2/13.3) — tapping the Place Order button navigates to the Checkout Complete screen. |

| Field | Value |
|---|---|
| ID | FR-027 |
| Title | Order Completion Confirmation |
| Description | Framework must support verifying the Order Completion confirmation screen. |
| Priority | Must |
| Source | MA-AA-001 §6 |
| Acceptance Criteria | Order Completion screen displays a "Checkout Complete" / "Order Completed" success message. |

### 6.10 Navigation

| Field | Value |
|---|---|
| ID | FR-028 |
| Title | Navigation Drawer Item Access |
| Description | Framework must support selecting each Navigation Drawer item (Catalog, WebView, QR Code Scanner, Geo Location, Drawing, About, Reset App State, FingerPrint, Virtual USB, Crash App (Debug), Login). |
| Priority | Should |
| Source | MA-AA-001 §6 |
| Acceptance Criteria | Each drawer item is selectable. Selecting WebView, QR Code Scanner, Geo Location, Drawing, About, FingerPrint, or Virtual USB navigates to that item's own screen (confirmed via its title, or for Virtual USB its one id-bearing element — Virtual USB is a separate Android Activity, not a fragment). Selecting Reset App State opens a confirmation dialog, not a navigation, which must be dismissed via Cancel. Resolved via Phase 15.1 real-device automated-execution evidence and Phase 15.2 Enterprise Acceptance Review. Crash App (Debug) is permanently excluded from automation — it intentionally terminates the app process — and remains untested; Catalog and Login destinations are covered by FR-001/FR-004/FR-018, not by this requirement. |

| Field | Value |
|---|---|
| ID | FR-029 |
| Title | Back Navigation Support |
| Description | Framework must support returning from Product Details to Product Catalog. |
| Priority | Must |
| Source | MA-AA-001 §6 |
| Acceptance Criteria | Back navigation from Product Details returns to the Product Catalog screen. |

### 6.11 Error Handling

| Field | Value |
|---|---|
| ID | FR-030 |
| Title | Error Handling Coverage |
| Description | Application error-handling behavior was not captured during AUT analysis. |
| Priority | Could |
| Source | MA-AA-001 §14 (Known Limitations) |
| Acceptance Criteria | Out of Current Observation — no verifiable error states exist to define acceptance criteria against at this time. |

### 6.12 State Management

| Field | Value |
|---|---|
| ID | FR-031 |
| Title | Dynamic Element State Verification |
| Description | Framework must support verifying state-dependent UI elements (cart badge, quantity, product color selection, cart/order total) reflect current application state. |
| Priority | Must |
| Source | MA-AA-001 §10 |
| Acceptance Criteria | Each listed dynamic element's displayed value matches the expected state after a triggering action. |

## 7. Non-Functional Requirements

| ID | Description | Priority | Acceptance Criteria |
|---|---|---|---|
| NFR-001 | Framework must be structured so it can be understood and modified by someone other than the original author (Maintainability). | Must | Components are organized such that a third party can locate and modify a given behavior without author assistance. |
| NFR-002 | Framework must accommodate additional flows, and eventually additional AUTs, without structural rewrite (Scalability). | Must | Adding a new flow does not require modifying unrelated existing components. |
| NFR-003 | Framework components must be reusable across multiple scenarios rather than duplicated per scenario (Reusability). | Must | A common interaction is implemented once and reused wherever applicable. |
| NFR-004 | Framework code and test logic must be understandable without additional narrative explanation (Readability). | Must | Naming and structure convey intent without requiring supplementary documentation. |
| NFR-005 | Automated tests must produce consistent results across repeated executions against an unchanged AUT state (Execution Stability). | Must | Repeated execution of the same suite yields consistent pass/fail results. |
| NFR-006 | Framework must produce a human-readable execution report per run (Reporting). | Must | Each execution generates a report accessible without inspecting raw logs. |
| NFR-007 | Framework must record execution activity sufficient to diagnose a failure (Logging). | Should | A failure produces a traceable log entry identifying the failing step. |
| NFR-008 | Framework must support environment-specific configuration without modifying core logic (Configuration). | Must | Execution target (device/emulator) is configurable externally to test logic. |
| NFR-009 | Framework must support execution on both Android emulator and physical Android device (Cross-Device Support). | Must | The same suite executes against an emulator and a physical device without code changes. |
| NFR-010 | Framework execution overhead should not be the primary contributor to total run time (Performance Expectations). | Could | Out of Current Observation for a numeric benchmark; qualitative expectation only at this stage. |
| NFR-011 | Framework must support supplying test input data without hardcoding values inside test logic (Data Management). | Should | Login, Shipping Address, and Payment input values are externally supplied rather than embedded in test logic. |
| NFR-012 | Framework must rely only on publicly available tools and the named AUT (Environment Independence). | Must | Framework setup introduces no dependency on proprietary or internally-restricted systems. |

## 8. Constraints

Inherited from MA-PV-001 Section 19:

- Platform limited to Android only.
- Automation engine limited to Appium 3.x.
- Automation driver limited to UiAutomator2.
- Programming language fixed to Java 17.
- Build tool fixed to Gradle.
- Test framework fixed to TestNG.
- Execution limited to local device/emulator; no cloud device grid in this phase.
- No iOS support.
- Single-contributor development capacity.
- No dedicated device lab or cloud grid budget in this phase.
- Project must remain suitable for public disclosure; no proprietary or confidential material.

## 9. Assumptions

Inherited from MA-PV-001 Section 18:

- The AUT remains publicly available and functionally stable at version 2.2.0 throughout active development.
- A suitable local development environment (Android emulator and/or physical device connectivity) is available.
- GitHub remains the project's hosting platform.
- The project is developed and maintained by a single primary contributor.
- Reviewers will primarily engage with the GitHub repository directly.
- The AUT does not require authentication against a live backend service beyond what is bundled in the demo application.

## 10. Dependencies

| Dependency | Type |
|---|---|
| Sauce Labs Android Demo App v2.2.0 (AUT) | External |
| Appium 3.x Server | External Tooling |
| UiAutomator2 Driver | External Tooling |
| Java 17 Runtime | External Tooling |
| Gradle | External Tooling |
| TestNG | External Tooling |
| Android SDK / Emulator Tooling | External Tooling |
| ADB (Android Debug Bridge) | External Tooling |
| Real Android Device (physical) | External Tooling |
| GitHub | External Platform |

## 11. Out of Scope

Inherited from MA-PV-001 Section 11.2:

- iOS automation, cross-platform automation, or any driver other than UiAutomator2.
- Cloud device farm execution in this phase.
- Performance testing, load testing, or security testing of the AUT.
- API-level or backend testing of any service the AUT communicates with.
- Exhaustive regression coverage of every screen and flow.
- Visual regression or pixel-level UI comparison.
- Any modification of the AUT itself.
- Framework support for applications other than the named AUT, in this phase.

## 12. Requirement Traceability Summary

| Requirement Category | Source Document | Observed | Remarks |
|---|---|---|---|
| Application Launch | MA-AA-001 | Yes | Fully observed |
| Authentication | MA-AA-001 | Yes | Post-login destination resolved as of v1.2 (Phase 10.1A) — see Version History |
| Product Browsing | MA-AA-001 | Yes | Sort control resulting re-ordering behavior resolved as of v1.7 (Phase 14.2A, using Phase 14.1/14.2 real-device evidence) — see Version History |
| Product Details | MA-AA-001 | Partial | Quantity selector step-size/floor mechanics (FR-011) resolved as of v1.3 (Phase 10.4A, using Phase 10.3 runtime evidence); the selector's maximum bound remains Out of Current Observation. Add to Cart destination (FR-012) resolved as of v1.1 — see Version History |
| Cart | MA-AA-001 | Yes | Proceed to Checkout ↔ Login sequencing resolved as of v1.4 (Phase 11.9A, using Phase 11.7 runtime evidence) — see Version History |
| Checkout — Shipping | MA-AA-001 | Yes | Fully observed |
| Checkout — Payment | MA-AA-001 | Yes | Card entry field structure and formatting behavior resolved as of v1.5 (Phase 12.7A, using Phase 12.3/12.5 runtime evidence) — see Version History |
| Order Review | MA-AA-001 | Yes | Fully observed |
| Order Placement | MA-AA-001 | Yes | Place Order hosting screen (Review Order) resolved as of v1.6 (Phase 13.3A, using Phase 13.2/13.3 real-device evidence) — see Version History |
| Navigation | MA-AA-001 | Yes | Drawer item destinations resolved as of v1.8 (Phase 15.2A, using Phase 15.1/15.2 real-device evidence) — see Version History. Crash App (Debug) permanently excluded from automation, not a gap |
| Error Handling | MA-AA-001 | No | Explicitly listed as not analyzed |
| State Management | MA-AA-001 | Yes | Derived from documented dynamic UI elements |
| Non-Functional Requirements | MA-PV-001 | Yes | Derived from Project Principles, Deliverables, Supported Execution Modes, Constraints |
| Constraints | MA-PV-001 | Yes | Direct reuse of Section 19 |
| Assumptions | MA-PV-001 | Yes | Direct reuse of Section 18 |
| Dependencies | MA-PV-001 / MA-AA-001 | Yes | Combined from Section 22 and AUT tooling context |
| Out of Scope | MA-PV-001 | Yes | Direct reuse of Section 11.2 |

## 13. Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-07-29 |
| Reviewed By | Pending | Pending | — |
| Approved By | Pending | Pending | — |
| Document Status | Draft | — | — |

---

**End of Document — MA-RS-001, v1.8**
