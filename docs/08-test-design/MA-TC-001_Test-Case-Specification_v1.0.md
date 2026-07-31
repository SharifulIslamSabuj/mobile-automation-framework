---
document_id: MA-TC-001
title: Test Case Specification
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
related_documents: [MA-PV-001, MA-AA-001, MA-RS-001, MA-TS-001, MA-FA-001, MA-TP-001, MA-TD-001]
classification: Internal
---

# MA-TC-001 — Test Case Specification

**Mobile Automation Framework**

| Field | Value |
|---|---|
| Document ID | MA-TC-001 |
| Document Name | Test Case Specification |
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
| v0.1 | 2026-07-29 | Project Owner | Draft scaffold with TC-001 |
| v1.0 | 2026-07-29 | Project Owner | Complete set of 32 Test Cases (TC-001–TC-032) submitted for approval |

---

## 1. Purpose

This document converts every Test Scenario defined in [MA-TD-001] into a professional, evidence-based Test Case, forming the bridge between test design and future automation implementation. It was not part of the original documentation roadmap; it is introduced because every automated test requires a documented Test Case before implementation begins. It does not modify, reinterpret, or add scope to any previously approved document.

## 2. Scope

Every one of the 32 Test Scenarios in MA-TD-001 (TS-001–TS-032) produces exactly one Test Case (TC-001–TC-032). No new requirement, scenario, or application behavior is introduced. Only information already present in MA-PV-001, MA-AA-001, MA-RS-001, MA-TS-001, MA-FA-001, MA-TP-001, and MA-TD-001 is used, in that evidence priority order (MA-AA-001 observed behavior first, then MA-RS-001, MA-TD-001, MA-TP-001, and MA-FA-001/MA-TS-001 only where directly relevant). Where those documents do not confirm a detail, the corresponding field states "Pending Manual Verification" or "Not Confirmed by Current Evidence" rather than inventing one.

## 3. Referenced Documents

| Document ID | Title | Role in This Document |
|---|---|---|
| MA-PV-001 | Project Vision & Scope | Principles, constraints, execution modes |
| MA-AA-001 | AUT Analysis | Priority 1 evidence — observed screen elements and behavior |
| MA-RS-001 | Requirements Specification | Priority 2 evidence — parent Functional Requirement per Test Case |
| MA-TS-001 | Test Strategy | Priority 6 evidence — elevated-risk items, environment/device strategy |
| MA-FA-001 | Framework Architecture | Priority 5 evidence — architectural references only where needed |
| MA-TP-001 | Master Test Plan | Priority 4 evidence — execution stage/environment context |
| MA-TD-001 | Test Design Specification | Priority 3 evidence — parent Test Scenario per Test Case |

## 4. Test Case Design Principles

- Each Test Case maps to exactly one Test Scenario in MA-TD-001, which maps to exactly one Functional Requirement in MA-RS-001.
- No field is populated from outside knowledge, prior experience, or generic mobile/e-commerce assumptions — only from the seven documents in Section 3, applied in the stated evidence priority order.
- Where a Test Scenario's Expected Result carries an "Out of Current Observation" qualifier, the corresponding Test Case preserves that qualifier, sets Automation Status to Blocked, and does not resolve the ambiguity.
- Every Expected Result — overall and per step — is observable, objective, and independently answerable as Pass or Fail. Vague terms ("works correctly," "successfully," "properly") are never used.
- Each step performs exactly one action and yields exactly one observable result.
- Test data is never invented; fields requiring data reference "Pending Test Data Design (Future MA-TDD-001)" unless a value is already documented in an approved source (none currently is).

## 5. Test Case Numbering

Pattern: `TC-<3-digit sequence>`, aligned 1:1 with the corresponding `TS-<3-digit sequence>` in MA-TD-001. Traceability chain: Requirement → Scenario → Test Case → Future Automation Script.

## 6. Test Cases

### TC-001 — Application Launch to Product Catalog

| Field | Value |
|---|---|
| Test Case ID | TC-001 |
| Requirement ID | FR-001 |
| Scenario ID | TS-001 |
| Module | Application Launch (MA-RS-001 §6.1) |
| Feature | Application Launch to Product Catalog |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — Product Catalog elements (grid, cart icon, menu icon) and the Launch → Product Catalog sequence are both directly observed in MA-AA-001 (§6, §7) |
| Objective | Verify that launching the AUT displays the Product Catalog screen with its confirmed elements |
| Preconditions | AUT is installed and not currently running |
| Dependencies | None — entry-point scenario (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Launch the AUT. | AUT launches without error. |
| 2 | Observe the screen displayed immediately after launch. | Product Catalog screen is displayed. |
| 3 | Observe the Product Catalog screen for the product grid. | Product grid is displayed. |
| 4 | Observe the Product Catalog screen for the cart icon. | Cart icon is displayed. |
| 5 | Observe the Product Catalog screen for the menu icon. | Menu icon is displayed. |

| Field | Value |
|---|---|
| Post Condition | AUT remains running with Product Catalog displayed, ready for scenarios that depend on TS-001 (e.g., TS-009) |
| Requirement Traceability | FR-001 |
| Scenario Traceability | TS-001 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Smoke (MA-TP-001 §10 names Application Launch as part of the Smoke stage) |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Underlying resource identifiers for the grid/cart/menu icons remain unconfirmed (MA-AA-001 Known Limitations); this is an implementation-phase concern, not a defect in this Test Case |

---

### TC-002 — Login Screen Access

| Field | Value |
|---|---|
| Test Case ID | TC-002 |
| Requirement ID | FR-002 |
| Scenario ID | TS-002 |
| Module | Authentication (MA-RS-001 §6.2) |
| Feature | Login Screen Access |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — Login screen and its fields (Username, Password, Login button) are directly observed in MA-AA-001, reachable from the Navigation Drawer |
| Objective | Verify the Login screen is reachable from the Navigation Drawer and displays its confirmed fields |
| Preconditions | Navigation Drawer is open (TC-008 executed) |
| Dependencies | TC-008 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap "Login" in the Navigation Drawer. | Login screen is displayed. |
| 2 | Observe the Login screen for the Username field. | Username field is displayed. |
| 3 | Observe the Login screen for the Password field. | Password field is displayed. |
| 4 | Observe the Login screen for the Login button. | Login button is displayed. |

| Field | Value |
|---|---|
| Post Condition | Login screen remains displayed, ready for TC-003 |
| Requirement Traceability | FR-002 |
| Scenario Traceability | TS-002 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | None |

---

### TC-003 — Login Credential Entry

| Field | Value |
|---|---|
| Test Case ID | TC-003 |
| Requirement ID | FR-003 |
| Scenario ID | TS-003 |
| Module | Authentication (MA-RS-001 §6.2) |
| Feature | Login Credential Entry |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — Username and Password are classified as input fields in MA-AA-001 §9, and the Login button is a confirmed control |
| Objective | Verify the Username and Password fields accept input and the Login button is triggerable |
| Preconditions | Login screen is displayed (TC-002 executed) |
| Dependencies | TC-008 (MA-TD-001 §10); sequential execution after TC-002 is a natural manual order not separately formalized as a dependency in MA-TD-001 |
| Test Data | Login Credentials — Reference: Pending Test Data Design (Future MA-TDD-001) |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Enter a value into the Username field. | Username field contains the entered value. |
| 2 | Enter a value into the Password field. | Password field contains the entered value. |
| 3 | Tap the Login button. | Login button responds to activation. |

| Field | Value |
|---|---|
| Post Condition | Login form has been submitted; resulting screen is not yet confirmed (see TC-004) |
| Requirement Traceability | FR-003 |
| Scenario Traceability | TS-003 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Credential values are not invented; execution requires MA-TDD-001 or officially documented sample credentials |

---

### TC-004 — Login Outcome Verification

| Field | Value |
|---|---|
| Test Case ID | TC-004 |
| Requirement ID | FR-004 |
| Scenario ID | TS-004 |
| Module | Authentication (MA-RS-001 §6.2) |
| Feature | Login Outcome Verification |
| Priority | Should |
| Automation Status | Blocked — TS-004 Expected Result contains "Out of Current Observation" |
| Verification Status | Partially Confirmed — Login submission action is confirmed; the resulting destination screen is not confirmed by MA-AA-001 or MA-RS-001 |
| Objective | Verify the screen displayed after Login submission is capturable and identifiable |
| Preconditions | Valid credentials entered on Login screen (TC-003 executed) |
| Dependencies | TC-003 |
| Test Data | Login Credentials — Reference: Pending Test Data Design (Future MA-TDD-001) |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Submit the Login form with previously entered credentials. | A resulting screen is displayed. |
| 2 | Identify the resulting screen. | Pending Manual Verification — exact destination screen not confirmed by MA-RS-001 FR-004. |

| Field | Value |
|---|---|
| Post Condition | Pending Manual Verification |
| Requirement Traceability | FR-004 |
| Scenario Traceability | TS-004 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Requires manual verification pass per MA-TS-001 §8 before this Test Case can be finalized for automation |

---

### TC-005 — Product Catalog Display Verification

| Field | Value |
|---|---|
| Test Case ID | TC-005 |
| Requirement ID | FR-005 |
| Scenario ID | TS-005 |
| Module | Product Browsing (MA-RS-001 §6.3) |
| Feature | Product Catalog Display Verification |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — product image, name, price, and rating are all directly listed as observed in MA-AA-001 |
| Objective | Verify each product card on the Catalog displays image, name, price, and rating |
| Preconditions | Product Catalog screen is displayed (TC-001 executed) |
| Dependencies | Not formally listed in MA-TD-001 §10; precondition requires Product Catalog to be displayed |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Observe a product card on the Product Catalog screen for a product image. | Product image is displayed. |
| 2 | Observe the same product card for a product name. | Product name is displayed. |
| 3 | Observe the same product card for a product price. | Product price is displayed. |
| 4 | Observe the same product card for a product rating. | Product rating is displayed. |

| Field | Value |
|---|---|
| Post Condition | Product Catalog remains displayed |
| Requirement Traceability | FR-005 |
| Scenario Traceability | TS-005 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Smoke (MA-TP-001 §10 names Catalog display as part of the Smoke stage) |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | None |

---

### TC-006 — Product Sort Interaction

| Field | Value |
|---|---|
| Test Case ID | TC-006 |
| Requirement ID | FR-006 |
| Scenario ID | TS-006 |
| Module | Product Browsing (MA-RS-001 §6.3) |
| Feature | Product Sort Interaction |
| Priority | Should |
| Automation Status | Blocked — TS-006 Expected Result contains "Out of Current Observation" |
| Verification Status | Partially Confirmed — Sort control existence is confirmed; resulting catalog behavior is not confirmed |
| Objective | Verify the Sort control on the Product Catalog is interactable |
| Preconditions | Product Catalog screen is displayed |
| Dependencies | Not formally listed in MA-TD-001 §10; precondition requires Product Catalog to be displayed |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap the Sort control on the Product Catalog screen. | Sort control responds to activation. |
| 2 | Observe the Product Catalog screen after Sort control activation. | Pending Manual Verification — resulting catalog behavior not confirmed by MA-RS-001 FR-006. |

| Field | Value |
|---|---|
| Post Condition | Pending Manual Verification |
| Requirement Traceability | FR-006 |
| Scenario Traceability | TS-006 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Requires manual verification pass per MA-TS-001 §8 |

---

### TC-007 — Cart Icon and Badge Verification

| Field | Value |
|---|---|
| Test Case ID | TC-007 |
| Requirement ID | FR-007 |
| Scenario ID | TS-007 |
| Module | Product Browsing (MA-RS-001 §6.3) |
| Feature | Cart Icon and Badge Verification |
| Priority | Must |
| Automation Status | Ready — TS-007 Expected Result contains no "Out of Current Observation" qualifier |
| Verification Status | Partially Confirmed — cart icon and cart badge existence are confirmed (MA-AA-001 §5, §10); that the badge value specifically represents item count is inferred, not literally stated in MA-AA-001 |
| Objective | Verify the cart icon is displayed and its badge value reflects the current cart item count |
| Preconditions | An item has been added to the Cart (TC-012 executed) |
| Dependencies | TC-012 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Observe the Product Catalog screen for the cart icon. | Cart icon is displayed. |
| 2 | Observe the cart badge value. | Cart badge displays a value. |
| 3 | Compare the cart badge value to the number of items added to the Cart. | Cart badge value matches the number of items added. |

| Field | Value |
|---|---|
| Post Condition | Cart badge reflects current state |
| Requirement Traceability | FR-007 |
| Scenario Traceability | TS-007 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Step 3's exact matching semantics rely on a reasonable but not literally documented reading of "badge" as an item-count indicator; flagged for reviewer awareness even though Automation Status is Ready per the literal-text rule |

---

### TC-008 — Navigation Drawer Access

| Field | Value |
|---|---|
| Test Case ID | TC-008 |
| Requirement ID | FR-008 |
| Scenario ID | TS-008 |
| Module | Product Browsing (MA-RS-001 §6.3) |
| Feature | Navigation Drawer Access |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — the Navigation Drawer and its full item list are directly observed in MA-AA-001 §6 |
| Objective | Verify the Navigation Drawer opens from the Product Catalog and displays its confirmed items |
| Preconditions | Product Catalog screen is displayed |
| Dependencies | Not formally listed in MA-TD-001 §10; precondition requires Product Catalog to be displayed |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap the menu icon on the Product Catalog screen. | Navigation Drawer is displayed. |
| 2 | Observe the Navigation Drawer contents. | Navigation Drawer displays Catalog, WebView, QR Code Scanner, Geo Location, Drawing, About, Reset App State, FingerPrint, Virtual USB, Crash App (Debug), and Login. |

| Field | Value |
|---|---|
| Post Condition | Navigation Drawer remains open, ready for TC-002, TC-028 |
| Requirement Traceability | FR-008 |
| Scenario Traceability | TS-008 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | None |

---

### TC-009 — Product Detail Navigation

| Field | Value |
|---|---|
| Test Case ID | TC-009 |
| Requirement ID | FR-009 |
| Scenario ID | TS-009 |
| Module | Product Details (MA-RS-001 §6.4) |
| Feature | Product Detail Navigation |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — Product Details screen and its elements (image, title, price, rating) are directly observed in MA-AA-001, and the Catalog → Details transition is confirmed in the Navigation Summary |
| Objective | Verify tapping a product card opens Product Details with its confirmed elements |
| Preconditions | Product Catalog screen is displayed (TC-001 executed) |
| Dependencies | TC-001 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap a product card on the Product Catalog screen. | Product Details screen is displayed. |
| 2 | Observe the Product Details screen for the product image. | Product image is displayed. |
| 3 | Observe the Product Details screen for the product title. | Product title is displayed. |
| 4 | Observe the Product Details screen for the product price. | Product price is displayed. |
| 5 | Observe the Product Details screen for the product rating. | Product rating is displayed. |

| Field | Value |
|---|---|
| Post Condition | Product Details screen remains displayed, ready for TC-010, TC-011, TC-012, TC-013, TC-029, TC-032 |
| Requirement Traceability | FR-009 |
| Scenario Traceability | TS-009 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | None |

---

### TC-010 — Product Color Selection

| Field | Value |
|---|---|
| Test Case ID | TC-010 |
| Requirement ID | FR-010 |
| Scenario ID | TS-010 |
| Module | Product Details (MA-RS-001 §6.4) |
| Feature | Product Color Selection |
| Priority | Should |
| Automation Status | Ready |
| Verification Status | Partially Confirmed — color selection control existence is confirmed (MA-AA-001 §5, §10); the precise visual mechanism by which selection state is reflected is not literally described |
| Objective | Verify a color option can be selected and visibly reflects a selected state |
| Preconditions | Product Details screen is displayed (TC-009 executed) |
| Dependencies | TC-009 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap a color selection option on the Product Details screen. | Color selection option responds to activation. |
| 2 | Observe the color selection option after activation. | Selected color option visibly reflects a selected state. |

| Field | Value |
|---|---|
| Post Condition | Selected color state persists on screen |
| Requirement Traceability | FR-010 |
| Scenario Traceability | TS-010 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Reviewer should confirm exact visual indicator of "selected state" during implementation-phase inspection |

---

### TC-011 — Product Quantity Selection

| Field | Value |
|---|---|
| Test Case ID | TC-011 |
| Requirement ID | FR-011 |
| Scenario ID | TS-011 |
| Module | Product Details (MA-RS-001 §6.4) |
| Feature | Product Quantity Selection |
| Priority | Must |
| Automation Status | Blocked — TS-011 Expected Result contains "Out of Current Observation" |
| Verification Status | Partially Confirmed — quantity selector existence is confirmed; exact control mechanics are not confirmed |
| Objective | Verify the quantity selector on Product Details accepts a value change |
| Preconditions | Product Details screen is displayed (TC-009 executed) |
| Dependencies | TC-009 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Interact with the quantity selector on the Product Details screen. | Quantity selector responds to interaction. |
| 2 | Observe the quantity value after interaction. | Pending Manual Verification — exact control mechanics not confirmed by MA-RS-001 FR-011. |

| Field | Value |
|---|---|
| Post Condition | Pending Manual Verification |
| Requirement Traceability | FR-011 |
| Scenario Traceability | TS-011 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Requires manual verification pass per MA-TS-001 §8 |

---

### TC-012 — Add to Cart Action

| Field | Value |
|---|---|
| Test Case ID | TC-012 |
| Requirement ID | FR-012 |
| Scenario ID | TS-012 |
| Module | Product Details (MA-RS-001 §6.4) |
| Feature | Add to Cart Action |
| Priority | Must |
| Automation Status | Blocked — TS-012 Expected Result contains "Out of Current Observation" |
| Verification Status | Partially Confirmed — Add to Cart button existence is confirmed; resulting destination/state change is not confirmed |
| Objective | Verify the Add to Cart button is interactable and triggers a state change |
| Preconditions | Product Details screen is displayed (TC-009 executed) |
| Dependencies | TC-009 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap the Add to Cart button on the Product Details screen. | Add to Cart button responds to activation. |
| 2 | Observe the application state after activation. | Pending Manual Verification — resulting screen/state not confirmed by MA-RS-001 FR-012. |

| Field | Value |
|---|---|
| Post Condition | Pending Manual Verification |
| Requirement Traceability | FR-012 |
| Scenario Traceability | TS-012 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Downstream Test Cases (TC-007, TC-014, TC-031) treat this action as their precondition despite its Blocked status; resolving this ambiguity is a priority manual-verification item |

---

### TC-013 — Product Details Scroll Support

| Field | Value |
|---|---|
| Test Case ID | TC-013 |
| Requirement ID | FR-013 |
| Scenario ID | TS-013 |
| Module | Product Details (MA-RS-001 §6.4) |
| Feature | Product Details Scroll Support |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — scrollable content and Product Highlights are both directly observed together on the Product Details screen in MA-AA-001 |
| Objective | Verify scrolling the Product Details screen reveals Product Highlights |
| Preconditions | Product Details screen is displayed (TC-009 executed) |
| Dependencies | TC-009 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Scroll down on the Product Details screen. | Screen content scrolls. |
| 2 | Observe the screen after scrolling. | Product Highlights content is displayed. |

| Field | Value |
|---|---|
| Post Condition | Product Highlights remain visible |
| Requirement Traceability | FR-013 |
| Scenario Traceability | TS-013 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | None |

---

### TC-014 — Cart Screen Access

| Field | Value |
|---|---|
| Test Case ID | TC-014 |
| Requirement ID | FR-014 |
| Scenario ID | TS-014 |
| Module | Cart (MA-RS-001 §6.5) |
| Feature | Cart Screen Access |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — Cart screen and the cart-icon navigation path are directly confirmed in MA-AA-001 Navigation Summary |
| Objective | Verify tapping the cart icon opens the Cart screen with items and total displayed |
| Preconditions | An item has been added to the Cart (TC-012 executed) |
| Dependencies | TC-012 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap the cart icon on the Product Catalog screen. | Cart screen is displayed. |
| 2 | Observe the Cart screen for cart items. | Cart items are displayed. |
| 3 | Observe the Cart screen for the total. | Total is displayed. |

| Field | Value |
|---|---|
| Post Condition | Cart screen remains displayed, ready for TC-015, TC-016, TC-017, TC-018 |
| Requirement Traceability | FR-014 |
| Scenario Traceability | TS-014 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Smoke (MA-TP-001 §10 names Cart access as part of the Smoke stage) |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | None |

---

### TC-015 — Cart Item Quantity Update

| Field | Value |
|---|---|
| Test Case ID | TC-015 |
| Requirement ID | FR-015 |
| Scenario ID | TS-015 |
| Module | Cart (MA-RS-001 §6.5) |
| Feature | Cart Item Quantity Update |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Partially Confirmed — quantity controls and total are each confirmed as dynamic elements in MA-AA-001 §10; the causal recalculation between the two is inferred, not literally observed |
| Objective | Verify updating item quantity on the Cart screen recalculates the displayed total |
| Preconditions | Cart screen is displayed with at least one item (TC-014 executed) |
| Dependencies | TC-014 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Update the quantity of a Cart item using the quantity control. | Quantity control responds to the update. |
| 2 | Observe the Cart total after the update. | Cart total displays an updated value. |

| Field | Value |
|---|---|
| Post Condition | Cart total reflects updated quantity |
| Requirement Traceability | FR-015 |
| Scenario Traceability | TS-015 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Causal relationship between quantity change and total recalculation is a reasonable inference from MA-AA-001, not a literal observation; flagged for reviewer awareness |

---

### TC-016 — Cart Item Removal

| Field | Value |
|---|---|
| Test Case ID | TC-016 |
| Requirement ID | FR-016 |
| Scenario ID | TS-016 |
| Module | Cart (MA-RS-001 §6.5) |
| Feature | Cart Item Removal |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Partially Confirmed — Remove Item control is confirmed in MA-AA-001; the resulting update to the cart summary upon removal is inferred, not literally observed |
| Objective | Verify the Remove Item action removes the selected item and updates the cart summary |
| Preconditions | Cart screen is displayed with at least one item (TC-014 executed) |
| Dependencies | TC-014 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap the Remove Item action for a Cart item. | Item is removed from the Cart. |
| 2 | Observe the Cart summary after removal. | Cart summary displays the updated item list. |

| Field | Value |
|---|---|
| Post Condition | Cart summary reflects removal |
| Requirement Traceability | FR-016 |
| Scenario Traceability | TS-016 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | None |

---

### TC-017 — Cart Total Verification

| Field | Value |
|---|---|
| Test Case ID | TC-017 |
| Requirement ID | FR-017 |
| Scenario ID | TS-017 |
| Module | Cart (MA-RS-001 §6.5) |
| Feature | Cart Total Verification |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Partially Confirmed — Cart total display is confirmed in MA-AA-001; the exact price × quantity summation logic is inferred, not literally documented as an observed formula |
| Objective | Verify the Cart total reflects the sum of item price and quantity as displayed |
| Preconditions | Cart screen is displayed with at least one item (TC-014 executed) |
| Dependencies | TC-014 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Observe the price and quantity of each item on the Cart screen. | Price and quantity are displayed for each item. |
| 2 | Observe the displayed Cart total. | Cart total is displayed. |
| 3 | Compare the displayed total to the sum of displayed item price and quantity values. | Displayed total matches the calculated sum of displayed item values. |

| Field | Value |
|---|---|
| Post Condition | Cart total verified against displayed line items |
| Requirement Traceability | FR-017 |
| Scenario Traceability | TS-017 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | None |

---

### TC-018 — Proceed to Checkout Action

| Field | Value |
|---|---|
| Test Case ID | TC-018 |
| Requirement ID | FR-018 |
| Scenario ID | TS-018 |
| Module | Cart (MA-RS-001 §6.5) |
| Feature | Proceed to Checkout Action |
| Priority | Must |
| Automation Status | Blocked — TS-018 Expected Result contains "Out of Current Observation" |
| Verification Status | Partially Confirmed — Proceed to Checkout button is confirmed; the next screen and its sequence relative to Login is not confirmed |
| Objective | Verify the Proceed to Checkout button is interactable and advances the flow |
| Preconditions | Cart screen is displayed with at least one item (TC-014 executed) |
| Dependencies | TC-014 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap the Proceed to Checkout button on the Cart screen. | Proceed to Checkout button responds to activation. |
| 2 | Observe the screen displayed after activation. | Pending Manual Verification — exact next screen and sequence relative to Login not confirmed by MA-RS-001 FR-018. |

| Field | Value |
|---|---|
| Post Condition | Pending Manual Verification |
| Requirement Traceability | FR-018 |
| Scenario Traceability | TS-018 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Downstream Checkout Test Cases (TC-019–TC-027) assume this transition succeeds; resolving this ambiguity is a priority manual-verification item |

---

### TC-019 — Shipping Address Screen Access

| Field | Value |
|---|---|
| Test Case ID | TC-019 |
| Requirement ID | FR-019 |
| Scenario ID | TS-019 |
| Module | Checkout — Shipping (MA-RS-001 §6.6) |
| Feature | Shipping Address Screen Access |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — all seven Shipping Address fields are directly listed as observed in MA-AA-001 |
| Objective | Verify the Shipping Address screen displays all required fields |
| Preconditions | Checkout flow initiated (TC-018 executed) |
| Dependencies | TC-018 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Observe the Shipping Address screen for the Full Name field. | Full Name field is displayed. |
| 2 | Observe the Shipping Address screen for the Address Line 1 field. | Address Line 1 field is displayed. |
| 3 | Observe the Shipping Address screen for the Address Line 2 field. | Address Line 2 field is displayed. |
| 4 | Observe the Shipping Address screen for the City field. | City field is displayed. |
| 5 | Observe the Shipping Address screen for the State/Region field. | State/Region field is displayed. |
| 6 | Observe the Shipping Address screen for the Zip Code field. | Zip Code field is displayed. |
| 7 | Observe the Shipping Address screen for the Country field. | Country field is displayed. |

| Field | Value |
|---|---|
| Post Condition | Shipping Address screen remains displayed, ready for TC-020 |
| Requirement Traceability | FR-019 |
| Scenario Traceability | TS-019 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | None |

---

### TC-020 — Shipping Address Data Entry

| Field | Value |
|---|---|
| Test Case ID | TC-020 |
| Requirement ID | FR-020 |
| Scenario ID | TS-020 |
| Module | Checkout — Shipping (MA-RS-001 §6.6) |
| Feature | Shipping Address Data Entry |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — all seven fields are classified as input fields in MA-AA-001 §9 |
| Objective | Verify each Shipping Address field accepts text input |
| Preconditions | Shipping Address screen is displayed (TC-019 executed) |
| Dependencies | TC-018 (MA-TD-001 §10) |
| Test Data | Shipping Address — Reference: Pending Test Data Design (Future MA-TDD-001) |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Enter a value into the Full Name field. | Full Name field contains the entered value. |
| 2 | Enter a value into the Address Line 1 field. | Address Line 1 field contains the entered value. |
| 3 | Enter a value into the Address Line 2 field. | Address Line 2 field contains the entered value. |
| 4 | Enter a value into the City field. | City field contains the entered value. |
| 5 | Enter a value into the State/Region field. | State/Region field contains the entered value. |
| 6 | Enter a value into the Zip Code field. | Zip Code field contains the entered value. |
| 7 | Enter a value into the Country field. | Country field contains the entered value. |

| Field | Value |
|---|---|
| Post Condition | All Shipping Address fields populated |
| Requirement Traceability | FR-020 |
| Scenario Traceability | TS-020 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Field values are not invented; execution requires MA-TDD-001 |

---

### TC-021 — Proceed to Payment Action

| Field | Value |
|---|---|
| Test Case ID | TC-021 |
| Requirement ID | FR-021 |
| Scenario ID | TS-021 |
| Module | Checkout — Shipping (MA-RS-001 §6.6) |
| Feature | Proceed to Payment Action |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — the "To Payment" → Payment Method transition is directly confirmed in MA-AA-001 Navigation Summary |
| Objective | Verify the "To Payment" button navigates to the Payment Method screen |
| Preconditions | Shipping Address fields are populated (TC-020 executed) |
| Dependencies | TC-018 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap the "To Payment" button on the Shipping Address screen. | Payment Method screen is displayed. |

| Field | Value |
|---|---|
| Post Condition | Payment Method screen displayed, ready for TC-022, TC-023 |
| Requirement Traceability | FR-021 |
| Scenario Traceability | TS-021 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | None |

---

### TC-022 — Payment Method Screen Access

| Field | Value |
|---|---|
| Test Case ID | TC-022 |
| Requirement ID | FR-022 |
| Scenario ID | TS-022 |
| Module | Checkout — Payment (MA-RS-001 §6.7) |
| Feature | Payment Method Screen Access |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — Payment Method screen and card entry fields are directly observed in MA-AA-001 |
| Objective | Verify the Payment Method screen displays card entry fields |
| Preconditions | Navigated from Shipping Address screen (TC-021 executed) |
| Dependencies | TC-021 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Observe the Payment Method screen for card entry fields. | Card entry fields are displayed. |

| Field | Value |
|---|---|
| Post Condition | Payment Method screen remains displayed, ready for TC-023 |
| Requirement Traceability | FR-022 |
| Scenario Traceability | TS-022 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | None |

---

### TC-023 — Payment Card Data Entry

| Field | Value |
|---|---|
| Test Case ID | TC-023 |
| Requirement ID | FR-023 |
| Scenario ID | TS-023 |
| Module | Checkout — Payment (MA-RS-001 §6.7) |
| Feature | Payment Card Data Entry |
| Priority | Must |
| Automation Status | Blocked — TS-023 Expected Result contains "Out of Current Observation" |
| Verification Status | Partially Confirmed — card entry fields exist and accept input generically; individual field breakdown (card number, expiry, CVV, etc.) is not confirmed |
| Objective | Verify card entry fields on the Payment Method screen accept input |
| Preconditions | Payment Method screen is displayed (TC-021 executed) |
| Dependencies | TC-021 (MA-TD-001 §10) |
| Test Data | Payment Card — Reference: Pending Test Data Design (Future MA-TDD-001) |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Enter a value into a card entry field on the Payment Method screen. | Card entry field contains the entered value. |
| 2 | Observe the remaining card entry fields. | Pending Manual Verification — individual field breakdown not confirmed by MA-RS-001 FR-023. |

| Field | Value |
|---|---|
| Post Condition | Pending Manual Verification |
| Requirement Traceability | FR-023 |
| Scenario Traceability | TS-023 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Requires manual verification pass per MA-TS-001 §8 to enumerate individual card fields |

---

### TC-024 — Continue to Review Action

| Field | Value |
|---|---|
| Test Case ID | TC-024 |
| Requirement ID | FR-024 |
| Scenario ID | TS-024 |
| Module | Checkout — Payment (MA-RS-001 §6.7) |
| Feature | Continue to Review Action |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — Continue → Review Order transition is directly confirmed in MA-AA-001 Navigation Summary |
| Objective | Verify the Continue action navigates from Payment Method to Review Order |
| Preconditions | Payment details are entered (TC-023 executed) |
| Dependencies | TC-021 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap the Continue button on the Payment Method screen. | Review Order screen is displayed. |

| Field | Value |
|---|---|
| Post Condition | Review Order screen displayed, ready for TC-025 |
| Requirement Traceability | FR-024 |
| Scenario Traceability | TS-024 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | None |

---

### TC-025 — Review Order Screen Verification

| Field | Value |
|---|---|
| Test Case ID | TC-025 |
| Requirement ID | FR-025 |
| Scenario ID | TS-025 |
| Module | Order Review (MA-RS-001 §6.8) |
| Feature | Review Order Screen Verification |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Partially Confirmed — Review Order screen and order summary existence are confirmed; that the summary specifically reflects the earlier Cart/Shipping/Payment entries is inferred, not literally documented as verified data carry-through |
| Objective | Verify the Review Order screen displays an order summary consistent with data entered earlier in the flow |
| Preconditions | Navigated from Payment Method screen (TC-024 executed) |
| Dependencies | TC-024 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Observe the Review Order screen for an order summary. | Order summary is displayed. |
| 2 | Compare the order summary to the Cart, Shipping Address, and Payment data entered earlier in the flow. | Order summary content reflects the data entered earlier in the flow. |

| Field | Value |
|---|---|
| Post Condition | Review Order screen remains displayed, ready for TC-026 |
| Requirement Traceability | FR-025 |
| Scenario Traceability | TS-025 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Step 2's data-consistency comparison is a reasonable inference, not a literal MA-AA-001 observation; flagged for reviewer awareness |

---

### TC-026 — Place Order Action

| Field | Value |
|---|---|
| Test Case ID | TC-026 |
| Requirement ID | FR-026 |
| Scenario ID | TS-026 |
| Module | Order Placement (MA-RS-001 §6.9) |
| Feature | Place Order Action |
| Priority | Must |
| Automation Status | Blocked — TS-026 Expected Result contains "Out of Current Observation" |
| Verification Status | Partially Confirmed — the Place Order action exists; the exact screen hosting it is not confirmed |
| Objective | Verify the Place Order action is triggerable |
| Preconditions | Review Order screen is displayed (TC-025 executed) |
| Dependencies | TC-025 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap the Place Order action. | Place Order action responds to activation. |
| 2 | Observe the screen hosting the Place Order action. | Pending Manual Verification — exact hosting screen not confirmed by MA-RS-001 FR-026. |

| Field | Value |
|---|---|
| Post Condition | Pending Manual Verification |
| Requirement Traceability | FR-026 |
| Scenario Traceability | TS-026 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Requires manual verification pass per MA-TS-001 §8 |

---

### TC-027 — Order Completion Confirmation

| Field | Value |
|---|---|
| Test Case ID | TC-027 |
| Requirement ID | FR-027 |
| Scenario ID | TS-027 |
| Module | Order Placement (MA-RS-001 §6.9) |
| Feature | Order Completion Confirmation |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — the "Checkout Complete" / "Order Completed" success message is directly observed in MA-AA-001 |
| Objective | Verify the Order Completion screen displays a success confirmation after Place Order |
| Preconditions | Place Order action has been triggered (TC-026 executed) |
| Dependencies | TC-025 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Observe the screen displayed after the Place Order action. | Order Completion screen is displayed. |
| 2 | Observe the Order Completion screen for a success message. | "Checkout Complete" / "Order Completed" message is displayed. |

| Field | Value |
|---|---|
| Post Condition | Order Completion screen displayed as the terminal screen of the flow |
| Requirement Traceability | FR-027 |
| Scenario Traceability | TS-027 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Further navigation beyond this screen is Not Observed per MA-AA-001; not in scope for this Test Case |

---

### TC-028 — Navigation Drawer Item Access

| Field | Value |
|---|---|
| Test Case ID | TC-028 |
| Requirement ID | FR-028 |
| Scenario ID | TS-028 |
| Module | Navigation (MA-RS-001 §6.10) |
| Feature | Navigation Drawer Item Access |
| Priority | Should |
| Automation Status | Blocked — TS-028 Expected Result contains "Out of Current Observation" |
| Verification Status | Partially Confirmed — Drawer item list is confirmed; individual destination screens beyond Catalog and Login are not confirmed |
| Objective | Verify each Navigation Drawer item responds to selection |
| Preconditions | Navigation Drawer is open (TC-008 executed) |
| Dependencies | TC-008 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap a Navigation Drawer item other than Catalog or Login. | Navigation Drawer item responds to activation. |
| 2 | Observe the screen displayed after activation. | Pending Manual Verification — resulting screen content not confirmed by MA-RS-001 FR-028. |

| Field | Value |
|---|---|
| Post Condition | Pending Manual Verification |
| Requirement Traceability | FR-028 |
| Scenario Traceability | TS-028 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Requires manual verification pass per MA-TS-001 §8 for each of the eight utility items individually |

---

### TC-029 — Back Navigation Support

| Field | Value |
|---|---|
| Test Case ID | TC-029 |
| Requirement ID | FR-029 |
| Scenario ID | TS-029 |
| Module | Navigation (MA-RS-001 §6.10) |
| Feature | Back Navigation Support |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — the Product Details → Product Catalog back-navigation path is directly confirmed in MA-AA-001 Screen Inventory |
| Objective | Verify back navigation from Product Details returns to Product Catalog |
| Preconditions | Product Details screen is displayed (TC-009 executed) |
| Dependencies | TC-009 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Trigger back navigation from the Product Details screen. | Product Catalog screen is displayed. |

| Field | Value |
|---|---|
| Post Condition | Product Catalog screen displayed |
| Requirement Traceability | FR-029 |
| Scenario Traceability | TS-029 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | None |

---

### TC-030 — Error Handling Coverage (Deferred)

| Field | Value |
|---|---|
| Test Case ID | TC-030 |
| Requirement ID | FR-030 |
| Scenario ID | TS-030 |
| Module | Error Handling (MA-RS-001 §6.11) |
| Feature | Error Handling Coverage |
| Priority | Could |
| Automation Status | Deferred — excluded from execution scope per MA-TS-001 §6 |
| Verification Status | Pending Manual Verification — MA-AA-001 Known Limitations explicitly states error handling was not analyzed; no verifiable error state exists in current evidence |
| Objective | Not Applicable — no verifiable error state exists to design a manual execution step against |
| Preconditions | Not Applicable |
| Dependencies | Not Applicable |
| Test Data | Not Applicable |
| Execution Type | Not Applicable — deferred |
| Environment | Not Applicable |

| Step | Action | Expected Result |
|---|---|---|
| — | Not Applicable — no verifiable error state exists in the evidence base (MA-AA-001 Known Limitations; MA-TD-001 TS-030). | Not Applicable. |

| Field | Value |
|---|---|
| Post Condition | Not Applicable |
| Requirement Traceability | FR-030 |
| Scenario Traceability | TS-030 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Not Applicable — Excluded from Execution |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Retained only for traceability to FR-030; requires a dedicated evidence-gathering pass before it can become an executable Test Case |

---

### TC-031 — Dynamic Element State Verification (Cart)

| Field | Value |
|---|---|
| Test Case ID | TC-031 |
| Requirement ID | FR-031 |
| Scenario ID | TS-031 |
| Module | State Management (MA-RS-001 §6.12) |
| Feature | Dynamic Element State Verification — Cart |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Partially Confirmed — cart badge and cart total existence as dynamic elements are confirmed (MA-AA-001 §10); their exact real-time matching to cart contents is inferred |
| Objective | Verify cart badge and cart total update to reflect the current Cart state |
| Preconditions | An item has been added to the Cart (TC-012 executed) |
| Dependencies | TC-012 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Observe the cart badge value. | Cart badge displays a value. |
| 2 | Observe the cart total value. | Cart total displays a value. |
| 3 | Compare both values to the current Cart contents. | Cart badge and total values match the current Cart contents. |

| Field | Value |
|---|---|
| Post Condition | Dynamic elements reflect current Cart state |
| Requirement Traceability | FR-031 |
| Scenario Traceability | TS-031 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Same inference caveat as TC-007/TC-017 regarding badge/total semantics |

---

### TC-032 — Dynamic Element State Verification (Product Details)

| Field | Value |
|---|---|
| Test Case ID | TC-032 |
| Requirement ID | FR-031 |
| Scenario ID | TS-032 |
| Module | State Management (MA-RS-001 §6.12) |
| Feature | Dynamic Element State Verification — Product Details |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Partially Confirmed — quantity and color selection existence as dynamic elements are confirmed (MA-AA-001 §10); their exact persistence-of-selection behavior is inferred |
| Objective | Verify quantity and color selection controls reflect the currently selected state |
| Preconditions | Product Details screen is displayed (TC-009 executed) |
| Dependencies | TC-009 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Select a quantity value on the Product Details screen. | Quantity selector displays the selected value. |
| 2 | Select a color option on the Product Details screen. | Color selection displays the selected option. |
| 3 | Observe both controls after selection. | Displayed quantity and color selection match the most recent user selection. |

| Field | Value |
|---|---|
| Post Condition | Dynamic elements reflect most recent selection |
| Requirement Traceability | FR-031 |
| Scenario Traceability | TS-032 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Same inference caveat as TC-010/TC-011 regarding exact control mechanics |

---

## 7. Coverage Summary

| Category | FR Count | TC Count |
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

## 8. Requirement → Scenario → Test Case Traceability Matrix

| FR | TS | TC |
|---|---|---|
| FR-001 | TS-001 | TC-001 |
| FR-002 | TS-002 | TC-002 |
| FR-003 | TS-003 | TC-003 |
| FR-004 | TS-004 | TC-004 |
| FR-005 | TS-005 | TC-005 |
| FR-006 | TS-006 | TC-006 |
| FR-007 | TS-007 | TC-007 |
| FR-008 | TS-008 | TC-008 |
| FR-009 | TS-009 | TC-009 |
| FR-010 | TS-010 | TC-010 |
| FR-011 | TS-011 | TC-011 |
| FR-012 | TS-012 | TC-012 |
| FR-013 | TS-013 | TC-013 |
| FR-014 | TS-014 | TC-014 |
| FR-015 | TS-015 | TC-015 |
| FR-016 | TS-016 | TC-016 |
| FR-017 | TS-017 | TC-017 |
| FR-018 | TS-018 | TC-018 |
| FR-019 | TS-019 | TC-019 |
| FR-020 | TS-020 | TC-020 |
| FR-021 | TS-021 | TC-021 |
| FR-022 | TS-022 | TC-022 |
| FR-023 | TS-023 | TC-023 |
| FR-024 | TS-024 | TC-024 |
| FR-025 | TS-025 | TC-025 |
| FR-026 | TS-026 | TC-026 |
| FR-027 | TS-027 | TC-027 |
| FR-028 | TS-028 | TC-028 |
| FR-029 | TS-029 | TC-029 |
| FR-030 | TS-030 | TC-030 |
| FR-031 | TS-031, TS-032 | TC-031, TC-032 |

## 9. Automation Readiness Summary

| Automation Status | Count | Test Case IDs |
|---|---|---|
| Ready | 23 | TC-001, 002, 003, 005, 007, 008, 009, 010, 013, 014, 015, 016, 017, 019, 020, 021, 022, 024, 025, 027, 029, 031, 032 |
| Blocked | 8 | TC-004, 006, 011, 012, 018, 023, 026, 028 |
| Deferred | 1 | TC-030 |

| Verification Status | Count |
|---|---|
| Confirmed | 15 |
| Partially Confirmed | 16 |
| Pending Manual Verification | 1 |

Automation of the 8 Blocked Test Cases must not begin until the manual verification pass committed to in MA-TS-001 Section 8 resolves the corresponding ambiguity. TC-030 remains Deferred and out of automation scope per MA-TS-001 Section 6.

## 10. Assumptions

- The AUT's screen structure and behavior remain consistent with MA-AA-001 observations at execution time.
- The elevated-risk items identified in MA-TS-001 Section 8 will be resolved through manual verification before or during automation of the 8 Blocked Test Cases (TC-004, 006, 011, 012, 018, 023, 026, 028).
- Test data values will be supplied by a future MA-TDD-001 (or equivalent) document; none are invented here.
- A single contributor authors, reviews, and later automates these Test Cases, consistent with MA-PV-001 Section 19 (C-6).

## 11. Risks

| Risk | Note |
|---|---|
| 25% of Test Cases (8 of 32) cannot proceed to automation until manual verification | Concentrated in Authentication, Product Details, Cart, Payment, Navigation modules |
| Checkout dependency chain concentration | A failure or ambiguity at TC-018 (Proceed to Checkout, Blocked) blocks confirmation of the entire downstream Checkout chain (TC-019–TC-027) |
| Several "Ready" Test Cases carry Partially Confirmed verification (16 of 32) | These do not literally contain "Out of Current Observation" text and are mechanically Ready, but rely on reasonable inference beyond literal MA-AA-001 statements (flagged individually in each Test Case's Reviewer Notes) |
| Test data remains undocumented | No Test Case involving Login, Shipping, or Payment can be executed until MA-TDD-001 or equivalent exists |
| Scope creep during future automation implementation | Inherited from MA-PV-001 Section 20, R-1 |

## 12. Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-07-29 |
| Reviewed By | Pending | Pending | — |
| Approved By | Pending | Pending | — |
| Document Status | Draft | — | — |

---

End of Document — MA-TC-001, v1.0
