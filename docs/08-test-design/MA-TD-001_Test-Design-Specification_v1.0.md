---
document_id: MA-TD-001
title: Test Design Specification
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
related_documents: [MA-PV-001, MA-AA-001, MA-RS-001, MA-TS-001, MA-FA-001, MA-TP-001]
classification: Internal
---

# MA-TD-001 — Test Design Specification

**Mobile Automation Framework**

| Field | Value |
|---|---|
| Document ID | MA-TD-001 |
| Document Name | Test Design Specification |
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
| v0.1 | 2026-07-29 | Project Owner | Initial draft derived from MA-PV-001, MA-AA-001, MA-RS-001, MA-TS-001, MA-FA-001, MA-TP-001 |
| v1.0 | 2026-07-29 | Project Owner | First baseline version submitted for approval |

---

## 1. Purpose

This document converts every functional requirement baselined in [MA-RS-001] into one or more executable Test Scenarios, scheduled for execution under [MA-TP-001] and following the approach defined in [MA-TS-001]. It defines WHAT will be verified for each requirement, not the steps, data, or automation used to verify it. No requirement or application behavior is introduced beyond what MA-RS-001 and MA-AA-001 already document.

## 2. Scope

Every functional requirement (FR-001–FR-031) in MA-RS-001 receives at least one Test Scenario. Non-functional requirements (NFR-001–NFR-012) are not converted into scenarios in this document — they are verified at the framework/execution level per MA-TS-001 Sections 10 and 16–17, not as discrete functional scenarios. Scenarios inherit scope boundaries unchanged from MA-PV-001 Section 11.2 and MA-RS-001 Section 11.

## 3. Referenced Documents

| Document ID | Title | Role in This Document |
|---|---|---|
| MA-PV-001 | Project Vision & Scope | Source of scope boundaries and principles |
| MA-AA-001 | AUT Analysis | Source of confirmed screen behavior |
| MA-RS-001 | Requirements Specification | Source of the 31 functional requirements converted to scenarios |
| MA-TS-001 | Test Strategy | Source of priority ordering and elevated-risk items |
| MA-FA-001 | Framework Architecture | Confirms scenarios remain implementation-independent of this design |
| MA-TP-001 | Master Test Plan | Defines the execution stages these scenarios are scheduled into |

## 4. Scenario Design Principles

- Each scenario expresses exactly one verifiable behavior and maps to exactly one parent requirement.
- No scenario introduces AUT behavior beyond what MA-AA-001 or MA-RS-001 already documents; where a requirement is scoped as "Out of Current Observation," its scenario is limited to the confirmed portion only.
- Scenarios contain no steps, test data, or automation detail — those belong to future execution/implementation artifacts, not this design.
- A requirement with compound, independently verifiable outcomes receives more than one scenario; a requirement with a single verifiable outcome receives one.

## 5. Scenario Priority

Scenario priority is inherited directly from the parent requirement's priority field in MA-RS-001 (Must / Should / Could) — it is not re-derived. This keeps scenario prioritization consistent with the execution ordering already established in MA-TS-001 Section 7.

## 6. Scenario Naming Convention

Pattern: `TS-<3-digit sequence>`, numbered sequentially across the entire document (not restarted per requirement). Each scenario record carries its own Requirement ID field linking it to exactly one FR. Scenario titles are short, imperative phrases describing the behavior under verification.

## 7. Test Scenario List

### 7.1 Application Launch

| Field | Value |
|---|---|
| Scenario ID | TS-001 |
| Requirement ID | FR-001 |
| Scenario Title | Verify application launches to Product Catalog |
| Priority | Must |
| Precondition | AUT is installed and closed |
| Description | Verify that launching the AUT displays the Product Catalog screen |
| Expected Result | Product Catalog screen is displayed with grid, cart icon, and menu icon present |

### 7.2 Authentication

| Field | Value |
|---|---|
| Scenario ID | TS-002 |
| Requirement ID | FR-002 |
| Scenario Title | Verify Login screen accessible from Navigation Drawer |
| Priority | Must |
| Precondition | Navigation Drawer is open |
| Description | Verify selecting "Login" from the Navigation Drawer displays the Login screen |
| Expected Result | Login screen displays Username field, Password field, and Login button |

| Field | Value |
|---|---|
| Scenario ID | TS-003 |
| Requirement ID | FR-003 |
| Scenario Title | Verify credential entry and Login submission |
| Priority | Must |
| Precondition | Login screen is displayed |
| Description | Verify Username and Password fields accept input and the Login button is triggerable |
| Expected Result | Values are accepted in both fields and Login button responds to activation |

| Field | Value |
|---|---|
| Scenario ID | TS-004 |
| Requirement ID | FR-004 |
| Scenario Title | Verify outcome after Login submission |
| Priority | Should |
| Precondition | Valid credentials entered on Login screen |
| Description | Verify the screen displayed immediately after Login submission is capturable and verifiable |
| Expected Result | A resulting screen is displayed and identifiable; exact destination is Out of Current Observation per MA-RS-001 FR-004 |

### 7.3 Product Browsing

| Field | Value |
|---|---|
| Scenario ID | TS-005 |
| Requirement ID | FR-005 |
| Scenario Title | Verify Product Catalog listing elements |
| Priority | Must |
| Precondition | Product Catalog screen is displayed |
| Description | Verify each product card displays image, name, price, and rating |
| Expected Result | All four elements are present and readable on each product card |

| Field | Value |
|---|---|
| Scenario ID | TS-006 |
| Requirement ID | FR-006 |
| Scenario Title | Verify Sort control is interactable |
| Priority | Should |
| Precondition | Product Catalog screen is displayed |
| Description | Verify the Sort control on the Product Catalog can be selected |
| Expected Result | Sort control is selectable; resulting catalog behavior is Out of Current Observation per MA-RS-001 FR-006 |

| Field | Value |
|---|---|
| Scenario ID | TS-007 |
| Requirement ID | FR-007 |
| Scenario Title | Verify cart icon and badge are readable |
| Priority | Must |
| Precondition | Product Catalog screen is displayed |
| Description | Verify the cart icon is present and its badge value is readable |
| Expected Result | Cart icon and badge are visible and the badge value reflects the current cart item count |

| Field | Value |
|---|---|
| Scenario ID | TS-008 |
| Requirement ID | FR-008 |
| Scenario Title | Verify Navigation Drawer opens from Catalog |
| Priority | Must |
| Precondition | Product Catalog screen is displayed |
| Description | Verify tapping the menu icon opens the Navigation Drawer |
| Expected Result | Navigation Drawer is displayed with its listed items |

### 7.4 Product Details

| Field | Value |
|---|---|
| Scenario ID | TS-009 |
| Requirement ID | FR-009 |
| Scenario Title | Verify navigation to Product Details |
| Priority | Must |
| Precondition | Product Catalog screen is displayed |
| Description | Verify tapping a product card opens the Product Details screen |
| Expected Result | Product Details screen displays image, title, price, and rating for the selected product |

| Field | Value |
|---|---|
| Scenario ID | TS-010 |
| Requirement ID | FR-010 |
| Scenario Title | Verify product color selection |
| Priority | Should |
| Precondition | Product Details screen is displayed |
| Description | Verify a color option can be selected and reflects a selected state |
| Expected Result | Selected color option visibly reflects the selected state |

| Field | Value |
|---|---|
| Scenario ID | TS-011 |
| Requirement ID | FR-011 |
| Scenario Title | Verify product quantity selection |
| Priority | Must |
| Precondition | Product Details screen is displayed |
| Description | Verify the quantity selector accepts a value change |
| Expected Result | Quantity value changes in response to interaction; exact control mechanics are Out of Current Observation per MA-RS-001 FR-011 |

| Field | Value |
|---|---|
| Scenario ID | TS-012 |
| Requirement ID | FR-012 |
| Scenario Title | Verify Add to Cart action |
| Priority | Must |
| Precondition | Product Details screen is displayed |
| Description | Verify the Add to Cart button is interactable and triggers a state change |
| Expected Result | A verifiable state change occurs; exact resulting screen is Out of Current Observation per MA-RS-001 FR-012 |

| Field | Value |
|---|---|
| Scenario ID | TS-013 |
| Requirement ID | FR-013 |
| Scenario Title | Verify scroll reveals Product Highlights |
| Priority | Must |
| Precondition | Product Details screen is displayed |
| Description | Verify the scrollable region can be scrolled to expose Product Highlights |
| Expected Result | Product Highlights content becomes visible after scrolling |

### 7.5 Cart

| Field | Value |
|---|---|
| Scenario ID | TS-014 |
| Requirement ID | FR-014 |
| Scenario Title | Verify Cart screen access from Catalog |
| Priority | Must |
| Precondition | Product Catalog screen is displayed |
| Description | Verify tapping the cart icon opens the Cart screen |
| Expected Result | Cart screen displays current cart items and total |

| Field | Value |
|---|---|
| Scenario ID | TS-015 |
| Requirement ID | FR-015 |
| Scenario Title | Verify Cart item quantity update recalculates total |
| Priority | Must |
| Precondition | Cart screen is displayed with at least one item |
| Description | Verify updating item quantity on the Cart screen recalculates the displayed total |
| Expected Result | Total updates to reflect the new quantity |

| Field | Value |
|---|---|
| Scenario ID | TS-016 |
| Requirement ID | FR-016 |
| Scenario Title | Verify Cart item removal |
| Priority | Must |
| Precondition | Cart screen is displayed with at least one item |
| Description | Verify the Remove Item action removes the selected item from the Cart |
| Expected Result | Item is removed and the cart summary updates accordingly |

| Field | Value |
|---|---|
| Scenario ID | TS-017 |
| Requirement ID | FR-017 |
| Scenario Title | Verify Cart total accuracy |
| Priority | Must |
| Precondition | Cart screen is displayed with at least one item |
| Description | Verify the Cart total reflects the sum of item price and quantity as displayed |
| Expected Result | Displayed total matches the expected sum of displayed item values |

| Field | Value |
|---|---|
| Scenario ID | TS-018 |
| Requirement ID | FR-018 |
| Scenario Title | Verify Proceed to Checkout action |
| Priority | Must |
| Precondition | Cart screen is displayed with at least one item |
| Description | Verify the Proceed to Checkout button is interactable and advances the flow |
| Expected Result | A next screen in the checkout sequence is displayed; exact sequence relative to Login is Out of Current Observation per MA-RS-001 FR-018 |

### 7.6 Checkout — Shipping

| Field | Value |
|---|---|
| Scenario ID | TS-019 |
| Requirement ID | FR-019 |
| Scenario Title | Verify Shipping Address screen fields |
| Priority | Must |
| Precondition | Checkout flow has been initiated |
| Description | Verify the Shipping Address screen displays all required fields |
| Expected Result | Full Name, Address Line 1, Address Line 2, City, State/Region, Zip Code, and Country fields are all displayed |

| Field | Value |
|---|---|
| Scenario ID | TS-020 |
| Requirement ID | FR-020 |
| Scenario Title | Verify Shipping Address data entry |
| Priority | Must |
| Precondition | Shipping Address screen is displayed |
| Description | Verify each Shipping Address field accepts text input |
| Expected Result | All fields accept and retain entered values |

| Field | Value |
|---|---|
| Scenario ID | TS-021 |
| Requirement ID | FR-021 |
| Scenario Title | Verify Proceed to Payment action |
| Priority | Must |
| Precondition | Shipping Address fields are populated |
| Description | Verify the "To Payment" button navigates to the Payment Method screen |
| Expected Result | Payment Method screen is displayed |

### 7.7 Checkout — Payment

| Field | Value |
|---|---|
| Scenario ID | TS-022 |
| Requirement ID | FR-022 |
| Scenario Title | Verify Payment Method screen display |
| Priority | Must |
| Precondition | Navigated from Shipping Address screen |
| Description | Verify the Payment Method screen displays card entry fields |
| Expected Result | Card entry fields are displayed |

| Field | Value |
|---|---|
| Scenario ID | TS-023 |
| Requirement ID | FR-023 |
| Scenario Title | Verify Payment card data entry |
| Priority | Must |
| Precondition | Payment Method screen is displayed |
| Description | Verify card entry fields accept input |
| Expected Result | Card entry fields accept and retain entered values; individual field breakdown is Out of Current Observation per MA-RS-001 FR-023 |

| Field | Value |
|---|---|
| Scenario ID | TS-024 |
| Requirement ID | FR-024 |
| Scenario Title | Verify Continue to Review action |
| Priority | Must |
| Precondition | Payment details are entered |
| Description | Verify the Continue action navigates from Payment Method to Review Order |
| Expected Result | Review Order screen is displayed |

### 7.8 Order Review

| Field | Value |
|---|---|
| Scenario ID | TS-025 |
| Requirement ID | FR-025 |
| Scenario Title | Verify Review Order summary display |
| Priority | Must |
| Precondition | Navigated from Payment Method screen |
| Description | Verify the Review Order screen displays an order summary consistent with prior entries |
| Expected Result | Order summary content reflects cart, shipping, and payment data entered earlier in the flow |

### 7.9 Order Placement

| Field | Value |
|---|---|
| Scenario ID | TS-026 |
| Requirement ID | FR-026 |
| Scenario Title | Verify Place Order action |
| Priority | Must |
| Precondition | Review Order screen is displayed |
| Description | Verify the Place Order action is triggerable |
| Expected Result | Place Order action is triggerable; exact hosting screen is Out of Current Observation per MA-RS-001 FR-026 |

| Field | Value |
|---|---|
| Scenario ID | TS-027 |
| Requirement ID | FR-027 |
| Scenario Title | Verify Order Completion confirmation |
| Priority | Must |
| Precondition | Place Order action has been triggered |
| Description | Verify the Order Completion screen displays a success confirmation |
| Expected Result | "Checkout Complete" / "Order Completed" message is displayed |

### 7.10 Navigation

| Field | Value |
|---|---|
| Scenario ID | TS-028 |
| Requirement ID | FR-028 |
| Scenario Title | Verify Navigation Drawer items are selectable |
| Priority | Should |
| Precondition | Navigation Drawer is open |
| Description | Verify each listed Navigation Drawer item can be selected |
| Expected Result | Each item responds to selection; resulting screen content beyond Catalog and Login is Out of Current Observation per MA-RS-001 FR-028 |

| Field | Value |
|---|---|
| Scenario ID | TS-029 |
| Requirement ID | FR-029 |
| Scenario Title | Verify back navigation from Product Details |
| Priority | Must |
| Precondition | Product Details screen is displayed |
| Description | Verify back navigation returns to the Product Catalog screen |
| Expected Result | Product Catalog screen is displayed |

### 7.11 Error Handling

| Field | Value |
|---|---|
| Scenario ID | TS-030 |
| Requirement ID | FR-030 |
| Scenario Title | Error handling scenario — deferred |
| Priority | Could |
| Precondition | Not applicable |
| Description | No verifiable error state exists in the evidence base to design a scenario against |
| Expected Result | Not Applicable — excluded from execution scope per MA-TS-001 Section 6; retained here only for traceability to FR-030 |

### 7.12 State Management

| Field | Value |
|---|---|
| Scenario ID | TS-031 |
| Requirement ID | FR-031 |
| Scenario Title | Verify Cart-related dynamic elements reflect state |
| Priority | Must |
| Precondition | An item has been added to the Cart |
| Description | Verify cart badge and cart total update to reflect the current cart state |
| Expected Result | Cart badge and total values match the current cart contents |

| Field | Value |
|---|---|
| Scenario ID | TS-032 |
| Requirement ID | FR-031 |
| Scenario Title | Verify Product Details dynamic elements reflect state |
| Priority | Must |
| Precondition | Product Details screen is displayed |
| Description | Verify quantity and color selection controls reflect the currently selected state |
| Expected Result | Displayed quantity and color selection match the most recent user selection |

## 8. Scenario Coverage Summary

| Category | FR Count | Scenario Count |
|---|---|---|
| Application Launch | 1 | 1 |
| Authentication | 3 | 3 |
| Product Browsing | 4 | 4 |
| Product Details | 5 | 5 |
| Cart | 5 | 5 |
| Checkout — Shipping | 3 | 3 |
| Checkout — Payment | 3 | 3 |
| Order Review | 1 | 1 |
| Order Placement | 2 | 2 |
| Navigation | 2 | 2 |
| Error Handling | 1 | 1 |
| State Management | 1 | 2 |
| **Total** | **31** | **32** |

## 9. Requirement Traceability Matrix

| FR | Mapped TS |
|---|---|
| FR-001 | TS-001 |
| FR-002 | TS-002 |
| FR-003 | TS-003 |
| FR-004 | TS-004 |
| FR-005 | TS-005 |
| FR-006 | TS-006 |
| FR-007 | TS-007 |
| FR-008 | TS-008 |
| FR-009 | TS-009 |
| FR-010 | TS-010 |
| FR-011 | TS-011 |
| FR-012 | TS-012 |
| FR-013 | TS-013 |
| FR-014 | TS-014 |
| FR-015 | TS-015 |
| FR-016 | TS-016 |
| FR-017 | TS-017 |
| FR-018 | TS-018 |
| FR-019 | TS-019 |
| FR-020 | TS-020 |
| FR-021 | TS-021 |
| FR-022 | TS-022 |
| FR-023 | TS-023 |
| FR-024 | TS-024 |
| FR-025 | TS-025 |
| FR-026 | TS-026 |
| FR-027 | TS-027 |
| FR-028 | TS-028 |
| FR-029 | TS-029 |
| FR-030 | TS-030 |
| FR-031 | TS-031, TS-032 |

## 10. Execution Dependency

| Scenario | Depends On | Note |
|---|---|---|
| TS-009 | TS-001 | Requires Catalog to be displayed first |
| TS-010, TS-011, TS-013, TS-032 | TS-009 | Require Product Details screen to be reached first |
| TS-012 | TS-009 | Add to Cart requires Product Details to be displayed |
| TS-007, TS-031 | TS-012 | Cart-related dynamic elements require an item to have been added first |
| TS-014 | TS-012 | Cart screen access is meaningful once an item exists |
| TS-015, TS-016, TS-017 | TS-014 | Require Cart screen with at least one item |
| TS-018 | TS-014 | Proceed to Checkout requires the Cart screen |
| TS-019, TS-020, TS-021 | TS-018 | Shipping Address steps follow Checkout initiation |
| TS-022, TS-023, TS-024 | TS-021 | Payment steps follow Shipping completion |
| TS-025 | TS-024 | Review Order follows Payment completion |
| TS-026, TS-027 | TS-025 | Order Placement follows Review |
| TS-002, TS-003, TS-004 | TS-008 | Login is reachable via the Navigation Drawer independent of the checkout chain |
| TS-028 | TS-008 | Requires Navigation Drawer to be open |
| TS-029 | TS-009 | Back navigation requires Product Details to be reached first |

The exact dependency between TS-018 (Proceed to Checkout) and TS-002–TS-004 (Login) remains unresolved, consistent with the "Out of Current Observation" status of FR-018 and FR-004 in MA-RS-001; this chain will be confirmed during the manual verification pass committed to in MA-TS-001 Section 8.

## 11. Assumptions

- The AUT's screen structure and behavior remain consistent with MA-AA-001 observations at execution time.
- The elevated-risk items identified in MA-TS-001 Section 8 (Login sequencing, Add to Cart destination, Place Order hosting screen, Drawer item destinations, Sort behavior) will be resolved through manual verification before or during execution of the scenarios that depend on them (TS-004, TS-006, TS-012, TS-018, TS-026, TS-028).
- A single contributor designs, implements, and executes these scenarios, consistent with MA-PV-001 Section 19 (C-6).

## 12. Risks

| Risk | Note |
|---|---|
| Checkout dependency chain concentration | A failure at TS-014 (Cart access) or earlier blocks execution of all downstream checkout scenarios (TS-018–TS-027) |
| Unresolved elevated-risk items (MA-TS-001 §8) | Six scenarios (TS-004, TS-006, TS-012, TS-018, TS-026, TS-028) cannot be finalized until manual verification is complete |
| Scope creep during scenario refinement | Inherited from MA-PV-001 Section 20, R-1 |
| Environment instability affecting scenario execution reliability | Inherited from MA-PV-001 Section 20, R-2 |

## 13. Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-07-29 |
| Reviewed By | Pending | Pending | — |
| Approved By | Pending | Pending | — |
| Document Status | Draft | — | — |

---

End of Document — MA-TD-001, v1.0
